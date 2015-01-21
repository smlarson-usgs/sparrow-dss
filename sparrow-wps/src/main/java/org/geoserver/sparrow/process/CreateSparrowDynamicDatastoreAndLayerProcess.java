package org.geoserver.sparrow.process;

import gov.usgs.cida.sparrow.service.util.NamingConventions;
import static gov.usgs.cida.sparrow.service.util.NamingConventions.convertContextIdToXMLSafeName;
import gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.naming.NamingException;

import org.apache.commons.lang.StringUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jndi.JndiTemplate;

		
/**
 * WPS to add a layer to GeoServer based on an export dbf file from the user's
 * prediction context and a shapefile.
 * 
 * This WPS is expecting to shapefiles structured on the server as shown:
 * <pre>
 *	|-sparrow_data (this directory is configured in jndi as 'shapefile-directory')
 *		|-shapefile
 *			|-MRB_1_NHD (this directory passed as 'coverageName' execution arg)
 *				|-flowline
 *					|-coverage.shp
 *					|-coverage.shp.xml
 *					|- ... (other files associated w/ the shapefile)
 *				|-catchment
 *					|-coverage.shp (all shapefiles have the 'coverage' name)
 *					|- ... (other files associated w/ the shapefile)
 *	|-MRB_2_E2RF1...
 * </pre>
 * 
 * For a single execution, a flowline and catchment layer are registered, each
 * in a separate namespace.
 * 
 * The response is an XML document:
 * <pre>
 *	&lt;sparrow-wps-response service-name=\"CreateDatastoreProcess\"&gt;
 *		&lt;status&gt;OK&lt;/status&gt;
 *		&lt;wms-endpoint&gt;
 *			&lt;url&gt;url to access the WMS service at&lt;/url&gt;
 *			&lt;flowline-layer&gt;fully qualified layer name for the flowline layer&lt;/flowline-layer&gt;
 *			&lt;catchment-layer&gt;fully qualified layer name for the catchment layer&lt;/catchment-layer&gt;
 *		&lt;/wms-endpoint&gt;
 *	&lt;sparrow-wps-response/&gt;
 * </pre>
 * @author eeverman
 */
@DescribeProcess(title="CreateSparrowDynamicDatastoreAndLayerProcess", version="1.0.0",
		description="Creates datastore based on a shapefile and a dbf file.  OK to re-request layers - they will not be recreated, however.")
public class CreateSparrowDynamicDatastoreAndLayerProcess implements SparrowWps, GeoServerProcess {
	Logger log = LoggerFactory.getLogger(CreateSparrowDynamicDatastoreAndLayerProcess.class);
	
	/**
	 * JNDI key to lookup the base path containing shapefiles.
	 * The directory must exist or an exception will be thrown.  The configured
	 * path will be interpreted as relative to the user home (tomcat user) if
	 * the path is relative or begins with "~/" (nix)or "~\\" (Win).
	 * Otherwise it will be interpreted as absolute.
	 * 
	 * If unspecified the DEFAULT_SHAPEFILE_DIRECTORY is tried.
	 */
	private static final String JNDI_KEY_FOR_SHAPEFILE_DIRECTORY = "java:comp/env/shapefile-directory";
	
	/**
	 * URL that this server can be contacted at.
	 */
	private static final String JNDI_KEY_FOR_GEOSERVER_PUBLIC_URL = "java:comp/env/geoserver-public-url";
	
	private static final String DEFAULT_SHAPEFILE_DIRECTORY = "~/sparrow_data/shapefile";
	
	private static final String JOIN_COLUMN = "IDENTIFIER";
	
	//Passed at construction
	private Catalog catalog;
	private JndiTemplate jndiTemplate;
	private CreateStyleProcess createStyleProcess;
	private CreateDbfShapefileJoiningDatastoreAndLayerProcess createDbfShapefileJoiningDatastoreAndLayerProcess;
	
