package gov.usgswim.sparrow.action;

import com.mockrunner.jdbc.JDBCTestCaseAdapter;
import com.mockrunner.jdbc.ParameterSets;
import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockResultSet;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.datatable.ModelReachAreaDataTable;
import gov.usgswim.sparrow.domain.ReachFullId;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.ReachClientId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import org.apache.commons.lang.StringUtils;

/**
 * Test with a Mock connection so that we don't go to the DB.
 * @author eeverman
 */
public class LoadReachAreasTest extends JDBCTestCaseAdapter {
	MockConnection connection;
	
	PredictData model50PredictData;
	
	@Override
	public void setUp() throws Exception {
		
		model50PredictData = SparrowTestBase.getTestModelPredictData();
		
		
		connection =
				getJDBCMockObjectFactory().getMockConnection();
		PreparedStatementResultSetHandler handler = connection.getPreparedStatementResultSetHandler();
		MockResultSet resultSet = handler.createResultSet();
		
		//These need to be real reach IDs, b/c they are looked up in the action
		Long[] reachIds = {model50PredictData.getIdForRow(1000), model50PredictData.getIdForRow(1001)};
		
		Double[] catchAreas = {10D, 20D};
		Double[] contribAreas = {11D, 21D};
		Double[] upstreamAreas = {12D, 22D};
		
		resultSet.addColumn("identifier", reachIds);
		resultSet.addColumn("incrementalArea", catchAreas);
		resultSet.addColumn("totalContributingArea", contribAreas);
		resultSet.addColumn("totalUpstreamArea", upstreamAreas);
		
		handler.prepareGlobalResultSet(resultSet);
		
		
	}
	
	
	public void testBasicSingleQuery() throws Exception {
		
		ArrayList<Long> reachIds = new ArrayList<Long>();
		
		reachIds.add(model50PredictData.getIdForRow(1000));
		reachIds.add(model50PredictData.getIdForRow(1001));
		
		LoadReachesAreas action = new LoadReachesAreas(reachIds, model50PredictData);
		ModelReachAreaDataTable reachAreas = action.run(connection, connection);
		
		//Actual SQL string and params
		String actualSql = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		ParameterSets actualParams = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql);
		
		String expectedSql = "SELECT identifier, catch_area as incrementalArea, tot_contrib_area as totalContributingArea, tot_upstream_area as totalUpstreamArea FROM MODEL_ATTRIB_VW WHERE sparrow_model_id = ? AND identifier IN (?, ?)";
		
		//Check sql and params
		assertEquals(StringUtils.deleteWhitespace(expectedSql), StringUtils.deleteWhitespace(actualSql));
		assertEquals(3, actualParams.getParameterSet(0).size());
		assertEquals(new Long(50), actualParams.getParameterSet(0).get(1));
		assertEquals(new Long(model50PredictData.getIdForRow(1000)), actualParams.getParameterSet(0).get(2));
		assertEquals(new Long(model50PredictData.getIdForRow(1001)), actualParams.getParameterSet(0).get(3));
		
		
		
		assertFalse(action.hasValidationErrors());
		assertNotNull(reachAreas);
		assertFalse(reachAreas.hasRowIds());
		assertEquals(model50PredictData.getTopo().getRowCount(), reachAreas.getRowCount());
		
		//Row 1000
		assertEquals(new Double(10D), reachAreas.getIncrementalAreaColumn().getDouble(1000), .0000001D);
		assertEquals(new Double(11D), reachAreas.getTotalContributingAreaColumn().getDouble(1000), .0000001D);
		assertEquals(new Double(12D), reachAreas.getTotalUpstreamAreaColumn().getDouble(1000), .0000001D);
		
