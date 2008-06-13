package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Arrays;

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
public class BinningFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(BinningFactory.class); //logging for this class
	
	public Object createEntry(Object binningRequest) throws Exception {
		BinningRequest request = (BinningRequest)binningRequest;
		PredictionContext context = SharedApplication.getInstance().getPredictionContext(request.getContextID());
		
		if (context == null) {
			throw new Exception("No context found for context-id '" + request.getContextID() + "'");
		}
		
		PredictResult adjResult = SharedApplication.getInstance().getAnalysisResult(context);

		adjResult.getRowCount();
		int totalRows = adjResult.getRowCount();
		double binSize = (double)(totalRows) / (double)(request.getBinCount());
		
		float[] values = new float[totalRows];
		float[] bins = new float[request.getBinCount() + 1];
		
		for (int r=0; r<totalRows; r++) {
			values[r] = adjResult.getFloat(r, request.getColumnIndex());
		}
		
		Arrays.sort(values);
		
		bins[0] = values[0];
		bins[request.getBinCount()] = values[totalRows - 1];
		
		for (int i=1; i<(request.getBinCount()); i++) {
			int split = (int) ((double)i * binSize);
			bins[i] = values[split];
		}
		
		return bins;
	}
}
