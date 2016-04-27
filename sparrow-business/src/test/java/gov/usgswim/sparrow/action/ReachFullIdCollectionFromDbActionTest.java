package gov.usgswim.sparrow.action;

import com.mockrunner.jdbc.JDBCTestCaseAdapter;
import com.mockrunner.jdbc.ParameterSets;
import com.mockrunner.jdbc.PreparedStatementResultSetHandler;
import com.mockrunner.mock.jdbc.MockConnection;
import com.mockrunner.mock.jdbc.MockResultSet;
import gov.usgswim.sparrow.domain.ReachFullId;
import gov.usgswim.sparrow.request.ReachClientId;
import java.util.ArrayList;
import java.util.List;

/**
 * Test with a Mock connection so that we don't go to the DB.
 * @author eeverman
 */
public class ReachFullIdCollectionFromDbActionTest extends JDBCTestCaseAdapter {
	MockConnection connection;
	
	public void setUp() {
		connection =
				getJDBCMockObjectFactory().getMockConnection();
		PreparedStatementResultSetHandler handler = connection.getPreparedStatementResultSetHandler();
		MockResultSet resultSet = handler.createResultSet();
		
		Long[] reachIds = {1L, 2L};
		String[] reachClientids = {"C1", "C2"};
		
		resultSet.addColumn("IDENTIFIER", reachIds);
		resultSet.addColumn("FULL_IDENTIFIER", reachClientids);
		
		handler.prepareGlobalResultSet(resultSet);
	}
	
	public void testBasicSingleQuery() throws Exception {
		ArrayList<ReachClientId> clientIds = new ArrayList<ReachClientId>();
		
		clientIds.add(new ReachClientId(50L, "C1"));
		clientIds.add(new ReachClientId(50L, "C2"));
		
		ReachFullIdCollectionFromDbAction action = new ReachFullIdCollectionFromDbAction(clientIds);
		
		List<ReachFullId> fullIds = action.run(connection, connection, connection);
	
		String actualSql = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		//ParameterSets actualParams = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql);
		String expectedSql = "SELECT IDENTIFIER, FULL_IDENTIFIER FROM MODEL_REACH WHERE SPARROW_MODEL_ID = ? AND FULL_IDENTIFIER IN (?, ?)";
		
		assertEquals(expectedSql, actualSql);
		
		
		assertEquals(2, fullIds.size());
		assertEquals(50L, fullIds.get(0).getModelId());
		assertEquals(1L, fullIds.get(0).getReachId());
		assertEquals("C1", fullIds.get(0).getReachClientId());
		assertEquals(50L, fullIds.get(1).getModelId());
		assertEquals(2L, fullIds.get(1).getReachId());
		assertEquals("C2", fullIds.get(1).getReachClientId());
	}
	
	public void testBasicQueryWith1000Ids() throws Exception {
		ArrayList<ReachClientId> clientIds = new ArrayList<ReachClientId>();
		
		for (int i = 1; i <= 1000; i++) {
			clientIds.add(new ReachClientId(50L, "C" + i));
		}
		
		ReachFullIdCollectionFromDbAction action = new ReachFullIdCollectionFromDbAction(clientIds);
		
		List<ReachFullId> fullIds = action.run(connection, connection, connection);
	
		String actualSql = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		ParameterSets actualParams = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql);

		//Should only be a single SQL statement run
		assertEquals(1, connection.getPreparedStatementResultSetHandler().getExecutedStatements().size());
		
