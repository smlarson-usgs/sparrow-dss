package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.idbypoint.ModelPoint;

import java.awt.geom.Point2D.Double;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.config.CacheConfiguration;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.RMICacheReplicatorFactory;
import net.sf.ehcache.statistics.LiveCacheStatistics;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;

/**
 * This class reads the configuration information from the ehcache.xml (suitably modified by the SparrowCacheManager)
 * @author ilinkuo
 *
 */
public class EhCacheConfigurationReader {
	static private EhCacheConfigElement configuration = null;

	public static class EhCacheConfigElement{
		public List<EhCacheConfigurationReader.CacheConfigElement> caches;
		RMICacheManagerPeerProviderFactory cacheManagerPeerProviderFactory;
		RMICacheManagerPeerListenerFactory cacheManagerPeerListenerFactory;

		public boolean isDistributed() {
			return cacheManagerPeerListenerFactory != null && cacheManagerPeerProviderFactory != null;
		}
	}

	public static class CacheConfigElement{
		public String name;
		RMICacheReplicatorFactory cacheEventListenerFactory;
		public boolean isDistributed() {
			return cacheEventListenerFactory != null;
		}
	}

//	public static void outputConfig() throws IOException {
//
//		for (EhCacheConfigurationReader.CacheConfigElement cache: configuration.caches) {
//			System.out.println(cache.name + " listener? " + ((cache.isDistributed())? "yes" : ""));
//		}
//	}

	private synchronized static EhCacheConfigElement loadConfiguration(boolean isJndiAware) {
		InputStream inStream = null;
		StringBuilder xml = new StringBuilder();
		try {
			inStream = SparrowCacheManager.getConfigurationStream(isJndiAware);

			xml = DynamicReadOnlyProperties.readStream2StringBuilder(inStream);
		} catch (Exception excp) {
			excp.printStackTrace();
			System.err.println("Failed to read configuration");
			return null;
		} finally {
			try {
				if (inStream != null) inStream.close();
			} catch (Exception ex) {
			}
		}

		XStream xstream = new XStream(new DomDriver());
		xstream = configureXStream(xstream);
		EhCacheConfigElement config = (EhCacheConfigElement) xstream.fromXML(xml.toString());
		return config;
	}

	/**
	 * Configures XStream to properly read the ehcache.xml. This may have to be adjusted if further features are used
	 * @param xstream
	 * @return
	 */
	public static XStream configureXStream(XStream xstream) {
		xstream.alias("ehcache", EhCacheConfigurationReader.EhCacheConfigElement.class);
		xstream.useAttributeFor(EhCacheConfigurationReader.CacheConfigElement.class, "name");
		xstream.omitField(EhCacheConfigurationReader.EhCacheConfigElement.class, "diskStore");
		xstream.omitField(EhCacheConfigurationReader.EhCacheConfigElement.class, "defaultCache");
		//
		xstream.omitField(EhCacheConfigurationReader.EhCacheConfigElement.class, "cacheManagerPeerListenerFactory");
		xstream.omitField(EhCacheConfigurationReader.EhCacheConfigElement.class, "cacheManagerPeerProviderFactory");
		xstream.omitField(EhCacheConfigurationReader.CacheConfigElement.class, "cacheEventListenerFactory");

		//xstream.aliasField("name", CacheConfig.class, "name");
		//xstream.aliasField("name", CacheConfig.class, "name"); cacheManagerPeerProviderFactory

		xstream.addImplicitCollection(EhCacheConfigurationReader.EhCacheConfigElement.class, "caches", "cache", EhCacheConfigurationReader.CacheConfigElement.class);
		return xstream;
	}

	public static String key2String(Object key) {
		if (key instanceof Number) {
			return key.toString();
		} else if (key instanceof PredictionContext) {
			PredictionContext context = (PredictionContext) key;
			return context.getId().toString();
		} else if (key instanceof TerminalReaches) {
			TerminalReaches reaches = (TerminalReaches) key;
			return reaches.getId().toString();
		} else if (key instanceof UncertaintyDataRequest) {
			UncertaintyDataRequest req = (UncertaintyDataRequest) key;
			return UncertaintyDataRequest.class.getSimpleName() + "_M" + req.getModelId() + "_S" + req.getSourceId();
		} else if (key instanceof AdjustmentGroups) {
			AdjustmentGroups adj = (AdjustmentGroups) key;
			return adj.getId().toString();
		} else if (key instanceof ModelPoint) {
			Double point = ((ModelPoint) key).getPoint();
			return "(" + point.x + ", " + point.y + ")";
		} else if (key instanceof ReachID) {
			ReachID reach = (ReachID) key;
			return "reach_" + reach.getReachID();
		} else if (key instanceof ModelPoint) {
			Double point = ((ModelPoint) key).getPoint();
			return "(" + point.x + ", " + point.y + ")";
		}

		return key.toString();
	}

