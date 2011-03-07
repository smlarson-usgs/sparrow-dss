package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.ReachesByCriteria;
import gov.usgswim.sparrow.domain.Criteria;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 * 
 * @author eeverman
 */
public class ReachesByCriteriaFactory extends AbstractCacheFactory {

	@Override
	public long[] createEntry(Object criteriaInstance) throws Exception {
		
		Criteria criteria = (Criteria)criteriaInstance;
		
		ReachesByCriteria action = new ReachesByCriteria();
		action.setCriteria(criteria);
		
		long[] result = action.run();
		return result;
	}
}
