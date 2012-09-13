package gov.usgswim.sparrow.service.predict.aggregator;

import gov.usgs.cida.datatable.AggregateType;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcPrediction;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.BaseDataSeriesType;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

/**
 * Utility class used to run aggregations against {@code DataTable} objects.
 */
public class AggregationRunner {

	/** Logging object for this class. */
	protected static Logger log = Logger.getLogger(AggregationRunner.class);

	/** The prediction context used by this aggregator. */
	protected PredictionContext context;

	/**
	 * An enumeration of the possible aggregation functions.
	 * TODO: not implemented/used
	 */
	public static enum AggregateFunction {
		AVERAGE,
		MAXIMUM,
		MEDIAN,
		MINIMUM,
		SUM
	};

	/**
	 * Constructs a new {@code AggregationRunner}.
	 *
	 * @param context The prediction context used by this aggregator to
	 *                determine what to group by and how to calculate results.
	 */
	public AggregationRunner(PredictionContext context) {
		this.context = context;
	}

	/**
	 * Runs an aggregation against the specified predict result data, using the
	 * group and function specified in the prediction context.
	 *
	 * @param result The data to be aggregated.
	 * @return A {@code PredictResult} with its data aggregated by the group and
	 *         function specified in the prediction context.
	 */
	public PredictResult doAggregation(PredictResult result) throws Exception {
		log.debug("Entering AggregationRunner.doAggregation(PredictResult).");

		// Set up the query, result set, and connection
		String query = buildAggregateInfoQuery();
		ResultSet rs = null;
		Connection conn = SharedApplication.getInstance().getROConnection();

		try {
			// Run the query
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(query);

			// Aggregate the data based on the query results
			HashMap<String, AggregateData> aggregateDataMap = aggregate(result, rs, true);

			// Move the data from hashmap to two-dimensional array
			Iterator<String> iter = aggregateDataMap.keySet().iterator();
			long[] ids = new long[aggregateDataMap.keySet().size()];
			double[][] data = new double[aggregateDataMap.size()][result.getColumnCount()];
			for (int i = 0; i < data.length; i++) {
				String id = iter.next();

				// Push the data into the array and add the corresponding id
				double[] dataRow = aggregateDataMap.get(id).getData();
				data[i] = dataRow;
				ids[i] = Long.parseLong(id);
			}


			// Add a table-level filter if we're dealing with a weighted result
			DataSeriesType dataSeries = context.getAnalysis().getDataSeries();
			Map<String, String> properties = new HashMap<String, String>();
			if (dataSeries == DataSeriesType.incremental_yield) {
				properties.put("filterColumnType", BaseDataSeriesType.total.name());
			} else if (dataSeries == DataSeriesType.total_concentration) {
				properties.put("filterColumnType", BaseDataSeriesType.incremental.name());
			}

			PredictData predictData = SharedApplication.getInstance().getPredictData(context.getModelID());
			PredictResult aggResult = CalcPrediction.buildPredictResult(data, predictData, ids, properties);
			return aggResult;
		} finally {
			SharedApplication.closeConnection(conn, rs);
		}
	}

	/**
	 * Runs an aggregation against the specified data table, using the group and
	 * function specified in the prediction context.
	 *
	 * @param dataTable The data to be aggregated.
	 * @return A {@code DataTable} with its data aggregated by the group and
	 *         function specified in the prediction context.
	 * TODO [ik] consolidate doAggregation(DataTable) with doAggregation(PredictResult)
	 */
	public DataTable doAggregation(DataTable dataTable) throws Exception {
		log.debug("Entering AggregationRunner.doAggregation(DataTable).");

		// Set up the query, result set, and connection
		String query = buildAggregateInfoQuery();
		ResultSet rs = null;
		Connection conn = SharedApplication.getInstance().getROConnection();

		try {
			// Run the query
			Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
			rs = st.executeQuery(query);

			// Aggregate the data based on the query results
			HashMap<String, AggregateData> aggregateDataMap = aggregate(dataTable, rs, false);

			// Move the data from hashmap to two-dimensional array
			Iterator<String> iter = aggregateDataMap.keySet().iterator();
			long[] ids = new long[aggregateDataMap.keySet().size()];
			double[][] data = new double[aggregateDataMap.size()][dataTable.getColumnCount()];
			for (int i = 0; i < data.length; i++) {
				String id = iter.next();

				// Push the data into the array and add the corresponding id
				double[] dataRow = aggregateDataMap.get(id).getData();
				data[i] = dataRow;
				ids[i] = Long.parseLong(id);
			}

			return buildDataTable(data, ids, dataTable);
		} finally {
			SharedApplication.closeConnection(conn, rs);
		}
	}

