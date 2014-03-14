/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.sparrow.process;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
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
import org.geotools.process.ProcessException;

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
		dsParams.put("namespace", "http://www.opengeospatial.net/cite");
		dsParams.put("dbase_field", idFieldInDbf);
		
		DataStoreInfoImpl info = new DataStoreInfoImpl(catalog);
		info.setType("Dbase Shapefile Joining Data Store");
		info.setWorkspace(catalog.getWorkspaceByName("cite"));
		info.setEnabled(true);
		info.setName(contextId.toString());
		info.setConnectionParameters(dsParams);
		
			
        try {
            DataAccess<? extends FeatureType, ? extends Feature> dataStore = info.getDataStore(new NullProgressListener());
            dataStore.dispose();
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
}
