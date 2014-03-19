/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.sparrow.process;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.naming.NamingException;
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
import org.apache.log4j.Logger;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geotools.process.ProcessException;
import org.opengis.feature.type.Name;
import org.springframework.jndi.JndiTemplate;

/**
 *
 * @author eeverman
 */
@DescribeProcess(title="CreateDatastoreProcess", version="1.0.0",
		description="Creates datastore based on a shapefile and a dbf file.")
public class CreateDatastoreProcess implements SparrowWps, GeoServerProcess {
	protected static Logger log = Logger.getLogger(CreateDatastoreProcess.class);
	
	private static final String DEFAULT_SHAPEFILE_DIRECTORY = System.getProperty("user.home") + "/sparrow/shapefiles/";
	
	private Catalog catalog;
	
	public CreateDatastoreProcess(Catalog catalog) {
		this.catalog = catalog;
	}
	
	@DescribeResult(name="response", description="test")
	public String execute(
			@DescribeParameter(name="contextId", description="The ID of the context to create a store for", min = 1) Integer contextId,
			@DescribeParameter(name="shapefFileName", description="The name of the shapefile, it is assumed that GeoServer has a configured directory to find it.", min = 1) String shapefFileName,
			@DescribeParameter(name="dbfFilePath", description="The path to the dbf file", min = 1) String dbfFilePath,
			@DescribeParameter(name="idFieldInDbf", description="The name of the ID column in the shapefile", min = 1) String idFieldInDbf
		) throws Exception {
		
		
		JndiTemplate template = new JndiTemplate();
		File shapeFileDir = null;
		
		try {
			String shapefileDirPath = (String)template.lookup("java:comp/env/shapefile-directory");
			shapeFileDir = new File(shapefileDirPath);
			if (! (shapeFileDir.exists() && shapeFileDir.canRead())) {
				throw new Exception("The shapefile location '" + shapefileDirPath + 
						"' does not exist or is unreadable.");
			}
		} catch(NamingException exception){
			//Try the default location
			shapeFileDir = new File(DEFAULT_SHAPEFILE_DIRECTORY);
			
			if (! (shapeFileDir.exists() && shapeFileDir.canRead())) {
				throw new Exception("The configuration parameter 'java:comp/env/shapefile-directory'"
						+ " is unset and the default location '" + DEFAULT_SHAPEFILE_DIRECTORY + 
						"' does not exist or is unreadable.", exception);
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
		dsParams.put("namespace", "http://water.usgs.gov/nawqa/sparrow/dss/spatial/sparrow-flowline");
		dsParams.put("dbase_field", idFieldInDbf);
		
		DataStoreInfoImpl info = new DataStoreInfoImpl(catalog);
		info.setType("Dbase Shapefile Joining Data Store");
		info.setWorkspace(catalog.getWorkspaceByName("sparrow-flowline"));
		info.setEnabled(true);
		info.setName(contextId.toString());
		info.setConnectionParameters(dsParams);
		
			
        try {
			catalog.add(info);
            DataAccess<? extends FeatureType, ? extends Feature> dataStore = info.getDataStore(new NullProgressListener());
			
			
			List<Name> names = dataStore.getNames();
			Name allData = names.get(0);

			String targetSRSCode = "EPSG:3857";
			ProjectionPolicy srsHandling = ProjectionPolicy.FORCE_DECLARED;
			
			//Create some cat builder thing for some purpose
			CatalogBuilder cb = new CatalogBuilder(catalog);
			cb.setWorkspace(info.getWorkspace());
			cb.setStore(info);
			FeatureTypeInfo fti = cb.buildFeatureType(dataStore.getFeatureSource(allData));
			fti.setSRS(targetSRSCode);
			fti.setName(contextId.toString());
			fti.setTitle(contextId.toString());
			fti.setDescription("A datalayer constructed for a single SPARROW DSS Prediction Context");
			fti.setProjectionPolicy(srsHandling);
			cb.setupBounds(fti);
			LayerInfo li = cb.buildLayer(fti);
			
			catalog.add(fti);
			catalog.add(li);
			return info.getWorkspace().getName() + ":" + li.getName();

        } catch (IOException e) {
            log.error("Error obtaining new data store", e);
            String message = e.getMessage();
            if (message == null && e.getCause() != null) {
                message = e.getCause().getMessage();
            }
			
            fail("Error creating data store, check the parameters. Err message: " + message);
        }
		
		return "FAILED";	//We never actually reach this line
	}
	
	private void fail(String message) throws Exception {
		throw new ProcessException(message);
	}
	
//	private createStore() {
//		// TODO - This only works for file based data stores. This will not work for 
//		// database-backed datastores
//		storeInfo = catalog.getDataStoreByName(ws.getName(), store);
//		if (storeInfo == null) {
//			LOGGER.log(Level.INFO, "Store {0} not found. Will try to create", store);
//			File dataRoot = dataDir.findWorkspaceDir(ws);
//							if (dataRoot == null) {
//								dataRoot = new File(dataDir.root() + File.separator + "workspaces" + File.separator + ws.getName());
//								org.apache.commons.io.FileUtils.forceMkdir(dataRoot);
//							}
//			String dataDirLocation = dataRoot.getPath();
//			LOGGER.log(Level.INFO, "GEOSERVER_DATA_DIR found @ {0}", dataDirLocation);
//			File storeDirectory = new File(dataRoot, store);
//			if (!storeDirectory.exists()) {
//				LOGGER.log(Level.INFO, "Store directory @ {0} not found. Will try to create", storeDirectory.getPath());
//				storeDirectory.mkdirs();
//			}
//			LOGGER.log(Level.INFO, "Store directory @ {0} created", storeDirectory.getPath());
//			CatalogBuilder builder = new CatalogBuilder(catalog);
//			storeInfo = builder.buildDataStore(store);
//
//			try {
//				storeInfo.getConnectionParameters().put("url", storeDirectory.toURI().toURL().toExternalForm());
//			} catch (MalformedURLException ex) {
//				storeInfo.getConnectionParameters().put("url", "file://" + storeDirectory.getPath());
//			}
//			catalog.add(storeInfo);
//			LOGGER.info("Store created");
//		}
//	}
	
	
}
