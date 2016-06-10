package org.geoserver.sparrow.process;

import gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


import org.apache.commons.lang.StringUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.FeatureTypeInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.NamespaceInfo;
import org.geoserver.catalog.ProjectionPolicy;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.catalog.impl.DataStoreInfoImpl;
import org.geoserver.gwc.GWC;
import org.geoserver.gwc.layer.GeoServerTileLayer;
import org.geoserver.gwc.layer.GeoServerTileLayerInfo;
import org.geoserver.sparrow.util.GeoServerSparrowLayerSweeper;
import org.geoserver.sparrow.util.SweepResponse;
import org.geoserver.wps.gs.GeoServerProcess;
import org.geotools.data.DataStore;
import org.geotools.data.DataStoreFinder;
import org.geotools.data.postgis.PostGISDialect;
import org.geotools.jdbc.JDBCDataStore;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.jdbc.SQLDialect;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geowebcache.filter.parameters.ParameterFilter;
import org.geowebcache.filter.parameters.RegexParameterFilter;
import org.geowebcache.filter.parameters.StringParameterFilter;
import org.opengis.feature.simple.SimpleFeatureType;

		
/**
 * WPS to add a layer to GeoServer based on an export model_ouput from the user's
 * prediction context as views created in Postgres. The view is exposed as a layer.
 * 
 * 
 * This WPS has been updated to retrieve two views for each model output run from Postgres.  
 * Done to remove the dependency on the multi-dbf-datastore (see SPDSSII-28) and war-overlays that prevented geoserver upgrades.
 * 
 * For a single execution, a flowline and catchment layer are registered, each
 * in a separate namespace. 
 * 
 * (SPDSSII-28)Examples of the names for the shape files that were once called coverage.shp
 * where the flow/catch was derived from the parent file name:
 * sparrow_overlay.mrb02_mrbe2rf1_catch, and sparrow_overlay.mrb02_mrbe2rf1_flow.
 * 
 * Workspace names: sparrow-calibration, sparrow-catchment, sparrow-catchment-reusable, sparrow-flowline, sparrow-flowline-reusable 
 * Stores: Was the same as the layer name. This will change to a single store for Postgres.
 * Styles: layer name plus catchment or flowline plus default : 22N1220785281-catchment-default; spdssi-28 will need to match to the view/layer name.
 * Namespace: http://water.usgs.gov/nawqa/sparrow/dss/spatial/sparrow-flowline  
 * 
 * The response is an XML document:
 * <pre>
 *	&lt;sparrow-wps-response service-name=\"CreateDbfShapefileJoiningDatastoreAndLayerProcess\"&gt;
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
@DescribeProcess(title="CreateDbfShapefileJoiningDatastoreAndLayerProcess", version="1.0.0",
		description="Creates datastore based on a shapefile joined to a dbf file as well as an associated layer.  OK to re-request creation - they will not be recreated, however.")
public class CreateDbfShapefileJoiningDatastoreAndLayerProcess implements SparrowWps, GeoServerProcess {
	Logger log = LoggerFactory.getLogger(CreateDbfShapefileJoiningDatastoreAndLayerProcess.class);
	
	//public static String DBASE_SHAPEFILE_JOIN_DATASTORE_NAME = "Dbase Shapefile Joining Data Store";
	public static String POSTGRES_SHAPEFILE_JOIN_DATASTORE_NAME = "Postgres View Shapefile Joining Data Store";
        
	//Set at construction
	private Catalog catalog;
	private GWC gwc;
	
	public CreateDbfShapefileJoiningDatastoreAndLayerProcess(Catalog catalog, GWC gwc) throws Exception {
		this.catalog = catalog;
		this.gwc = gwc;
		
		if (catalog == null) {
			throw new Exception("Configuration Error - The 1st constructor arg org.geoserver.catalog.Catalog is null");
		}
		
		if (gwc == null) {
			throw new Exception("Configuration Error - The 2nd constructor arg org.geoserver.gwc.GWC is null");
		}
	}
	
	/**
	 * Requests that the layer be created if it does not already exist.
	 * 
	 * @param layerName
	 * @param workspaceName
	 * @param shapeFilePath
	 * @param dbfFilePath
	 * @param idFieldInDbf
	 * @param projectedSrs
	 * @param defaultStyleName
         * @param gwcParamFilters
	 * @param description
	 * @param overwrite
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="Registers a Sparrow context for mapping.", type=ServiceResponseWrapper.class)
	public ServiceResponseWrapper execute(
			@DescribeParameter(name="layerName", description="The layer name to add, without the workspace name", min = 1) String layerName,
			@DescribeParameter(name="workspaceName", description="Name of the workspace to use for the style, which must exist.  Null OK to put in the default namespace", min = 0, max = 1) String workspaceName,
			@DescribeParameter(name="shapeFilePath", description="NO LONGER NEEDED. The complete path on the local machine to a shapefile that the dbf file should be linked to.  Must be parsable into a Java File path.", min = 1) String shapeFilePath,
			@DescribeParameter(name="dbfFilePath", description="NO LONGER NEEDED. The path to the dbf file", min = 1) String dbfFilePath,
			@DescribeParameter(name="idFieldInDbf", description="The name of the ID column in the shapefile (NO LONGER USED - ALWAYS IDENTIFIER)", min = 1) String idFieldInDbf,
			@DescribeParameter(name="projectedSrs", description="A fully qualified name of an SRS to project to.  If unspecified, EPSG:4326 is used.", min = 0) String projectedSrs,
			@DescribeParameter(name="defaultStyleName", description="The name of an existing style to use as the default style w/o the workspace designation  The style must exist in the global workspace or the workspace of the layer..", min = 0) String defaultStyleName,
			@DescribeParameter(name="gwcParamFilters", description="Optional array of GeoWebCache parameter filters, passed in sets of four: 1) Key (what the filter is on, like format_options) 2) type - string or regex 3) default value 4) matched string or regex.", min = 0) String[] gwcParamFilters,
			@DescribeParameter(name="description", description="Text description of the context to help ID the layers in GeoServer.  Used as the abstract. Optional.", min = 0, max = 1) String description,
			@DescribeParameter(name="overwrite", description="If true and there is an existing datastore or layer of the same name/workspace, the existing ones will be replaced by the new. (not implemented)", min = 0, max = 1) Boolean overwrite
	) throws Exception {
			
		UserState state = new UserState();
		
		state.layerName = layerName;
		state.workspaceName = workspaceName;
		state.shapeFilePath = shapeFilePath;  // TODO SPDSSII-28 List past in with the view names created in Postgres
		state.dbfFilePath = dbfFilePath;  
		state.idFieldInDbf = idFieldInDbf;  //  TODO SPDSSII-28 select the rows pertaining to this dbf from model_output (dbfId is the model_output_id = n776208324)
		state.projectedSrs = projectedSrs;
		state.defaultStyleName = defaultStyleName;
		state.gwcParamFilters = gwcParamFilters; 
		state.description = description;
		state.overwrite = overwrite;
	
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper();
		wrap.setEntityClass(SparrowDataLayerResponse.class);
		wrap.setMimeType(ServiceResponseMimeType.XML);
		wrap.setOperation(ServiceResponseOperation.CREATE);
		wrap.setStatus(ServiceResponseStatus.OK);
		
		init(state, wrap);
		
		log.debug("Request to create a new dbf/shapefile joined DataStore and Layer '{}'",  new Object[] {state.fullLayerName});
		
		if (! wrap.isOK()) {
			log.error("DataStore and/or Layer creation failed due to validation error: " + wrap.getMessage());
			return wrap;
		}

			
		try {
			//Check if the layer exists
			LayerInfo layer = catalog.getLayerByName(state.fullLayerName);
                        
			if (layer != null) {
				wrap.setStatus(ServiceResponseStatus.OK_ALREADY_EXISTS);
				LayerResponse resp = new LayerResponse();
				resp.setlayerName(state.fullLayerName);

				log.debug("Request to create new DataStore and Layer '{}', but they already existed.",  new Object[] {state.fullLayerName});
			} else {
				createLayer(state, wrap);
			}
			
		} catch (Exception e) {
			//Unexpected and unhandled error
			wrap.setStatus(ServiceResponseStatus.FAIL);
			wrap.setError(e);

			String msg = "FAILED:  An unexpected error happened during the creation of create DataStore or the Layer '" + state.fullLayerName + "' in workspace " + workspaceName;
			wrap.setMessage(msg);
			log.error(msg, e);
			return wrap;
		}
			
		if (wrap.isOK()) {
			log.debug("Request COMPLETED OK to created a new dbf/shapefile joined DataStore and Layer '{}'",  new Object[] {state.fullLayerName});
		} else {
			log.error("Request to create a new DataStore and Layer '{}' failed.  Message: {}", new Object[] {state.fullLayerName, wrap.getMessage()});
			if (wrap.getError() != null) {
				log.error("Actual Error: ", wrap.getError());
			}
			return wrap;	//No needed, but following the pattern
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
		
		state.fullLayerName = state.workspaceName + ":" + state.layerName;
		
		
		//Cleanup the SRS
		state.projectedSrs = StringUtils.trimToNull(state.projectedSrs);
		if (state.projectedSrs == null) state.projectedSrs = "EPSG:4326";
		
		
		state.workspace = catalog.getWorkspaceByName(state.workspaceName); 
		if (state.workspace == null) {
			wrap.setMessage("The workspace " + state.workspaceName + " does not exist.");
			wrap.setStatus(ServiceResponseStatus.FAIL);
			return;
		}
		
		state.namespace = catalog.getNamespaceByPrefix(state.workspace.getName());
		if (state.namespace == null) {
			wrap.setMessage("The namespace " + state.workspace.getName() + 
					" associated with the workspace of the same name cannot be found.  Configuration error?");
			wrap.setStatus(ServiceResponseStatus.FAIL);
			return;
		}
		
		//Look for the default style first in the specified workspace, then in the global workspace
		if (state.defaultStyleName != null) {
			state.defaultStyle =  catalog.getStyleByName(state.workspaceName, state.defaultStyleName); // postgres change: The styles already exist in global so it may be ok- depends on what global is
			if (state.defaultStyle == null) {
				state.defaultStyle =  catalog.getStyleByName(state.defaultStyleName);
			}
			
			if (state.defaultStyle == null) {
				wrap.setMessage("The defaultStyleName " + state.defaultStyleName + 
						" does not exist in the specified workspace (if set) or in the global styles.");
				wrap.setStatus(ServiceResponseStatus.FAIL);
				return;
			}
		}
                state.shapeFile = new File(state.shapeFilePath); // to be removed SPDSSI-28
		state.dbfFile = new File(state.dbfFilePath); // to be removed SPDSSI-28

		
		if (state.gwcParamFilters != null && state.gwcParamFilters.length > 0) {
			if (state.gwcParamFilters.length % 4 != 0) {
				wrap.setMessage("The gwcParamFilters must be set in groups of 4");
				wrap.setStatus(ServiceResponseStatus.FAIL);
			} else {
				ArrayList<ParameterFilter> paramFilters = new ArrayList();
				
				for (int i=0; i<state.gwcParamFilters.length; i+=4) {
					String key = state.gwcParamFilters[i];
					String type = state.gwcParamFilters[i + 1];
					String defaultVal = StringUtils.trimToNull(state.gwcParamFilters[i + 2]);
					String value = state.gwcParamFilters[i + 3];
					
					
					if (key == null) {
						wrap.setMessage("The gwcParamFilters key cannot be null");
						wrap.setStatus(ServiceResponseStatus.FAIL);
					}
					
					if (value == null) {
						wrap.setMessage("The gwcParamFilters value cannot be null");
						wrap.setStatus(ServiceResponseStatus.FAIL);
					}
					
					if ("string".equalsIgnoreCase(type)) {
						
						ArrayList<String> vals = new ArrayList();
						vals.add(value);
						
						StringParameterFilter filter = new StringParameterFilter();
						filter.setKey(key);
						filter.setValues(vals);
						if (defaultVal != null)	filter.setDefaultValue(defaultVal);
						
						paramFilters.add(filter);
					} else if ("regex".equalsIgnoreCase(type)) {
						RegexParameterFilter filter = new RegexParameterFilter();
						filter.setKey(key);
						filter.setRegex(value);
						if (defaultVal != null)	filter.setDefaultValue(defaultVal);
						
						paramFilters.add(filter);
					} else {
						wrap.setMessage("The gwcParamFilters type '" + type + "' is not recgonized.  Must be 'string' or 'regex'");
						wrap.setStatus(ServiceResponseStatus.FAIL);
					}
				}
				
				state.parameterFilters = paramFilters;
			}
		} else {
			state.parameterFilters = Collections.emptyList();
		}
	}
                
 	/**
	 * Actually creates the layer.
	 * 
	 * @param wrap
         * @param state
	 * @throws Exception 
	 */       
        private void createLayer(UserState state, ServiceResponseWrapper wrap) throws Exception {

		LayerResponse resp = new LayerResponse();
                resp.setlayerName(state.layerName);
                
                DataStoreInfo dsInfo = null;
               // DataStore postgis1 = null;
                
                JDBCDataStore jdbcPostgis = null;
 
                    try{        
                       // postgis1 =  DataStoreFinder.getDataStore(getPostgresParms(state)); //Assumes you created it already - the postgres datastore, in the context.xml and have the driver in the lib
                        
                        // new jdbc store....
                        jdbcPostgis = createJDBCStoreForLayer(state, wrap); 
                        
                    // PostGISDataStore represents the database, while a FeatureSource represents a table in the database
                    SimpleFeatureType schema = jdbcPostgis.getSchema(state.layerName); // this is the view
                    SimpleFeatureSource simSource = jdbcPostgis.getFeatureSource(state.layerName);
                           
                    CatalogBuilder builder = new CatalogBuilder(catalog);
                    
                    //check to see if the store exists already and dont recreate the wheel if it does
                    //the datastore is the DB. The store is the view or table.
                    dsInfo = catalog.getDataStoreByName(state.workspace, state.layerName); //datastore and layer name are the same
                    
                    if (dsInfo == null){
                        //create a datastore info object for the layer (its postgres but has a name that matches the layer name
                        try{
                            DataStoreInfoImpl store = createStoreForLayer(state);

                            builder.setStore(store); 
                            builder.setWorkspace(store.getWorkspace()); //it looks like geoserver wants a builder for each workspace
                                                 
                            catalog.add(store);  //this should add the new store to the geoserver catalog
                        }catch (IllegalStateException ex) {
                            log.error("Exception caught while trying to set the new store in the geo catalog builder "); 
                            wrap.setError(ex);
                        }
                    }else {
                        log.info("Data store already exists for " + dsInfo.getName() + " so there's no need to recreate it.");
                        builder.setStore(dsInfo);
                        builder.setWorkspace(dsInfo.getWorkspace());
                    }

                    FeatureTypeInfo featureTypeInfo = null;
                    try{
                        featureTypeInfo = builder.buildFeatureType(simSource); 
                        featureTypeInfo = setFeatureTypeInfo(featureTypeInfo, state); 
                        
                    }catch (IllegalStateException ex) {
                       log.error("Exception caught while trying to create/build featureSource " + simSource.getName()); //http://water.usgs.gov/nawqa/sparrow/dss/spatial/postgres-sparrow-flowline-reusable:flow_-721080852
                       log.error("Illegal state exception:" + ex.getMessage());
                       wrap.setError(ex);
                    }
                                       
                    builder.lookupSRS(featureTypeInfo, true);  //if permissions on Postgres tables are not granted to sparrow_model_output_user by postgres user, you will get an exception...
                    //GRANT ALL ON TABLE public.spatial_ref_sys TO sparrow_model_output_user;
                    //GRANT SELECT ON TABLE public.geometry_columns TO sparrow_model_output_user;
                    log.info("About to set up Bounds for layer feature type.");
                    builder.setupBounds(featureTypeInfo);
                    
                    //build the layer and add the style 
                    LayerInfo layerInfo = builder.buildLayer(featureTypeInfo);
                    if (state.defaultStyle != null) {
                        layerInfo.setDefaultStyle(state.defaultStyle);   
                    }
                    // add the features etc to the catalog
                    
                    catalog.add(featureTypeInfo);  
                    catalog.add(layerInfo);
                   
                    setTileLayers(state, layerInfo);
			
                    //The response object and its wrapper
                    resp.setlayerName(state.fullLayerName);
                    wrap.addEntity(resp);

                } catch (IOException e){
                    //Message and error will be auto-logged from the wrapper
                    wrap.setMessage("Error obtaining new data store. ");
                    wrap.setError(e);
                    wrap.setStatus(ServiceResponseStatus.FAIL);
                    //call the sweeper to clean up the mess
                    sweepMess(dsInfo, state);
                }
                finally 
                {
                    jdbcPostgis.dispose();  
                }
        } 	
        
        private void setTileLayers(UserState state, LayerInfo layerInfo){
                //Set tile cache options
                if (state.parameterFilters.size() > 0) {
                    GeoServerTileLayer tileLayer = gwc.getTileLayer(layerInfo);
                    GeoServerTileLayerInfo tileLayerInfo = tileLayer.getInfo();
				
                    for (ParameterFilter filter : state.parameterFilters) {
                        tileLayerInfo.addParameterFilter(filter);
                    }				
                        gwc.save(tileLayer);
                    }  
        }
        
        private void sweepMess(DataStoreInfo dsInfo, UserState state){
                log.debug("Attempting to roll back layer creation changes after error...");
            
            /**
             * Since we dont know exactly when the exception was thrown we will do the full layer removal
             * process.  It will do everything it needs to remove a layer that already exists but its
             * possible that it wont get to something as a prerequisite for full removal might be what 
             * threw this exception.
             */
            log.info("An exception occurred during the creation of the layer that now requires clean-up via the Sweeper. " + state.layerName); //#TODO# SPDSSI-28
            SweepResponse.DataStoreResponse dsr = GeoServerSparrowLayerSweeper.cascadeDeleteDataStore(catalog, dsInfo); // This appears to attempt to delete whatever it can
            if(! dsr.isDeleted) {
		log.error("Unable to fully remove all layer creation changes for datastore [" + dsInfo.getName() + "]");
            }
        }
        
        private FeatureTypeInfo setFeatureTypeInfo(FeatureTypeInfo featureTypeInfo, UserState state){
                               
                featureTypeInfo.setSRS(state.projectedSrs);  
                featureTypeInfo.setName(state.layerName);
                featureTypeInfo.setTitle(state.layerName); 
                featureTypeInfo.setDescription(state.description);//model nbr is mentioned here
                featureTypeInfo.setAbstract(state.description);
                featureTypeInfo.setProjectionPolicy(ProjectionPolicy.FORCE_DECLARED);
                    
                return featureTypeInfo;
        }
                 
        private HashMap getPostgresParms(UserState state)
        {
            // http://docs.geotools.org/stable/userguide/library/jdbc/datastore.html
            HashMap<String, Serializable>map = new HashMap<String, Serializable>();
                map.put("dbtype", "postgis");
                map.put("jndiReferenceName", "java:comp/env/jdbc/postgres");  //in the tomcat context.xml
             //   map.put("loose bbox", true); //PostgisDataStoreFactory.LOOSEBBOX    does not work in this version of gt
             //   map.put("preparedStatements", true); //this does work but it will change your dialect and its more restrictive. Return to this if there's perf issues.
                map.put("namespace", state.namespace.getURI());  //<-- not listed as an option /library/jdbc/postgis.html
                map.put("schema", "sparrow_overlay");
                map.put("Expose primary keys", true);
                map.put("lastUsedMS", System.currentTimeMillis());  // Date for pruning process in sweeper
            return map;
        }
        
        // since we are working with an oder version of gs, it requires an older version of gt. The comments are based on what was functional with the gt version (not in synch with the current gt doc)
        private JDBCDataStore createJDBCStoreForLayer(UserState state, ServiceResponseWrapper wrap)
        {
            JDBCDataStore jdbcStore = null;
            DataStore dataStore = null;
            try {
                try {
                    dataStore = DataStoreFinder.getDataStore(getPostgresParms(state));
                } catch (IOException ex) {
                    log.error(ex.getMessage());
                    wrap.setError(ex);
                }
                if (dataStore instanceof JDBCDataStore) {
                    jdbcStore = (JDBCDataStore)dataStore;  
                    log.info("Successfully created and cast the JDBC store for layer.");
                    SQLDialect dialect= jdbcStore.getSQLDialect(); //PostGISDialect is expected
                    log.info("Dialect type: " + dialect.toString()); //PostGISPSDialect - postgis prepared statement dialect occurs when map.put("preparedStatements", true);
                 
                if (dialect instanceof PostGISDialect)  //none of these settings work in geotools v 10.5, geoserver v2.4.5
                    ((PostGISDialect) jdbcStore.getSQLDialect()).setEstimatedExtentsEnabled(true);//  I shouldnt need to do this if the parms worked. Prepared statements does work in the map and expose primary keys. 
                    //((PostGISDialect) jdbcStore.getSQLDialect()).setLooseBBOXEnabled(true); 
                    //((PostGISDialect) jdbcStore.getSQLDialect()).setFunctionEncodingEnabled(true);
                    log.info("Dialect type has set to enabled: Estimated ext "); //, Loose BBox, Func Encoding. ");
                }
            }catch (Exception ex) {
                log.info("Caught exception creating postgres datastore " + ex.getMessage()); 
                wrap.setError(ex);
                wrap.setStatus(ServiceResponseStatus.FAIL);
                if (dataStore != null)
                    dataStore.dispose();
                if (jdbcStore != null)
                    jdbcStore.dispose(); 
            }
            
            return jdbcStore;
        }
        
	private DataStoreInfoImpl createStoreForLayer(UserState state)
        {
            DataStoreInfoImpl store = new DataStoreInfoImpl(catalog); //this is so that you can register the new store for a particular layer
                store.setType(POSTGRES_SHAPEFILE_JOIN_DATASTORE_NAME);
                store.setWorkspace(state.workspace);
                store.setEnabled(true);
                store.setName(state.layerName);//Sweeper uses this convention. This is just a Name but it will look like it has a store per layer in the ui. postgis.getNames().get(0).toString());//catch_-789789789 or flow_-789789789 for example
                log.info("Created new datastore info with name (ie view name):" + state.layerName.toString());
                store.setConnectionParameters(getPostgresParms(state));
            return store;
        }
   
	/**
	 * The WPS is single instance, so a state class for each user request holds
	 * all info for a single execution.
	 */
	private class UserState {
		//User params, set for each invocation
		private String layerName;
		private String workspaceName;
		private String shapeFilePath;
		private String dbfFilePath;
		private String idFieldInDbf; // NO LONGER NEEDED (previous change before SPDSSI-28
		private String projectedSrs;
		private String defaultStyleName;
		private String[] gwcParamFilters;
		private String description;
		private Boolean overwrite;

		//Self Init based on user params
		private String fullLayerName;
		private WorkspaceInfo workspace;
		private File shapeFile; // NO LONGER NEEDED- SPDSSI-28
		private File dbfFile; // NO LONGER NEEDED- SPDSSI-28
		private NamespaceInfo namespace;
		private StyleInfo defaultStyle;
		private List<ParameterFilter> parameterFilters;
	}
	
}
