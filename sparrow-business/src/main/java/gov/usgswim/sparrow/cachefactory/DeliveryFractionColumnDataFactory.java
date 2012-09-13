package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcDeliveryFractionColumnData;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
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
		
		ReachRowValueMap deliveryFractionHash =
			SharedApplication.getInstance().getDeliveryFractionMap(targets);
		
		CalcDeliveryFractionColumnData action = new CalcDeliveryFractionColumnData();
		action.setPredictData(predictData);
		action.setDeliveryFractionHash(deliveryFractionHash);
		
		ColumnData accumulatedDeliveryFrac = action.run();
		
		return accumulatedDeliveryFrac;
	}
	


}
