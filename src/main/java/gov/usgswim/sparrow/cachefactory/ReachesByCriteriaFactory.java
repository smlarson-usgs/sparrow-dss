package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.CalcReachesByNavigation;
import gov.usgswim.sparrow.action.LoadReachesInHuc;
import gov.usgswim.sparrow.domain.Criteria;

import org.apache.log4j.Logger;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 * 
 * This factoy does a bit more logic than most:  It decides which action to
 * run based on the type of criteria.
 * 
 * @author eeverman
 */
public class ReachesByCriteriaFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(ReachesByCriteriaFactory.class); //logging for this class
	
	@Override
	public long[] createEntry(Object logicalSetCriteria) throws Exception {
		Criteria criteria = (Criteria) logicalSetCriteria;
		
		// Branching code based on criteria type
		if (criteria.getCriteriaType().isHucCriteria()) {
			LoadReachesInHuc action = new LoadReachesInHuc();
			action.setCriteria(criteria);
			long[] results = action.run();
			return results;
		} else {
			CalcReachesByNavigation action = new CalcReachesByNavigation();
			action.setCriteria(criteria);
			long[] results = action.run();
			return results;
		}
	}
}
