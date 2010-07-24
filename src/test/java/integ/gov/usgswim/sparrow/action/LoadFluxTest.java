package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowDBTest;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadFlux methods.
 * 
 * @author klangsto
 */

public class LoadFluxTest extends SparrowDBTest {

	/**
	 * Tests the basic getter and setter functionality.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxSetters() throws Exception {

		LoadFlux lf = new LoadFlux();
		lf.setModelId(50);
		assertEquals(50, lf.getModelId());

	}
	
	/**
	 * Tests the columns.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxColumns() throws Exception {
		
		LoadFlux lf = new LoadFlux();
		DataTable dt = lf.run();
		assertEquals(2, dt.getColumnCount());
		assertEquals(null, dt.getUnits(0));
		assertEquals("IDENTIFIER", dt.getName(0));
		assertEquals("cu ft/s", dt.getUnits(1));
		assertEquals("Average Daily Flux", dt.getName(1));

	}
	
	/**
	 * Tests the data.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxData() throws Exception {
		
		LoadFlux lf = new LoadFlux(50);
		DataTable dt = lf.run();
		assertEquals(8321, dt.getRowCount());
		int row = dt.findFirst(0, 9388);
		assertEquals((Double) 1063.71, dt.getDouble(row, 1));
		
		lf = new LoadFlux(50);
		dt = lf.run();
		assertEquals(8321, dt.getRowCount());
		row = dt.findFirst(0, 9390);
		assertEquals((Double) 741.45, dt.getDouble(row, 1));

	}
	
}

