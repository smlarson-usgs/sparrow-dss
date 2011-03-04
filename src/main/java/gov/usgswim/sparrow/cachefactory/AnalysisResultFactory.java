package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.CalcAnalysis;
import gov.usgswim.sparrow.datatable.DataColumn;
import gov.usgswim.sparrow.domain.PredictionContext;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

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
	@Override
	public DataColumn createEntry(Object predictContext) throws Exception {
		PredictionContext context = (PredictionContext) predictContext;

		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);

		DataColumn result = action.run();
		return result;
	}

}
