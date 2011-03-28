package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.CalcBinning;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.math.BigDecimal;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;


public class BinningFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(BinningFactory.class); //logging for this class


	@Override
	public BigDecimal[] createEntry(Object binningRequest) throws Exception {
		BinningRequest request = (BinningRequest)binningRequest;
		PredictionContext context = SharedApplication.getInstance().getPredictionContext(request.getContextID());

		if (context == null) {
			throw new Exception("No context found for context-id '" + request.getContextID() + "'");
		}

		SparrowColumnSpecifier dc = context.getDataColumn();

		CalcBinning action = new CalcBinning();
		action.setDataColumn(dc);
		action.setRequest(request);
		
		BigDecimal[] result = action.run();
		return result;
	}


}
