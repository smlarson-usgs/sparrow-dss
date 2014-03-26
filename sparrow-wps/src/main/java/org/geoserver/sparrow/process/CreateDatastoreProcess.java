package org.geoserver.sparrow.process;

import gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
import org.apache.commons.lang.StringUtils;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geotools.data.DataAccess;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geotools.process.ProcessException;
import org.opengis.feature.type.Name;
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
@DescribeProcess(title="CreateDatastoreProcess", version="1.0.0",
		description="Creates datastore based on a shapefile and a dbf file.  OK to re-request layers - they will not be recreated, however.")
public class CreateDatastoreProcess implements SparrowWps, GeoServerProcess {
	Logger log = LoggerFactory.getLogger(CreateDatastoreProcess.class);
	
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
	private static final String FLOWLINE_WORKSPACE_NAME = "sparrow-flowline";
	private static final String CATCHMENT_WORKSPACE_NAME = "sparrow-catchment";
	private static final String FLOWLINE_NAMESPACE = "http://water.usgs.gov/nawqa/sparrow/dss/spatial/" + FLOWLINE_WORKSPACE_NAME;
	private static final String CATCHMENT_NAMESPACE = "http://water.usgs.gov/nawqa/sparrow/dss/spatial/" + CATCHMENT_WORKSPACE_NAME;
	
	private Catalog catalog;
	private JndiTemplate jndiTemplate;
	
	//Self initiated
	private File baseShapefileDir;
	
