package gov.usgswim.sparrow.postgres.action;

import gov.usgswim.sparrow.action.Action;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.LoggerFactory;

/**
 *
 * @author smlarson
 */
public class DeleteModelOutput extends Action<List> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(DeleteModelOutput.class);
    private List<Integer> modelOutputIds;

    /**
     *
     * @param modelOutputIds
     */
    public DeleteModelOutput(List<Integer> modelOutputIds) {
        init(modelOutputIds);
    }

    private void init(List modelOutputIds) {
        this.modelOutputIds = modelOutputIds;
    }

    @Override
    protected void validate() {
        if (this.modelOutputIds.isEmpty() || this.modelOutputIds == null) {
            String msg = "The list of model_output ids was empty/null. Without the values, the rows can not be removed from the Postgres table, model_output ";
            this.addValidationError(msg);
        }

    }

    @Override
    public List doAction() throws Exception {
        List list = new ArrayList();

        for (Integer id : this.modelOutputIds) {
            deleteViews(id);
            deleteRow(id);
        }

        return list;
    }

    private void deleteRow(Integer modelOutputId) throws Exception {

        //get the values out of the map, each set requires an insert statement
        Map<String, Object> paramMap = new HashMap<>();// this map is for sql parms
        paramMap.put("MODEL_OUTPUT_ID", modelOutputId);

        PreparedStatement deleteSqlps = getPostgresPSFromPropertiesFile("DeleteModelOutput", null, paramMap);
        LOGGER.info("Postgres delete sql: " + deleteSqlps.toString());
        int value = deleteSqlps.executeUpdate();
        if (value > 0) {
            LOGGER.info("Postgres model_output rows with : " + modelOutputId + " are deleted. Quantity: " + value + " deleted rows.");
        } else {
            String msg = "No model output rows were deleted for model_output_id: " + modelOutputId;
            //this.addValidationError(msg); //not certain this is an error
            LOGGER.debug(msg);
            
        }
       
    }
    
    private void deleteViews(Integer modelOutputId) throws Exception {
        //hoping the one statement will drop both views.
        Map<String, Object> paramMap = new HashMap<>();// this map is for sql parms
        paramMap.put("MODEL_OUTPUT_ID", modelOutputId);
        paramMap.put("MODEL_OUTPUT_ID", modelOutputId);

        Statement statement = getPostgresStatement();
        String sql = getPostgresSqlFromPropertiesFile("DropView", null, paramMap);
        LOGGER.info("Postgres drop views: " + sql.toString()); 
        statement.executeUpdate(sql);  
    }
    
}