		//Row 1001
		assertEquals(new Double(20D), reachAreas.getIncrementalAreaColumn().getDouble(1001), .0000001D);
		assertEquals(new Double(21D), reachAreas.getTotalContributingAreaColumn().getDouble(1001), .0000001D);
		assertEquals(new Double(22D), reachAreas.getTotalUpstreamAreaColumn().getDouble(1001), .0000001D);

	}
	
	public void testBasicQueryWith1000Ids() throws Exception {
		ArrayList<Long> reachIds = new ArrayList<Long>();
		
		for (int i = 0; i < 1000; i++) {
			reachIds.add(model50PredictData.getIdForRow(1000 + i));
		}
		
		LoadReachesAreas action = new LoadReachesAreas(reachIds, model50PredictData);
		ModelReachAreaDataTable reachAreas = action.run(connection, connection);
	
		//Actual SQL string and params
		String actualSql = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		ParameterSets actualParams = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql);
		
		StringBuffer expectedSql = new StringBuffer("SELECT identifier, catch_area as incrementalArea, tot_contrib_area as totalContributingArea, tot_upstream_area as totalUpstreamArea FROM MODEL_ATTRIB_VW WHERE sparrow_model_id = ? AND identifier IN (");
		expectedSql.append(StringUtils.repeat("?", ",", 1000));
		expectedSql.append(")");
		
		//Should only be a single SQL statement run
		assertEquals(1, connection.getPreparedStatementResultSetHandler().getExecutedStatements().size());
		
		//Check sql and params
		assertEquals(StringUtils.deleteWhitespace(expectedSql.toString()), StringUtils.deleteWhitespace(actualSql));
		assertEquals(1001, actualParams.getParameterSet(0).size());
		assertEquals(new Long(50), actualParams.getParameterSet(0).get(1));
		assertEquals(new Long(model50PredictData.getIdForRow(1000)), actualParams.getParameterSet(0).get(2));
		assertEquals(new Long(model50PredictData.getIdForRow(1999)), actualParams.getParameterSet(0).get(1001));
		

	}
	
	public void testBasicQueryWith1001Ids() throws Exception {
		ArrayList<Long> reachIds = new ArrayList<Long>();
		
		for (int i = 0; i < 1001; i++) {
			reachIds.add(model50PredictData.getIdForRow(1000 + i));
		}
		
		System.out.println("1000 index for 10001 reaches: " + reachIds.get(1000));
		
		LoadReachesAreas action = new LoadReachesAreas(reachIds, model50PredictData);
		ModelReachAreaDataTable reachAreas = action.run(connection, connection);
		
		//Should be 2 SQL statements run
		assertEquals(2, connection.getPreparedStatementResultSetHandler().getExecutedStatements().size());
	
		//Actual SQL string and params
		String actualSql_1 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		ParameterSets actualParams_1 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql_1);
		String actualSql_2 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(1).toString();
		ParameterSets actualParams_2 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql_2);
		
		StringBuffer expectedSql_1 = new StringBuffer("SELECT identifier, catch_area as incrementalArea, tot_contrib_area as totalContributingArea, tot_upstream_area as totalUpstreamArea FROM MODEL_ATTRIB_VW WHERE sparrow_model_id = ? AND identifier IN (");
		expectedSql_1.append(StringUtils.repeat("?", ",", 1000));
		expectedSql_1.append(")");
		
		String expectedSql_2 = "SELECT identifier, catch_area as incrementalArea, tot_contrib_area as totalContributingArea, tot_upstream_area as totalUpstreamArea FROM MODEL_ATTRIB_VW WHERE sparrow_model_id = ? AND identifier IN (?)";
		
		
		//Check 1st set of sql and params
		assertEquals(StringUtils.deleteWhitespace(expectedSql_1.toString()), StringUtils.deleteWhitespace(actualSql_1));
		assertEquals(1001, actualParams_1.getParameterSet(0).size());
		assertEquals(new Long(50), actualParams_1.getParameterSet(0).get(1));
		assertEquals(new Long(model50PredictData.getIdForRow(1000)), actualParams_1.getParameterSet(0).get(2));
		assertEquals(new Long(model50PredictData.getIdForRow(1999)), actualParams_1.getParameterSet(0).get(1001));
		
		//Check 1st set of sql and params
		assertEquals(StringUtils.deleteWhitespace(expectedSql_2.toString()), StringUtils.deleteWhitespace(actualSql_2));
		assertEquals(2, actualParams_2.getParameterSet(0).size());
		assertEquals(new Long(50), actualParams_2.getParameterSet(0).get(1));
		assertEquals(new Long(model50PredictData.getIdForRow(2000)), actualParams_2.getParameterSet(0).get(2));
		
	}
	
	

	
	public void testBasicQueryWith2000Ids() throws Exception {
		ArrayList<Long> reachIds = new ArrayList<Long>();
		
		for (int i = 0; i < 2000; i++) {
			reachIds.add(model50PredictData.getIdForRow(1000 + i));
		}
		
		System.out.println("1000 index for 2000 reaches: " + reachIds.get(1000));
		
		LoadReachesAreas action = new LoadReachesAreas(reachIds, model50PredictData);
		ModelReachAreaDataTable reachAreas = action.run(connection, connection);
		
		//Should be 2 SQL statements run
		assertEquals(2, connection.getPreparedStatementResultSetHandler().getExecutedStatements().size());
	
		//Actual SQL string and params (for 2K ids, the same statement is executed twice w/ two difference sets of params)
		String actualSql_1 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		Map actualParams_1 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql_1).getParameterSet(0);
		String actualSql_2 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(1).toString();
		Map actualParams_2 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql_1).getParameterSet(1);
		
		//This statement would have been executed twice
		StringBuffer expectedSql_1 = new StringBuffer("SELECT identifier, catch_area as incrementalArea, tot_contrib_area as totalContributingArea, tot_upstream_area as totalUpstreamArea FROM MODEL_ATTRIB_VW WHERE sparrow_model_id = ? AND identifier IN (");
		expectedSql_1.append(StringUtils.repeat("?", ",", 1000));
		expectedSql_1.append(")");
		
		
		//Check 1st set of sql and params
		assertEquals(StringUtils.deleteWhitespace(expectedSql_1.toString()), StringUtils.deleteWhitespace(actualSql_1));
		assertEquals(1001, actualParams_1.size());
		assertEquals(new Long(50), actualParams_1.get(1));
		assertEquals(new Long(model50PredictData.getIdForRow(1000)), actualParams_1.get(2));
		assertEquals(new Long(model50PredictData.getIdForRow(1999)), actualParams_1.get(1001));
		
		//Check 2st set of sql and params
		assertEquals(StringUtils.deleteWhitespace(expectedSql_1.toString()), StringUtils.deleteWhitespace(actualSql_2));
		assertEquals(1001, actualParams_2.size());
		assertEquals(new Long(50), actualParams_2.get(1));
		assertEquals(new Long(model50PredictData.getIdForRow(2000)), actualParams_2.get(2));
		assertEquals(new Long(model50PredictData.getIdForRow(2999)), actualParams_2.get(1001));
		
	}
}