	public CreateDatastoreProcess(Catalog catalog, JndiTemplate jndiTemplate) {
		this.catalog = catalog;
		this.jndiTemplate = jndiTemplate;
		
		//Check to see if we can access the base shapefile directory
		try {
			getBaseShapefileDirectory();
			getWmsPath();
		} catch (Exception e) {
			log.error("Configuration Error.", e);
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
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="Registers a Sparrow context for mapping.", type=ServiceResponseWrapper.class)
	public ServiceResponseWrapper execute(
			@DescribeParameter(name="contextId", description="The ID of the context to create a store for", min = 1) Integer contextId,
			@DescribeParameter(name="coverageName", description="The name of the coverage,assumed to be a directory in the filesystem GeoServer is running on.", min = 1) String coverageName,
			@DescribeParameter(name="dbfFilePath", description="The path to the dbf file", min = 1) String dbfFilePath,
			@DescribeParameter(name="idFieldInDbf", description="The name of the ID column in the shapefile", min = 1) String idFieldInDbf,
			@DescribeParameter(name="projectedSrs", description="A fully qualified name of an SRS to project to.  If unspecified, EPSG:4326 is used.", min = 0) String projectedSrs
		) throws Exception {
				
				
		//Cleanup the SRS
		projectedSrs = StringUtils.trimToNull(projectedSrs);
		if (projectedSrs == null) projectedSrs = "EPSG:4326";
		
		String fullFlowlineLayerName = FLOWLINE_WORKSPACE_NAME + ":" + contextId;
		String fullCatchmentLayerName = CATCHMENT_WORKSPACE_NAME + ":" + contextId;
		
		//Ensure the flowline layer exists
		LayerInfo flowLayer = catalog.getLayerByName(fullFlowlineLayerName);
		LayerInfo catchLayer = catalog.getLayerByName(fullCatchmentLayerName);
		
		log.debug("Request for layers for contextId {}.  Exists (flow/catch)? {}/{}, coverageName: {}, dbfFile: {}, idField: {}, projectedSrs: {}", 
				new Object[] {contextId, (flowLayer != null), (catchLayer != null), coverageName, dbfFilePath, idFieldInDbf, projectedSrs});
		
		
		if (flowLayer == null) {
			File flowlineShapefile = this.getFlowlineShapefile(coverageName);
			createLayer(FLOWLINE_NAMESPACE, FLOWLINE_WORKSPACE_NAME, contextId, flowlineShapefile, dbfFilePath, idFieldInDbf, projectedSrs);
			log.debug("Request for flowline layer for contextId {} created OK.  Returning layer '{}'", new Object[] {contextId, fullFlowlineLayerName});
		}
		
		if (catchLayer == null) {
			File catchmentShapefile = this.getCatchmentShapefile(coverageName);
			createLayer(CATCHMENT_NAMESPACE, CATCHMENT_WORKSPACE_NAME, contextId, catchmentShapefile, dbfFilePath, idFieldInDbf, projectedSrs);
			log.debug("Request for flowline layer for contextId {} created OK.  Returning layer '{}'", new Object[] {contextId, fullFlowlineLayerName});
		}
		
		//return buildResponse(getWmsPath(), fullFlowlineLayerName, fullCatchmentLayerName);
		SparrowDataLayerResponse resp = new SparrowDataLayerResponse();
		resp.setCatchLayerName(fullCatchmentLayerName);
		resp.setFlowLayerName(fullFlowlineLayerName);
		resp.setEndpointUrl(getWmsPath());
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper();
		wrap.setEntityClass(SparrowDataLayerResponse.class);
		wrap.setMimeType(ServiceResponseMimeType.XML);
		wrap.setOperation(ServiceResponseOperation.CREATE);
		wrap.setStatus(ServiceResponseStatus.OK);
		wrap.addEntity(resp);
		
		return wrap;
	}
	
	
	/**
	 * Actually creates the layer, working on the assumption that it does not exist.
	 * 
	 * @param namespace
	 * @param workspaceName
	 * @param contextId
	 * @param shapeFile
	 * @param dbfFilePath
	 * @param idFieldInDbf
	 * @param projectedSrs
	 * @return
	 * @throws Exception 
	 */
	protected boolean createLayer(String namespace, String workspaceName, Integer contextId, File shapeFile,
			String dbfFilePath, String idFieldInDbf, String projectedSrs) throws Exception {

		File dbfFile = new File(dbfFilePath);
		
		Map<String, Serializable> dsParams = new HashMap<String, Serializable>();
		dsParams.put("shapefile", shapeFile.toURI().toURL());
		dsParams.put("dbase_file", dbfFile.toURI().toURL());
		dsParams.put("namespace", namespace);
		dsParams.put("dbase_field", idFieldInDbf);
		
		DataStoreInfoImpl info = new DataStoreInfoImpl(catalog);
		info.setType("Dbase Shapefile Joining Data Store");
		info.setWorkspace(catalog.getWorkspaceByName(workspaceName));
		info.setEnabled(true);
		info.setName(contextId.toString());
		info.setConnectionParameters(dsParams);
		
			
        try {
			catalog.add(info);
            DataAccess<? extends FeatureType, ? extends Feature> dataStore = info.getDataStore(new NullProgressListener());
			
			
			List<Name> names = dataStore.getNames();
			Name allData = names.get(0);

			ProjectionPolicy srsHandling = ProjectionPolicy.FORCE_DECLARED;
			
			//Create some cat builder thing for some purpose
			CatalogBuilder cb = new CatalogBuilder(catalog);
			cb.setWorkspace(info.getWorkspace());
			cb.setStore(info);
			FeatureTypeInfo fti = cb.buildFeatureType(dataStore.getFeatureSource(allData));
			fti.setSRS(projectedSrs);
			fti.setName(contextId.toString());
			fti.setTitle(contextId.toString());
			fti.setDescription("A datalayer constructed for a single SPARROW DSS Prediction Context");
			fti.setProjectionPolicy(srsHandling);
			cb.setupBounds(fti);
			LayerInfo li = cb.buildLayer(fti);
			
			catalog.add(fti);
			catalog.add(li);
			return true;

        } catch (IOException e) {
            log.error("Error obtaining new data store", e);
            String message = e.getMessage();
            if (message == null && e.getCause() != null) {
                message = e.getCause().getMessage();
            }
			
            fail("Error creating data store, check the parameters. Err message: " + message);
        }
		
		return false;	//We never actually reach this line
	}
	
	private void fail(String message) throws ProcessException {
		log.error(message);
		throw new ProcessException(message);
	}
	
	/**
	 * Returns the flowline shapefile based on the specified coverage name.
	 * @param coverageName
	 * @return
	 * @throws Exception if the file does not exist or cannot be read.
	 */
	protected File getFlowlineShapefile(String coverageName) throws Exception {
		File base = getBaseShapefileDirectory();
		File coverageDir = new File(base, coverageName);
		File shapeFile = new File(coverageDir, "flowline/coverage.shp");
		
		if (! (shapeFile.exists() && shapeFile.canRead())) {
			throw new Exception("The flowline coverage '" + shapeFile.getAbsolutePath() + "' does not exist or cannot be read.");
		}
		
		return shapeFile;
	}
	
	/**
	 * Returns the catchment shapefile based on the specified coverage name.
	 * @param coverageName
	 * @return
	 * @throws Exception if the file does not exist or cannot be read.
	 */
	protected File getCatchmentShapefile(String coverageName) throws Exception {
		File base = getBaseShapefileDirectory();
		File coverageDir = new File(base, coverageName);
		File shapeFile = new File(coverageDir, "catchment/coverage.shp");
		
		if (! (shapeFile.exists() && shapeFile.canRead())) {
			throw new Exception("The catchment coverage '" + shapeFile.getAbsolutePath() + "' does not exist or cannot be read.");
		}
		
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

				if (shapefileDirPath.startsWith("/")) {
					shapeFileDir = new File(shapefileDirPath);
				} else {

					File home = new File(System.getProperty("user.home"));

					if (shapefileDirPath.startsWith("~" + File.separator)) {
						shapeFileDir = new File(home, shapefileDirPath.substring(2));
					} else {
						shapeFileDir = new File(home, shapefileDirPath);
					}
				}


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

				File home = new File(System.getProperty("user.home"));
				shapeFileDir = new File(home, DEFAULT_SHAPEFILE_DIRECTORY);

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
	
	protected final String getWmsPath() throws Exception {
		try {
			String serverPath = (String)jndiTemplate.lookup(JNDI_KEY_FOR_GEOSERVER_PUBLIC_URL);

			
			if (! serverPath.endsWith("/")) serverPath += "/";
			
			return serverPath + "wms";
			
		} catch(NamingException exception) {


			String msg = "The configuration parameter '" + JNDI_KEY_FOR_GEOSERVER_PUBLIC_URL + "' is unset";

			log.error(msg);
			throw new Exception(msg, exception);

		}
	}
	
}
