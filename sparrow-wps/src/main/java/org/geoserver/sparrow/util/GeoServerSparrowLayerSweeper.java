package org.geoserver.sparrow.util;

import gov.usgs.cida.sparrow.service.util.NamingConventions;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingException;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.geoserver.catalog.Catalog;
import org.geoserver.catalog.DataStoreInfo;
import org.geoserver.catalog.LayerInfo;
import org.geoserver.catalog.StyleInfo;
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geoserver.config.GeoServerDataDirectory;
import org.geoserver.platform.GeoServerExtensions;
import org.geotools.data.DataAccess;
import org.geotools.util.DefaultProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jndi.JndiTemplate;

public class GeoServerSparrowLayerSweeper implements InitializingBean, DisposableBean {
	protected static final Logger log = org.geotools.util.logging.Logging.getLogger("org.geoserver.sparrow.util");
	private static final Long DEFAULT_MAX_LAYER_AGE = 172800000L; // 2d in ms
	private static final Long DEFAULT_RUN_EVER_MS = 3600000L; // 1h in ms
	//private static final String DEFAULT_PRUNED_WORKSPACES = "sparrow-catchment,sparrow-flowline";
        private static final String DEFAULT_PRUNED_WORKSPACES = "postgres-sparrow-catchment,postgres-sparrow-flowline";
	private static final String DBASE_KEY = "dbase_file";
	private static final String DBASE_TIME_KEY = "lastUsedMS";
	private Long maxAge;	//Age in miliseconds
	private Long runEveryMs;
	private final Catalog catalog;
	private String[] prunedWorkspaces;
	private Thread sweeperThread;
	
	/** flag to determine if the sweep process should keep running */
	private volatile boolean keepRunning = true;
	
	/** When the sweep actually runs (runSweep), this is used as a thread lock.  Do not share. */
	private static final Object SWEEP_LOCK = new Object();
	
	/** 
	 * Lock used when a single data store is deleted (cascadeDeleteDataStore).
	 * This is a separate lock from the sweep b/c the delete method may be
	 * called separately by other processes.
	 */
	private static final Object DELETE_LOCK = new Object();

	public GeoServerSparrowLayerSweeper(Catalog catalog) {
		this.catalog = catalog;
		
	}

	@Override
	public void destroy() throws Exception {
		log.log(Level.INFO, "Sweeper thread is shutting down");
		
		keepRunning = false;
		this.sweeperThread.interrupt();
		this.sweeperThread.join(2000L);	//wait 2 seconds for the thread to die
		log.log(Level.INFO, "Sweeper thread is shut down");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		JndiTemplate template = new JndiTemplate();

		try {
			this.maxAge = template.lookup("java:comp/env/sparrow.geoserver.layer.age.maximum", Long.class);
		} catch (NamingException ex) {
			this.maxAge = DEFAULT_MAX_LAYER_AGE;
			log.log(Level.INFO, "Init parameter 'sparrow.geoserver.layer.age.maximum' was not set. Maximum layer age set to {0}ms", this.maxAge);
		}

		try {
			this.runEveryMs = template.lookup("java:comp/env/sparrow.geoserver.sweeper.run.period", Long.class);
		} catch (NamingException ex) {
			this.runEveryMs = DEFAULT_RUN_EVER_MS;
			log.log(Level.INFO, "Init parameter 'sparrow.geoserver.sweeper.run.period' was not set. Sweeper will run every {0}ms", this.runEveryMs);
		}

		String envPrunedWorkspaces;
		try {
			envPrunedWorkspaces = template.lookup("java:comp/env/sparrow.geoserver.sweeper.workspaces.pruned", String.class);
		} catch (NamingException ex) {
			envPrunedWorkspaces = DEFAULT_PRUNED_WORKSPACES;
			log.log(Level.INFO, "Init parameter sparrow.geoserver.sweeper.workspaces.pruned was not set. Read only workspaces set to: {0}", envPrunedWorkspaces);
		}

		if (StringUtils.isNotBlank(envPrunedWorkspaces)) {
			this.prunedWorkspaces = envPrunedWorkspaces.split(",");
		}

		if (null != this.prunedWorkspaces && this.prunedWorkspaces.length != 0) {
			this.sweeperThread = new Thread(new SparrowSweeper(this.catalog, this.maxAge, this.prunedWorkspaces, this.runEveryMs), "sweeper-thread");
			this.sweeperThread.start();
		} else {
			// Failsafe
			log.log(Level.INFO, "Because there were no workspaces set, the sweeper process will not be started. If this is a mistake, set the parameter 'sparrow.geoserver.sweeper.workspaces.pruned' to a comma separated list of workspace names.");
		}
	}
	
