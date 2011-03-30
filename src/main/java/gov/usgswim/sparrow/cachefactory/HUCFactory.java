package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadHUCDetail;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.request.HUCRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class HUCFactory implements CacheEntryFactory {

	@Override
	public HUC createEntry(Object inHUCRequest) throws Exception {
		HUCRequest hucRequest = (HUCRequest)inHUCRequest;
		HUC huc = null;
		LoadHUCDetail action = new LoadHUCDetail(hucRequest);
		huc = action.run();
		return huc;
	}

}
