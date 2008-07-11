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
		
		return getEqualCountBins(request, adjResult);
	}
	
	/**
	 * Returns an equal count set of bins so that the bins define break-point boundaries
	 * with approximately an equal number of values in each bin.
	 * 
	 * @param request
	 * @param data
	 * @return
	 */
	protected float[] getEqualCountBins(BinningRequest request, PredictResult data) {

		int totalRows = data.getRowCount();	//Total rows of data
		
		//Number of rows 'contained' in each bin.  This likely will not come out even,
		//so use a double to preserve the fractional rows.
		double binSize = (double)(totalRows) / (double)(request.getBinCount());	
		
		float[] values = new float[totalRows];	//Array holding all values
		//The bins, where each value is a fence post w/ values between, thus, there is one more 'post' than bins.
		//The first value is the lowest value in values[], the last value is the largest value.
		float[] bins = new float[request.getBinCount() + 1];	
		
		//Export all values in the specified column to values[] so they can be sorted
		for (int r=0; r<totalRows; r++) {
			values[r] = data.getFloat(r, request.getColumnIndex());
		}
		
		Arrays.sort(values);
		
		//Assign first and last values for the bins (min and max)
		bins[0] = values[0];
		bins[request.getBinCount()] = values[totalRows - 1];
		
		//Assign the middle breaks so that equal numbers of values fall into each bin
		for (int i=1; i<(request.getBinCount()); i++) {
			
			//Get the row containing the nearest integer split
			int split = (int) ((double)i * binSize);
			
			//The bin boundary is the value contained at that row.
			bins[i] = values[split];
		}
		
		return bins;
	}
}
