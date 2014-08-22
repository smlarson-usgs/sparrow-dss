package org.geoserver.sparrow.util;

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
import org.geoserver.catalog.CatalogBuilder;
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
import org.springframework.jndi.JndiTemplate;

public class GeoserverSweeperStartupListener implements InitializingBean {
	protected static final Logger LOGGER = org.geotools.util.logging.Logging.getLogger("org.geoserver.sparrow.util");
	private static final Long DEFAULT_MAX_LAYER_AGE = 2592000000l; // 30d
	private static final Long DEFAULT_RUN_EVER_MS = 3600000l; // 1h
	private static final String DEFAULT_PRUNED_WORKSPACES = "sparrow-catchment,sparrow-flowline";
	private static final String DBASE_KEY = "dbase_file";
	private static final String DBASE_TIME_KEY = "lastUsedMS";
	private Long maxAge;
	private Long runEveryMs;
	private Catalog catalog;
	private String[] prunedWorkspaces;
	private Thread sweeperThread;

	public GeoserverSweeperStartupListener(Catalog catalog) {
		this.catalog = catalog;
	}

	public void destroy() throws Exception {
		LOGGER.log(Level.INFO, "Sweeper thread is shutting down");
		this.sweeperThread.interrupt();
		this.sweeperThread.join(this.runEveryMs + 60000);
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
			LOGGER.log(Level.INFO, "Because there were no workspaces set to read-only, sweeper will not run. If this is a mistake, set the parameter 'sparrow.geoserver.sweeper.workspaces.read-only' to any workspace. The workspace does not need to actually exist.");
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
	 * 
	 * 		1) Get all resource names associated with this store (all layer names)
	 * 		2) For each layer name, get the layer info object
	 * 		3) For each layer info object, detach the layer from the GeoServer Catalog
	 * 		4) For each layer info object, remove the layer completely from the GeoServer Catalog
	 * 		5) For each layer name, get the resource info object
	 * 		6) For each resource info object, detach the resource from the GeoServer Catalog
	 * 		7) For each resource info object, remove the resource completely from the GeoServer Catalog
	 * 		8) Clean up the GeoTools cache (DataAccess Object dispose() method)
	 * 		9) Delete the datastore itself (cBuilder.removeStore(dsInfo, false);)
	 * 		10) Delete the dbf file
	 * 
	 */
	public static boolean pruneDataStore(CatalogBuilder cBuilder, Catalog dsCatalog, DataAccess<? extends FeatureType, ? extends Feature> da, DataStoreInfo dsInfo, File dbfFile) {
		boolean removedData = false;
		
		LOGGER.log(Level.INFO, "==========> PRUNING DATASTORE [" + dsInfo.getName() + "]");
		
		// 1) Get all resource names associated with this store (all layer names)
		try {
			List<Name> resourceNames = da.getNames();
			if (!resourceNames.isEmpty()) {
				for (Name resourceName : resourceNames) {
					// 2) For each layer name, get the layer info object
					// 3) For each layer info object, detach the layer from the GeoServer Catalog
					// 4) For each layer info object, remove the layer completely from the GeoServer Catalog
					LayerInfo layerInfo = dsCatalog.getLayerByName(resourceName);
					dsCatalog.detach(layerInfo);
					dsCatalog.remove(layerInfo);
					
					// 5) For each layer name, get the resource info object
					// 6) For each resource info object, detach the resource from the GeoServer Catalog
					// 7) For each resource info object, remove the resource completely from the GeoServer Catalog
					ResourceInfo resourceInfo = dsCatalog.getResourceByName(resourceName, ResourceInfo.class);
					dsCatalog.detach(resourceInfo);
					dsCatalog.remove(resourceInfo);
				}
			}
			
			// 8) Clean up the GeoTools cache (DataAccess Object dispose() method)
			da.dispose();
			
			// 9) Delete the datastore itself (cBuilder.removeStore(dsInfo, false);)
			cBuilder.removeStore(dsInfo, false); 	// Use false here so it doesnt inadvertantly delete the common shapefile
			
			// 10) Delete the dbf file
			FileUtils.deleteQuietly(dbfFile);										
			removedData = true;
			
			LOGGER.log(Level.INFO, "===============> DATASTORE [" + dsInfo.getName() + "] HAS BEEN REMOVED");
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "A Sweeper exception has occurred during DataStore [" + dsInfo.getName() + "] removal.  Skipping DataStore and resuming...", e);
		}
		
		return removedData;
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
			while (!Thread.interrupted()) {
				try {
					LOGGER.log(Level.INFO, "\n\n************************************************************\nRunning Sparrow DBF Sweep\n************************************************************\n");
					Long currentTime = new Date().getTime();
					CatalogBuilder cBuilder = new CatalogBuilder(catalog);

					// Get a cleaned list of workspaces
					List<WorkspaceInfo> workspaceInfoList = catalog.getWorkspaces();
					for (WorkspaceInfo wsInfo : workspaceInfoList) {
						if (!java.util.Arrays.asList(prunedWorkspaces).contains(wsInfo.getName())) {
							LOGGER.log(Level.INFO, "\n\n=====> WORKSPACE [" + wsInfo.getName() + "] IS NOT LISTED TO BE PRUNED.  SKIPPING...");
							continue;
						} 
						
						LOGGER.log(Level.INFO, "\n\n----------\nCLEANING WORKSPACE [" + wsInfo.getName() + "]\n----------");
						boolean removedData = false;
						
						List<DataStoreInfo> dsInfoList = catalog.getDataStoresByWorkspace(wsInfo);

						if (!dsInfoList.isEmpty()) {							
							for (DataStoreInfo dsInfo : dsInfoList) {
								DataAccess<? extends FeatureType, ? extends Feature> da = dsInfo.getDataStore(new DefaultProgressListener());
								
								/**
								 * Lets get the dbf filename for this specific datastore
								 */
								Map<String, Serializable> connectionParams = dsInfo.getConnectionParameters();
								
								if(connectionParams == null) {
									/**
									 * This should not be null for this type of datastore
									 */
									LOGGER.log(Level.WARNING, "The sweeper found an incorrectly configured DataStore in the [" +
											wsInfo.getName() + "] workspace.  DataStore [" + dsInfo.getName() + "] has no connection parameters " +
											"configured.  Skipping DataStore...");
									continue;
								}
								
								Object dbaseLocationObj = connectionParams.get(DBASE_KEY);
								if(dbaseLocationObj == null) {
									continue;
								}								
								
								String dbaseLocation = null;								
								if(dbaseLocationObj instanceof URL) {
									dbaseLocation = ((URL)dbaseLocationObj).toString();
								} else if(dbaseLocationObj instanceof String) {
									dbaseLocation = (String)dbaseLocationObj;
								} else {
									/**
									 * This should either be a String or a URL.
									 */
									LOGGER.log(Level.WARNING, "The sweeper found an incorrectly configured DataStore in the [" +
											wsInfo.getName() + "] workspace.  DataStore [" + dsInfo.getName() + "] has an unknown dbase_file " +
											"object type.  Skipping DataStore...");
									continue;
								}
								
								if((dbaseLocation != null) && (!dbaseLocation.equals(""))) {
									dbaseLocation = dbaseLocation.replace("file:", "");
								} else {
									/**
									 * DBF file mentioned is not valid.  Continue on to the next one
									 */
									continue;
								}
								
								/**
								 * Lets get the age of this dbf file which is embedded in an 
								 * attribute for the datastore "lastUsedMS"
								 */
								Long fileAge = 0L;
								Object ageObject = connectionParams.get(DBASE_TIME_KEY);
								
								if(ageObject == null) {
									LOGGER.log(Level.WARNING, "The sweeper found a DataStore [" + dsInfo.getName() + "] in the [" +
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
											LOGGER.log(Level.WARNING, "The sweeper found a DataStore [" + dsInfo.getName() + "] in the [" +
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
								if (currentTime - fileAge > this.maxAge) {
									File dbfFile = new File(dbaseLocation);
									removedData = GeoserverSweeperStartupListener.pruneDataStore(cBuilder, catalog, da,  dsInfo, dbfFile);							
								}
							}
						}
						
						if(removedData) {
							LOGGER.log(Level.INFO, "\n=====> DATA WAS CLEANED FOR WORKSPACE [" + wsInfo.getName() + "]");
						} else {
							LOGGER.log(Level.INFO, "\n=====> NO DATA WAS CLEANED FOR WORKSPACE [" + wsInfo.getName() + "]");
						}
						
						LOGGER.log(Level.INFO, "\n----------\nWORKSPACE [" + wsInfo.getName() + "] SWEEP COMPLETED\n----------");
					}

					LOGGER.log(Level.INFO, "\n\n************************************************************\nSparrow DBF Sweep Completed\n************************************************************\n\n");
				} catch (Exception ex) {
					LOGGER.log(Level.WARNING, "An error has occurred during execution of sweep", ex);
				} finally {
					// Clean up
				}
				try {
					// TODO: Use ThreadPoolExecutor to do this - ( http://docs.oracle.com/javase/6/docs/api/java/util/concurrent/ThreadPoolExecutor.html ) 
					Thread.sleep(runEveryMs);
				} catch (InterruptedException ex) {
					LOGGER.log(Level.INFO, "Sweeper thread is shutting down");
				}
			}

		}
	}
}
