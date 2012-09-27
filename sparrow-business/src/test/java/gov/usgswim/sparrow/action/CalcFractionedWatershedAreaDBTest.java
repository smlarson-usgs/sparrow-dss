package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.SimpleDataTableWritable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.domain.AggregationLevel;
import static org.junit.Assert.*;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.request.ReachID;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.Map.Entry;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class CalcFractionedWatershedAreaDBTest extends SparrowTestBase {
		
		
	@Test
	public void PerfTestAndValueTest() throws Exception {
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = pd.getTopo();
		int rowCount = topo.getRowCount();
		DataTable incAreaTable = buildFakeAreaTable(rowCount);
		
		ArrayList<Double> uncachedAreas = new ArrayList<Double>(rowCount);
		ArrayList<Double> cachedAreas = new ArrayList<Double>(rowCount);
		
		//Force the areas to be loaded
//		DataTable incrementalReachAreas = 
//				SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(TEST_MODEL_ID, AggregationLevel.REACH, false));

		long startTime = System.currentTimeMillis();
		System.out.println("Starting no-cache area run: " + startTime);
		for (int row = 0; row < rowCount; row++) {
			Long rowId = topo.getIdForRow(row);
			ReachID reachId = new ReachID(TEST_MODEL_ID, rowId);
			
			if (row >= 8281 && row <= 8283) {
				System.out.println("This is a problem row....");
			}
			
			CalcFractionedWatershedArea action = new CalcFractionedWatershedArea(reachId, incAreaTable, false, false, false);
			Double area = action.run();
			uncachedAreas.add(area);
			
			//ConfiguredCache.FractionedWatershedArea
					
			assertNotNull(area);
			assertTrue(area > 0d);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Ending no-cache area run.  Total time: " + (endTime - startTime) + "ms");
		
		
		
		//Do test allowing the cache to be used
		startTime = System.currentTimeMillis();
		System.out.println("Starting cache area run: " + startTime);
		for (int row = 0; row < rowCount; row++) {
			Long rowId = topo.getIdForRow(row);
			ReachID reachId = new ReachID(TEST_MODEL_ID, rowId);
			
			if (row >= 8281 && row <= 8283) {
				System.out.println("This is a problem row: " + row + " id: " + topo.getIdForRow(row));
			}
			
			CalcFractionedWatershedArea action = new CalcFractionedWatershedArea(reachId, incAreaTable, false, false, true);
			Double area = action.run();
			ConfiguredCache.FractionedWatershedArea.put(reachId, area);
			cachedAreas.add(area);
					
			assertNotNull(area);
			assertTrue(area > 0d);
		}
		
		endTime = System.currentTimeMillis();
		System.out.println("Ending cache area run.  Total time: " + (endTime - startTime) + "ms");
		
		
		//Compare results
		for (int row = 0; row < rowCount; row++) {
			boolean equal = isEqual(uncachedAreas.get(row), cachedAreas.get(row), .00001D);
			
			if (! equal) {
				System.out.println("Expect: " + uncachedAreas.get(row) + " Actual: " + cachedAreas.get(row));
				System.out.println("** Row: " + row);
			}
			
			//assertTrue(isEqual(uncachedAreas.get(row), cachedAreas.get(row), .000001D));
		}
		
	}
	
	@Test
	public void compareFracMapsOfProblemReaches() throws Exception {
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = pd.getTopo();
		
		ReachID reach6717 = new ReachID(TEST_MODEL_ID, 6717L);
		ReachID reach6719 = new ReachID(TEST_MODEL_ID, 6719L);
		ReachID reach6716 = new ReachID(TEST_MODEL_ID, 6716L);
		
		CalcReachAreaFractionMap action6717 = new CalcReachAreaFractionMap(reach6717, false);
		CalcReachAreaFractionMap action6719 = new CalcReachAreaFractionMap(reach6719, false);
		CalcReachAreaFractionMap action6716 = new CalcReachAreaFractionMap(reach6716, false);
		
		ReachRowValueMap map6717 = action6717.run();
		ReachRowValueMap map6719 = action6719.run();
		ReachRowValueMap map6716 = action6716.run();
		
		System.out.println("6716 vs 6717: ");
		reportDifferences(topo, map6716, map6717);
		
		assertTrue(map6717.containsKey(new Integer(8280)));
		assertTrue(! map6717.containsKey(new Integer(8281)));
		assertTrue(map6716.containsKey(new Integer(8281)));
		assertTrue(map6716.containsKey(new Integer(8280)));
		
		
	}
	
	public void reportDifferences(ReachRowValueMap a, ReachRowValueMap b) {
		for(Entry<Integer, Float> aEntry: a.entrySet()) {
			
			Integer aRow = aEntry.getKey();
			Float aVal = aEntry.getValue();
		
			Float bVal = b.get(aEntry.getKey());
			
			if (bVal == null) {
				System.out.println("b does not contain the Entry <" + aRow + ", " + aVal + ">");
			} else if (! isEqual(aVal, bVal, .00001D)) {
				System.out.println("b contains a different value for row: " +  aRow + ".  A val: " + aVal+ ", " + "b val: " + bVal);
			} else {
				System.out.println("both  contain the Entry <" + aRow + ", " + aVal + ">");
			}
		}
		
		for(Entry<Integer, Float> bEntry: b.entrySet()) {
			
			Integer bRow = bEntry.getKey();
			Float bVal = bEntry.getValue();
		
			Float aVal = a.get(bEntry.getKey());
			
			if (aVal == null) {
				System.out.println("a does not contain the Entry <" + bRow + ", " + bVal + ">");
			}
		}
	}
	
	public void reportDifferences(DataTable topo, ReachRowValueMap a, ReachRowValueMap b) {
		
		int rowCount = topo.getRowCount();
		
		for (int row = 0; row < rowCount; row++) {
			Float aVal = a.get(row);
			Float bVal = b.get(row);
			
			if (aVal != null && bVal != null) {
				if (isEqual(aVal, bVal, .00001D)) {
					System.out.println("both  contain the Entry <" + row + ", " + aVal + ">");
				} else {
					System.out.println("b has a different value for row: " +  row + ".  A val: " + aVal+ ", " + "b val: " + bVal + "( id: " + topo.getIdForRow(row) + ")");
				}
			} else if (aVal != null) {
				System.out.println("b missing the Entry <" + row + ", " + aVal + ">");
			} else if (bVal != null) {
				System.out.println("b missing the Entry <" + row + ", " + bVal + ">");
			} else {
				//both null
				
			}

		}
			
//			
//		for(Entry<Integer, Float> aEntry: a.entrySet()) {
//			
//			Integer aRow = aEntry.getKey();
//			Float aVal = aEntry.getValue();
//		
//			Float bVal = b.get(aEntry.getKey());
//			
//			if (bVal == null) {
//				System.out.println("b does not contain the Entry <" + aRow + ", " + aVal + ">");
//			} else if (! isEqual(aVal, bVal, .00001D)) {
//				System.out.println("b contains a different value for row: " +  aRow + ".  A val: " + aVal+ ", " + "b val: " + bVal);
//			} else {
//				System.out.println("both  contain the Entry <" + aRow + ", " + aVal + ">");
//			}
//		}
//		
//		for(Entry<Integer, Float> bEntry: b.entrySet()) {
//			
//			Integer bRow = bEntry.getKey();
//			Float bVal = bEntry.getValue();
//		
//			Float aVal = a.get(bEntry.getKey());
//			
//			if (aVal == null) {
//				System.out.println("a does not contain the Entry <" + bRow + ", " + bVal + ">");
//			}
//		}
	}
	
	protected DataTable buildFakeAreaTable(int numberOfRows) {
		double[][] data = new double[numberOfRows][];
		
		for (int r = 0; r < numberOfRows; r++) {
			data[r] = new double[] {(double)r, 1d};
		}
		
		SimpleDataTableWritable table = new SimpleDataTableWritable(data, new String[] {"Fake ID (row)", "Incremental Area"});
		return table.toImmutable();
	}
	
	//@Test
	public void PerfTestWithTheCache() throws Exception {
		PredictData pd = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = pd.getTopo();
		
		//Force the areas to be loaded
		DataTable incrementalReachAreas = 
				SharedApplication.getInstance().getCatchmentAreas(new UnitAreaRequest(TEST_MODEL_ID, AggregationLevel.REACH, false));

		long startTime = System.currentTimeMillis();
		System.out.println("Starting area run: " + startTime);
		for (int row = 0; row < topo.getRowCount(); row++) {
			Long rowId = topo.getIdForRow(row);
			ReachID reachId = new ReachID(TEST_MODEL_ID, rowId);
			
			Double area = SharedApplication.getInstance().getFractionedWatershedArea(reachId);

					
			assertNotNull(area);
			assertTrue(area > 0d);
		}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Ending area run.  Total time: " + (endTime - startTime) + "ms");
		
	}
}