	/**
	 * 
	 * @param cBuilder
	 * @param da
	 * @param dsInfo
	 * @param dbfFile
	 * @return
	 * 
	 *
	 * The logic for removing everything associated with this dbf file is as follows:
 
 		1) Get all resource names associated with this store (all layer names)
 		2) For each layer dsName, get the layer info object
 		3) For each layer info object, detach the layer from the GeoServer Catalog
 		4) For each layer info object, remove the layer completely from the GeoServer Catalog
 		5) For each layer dsName, get the resource info object
 		6) For each resource info object, detach the resource from the GeoServer Catalog
 		7) For each resource info object, remove the resource completely from the GeoServer Catalog
 		8) Clean up the GeoTools cache (DataAccess Object dispose() method)
 		9) Delete the datastore itself (cBuilder.removeStore(dsInfo, false);)
 		10) Delete the rows on the Postgres model_output table.
	 * 
	 */
	public static SweepResponse.DataStoreResponse cascadeDeleteDataStore(Catalog dsCatalog, DataStoreInfo dsInfo, File dbfFile) {
		
		synchronized (DELETE_LOCK) {
			
			String dsName = dsInfo.getName();
			
			log.log(Level.INFO, "==========> PRUNING DATASTORE [{}]", dsName);
			
			SweepResponse.DataStoreResponse response = new SweepResponse.DataStoreResponse(dsInfo.getWorkspace().getName(), dsName);
			
			DataAccess<? extends FeatureType, ? extends Feature> da = null;
			
			
			try {
				da = dsInfo.getDataStore(null);
			} catch (IOException ex) {
				log.log(Level.WARNING, "Could not connect to the data-access for '{}'.  Will continue trying to delete other aspects", dsName);
				response.addMessage("Could not connect to the data-access for this layer.  Will continue trying to delete other aspects");
			}
			

			try {
				
				//Attempt to delete related layers and their default styles (if style is specific to the layer)
				if (da != null) {
					List<Name> layerNames = da.getNames();
					
					for (Name layerName : layerNames) {

						String layerLocalName = layerName.getLocalPart();
						LayerInfo layer = dsCatalog.getLayerByName(layerName);
						
						if (layer != null) {
							
							//
							//delete layer
							response.addResource(deleteLayer(dsCatalog, layer));
							
							//
							//delete associated style, maybe
							StyleInfo style = layer.getDefaultStyle();

							if (style != null) {
								String styleLocalName = style.getName();

								if (NamingConventions.isLikelyReusableStyleNameForLayer(styleLocalName, layerLocalName)) {
									//OK, it is very extremely likely (barring someone manually messing w/ layers and
									//styles on GeoServer), that this is a dedicated style just for this layer,
									//which must be a 'reusable' layer based on the style naming conventions.
									//Since the style is only for the use of this layer, it should be deleted w/ the layer.
									//Next time this layer is created, the style will be recreated.
									response.addResource(deleteStyle(dsCatalog, style));
								}	
							}

						} else {
							response.addResource(layerLocalName, "Layer is not in catalog, even though listed with the datastore.  Ignoring.");
						}
					}
					
					//I *think* this may release the resources
					da.dispose();
				}

				//Attempt to delete any other (?? what would that be??) resources
				for (ResourceInfo resource : dsCatalog.getResourcesByStore(dsInfo, ResourceInfo.class)) {
					response.addResource(deleteResource(dsCatalog, dsInfo, resource));
				}

				//Now remove the datastore itself
				try {
					dsCatalog.detach(dsInfo);
					dsCatalog.remove(dsInfo);	//can throw an exception if there are still undeleted resources
				} catch (IllegalArgumentException ie) {
					response.err = ie;
					
					response.addMessage("Err during catalog.remove(datasource), likely due to resources still existing for ds.");
					
					try {
						for (ResourceInfo resource : dsCatalog.getResourcesByStore(dsInfo, ResourceInfo.class)) {
							response.addResource(resource.getName(), "Reported as still present when dataStore.remove() was called.");
						}
					} catch (Exception ee) {
						response.addMessage("Unable to build list of undeleted resources.");
					}
				} catch (Throwable t) {
					response.addMessage("Throwable during catalog.remove(datasource) for unknown reason.");
				}

				//Can only delete the dbf file is no other datastores are using it
				//For Sparrow, that happens when there is a ds with the same name (will be in a different workspace)
				List<DataStoreInfo> dsOfSameName = getDatastoresForName(dsCatalog, dsName);
				
				if (dsOfSameName.isEmpty()) {
					FileUtils.deleteQuietly(dbfFile); //SPDSSI-28 #TODO# call a action to delete the rows given the model_output id derived from the layer name.
					response.isDbfDeleted = true;	//false by default
				}
										
				response.isDeleted = true;	//false by default
				log.log(Level.INFO, "===============> DATASTORE [{}] HAS BEEN REMOVED", dsName);
			} catch (Exception e) {
				log.log(Level.WARNING, "A Sweeper exception has occurred during DataStore [" + dsName + "] removal.  Skipping DataStore and resuming...", e);
				response.err = e;
			}

			return response;
		}
	}
	
	
	private static SweepResponse.Resource deleteLayer(Catalog dsCatalog, LayerInfo layer) {
		
		if (layer != null) {
			synchronized (DELETE_LOCK) {

				String name = layer.getName();
				String id = layer.getId();

				log.log(Level.FINE, "Deleting layer [{}]", name);

				try {

					dsCatalog.detach(layer);
					dsCatalog.remove(layer);

					layer = dsCatalog.getLayer(id);

					if (layer == null) {
						return new SweepResponse.Resource(name, "deleted layer");
					} else {
						log.log(Level.WARNING, "Attempted to delete layer [{}], but it is still in the catalog", name);
						return new SweepResponse.Resource(name, "attempted to delete layer, but still present in catalog.");
					}
				} catch (Throwable e) {
					log.log(Level.WARNING, "Error deleting layer [" + name + "]", e);
					return new SweepResponse.Resource(name, "attempted to delete layer, but it caused an error", e);
				}
			}
		} else {
			return new SweepResponse.Resource("NULL layer", "attempted to delete layer, but it didn't exist in the catalog");
		}
	}
	
