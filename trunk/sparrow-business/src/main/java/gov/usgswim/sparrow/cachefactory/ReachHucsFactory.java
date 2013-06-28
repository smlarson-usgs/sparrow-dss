package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadReachHucs;
import gov.usgswim.sparrow.request.ModelHucsRequest;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class ReachHucsFactory extends AbstractCacheFactory {

	@Override
	public DataTable createEntry(Object reachHucsRequest) throws Exception {
		ModelHucsRequest req = (ModelHucsRequest) reachHucsRequest;

		LoadReachHucs action = new LoadReachHucs();
		action.setRequest(req);
		
		DataTable result = action.run();
		return result;
	}
}