	/**
	 * Returns HTML text string showing the state of the cache, useful for debugging purposes
	 * @param showDetails
	 * @return
	 * TODO HTML stuff doesn't really belong in this class, but there isn't an ideal place to put it, so I'm leaving it here for now.
	 * @throws IOException 
	 */
	public static StringBuilder listDistributedCacheStatus(boolean showDetails) throws Exception {

//		outputConfig();

		StringBuilder result = new StringBuilder();
		// checking 
//		DynamicReadOnlyProperties sparrowProps = SparrowCacheManager.getProperties(isJndiAware);
//		String configKey = "cacheManagerPeerProviderFactory.properties";
//		result.append("CONFIG: " + configKey + " = " + sparrowProps.get(configKey) + "\n\n");


		String HEADER_FORMAT = "<font color='%s'>%s</font> ( %s objects of %s) %s %s ; transactions %s\n";
		String MEMORY_STATS_FORMAT = "\t  *memory used: %s bytes; memory store: %s; eviction policy: %s; time to live: %s; time to idle: %s;\n";
		String CLUSTER_STATS_FORMAT = "\t  *cluster coherent: %s; node coherent: %s\n";
		String HITS_AND_MISSES_FORMAT = "\t  *total HITS = mem + disk : %s = %s + %s; expired/total MISSES: %s/%s;\n";
		String REMOVALS_FORMAT = "\t  *updates/puts/removed/expired/evicted: %s/%s/%s/%s/%s\n";
		String PERFORMANCE_FORMAT = "\t  *average/min/max: %s/%s/%s\n";

		String ENTRY_FORMAT = "\t%s: %s\n";
		for (ConfiguredCache cache: ConfiguredCache.values()) {
			try {
				Ehcache c = SparrowCacheManager.getInstance().getEhcache(cache.name());
				CacheConfiguration config = c.getCacheConfiguration();

				List allKeys = c.getKeys();
				List liveKeys = c.getKeysWithExpiryCheck();

				// output cache header
				String memorySizeInKB = "";
				if (!showDetails) {
					long memSize = c.calculateInMemorySize();
					memorySizeInKB = Long.toString(memSize >>> 10) + " Kb";
				}
				String displayColor = (cache.isDistributed)? "green": "black";

				String cacheHeader = String.format(HEADER_FORMAT, 
						displayColor,
						cache,
						allKeys.size(), 
						config.getMaxElementsInMemory(),
						memorySizeInKB,
						(cache.isDistributed) ? "distributed": "", 
								config.getTransactionalMode());
				result.append(cacheHeader);

				if (showDetails) {
					// output memory stats
					String cacheMemoryStats = String.format(MEMORY_STATS_FORMAT,
							c.calculateInMemorySize(), 
							c.getMemoryStoreSize(),
							config.getMemoryStoreEvictionPolicy(),
							config.getTimeToLiveSeconds(),
							config.getTimeToIdleSeconds());
					result.append(cacheMemoryStats);
					// output cluster state
					String clusterState = String.format(CLUSTER_STATS_FORMAT,
							c.calculateInMemorySize(), 
							c.getMemoryStoreSize(),
							config.getMemoryStoreEvictionPolicy(),
							config.getTimeToLiveSeconds(),
							config.getTimeToIdleSeconds());
					result.append(clusterState);
					// output Live stats
					LiveCacheStatistics liveStats = c.getLiveCacheStatistics();
					if (liveStats != null && liveStats.isStatisticsEnabled()) {
						String hitsAndMisses = String.format(HITS_AND_MISSES_FORMAT, 
								liveStats.getCacheHitCount(),
								liveStats.getInMemoryHitCount(),
								liveStats.getOnDiskHitCount(),
								liveStats.getCacheMissCountExpired(),
								liveStats.getCacheMissCount()
						);
						result.append(hitsAndMisses);

						String removalStats = String.format(REMOVALS_FORMAT, 
								liveStats.getUpdateCount(),
								liveStats.getPutCount(),
								liveStats.getRemovedCount(),
								liveStats.getExpiredCount(),
								liveStats.getEvictedCount()
						);
						result.append(removalStats);
						String perfStats = String.format(PERFORMANCE_FORMAT, 
								liveStats.getAverageGetTimeMillis(),
								liveStats.getMinGetTimeMillis(),
								liveStats.getMaxGetTimeMillis()
						);
						result.append(perfStats);

					}
				}


				// output cache contents
				for (Object key: allKeys) {
					result.append(String.format(ENTRY_FORMAT, key2String(key), (liveKeys.contains(key))? "live": "expired"));
				}
			} catch (Exception e) {
				System.err.println("Unable to print distributed cache status");
			}
		}
		return result;
	}