	private static SweepResponse.Resource deleteResource(Catalog dsCatalog, DataStoreInfo dsInfo, ResourceInfo resource) {
		
		if (resource != null) {
			synchronized (DELETE_LOCK) {
				String name = resource.getName();

				log.log(Level.FINE, "Deleting resource [{}]", name);

				try {
					dsCatalog.detach(resource);
					dsCatalog.remove(resource);

					ResourceInfo followUp = dsCatalog.getResourceByStore(dsInfo, name, ResourceInfo.class);

					if (followUp == null) {
						return new SweepResponse.Resource(name, "deleted resource");
					} else {
						log.log(Level.WARNING, "Attempted to delete resource [{}], but it is still in the catalog", name);
						return new SweepResponse.Resource(name, "attempted to delete resource, but still present in catalog");
					}
				} catch (Throwable e) {
					log.log(Level.WARNING, "Error deleting resource [" + name + "]", e);
					return new SweepResponse.Resource(name, "attempted to delete resource, but it caused an error", e);
				}
			}
		} else {
			return new SweepResponse.Resource("NULL resource", "attempted to delete resource, but it didn't exist in the catalog");
		}
	}
	
	private static SweepResponse.Resource deleteStyle(Catalog dsCatalog, StyleInfo style) {
		
		if (style != null) {
			synchronized (DELETE_LOCK) {

				String name = style.getName();
				String id = style.getId();

				log.log(Level.FINE, "Deleting style [{}]", name);

				try {

					GeoServerDataDirectory gsdd = getGeoServerDataDirectory();
					File styleSld = gsdd.findStyleSldFile(style);

					dsCatalog.detach(style);
					dsCatalog.remove(style);

					if (styleSld != null) {
						FileUtils.deleteQuietly(styleSld);
					}

					style = dsCatalog.getStyle(id);

					if (style == null) {
						return new SweepResponse.Resource(name, "deleted style");
					} else {
						log.log(Level.WARNING, "Attempted to delete style [{}], but it is still in the catalog", name);
						return new SweepResponse.Resource(name, "attempted to delete style, but still present in catalog.  Will be invalid b/c its .sld file was deleted.");
					}
				} catch (Throwable e) {
					log.log(Level.WARNING, "Error deleting style [" + name + "]", e);
					return new SweepResponse.Resource(name, "attempted to delete style, but it caused an error", e);
				}
			}
		} else {
			return new SweepResponse.Resource("NULL style", "attempted to delete style, but it didn't exist in the catalog");
		}
	}
	
