package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultCompare;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;

/**
 * This factory class creates a PredictResult on demand for an EHCache.
 * 
 * When the cache receives a getAnalysisResult(PredictContext) call and it doesn't have a cache
 * entry for that request, the createEntry() method of this class is called
 * and the returned value is cached.
 * 
 * The basic process followed in this class is:
 * <ul>
 * <li>Run the prediction and if needed the nominal prediction by calling getPredictResult()
 * <li>Do the analysis based on the Analysis section of the PredictionContext.
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
public class AnalysisResultFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(AnalysisResultFactory.class); //logging for this class
	
	public Object createEntry(Object predictContext) throws Exception {
		PredictionContext context = (PredictionContext)predictContext;
		Analysis analysis = context.getAnalysis();
		PredictResult result = null;
		
		PredictResult adjResult = SharedApplication.getInstance().getPredictResult(context);

		switch (analysis.getSelect().getNominalComparison()) {
			case none: {
				result = adjResult;
				break;
			} case percent: {
				
				PredictionContext nomContext = new PredictionContext(context.getModelID(), null, null, null, null);
				PredictResult nomResult = SharedApplication.getInstance().getPredictResult(nomContext);
				
				result = new PredictResultCompare(nomResult, adjResult, false);
				
				break;
			} case absolute: {
				
				PredictionContext nomContext = new PredictionContext(context.getModelID(), null, null, null, null);
				PredictResult nomResult = SharedApplication.getInstance().getPredictResult(nomContext);
				
				result = new PredictResultCompare(nomResult, adjResult, true);
				
				break;
			} default: {
				throw new Exception("Should never be in here...");
			}
			
		
		}
		
		return result;
	}
}
