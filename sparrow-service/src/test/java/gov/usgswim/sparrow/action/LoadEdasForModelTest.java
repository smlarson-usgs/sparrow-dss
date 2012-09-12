package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgs.cida.datatable.impl.SparseDoubleColumnData;
import gov.usgs.cida.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.*;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.domain.Adjustment;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
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
public class LoadEdasForModelTest extends SparrowTestBaseWithDB {

	
	static final double COMP_ERROR = .0000001d;
	
	
	@Test
	public void dataSanityCheck() throws Exception {
		
		LoadEdasForModel action = new LoadEdasForModel(TEST_MODEL_ID);

		DataTable result = action.run();
		
		assertNotNull(result);
		assertEquals(2, result.getColumnCount());
		assertEquals(99, result.getRowCount());		//This count may change if the data is cleaned up
		assertFalse(result instanceof DataTableWritable);
	}
	
	@Test
	public void cacheTest() throws Exception {
		

		DataTable result = SharedApplication.getInstance().getEdasForModel(TEST_MODEL_ID);
		
		long startTime = System.currentTimeMillis();
		result = SharedApplication.getInstance().getEdasForModel(TEST_MODEL_ID);
		long endTime = System.currentTimeMillis();
		double totalSeconds = (endTime - startTime) / 1000d;
		
		//Should be instant
		assertTrue(totalSeconds < 1);
		
		//data should be the same
		assertNotNull(result);
		assertEquals(2, result.getColumnCount());
		assertEquals(99, result.getRowCount());	//This count may change if the data is cleaned up
		assertFalse(result instanceof DataTableWritable);
		
	}
	

	
}

