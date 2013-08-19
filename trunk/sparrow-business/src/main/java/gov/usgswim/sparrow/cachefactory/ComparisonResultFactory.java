package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.CalcComparison;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.PredictionContext;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class ComparisonResultFactory implements CacheEntryFactory {

	@Override
	public SparrowColumnSpecifier createEntry(Object predictContext) throws Exception {
		PredictionContext fullContext = (PredictionContext) predictContext;
		CalcComparison action = new CalcComparison();
		action.setContext(fullContext);
		return action.run();
	}
}
