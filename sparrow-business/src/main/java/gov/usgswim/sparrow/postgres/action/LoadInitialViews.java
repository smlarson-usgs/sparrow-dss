package gov.usgswim.sparrow.postgres.action;

import gov.usgswim.sparrow.action.Action;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smlarson
 */
public class LoadInitialViews extends Action<List> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(LoadInitialViews.class);

    public List doAction() throws Exception {
        List list = new ArrayList();

        //init() NA
        //validate() NA
        Set uModelOutputIds = getUniqueModelIds();// get all the unique model_output_ids 
        HashSet retrieveAllViewIds = retrieveAllViewNames();// get all the views and parse the name down to the id
        HashSet viewsNeeded = determineModelOutputIdsWithoutViews(uModelOutputIds,retrieveAllViewIds);//all the model_output_ids that did not have view 
        createViews(viewsNeeded); 

        return list;
    }

    //takes the model output IDs and joins to the approp river region determined by join to lookup table
    //# Parms : VIEW_LAYER_NAME, GEOMTYPE, RIVER_NETWORK_TABLE_NAME, DBF_ID
    private void createViews(HashSet<Integer> model_output_ids) throws Exception
    {
       // loop thru the list of unique model output ids and create two views (catch and flow)
        for (Integer model_id : model_output_ids) {
            ArrayList<String> tables = GetRegionTableNames(model_id);
          //create two views: one catch one flow
          createView(getCatchViewParams(tables.get(0), model_id)); //catchment
          createView(getFlowViewParams(tables.get(1), model_id)); //flow or reach
        }
    }
    
    // Parms : VIEW_LAYER_NAME, GEOMTYPE, RIVER_NETWORK_TABLE_NAME, DBF_ID
    // Build filtering parameters and retrieve the queries from properties
    private Map getCatchViewParams(String tableName, Integer model_output_id) {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String catchGeom = "net.geom ::geometry(MultiPolygon, 4326)";

        paramMap.put("VIEW_LAYER_NAME", "\"catch_" + model_output_id + "\"");
        paramMap.put("GEOMTYPE", catchGeom);
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName);
        paramMap.put("DBF_ID", model_output_id);

        return paramMap;
    }

    private Map getFlowViewParams(String tableName, Integer model_output_id) throws Exception {
        Map<String, Object> paramMap = new HashMap<String, Object>();
        String flowGeom = "net.geom ::geometry(MultiLineString, 4326)"; 
        
        HashSet<String> multiZnetwork = new HashSet(); 
        multiZnetwork.add("mrb01_nhd_flow");
        multiZnetwork.add("chesa_nhd_flow");
        
        if (getMultiZnetworks().contains(tableName))  //it has 4 dim thus needs the ZM
        {
            flowGeom = "net.geom ::geometry(MultiLineStringZM, 4326)";
        }

        paramMap.put("VIEW_LAYER_NAME", "\"flow_" + model_output_id + "\"");
        paramMap.put("GEOMTYPE", flowGeom);
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName); //tables.get(1));
        paramMap.put("DBF_ID", model_output_id);

        return paramMap;
    }    
    
    private HashSet getMultiZnetworks() throws Exception
    { //currently only flows
        //dynamic retrieval is select distinct f_table_name from public.geometry_columns where f_table_schema = 'sparrow_overlay' and coord_dimension = 4;
        //note that this is not checking for 3 dim
        HashSet<String> multiZnetwork = new HashSet();
       // multiZnetwork.add("mrb01_nhd_flow");  //as of June 2016, only these two flows have the 4 dimension
       // multiZnetwork.add("chesa_nhd_flow");

        String sql = getText("Select4DimTables", this.getClass());
        LOGGER.info("getMultiZ table names sql: " + sql.toString());
        
        ResultSet rset = null;
        Statement statement = getPostgresStatement();
        
        try {
            rset = statement.executeQuery(sql);
            addResultSetForAutoClose(rset);

            while (rset.next()) {
                String tableName = rset.getString(1);
                multiZnetwork.add(tableName);
            }

        } finally {
            // rset can be null if there is an sql error. 
            if (rset != null) {
                rset.close();
            }
        }
        LOGGER.info("Quantity of tables with 4Dim found: " + multiZnetwork.size()); 
        return multiZnetwork;
    }    
    
    private void createCatchView(String tableName, Integer id) throws Exception
    {
        // VIEW_LAYER_NAME @RIVER_NETWORK_TABLE_NAME@, @DBF_ID@
        Map<String, Object> paramMap = new HashMap<>();
        String catchGeom = "net.geom ::geometry(MultiPolygon, 4326)";
        
        paramMap.put("VIEW_LAYER_NAME", "\"catch_" + id + "\"");
        paramMap.put("GEOMTYPE", catchGeom);
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName);
        paramMap.put("DBF_ID", id);
        
        String sql = getPostgresSqlFromPropertiesFile("CreateCatchView", null, paramMap);
        LOGGER.info("Postgres catch view created from: " + sql);
        Statement statement = getPostgresStatement();
        statement.executeUpdate(sql);
    }
    
    private void createFlowView(String tableName, Integer id) throws Exception
    {
        //VIEW_LAYER_NAME @RIVER_NETWORK_TABLE_NAME@, @DBF_ID@
        Map<String, Object> paramMap = new HashMap<>();
        
        paramMap.put("VIEW_LAYER_NAME", "\"flow_" + id + "\"");
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName);
        paramMap.put("DBF_ID", id);
        
        String sql = getPostgresSqlFromPropertiesFile("CreateFlowView", null, paramMap);
        LOGGER.info("Postgres flow view created from: " + sql);
        Statement statement = getPostgresStatement();
        statement.executeUpdate(sql);
    }
    
    private String createView(Map paramMap) throws Exception 
    {
        // Note: can not use a prepared statement for DDL queries
        //String sql = getPostgresSqlFromPropertiesFile("CreateView", null, paramMap);
        LOGGER.info("About to create Postgres view in LoadInitViews: " + paramMap.get("VIEW_LAYER_NAME"));
        Statement statement = getPostgresStatement();
        statement.executeUpdate(getPostgresSqlFromPropertiesFile("CreateView", null, paramMap));
        return (String) paramMap.get("VIEW_LAYER_NAME");
    }
    
    private ArrayList GetRegionTableNames(Integer id) throws Exception{
        ArrayList<String> result = new ArrayList();

        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("DBF_ID", id);
        
        PreparedStatement tableName = getPostgresPSFromPropertiesFile("GetRegionTableNames", null, paramMap);
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
        if (result.size() > 0){
            LOGGER.info("Using catch table name: " + result.get(0) );
            LOGGER.info(" and flow table name: " + result.get(1));
        }
        else
            log.debug("Unable to find region table for model id with this model_output_id:" + id);
        
        return result;
    }
    
    private Set getUniqueModelIds() throws Exception {
        Set<Integer> result = new HashSet();

        //PreparedStatement sqlPs = getPostgresPSFromPropertiesFile("GetDistinctModelOutputIds", null, null);
        String sql = getText("GetDistinctModelOutputIds", this.getClass());
        LOGGER.info("Get distinct output IDs sql: " + sql.toString());
        
        ResultSet rset = null;
        Statement statement = getPostgresStatement();
        
        try {
            rset = statement.executeQuery(sql);
            addResultSetForAutoClose(rset);

            while (rset.next()) {
                result.add(rset.getInt(1));
            }

        } finally {
            // rset can be null if there is an sql error. 
            if (rset != null) {
                rset.close();
            }
        }
        
        LOGGER.info("Quantity of unique model output ids obtained: " + result.size());
        return result;
    }

    private HashSet retrieveAllViewNames() throws Exception {
        HashSet<Integer> result = new HashSet();  //no duplicates allowed


        String sql = getText("RetrieveAllViews", this.getClass());
        LOGGER.info("Retrieve all view names sql: " + sql.toString());
        
        ResultSet rset = null;
        Statement statement = getPostgresStatement();
        
        try {
            rset = statement.executeQuery(sql);
            addResultSetForAutoClose(rset);

            while (rset.next()) {
                Integer parsed = parseIdFromViewName(rset.getString(1));
                if (parsed != 0)
                    result.add(parseIdFromViewName(rset.getString(1)));
            }

        } finally {
            // rset can be null if there is an sql error. 
            if (rset != null) {
                rset.close();
            }
        }
        LOGGER.info("Quantity of views found: " + result.size());
        return result;
    }

    // regex that makes sure the name matches the prefix, either the flow_ or the catch_, and then retrieves the int after that
    private Integer parseIdFromViewName(String viewName) {
        if (viewName.matches("^(flow_|catch_)[-]?[0-9]{1,12}$")) { //|| viewName.matches("catch_" + "-?\\d+")) {
            //parse the numeric off of the string that starts with either flow_ or catch_
            
            String[] strings = viewName.split("_");
            String id = strings[1]; //second half of name anticipated to be the model_output_id
            if (isInteger(id)) //return it as a qualifying model output id
            {
                // if the id exists, its included in the existing views set of ids
                log.info("The model output id: " + id + " has a view representation already:" + viewName + " No additional views will be created for it.");
                return new Integer(id);
            } else {
                log.info("The portion of the view name that represents the model output id was not an Integer and won't be included in the set to create views : " + id);
            }
        }
        else {
            log.info("View name did not match catch_ or flow_ prefix and is not included in the set.: " + viewName);
        }
        return 0;
    }

    private boolean isInteger(String parsed) {
        try {
            Integer.parseInt(parsed);
        } catch (NumberFormatException | NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    private HashSet determineModelOutputIdsWithoutViews(Set<Integer> rowIds, Set<Integer> viewIds) {
        //remove all ids from the rowIds where the viewId exists already
        HashSet<Integer> result = new HashSet();

        rowIds.removeAll(viewIds); // one line of code
        log.info("Views that will need to be created for these model output ids: " + rowIds);
        
        result.addAll(rowIds);
        
        return result;
    }
}