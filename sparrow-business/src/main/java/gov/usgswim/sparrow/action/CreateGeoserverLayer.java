package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import gov.usgswim.sparrow.service.SharedApplication;
import java.io.File;
import javax.naming.NamingException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.springframework.jndi.JndiTemplate;

/**
 * Contact the GeoServer spds:CreateDatastore WPS to register a prediction
 * context as a mappable layer(s).
 * 
 * Returns a workspace:layerName
 *
 * @author eeverman
 *
 */
public class CreateGeoserverLayer extends Action<String> {

	public static final String DEFAULT_ENCODING = "UTF-8";
	
	//User init
	private final PredictionContext context;
	private final File dbfFile;
	private final String projectedSrs;
	
	
	//Self Init
	private String geoserverHost;
	private int geoserverPort;
	private String geoserverPath;
	private String shapefileFileName;
	private String idFieldInShapeFileAndDbfFile;
	private boolean isReusable;
	private String description;
	
	/**
	 * Constructs the action w/ all needed parameters
	 * 
	 * @param context A prediction context to construct the map layer for
	 * @param dbfFile Reference to a DBF file that contains ID and value columns.
	 * @param projectedSrs A fully qualified name of an SRS to project to.  If unspecified, GeoServer will default to.
	 */
	public CreateGeoserverLayer(PredictionContext context, File dbfFile, String projectedSrs) {
		this.context = context;
		this.dbfFile = dbfFile;
		this.projectedSrs = projectedSrs;
	}
	
	@Override
	protected void initFields() throws Exception {
	
		ModelRequestCacheKey mrk = new ModelRequestCacheKey(context.getModelID(), false, false, false);
		SparrowModel model = SharedApplication.getInstance().getModelMetadata(mrk).get(0);
		shapefileFileName = model.getThemeName();
		idFieldInShapeFileAndDbfFile = model.getEnhNetworkIdColumn();
		isReusable = context.isLikelyReusable();
		
		if (isReusable) {
			description = "Layer created for model " + context.getModelID() + " and considered a cachable (it has no adjustments or funky analysis)";
		} else {
			description = "Layer created for model " + context.getModelID() + " and considered a non-cachable for one of these reasons: " +
					"Reusable Comparison? " + ((context.getComparison() == null)?"true":context.getComparison().isLikelyReusable()) + " " +
					"Reusable Analysis? " + ((context.getAnalysis() == null)?"true":context.getAnalysis().isLikelyReusable()) + " " +
					"Reusable Terminal Reaches? " + ((context.getTerminalReaches() == null)?"true":context.getTerminalReaches().isLikelyReusable()) + " " +
					"Reusable Adjustment Groups? " + ((context.getAdjustmentGroups() == null)?"true":context.getAdjustmentGroups().isLikelyReusable()) + " " +
					"Reusable Area Of Interest? " + ((context.getAreaOfInterest() == null)?"true":context.getAreaOfInterest().isLikelyReusable());
		}
	}

	@Override
	protected void validate() {

		if (context == null) {
			addValidationError("Context cannot be null");
		}
		
		if (dbfFile == null) {
			addValidationError("DBF File cannot be null");
		} else if (! dbfFile.exists()) {
			addValidationError("DBF must exist");
		}
		
		
		//We need to access these params to check if they exist, so we'll
		//do this initiation here.
		JndiTemplate template = new JndiTemplate();
		
		try {
			geoserverHost = (String)template.lookup("java:comp/env/geoserver-host");
			geoserverPort = (Integer)template.lookup("java:comp/env/geoserver-port");
			geoserverPath = (String)template.lookup("java:comp/env/geoserver-path");
			
			if (! geoserverPath.endsWith("/")) geoserverPath = geoserverPath + "/";
			geoserverPath = geoserverPath + "wps";
			
		} catch(NamingException exception){
			addValidationError("All the configuration parameters must be specified"
					+ " in the Context.xml file: geoserver-host, geoserver-port & geoserver-path");
		} catch (RuntimeException rte) {
			addValidationError("The context configuration parameters must be specified as the correct types: "
					+ "geoserver-host (String), geoserver-port (Integer) & geoserver-path (String)");
		}

		
	}

	
	@Override
	public String doAction() throws Exception {

		String xmlReq = this.getTextWithParamSubstitution("template",
				"contextId", context.getId().toString(), 
				"themeName", shapefileFileName, 
				"dbfFilePath", dbfFile.getAbsolutePath(), 
				"idField", idFieldInShapeFileAndDbfFile,
				"projectedSrs", (projectedSrs == null)?"":projectedSrs,
				"isReusable", Boolean.toString(isReusable),
				"description", description);
		
		
		String response = getQueryResponse(xmlReq);
		
		return response;
	}

