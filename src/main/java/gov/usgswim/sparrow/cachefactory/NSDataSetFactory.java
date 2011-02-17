package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.DeliveryReach;
import gov.usgswim.sparrow.action.NSDataSetBuilder;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.HashMap;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import oracle.mapviewer.share.ext.NSDataSet;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class NSDataSetFactory implements CacheEntryFactory {

	@Override
	public NSDataSet createEntry(Object predictContext) throws Exception {
		PredictionContext context = (PredictionContext) predictContext;

		NSDataSetBuilder action = new NSDataSetBuilder();
		
		
		//Optionally add the deliveryHash if this is a delivery type
		//series.  This will cause reaches not upstream of a target to be
		//given a special value.
		if (	
				context.getAnalysis().getDataSeries().isDeliveryBased() ||
				context.getAnalysis().getDataSeries().isDeliveryRequired()) {
			
			HashMap<Integer, DeliveryReach> upstreamReaches =
				SharedApplication.getInstance().getDeliveryFractionHash(
					context.getTerminalReaches());
			
			action.setInclusionHash(upstreamReaches);
		}
		
		DataColumn data = context.getDataColumn();
		action.setData(data);
		NSDataSet result = action.run();
		return result;
	}

}
