package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.sparrow.action.LoadEDACodeColumn;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class EDACodeColumnFactory implements CacheEntryFactory {


	@Override
	public ColumnData createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		ColumnData result = null;
		
		LoadEDACodeColumn action = new LoadEDACodeColumn();
		

		action.setModelId(id);
		result = action.run();
		
		return result;
	}

}
