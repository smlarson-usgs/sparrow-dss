package org.geoserver.sparrow.util;

import gov.usgs.cida.sparrow.service.util.NamingConventions;
import java.io.File;
import java.io.Serializable;
import java.net.URL;
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
import org.geoserver.catalog.ResourceInfo;
import org.geoserver.catalog.WorkspaceInfo;
import org.geotools.data.DataAccess;
import org.geotools.util.DefaultProgressListener;
import org.opengis.feature.Feature;
import org.opengis.feature.type.FeatureType;
import org.opengis.feature.type.Name;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.jndi.JndiTemplate;

public class GeoServerSparrowLayerSweeper implements InitializingBean, DisposableBean {
	protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.sparrow.util");
	private static final Long DEFAULT_MAX_LAYER_AGE = 172800000L; // 2d in ms
	private static final Long DEFAULT_RUN_EVER_MS = 3600000L; // 1h in ms
	private static final String DEFAULT_PRUNED_WORKSPACES = "sparrow-catchment,sparrow-flowline";
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
	 * Lock used when a single data store is deleted (pruneDataStore).
	 * This is a separate lock from the sweep b/c the delete method may be
	 * called separately by other processes.
	 */
	private static final Object DELETE_LOCK = new Object();

	public GeoServerSparrowLayerSweeper(Catalog catalog) {
		this.catalog = catalog;
	}

