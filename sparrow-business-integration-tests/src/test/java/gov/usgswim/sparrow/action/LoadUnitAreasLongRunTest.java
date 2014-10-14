package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.AreaType;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;

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
		lua.setAreaType(AreaType.INCREMENTAL);
		assertEquals(lua.getAreaType(), AreaType.INCREMENTAL);
		lua.setAreaType(AreaType.TOTAL_CONTRIBUTING);
		assertEquals(lua.getAreaType(), AreaType.TOTAL_CONTRIBUTING);
		lua.setAreaType(AreaType.TOTAL_UPSTREAM);
		assertEquals(lua.getAreaType(), AreaType.TOTAL_UPSTREAM);

	}

	/**
	 * Tests the columns.
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasColumns() throws Exception {
		Long mockModelId = 50L;
		LoadUnitAreas lua;
		for(AreaType areaType : AreaType.values()){
			lua = new LoadUnitAreas(mockModelId, areaType);
			DataTable dt = lua.run();
			assertEquals(2, dt.getColumnCount());
			assertEquals(null, dt.getUnits(0));
			assertEquals("IDENTIFIER", dt.getName(0));

			assertEquals(SparrowUnits.SQR_KM.toString(), dt.getUnits(1));
			assertEquals(areaType.getName(), dt.getName(1));
			assertEquals(areaType.getDescription(), dt.getDescription(1));
			assertEquals(areaType.name(), dt.getProperty(1, TableProperties.DATA_SERIES.toString()));
			assertEquals("land area", dt.getProperty(1, TableProperties.CONSTITUENT.toString()));
		}
	}

	/**
	 * Tests the data.
	 * @throws Exception
	 */
	@Test
	public void testLoadUnitAreasData() throws Exception {

		LoadUnitAreas lua = new LoadUnitAreas(50, AreaType.INCREMENTAL);
		DataTable dt = lua.run();
		assertEquals(8321, dt.getRowCount());
		int row = dt.findFirst(0, 9388L);
		assertEquals((Double) 100.91, dt.getDouble(row, 1));

		lua = new LoadUnitAreas(50, AreaType.TOTAL_CONTRIBUTING);
		dt = lua.run();
		assertEquals(8321, dt.getRowCount());
		row = dt.findFirst(0, 9390L);
		assertEquals((Double) 1091.98, dt.getDouble(row, 1));

	}

}

