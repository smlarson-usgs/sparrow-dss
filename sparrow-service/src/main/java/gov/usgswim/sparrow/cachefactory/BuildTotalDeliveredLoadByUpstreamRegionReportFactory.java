package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.DataTableSet;
import gov.usgswim.sparrow.action.BuildTotalDeliveredLoadByUpstreamRegionReport;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class BuildTotalDeliveredLoadByUpstreamRegionReportFactory implements CacheEntryFactory {


	@Override
	public DataTableSet createEntry(Object deliveryReportRequest) throws Exception {
		DeliveryReportRequest req = (DeliveryReportRequest)deliveryReportRequest;
		DataTableSet result = null;
		
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		
		result = action.run();
		
		return result;
	}

}
