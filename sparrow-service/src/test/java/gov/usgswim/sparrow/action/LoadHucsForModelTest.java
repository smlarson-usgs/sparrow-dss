package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.impl.SparseDoubleColumnData;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.sparrow.*;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.domain.Adjustment;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.junit.Test;

/**
 * @author eeverman
 */
public class LoadHucsForModelTest extends SparrowTestBaseWithDB {

	
	static final double COMP_ERROR = .0000001d;
	
	
	@Test
	public void dataSanityCheckForHUC2() throws Exception {
		
		LoadHucsForModel action = new LoadHucsForModel(TEST_MODEL_ID, HucLevel.HUC2);

		DataTable result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The HUC2s for model 50 Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(2, result.getColumnCount());
		assertEquals(2, result.getRowCount());
		assertFalse(result instanceof DataTableWritable);
		
		//Make sure indexing is set correctly
		assertTrue(result.hasRowIds());
		assertFalse(result.isIndexed(0));
		assertFalse(result.isIndexed(1));
		
		assertEquals(0, result.findFirst(1, "03"));
		assertEquals(1, result.findFirst(1, "06"));
		
		
		//Only four values to check - check 'em all
		assertEquals("03", result.getString(0, 1));
		assertEquals("SOUTH ATLANTIC-GULF", result.getString(0, 0));
		assertEquals("06", result.getString(1, 1));
		assertEquals("TENNESSEE", result.getString(1, 0));
	}
	
	@Test
	public void dataSanityCheckForHUC4() throws Exception {
		
		LoadHucsForModel action = new LoadHucsForModel(TEST_MODEL_ID, HucLevel.HUC4);

		DataTable result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The HUC4s for model 50 Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(2, result.getColumnCount());
		assertEquals(21, result.getRowCount());
		assertFalse(result instanceof DataTableWritable);
		assertTrue(result.hasRowIds());
		
		
		//Check first and last row
		assertEquals("0301", result.getString(0, 1));
		assertEquals("CHOWAN-ROANOKE", result.getString(0, 0));
		assertEquals("0604", result.getString(20, 1));
		assertEquals("LOWER TENNESSEE", result.getString(20, 0));
	}
	
	@Test
	public void cacheTest() throws Exception {
		

		ModelHucsRequest req = new ModelHucsRequest(TEST_MODEL_ID, HucLevel.HUC4);
		DataTable result = SharedApplication.getInstance().getHucsForModel(req);
		
		long startTime = System.currentTimeMillis();
		result = SharedApplication.getInstance().getHucsForModel(req);
		long endTime = System.currentTimeMillis();
		double totalSeconds = (endTime - startTime) / 1000d;
		
		//Should be instant
		assertTrue(totalSeconds < .5d);
		
		//data should be the same
		assertNotNull(result);
		assertEquals(2, result.getColumnCount());
		assertEquals(21, result.getRowCount());
		assertFalse(result instanceof DataTableWritable);
		assertTrue(result.hasRowIds());
		
		
		//Check first and last row
		assertEquals("0301", result.getString(0, 1));
		assertEquals("CHOWAN-ROANOKE", result.getString(0, 0));
		assertEquals("0604", result.getString(20, 1));
		assertEquals("LOWER TENNESSEE", result.getString(20, 0));
		
	}
	

	
}