	//Self initiated at construction
	private File baseShapefileDir;

	
	public CreateSparrowDynamicDatastoreAndLayerProcess(
			Catalog catalog, 
			JndiTemplate jndiTemplate, 
			CreateStyleProcess createStyleProcess, 
			CreateDbfShapefileJoiningDatastoreAndLayerProcess createDbfShapefileJoiningDatastoreAndLayerProcess) throws Exception {
		this.catalog = catalog;
		this.jndiTemplate = jndiTemplate;
		this.createStyleProcess = createStyleProcess;
		this.createDbfShapefileJoiningDatastoreAndLayerProcess = createDbfShapefileJoiningDatastoreAndLayerProcess;
		
		//Check to see if we can access the base shapefile directory
		try {
			baseShapefileDir = getBaseShapefileDirectory();
			getWmsPath();
		} catch (Exception e) {
			log.error("Configuration Error.", e);
			throw e;
		}
		
		if (baseShapefileDir == null || ! baseShapefileDir.exists() || ! baseShapefileDir.canRead()) {
			log.error("The baseShapefileDir does not exist or cannot be read - see previous error.");
		}
	
	
	}
	
	/**
	 * Requests that the layer be created if it does not already exist.
	 * 
	 * @param contextId
	 * @param coverageName
	 * @param dbfFilePath
	 * @param idFieldInDbf
	 * @param projectedSrs
	 * @param isReusable
	 * @param flowlineStyleUrl
	 * @param catchStyleUrl
	 * @param description
	 * @param overwrite
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="Registers a Sparrow context for mapping.", type=ServiceResponseWrapper.class)
	public ServiceResponseWrapper execute(
			@DescribeParameter(name="contextId", description="The ID of the context to create a store for", min = 1) Integer contextId,
			@DescribeParameter(name="coverageName", description="The name of the coverage,assumed to be a directory in the filesystem GeoServer is running on.", min = 1) String coverageName,
			@DescribeParameter(name="dbfFilePath", description="The path to the dbf file", min = 1) String dbfFilePath,
			@DescribeParameter(name="idFieldInDbf", description="The name of the ID column in the shapefile", min = 1) String idFieldInDbf,
			@DescribeParameter(name="projectedSrs", description="A fully qualified name of an SRS to project to.  If unspecified, EPSG:4326 is used.", min = 0) String projectedSrs,
			@DescribeParameter(name="isReusable", description="If true, the layers created are considered highly likely to be reused, which will put them in a different workspace and enable tile caching.", min = 0, max = 1) Boolean isReusable,
			@DescribeParameter(name="flowlineStyleUrl", description="If isReusable is true, a url MUST be provided so that a default SLD style can be created for the layer.  The url should return a valid SLD document in UTF-8, or just be params for one.", min = 0, max = 1) String flowlineStyleUrl,
			@DescribeParameter(name="catchStyleUrl", description="If isReusable is true, a url MUST be provided so that a default SLD style can be created for the layer.  The url should return a valid SLD document in UTF-8, or just be params for one.", min = 0, max = 1) String catchStyleUrl,
			@DescribeParameter(name="description", description="Text description of the context to help ID the layers in GeoServer.  Used as the abstract. Optional.", min = 0, max = 1) String description,
			@DescribeParameter(name="overwrite", description="If true and there is an existing datastore or layer of the same name/workspace, the existing ones will be replaced by the new. (not implemented)", min = 0, max = 1) Boolean overwrite
		) throws Exception {
		
		UserState state = new UserState();
		
		state.contextId = contextId;
		state.coverageName = coverageName;
		state.dbfFilePath = dbfFilePath;
		state.idFieldInDbf = idFieldInDbf;
		state.projectedSrs = projectedSrs;
		state.isReusable = isReusable;
		state.flowlineStyleUrlStr = flowlineStyleUrl;
		state.catchStyleUrlStr = catchStyleUrl;
		state.description = description;
		state.overwrite = overwrite;

		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper();
		wrap.setEntityClass(SparrowDataLayerResponse.class);
		wrap.setMimeType(ServiceResponseMimeType.XML);
		wrap.setOperation(ServiceResponseOperation.CREATE);
		wrap.setStatus(ServiceResponseStatus.OK);
		
		
		
		log.debug("Request for layers for contextId {}. coverageName: {}, dbfFile: {}, projectedSrs: {}, reusable: {}", 
				new Object[] {state.contextId, state.coverageName, state.dbfFilePath, state.projectedSrs, state.isReusable});
		
		init(state, wrap);
		
		if (! wrap.isOK()) {
			log.error("Unable to create new datastores and layers for contextID " + state.contextId + " b/c the validation failed with this message: {}", new Object[] {wrap.getMessage()});
			return wrap;
		}
		

		try {
			createAll(state, wrap);
		} catch (Exception e) {
			//unexpected exception - likely not logged or status set
			wrap.setStatus(ServiceResponseStatus.FAIL);
			wrap.setError(e);

			String msg = "FAILED:  An unexpected error happened while creating a new datastores and layers for contextID " + state.contextId + ".  Message: " + wrap.getMessage();
			wrap.setMessage(msg);
			log.error(msg, e);
			return wrap;
		}
		
		
		if (wrap.isOK()) {
			log.debug("Request COMPLETE OK for layers for contextId {}", 
					new Object[] {state.contextId});
		} else {
			log.error("FAILED:  Unable to create new datastores and layers for contextID " + state.contextId + ".  Failed during create w/ message: {}", new Object[] {wrap.getMessage()});
			return wrap;
		}

		

		return wrap;
	}
	

	
	/**
	 * Initiate the self-initialized params from the user params and does validation.
	 * @param state
	 * @param wrap
	 */
	private void init(UserState state, ServiceResponseWrapper wrap) {
		
		if (state.overwrite == null) state.overwrite = Boolean.FALSE;
		
		//If not specified, assume its not reusable
		if (state.isReusable == null) state.isReusable = Boolean.FALSE;
		
		//Build complete names
		state.fullFlowlineLayerName = NamingConventions.getFullFlowlineLayerName(state.contextId, state.isReusable);
		state.fullCatchmentLayerName = NamingConventions.getFullCatchmentLayerName(state.contextId, state.isReusable);
		
		
		//Cleanup the SRS
		state.projectedSrs = StringUtils.trimToNull(state.projectedSrs);
		if (state.projectedSrs == null) state.projectedSrs = "EPSG:4326";
		
		//Create style urls if this is a reusable layer
		if (state.isReusable) {
			
			state.flowlineStyleUrlStr = StringUtils.trimToNull(state.flowlineStyleUrlStr);
			state.catchStyleUrlStr = StringUtils.trimToNull(state.catchStyleUrlStr);
			
			if (state.flowlineStyleUrlStr == null || state.catchStyleUrlStr == null) {
				wrap.setStatus(ServiceResponseStatus.FAIL);
				wrap.setMessage("The flowlineStyleUrlStr and the catchStyleUrlStr must be set if the layer is reusable.");
				return;
			}
			
			try {
				
				if (state.flowlineStyleUrlStr.startsWith("http:")) {
					state.flowlineStyleUrl = new URL(state.flowlineStyleUrlStr);
					state.flowlineStyleUrlStr = state.flowlineStyleUrl.toExternalForm();	//normalize
				} else {
					//Assume we just have the sld params params
					String url = getServerPath() + "/rest/sld"
							+ "/workspace/" + NamingConventions.getFlowlineWorkspaceName(true) +
							"/layer/" + NamingConventions.convertContextIdToXMLSafeName(state.contextId)+
							"/reach.sld?" + state.flowlineStyleUrlStr;
					state.flowlineStyleUrl = new URL(url);
					state.flowlineStyleUrlStr = state.flowlineStyleUrl.toExternalForm();	//normalize
				}
				
			} catch (Exception ex) {
				wrap.setStatus(ServiceResponseStatus.FAIL);
				wrap.setMessage("The flowlineStyleUrlStr '" + state.flowlineStyleUrlStr + "' is not a valid url or set of url params");
				return;
			}
			
			try {
				//state.catchStyleUrl = new URL(state.catchStyleUrlStr);
				
				if (state.catchStyleUrlStr.startsWith("http:")) {
					state.catchStyleUrl = new URL(state.catchStyleUrlStr);
					state.catchStyleUrlStr = state.catchStyleUrl.toExternalForm();	//normalize
				} else {
					//Assume we just have the sld params params
					String url = getServerPath() + "/rest/sld"
							+ "/workspace/" + NamingConventions.getCatchmentWorkspaceName(true) +
							"/layer/" + NamingConventions.convertContextIdToXMLSafeName(state.contextId) +
							"/catch.sld?" + state.catchStyleUrlStr;
					state.catchStyleUrl = new URL(url);
					state.catchStyleUrlStr = state.catchStyleUrl.toExternalForm();	//normalize
				}
								
			} catch (Exception ex) {
				wrap.setStatus(ServiceResponseStatus.FAIL);
				wrap.setMessage("The catchStyleUrlStr '" + state.catchStyleUrlStr + "' is not a valid url");
				return;
			}
			
		}
		
		//Check the file references
		state.flowlineShapefile = getFlowlineShapefile(state.coverageName);
		
		if (state.flowlineShapefile == null || ! state.flowlineShapefile.exists() || ! state.flowlineShapefile.canRead()) {
			wrap.setStatus(ServiceResponseStatus.FAIL);
			wrap.setMessage("The flowline coverage '" + state.flowlineShapefile.getAbsolutePath() + "' does not exist or cannot be read.");
			return;
		}
		
		state.catchmentShapefile  = getCatchmentShapefile(state.coverageName);
		if (state.catchmentShapefile == null || ! state.catchmentShapefile.exists() || ! state.catchmentShapefile.canRead()) {
			wrap.setStatus(ServiceResponseStatus.FAIL);
			wrap.setMessage("The catchment coverage '" + state.catchmentShapefile.getAbsolutePath() + "' does not exist or cannot be read.");
			return;
		}
	}
	
