package gov.usgswim.sparrow.postgres.action;

import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.domain.PredictionContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 * Rather than write a dbf file out, this writes a row out to the postgres
 * model_output table.
 *
 * @author smlarson
 */
public class CreateViewForLayer extends Action<List> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CreateViewForLayer.class);

    private int model_nbr;
    private int model_output_id;
    private HashMap modelOutputValueMap;

    /**
     *
     * @param context
     * @param map consists of the dbf_identifier as the key and a double as a
     * value
     */
    public CreateViewForLayer(PredictionContext context, HashMap map) {
        init(context, map);
    }

    private void init(PredictionContext context, HashMap map) //int identifier, double dbfValue)
    {
        this.model_nbr = context.getModelID().intValue();  //two digits typically
        this.modelOutputValueMap = map; //map key is an integer 81017, map value is a double 39120.7
        this.model_output_id = context.getId();  // 776208324  -no prefix, can be negative. Was the dbf ID.

    }

    //#TODO# add validate method - makes sure the map is not empty
    /**
     * Take the former dbf output and insert it into a Postgres table, then
     * create the view by joining to the river network shape file. Later the
     * view is exposed as a layer in Geoserver.
     *
     * @return boolean true if Inserted 1 row successfully
     * @throws java.lang.Exception
     */
    @Override
    public List doAction() throws Exception {

        insertModelOutputRow(this.modelOutputValueMap);
        List result = createViews(); // always create two views: one for catchment, the other for flows (aka reaches).

        return result; //somthing from the select like the modelregion

    }

    /**
     *
     * @return Timestamp a current UTC sql timestamp
     */
    public static Timestamp getUTCNowAsSQLTimestamp() {

        Instant now = Instant.now();
        Timestamp currentTimestamp = Timestamp.from(now);
        return currentTimestamp;
    }

    /**
     * As the dbf writer would write to a file, this inserts rows into a table.
     * Parms: $MODEL_NBR$, $IDENTIFIER$, $VALUE$, $MODEL_OUTPUT_ID$,
     * $LAST_UPDATE$
     *
     * @param map
     * @throws java.lang.Exception
     */
    public void insertModelOutputRow(HashMap map) throws Exception {
        Timestamp now = getUTCNowAsSQLTimestamp(); //2016-04-20 08:26:26.345

        if (!exists()) { // if the model_output_id is not found on the model_output table, perform the insert
            //get the values out of the map, each set requires an insert statement
            Map<String, Object> paramMap = new HashMap<>();// this map is for sql parms
            paramMap.put("MODEL_NBR", this.model_nbr);
            paramMap.put("MODEL_OUTPUT_ID", this.model_output_id);
            paramMap.put("LAST_UPDATE", now); // check to see if this is formatted #TODO# ISO_8601 2007-04-05T12:30

            Set set = map.keySet();
            Iterator it = set.iterator();

            while (it.hasNext()) {

                int key = (int) it.next();
                double value = (double) map.get(key);

                paramMap.put("IDENTIFIER", key);  //iterate thru the map to get the id value
                paramMap.put("VALUE", value);

                PreparedStatement insertSqlps = getPostgresPSFromPropertiesFile("InsertModelOutputRow", null, paramMap);
                //LOGGER.info("Postgres insert sql: " + insertSqlps.toString());
                insertSqlps.executeUpdate();
            }
        } else {
            LOGGER.info("Insert for model_output_id: " + this.model_output_id + " will not be performed. Already exists on model_output table.");
        }
    }

    // if the quantity returned is greater than zero, return true 
    private boolean exists() throws Exception {
        //checks to see if the model_ouput has already been inserted for the first record
        //assumption is that the dbf id would be different if any of the data in the rows was different
        //and therefore doesnt require an upsert
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("MODEL_OUTPUT_ID", this.model_output_id);

        PreparedStatement sql = getPostgresPSFromPropertiesFile("SelectExists", null, paramMap);
        LOGGER.info("Check existence with: " + sql.toString());
        ResultSet rset = null;
        int quantity = 0;

        try {
            rset = sql.executeQuery();
            addResultSetForAutoClose(rset);

            while (rset.next()) {
                quantity = rset.getInt(1);
            }

        } finally {
            // rset can be null if there is an sql error. 
            if (rset != null) {
                rset.close();
            }
        }
        LOGGER.info("Quantity of model_id rows for this model output id before insert: " + quantity);

        return (quantity > 0);
    }

    public List createViews() throws Exception {
        List<String> viewNames = new ArrayList();
        List tables = getTableNames(this.model_nbr);

        viewNames.add(createView(getCatchViewParams(tables.get(0).toString()))); //catchment
        viewNames.add(createView(getFlowViewParams(tables.get(1).toString()))); //flow or reach

        return viewNames;
    }

    // Parms : VIEW_LAYER_NAME, GEOMTYPE, RIVER_NETWORK_TABLE_NAME, DBF_ID
    // Build filtering parameters and retrieve the queries from properties
    private Map getCatchViewParams(String tableName) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String catchGeom = "net.geom ::geometry(MultiPolygon, 4326)";

        paramMap.put("VIEW_LAYER_NAME", "\"catch_" + this.model_output_id + "\"");
        paramMap.put("GEOMTYPE", catchGeom);
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName);
        paramMap.put("DBF_ID", this.model_output_id); //this.model_output_id);

        return paramMap;
    }

    private Map getFlowViewParams(String tableName) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String flowGeom = "net.geom ::geometry(MultiLineString, 4326)";

        paramMap.put("VIEW_LAYER_NAME", "\"flow_" + this.model_output_id + "\"");
        paramMap.put("GEOMTYPE", flowGeom);
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName); //tables.get(1));
        paramMap.put("DBF_ID", this.model_output_id);

        return paramMap;
    }

    private String createView(Map paramMap) throws Exception {
        // Note: can not use a prepared statement for DDL queries
        //String sql = getPostgresSqlFromPropertiesFile("CreateView", null, paramMap);
        //LOGGER.info("Postgres view created from: " + sql);
        Statement statement = getPostgresStatement();
        statement.executeUpdate(getPostgresSqlFromPropertiesFile("CreateView", null, paramMap));
        return (String) paramMap.get("VIEW_LAYER_NAME");
    }

    /**
     *
     * @param modelNbr $MODEL_NBR$ a two digit number
     * @return a list of table names, one for catch at index 0, the other for
     * flow
     */
    public List getTableNames(int modelNbr) throws Exception {
        List result = new ArrayList();

        Map<String, Object> paramMap = new HashMap<String, Object>();
        paramMap.put("MODEL_NBR", modelNbr);
        PreparedStatement tableName = getPostgresPSFromPropertiesFile("GetTableNames", null, paramMap);
        LOGGER.info("GetTableName w/prepared statement: " + tableName.toString());
        ResultSet rset = null;

        try {
            rset = tableName.executeQuery();
            addResultSetForAutoClose(rset);

            while (rset.next()) {
                result.add(0, rset.getString("catch_table_name"));
                result.add(1, rset.getString("flow_table_name"));
            }

        } finally {
            // rset can be null if there is an sql error. 
            if (rset != null) {
                rset.close();
            }
        }
        LOGGER.info("Using catch table name: " + result.get(0) + " for model:" + modelNbr);
        LOGGER.info(" and flow table name: " + result.get(1));

        return result;
    }

    @Override
    public Long getModelId() {
        return (new Long(model_nbr));
    }
}
