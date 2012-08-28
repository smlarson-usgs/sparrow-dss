package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.BuildTotalDeliveredLoadSummaryReport;
import gov.usgswim.sparrow.action.CalcFractionedWatershedAreaTable;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class FractionedWatershedAreaTableFactory implements CacheEntryFactory {


	@Override
	public ColumnData createEntry(Object terminalReachId) throws Exception {
		//The ConfiguredCache may pass a Long instead of an Integer, so careful type casting
		Number numReq = (Number)terminalReachId;
		Integer req = numReq.intValue();
		
		CalcFractionedWatershedAreaTable action = new CalcFractionedWatershedAreaTable(req);
		
		ColumnData result = action.run();
		
		return result;
	}

}
