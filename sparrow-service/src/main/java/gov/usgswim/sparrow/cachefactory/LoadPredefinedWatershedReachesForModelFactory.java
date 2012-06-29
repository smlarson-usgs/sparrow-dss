package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadPredefinedWatershedReachesForModel;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class LoadPredefinedWatershedReachesForModelFactory extends AbstractCacheFactory {

	@Override
	public DataTable createEntry(Object watershedId) throws Exception {
		Long id = (Long)watershedId;
		DataTable result = null;
		
		LoadPredefinedWatershedReachesForModel action = new LoadPredefinedWatershedReachesForModel(id);
		
		result = action.run();
		
		return result;
	}
}
