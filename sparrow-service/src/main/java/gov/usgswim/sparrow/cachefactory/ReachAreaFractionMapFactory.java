package gov.usgswim.sparrow.cachefactory;


import gov.usgswim.sparrow.action.CalcReachAreaFractionMap;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
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
	public ReachRowValueMap createEntry(Object reachId) throws Exception {
		
		CalcReachAreaFractionMap action = new CalcReachAreaFractionMap((ReachID) reachId);
		ReachRowValueMap result = action.run();
		
		return result;
	}
	


}
