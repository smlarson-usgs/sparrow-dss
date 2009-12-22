package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.DeliveryRunner;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.UncertaintySeries;
import gov.usgswim.sparrow.action.CalcAnalysis;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.StdErrorEstTable;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.parser.PredictionContext.DataColumn;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.WeightRunner;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import java.util.Set;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;

/**
 * An EHCache CacheEntryFactory to run a SPARROW calculation when the results
 * are not present in the cache.
 * 
 * This class is a thin wrapper over the action CalcAnalysis and is only needed
 * to provide compatibility w/ the EHCache framework.  See the action class
 * for implementation details.
 *
 * Caching, blocking, and de-caching are all handled by the caching system.
 *
 * @author eeverman
 */
public class AnalysisResultFactory implements CacheEntryFactory {

	/**
	 * Build a new entry.
	 * 
	 * @param predictContext the PredictionContext to build the results for.
	 * @return A PredictionContext.DataColumn containing the single column results.
	 */
	public Object createEntry(Object predictContext) throws Exception {
		PredictionContext context = (PredictionContext) predictContext;

		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);

		PredictionContext.DataColumn result = action.run();
		return result;
	}

}