	private void createAll(UserState state, ServiceResponseWrapper wrap) throws Exception {
		
		//Leave these as null if the layer is non-reusable
		String flowStyleName = null;
		String catchStyleName = null;
		
		String[] tileCacheFilters = null;
		
		//Build styles if this is a reusable layer
		//TODO:  Using global workspace for styles instead of workspace-specific.
		//This is do to a bug described here:
		//https://www.mail-archive.com/geoserver-users@lists.sourceforge.net/msg18345.html
		//IE, The web cache does not work when the layer style is in a workspace.
		if (state.isReusable) {
			ServiceResponseWrapper flowStyleWrap = createStyleProcess.execute(
					NamingConventions.buildDefaultFlowlineStyleName(state.contextId),
					null, //NamingConventions.getFlowlineWorkspaceName(true),
					null,
					state.flowlineStyleUrlStr,
					state.overwrite);
			
			if (flowStyleWrap.isOK()) {
				//flowStyleName = NamingConventions.getFlowlineWorkspaceName(true) + ":" + ((StyleResponse) (flowStyleWrap.getEntityList().get(0))).getStyleName();
				flowStyleName = ((StyleResponse) (flowStyleWrap.getEntityList().get(0))).getStyleName();
			} else {
				wrap.inheritChainedFailure("Unable to create datastore or layers because the creation of a style failed", flowStyleWrap);
				return;
			}
			
			ServiceResponseWrapper catchStyleWrap = createStyleProcess.execute(
					NamingConventions.buildDefaultCatchmentStyleName(state.contextId),
					null, //NamingConventions.getCatchmentWorkspaceName(true),
					null,
					state.catchStyleUrlStr,
					state.overwrite);
			
			if (catchStyleWrap.isOK()) {
				//catchStyleName = NamingConventions.getCatchmentWorkspaceName(true) + ":" + ((StyleResponse) (catchStyleWrap.getEntityList().get(0))).getStyleName();
				catchStyleName = ((StyleResponse) (catchStyleWrap.getEntityList().get(0))).getStyleName();
			} else {
				wrap.inheritChainedFailure("Unable to create datastore or layers because the creation of a style failed", flowStyleWrap);
				return;
			}
			
			//Tile cache filter parameter that forces some rendering options
			//we need for layers to use solid colors even when zoomed out.
			tileCacheFilters = new String[] {
				"format_options",
				"regex",
				"antialiasing:none;quantizer:octree;",
				".*"
			};
		}
		
		ServiceResponseWrapper flowLayerWrap = createDbfShapefileJoiningDatastoreAndLayerProcess.execute(
				NamingConventions.convertContextIdToXMLSafeName(state.contextId),
				NamingConventions.getFlowlineWorkspaceName(state.isReusable),
				state.flowlineShapefile.getAbsolutePath(),
				state.dbfFilePath,
				JOIN_COLUMN,
				state.projectedSrs,
				flowStyleName,
				tileCacheFilters,
				state.description,
				state.overwrite);
		
		if (!flowLayerWrap.isOK()) {
			wrap.inheritChainedFailure("Unable to create the flowline datastore or layer (catchment not attempted)", flowLayerWrap);
			return;
		}
		
		ServiceResponseWrapper catchLayerWrap = createDbfShapefileJoiningDatastoreAndLayerProcess.execute(
				NamingConventions.convertContextIdToXMLSafeName(state.contextId),
				NamingConventions.getCatchmentWorkspaceName(state.isReusable),
				state.catchmentShapefile.getAbsolutePath(),
				state.dbfFilePath,
				JOIN_COLUMN,
				state.projectedSrs,
				catchStyleName,
				tileCacheFilters,
				state.description,
				state.overwrite);

		if (!catchLayerWrap.isOK()) {
			wrap.inheritChainedFailure("Unable to create the catchment datastore or layer (flowline already created)", flowLayerWrap);
			return;
		}
		
		//Set webcaching properties on these newly added layers
		
		
		
		SparrowDataLayerResponse resp = new SparrowDataLayerResponse();
		resp.setFlowLayerName(state.fullFlowlineLayerName);
		if (flowStyleName != null) {
			//See bug noted above
			resp.setFlowLayerDefaultStyleName(flowStyleName);
			//resp.setFlowLayerDefaultStyleName(NamingConventions.getFlowlineWorkspaceName(true) + ":" + flowStyleName);
		}
		resp.setCatchLayerName(state.fullCatchmentLayerName);
		if (catchStyleName != null) {
			//See bug noted above
			resp.setCatchLayerDefaultStyleName(catchStyleName);
			//resp.setCatchLayerDefaultStyleName(NamingConventions.getCatchmentWorkspaceName(true) + ":" + catchStyleName);
		}
		resp.setEndpointUrl(getWmsPath());
		wrap.addEntity(resp);
	}
	
	
	/**
	 * Returns the flowline shapefile based on the specified coverage name.
	 * @param coverageName
	 * @return
	 */
	protected File getFlowlineShapefile(String coverageName) {
		File coverageDir = new File(baseShapefileDir, coverageName);
		File shapeFile = new File(coverageDir, "flowline/coverage.shp");
		
		return shapeFile;
	}
	
