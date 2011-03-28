package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.util.DLUtils;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

/**
 * @author eeverman
 *
 */
public class CalcConcentrationTest {

	static final int EXAMPLE_CONTEXT_ID = 50;
	static final long EXAMPLE_MODEL_ID = 99;
	
	final static double CONV_FACTOR = .0011198d;
	
	final static double OK_ERR = .00000000001d;
	
	
	DataTableWritable baseTable;
	
	@Before
	public void doInit() throws Exception {
		InputStream fileStream =
			getClass().getResourceAsStream("/gov/usgswim/sparrow/tab_delimit_sample_data.txt");

		BufferedReader br = new BufferedReader(new InputStreamReader(
				fileStream
			));
		
		baseTable = TabDelimFileUtil.readAsDouble(br,
				true, DLUtils.DO_NOT_INDEX);
		
		//build row IDs matching the row index
		for (int i=0; i< baseTable.getRowCount(); i++) {
			baseTable.setRowId(i, i);
		}
		

	}
	
	@Test
	public void testTheTestSetup() {
		assertEquals(5, baseTable.getColumnCount());
		assertEquals(10, baseTable.getRowCount());
		assertEquals(0d, baseTable.getDouble(0, 0), OK_ERR);
		assertEquals(94d, baseTable.getDouble(9, 4), OK_ERR);
		assertTrue(baseTable.hasRowIds());
	}
	

	@Test
	public void testWithNoZeroDivisors() throws Exception {

		int baseColIndex = 0;
		int flowColIndex = 1;
		
		baseTable.getColumns()[baseColIndex].setUnits(SparrowUnits.KG_PER_YEAR.getUserName());
		baseTable.getColumns()[flowColIndex].setUnits(SparrowUnits.CFS.getUserName());
		
		SparrowColumnSpecifier baseCol = new SparrowColumnSpecifier(baseTable, baseColIndex, EXAMPLE_CONTEXT_ID);
		SparrowColumnSpecifier flowCol = new SparrowColumnSpecifier(baseTable, flowColIndex, null, EXAMPLE_MODEL_ID);
		
		CalcConcentration calc = new CalcConcentration();
		calc.setBaseData(baseCol);
		calc.setStreamFlowData(flowCol);
		
		SparrowColumnSpecifier result = calc.run();
		
		
		assertEquals((0d / 1d) * CONV_FACTOR, result.getDouble(0), OK_ERR);
		assertEquals((10d / 11d) * CONV_FACTOR, result.getDouble(1), OK_ERR);
		assertEquals((90d / 91d) * CONV_FACTOR, result.getDouble(9), OK_ERR);
		
	}
	
	@Test
	public void testWithZeroDivisor() throws Exception {

		int baseColIndex = 1;
		int flowColIndex = 0;
		
		baseTable.getColumns()[baseColIndex].setUnits(SparrowUnits.KG_PER_YEAR.getUserName());
		baseTable.getColumns()[flowColIndex].setUnits(SparrowUnits.CFS.getUserName());
		
		
		SparrowColumnSpecifier baseCol = new SparrowColumnSpecifier(baseTable, baseColIndex, EXAMPLE_CONTEXT_ID);
		SparrowColumnSpecifier flowCol = new SparrowColumnSpecifier(baseTable, flowColIndex, null, EXAMPLE_MODEL_ID);
		
		CalcConcentration calc = new CalcConcentration();
		calc.setBaseData(baseCol);
		calc.setStreamFlowData(flowCol);
		
		SparrowColumnSpecifier result = calc.run();
		
		
		assertEquals(Double.POSITIVE_INFINITY, result.getDouble(0), OK_ERR);
		assertEquals((11d / 10d) * CONV_FACTOR, result.getDouble(1), OK_ERR);
		assertEquals((91d / 90d) * CONV_FACTOR, result.getDouble(9), OK_ERR);
		
	}
	
	@Test
	public void testWithNotANumber() throws Exception {

		int baseColIndex = 1;
		int flowColIndex = 0;
		
		baseTable.getColumns()[baseColIndex].setUnits(SparrowUnits.KG_PER_YEAR.getUserName());
		baseTable.getColumns()[flowColIndex].setUnits(SparrowUnits.CFS.getUserName());
		
		baseTable.setValue(Double.NaN, 0, baseColIndex);
		SparrowColumnSpecifier baseCol = new SparrowColumnSpecifier(baseTable, baseColIndex, EXAMPLE_CONTEXT_ID);
		SparrowColumnSpecifier flowCol = new SparrowColumnSpecifier(baseTable, flowColIndex, null, EXAMPLE_MODEL_ID);
		
		CalcConcentration calc = new CalcConcentration();
		calc.setBaseData(baseCol);
		calc.setStreamFlowData(flowCol);
		
		SparrowColumnSpecifier result = calc.run();
		
		
		assertEquals(Double.NaN, result.getDouble(0), OK_ERR);
		assertEquals((11d / 10d) * CONV_FACTOR, result.getDouble(1), OK_ERR);
		assertEquals((91d / 90d) * CONV_FACTOR, result.getDouble(9), OK_ERR);
		
	}
	
	@Test
	public void testWithWrongLoadUnit() throws Exception {

		int baseColIndex = 1;
		int flowColIndex = 0;
		
		baseTable.getColumns()[baseColIndex].setUnits(SparrowUnits.SQR_KM.getUserName());	//wrong
		baseTable.getColumns()[flowColIndex].setUnits(SparrowUnits.CFS.getUserName());
		
		SparrowColumnSpecifier baseCol = new SparrowColumnSpecifier(baseTable, baseColIndex, EXAMPLE_CONTEXT_ID);
		SparrowColumnSpecifier flowCol = new SparrowColumnSpecifier(baseTable, flowColIndex, null, EXAMPLE_MODEL_ID);
		
		CalcConcentration calc = new CalcConcentration();
		calc.setBaseData(baseCol);
		calc.setStreamFlowData(flowCol);
		
		//Run the action w/ logging turned off
		Level actionLogLevel = Logger.getLogger(Action.class).getLevel();
		Logger.getLogger(Action.class).setLevel(Level.FATAL);
		SparrowColumnSpecifier result = calc.run();
		Logger.getLogger(Action.class).setLevel(actionLogLevel);
		
		assertNull(result);
	}
	
	@Test
	public void testWithWrongFlowUnit() throws Exception {

		int baseColIndex = 1;
		int flowColIndex = 0;
		
		baseTable.getColumns()[baseColIndex].setUnits(SparrowUnits.KG_PER_YEAR.getUserName());	//wrong
		baseTable.getColumns()[flowColIndex].setUnits(SparrowUnits.MG_PER_L.getUserName());
		
		SparrowColumnSpecifier baseCol = new SparrowColumnSpecifier(baseTable, baseColIndex, EXAMPLE_CONTEXT_ID);
		SparrowColumnSpecifier flowCol = new SparrowColumnSpecifier(baseTable, flowColIndex, null, EXAMPLE_MODEL_ID);
		
		CalcConcentration calc = new CalcConcentration();
		calc.setBaseData(baseCol);
		calc.setStreamFlowData(flowCol);
		
		//Run the action w/ logging turned off
		Level actionLogLevel = Logger.getLogger(Action.class).getLevel();
		Logger.getLogger(Action.class).setLevel(Level.FATAL);
		SparrowColumnSpecifier result = calc.run();
		Logger.getLogger(Action.class).setLevel(actionLogLevel);
		
		assertNull(result);
	}
	

}
