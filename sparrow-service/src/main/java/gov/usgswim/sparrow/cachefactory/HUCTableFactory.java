package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadHUCTable;
import gov.usgswim.sparrow.request.HUCTableRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class HUCTableFactory implements CacheEntryFactory {

	@Override
	public DataTable createEntry(Object inHUCDataRequest) throws Exception {
		HUCTableRequest hucRequest = (HUCTableRequest)inHUCDataRequest;
		DataTable hucs = null;
		LoadHUCTable action = new LoadHUCTable(hucRequest.getModelID());
		hucs = action.run();
		return hucs;
	}

}