	public String getQueryResponse(String thePost) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		URIBuilder uriBuild = new URIBuilder();
		uriBuild.setScheme("http");
		uriBuild.setHost(geoserverHost);
		uriBuild.setPort(geoserverPort);
		uriBuild.setPath(geoserverPath);
		
		//service=wps&version=1.0.0&request=execute&identifier=dss:CreateDatastore
		uriBuild.addParameter("service", "wps");
		uriBuild.addParameter("version", "1.0.0");
		uriBuild.addParameter("request", "execute");
		uriBuild.addParameter("identifier", "dss:CreateDatastore");
		

		
		HttpPost httpPost = new HttpPost(uriBuild.build());
		HttpEntity httpEntity = new StringEntity(thePost, ContentType.APPLICATION_XML);
		httpPost.setEntity(httpEntity);
		httpPost.addHeader("Accept", "application/xml; charset=UTF-8");
		httpPost.addHeader("Accept-Charset", "UTF-8");
		
		log.debug("Requesting to create data layer w/ GeoServer WPS at: " + httpPost.getURI());

		try (CloseableHttpResponse response1 = httpclient.execute(httpPost)) {
			
			HttpEntity entity = response1.getEntity();
			String encoding = findEncoding(entity, "UTF-8");
			String contentType = findContentType(entity, "application/xml");
			String stringFromStream = IOUtils.toString(entity.getContent(), encoding);
			EntityUtils.consume(entity);
			
			if (! contentType.contains("xml")) {
				//We were expecting xml, so anything else is an error.
				throw new Exception("Unexpected response of type '" + entity.getContentType().getValue() + "' :" + stringFromStream);
			}
			
			return stringFromStream;
		}

	}
	
	
	/**
	 * Parses an http contentType header string into the encoding, if it exists.
	 * If it cannot find the encoding, the default is returned.
	 * 
	 * @param entity Find the header in this entity
	 * @param defaultEncoding Return this encoding if we cannot find the value in the header.
	 * @return 
	 */
	protected String findEncoding(HttpEntity entity, String defaultEncoding) {
		
		try {
			return findEncoding(entity.getContentType().getValue(), defaultEncoding);
		} catch (RuntimeException e) {
			return defaultEncoding;
		}
	}
	
	protected String findContentType(HttpEntity entity, String defaultType) {
		try {
			return findContentType(entity.getContentType().getValue(), defaultType);
		} catch (RuntimeException e) {
			return defaultType;
		}
	}
	
	/**
	 * Parses an http contentType header string into the encoding, if it exists.
	 * If it cannot find the encoding, the default is returned.
	 * 
	 * @param contentTypeHeaderString The String value of the http contentType header
	 * @param defaultEncoding Return this encoding if we cannot find the value in the header.
	 * @return 
	 */
	protected String findEncoding(String contentTypeHeaderString, String defaultEncoding) {
		
		try {
			//Example contentType:  text/html; charset=utf-8

			String[] parts = contentTypeHeaderString.split(";");
			String encoding = StringUtils.trimToNull(parts[1]);
			parts = encoding.split("=");
			
			if (parts[0].trim().equalsIgnoreCase("charset")) {
				encoding = StringUtils.trimToNull(parts[1]);
				return encoding.toUpperCase();
			} else {
				return defaultEncoding;
			}
			
		} catch (RuntimeException e) {
			return defaultEncoding;
		}
	}
	
	/**
	 * Parses an http contentType header string to find the content type, if it exists.
	 * If it cannot find the encoding, the default is returned.
	 * 
	 * @param contentTypeHeaderString The String value of the http contentType header
	 * @param defaultType Return this encoding if we cannot find the value in the header.
	 * @return 
	 */
	protected String findContentType(String contentTypeHeaderString, String defaultType) {
		
		try {
			//Example contentType:  text/html; charset=utf-8

			String[] parts = contentTypeHeaderString.split(";");
			String type = StringUtils.trimToNull(parts[0]);
			return type;
			
		} catch (RuntimeException e) {
			return defaultType;
		}
	}

}