	public static List getCacheKeys(String cacheName) {
		try {
			Ehcache c = SparrowCacheManager.getInstance().getEhcache(cacheName);
			return c.getKeys();
		} catch (Exception e) {
			System.err.println("Could not obtain a list of keys");
		}
		return null;
	}

	public static void verifyCacheConfiguration(boolean isJndiAware) {
		Set<String> configuredCaches = getConfiguredCaches(isJndiAware);
		Set<String> enumeratedCaches = getEnumeratedCaches(isJndiAware);
		compareCacheConfigurations(configuredCaches, enumeratedCaches);

		Set<String> configuredDistributedCaches = getConfiguredDistributedCaches(isJndiAware);
		Set<String> enumeratedDistributedCaches = getEnumeratedDistributedCaches(isJndiAware);
		String distributedCacheWarningMessage = compareDistributedCacheConfigurations(configuredDistributedCaches, enumeratedDistributedCaches);
		System.out.println(distributedCacheWarningMessage);
	}


	private static String compareDistributedCacheConfigurations(
			final Set<String> configuredDistributedCaches,
			final Set<String> enumeratedDistributedCaches) {
		StringBuilder result = new StringBuilder();

		{
			Set<String> unEnumeratedCaches = new HashSet<String>(configuredDistributedCaches);
			unEnumeratedCaches.removeAll(enumeratedDistributedCaches);
			if (!unEnumeratedCaches.isEmpty()) {
				result.append("WARN: The following caches should be configured as distributed caches: ");
				for (String cache: unEnumeratedCaches) {
					result.append(cache).append(", ");
				}
				result.append("\n");
			}
		}
		{
			Set<String> unConfiguredCaches = new HashSet<String>(enumeratedDistributedCaches);
			unConfiguredCaches.removeAll(configuredDistributedCaches);
			if (!unConfiguredCaches.isEmpty()) {
				result.append("WARN: The following caches have been configured as distributed caches but not identified as so in code: ");
				for (String cache: unConfiguredCaches) {
					result.append(cache).append(", ");
				}
				result.append("\n");
			}
		}
		return result.toString();
	}

	public static void compareCacheConfigurations(
			final Set<String> configuredCaches, final Set<String> enumeratedCaches) {
		Set<String> unEnumeratedCaches = new HashSet<String>(configuredCaches);
		unEnumeratedCaches.removeAll(enumeratedCaches);

		Set<String> unConfiguredCaches = new HashSet<String>(enumeratedCaches);
		unConfiguredCaches.removeAll(configuredCaches);

		if (!unEnumeratedCaches.isEmpty()) {
			StringBuilder unEnumeratedCacheList = new StringBuilder("Unenumerated caches: ");
			for (String cache: unEnumeratedCaches) {
				unEnumeratedCacheList.append(cache).append(", ");
			}
			throw new RuntimeException("Cache configuration aborted due to " + unEnumeratedCacheList);
		}

		if (!unConfiguredCaches.isEmpty()) {
			StringBuilder unConfiguredCacheList = new StringBuilder("unconfigured caches: ");
			for (String cache: unConfiguredCaches) {
				unConfiguredCacheList.append(cache).append(", ");
			}
			throw new RuntimeException("Cache configuration aborted due to " + unConfiguredCacheList);
		}
	}

	public static Set<String> getEnumeratedCaches(boolean isJndiAware) {
		if (configuration == null) {
			configuration = loadConfiguration(isJndiAware);
		}
		
		ConfiguredCache[] allCaches = ConfiguredCache.values();
		Set<String> result = new HashSet<String>();
		for (ConfiguredCache cache: allCaches) {
			if (cache.isCached) {
				result.add(cache.name());
			}
		}
		return result;
	}

	public static Set<String> getConfiguredCaches(boolean isJndiAware) {
		if (configuration == null) {
			configuration = loadConfiguration(isJndiAware);
		}
		
		Set<String> result = new HashSet<String>();
		for (CacheConfigElement cache: configuration.caches) {
			result.add(cache.name);
		}
		return result;
	}

	public static Set<String> getEnumeratedDistributedCaches(boolean isJndiAware) {
		if (configuration == null) {
			configuration = loadConfiguration(isJndiAware);
		}
		
		ConfiguredCache[] allCaches = ConfiguredCache.values();
		Set<String> result = new HashSet<String>();
		for (ConfiguredCache cache: allCaches) {
			if (cache.isDistributed) result.add(cache.name());
		}
		return result;
	}

	public static Set<String> getConfiguredDistributedCaches(boolean isJndiAware) {
		if (configuration == null) {
			configuration = loadConfiguration(isJndiAware);
		}
		
		Set<String> result = new HashSet<String>();
		for (CacheConfigElement cache: configuration.caches) {
			if (cache.isDistributed()) result.add(cache.name);
		}
		return result;
	}
}
