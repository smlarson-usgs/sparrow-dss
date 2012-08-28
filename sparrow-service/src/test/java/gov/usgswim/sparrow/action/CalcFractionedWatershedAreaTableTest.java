package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import static org.junit.Assert.*;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.request.ReachID;
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
	
}

