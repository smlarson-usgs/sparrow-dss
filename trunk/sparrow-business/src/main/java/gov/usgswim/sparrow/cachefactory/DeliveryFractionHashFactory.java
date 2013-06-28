package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcDeliveryFractionMap;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.HashSet;
import java.util.List;

import java.util.Set;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 *
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class DeliveryFractionHashFactory implements CacheEntryFactory {

	@Override
	public ReachRowValueMap createEntry(Object terminalReaches) throws Exception {
		TerminalReaches targets = (TerminalReaches) terminalReaches;

		PredictData predictData = SharedApplication.getInstance().
			getPredictData(new Long(targets.getModelID()));

		Set<String> clientTargetReachIdSet = targets.getReachIdsAsSet();

		List<Long> targetReachIdList = SharedApplication.getInstance().getReachFullIdAsLong(targets.getModelID(), clientTargetReachIdSet);
		HashSet<Long> targetReachIdSet = new HashSet<Long>();
		targetReachIdSet.addAll(targetReachIdList);

		CalcDeliveryFractionMap action = new CalcDeliveryFractionMap(
			predictData,
			targetReachIdSet
			);

		ReachRowValueMap delFrac = action.run();

		return delFrac;
	}


}