	private static GeoServerDataDirectory getGeoServerDataDirectory() throws Exception {
		try {
			GeoServerDataDirectory gsDataDirectory = ((GeoServerDataDirectory) GeoServerExtensions.bean("dataDirectory"));
			return gsDataDirectory;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Configuration Error - GeoServerDataDirectory is null or unsuable", e);
			throw e;
		}
	}
	
	
	/**
	 * Returns all the DataStores with a particular name, contained in any
	 * workspace.
	 * 
	 * @param dsCatalog
	 * @param name
	 * @return 
	 */
	private static List<DataStoreInfo> getDatastoresForName(Catalog dsCatalog, String name) {
		
		ArrayList<DataStoreInfo> list = new ArrayList(1);
		for (WorkspaceInfo w : dsCatalog.getWorkspaces()) {
			DataStoreInfo ds = dsCatalog.getDataStoreByName(w, name);
			if (ds != null) list.add(ds);
		}
		
		return list;
	}
	
	/**
	 * Runs a sweep with default values
	 */
	public SweepResponse runSweep() throws Exception {
		return runSweep(this.catalog, this.prunedWorkspaces, (String)null, this.maxAge);
	}
	
	public SweepResponse runSweep(Long maxAgeMs) throws Exception {
		
		if (maxAgeMs == null) maxAgeMs = this.maxAge;
		
		return runSweep(this.catalog, this.prunedWorkspaces, (String)null, maxAgeMs);
	}
	
	public SweepResponse runSweep(String[] prunedWorkspaces, Integer modelId, Long maxAgeMs) throws Exception {
		
		if (prunedWorkspaces == null) prunedWorkspaces = this.prunedWorkspaces;
		if (maxAgeMs == null) maxAgeMs = this.maxAge;
		
		if (modelId != null) {
			return runSweep(catalog, prunedWorkspaces, NamingConventions.buildModelRegex(modelId), maxAgeMs);
		} else {
			return runSweep(catalog, prunedWorkspaces, (String)null, maxAgeMs);	
		}
	}
	
	public SweepResponse runSweep(String[] prunedWorkspaces, String namePattern, Long maxAgeMs) throws Exception {
		
		if (prunedWorkspaces == null) prunedWorkspaces = this.prunedWorkspaces;
		if (maxAgeMs == null) maxAgeMs = this.maxAge;
		
		
		return runSweep(catalog, prunedWorkspaces, namePattern, maxAgeMs);
	}
	
	private SweepResponse runSweep(Catalog catalog, String[] prunedWorkspaces, Integer modelId, Long maxAgeMs) throws Exception {
		
		if (modelId != null) {
			return runSweep(catalog, prunedWorkspaces, NamingConventions.buildModelRegex(modelId), maxAgeMs);
		} else {
			return runSweep(catalog, prunedWorkspaces, (String)null, maxAgeMs);	
		}
	}
	
