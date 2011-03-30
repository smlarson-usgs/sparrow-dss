package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.UnitAreaType;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadUnitAreas methods.
 * 
 * @author klangsto
 */

public class LoadUnitAreasLongRunTest extends SparrowTestBaseWithDB {

	/**
	 * Tests the basic getter and setter functionality.
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasSetters() throws Exception {
		
		LoadUnitAreas lua = new LoadUnitAreas();
		assertEquals(UnitAreaType.HUC_NONE, lua.getHucLevel());
		assertEquals(false, lua.isCumulative());
		lua.setCumulative(true);
		assertEquals(true, lua.isCumulative());
		lua.setCumulative(false);
		assertEquals(false, lua.isCumulative());

	}
	
	/**
	 * Tests the columns.
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasColumns() throws Exception {
		
		LoadUnitAreas lua = new LoadUnitAreas();
		DataTable dt = lua.run();
		assertEquals(2, dt.getColumnCount());
		assertEquals(null, dt.getUnits(0));
		assertEquals("IDENTIFIER", dt.getName(0));
		
		assertEquals(SparrowUnits.SQR_KM.toString(), dt.getUnits(1));
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.catch_area, false), dt.getName(1));
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.catch_area, true), dt.getDescription(1));
		assertEquals(DataSeriesType.catch_area.name(), dt.getProperty(1, TableProperties.DATA_SERIES.getPublicName()));
		assertEquals("land area", dt.getProperty(1, TableProperties.CONSTITUENT.getPublicName()));
		
		lua = new LoadUnitAreas();
		lua.setCumulative(true);
		dt = lua.run();
		assertEquals(2, dt.getColumnCount());
		assertEquals(null, dt.getUnits(0));
		assertEquals("IDENTIFIER", dt.getName(0));
		
		assertEquals(SparrowUnits.SQR_KM.toString(), dt.getUnits(1));
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.watershed_area, false), dt.getName(1));
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.watershed_area, true), dt.getDescription(1));
		assertEquals(DataSeriesType.watershed_area.name(), dt.getProperty(1, TableProperties.DATA_SERIES.getPublicName()));
		assertEquals("land area", dt.getProperty(1, TableProperties.CONSTITUENT.getPublicName()));

	}
	
	/**
	 * Tests the data.
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasData() throws Exception {
		
		LoadUnitAreas lua = new LoadUnitAreas(50, UnitAreaType.HUC_NONE, false);
		DataTable dt = lua.run();
		assertEquals(8321, dt.getRowCount());
		int row = dt.findFirst(0, 9388);
		assertEquals((Double) 100.91, dt.getDouble(row, 1));
		
		lua = new LoadUnitAreas(50, UnitAreaType.HUC_NONE, true);
		dt = lua.run();
		assertEquals(8321, dt.getRowCount());
		row = dt.findFirst(0, 9390);
		assertEquals((Double) 1091.98, dt.getDouble(row, 1));

	}
	
}

