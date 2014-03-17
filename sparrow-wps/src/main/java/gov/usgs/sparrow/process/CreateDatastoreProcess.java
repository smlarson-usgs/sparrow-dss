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
import org.geoserver.wps.gs.GeoServerProcess;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.catalog.DataStoreInfo;
import org.geotools.data.DataAccess;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.apache.log4j.Logger;
import org.geoserver.catalog.LayerInfo;
import org.geotools.data.DataStore;
import org.geotools.data.DataUtilities;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.Transaction;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureIterator;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.feature.FeatureIterator;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.geotools.feature.simple.SimpleFeatureTypeBuilder;
import org.geotools.process.ProcessException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.Name;

/**
 *
 * @author eeverman
 */
@DescribeProcess(title="CreateDatastoreProcess", version="1.0.0",
		description="Creates datastore based on a shapefile and a dbf file.")
public class CreateDatastoreProcess implements SparrowWps, GeoServerProcess {
	protected static Logger log = Logger.getLogger(CreateDatastoreProcess.class);
	
	
	private Catalog catalog;
	
	public CreateDatastoreProcess(Catalog catalog) {
		this.catalog = catalog;
	}
	
	@DescribeResult(name="response", description="test")
	public String execute(
			@DescribeParameter(name="contextId", description="The ID of the context to create a store for", min = 1) Integer contextId,
			@DescribeParameter(name="shapefFilePath", description="The path to the shapefile", min = 1) String shapefFilePath,
			@DescribeParameter(name="dbfFilePath", description="The path to the dbf file", min = 1) String dbfFilePath,
			@DescribeParameter(name="idFieldInDbf", description="The name of the ID column in the shapefile", min = 1) String idFieldInDbf
		) throws Exception {

		
		File shpFile = new File(shapefFilePath);
		File dbfFile = new File(dbfFilePath);
		
		
		if (! shpFile.exists()) {
			fail("The shape file '" + shapefFilePath + "' does not exist.");
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
			for (Name n : names) {
				System.out.println(n.getLocalPart());
				FeatureIterator<? extends Feature> fi = dataStore.getFeatureSource(n).getFeatures().features();
				while (fi.hasNext()) {
					Feature f = fi.next();
					System.out.println(f.getType().getName().getLocalPart() + ", " + f.getIdentifier().getID());
				}
			}
			//dataStore.getNames()
            //dataStore.dispose();
        } catch (IOException e) {
            log.error("Error obtaining new data store", e);
            String message = e.getMessage();
            if (message == null && e.getCause() != null) {
                message = e.getCause().getMessage();
            }
			
            fail("Error creating data store, check the parameters. Err message: " + message);
        }
		
		return "OK";
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
