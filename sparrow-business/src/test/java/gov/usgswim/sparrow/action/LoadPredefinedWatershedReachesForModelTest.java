package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgs.cida.datatable.impl.SparseDoubleColumnData;
import gov.usgs.cida.datatable.impl.StandardDoubleColumnData;
import gov.usgs.cida.datatable.utils.DataTablePrinter;
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
public class LoadPredefinedWatershedReachesForModelTest extends SparrowTestBaseWithDB {

	
	@Test
	public void dataSanityCheck() throws Exception {
		
		LoadPredefinedWatershedReachesForModel action = new LoadPredefinedWatershedReachesForModel(335L);

		DataTable result = action.run();
		
		assertNotNull(result);
		assertEquals(1, result.getColumnCount());
		assertEquals("Name", result.getName(0));
		
		assertEquals(1, result.getRowCount());
		assertFalse(result instanceof DataTableWritable);
		
		//Make sure indexing is set correctly
		assertTrue(result.hasRowIds());
		assertFalse(result.isIndexed(0));
		
		//Check a few values
		assertEquals(new Long(18082), result.getIdForRow(0));
		assertEquals("TENNESSEE R", result.getString(0, 0));
	}
	
	@Test
	public void cacheTest() throws Exception {
		

		DataTable result = SharedApplication.getInstance().getPredefinedWatershedReachesForModel(335L);
		
		long startTime = System.currentTimeMillis();
		result = SharedApplication.getInstance().getPredefinedWatershedReachesForModel(335L);
		long endTime = System.currentTimeMillis();
		double totalSeconds = (endTime - startTime) / 1000d;
		
		//Should be instant
		assertTrue(totalSeconds < .2d);
		
		//data should be the same
		assertNotNull(result);
		assertEquals(1, result.getColumnCount());
		assertEquals("Name", result.getName(0));
		
		assertEquals(1, result.getRowCount());
		assertFalse(result instanceof DataTableWritable);
		
		//Make sure indexing is set correctly
		assertTrue(result.hasRowIds());
		assertFalse(result.isIndexed(0));
		
		//Check a few values
		assertEquals(new Long(18082), result.getIdForRow(0));
		assertEquals("TENNESSEE R", result.getString(0, 0));
		
	}
	
}

