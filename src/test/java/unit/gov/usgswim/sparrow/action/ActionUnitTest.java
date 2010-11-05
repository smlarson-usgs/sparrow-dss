package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import gov.usgswim.sparrow.parser.DataSeriesType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;


public class ActionUnitTest {

	
	@Test
	public void testProcessSql() {
		
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
	
	@Test
	public void checkDataSeriesTypeProperties() throws Exception {
		Action act = new CalcConcentration();
		
		assertEquals("Total Load",
				act.getDataSeriesProperty(DataSeriesType.total.name(), false, "Not Found"));
		assertEquals("Total Load",
				act.getDataSeriesProperty(DataSeriesType.total, false, "Not Found"));
		assertEquals("Not Found",
				act.getDataSeriesProperty("does_not_exist", false, "Not Found"));
	}
}
