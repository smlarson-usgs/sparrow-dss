package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadReachHucs;
import gov.usgswim.sparrow.action.LoadReachHucsRequest;

/**
 * An EHCache CacheEntryFactory to load reach-HUC associations.
 * 
 * This class is a thin wrapper over the action LoadReachHucs and is only
 * needed to provide compatibility w/ the EHCache framework.  See the action
 * class for implementation details.
 *
 * Caching, blocking, and de-caching are all handled by the caching system.
 *
 * @author eeverman
 */
public class ReachHucsFactory extends AbstractCacheFactory {

	@Override
	public DataTable createEntry(Object reachHucsRequest) throws Exception {
		LoadReachHucsRequest req = (LoadReachHucsRequest) reachHucsRequest;

		LoadReachHucs action = new LoadReachHucs();
		action.setRequest(req);
		
		DataTable result = action.run();
		return result;
	}
}
