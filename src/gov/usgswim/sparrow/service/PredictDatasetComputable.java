package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.Computable;
import gov.usgswim.sparrow.PredictionDataSet;
import gov.usgswim.sparrow.util.JDBCUtil;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

public class PredictDatasetComputable implements Computable<Long, PredictionDataSet> {
	protected static Logger log =
		Logger.getLogger(PredictDatasetComputable.class); //logging for this class
		
	public PredictDatasetComputable() {
	}

	public PredictionDataSet compute(Long modelId) throws Exception {
		Connection conn = null;
		PredictionDataSet data = null;
		try {
			conn = SharedApplication.getInstance().getConnection();
			
			long startTime = System.currentTimeMillis();
			log.debug("Begin loading predict data for model #" + modelId);
			
			data = JDBCUtil.loadMinimalPredictDataSet(conn, modelId.intValue());
			
			log.debug("End loading predict data for model #" + modelId + "  Time: " + (System.currentTimeMillis() - startTime) + "ms");
			
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
					//ignore
				}
			}
		}
		
		return data;
		
	}
}