	/**
	 * Returns the catchment shapefile based on the specified coverage name.
	 * @param coverageName
	 * @return
	 */
	protected File getCatchmentShapefile(String coverageName) {
		File coverageDir = new File(baseShapefileDir, coverageName);
		File shapeFile = new File(coverageDir, "catchment/coverage.shp");
		
		return shapeFile;
	}
	
	/**
	 * Finds the configured or default base directory for the base directory containing
	 * all the shape files for the models.
	 * 
	 * The directory must exist or an exception will be thrown.  The configured
	 * path will be interpreted as relative to the user home (tomcat user) if
	 * the path is relative or begins with "~/" (nix)or "~\\" (Win).
	 * Otherwise it will be interpreted as absolute.
	 * 
	 * @return
	 * @throws Exception 
	 */
	protected synchronized final File getBaseShapefileDirectory() throws Exception {
		
		if (baseShapefileDir == null) {

			File shapeFileDir = null;

			try {
				String shapefileDirPath = (String)jndiTemplate.lookup(JNDI_KEY_FOR_SHAPEFILE_DIRECTORY);

				shapeFileDir = getFileReference(shapefileDirPath);

				if (! (shapeFileDir.exists() && shapeFileDir.canRead())) {

					String msg = "The shapefile location '" + shapefileDirPath + 
							"' does not exist or is unreadable.";
					log.error(msg);
					throw new Exception(msg);
				} else {
					log.debug("Using the configured shapefile directory: " + shapeFileDir.getAbsolutePath());
				}
			} catch(NamingException exception) {
				//Try the default location

				shapeFileDir = getFileReference(DEFAULT_SHAPEFILE_DIRECTORY);

				if (! (shapeFileDir.exists() && shapeFileDir.canRead())) {

					String msg = "The configuration parameter 'java:comp/env/shapefile-directory'"
							+ " is unset and the default location '" + DEFAULT_SHAPEFILE_DIRECTORY + 
							"' does not exist or is unreadable.";

					log.error(msg);
					throw new Exception(msg, exception);
				} else {
					log.debug("Using the default shapefile directory: " + shapeFileDir.getAbsolutePath());
				}
			}

			baseShapefileDir = shapeFileDir;
		}
		
		return baseShapefileDir;
	}
	
