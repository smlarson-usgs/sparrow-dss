package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import gov.usgswim.sparrow.parser.DataSeriesType;

import java.util.ArrayList;

import org.junit.Test;


public class ActionUnitTest {

	
	@Test
	public void testProcessSql() {
		String testCase = "blah blahb aehawo $Variable1$ lhwoew whea fown $Variable2$ wlheown weoah $Variable3$";
		String expected = "blah blahb aehawo ? lhwoew whea fown ? wlheown weoah ?";
		Action.SQLString result = Action.processSql(testCase);
		
		assertEquals(expected, result.sql.toString());
		
		ArrayList<String> expectedVariables = new ArrayList<String>();
		expectedVariables.add("Variable1");
		expectedVariables.add("Variable2");
		expectedVariables.add("Variable3");
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
