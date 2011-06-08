package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.CalcBins;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.BinSet;
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

		if (context == null) {
			throw new Exception("No context found for context-id '" + request.getContextID() + "'");
		}

		SparrowColumnSpecifier dc = context.getDataColumn();

		CalcBins action = new CalcBins();
		action.setDataColumn(dc);
		action.setRequest(request);
		
		BinSet result = action.run();
		return result;
	}


}
