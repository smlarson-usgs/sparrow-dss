package gov.usgswim.sparrow.service.predict;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.task.Computable;

import java.sql.Connection;

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
		Connection conn = null;
		PredictData data = null;
		try {
			conn = SharedApplication.getInstance().getConnection();

			long startTime = System.currentTimeMillis();
			log.debug("Begin loading predict data for model #" + modelId);

			data = DataLoader.loadModelDataOnly(conn, modelId.intValue());

			log.debug("End loading predict data for model #" + modelId + "  Time: " + (System.currentTimeMillis() - startTime) + "ms");

		} finally {
			// not really necessary as it's taken care of in loadMinimalPredictDataSet
			SharedApplication.closeConnection(conn, null);
		}

		return data.toImmutable();

	}
}

