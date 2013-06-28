package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadReachWatershedDetail;
import gov.usgswim.sparrow.domain.ReachGeometry;
import gov.usgswim.sparrow.request.ReachID;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class ReachWatershedFactory implements CacheEntryFactory {

	@Override
	public ReachGeometry createEntry(Object inReachID) throws Exception {
		ReachID req = (ReachID)inReachID;
		ReachGeometry rw = null;
		LoadReachWatershedDetail action = new LoadReachWatershedDetail(req);
		rw = action.run();
		return rw;
	}

}
