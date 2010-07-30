package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadFlux methods.
 * 
 * @author klangsto
 */

public class LoadStreamFlowTest extends SparrowDBTest {

	/**
	 * Tests the basic getter and setter functionality.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxSetters() throws Exception {

		LoadStreamFlow lf = new LoadStreamFlow();
		lf.setModelId(50);
		assertEquals(50, lf.getModelId());

	}
	
	/**
	 * Tests the columns.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxColumns() throws Exception {
		
		LoadStreamFlow lf = new LoadStreamFlow();
		DataColumn dataColumn = lf.run();
		DataTable dt = dataColumn.getTable();
		
		assertEquals(2, dt.getColumnCount());
		assertEquals(null, dt.getUnits(0));
		assertEquals("IDENTIFIER", dt.getName(0));
		
		assertEquals("Stream Flow", dt.getName(1));
		assertEquals("Averaged flow of the stream reach", dt.getDescription(1));
		assertEquals(DataSeriesType.flux.name(),
				dt.getProperty(1, TableProperties.DATA_SERIES.getPublicName()));
		assertEquals("Water",
				dt.getProperty(1, TableProperties.CONSTITUENT.getPublicName()));
		

	}
	
	/**
	 * Tests the data.
	 * @throws Exception
	 */
	@Test
	public void testLoadFluxData() throws Exception {
		
		LoadStreamFlow lf = new LoadStreamFlow(50);
		DataColumn dataColumn = lf.run();
		DataTable dt = dataColumn.getTable();
		
		assertEquals(8321, dt.getRowCount());
		int row = dt.findFirst(0, 9388);
		assertEquals((Double) 1063.71, dt.getDouble(row, 1));
		row = dt.findFirst(0, 9390);
		assertEquals((Double) 741.45, dt.getDouble(row, 1));

	}
	
}

