package gov.usgswim.sparrow;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
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
    public enum AggregateFunction {
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
        Connection conn = SharedApplication.getInstance().getConnection();

        try {
            // Run the query
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery(query);

            // Aggregate the data based on the query results
            HashMap<String, AggregateData> aggregateDataMap = aggregate(result, rs);

            // Move the data from hashmap to two-dimensional array
            Iterator iter = aggregateDataMap.keySet().iterator();
            long[] ids = new long[aggregateDataMap.keySet().size()];
            double[][] data = new double[aggregateDataMap.size()][result.getColumnCount()];
            for (int i = 0; i < data.length; i++) {
                String id = (String) iter.next();

                // Push the data into the array and add the corresponding id
                double[] dataRow = aggregateDataMap.get(id).getData();
                data[i] = dataRow;
                ids[i] = Long.parseLong(id);
            }

            PredictData predictData = SharedApplication.getInstance().getPredictData(context.getModelID());
            return PredictResultImm.buildPredictResult(data, predictData, ids);
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
     */
    public DataTable doAggregation(DataTable dataTable) throws Exception {
        log.debug("Entering AggregationRunner.doAggregation(DataTable).");
        
        // Set up the query, result set, and connection
        String query = buildAggregateInfoQuery();
        ResultSet rs = null;
        Connection conn = SharedApplication.getInstance().getConnection();

        try {
            // Run the query
            Statement st = conn.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY);
            rs = st.executeQuery(query);

            // Aggregate the data based on the query results
            HashMap<String, AggregateData> aggregateDataMap = aggregate(dataTable, rs);

            // Move the data from hashmap to two-dimensional array
            Iterator iter = aggregateDataMap.keySet().iterator();
            long[] ids = new long[aggregateDataMap.keySet().size()];
            double[][] data = new double[aggregateDataMap.size()][dataTable.getColumnCount()];
            for (int i = 0; i < data.length; i++) {
                String id = (String) iter.next();

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
        for (String propertyName : dataTable.getPropertyNames()) {
            aggTable.setProperty(propertyName, dataTable.getProperty(propertyName));
        }
        
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
        
        // Build query to retrieve catchment area and huc id for every reach
        String query = ""
            + "SELECT A.identifier, A.catch_area, A." + groupBy + ", B."
            + groupBy + "_id AS hucId "
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
    private HashMap aggregate(DataTable dataTable, ResultSet rs) throws Exception {
        // Map to hold aggregate data, and running variables for the function
        HashMap<String, AggregateData> aggregateDataMap = new HashMap<String, AggregateData>();
        int colCount = dataTable.getColumnCount();

        // Iterate over the query results
        String aggFunction = context.getAnalysis().getSelect().getAggFunction();
        for (int i = 0; rs.next(); i++) {

            // Get the HUC's row if it already exists
            String id = rs.getString("hucId");
            int catchArea = rs.getInt("catch_area");
            if (id == null || "".equals(id.trim()) || catchArea == 0) {
                continue;
            }
            AggregateData aggData = aggregateDataMap.get(id);

            // Create a new entry otherwise
            if (aggData == null) {
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
                double curVal = dataTable.getDouble(i, j);

                // TODO: change string matches in enum/switch
                if ("avg".equals(aggFunction)) {
                    curVal /= catchArea;
                    data[j] = ((data[j] * count) + curVal) / (count + 1);
                } else if ("max".equals(aggFunction)) {
                    data[j] = Math.max(data[j], curVal);
                } else if ("min".equals(aggFunction)) {
                    data[j] = Math.min(data[j], curVal);
                } else if ("sum".equals(aggFunction)) {
                    data[j] += curVal;
                }
            }

            aggData.setData(data);
            aggData.setCount(++count);
            aggregateDataMap.put(id, aggData);
        }
        
        return aggregateDataMap;
    }
    
    /**
     * A data structure used to contain information about a specific row of
     * aggregated data.
     */
    protected class AggregateData {
        private double[] data;
        private int count;
        
        public AggregateData(double[] data, int count) {
            this.data = data;
            this.count = count;
        }
        
        public double[] getData() {
            return data;
        }
        public void setData(double[] data) {
            this.data = data;
        }
        
        public int getCount() {
            return count;
        }
        public void setCount(int count) {
            this.count = count;
        }
    }
}
