package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.parser.AdvancedComparison;
import gov.usgswim.sparrow.parser.Comparison;
import gov.usgswim.sparrow.parser.NominalComparison;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;

/**
 * This factory class creates a Comparison on demand for an EHCache.
 *
 * When the cache receives a getComparisonResult(PredictContext) call and it doesn't have a cache
 * entry for that request, the createEntry() method of this class is called
 * and the returned value is cached.
 *
 * The basic process followed in this class is:
 * <ul>
 * <li>Fetch both results to be compared from the analysis cache
 * <li>Create a view that compares the two results.
 * </ul>
 *
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 *
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 *
 * @author eeverman
 *
 */
public class ComparisonResultFactory implements CacheEntryFactory {

	protected static Logger log =
		Logger.getLogger(ComparisonResultFactory.class); //logging for this class

	public Object createEntry(Object predictContext) throws Exception {
		PredictionContext fullContext = (PredictionContext) predictContext;
		Comparison comparison = fullContext.getComparison();

		PredictionContext noCompContext = fullContext.getNoComparisonVersion();
		PredictionContext baseContext = null;	//TBD based on type of comparison

		if (comparison instanceof NominalComparison) {
			baseContext = noCompContext.getNoAdjustmentVersion();
		} else if (comparison instanceof AdvancedComparison) {
			AdvancedComparison ac = (AdvancedComparison) comparison;
			baseContext = ac.getBasePredictionContext();

			if (baseContext == null) {
				Integer id = ac.getPredictionContextId();
				SharedApplication.getInstance().getPredictionContext(id);

				if (baseContext == null) {
					throw new Exception("Unable to locate context id '"
							+ id + "' for comparison.");
				}
			}

		} else {
			throw new Exception("Unrecognized Comparison Subclass '"
					+ comparison.getClass().getName() + "'");
		}

		//Get analysis results from analysis cache
		PredictionContext.DataColumn baseCol = SharedApplication.getInstance().getAnalysisResult(baseContext);
		PredictionContext.DataColumn compCol = SharedApplication.getInstance().getAnalysisResult(noCompContext);
		DataTable baseResult = baseCol.getTable();
		DataTable compResult = compCol.getTable();

		DataTable resultTable = null;
		switch (comparison.getComparisonType()) {
			case none: {
				//degenerative case - this should never be called
				resultTable = compResult;
				break;
			}
			case percent: {
				resultTable = new DataTableCompare(baseResult, compResult, false);
				break;
			}
			case absolute: {
				resultTable = new DataTableCompare(baseResult, compResult, true);
				break;
			}
			default: {
				throw new Exception("Should never be in here...");
			}


		}

		return new PredictionContext.DataColumn(resultTable, compCol.getColumn());
	}
}
