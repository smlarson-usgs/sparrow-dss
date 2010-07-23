package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.datatable.HucLevel;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadUnitAreas methods.
 * 
 * @author klangsto
 */

public class LoadUnitAreasTest extends SparrowDBTest {

	/**
	 * Tests the basic getter and setter functionality.
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasGetters() throws Exception {
		
		LoadUnitAreas lua = new LoadUnitAreas();
		assertEquals(HucLevel.HUC_NONE, lua.getHucLevel());
		assertEquals(false, lua.isCumulative());
		lua.setCumulative(true);
		assertEquals(true, lua.isCumulative());
		lua.setCumulative(false);
		assertEquals(false, lua.isCumulative());

	}
	
	/**
	 * Tests the columns.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasColumns() throws Exception {
		
		LoadUnitAreas lua = new LoadUnitAreas();
		DataTable dt = lua.run();
		assertEquals(2, dt.getColumnCount());
		assertEquals(null, dt.getUnits(0));
		assertEquals("IDENTIFIER", dt.getName(0));
		assertEquals("sqr km", dt.getUnits(1));
		assertEquals("Catchment Area", dt.getName(1));
		
		lua = new LoadUnitAreas();
		lua.setCumulative(true);
		dt = lua.run();
		assertEquals(2, dt.getColumnCount());
		assertEquals(null, dt.getUnits(0));
		assertEquals("IDENTIFIER", dt.getName(0));
		assertEquals("sqr km", dt.getUnits(1));
		assertEquals("Cumulative Catchment Area", dt.getName(1));

	}
	
	/**
	 * Tests the data.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasData() throws Exception {
		
		LoadUnitAreas lua = new LoadUnitAreas(50, HucLevel.HUC_NONE, false);
		DataTable dt = lua.run();
		assertEquals(8321, dt.getRowCount());
		int row = dt.findFirst(0, 9388);
		assertEquals((Double) 100.91, dt.getDouble(row, 1));
		
		lua = new LoadUnitAreas(50, HucLevel.HUC_NONE, true);
		dt = lua.run();
		assertEquals(8321, dt.getRowCount());
		row = dt.findFirst(0, 9390);
		assertEquals((Double) 1091.98, dt.getDouble(row, 1));

	}
	
}

