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
        Set retrieveAllViewIds = retrieveAllViewNames();// get all the views and parse the name down to the id
        HashSet viewsNeeded = determineModelOutputIdsWithoutViews(uModelOutputIds,retrieveAllViewIds);//all the model_output_ids that did not have view 
        createViews(viewsNeeded); 

        return list;
    }

    //takes the model output IDs and joins to the approp river region determined by join to lookup table
    private void createViews(HashSet<Integer> model_output_ids) throws Exception
    {
       // loop thru the list of unique model output ids and create two views (catch and flow)
        for (Integer model_id : model_output_ids) {
            ArrayList<String> tables = GetRegionTableNames(model_id);
            createCatchView(tables.get(0), model_id);
            createFlowView(tables.get(1), model_id);
        }
    }
    
    private void createCatchView(String tableName, Integer id) throws Exception
    {
        // @RIVER_NETWORK_TABLE_NAME@, @DBF_ID@
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName);
        paramMap.put("DBF_ID", id);
        
        String sql = getPostgresSqlFromPropertiesFile("CreateCatchView", null, paramMap);
        LOGGER.info("Postgres catch view created from: " + sql);
        Statement statement = getPostgresStatement();
        statement.executeUpdate(sql);
    }
    
    private void createFlowView(String tableName, Integer id) throws Exception
    {
        //@RIVER_NETWORK_TABLE_NAME@, @DBF_ID@
        Map<String, Object> paramMap = new HashMap<>();
        paramMap.put("RIVER_NETWORK_TABLE_NAME", tableName);
        paramMap.put("DBF_ID", id);
        
        String sql = getPostgresSqlFromPropertiesFile("CreateFlowView", null, paramMap);
        LOGGER.info("Postgres catch view created from: " + sql);
        Statement statement = getPostgresStatement();
        statement.executeUpdate(sql);
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
        LOGGER.info("Using catch table name: " + result.get(0) );
        LOGGER.info(" and flow table name: " + result.get(1));
        
        
        return result;
    }
    
    private Set getUniqueModelIds() throws Exception {
        Set<Integer> result = new HashSet();

        PreparedStatement sqlPs = getPostgresPSFromPropertiesFile("GetDistinctModelOutputIds", null, null);
        LOGGER.info("Get distinct output IDs sql: " + sqlPs.toString());
        ResultSet rset = null;

        try {
            rset = sqlPs.executeQuery();
            addResultSetForAutoClose(rset);

            while (rset.next()) {
                result.add(rset.getInt("model_output_id"));
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

        PreparedStatement sqlPs = getPostgresPSFromPropertiesFile("RetrieveAllViews", null, null);
        LOGGER.info("Get all view names in schema sparrow_overlay: " + sqlPs.toString());
        ResultSet rset = null;

        try {
            rset = sqlPs.executeQuery();
            addResultSetForAutoClose(rset);

            while (rset.next()) {
                String viewName = rset.getString("viewname");  //will look like catch_-897439434 or flow_343243243
                Integer model_output_id = parseIdFromViewName(viewName);// #TODO# some parsed int from the view name catch or flow removed (only need one or the other)
                if (model_output_id != 0){
                    result.add(parseIdFromViewName(viewName)); //the set, by its nature, will eliminate the duplicate entries
                }
            }

        } finally {
            // rset can be null if there is an sql error. 
            if (rset != null) {
                rset.close();
            }
        }
        LOGGER.info("Quantity of modelIds from views: " + result.size());
        return result;
    }

    // regex that makes sure the name matches the prefix, either the flow_ or the catch_, and then retrieves the int after that
    private Integer parseIdFromViewName(String viewName) {
        if (viewName.matches("flow_" + "^-?\\d+") || viewName.matches("catch_" + "^-?\\d+")) {
            //parse the numeric off of the string
            String[] strings = viewName.split("_");
            String id = strings[1]; //second half of name anticipated to be the model_output_id
            if (isInteger(id)) //return it as a qualifying model output id
            {
                return new Integer(id);
            } else {
                log.info("The portion of the view name that represents the model output id was not an Integer and won't be included in the set: " + id);
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
        //Iterator it = rowIds.iterator();

        for (Integer id : viewIds) {
            if (rowIds.contains(id)) {
                //skip it
                log.info("View already exists for model_output_id: " + id);
            } else {    
                result.add(id);
                log.info("Will create two views for each model_output_id: " + id);
            }
        }
        log.info("Found: " + result.size()+ " model_output_ids that will require views to be created.");

        return result;
    }
}
