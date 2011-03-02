package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcDeliveryFractionMap;
import gov.usgswim.sparrow.domain.DeliveryFractionMap;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

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
	public DeliveryFractionMap createEntry(Object terminalReaches) throws Exception {
		TerminalReaches targets = (TerminalReaches) terminalReaches;
		
		PredictData predictData = SharedApplication.getInstance().
			getPredictData(new Long(targets.getModelID()));
		Set<Long> targetReachIds = targets.asSet();
		
		CalcDeliveryFractionMap action = new CalcDeliveryFractionMap();
		action.setPredictData(predictData);
		action.setTargetReachIds(targetReachIds);
		
		DeliveryFractionMap delFrac = action.run();
		
		return delFrac;
	}


}
