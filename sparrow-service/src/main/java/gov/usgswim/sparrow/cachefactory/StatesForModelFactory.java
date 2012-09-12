package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadStatesForModel;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 * 
 * @author eeverman
 */
public class StatesForModelFactory implements CacheEntryFactory {


	@Override
	public DataTable createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		DataTable result = null;
		
		LoadStatesForModel action = new LoadStatesForModel(id);
		
		result = action.run();
		
		return result;
	}

}
