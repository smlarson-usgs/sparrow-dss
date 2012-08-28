package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.domain.AggregationLevel;
import static org.junit.Assert.*;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.List;


import org.junit.Test;

/**
 * 
 * @author eeverman
 */
public class CalcFractionedWatershedAreaTableTest extends SparrowTestBaseWithDBandCannedModel50 {
		
	
	
	@Test
	public void testTargetWith4UpstreamReaches() throws Exception {
		Long TEST_ROW_ID = 9687L;
		
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(TEST_ROW_ID);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		ConfiguredCache.TerminalReaches.put(targets.getId(), targets);
		
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		Double watershedArea = new CalcFractionedWatershedArea(new ReachID(TEST_MODEL_ID, TEST_ROW_ID)).run();
		int rowCount = pd.getTopo().getRowCount();
		int rowNumber = pd.getRowForReachID(TEST_ROW_ID);
		
		CalcFractionedWatershedAreaTable action = new CalcFractionedWatershedAreaTable(targets.getId());
		ColumnData data = action.run();
		
		assertEquals(rowCount, data.getRowCount().intValue());
		assertNotNull(data.getDouble(rowNumber));
		
		for (int row = 0; row < rowCount; row++) {
			if (row == rowNumber) {
				assertEquals("Row " + row + " should be " + watershedArea, watershedArea, data.getDouble(row), .000000001d);
			} else {
				assertNull("Row " + row + " should be null", data.getDouble(row));
			}
		}
		

	}
	
	@Test
	public void test9680_WhichHasAOneThousanthFraction() throws Exception {
		Long TEST_REACH_ID = 9680L;
		Long ONLY_REACH_UPSTREAM_OF_TEST_REACH_ID = 9681L;
		
		//Create terminal reaches and put in cache
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(TEST_REACH_ID);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		ConfiguredCache.TerminalReaches.put(targets.getId(), targets);
		
		//Load predict data and model incremental areas - used for comparison
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable incrementalReachAreas = SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(TEST_MODEL_ID, AggregationLevel.REACH, false));
		
		//Stats on the test reach
		int testReachRowNumber = pd.getRowForReachID(TEST_REACH_ID);
		Double testReachFrac = pd.getTopo().getDouble(testReachRowNumber, PredictData.TOPO_FRAC_COL);
		Double testReachFractionedWatershedArea = new CalcFractionedWatershedArea(new ReachID(TEST_MODEL_ID, TEST_REACH_ID)).run();
		Double testReachUnfractionedWatershedArea = new CalcFractionedWatershedArea(new ReachID(TEST_MODEL_ID, TEST_REACH_ID), true).run();
		Double testReachIncrementalArea = incrementalReachAreas.getDouble(testReachRowNumber, 1);
		
		//load stats on the only reach immediately upstream of the test reach
		Double upstreamReachFractionedWatershedArea = new CalcFractionedWatershedArea(new ReachID(TEST_MODEL_ID, ONLY_REACH_UPSTREAM_OF_TEST_REACH_ID), false).run();
		
		
		//Test the assumptions about the basic (non-table form) of the area data
		assertTrue(Math.abs(testReachFractionedWatershedArea - testReachUnfractionedWatershedArea) > 1d); //These should be different values
		assertTrue(Math.abs(testReachFractionedWatershedArea - testReachIncrementalArea) > 1d);
		assertEquals(testReachFrac * upstreamReachFractionedWatershedArea + testReachIncrementalArea, testReachFractionedWatershedArea, .0001d);
		
		
		//Do the same calculation via the Table version of the action
		CalcFractionedWatershedAreaTable action = new CalcFractionedWatershedAreaTable(targets.getId());
		ColumnData data = action.run();
		
		assertEquals(testReachFractionedWatershedArea, data.getDouble(testReachRowNumber), .0001d);

	}
	
}
