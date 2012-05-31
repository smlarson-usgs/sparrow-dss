package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.BuildTotalDeliveredLoadByStateSummaryReport;
import gov.usgswim.sparrow.action.BuildTotalDeliveredLoadSummaryReport;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class BuildTotalDeliveredLoadByStateSummaryReportFactory implements CacheEntryFactory {


	@Override
	public DataTable createEntry(Object deliveryReportRequest) throws Exception {
		DeliveryReportRequest req = (DeliveryReportRequest)deliveryReportRequest;
		DataTable result = null;
		
		BuildTotalDeliveredLoadByStateSummaryReport action =
				new BuildTotalDeliveredLoadByStateSummaryReport(req);
		
		result = action.run();
		
		return result;
	}

}
