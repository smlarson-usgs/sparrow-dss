package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.SparrowTestBaseWithDB;

import org.junit.Test;

/**
 * Tests the gov.usgswim.sparrow.action.LoadHUC8Table methods.
 * 
 * @author thongsav
 */

public class LoadHUC8TableLongRunTest extends SparrowTestBaseWithDB {
	
	/**
	 * Tests the columns.
	 * @throws Exception
	 */
	@Test
	public void testLoadHUC8Table() throws Exception {
		
		LoadHUC8Table hucs= new LoadHUC8Table(50);
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

