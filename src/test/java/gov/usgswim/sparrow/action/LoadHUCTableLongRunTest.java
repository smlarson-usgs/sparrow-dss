package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.SparrowDBTestBaseClass;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadHUCTable methods.
 * 
 * @author thongsav
 */

public class LoadHUCTableLongRunTest extends SparrowDBTestBaseClass {
	
	/**
	 * Tests the columns.
	 * @throws Exception
	 */
	@Test
	public void testLoadHUCTable() throws Exception {
		
		LoadHUCTable hucs= new LoadHUCTable(50);
		DataTable dt = hucs.run();
		assertEquals(1, dt.getColumnCount());
		
		assertEquals("HUC8", dt.getName(0));
		assertEquals("HUC8", dt.getDescription(0));
		
		assertEquals("03100103", dt.getString(68, 0));
		assertEquals("03130014", dt.getString(96, 0));
		assertEquals("03180004", dt.getString(156, 0));
		assertEquals("03010103", dt.getString(399, 0));
	}
}

