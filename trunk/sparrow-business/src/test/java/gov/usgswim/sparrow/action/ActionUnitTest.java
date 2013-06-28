package gov.usgswim.sparrow.action;

import com.mockrunner.mock.jdbc.*;
import com.mockrunner.jdbc.*;
import static org.junit.Assert.*;

import gov.usgswim.sparrow.domain.DataSeriesType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.junit.Before;

import org.junit.Test;

/**
 * Note that the JDBCTestCaseAdapter extends JUnit Test, so must follow old
 * JUnit test naming conventions.
 * @author eeverman
 */
public class ActionUnitTest extends JDBCTestCaseAdapter {
	MockConnection connection;
	
	public void setUp() {
		connection =
				getJDBCMockObjectFactory().getMockConnection();
	}
	

	/**
	 * IN clause type structures are created by passing in a collection as one of the
	 * params, resulting in one '?' per collection entry.
	 * @throws Exception 
	 */
	public void testSqlWithSimpleINClause() throws Exception {
		String testSqlSrc = "SELECT * FROM TABLE WHERE ID IN ($mylist$)";
		String expectSql = "SELECT * FROM TABLE WHERE ID IN (?, ?)";
		
		Map<String, Object> params = new HashMap<String, Object>();
		ArrayList<String> myList = new ArrayList<String>();
		myList.add("val1");
		myList.add("val2");
		params.put("mylist", myList);

		Action.SQLString result = Action.processSql(testSqlSrc, params);
		
		assertEquals(expectSql, result.sql.toString());
		assertEquals(1, result.variables.size());
		assertEquals("mylist", result.variables.get(0));
		
		MockPreparedStatement prepSatement = (MockPreparedStatement) connection.prepareStatement(result.sql.toString());
		
		Action.assignParameters(prepSatement, result, params);
		
		assertEquals("val1", prepSatement.getParameter(1).toString());
		assertEquals("val2", prepSatement.getParameter(2).toString());
		assertEquals(2, prepSatement.getParameterMap().size());
	}
	
	public void testSqlWithMultipleINClauses() throws Exception {
		String testSqlSrc = "SELECT * FROM TABLE WHERE ID IN ($myIds$) AND Name IN ($myNames$) AND Other = $other$";
		String expectSql =  "SELECT * FROM TABLE WHERE ID IN (?, ?) AND Name IN (?) AND Other = ?";
		
		Map<String, Object> params = new HashMap<String, Object>();
		ArrayList<String> myIds = new ArrayList<String>();
		myIds.add("val1");
		myIds.add("val2");
		params.put("myIds", myIds);
		
		HashSet<String> myNames = new HashSet<String>();	//Collection type shouldn't matter
		myNames.add("name1");
		params.put("myNames", myNames);
		params.put("myNames", myNames);
		
		params.put("other", "else");

		Action.SQLString result = Action.processSql(testSqlSrc, params);
		
		assertEquals(expectSql, result.sql.toString());
		assertEquals(3, result.variables.size());
		assertEquals("myIds", result.variables.get(0));
		assertEquals("myNames", result.variables.get(1));
		assertEquals("other", result.variables.get(2));
		
		MockPreparedStatement prepSatement = (MockPreparedStatement) connection.prepareStatement(result.sql.toString());
		
		Action.assignParameters(prepSatement, result, params);
		
		assertEquals("val1", prepSatement.getParameter(1).toString());
		assertEquals("val2", prepSatement.getParameter(2).toString());
		assertEquals("name1", prepSatement.getParameter(3).toString());
		assertEquals("else", prepSatement.getParameter(4).toString());
		assertEquals(4, prepSatement.getParameterMap().size());
	}
	
	
	public void testProcessSqlWithVanillaParams() {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("SqlParam1", "SqlParamVal1");
		params.put("SqlParam2", "SqlParamVal2");
		params.put("NonSqlParam1", "NonSqlVal1");
		
		String testCase = "blah @NonSqlParam1@ xx@NonSqlParam1@xx aehawo $SqlParam1$ lhwoew whea fown $SqlParam2$ wlheown weoah $SqlParam3$";
		String expected = "blah NonSqlVal1 xxNonSqlVal1xx aehawo ? lhwoew whea fown ? wlheown weoah ?";
		Action.SQLString result = Action.processSql(testCase, params);
		
		assertEquals(expected, result.sql.toString());
		
		ArrayList<String> expectedVariables = new ArrayList<String>();
		expectedVariables.add("SqlParam1");
		expectedVariables.add("SqlParam2");
		expectedVariables.add("SqlParam3");
		for (int i = 0; i < result.variables.size(); i++) {
			assertEquals(expectedVariables.get(i), result.variables.get(i));
		}
	}
	

	public void testProcessSqlWithNulls() {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("SqlParam1", "SqlParamVal1");
		params.put("SqlParam2", "SqlParamVal2");
		params.put("NonSqlParam1", null);
		params.put("DoesNotExist", null);
		
		String testCase = "blah @NonSqlParam1@ xx@NonSqlParam1@xx aehawo $SqlParam1$ lhwoew whea fown $SqlParam2$ wlheown weoah $SqlParam3$";
		String expected = "blah  xxxx aehawo ? lhwoew whea fown ? wlheown weoah ?";
		Action.SQLString result = Action.processSql(testCase, params);
		
		assertEquals(expected, result.sql.toString());
		
		ArrayList<String> expectedVariables = new ArrayList<String>();
		expectedVariables.add("SqlParam1");
		expectedVariables.add("SqlParam2");
		expectedVariables.add("SqlParam3");
		for (int i = 0; i < result.variables.size(); i++) {
			assertEquals(expectedVariables.get(i), result.variables.get(i));
		}
	}
	
	public void testCheckDataSeriesTypeProperties() throws Exception {
		Action act = new CalcConcentration();
		
		assertEquals("Total Load",
				act.getDataSeriesProperty(DataSeriesType.total.name(), false, "Not Found"));
		assertEquals("Total Load",
				act.getDataSeriesProperty(DataSeriesType.total, false, "Not Found"));
		assertEquals("Not Found",
				act.getDataSeriesProperty("does_not_exist", false, "Not Found"));
	}
}
