package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcDeliveryFraction;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Set;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * An EHCache CacheEntryFactory to run a SPARROW calculation when the results
 * are not present in the cache.
 * 
 * This class is a thin wrapper over the action CalcDeliveryFraction and is only
 * needed to provide compatibility w/ the EHCache framework.  See the action
 * class for implementation details.
 *
 * Caching, blocking, and de-caching are all handled by the caching system.
 *
 * @author eeverman
 */
public class CalcDeliveryFractionFactory implements CacheEntryFactory {

	public Object createEntry(Object terminalReaches) throws Exception {
		TerminalReaches targets = (TerminalReaches) terminalReaches;
		
		PredictData predictData = SharedApplication.getInstance().
			getPredictData(new Long(targets.getModelID()));
		Set<Long> targetReachIds = targets.asSet();
		
		CalcDeliveryFraction action = new CalcDeliveryFraction();
		action.setPredictData(predictData);
		action.setTargetReachIds(targetReachIds);
		
		ColumnData accumulatedDeliveryFrac = action.run();
		
		return accumulatedDeliveryFrac;
	}
	


}
