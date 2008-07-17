package gov.usgswim.sparrow;

import gov.usgswim.sparrow.cachefactory.*;
import gov.usgswim.sparrow.service.SharedApplication;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.ehcache.CacheManager;
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
			CacheManager.getInstance().clearAll();
		}
		
		CacheManager.getInstance().shutdown();
		
	}

	//TODO: [ee] This is set to clear always....
	public void contextInitialized(ServletContextEvent context) {
		contextInitialized(context, true);
	}
	/**
	 * Called when the context (the entire application) is initialize.
	 */
	public void contextInitialized(ServletContextEvent context, boolean clearCache) {
		if (context != null) {
			log.info("Starting the SPARROW application within a servlet context - (no init tasks)");
			//Nothing to do
		} else {
			log.info("Starting the SPARROW application (non-servlet deployment) - (no init tasks)");
			//Nothing to do
		}
		
		//Calling create here is not required, but gives a single place to customize
		//the creation of the singleton instance.
		CacheManager.create();
		
		//
		//Set up ehcaches that have decorators
		//
		
		CacheManager cm = CacheManager.getInstance();
		
		if (clearCache) {
			log.info("Clearing the SPARROW cache as requested.");
			cm.clearAll();
		}
		
		
		//PredictDataCache
		SelfPopulatingCache predictDataCache = new SelfPopulatingCache(cm.getEhcache(SharedApplication.PREDICT_DATA_CACHE), new PredictDataFactory());
		cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.PREDICT_DATA_CACHE), predictDataCache);
		
		//adjustedDataCache
		SelfPopulatingCache adjustedDataCache = new SelfPopulatingCache(cm.getEhcache(SharedApplication.ADJUSTED_SOURCE_CACHE), new AdjustedSourceFactory());
		cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.ADJUSTED_SOURCE_CACHE), adjustedDataCache);
		
		//PredictResultCache
		SelfPopulatingCache predictResultCache = new SelfPopulatingCache(cm.getEhcache(SharedApplication.PREDICT_RESULT_CACHE), new PredictResultFactory());
		cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.PREDICT_RESULT_CACHE), predictResultCache);
		
		//AnalysisResultCache
		SelfPopulatingCache predictAnalysisCache = new SelfPopulatingCache(cm.getEhcache(SharedApplication.ANALYSIS_RESULT_CACHE), new AnalysisResultFactory());
		cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.ANALYSIS_RESULT_CACHE), predictAnalysisCache);
		
		//IdentifyReachByPoint
		SelfPopulatingCache reachByPointCache = new SelfPopulatingCache(cm.getEhcache(SharedApplication.IDENTIFY_REACH_BY_POINT), new ReachByPointFactory());
		cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.IDENTIFY_REACH_BY_POINT), reachByPointCache);
		
		//IdentifyReachByID
		SelfPopulatingCache reachByIDCache = new SelfPopulatingCache(cm.getEhcache(SharedApplication.IDENTIFY_REACH_BY_ID), new ReachByIDFactory());
		cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.IDENTIFY_REACH_BY_ID), reachByIDCache);

		//ReachesByCriteria
		SelfPopulatingCache reachesByCriteria = new SelfPopulatingCache(cm.getEhcache(SharedApplication.REACHES_BY_CRITERIA), new ReachesByCriteriaFactory());
		cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.REACHES_BY_CRITERIA), reachesByCriteria);
		
		//DataBinning
        SelfPopulatingCache dataBinning = new SelfPopulatingCache(cm.getEhcache(SharedApplication.DATA_BINNING), new BinningFactory());
        cm.replaceCacheWithDecoratedCache(cm.getEhcache(SharedApplication.DATA_BINNING), dataBinning);
	}

}
