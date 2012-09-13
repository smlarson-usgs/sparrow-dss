package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.binning.domain.BinSet;
import gov.usgswim.sparrow.action.CalcBins;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 *
 * @author eeverman
 *
 */
public class BinningFactory implements CacheEntryFactory {


	@Override
	public BinSet createEntry(Object binningRequest) throws Exception {
		BinningRequest request = (BinningRequest)binningRequest;
		PredictionContext context = SharedApplication.getInstance().getPredictionContext(request.getContextID());
		ReachRowValueMap inclusionMap = null;
		
		if (context == null) {
			throw new Exception("No context found for context-id '" + request.getContextID() + "'");
		}
		
		if (context.getAnalysis().getDataSeries().isDeliveryRequired()) {
			inclusionMap =
				SharedApplication.getInstance().getDeliveryFractionMap(context.getTerminalReaches());
			
		}

		SparrowColumnSpecifier dc = context.getDataColumn();

		CalcBins action = new CalcBins();
		action.setDataColumn(dc);
		action.setInclusionMap(inclusionMap);
		action.setRequest(request);
		
		BinSet result = action.run();
		return result;
	}


}
