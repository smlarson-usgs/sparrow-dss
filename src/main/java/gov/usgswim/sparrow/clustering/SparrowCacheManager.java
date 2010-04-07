package gov.usgswim.sparrow.clustering;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgs.cida.config.DynamicReadOnlyProperties.NullKeyHandlingOption;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import org.apache.commons.lang.text.StrBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.management.ManagementService;

public class SparrowCacheManager {
	public static final String[] EXPECTED_JNDI_CONFIG_VALUES = 
		{	"cacheManagerPeerProviderFactory.class",
			"cacheManagerPeerProviderFactory.properties",
			"cacheManagerPeerListenerFactory.class",
			"cacheManagerPeerListenerFactory.properties",
			"cacheEventListenerFactory.class",
			"cacheEventListenerFactory.properties"
		};
	
	private static final Logger LOG = LoggerFactory.getLogger(CacheManager.class);

	/**
	 * The Singleton Instance.
	 */
	private static volatile CacheManager singleton;

	/**
	 * A factory method to create a singleton CacheManager with default config, or return it if it exists.
	 * <p/>
	 * The configuration will be read, {@link Ehcache}s created and required stores initialized. When the {@link CacheManager} is no longer
	 * required, call shutdown to free resources.
	 * 
	 * @return the singleton CacheManager
	 * @throws CacheException
	 *             if the CacheManager cannot be created
	 */
	public static CacheManager create() throws CacheException {
		if (singleton != null) {
			return singleton;
		}

		synchronized (CacheManager.class) {
			if (singleton == null) {
				// CacheManager created in this way will not have JNDI property substitution
				LOG.debug("Creating new SparrowCacheManager with default config");
				singleton = new CacheManager();
			} else {
				LOG.debug("Attempting to create an existing singleton. Existing singleton returned.");
			}
			return singleton;
		}
	}


	/**
	 * Creates singleton CacheManager from resource name
	 * @return
	 * @throws CacheException
	 * @throws IOException 
	 */
	public static CacheManager createFromResource(String resourceName) throws CacheException, IOException {

		synchronized (CacheManager.class) {
			LOG.debug("Creating new SparrowCacheManager from resource " + resourceName);
			
			InputStream in = SparrowCacheManager.class.getResourceAsStream(resourceName);
			if (in == null) {
				LOG.error(" resource " + resourceName + " not found. Make sure your resource name contains a '/'");
				throw new CacheException("unable to initialize cache due to resource " +  resourceName + " not found");
			}
			DynamicReadOnlyProperties dynProps = getProperties();
			in = dynProps.expand(in);
			
			try {
			    singleton = new CacheManager(in);
			} finally {
			    in.close();
			}
			
			// register cache in MBeanServer for JMX
			MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		    ManagementService.registerMBeans(singleton, mBeanServer, false, false, false, true);
		    
			return singleton;
		}
	}
	
	public static DynamicReadOnlyProperties getProperties() {
		DynamicReadOnlyProperties dynProps = new DynamicReadOnlyProperties();
		dynProps.addJNDIContexts(DynamicReadOnlyProperties.DEFAULT_JNDI_CONTEXTS);
		return dynProps;
	}


	/**
	 * A factory method to create a singleton CacheManager with default config, or return it if it exists.
	 * <p/>
	 * This has the same effect as {@link CacheManager#create}
	 * <p/>
	 * Same as {@link #create()}
	 * 
	 * @return the singleton CacheManager
	 * @throws CacheException
	 *             if the CacheManager cannot be created
	 */
	public static CacheManager getInstance() throws CacheException {
		return create();
	}


}
