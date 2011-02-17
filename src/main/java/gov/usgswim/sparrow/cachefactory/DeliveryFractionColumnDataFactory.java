package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcDeliveryFractionColumnData;
import gov.usgswim.sparrow.action.DeliveryReach;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.HashMap;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class DeliveryFractionColumnDataFactory implements CacheEntryFactory {

	@Override
	public ColumnData createEntry(Object terminalReaches) throws Exception {
		TerminalReaches targets = (TerminalReaches) terminalReaches;
		
		PredictData predictData = SharedApplication.getInstance().
			getPredictData(new Long(targets.getModelID()));
		
		HashMap<Integer, DeliveryReach> deliveryFractionHash =
			SharedApplication.getInstance().getDeliveryFractionHash(targets);
		
		CalcDeliveryFractionColumnData action = new CalcDeliveryFractionColumnData();
		action.setPredictData(predictData);
		action.setDeliveryFractionHash(deliveryFractionHash);
		
		ColumnData accumulatedDeliveryFrac = action.run();
		
		return accumulatedDeliveryFrac;
	}
	


}
