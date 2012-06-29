package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.sparrow.*;
import gov.usgswim.sparrow.service.SharedApplication;

import org.junit.Test;

/**
 * @author eeverman
 */
public class LoadPredefinedWatershedsForModelTest extends SparrowTestBaseWithDB {

	
	@Test
	public void dataSanityCheck() throws Exception {
		
		LoadPredefinedWatershedsForModel action = new LoadPredefinedWatershedsForModel(TEST_MODEL_ID);

		DataTable result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The HUC2s for model 50 Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(3, result.getColumnCount());
		assertEquals("Name", result.getName(0));
		assertEquals("Description", result.getName(1));
		assertEquals("Count", result.getName(2));
		
		assertEquals(16, result.getRowCount());
		assertFalse(result instanceof DataTableWritable);
		
		//Make sure indexing is set correctly
		assertTrue(result.hasRowIds());
		assertFalse(result.isIndexed(0));
		assertFalse(result.isIndexed(1));
		assertFalse(result.isIndexed(2));
		
		//Check a few values
		assertEquals(new Long(325), result.getIdForRow(0));
		assertEquals("Altamaha River Watershed", result.getString(0, 0));
		
		assertEquals(new Long(335), result.getIdForRow(15));
		assertEquals("Tennessee River Watershed", result.getString(15, 0));
		
	}
	
	@Test
	public void cacheTest() throws Exception {
		

		DataTable result = SharedApplication.getInstance().getPredefinedWatershedsForModel(TEST_MODEL_ID);
		
		long startTime = System.currentTimeMillis();
		result = SharedApplication.getInstance().getPredefinedWatershedsForModel(TEST_MODEL_ID);
		long endTime = System.currentTimeMillis();
		double totalSeconds = (endTime - startTime) / 1000d;
		
		//Should be instant
		assertTrue(totalSeconds < .2d);
		
		//data should be the same
		assertNotNull(result);
		assertEquals(16, result.getRowCount());
		assertFalse(result instanceof DataTableWritable);
		
		//Check a few values
		assertEquals(new Long(325), result.getIdForRow(0));
		assertEquals("Altamaha River Watershed", result.getString(0, 0));
		
		assertEquals(new Long(335), result.getIdForRow(15));
		assertEquals("Tennessee River Watershed", result.getString(15, 0));
		
	}
	
}

