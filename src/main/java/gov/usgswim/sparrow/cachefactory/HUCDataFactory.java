package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadHUCData;
import gov.usgswim.sparrow.request.HUCDataRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class HUCDataFactory implements CacheEntryFactory {

	@Override
	public DataTable createEntry(Object inHUCDataRequest) throws Exception {
		HUCDataRequest hucRequest = (HUCDataRequest)inHUCDataRequest;
		DataTable hucs = null;
		LoadHUCData action = new LoadHUCData(hucRequest.getModelID(), hucRequest.getHucLevel());
		hucs = action.run();
		return hucs;
	}

}
