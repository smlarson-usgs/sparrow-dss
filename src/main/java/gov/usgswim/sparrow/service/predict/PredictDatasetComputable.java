package gov.usgswim.sparrow.service.predict;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.LoadModelPredictData;
import gov.usgswim.task.Computable;

import org.apache.log4j.Logger;

/**
 * Task to load a PredictionDataSet from the db.
 * 
 * By implementing Computable, this task can be put in a ComputableCache, which
 * executes the task if the result does not already exist.
 * 
 */
public class PredictDatasetComputable implements Computable<Long, PredictData> {
	protected static Logger log =
		Logger.getLogger(PredictDatasetComputable.class); //logging for this class

	public PredictDatasetComputable() {
	}

	public PredictData compute(Long modelId) throws Exception {
		PredictData data = null;
		LoadModelPredictData loader = new LoadModelPredictData(modelId, true);

		long startTime = System.currentTimeMillis();
		log.debug("Begin loading predict data for model #" + modelId);

		data = loader.run();

		log.debug("End loading predict data for model #" + modelId + "  Time: " + (System.currentTimeMillis() - startTime) + "ms");

		return data.toImmutable();

	}
}

