package org.geoserver.sparrow.process;

import gov.usgs.cida.sparrow.service.util.ServiceResponseMimeType;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import org.apache.commons.lang.StringUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.CatalogBuilder;
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
import org.geotools.data.DataAccess;
import org.geotools.process.factory.DescribeParameter;
import org.geotools.process.factory.DescribeProcess;
import org.geotools.process.factory.DescribeResult;
import org.geotools.util.NullProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.geowebcache.filter.parameters.ParameterFilter;
import org.geowebcache.filter.parameters.RegexParameterFilter;
import org.geowebcache.filter.parameters.StringParameterFilter;

		
/**
 * WPS to add a layer to GeoServer based on an export dbf file from the user's
 * prediction context and a shapefile.
 * 
 * This WPS is expecting to shapefiles structured on the server as shown:
 * <pre>
	|-sparrow_data (this directory is configured in jndi as 'shapefile-directory')
		|-shapefile
			|-MRB_1_NHD (this directory passed as 'shapeFilePath' execution arg)
				|-flowline
					|-coverage.shp
					|-coverage.shp.xml
					|- ... (other files associated w/ the shapefile)
				|-catchment
					|-coverage.shp (all shapefiles have the 'coverage' name)
					|- ... (other files associated w/ the shapefile)
	|-MRB_2_E2RF1...
 </pre>
 * 
 * For a single execution, a flowline and catchment layer are registered, each
 * in a separate namespace.
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
	
	public static String DBASE_SHAPEFILE_JOIN_DATASTORE_NAME = "Dbase Shapefile Joining Data Store";
	
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
	 * @param description
	 * @param overwrite
	 * @return
	 * @throws Exception 
	 */
	@DescribeResult(name="response", description="Registers a Sparrow context for mapping.", type=ServiceResponseWrapper.class)
	public ServiceResponseWrapper execute(
			@DescribeParameter(name="layerName", description="The layer name to add, without the workspace name", min = 1) String layerName,
			@DescribeParameter(name="workspaceName", description="Name of the workspace to use for the style, which must exist.  Null OK to put in the default namespace", min = 0, max = 1) String workspaceName,
			@DescribeParameter(name="shapeFilePath", description="The complete path on the local machine to a shapefile that the dbf file should be linked to.  Must be parsable into a Java File path.", min = 1) String shapeFilePath,
			@DescribeParameter(name="dbfFilePath", description="The path to the dbf file", min = 1) String dbfFilePath,
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
		state.shapeFilePath = shapeFilePath;
		state.dbfFilePath = dbfFilePath;
		state.idFieldInDbf = idFieldInDbf;
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
		
		log.debug("Request to created a new dbf/shapefile joined DataStore and Layer '{}'",  new Object[] {state.fullLayerName});
		
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
			state.defaultStyle =  catalog.getStyleByName(state.workspaceName, state.defaultStyleName);
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

		
		try {
			state.shapeFile = new File(state.shapeFilePath);
			if (! state.shapeFile.exists() || ! state.shapeFile.canRead()) {
				wrap.setMessage("The shapefile " + state.shapeFilePath + " does not exist or cannot be read.");
				wrap.setStatus(ServiceResponseStatus.FAIL);
			}
		} catch (Exception e) {
			wrap.setMessage("Unable to parse the shapefile path into a valid path on the GeoServer host machine: " + state.shapeFilePath);
			wrap.setError(e);
			wrap.setStatus(ServiceResponseStatus.FAIL);
		}
		
		try {
			state.dbfFile = new File(state.dbfFilePath);
			if (! state.dbfFile.exists() || ! state.dbfFile.canRead()) {
				wrap.setMessage("The dbf file " + state.dbfFilePath + " does not exist or cannot be read.");
				wrap.setStatus(ServiceResponseStatus.FAIL);
			}
		} catch (Exception e) {
			wrap.setMessage("Unable to parse the dbfFilePath into a valid path on the GeoServer host machine: " + state.dbfFilePath);
			wrap.setError(e);
			wrap.setStatus(ServiceResponseStatus.FAIL);
		}
		
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
	 * @throws Exception 
	 */
	private void createLayer(UserState state, ServiceResponseWrapper wrap) throws Exception {

		LayerResponse resp = new LayerResponse();
		
		Map<String, Serializable> dsParams = new HashMap<>();
		dsParams.put("shapefile", state.shapeFile.toURI().toURL());
		dsParams.put("dbase_file", state.dbfFile.toURI().toURL());
		dsParams.put("namespace", state.namespace.getURI());
		dsParams.put("dbase_field", state.idFieldInDbf);
		dsParams.put("lastUsedMS", System.currentTimeMillis());		// Date for pruning process
		
		DataStoreInfoImpl info = new DataStoreInfoImpl(catalog);
		info.setType(DBASE_SHAPEFILE_JOIN_DATASTORE_NAME);
		info.setWorkspace(state.workspace);
		info.setEnabled(true);
		info.setName(state.layerName);
		info.setConnectionParameters(dsParams);
		
		CatalogBuilder cb = new CatalogBuilder(catalog);
		catalog.add(info);
		DataAccess<? extends FeatureType, ? extends Feature> dataStore = info.getDataStore(new NullProgressListener());
		
        try {
			List<Name> names = dataStore.getNames();
			Name allData = names.get(0);

			ProjectionPolicy srsHandling = ProjectionPolicy.FORCE_DECLARED;
			
			//Create some cat builder thing for some purpose
			cb.setWorkspace(info.getWorkspace());
			cb.setStore(info);
			FeatureTypeInfo fti = cb.buildFeatureType(dataStore.getFeatureSource(allData));
			fti.setSRS(state.projectedSrs);
			fti.setName(state.layerName);
			fti.setTitle(state.layerName);
			fti.setDescription(state.description);
			fti.setAbstract(state.description);
			fti.setProjectionPolicy(srsHandling);
			
			cb.setupBounds(fti);
			LayerInfo li = cb.buildLayer(fti);
			if (state.defaultStyle != null) {
				li.setDefaultStyle(state.defaultStyle);
			}
			
			catalog.add(fti);
			catalog.add(li);
			
			//Set tile cache options
			if (state.parameterFilters.size() > 0) {
				GeoServerTileLayer tileLayer = gwc.getTileLayer(li);
				GeoServerTileLayerInfo tileLayerInfo = tileLayer.getInfo();
				
				for (ParameterFilter filter : state.parameterFilters) {
					tileLayerInfo.addParameterFilter(filter);
				}
			}
			
			//The response object and its wrapper
			resp.setlayerName(state.fullLayerName);
			wrap.addEntity(resp);
			
        } catch (IOException e) {
			
			//Message and error will be auto-logged from the wrapper
			wrap.setMessage("Error obtaining new data store");
			wrap.setError(e);
			wrap.setStatus(ServiceResponseStatus.FAIL);

            log.debug("Attempting to roll back layer creation changes after error...");
            
            /**
             * Since we dont know exactly when the exception was thrown we will do the full layer removal
             * process.  It will do everything it needs to remove a layer that already exists but its
             * possible that it wont get to something as a prerequisite for full removal might be what 
             * threw this exception.
             */
			SweepResponse.DataStoreResponse dsr = GeoServerSparrowLayerSweeper.pruneDataStore(catalog, info, state.dbfFile);
			if(! dsr.isDeleted) {
				log.error("Unable to fully remove all layer creation changes for datastore [" + info.getName() + "]");
			}
			
        }
		
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
		private String idFieldInDbf;
		private String projectedSrs;
		private String defaultStyleName;
		private String[] gwcParamFilters;
		private String description;
		private Boolean overwrite;

		//Self Init based on user params
		private String fullLayerName;
		private WorkspaceInfo workspace;
		private File shapeFile;
		private File dbfFile;
		private NamespaceInfo namespace;
		private StyleInfo defaultStyle;
		private List<ParameterFilter> parameterFilters;
	}
	
}
