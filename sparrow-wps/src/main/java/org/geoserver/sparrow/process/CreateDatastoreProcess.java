package org.geoserver.sparrow.process;

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
 * prediction context, and a specified shapefile.
 * @author eeverman
 */
@DescribeProcess(title="CreateDatastoreProcess", version="1.0.0",
		description="Creates datastore based on a shapefile and a dbf file.  OK to re-request layers - they will not be recreated, however.")
public class CreateDatastoreProcess implements SparrowWps, GeoServerProcess {
	Logger log = LoggerFactory.getLogger(CreateDatastoreProcess.class);
	
	private static final String DEFAULT_SHAPEFILE_DIRECTORY = System.getProperty("user.home") + "/sparrow/shapefiles/";
	private static final String FLOWLINE_WORKSPACE_NAME = "sparrow-flowline";
	private static final String FLOWLINE_NAMESPACE = "http://water.usgs.gov/nawqa/sparrow/dss/spatial/" + FLOWLINE_WORKSPACE_NAME;
	
	private Catalog catalog;
	private JndiTemplate jndiTemplate;
	
	public CreateDatastoreProcess(Catalog catalog, JndiTemplate jndiTemplate) {
		this.catalog = catalog;
		this.jndiTemplate = jndiTemplate;
	}
	
	/**
	 * Requests that the layer be created if it does not already exist.
	 * 
	 * @param contextId
	 * @param shapefFileName
	 * @param dbfFilePath
	 * @param idFieldInDbf
	 * @param projectedSrs
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="test")
	public String execute(
			@DescribeParameter(name="contextId", description="The ID of the context to create a store for", min = 1) Integer contextId,
			@DescribeParameter(name="shapefFileName", description="The name of the shapefile, it is assumed that GeoServer has a configured directory to find it.", min = 1) String shapefFileName,
			@DescribeParameter(name="dbfFilePath", description="The path to the dbf file", min = 1) String dbfFilePath,
			@DescribeParameter(name="idFieldInDbf", description="The name of the ID column in the shapefile", min = 1) String idFieldInDbf,
			@DescribeParameter(name="projectedSrs", description="A fully qualified name of an SRS to project to.  If unspecified, EPSG:4326 is used.", min = 1) String projectedSrs
		) throws Exception {
		
		//Cleanup the SRS
		projectedSrs = StringUtils.trimToNull(projectedSrs);
		if (projectedSrs == null) projectedSrs = "EPSG:4326";
		
		
		String fullLayerName = FLOWLINE_WORKSPACE_NAME + ":" + contextId;
		LayerInfo layer = catalog.getLayerByName(FLOWLINE_WORKSPACE_NAME + ":" + contextId);
		log.debug("Request for layer for contextId {}.  Exists? {}, Shapefile: {}, dbfFile: {}, idField: {}, projectedSrs: {}", 
				new Object[] {contextId, (layer != null), shapefFileName, dbfFilePath, idFieldInDbf, projectedSrs});
		
		if (layer == null) {
			createLayer(contextId, shapefFileName, dbfFilePath, idFieldInDbf, projectedSrs);
			log.debug("Request for contextId {} created OK.  Returning layer '{}'", new Object[] {contextId, fullLayerName});
		}
		
		return fullLayerName;
		
	}
	
	/**
	 * Actually creates the layer, working on the assumption that it does not exist.
	 * 
	 * @param contextId
	 * @param shapefFileName
	 * @param dbfFilePath
	 * @param idFieldInDbf
	 * @param projectedSrs
	 * @return
	 * @throws Exception 
	 */
	protected boolean createLayer(Integer contextId, String shapefFileName,
			String dbfFilePath, String idFieldInDbf, String projectedSrs) throws Exception {

		
		String fullLayerName = FLOWLINE_WORKSPACE_NAME + ":" + contextId;
		catalog.getLayerByName(fullLayerName);
		
		
		File shapeFileDir = null;
		
		try {
			String shapefileDirPath = (String)jndiTemplate.lookup("java:comp/env/shapefile-directory");
			shapeFileDir = new File(shapefileDirPath);
			if (! (shapeFileDir.exists() && shapeFileDir.canRead())) {
				
				String msg = "The shapefile location '" + shapefileDirPath + 
						"' does not exist or is unreadable.";
				log.error(msg);
				throw new Exception(msg);
			}
		} catch(NamingException exception){
			//Try the default location
			shapeFileDir = new File(DEFAULT_SHAPEFILE_DIRECTORY);
			
			if (! (shapeFileDir.exists() && shapeFileDir.canRead())) {
				
				String msg = "The configuration parameter 'java:comp/env/shapefile-directory'"
						+ " is unset and the default location '" + DEFAULT_SHAPEFILE_DIRECTORY + 
						"' does not exist or is unreadable.";
				
				log.error(msg);
				throw new Exception(msg, exception);
			}
		}


		if (! shapefFileName.endsWith(".shp")) shapefFileName+= ".shp";
		
		File shpFile = new File(shapeFileDir, shapefFileName);
		File dbfFile = new File(dbfFilePath);
		
		
		if (! shpFile.exists()) {
			fail("The shape file '" + shapefFileName + "' does not exist in the configured directory '" + shapeFileDir.getAbsolutePath() + "'");
		}
		
		if (! dbfFile.exists()) {
			fail("The dbf file '" + dbfFilePath + "' does not exist.");
		}
		
		Map<String, Serializable> dsParams = new HashMap<String, Serializable>();
		dsParams.put("shapefile", shpFile.toURI().toURL());
		dsParams.put("dbase_file", dbfFile.toURI().toURL());
		dsParams.put("namespace", FLOWLINE_NAMESPACE);
		dsParams.put("dbase_field", idFieldInDbf);
		
		DataStoreInfoImpl info = new DataStoreInfoImpl(catalog);
		info.setType("Dbase Shapefile Joining Data Store");
		info.setWorkspace(catalog.getWorkspaceByName(FLOWLINE_WORKSPACE_NAME));
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
	
}
