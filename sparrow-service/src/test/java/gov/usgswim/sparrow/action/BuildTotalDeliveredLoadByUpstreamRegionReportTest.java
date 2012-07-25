package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.utils.DataTablePrinter;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Level;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author eeverman
 */
public class BuildTotalDeliveredLoadByUpstreamRegionReportTest extends DeliveryBase {

	
	static final double COMP_ERROR = .0000001d;
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
		super.doOneTimeCustomSetup();
	}
	
	@Test
	public void dataSanityCheckForStates() throws Exception {
		
		//Switches the predict data in the cache to the modified copy, which
		//has transport turned off above reach 9681.
		switchToModifiedPredictData();
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, target9682, AggregationLevel.STATE);
		
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		
		DataTable result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The State Delivery Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(10, result.getColumnCount());
		
		boolean containsSomeNonZeroData = false;
		
		//All the first columns should total to the last column
		for (int r=0; r < result.getRowCount(); r++) {
			double srcTotal = 0;
			for (int c=3; c<8; c++) {
				srcTotal += result.getDouble(r, c);
			}
			
			if (srcTotal > .0000000001d) containsSomeNonZeroData = true;
			
			//SUM of first 5 columns should equal the last column
			assertEquals(result.getDouble(r, 8), srcTotal, COMP_ERROR);
			
		}
		
		assertTrue(containsSomeNonZeroData);
		
	}
	
	@Test
	public void dataSanityCheckForHUC2() throws Exception {
		
		//Switches the predict data in the cache to the modified copy, which
		//has transport turned off above reach 9681.
		switchToModifiedPredictData();
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, target9682, AggregationLevel.HUC2);
		
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		
		DataTable result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The HUC2 Delivery Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(10, result.getColumnCount());
		
		boolean containsSomeNonZeroData = false;
		
		//All the first columns should total to the last column
		for (int r=0; r < result.getRowCount(); r++) {
			double srcTotal = 0;
			for (int c=3; c<8; c++) {
				srcTotal += result.getDouble(r, c);
			}
			
			if (srcTotal > .0000000001d) containsSomeNonZeroData = true;
			
			//SUM of first 5 columns should equal the last column
			assertEquals(result.getDouble(r, 8), srcTotal, COMP_ERROR);
			
		}
		
		assertTrue(containsSomeNonZeroData);
		
	}
	
	
	@Test
	public void compareAltPathCalculationForSmallHuc03100203Containing5Reaches() throws Exception {

		//These are all the reaches contained in the HUC8 03100203
		long[] reachesInHuc = new long[] {7571L, 7572L, 7573L, 657950L,  657960L};
		
		double area_7571 = 166.86d;
		double area_7572 = 102.90d;
		double area_7573 = 155.53d;
		double area_657950 = 136.69d;
		double area_657960 = 41.26d;
		double total_area = area_7571 + area_7572 + area_7573 + area_657950 + area_657960;
		
		
		//Create a terminalReach list containing the reach they all drain to:  7571
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(7571L);
		TerminalReaches termReaches = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		//No adjustment adjustment group
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		
		//Calc the HUC8 Aggregated result
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.HUC8);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		DataTable aggByHUC8Result = action.run();
		
		//Find the total value for HUC8 03100203
		int huc8RowNumber = aggByHUC8Result.findFirst(1, "03100203");
		double huc8Total = aggByHUC8Result.getDouble(huc8RowNumber, aggByHUC8Result.getColumnCount() - 2);
		double huc8AreaTotal = aggByHUC8Result.getDouble(huc8RowNumber, 2);
		
		//Calc the Delivery Fraction Hash
		CalcDeliveryFractionMap delFracAction = new CalcDeliveryFractionMap();
		delFracAction.setPredictData(SharedApplication.getInstance().getPredictData(TEST_MODEL_ID));
		delFracAction.setTargetReachIds(termReaches.asSet());
		DeliveryFractionMap delFracHashMap = delFracAction.run();
		
		//Calc Incremental Load
		PredictResult predictResult = SharedApplication.getInstance().getPredictResult(adjustmentGroups);
		//predictResult.getDecayedIncremental(row)
		
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(aggByHUC8Result, "The HUC8 Delivery Summary");
//		System.out.println("Table Dumped");
//
//		System.out.println("|| Reach Id || Inc Load (Decayed) || Del Frac || Inc Load * Del Frac || Running Total ||");
		double altCalcTotal = 0d;
		for (int i = 0; i < reachesInHuc.length; i++) {
			long reachId = reachesInHuc[i];
			double incLoad = predictResult.getDecayedIncremental(predictResult.getRowForId(reachId));
			double delFrac = delFracHashMap.getFraction(predictResult.getRowForId(reachId));
			double incDelLoad = incLoad * delFrac;
			altCalcTotal += incDelLoad;
			
//			System.out.println("| " + reachId + " | " + incLoad + " | " + delFrac + " | " + incDelLoad + " | " + altCalcTotal + " | ");
		}
		
		assertEquals(altCalcTotal, huc8Total, 1d);
		assertEquals(total_area, huc8AreaTotal, .01d);
			
	}
	
	
	/**
	 * Anne Hoose sent us the following scenario for model 50:
	 * Specifying these reaches as target reaches:
	 * 6194, 6187, 81045, 81046
	 * The load delivered from HUC 03050105 should be 912,050 kg/yr
	 * 
	 * EE 7/24/2012:  I think Anne typoed that - it should be 912,040.  I have
	 * an email in to her about that.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void investigateScenarioToSeeIfAnnesDataMatchesOurs() throws Exception {

		//These are the term reaches from Anne's example calc
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(6194L);
		targetList.add(6187L);
		targetList.add(81045L);
		targetList.add(81046L);
		TerminalReaches termReaches = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		//Calc all the reaches in the HUC8 03050105
		long[] reachesInHuc = SharedApplication.getInstance().getReachesByCriteria(new Criteria(TEST_MODEL_ID, CriteriaType.HUC8, CriteriaRelationType.IN, "03050105"));
		
		//Calc the HUC8 Aggregated result
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.HUC8);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		DataTable aggByHUC8Result = action.run();
		
		//Find the total value for HUC8 03050105
		int huc8RowNumber = aggByHUC8Result.findFirst(1, "03050105");
		double huc8Total = aggByHUC8Result.getDouble(huc8RowNumber, aggByHUC8Result.getColumnCount() - 2);
		
		
		//Calc the Delivery Fraction Hash
		CalcDeliveryFractionMap delFracAction = new CalcDeliveryFractionMap();
		delFracAction.setPredictData(SharedApplication.getInstance().getPredictData(TEST_MODEL_ID));
		delFracAction.setTargetReachIds(termReaches.asSet());
		DeliveryFractionMap delFracHashMap = delFracAction.run();
		
		//Calc Incremental Load
		PredictResult predictResult = SharedApplication.getInstance().getPredictResult(adjustmentGroups);
		//predictResult.getDecayedIncremental(row)
		
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(aggByHUC8Result, "The HUC8 Delivery Summary");
//		System.out.println("Table Dumped");

		//System.out.println("|| Reach Id || Inc Load (Decayed) || Del Frac || Inc Load * Del Frac || Running Total ||");
		double altCalcTotal = 0d;
		for (int i = 0; i < reachesInHuc.length; i++) {
			long reachId = reachesInHuc[i];
			double incLoad = predictResult.getDecayedIncremental(predictResult.getRowForId(reachId));
			double delFrac = delFracHashMap.getFraction(predictResult.getRowForId(reachId));
			double incDelLoad = incLoad * delFrac;
			altCalcTotal += incDelLoad;
			
			//System.out.println("| " + reachId + " | " + incLoad + " | " + delFrac + " | " + incDelLoad + " | " + altCalcTotal + " | ");
		}
		
		//System.out.println("Load originating in HUC8 03050105 as reported by Report Action: " + huc8Total);
		
		//TODO: Anne says the number is 912050...
		assertEquals(912040D, huc8Total, .5d);
			
	}
	
	
}

