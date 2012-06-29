package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadPredefinedWatershedsForModel;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class LoadPredefinedWatershedsForModelFactory extends AbstractCacheFactory {

	@Override
	public DataTable createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		DataTable result = null;
		
		LoadPredefinedWatershedsForModel action = new LoadPredefinedWatershedsForModel(id);
		
		result = action.run();
		
		return result;
	}
}
