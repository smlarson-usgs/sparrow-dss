package gov.usgswim.sparrow.revised;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.ValueType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 *
 */
public class WeightRunner2 {

    /** Logging object for this class. */
    protected static Logger log = Logger.getLogger(WeightRunner2.class);

	/**
	 * @param context
	 * @param result
	 * @return
	 * @throws Exception
	 *             Note that result is only being used as a DataTable. There are
	 *             no assumptions about it being a PredictResult except in the
	 *             naming of the headers
	 */
    public static PredictResult doWeighting(PredictionContext2 context, PredictResult result)
    throws Exception {
        log.debug("Entering WeightRunner.doWeighting(PredictResult).");

        // Set up the query, result set, and connection
        String query = WeightRunner2.buildWeightingInfoQuery(context);
        ResultSet rs = null;
        Connection conn = SharedApplication.getInstance().getConnection();

        try {
            // Run the query
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery(query);

            DataSeriesType dataSeries = context.getAnalysis().getDataSeries();
            //TODO:  This should really come off from a flag on the analysis
            //which indicates weighting by area or flow.
            String weightColName = null;
            switch (dataSeries) {
            	case incremental_yield:
            	case incremental_delivered_yield:
            		weightColName = "yield_weight";
            		break;
            	// case total_delivered_concentration: not a valid option
            	case total_concentration:
            		weightColName = "concentration_weight";
            		break;
            }

            long[] ids = new long[result.getRowCount()];
            double[][] data = new double[result.getRowCount()][result.getColumnCount()];

            // Iterate over the reaches, weighting them appropriately
            for (int i = 0; rs.next(); i++) {
                for (int j = 0; j < result.getColumnCount(); j++) {
                    double value = result.getDouble(i, j);
                    double weight = rs.getDouble(weightColName);

                    double weightedValue = value / weight;
                    data[i][j] = weightedValue;
                }

                ids[i] = result.getIdForRow(i);
            }

            // Add a table-level filter since we're dealing with a weighted result
            Map<String, String> properties = new HashMap<String, String>();
            if (dataSeries == DataSeriesType.incremental_yield) {
                properties.put("filterColumnType", ValueType.total.name());
            } else if (dataSeries == DataSeriesType.total_concentration) {
                properties.put("filterColumnType", ValueType.incremental.name());
            }

            // Build a new PredictResult based on the weighted data
            PredictData predictData = SharedApplication.getInstance().getPredictData(context.getModelID());
            PredictResult weightedResult = PredictResultImm.buildPredictResult(data, predictData, ids, properties);
            return weightedResult;

        } finally {
            SharedApplication.closeConnection(conn, rs);
        }
    }

    private static String buildWeightingInfoQuery(PredictionContext2 context) {
        // Get the model id
        String modelId = Long.toString(context.getModelID());

        // Build query to retrieve catchment area and mean q (flow) for every reach
        String query = ""
            + "SELECT identifier, "
            + " catch_area AS yield_weight, "
            + " meanq AS concentration_weight "
            + "FROM model_attrib_vw "
            + "WHERE sparrow_model_id = " + modelId
            + " ORDER BY hydseq, identifier";

        return query;
    }
}
