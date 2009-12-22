package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcDeliveryFraction;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Set;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * This factory class creates a ColumnData containing the delivery
 * fractions to the set of Target reaches on demand for an EHCache.
 *
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 *
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 *
 * @author eeverman
 *
 */
public class CalcDeliveryFractionFactory implements CacheEntryFactory {

	public Object createEntry(Object terminalReaches) throws Exception {
		TerminalReaches targets = (TerminalReaches) terminalReaches;
		
		PredictData predictData = SharedApplication.getInstance().
			getPredictData(new Long(targets.getModelID()));
		Set<Long> targetReachIds = targets.asSet();
		
		ColumnData accumulatedDeliveryFrac =
			CalcDeliveryFraction.calcDelivery(predictData, targetReachIds);
		
		return accumulatedDeliveryFrac;
	}
	


}