	@Override
	public void destroy() throws Exception {
		LOGGER.log(Level.INFO, "Sweeper thread is shutting down");
		
		keepRunning = false;
		this.sweeperThread.interrupt();
		this.sweeperThread.join(2000L);	//wait 2 seconds for the thread to die
		LOGGER.log(Level.INFO, "Sweeper thread is shut down");
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		JndiTemplate template = new JndiTemplate();

		try {
			this.maxAge = template.lookup("java:comp/env/sparrow.geoserver.layer.age.maximum", Long.class);
		} catch (NamingException ex) {
			this.maxAge = DEFAULT_MAX_LAYER_AGE;
			LOGGER.log(Level.INFO, "Init parameter 'sparrow.geoserver.layer.age.maximum' was not set. Maximum layer age set to {0}ms", this.maxAge);
		}

		try {
			this.runEveryMs = template.lookup("java:comp/env/sparrow.geoserver.sweeper.run.period", Long.class);
		} catch (NamingException ex) {
			this.runEveryMs = DEFAULT_RUN_EVER_MS;
			LOGGER.log(Level.INFO, "Init parameter 'sparrow.geoserver.sweeper.run.period' was not set. Sweeper will run every {0}ms", this.runEveryMs);
		}

		String envPrunedWorkspaces;
		try {
			envPrunedWorkspaces = template.lookup("java:comp/env/sparrow.geoserver.sweeper.workspaces.pruned", String.class);
		} catch (NamingException ex) {
			envPrunedWorkspaces = DEFAULT_PRUNED_WORKSPACES;
			LOGGER.log(Level.INFO, "Init parameter sparrow.geoserver.sweeper.workspaces.pruned was not set. Read only workspaces set to: {0}", envPrunedWorkspaces);
		}

		if (StringUtils.isNotBlank(envPrunedWorkspaces)) {
			this.prunedWorkspaces = envPrunedWorkspaces.split(",");
		}

		if (this.prunedWorkspaces.length != 0) {
			this.sweeperThread = new Thread(new SparrowSweeper(this.catalog, this.maxAge, this.prunedWorkspaces, this.runEveryMs), "sweeper-thread");
			this.sweeperThread.start();
		} else {
			// Failsafe
			LOGGER.log(Level.INFO, "Because there were no workspaces set, the sweeper process will not be started. If this is a mistake, set the parameter 'sparrow.geoserver.sweeper.workspaces.pruned' to a comma separated list of workspace names.");
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
 		10) Delete the dbf file
	 * 
	 */
	public static SweepResponse.DataStoreResponse pruneDataStore(Catalog dsCatalog, DataAccess<? extends FeatureType, ? extends Feature> da, DataStoreInfo dsInfo, File dbfFile) {
		
		synchronized (DELETE_LOCK) {
			SweepResponse.DataStoreResponse response = new SweepResponse.DataStoreResponse(dsInfo.getWorkspace().getName(), dsInfo.getName());

			LOGGER.log(Level.INFO, "==========> PRUNING DATASTORE [" + dsInfo.getName() + "]");

			// 1) Get all resource names associated with this store (all layer names)
			try {
				List<Name> resourceNames = da.getNames();
				if (!resourceNames.isEmpty()) {
					for (Name resourceName : resourceNames) {
						// 2) For each layer dsName, get the layer info object
						// 3) For each layer info object, detach the layer from the GeoServer Catalog
						// 4) For each layer info object, remove the layer completely from the GeoServer Catalog
						LayerInfo layerInfo = dsCatalog.getLayerByName(resourceName);
						
						//This could be null if the layer was deleted manually in the UI
						if (layerInfo != null) {
							response.layersDeleted.add(layerInfo.getName());
							dsCatalog.detach(layerInfo);
							dsCatalog.remove(layerInfo);
						}

						// 5) For each layer dsName, get the resource info object
						// 6) For each resource info object, detach the resource from the GeoServer Catalog
						// 7) For each resource info object, remove the resource completely from the GeoServer Catalog
						ResourceInfo resourceInfo = dsCatalog.getResourceByName(resourceName, ResourceInfo.class);
						
						if (resourceInfo != null) {
							dsCatalog.detach(resourceInfo);
							dsCatalog.remove(resourceInfo);
						}
					}
				}

				// 8) Clean up the GeoTools cache (DataAccess Object dispose() method)
				da.dispose();

				// 9) Delete the datastore itself (cBuilder.removeStore(dsInfo, false);)
				dsCatalog.detach(dsInfo);
				dsCatalog.remove(dsInfo);

				// 10) Delete the dbf file
				FileUtils.deleteQuietly(dbfFile);										
				response.isDeleted = true;

				LOGGER.log(Level.INFO, "===============> DATASTORE [" + dsInfo.getName() + "] HAS BEEN REMOVED");
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "A Sweeper exception has occurred during DataStore [" + dsInfo.getName() + "] removal.  Skipping DataStore and resuming...", e);
				response.err = e;
			}

			return response;
		}
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
				LOGGER.log(Level.INFO, "\n\n************************************************************\nRunning Sparrow DBF Sweep\n************************************************************\n");
				Long currentTime = new Date().getTime();

				// Get a cleaned list of workspaces
				List<WorkspaceInfo> workspaceInfoList = catalog.getWorkspaces();
				for (WorkspaceInfo wsInfo : workspaceInfoList) {
					if (!java.util.Arrays.asList(prunedWorkspaces).contains(wsInfo.getName())) {
						LOGGER.log(Level.FINEST, "\n\n=====> WORKSPACE [" + wsInfo.getName() + "] IS NOT LISTED TO BE PRUNED.  SKIPPING...");
						continue;
					} 

					LOGGER.log(Level.INFO, "\n\n----------\nCLEANING WORKSPACE [" + wsInfo.getName() + "]\n----------");

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
								LOGGER.log(Level.WARNING, "The sweeper found a DataStore [" + dsName + "] in the [" +
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
										LOGGER.log(Level.WARNING, "The sweeper found a DataStore [" + dsName + "] in the [" +
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
								
								DataAccess<? extends FeatureType, ? extends Feature> da = dsInfo.getDataStore(new DefaultProgressListener());
								File dbfFile = new File(dbaseLocation);
								response.deleted.add(GeoServerSparrowLayerSweeper.pruneDataStore(catalog, da,  dsInfo, dbfFile));	
								
							} else {
								SweepResponse.DataStoreResponse dsr = new SweepResponse.DataStoreResponse(wsInfo.getName(), dsName);
								response.kept.add(dsr);
							}
							
						} else {
							LOGGER.log(Level.FINE, "Skipping store '" + dsName + "' - does not match requested name pattern '" + namePattern + "'");
						}
						
					}


					if(response.hasDeletions()) {
						LOGGER.log(Level.INFO, "\n=====> " + response.deleted.size() + " DATASTORES WERE DELETED FROM WORKSPACE [" + wsInfo.getName() + "]");
					} else {
						LOGGER.log(Level.INFO, "\n=====> NO DATA WAS CLEANED FOR WORKSPACE [" + wsInfo.getName() + "]");
					}

					LOGGER.log(Level.INFO, "\n----------\nWORKSPACE [" + wsInfo.getName() + "] SWEEP COMPLETED\n----------");
				}

				LOGGER.log(Level.INFO, "\n\n************************************************************\nSparrow DBF Sweep Completed\n************************************************************\n\n");
			} catch (Exception ex) {
				LOGGER.log(Level.WARNING, "An error has occurred during execution of sweep", ex);
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
		LOGGER.log(Level.WARNING, "The sweeper found the DataStore [" + dsInfo.getName() + "] in workspace [" +
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
			
			LOGGER.log(Level.INFO, "\n\nCreating SparrowSweeper Instance:\n\tcatalog: " + this.catalog + "\n\tmaxAge: " + this.maxAge + "\n\tprunedWorkspaces: " + 
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
						LOGGER.log(Level.INFO, "Sweeper thread was interupted, but the keepRunning flag is true, so it will continue.");
					} else {
						LOGGER.log(Level.INFO, "Sweeper thread was interupted and the keepRunning flag is false, so it will stop.");
					}
					
				}
			}

		}
	}
	
}
