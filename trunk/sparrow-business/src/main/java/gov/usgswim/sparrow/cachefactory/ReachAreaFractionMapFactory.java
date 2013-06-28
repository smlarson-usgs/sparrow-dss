package gov.usgswim.sparrow.cachefactory;


import gov.usgswim.sparrow.action.CalcReachAreaFractionMap;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.ReachAreaFractionMapRequest;
import gov.usgswim.sparrow.request.ReachID;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class ReachAreaFractionMapFactory implements CacheEntryFactory {

	@Override
	public ReachRowValueMap createEntry(Object request) throws Exception {
		
		if (request instanceof ReachID) {
			CalcReachAreaFractionMap action = new CalcReachAreaFractionMap((ReachID) request, false, false);
			ReachRowValueMap result = action.run();
			return result;
		} else if (request instanceof ReachAreaFractionMapRequest) {
			ReachAreaFractionMapRequest rafr = (ReachAreaFractionMapRequest) request;
			CalcReachAreaFractionMap action = new CalcReachAreaFractionMap(rafr.getReachId(), rafr.isForceUncorrectedFracValues(), rafr.isForceIgnoreIfTran());
			ReachRowValueMap result = action.run();
			return result;
		} else {
			throw new IllegalArgumentException("The request object must be a reachID or a ReachAreaFractionMapRequest");
		}
	}
	


}
