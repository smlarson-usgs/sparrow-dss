package gov.usgswim.sparrow.clustering;

import gov.usgs.cida.config.DynamicReadOnlyProperties;

import java.io.IOException;
import java.io.InputStream;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;

import javax.naming.NamingException;
import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.management.ManagementService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The purpose of this class is to decorate the configuration loading of the
 * ehcache CacheManager by performing property substitution on the ehcache.xml
 * configuration file. As a result of doing this, the CacheManager no longer
 will manage its singleCacheManger, so that must be managed here.
 * 
 * @author ilinkuo
 * 
 */
public class SparrowCacheManager {
	public static final String[] EXPECTED_JNDI_CONFIG_VALUES = 
		{	"cacheManagerPeerProviderFactory.class",
			"cacheManagerPeerProviderFactory.properties",
			"cacheManagerPeerListenerFactory.class",
			"cacheManagerPeerListenerFactory.properties",
			"cacheEventListenerFactory.class",
			"cacheEventListenerFactory.properties"
		};
	
	public static final String DEFAULT_EHCACHE_CONFIG_LOCATION = "/ehcache.xml";
	
	private static final Logger LOG = LoggerFactory.getLogger(SparrowCacheManager.class);

	/**
	 * The Singleton cache Instance.
	 */
	private static volatile CacheManager singleCacheManger;
	
	/**
	 * Single cache manager - provides some hooks into an mbean server.
	 */
	private static volatile ManagementService singleCacheManagementService;

	/**
	 * A factory method to create a singleCacheManger CacheManager with default config, or return it if it exists.
	 * <p/>
	 * The configuration will be read, {@link Ehcache}s created and required stores initialized. When the {@link CacheManager} is no longer
	 * required, call shutdown to free resources.
	 * 
	 * @return the singleCacheManger CacheManager
	 * @throws CacheException
	 *             if the CacheManager cannot be created
	 */
	public synchronized static CacheManager create() throws CacheException {
		if (singleCacheManger != null) {
			return singleCacheManger;
		}

		if (singleCacheManger == null) {
			// CacheManager created in this way will not have JNDI property substitution
			LOG.warn("Creating new SparrowCacheManager with default config, no JNDI property substitution");
			singleCacheManger = new CacheManager();
		} else {
			LOG.debug("Attempting to create an existing singleton unneeded. Existing singleton returned.");
		}
		return singleCacheManger;
	}


	/**
	 * Creates singleCacheManger CacheManager from resource name
	 * @return
	 * @throws CacheException
	 * @throws IOException 
	 */
	public synchronized static CacheManager createFromResource(String resourceName, boolean isJndiAware) throws CacheException, IOException {

		LOG.info("Creating new SparrowCacheManager from resource " + resourceName);

		InputStream in = null;

		try {
			in = getConfigurationStream(isJndiAware, resourceName);
			singleCacheManger = new CacheManager(in);
			singleCacheManger.setName("SparrowCacheManager");

		} catch (Exception e){
			LOG.error("SparrowCacheManager was not able to create configured cache manager");
		} finally {
			in.close();
		}

		// register cache in MBeanServer for JMX
		MBeanServer mBeanServer = ManagementFactory.getPlatformMBeanServer();
		singleCacheManagementService  = new ManagementService(singleCacheManger, mBeanServer, false, false, false, true);

		return singleCacheManger;
	}
	
	public synchronized static void destroy() {
		
		if (singleCacheManger != null) {
			singleCacheManger.shutdown();
			singleCacheManger = null;
		}
		
		if (singleCacheManagementService != null) {
			singleCacheManagementService.dispose();
			singleCacheManagementService = null;
		}

	}


	/**
	 * @param resourceName optional. If not used or null passed in, then DEFAULT_EHCACHE_CONFIG_LOCATION is used.
	 * @return
	 * @throws IOException
	 */
	public static InputStream getConfigurationStream(boolean isJndiAware, String... resourceName)
			throws IOException, NamingException {
		String resName = DEFAULT_EHCACHE_CONFIG_LOCATION;
		if (resourceName != null && resourceName.length > 0 && resourceName[0] != null ) {
			resName = resourceName[0];
		}
		
		InputStream in = SparrowCacheManager.class.getResourceAsStream(resName);
		if (in == null) {
			LOG.error(" resource " + resName + " not found. Make sure your resource name contains a '/'");
			throw new CacheException("unable to initialize cache due to resource " +  resName + " not found");
		}
		DynamicReadOnlyProperties dynProps = getProperties(isJndiAware);
		in = dynProps.expand(in);
		return in;
	}
	

	
	public static DynamicReadOnlyProperties getProperties(boolean isJndiAware) throws NamingException {
		DynamicReadOnlyProperties dynProps = new DynamicReadOnlyProperties();
		
		if (isJndiAware) {
			dynProps.addJNDIContexts(DynamicReadOnlyProperties.DEFAULT_JNDI_CONTEXTS);
		}
		return dynProps;
	}


	/**
	 * A factory method to create a singleCacheManger CacheManager with default config, or return it if it exists.
	 * <p/>
	 * This has the same effect as {@link CacheManager#create}
	 * <p/>
	 * Same as {@link #create()}
	 * 
	 * @return the singleCacheManger CacheManager
	 * @throws CacheException
	 *             if the CacheManager cannot be created
	 */
	public static CacheManager getInstance() throws CacheException {
		return create();
	}


}
