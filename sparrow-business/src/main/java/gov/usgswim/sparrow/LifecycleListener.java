package gov.usgswim.sparrow;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import gov.usgswim.sparrow.cachefactory.EhCacheConfigurationReader;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;
import net.sf.ehcache.constructs.blocking.SelfPopulatingCache;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;

import org.apache.log4j.extras.DOMConfigurator;

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

	public static final String APP_MODE_KEY = "application-mode";
	public static final String APP_ENV_KEY = "application-environment";
	
	public static final String APP_CACHE_CONFIG_FILE_KEY = "application-cache-config-file";
	
	/**
	 * Called when the context (the entire application) is being shut down.
	 * This method should properly shutdown the cache and any other shared resources.
	 */
	//TODO: [ee] This is set to clear always....
	public void contextDestroyed(ServletContextEvent context) {
		contextDestroyed(context, true);
	}

	public void contextDestroyed(ServletContextEvent context, boolean clearCache) {

		if (clearCache) {
			CacheManager cacheManager = SparrowCacheManager.getInstance();
			if ( cacheManager.getStatus() == Status.STATUS_ALIVE) {
				cacheManager.clearAll();
			} 
		}

		SparrowCacheManager.destroy();

		if (context != null) {
			//Only log this if we have an actual servlet context, ie, we are running on a server
			LogManager.getLogger(LifecycleListener.class).fatal("*** Server Shutdown ***");
		}
	}

	//TODO: [ee] This is set to clear always....
	public void contextInitialized(ServletContextEvent context) {
		contextInitialized(context, true);
	}
	/**
	 * Called when the context (the entire application) is initialize.
	 * @throws IOException 
	 * @throws CacheException 
	 */
	public void contextInitialized(ServletContextEvent context, boolean clearCache){
		try {
			
			
			//Add a system property of the hostname so that the logging system
			//can include the hostname in emails.
			String host = findHostname();
			System.setProperty("hostname", host);
			
			DynamicReadOnlyProperties props = SharedApplication.getInstance().getConfiguration();
			boolean isJndiAware = SharedApplication.getInstance().isUsingJndi();
			
			String mode = null;
			String env = null;
			
			
			//If we are in a servlet environment, switch to the production log4j config
			if (context != null) {
				env = props.getProperty(APP_ENV_KEY, "prod");
				mode = props.getProperty(APP_MODE_KEY, "prod");
				
			} else {
				env = props.getProperty(APP_ENV_KEY, "local");
				mode = props.getProperty(APP_MODE_KEY, "dev");
			}
			
			String logFileName = "/log4j_" + env + "_" + mode + ".xml";
			
			System.out.println("**** SPARROW Service app is switching to the log4j config file '" + logFileName + "'");
			
			URL log4jUrl = LifecycleListener.class.getResource(logFileName);
			LogManager.resetConfiguration();
			DOMConfigurator.configure(log4jUrl);
			
			String cacheConfigLocation = props.getProperty(
							APP_CACHE_CONFIG_FILE_KEY, 
							SparrowCacheManager.DEFAULT_EHCACHE_CONFIG_LOCATION);
			
			//Calling create here is not required, but gives a single place to customize
			//the creation of the singleton instance.
			SparrowCacheManager.createFromResource(cacheConfigLocation, isJndiAware);

			//
			//Set up ehcaches that have decorators
			//

			CacheManager cm = SparrowCacheManager.getInstance();

			if (clearCache) {
				cm.clearAll();
			}

			EhCacheConfigurationReader.verifyCacheConfiguration(isJndiAware);
			
			// Decorate as necessary with SelfPopulatingCache
			for (ConfiguredCache cache: ConfiguredCache.values()) {
				if (cache.factory != null && cache.isCached) {
					SelfPopulatingCache spCache = new SelfPopulatingCache(cm.getEhcache(cache.name()), cache.factory);
					cm.replaceCacheWithDecoratedCache(cm.getEhcache(cache.name()), spCache);
				}
			}
			
			if (context != null) {
				LogManager.getLogger(LifecycleListener.class).fatal("*** Server Startup (Sorry, not really fatal...) ***");
			}
			
		} catch (Exception e) {
			
			try {
				LogManager.getLogger(LifecycleListener.class).fatal("*** Server Startup FAILED", e);
			} finally {
				throw new RuntimeException(e);
			}
		}
	}
	
	
	
	
	/**
	 * Attempts to find the name of the machine, using a few different methods
	 * @return 
	 */
	private static String findHostname() {
	    String OS = System.getProperty("os.name").toLowerCase();
		String hostName = null;

        if (OS.contains("win")) {
            hostName = StringUtils.trimToNull(System.getenv("COMPUTERNAME"));
        } else if (OS.indexOf("nix") >= 0 || OS.indexOf("nux") >= 0) {
			try {
				hostName = StringUtils.trimToNull(execReadToString("hostname"));
			} catch (IOException ex) {
				//ignore
			}
        }
		
		if (hostName == null || hostName.equalsIgnoreCase("localhost")) {
			try {
				hostName = StringUtils.trimToNull(InetAddress.getLocalHost().getHostName());
			} catch (UnknownHostException ex) {
				//ignore
			}
		}
		
		if (hostName == null) {
			hostName = "unknown";
		}
		
		return hostName;
    }

    public static String execReadToString(String execCommand) throws IOException {
        Process proc = Runtime.getRuntime().exec(execCommand);
        try (InputStream stream = proc.getInputStream()) {
            try (Scanner s = new Scanner(stream).useDelimiter("\\A")) {
                return s.hasNext() ? s.next() : "";
            }
        }
    }

}
