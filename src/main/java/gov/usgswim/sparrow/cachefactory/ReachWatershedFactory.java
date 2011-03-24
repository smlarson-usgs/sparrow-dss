package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadReachWatershed;
import gov.usgswim.sparrow.domain.ReachWatershed;
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
	public ReachWatershed createEntry(Object inReachID) throws Exception {
		ReachID req = (ReachID)inReachID;
		ReachWatershed rw = null;
		LoadReachWatershed action = new LoadReachWatershed(req);
		rw = action.run();
		return rw;
	}

}
