package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadHUC8Table;
import gov.usgswim.sparrow.request.HUC8TableRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class HUC8TableFactory implements CacheEntryFactory {

	@Override
	public DataTable createEntry(Object inHUCDataRequest) throws Exception {
		HUC8TableRequest hucRequest = (HUC8TableRequest)inHUCDataRequest;
		DataTable hucs = null;
		LoadHUC8Table action = new LoadHUC8Table(hucRequest.getModelID());
		hucs = action.run();
		return hucs;
	}

}