	private SweepResponse runSweep(Catalog catalog, String[] prunedWorkspaces, String namePattern, Long maxAgeMs) throws Exception {
		synchronized (SWEEP_LOCK) {
			
			SweepResponse response = new SweepResponse();
			
			try {
				log.log(Level.INFO, "\n\n************************************************************\nRunning Sparrow DBF Sweep\n************************************************************\n");
				Long currentTime = new Date().getTime();

				// Get a cleaned list of workspaces
				List<WorkspaceInfo> workspaceInfoList = catalog.getWorkspaces();
				for (WorkspaceInfo wsInfo : workspaceInfoList) {
					if (!java.util.Arrays.asList(prunedWorkspaces).contains(wsInfo.getName())) {
						log.log(Level.FINEST, "\n\n=====> WORKSPACE [" + wsInfo.getName() + "] IS NOT LISTED TO BE PRUNED.  SKIPPING...");
						continue;
					} 

					log.log(Level.INFO, "\n\n----------\nCLEANING WORKSPACE [" + wsInfo.getName() + "]\n----------");

					List<DataStoreInfo> dsInfoList = catalog.getDataStoresByWorkspace(wsInfo);
						
					for (DataStoreInfo dsInfo : dsInfoList) {
						
						String dsName = StringUtils.trimToEmpty(dsInfo.getName());
						
						if (namePattern == null || dsName.matches(namePattern)) {

							/**
							 * Lets get the dbf filename for this specific datastore
							 */
							Map<String, Serializable> connectionParams = dsInfo.getConnectionParameters();

							if(connectionParams == null) {
								response.kept.add(logSweepError(wsInfo, dsInfo, "There are no connection parameters for this datastore"));
								continue;
							}

							Object dbaseLocationObj = connectionParams.get(DBASE_KEY);
							if(dbaseLocationObj == null) {
								response.kept.add(logSweepError(wsInfo, dsInfo, "There is no " + DBASE_KEY + " connection parameter for this datastore"));
								continue;
							}								

							String dbaseLocation = null;								
							if(dbaseLocationObj instanceof URL) {
								dbaseLocation = ((URL)dbaseLocationObj).toString();
							} else if(dbaseLocationObj instanceof String) {
								dbaseLocation = (String)dbaseLocationObj;
							}

							if((dbaseLocation != null) && (!dbaseLocation.equals(""))) {
								dbaseLocation = dbaseLocation.replace("file:", "");
							} else {
								response.kept.add(logSweepError(wsInfo, dsInfo, "The " + DBASE_KEY + " connection parameter must be either a string or a url and cannot be empty"));
								continue;
							}

							/**
							 * Lets get the age of this dbf file which is embedded in an 
							 * attribute for the datastore "lastUsedMS"
							 */
							Long fileAge = 0L;
							Object ageObject = connectionParams.get(DBASE_TIME_KEY);

							if(ageObject == null) {
								log.log(Level.WARNING, "The sweeper found a DataStore [" + dsName + "] in the [" +
										wsInfo.getName() + "] workspace that does not have the age flag \"" + DBASE_TIME_KEY +
										"\" associated with it.  Using the dbf file's last modified time for the age calculation...");

								File tmpFile = new File(dbaseLocation);
								fileAge = tmpFile.lastModified();
							} else {
								if(ageObject instanceof Long) {
									fileAge = (Long)ageObject;
								} else {
									try {
										fileAge = Long.parseLong((String)ageObject);
									} catch (Exception e) {
										log.log(Level.WARNING, "The sweeper found a DataStore [" + dsName + "] in the [" +
												wsInfo.getName() + "] workspace that has a value associated with the age flag \"" + DBASE_TIME_KEY +
												"\" that cannot be converted to a long value [" + ageObject.toString() + "]. " +
												"Using the dbf file's last modified time for the age calculation...");

										File tmpFile = new File(dbaseLocation);
										fileAge = tmpFile.lastModified();
									}
								}
							}

							/**
							 * If the file age of the DBF is larger than our timeout age we remove the layer from 
							 * GeoServer's memory and then delete the dbf.
							 */
							if (currentTime - fileAge > maxAgeMs) {
								
								File dbfFile = new File(dbaseLocation);
								response.deleted.add(cascadeDeleteDataStore(catalog, dsInfo, dbfFile));	
								
							} else {
								SweepResponse.DataStoreResponse dsr = new SweepResponse.DataStoreResponse(wsInfo.getName(), dsName);
								response.kept.add(dsr);
							}
							
						} else {
							log.log(Level.FINE, "Skipping store '" + dsName + "' - does not match requested name pattern '" + namePattern + "'");
						}
						
					}


					if(response.hasDeletions()) {
						log.log(Level.INFO, "\n=====> " + response.deleted.size() + " DATASTORES WERE DELETED FROM WORKSPACE [" + wsInfo.getName() + "]");
					} else {
						log.log(Level.INFO, "\n=====> NO DATA WAS CLEANED FOR WORKSPACE [" + wsInfo.getName() + "]");
					}

					log.log(Level.INFO, "\n----------\nWORKSPACE [" + wsInfo.getName() + "] SWEEP COMPLETED\n----------");
				}

				log.log(Level.INFO, "\n\n************************************************************\nSparrow DBF Sweep Completed\n************************************************************\n\n");
			} catch (Exception ex) {
				log.log(Level.WARNING, "An error has occurred during execution of sweep", ex);
				throw ex;
			} finally {
				// Clean up
			}
			
			return response;
		}
	}
	
