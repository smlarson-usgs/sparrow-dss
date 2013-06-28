package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;

import org.junit.Test;

/**
 * Tests the LoadEDANameColumn action.
 * 
 * @author eeverman
 */

public class LoadEDANameColumnLongRunTest extends SparrowTestBaseWithDB {
	

	@Test
	public void testModel50() throws Exception {
		
		LoadEDANameColumn act = new LoadEDANameColumn(50L);
		ColumnData col = act.run();
		
		assertEquals(43, col.getRowCount().intValue());
		assertEquals("Albemarle Sound", col.getValue(0));
		assertEquals("Albemarle Sound", col.getString(0));
		
		assertEquals("_CDA_S183 (Daytona-St. Augustine)", col.getValue(42));
	}
}