		//Should be 1001 '?' characters, so 1002 string chunks
		assertEquals(1002, actualSql.split("\\?").length);
		assertEquals(1001, actualParams.getParameterSet(0).size());
	}
	
	public void testBasicQueryWith1001Ids() throws Exception {
		ArrayList<ReachClientId> clientIds = new ArrayList<ReachClientId>();
		
		for (int i = 1; i <= 1001; i++) {
			clientIds.add(new ReachClientId(50L, "C" + i));
		}
		
		ReachFullIdCollectionFromDbAction action = new ReachFullIdCollectionFromDbAction(clientIds);
		
		List<ReachFullId> fullIds = action.run(connection, connection, connection);
	
		String actualSql1 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		String actualSql2 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(1).toString();
		ParameterSets actualParams1 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql1);
		ParameterSets actualParams2 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql2);
		
		//Should 2 SQL statements run
		assertEquals(2, connection.getPreparedStatementResultSetHandler().getExecutedStatements().size());
		
		//First Query should have 1001 '?' characters, so 1002 string chunks
		assertEquals(1002, actualSql1.split("\\?").length);
		assertEquals(1001, actualParams1.getParameterSet(0).size());
				
		//2nd Query should have 2 '?' characters, so 3 string chunks
		assertEquals(3, actualSql2.split("\\?").length);
		assertEquals(2, actualParams2.getParameterSet(0).size());
		
	}
	
	public void testBasicQueryWith2000Ids() throws Exception {
		ArrayList<ReachClientId> clientIds = new ArrayList<ReachClientId>();
		
		for (int i = 1; i <= 2000; i++) {
			clientIds.add(new ReachClientId(50L, "C" + i));
		}
		
		ReachFullIdCollectionFromDbAction action = new ReachFullIdCollectionFromDbAction(clientIds);
		
		List<ReachFullId> fullIds = action.run(connection, connection, connection);
	
		String actualSql1 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		String actualSql2 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(1).toString();
		ParameterSets actualParams1 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql1);
		ParameterSets actualParams2 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql2);
		
		//Should 2 SQL statements run
		assertEquals(2, connection.getPreparedStatementResultSetHandler().getExecutedStatements().size());
		
		//First Query should have 1001 '?' characters, so 1002 string chunks
		assertEquals(1002, actualSql1.split("\\?").length);
		assertEquals(1001, actualParams1.getParameterSet(0).size());
				
		//2nd Query should have 1001 '?' characters, so 1002 string chunks
		assertEquals(1002, actualSql2.split("\\?").length);
		assertEquals(1001, actualParams2.getParameterSet(0).size());
		
	}
	
	public void testBasicQueryWith2001Ids() throws Exception {
		ArrayList<ReachClientId> clientIds = new ArrayList<ReachClientId>();
		
		for (int i = 1; i <= 2001; i++) {
			clientIds.add(new ReachClientId(50L, "C" + i));
		}
		
		ReachFullIdCollectionFromDbAction action = new ReachFullIdCollectionFromDbAction(clientIds);
		
		List<ReachFullId> fullIds = action.run(connection, connection, connection);
	
		String actualSql1 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(0).toString();
		String actualSql2 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(1).toString();
		String actualSql3 = connection.getPreparedStatementResultSetHandler().getExecutedStatements().get(2).toString();
		ParameterSets actualParams1 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql1);
		ParameterSets actualParams2 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql2);
		ParameterSets actualParams3 = connection.getPreparedStatementResultSetHandler().getParametersForExecutedStatement(actualSql3);
		
		//Should 2 SQL statements run
		assertEquals(3, connection.getPreparedStatementResultSetHandler().getExecutedStatements().size());
		
		//First Query should have 1001 '?' characters, so 1002 string chunks
		assertEquals(1002, actualSql1.split("\\?").length);
		assertEquals(1001, actualParams1.getParameterSet(0).size());
				
		//2nd Query should have 1001 '?' characters, so 1002 string chunks
		assertEquals(1002, actualSql2.split("\\?").length);
		assertEquals(1001, actualParams2.getParameterSet(0).size());
		
		//3rd Query should have 2 '?' characters, so 3 string chunks
		assertEquals(3, actualSql3.split("\\?").length);
		assertEquals(2, actualParams3.getParameterSet(0).size());
		
	}
}