	/**
	 * Builds a DataStoreResponse and logs a warning message for issues with a specific DataStore
	 * @param wsInfo
	 * @param dsInfo
	 * @param message
	 * @return 
	 */
	private static SweepResponse.DataStoreResponse logSweepError(WorkspaceInfo wsInfo, DataStoreInfo dsInfo, String message) {
		log.log(Level.WARNING, "The sweeper found the DataStore [" + dsInfo.getName() + "] in workspace [" +
				wsInfo.getName() + "] and didn't know what to do with it, so it was skipped.  The issue was: " +
				message);

		SweepResponse.DataStoreResponse dsr = new SweepResponse.DataStoreResponse(wsInfo.getName(), dsInfo.getName());
		dsr.dsName = dsInfo.getName();
		dsr.err = new Exception(message);
		return dsr;
	}

	private class SparrowSweeper implements Runnable {
		private final Long maxAge;
		private final Long runEveryMs;
		private final String[] prunedWorkspaces;
		private final Catalog catalog;

		public SparrowSweeper(Catalog catalog, Long maxAge, String[] envPrunedWorkspaces, Long runEveryMs) {
			this.catalog = catalog;
			this.maxAge = maxAge;
			this.prunedWorkspaces = envPrunedWorkspaces;
			this.runEveryMs = runEveryMs;
			
			log.log(Level.INFO, "\n\nCreating SparrowSweeper Instance:\n\tcatalog: " + this.catalog + "\n\tmaxAge: " + this.maxAge + "\n\tprunedWorkspaces: " + 
			Arrays.toString(this.prunedWorkspaces) + "\n\trunEveryMs: " + this.runEveryMs + "\n\n\n");
		}

		@Override
		public void run() {
			while (keepRunning) {
				
				try {
					runSweep(catalog, prunedWorkspaces, (String)null, maxAge);
				} catch (Exception ex) {
					//The error has already been logged.
				}

				try {
					// TODO: Use ThreadPoolExecutor to do this - ( http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html ) 
					Thread.sleep(runEveryMs);
				} catch (InterruptedException ex) {
					Thread.interrupted();	//clears the interupt flag
					
					if (keepRunning) {
						log.log(Level.INFO, "Sweeper thread was interupted, but the keepRunning flag is true, so it will continue.");
					} else {
						log.log(Level.INFO, "Sweeper thread was interupted and the keepRunning flag is false, so it will stop.");
					}
					
				}
			}

		}
	}
	
}
