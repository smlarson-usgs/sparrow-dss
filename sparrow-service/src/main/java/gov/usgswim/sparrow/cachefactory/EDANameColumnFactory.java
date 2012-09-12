package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.sparrow.action.LoadEDANameColumn;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class EDANameColumnFactory implements CacheEntryFactory {


	@Override
	public ColumnData createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		ColumnData result = null;
		
		LoadEDANameColumn action = new LoadEDANameColumn();
		

		action.setModelId(id);
		result = action.run();
		
		return result;
	}

}
