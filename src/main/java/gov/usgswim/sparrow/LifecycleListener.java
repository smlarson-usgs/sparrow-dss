package gov.usgswim.sparrow;

import gov.usgswim.sparrow.cachefactory.EhCacheConfigurationReader;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.service.ConfiguredCache;

import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;

import org.apache.log4j.Logger;

/**
 * This class should be registered as listener in the servlet container the
 * application is deployed to.  Example web.xml config:
 * <p>
 * <code>
 * &lt;listener>
 * 	&lt;listener-class>gov.usgswim.sparrow.LifecycleListener&lt;/listener-class>
 * &lt;/listener>
 * ...&lt;servlet> and other elements in the document...
 * </code>
 * <p>
 * If the SPARROW application is used in other contexts (non-web container),
 * the event methods should still be called with null contexts.  Failure to 
 * call the contextDestroyed event will cause the cache to become corrupted and
 * objects cached to disk will be discarded when the application restarts.
 * 
 * Note that the named caches listed here must be configured in the root
 * ehcache.xml file.
 * 
 * @author eeverman
 *
 */
public class LifecycleListener implements ServletContextListener { 
	protected static Logger log =
		Logger.getLogger(LifecycleListener.class); //logging for this class

	/**
	 * Called when the context (the entire application) is being shut down.
	 * This method should properly shutdown the cache and any other shared resources.
	 */
	//TODO: [ee] This is set to clear always....
	public void contextDestroyed(ServletContextEvent context) {
		contextDestroyed(context, true);
	}

	public void contextDestroyed(ServletContextEvent context, boolean clearCache) {
		if (context != null) {
			log.info("Stopping the SPARROW application within a servlet context - shutting down the cache");
		} else {
			log.info("Stopping the SPARROW application (non-servlet deployment) - shutting down the cache");
		}

		if (clearCache) {
			log.info("Clearing the cache as requested");
			CacheManager cacheManager = SparrowCacheManager.getInstance();
			if ( cacheManager.getStatus() == Status.STATUS_ALIVE) {
				cacheManager.clearAll();
			} 
		}

		SparrowCacheManager.getInstance().shutdown();

	}

	//TODO: [ee] This is set to clear always....
	public void contextInitialized(ServletContextEvent context) {
		try {
			contextInitialized(context, true);
		} catch (Exception ex) {
			log.fatal("Unable to initialize context", ex);
		} 
	}
	/**
	 * Called when the context (the entire application) is initialize.
	 * @throws IOException 
	 * @throws CacheException 
	 */
	public void contextInitialized(ServletContextEvent context, boolean clearCache){
		try {

			if (context != null) {
				log.info("Starting the SPARROW application within a servlet context - (no init tasks)");
				//Nothing to do
			} else {
				log.info("Starting the SPARROW application (non-servlet deployment) - (no init tasks)");
				//Nothing to do
			}

			//Calling create here is not required, but gives a single place to customize
			//the creation of the singleton instance.
			SparrowCacheManager.createFromResource(SparrowCacheManager.DEFAULT_EHCACHE_CONFIG_LOCATION);

			//
			//Set up ehcaches that have decorators
			//

			CacheManager cm = SparrowCacheManager.getInstance();

			if (clearCache) {
				log.info("Clearing the SPARROW cache as requested.");
				cm.clearAll();
			}

			EhCacheConfigurationReader.verifyCacheConfiguration();
			
			// Decorate as necessary with SelfPopulatingCache
			for (ConfiguredCache cache: ConfiguredCache.values()) {
				if (cache.factory != null) {
					SelfPopulatingCache spCache = new SelfPopulatingCache(cm.getEhcache(cache.name()), cache.factory);
					cm.replaceCacheWithDecoratedCache(cm.getEhcache(cache.name()), spCache);
				}
			}
			
		} catch (Exception e) {
			log.error("Error occured during " + this.getClass().getSimpleName() + ".contextInitialized() ehcache initialization ", e);
			throw new RuntimeException(e);
		}
	}

}
