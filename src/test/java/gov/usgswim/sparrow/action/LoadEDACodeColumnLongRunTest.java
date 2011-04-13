package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadEDACodeColumn.
 * 
 * @author eeverman
 */

public class LoadEDACodeColumnLongRunTest extends SparrowTestBaseWithDB {
	

	@Test
	public void testModel50() throws Exception {
		
		LoadEDACodeColumn act = new LoadEDACodeColumn(50L);
		ColumnData col = act.run();
		
		assertEquals(56, col.getRowCount().intValue());
		assertEquals("G050w", col.getValue(0));
		assertEquals("G050w", col.getString(0));
		
		assertEquals("S190x", col.getValue(55));
	}
}