	/**
	 * Constructs a {@code DataTable} using the specified data and ids.  The
	 * returned {@code DataTable}'s metadata is pulled from the given data table
	 * argument.
	 *
	 * @param data The raw data from which to construct the data table.
	 * @param ids The list of ids corresponding to the given data.
	 * @param dataTable The base data table used to extract metadata.
	 * @return A {@code DataTable} constructed from the given data and ids.
	 */
	public DataTable buildDataTable(double[][] data, long[] ids, DataTable dataTable) {
		DataTableWritable aggTable = new SimpleDataTableWritable();

		// Transfer metadata from the original DataTable
		for (String propertyName : dataTable.getPropertyNames()) {
			aggTable.setProperty(propertyName, dataTable.getProperty(propertyName));
		}
		// Add aggregation-specific table property
		// Here's a sweet kludge - temporary
		aggTable.setProperty("aggLevelKludge", context.getAnalysis().getGroupBy());

		// Add columns to the table
		for (int j = 0; j < data[0].length; j++) {
			// Create writable column
			StandardNumberColumnDataWritable<Double> column = new StandardNumberColumnDataWritable<Double>();

			// Populate column metadata
			column.setName(dataTable.getName(j));
			column.setUnits(dataTable.getUnits(j));
			for (String propertyName : dataTable.getPropertyNames(j)) {
				column.setProperty(propertyName, dataTable.getProperty(j, propertyName));
			}

			aggTable.addColumn(column);
		}

		// Iterate over the data, filling the table
		for (int i = 0; i < data.length; i++) {
			aggTable.setRowId(ids[i], i);

			for (int j = 0; j < data[i].length; j++) {
				aggTable.setValue(data[i][j], i, j);
			}
		}

		return aggTable.toImmutable();
	}

	/**
	 * Constructs a SQL query used to retrieve information necessary to
	 * aggregate the reach data.  When run, the query retrieves each reach's
	 * catchment area and HUC id.
	 *
	 * @return A SQL query used to retrieve information necessary to aggregate
	 *         reach data.
	 */
	private String buildAggregateInfoQuery() {
		// Get the model id and grouping level
		String modelId = Long.toString(context.getModelID());
		String groupBy = context.getAnalysis().getGroupBy();

		// Build query to retrieve catchment area, flow rate, and huc id for every reach
		String query = ""
			+ "SELECT A.identifier, A.catch_area as yield_weight, A.meanq as concentration_weight, A." + groupBy
			+ ", B." + groupBy + "_id AS hucId "
			+ "FROM model_attrib_vw A, stream_network." + groupBy + "_lkp B "
			+ "WHERE A.sparrow_model_id = " + modelId
			+ " AND A." + groupBy + " = B." + groupBy + "(+) "
			+ "ORDER BY A.hydseq, A.identifier";

		return query;
	}

	/**
	 * Performs aggregation of the given {@code dataTable} using information
	 * found in {@code rs}.  The {@code dataTable} and {@code rs} arguments
	 * should have a one-to-one mapping and be sorted identically.  This method
	 * returns a map of the aggregated results where each key/value pair
	 * represents a mapping from the group/huc id to the calculated data.
	 *
	 * @param dataTable The base data in need of aggregation.
	 * @param rs The SQL result set containing the grouping information for
	 *           aggregation.
	 * @return A map of the results where key = group/huc id and value = the
	 *         calculated data row.
	 */
	private HashMap<String, AggregateData> aggregate(DataTable dataTable, ResultSet rs, boolean weight)
	throws Exception {
		// Map to hold aggregate data, and running variables for the function
		HashMap<String, AggregateData> aggregateDataMap = new HashMap<String, AggregateData>();
		int colCount = dataTable.getColumnCount();

		AggregateType aggFunction = AggregateType.parse(context.getAnalysis().getAggFunction());
		assert(aggFunction != AggregateType.none);

		DataSeriesType dataSeries = context.getAnalysis().getDataSeries();
		String weightColumn = null;
		{	// Determine the correct weight to pull
			if (weight && context.getAnalysis().isWeighted()) {
				switch (dataSeries) {
					case incremental_yield: // intentional fall-through
					case incremental_delivered_yield:
						weightColumn = "yield_weight";
						break;
					// case DataSeriesType.total_delivered_concentration: this case doesn't make sense
					case total_concentration:
						weightColumn = "concentration_weight";
						break;
				}
			}
		}



		// Iterate over the query results -- all reaches
		for (int i = 0; rs.next(); i++) {

			// Get the HUC's row if it already exists
			String id = rs.getString("hucId");
			if (id == null || "".equals(id.trim())) {
				continue; // skip processing if no hucID
			}

			// Weight the value if necessary
			double weightValue = (weightColumn == null)? 1.0D: rs.getDouble(weightColumn);
			if (weightValue <= 0.0D) {
				continue; // skip processing for negative weights
			}

			AggregateData aggData = aggregateDataMap.get(id);

			// Create a new entry otherwise
			if (aggData == null) {
				// TODO [IK] using the full column count is not quite correct here, but we'll leave that for later.
				// These weights will work for only half of the PredictResult, either the incrementals or the [upstream] totals
				double[] data = new double[colCount];
				for (int k = 0; k < data.length; k++) {
					data[k] = 0.0D;
				}

				aggData = new AggregateData(data, 0);
			}

			double[] data = aggData.getData();
			int count = aggData.getCount();

			// Iterate over the reach's predicted values (columns)
			for (int j = 0; j < colCount; j++) {
				double curVal = dataTable.getDouble(i, j) / weightValue;

				// TODO: move calculations to AggregateData class
				// aggData.addValue();
				// aggData.addWeightedValue(value, weight);
				switch (aggFunction) {
					case avg:
						// TODO: change to sum + count and average on demand instead
						data[j] = ((data[j] * count) + curVal) / (count + 1);
						break;
					case max:
						data[j] = Math.max(data[j], curVal);
						break;
					case min:
						data[j] = Math.min(data[j], curVal);
						break;
					case sum:
						data[j] += curVal;
						break;
				}

			}

			aggData.setData(data);
			aggData.setCount(++count);
			aggregateDataMap.put(id, aggData);
		}

		return aggregateDataMap;
	}
}
