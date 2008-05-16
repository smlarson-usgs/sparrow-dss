package gov.usgswim.sparrow.cachefactory;

import org.apache.log4j.Logger;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.sparrow.PredictComputable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.task.ComputableCache;

/**
 * This factory class creates a PredictResult on demand for an EHCache.
 * 
 * When the cache receives a get(PredictRequest) call and it doesn't have a cache
 * entry for that fequest, the createEntry() method of this class is called
 * and the returned value is cached.
 * 
 * The basic process followed in this class is:
 * <li>load the PredictData for the specified model, pulling it from the cache
 * <li>adjust the PredictData as spec'ed in the PredictRequest
 * <li>run the prediction using the adjusted data
 * 
 * @author eeverman
 *
 */
public class PredictResultFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(PredictComputable.class); //logging for this class
	
	public Object createEntry(Object predictRequest) throws Exception {
		PredictRequest request = (PredictRequest)predictRequest;
		
		//return DataLoader.loadMinimalPredictDataSet(SharedApplication.getInstance().getConnection(), id.intValue()).toImmutable();
		
		PredictData data = SharedApplication.getInstance().getPredictData(request.getModelId());
		PredictData adjData = adjustData(request, data);

		long startTime = System.currentTimeMillis();			

		PredictResult result = runPrediction(request, adjData);

		log.debug(
				"Prediction done for model #" + request.getModelId() + 
				" Time: " + (System.currentTimeMillis() - startTime) + "ms, " +
				adjData.getSrc().getRowCount() + " reaches");

		return result.toImmutable();
	}

	/**
	 * Adjusts the passed data based on the adjustments in the requests.
	 *
	 * It is assumed that the passed data is immutable, in which case a mutable
	 * builder is created, adjusted, copied as immutable, and returned.
	 * 
	 * If the passed data is mutable (an instance of PredictionDataBuilder,
	 * possibly for testing purposes), it will be adjusted by setting a new source,
	 * copied as immutable, and returned.
	 * 
	 * If there are no adjustments, the passed dataset is returned.
	 *
	 * @param req
	 * @param data
	 * @return
	 */
	public PredictData adjustData(PredictRequest req, PredictData data) throws Exception {

		if (req.getAdjustmentSet().hasAdjustments()) {
			PredictDataBuilder mutable = data.getBuilder();


			//This method does not modify the underlying data
			mutable.setSrc(
					req.getAdjustmentSet().adjust(mutable.getSrc(), mutable.getSrcIds(), mutable.getSys())
			);

			return mutable.toImmutable();
		}

		return data;
	}

	/**
	 * Runs the actual prediction using the passed base data.
	 *
	 * The passed data is not modified.
	 *
	 * @param arg
	 * @param data
	 * @return
	 */
	public PredictResult runPrediction(PredictRequest req, PredictData data) {
		PredictRunner adjPredict = new PredictRunner(data);
		PredictResult result = adjPredict.doPredict();
		return result;
	}

}
