package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcDeliveryFractionHash;
import gov.usgswim.sparrow.action.DeliveryReach;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.HashMap;
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
	public HashMap<Integer, DeliveryReach> createEntry(Object terminalReaches) throws Exception {
		TerminalReaches targets = (TerminalReaches) terminalReaches;
		
		PredictData predictData = SharedApplication.getInstance().
			getPredictData(new Long(targets.getModelID()));
		Set<Long> targetReachIds = targets.asSet();
		
		CalcDeliveryFractionHash action = new CalcDeliveryFractionHash();
		action.setPredictData(predictData);
		action.setTargetReachIds(targetReachIds);
		
		HashMap<Integer, DeliveryReach> fractionHash = action.run();
		
		return fractionHash;
	}


}