	/**
	 * Interprets a file path, interpreting '~/' and relative paths as relative
	 * to the user.home.  Only paths staring with the system separator are
	 * interpreted as absolute.
	 * 
	 * @param path
	 * @return 
	 */
	public static File getFileReference(String path) {
		if (path.startsWith(File.separator)) {
			//Absolute path
			return new File(path);
		} else {
			File home = new File(System.getProperty("user.home"));
			if (path.startsWith("~" + File.separator)) {
				return new File(home, path.substring(2));
			} else {
				return new File(home, path);
			}
		}
	}
	
	protected final String getServerPath() throws Exception {
		try {
			String serverPath = (String)jndiTemplate.lookup(JNDI_KEY_FOR_GEOSERVER_PUBLIC_URL);

			
			if (serverPath.endsWith("/")) {
				serverPath = serverPath.substring(0, serverPath.length() - 1);
			}
			
			return serverPath;
			
		} catch(NamingException exception) {

			String msg = "The configuration parameter '" + JNDI_KEY_FOR_GEOSERVER_PUBLIC_URL + "' is unset";

			log.error(msg);
			throw new Exception(msg, exception);
		}
	}
	
	protected final String getWmsPath() throws Exception {
		return getServerPath() + "/" + "wms";
	}
	
	/**
	 * The WPS is single instance, so a state class for each user request holds
	 * all info for a single execution.
	 */
	private class UserState {
		//User params, set for each invocation
		Integer contextId;
		String coverageName;
		String dbfFilePath;
		String idFieldInDbf;
		String projectedSrs;
		Boolean isReusable;
		String description;
		String flowlineStyleUrlStr;
		String catchStyleUrlStr;
		Boolean overwrite;

		//Self init for each invocation based on user params
		String fullFlowlineLayerName;	//complete name (workspace:layerName) for the reach layer
		String fullCatchmentLayerName;	//complete name (workspace:layerName) for the catch layer
		File flowlineShapefile;
		File catchmentShapefile;
		URL flowlineStyleUrl;
		URL catchStyleUrl;
	}
	
}
