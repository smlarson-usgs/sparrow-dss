package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableSet;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.domain.reacharearelation.ReachAreaRelations;
import gov.usgswim.sparrow.request.DeliveryReportRequest;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
import gov.usgswim.sparrow.request.ModelHucsRequest;
import gov.usgswim.sparrow.service.SharedApplication;
import java.util.ArrayList;
import java.util.List;
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
		
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, target9682, AggregationLevel.STATE, false);
		
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		
		DataTableSet result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The State Delivery Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(2, result.getTableCount());
		
		
		//The 1st table contains ID info (state name, area, etc)
		//The 2nd table contains the source and total data
		DataTable infoDataTable = result.getTable(0);
		DataTable resultDataTable = result.getTable(1);
		
		assertEquals(3, infoDataTable.getColumnCount());
		assertEquals("Watershed Area", infoDataTable.getName(2));
		assertEquals(SparrowUnits.SQR_KM.toString(), infoDataTable.getUnits(2));
		assertEquals(6, resultDataTable.getColumnCount());
		
		
		boolean containsSomeNonZeroData = false;
		
		//All the first columns should total to the last column
		for (int r=0; r < resultDataTable.getRowCount(); r++) {
			double srcTotal = 0;
			for (int c=0; c<resultDataTable.getColumnCount() - 1; c++) {
				srcTotal += resultDataTable.getDouble(r, c);
			}
			
			if (srcTotal > .0000000001d) containsSomeNonZeroData = true;
			
			//SUM of first 5 columns should equal the last column
			assertEquals(resultDataTable.getDouble(r, resultDataTable.getColumnCount() - 1), srcTotal, COMP_ERROR);
			
		}
		
		assertTrue(containsSomeNonZeroData);
		
	}
	
	@Test
	public void dataSanityCheckForHUC2() throws Exception {
		
		//Switches the predict data in the cache to the modified copy, which
		//has transport turned off above reach 9681.
		switchToModifiedPredictData();
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, target9682, AggregationLevel.HUC2, false);
		
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		
		DataTableSet result = action.run();
		
//		System.out.println("Dumping Table");
//		DataTablePrinter.printDataTable(result, "The HUC2 Delivery Summary");
//		System.out.println("Table Dumped");
		
		assertNotNull(result);
		assertEquals(2, result.getTableCount());
		
		
		//The 1st table contains ID info (state name, area, etc)
		//The 2nd table contains the source and total data
		DataTable infoDataTable = result.getTable(0);
		DataTable resultDataTable = result.getTable(1);
		
		assertEquals(3, infoDataTable.getColumnCount());
		assertEquals(SparrowUnits.SQR_KM.toString(), infoDataTable.getUnits(2));
		assertEquals(6, resultDataTable.getColumnCount());
		
		
		boolean containsSomeNonZeroData = false;
		
		//All the first columns should total to the last column
		for (int r=0; r < resultDataTable.getRowCount(); r++) {
			double srcTotal = 0;
			for (int c=0; c<resultDataTable.getColumnCount() - 1; c++) {
				srcTotal += resultDataTable.getDouble(r, c);
			}
			
			if (srcTotal > .0000000001d) containsSomeNonZeroData = true;
			
			//SUM of first 5 columns should equal the last column
			assertEquals(resultDataTable.getDouble(r, resultDataTable.getColumnCount() - 1), srcTotal, COMP_ERROR);
			
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
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.HUC8, false);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		
		DataTableSet aggByHUC8Result = action.run();
		
		//The 1st table contains ID info (state name, area, etc)
		//The 2nd table contains the source and total data
		DataTable infoDataTable = aggByHUC8Result.getTable(0);
		DataTable resultDataTable = aggByHUC8Result.getTable(1);
		
		//Find the total value for HUC8 03100203
		int huc8RowNumber = infoDataTable.findFirst(1, "03100203");
		double huc8Total = resultDataTable.getDouble(huc8RowNumber, resultDataTable.getColumnCount() - 1);
		double huc8AreaTotal = infoDataTable.getDouble(huc8RowNumber, 2);
		
		//Calc the Delivery Fraction Hash
		CalcDeliveryFractionMap delFracAction = new CalcDeliveryFractionMap();
		delFracAction.setPredictData(SharedApplication.getInstance().getPredictData(TEST_MODEL_ID));
		delFracAction.setTargetReachIds(termReaches.asSet());
		ReachRowValueMap delFracHashMap = delFracAction.run();
		
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
	 * Anne Hoos sent us the following scenario for model 50:
	 * Specifying these reaches as target reaches:
	 * 6194, 6187, 81045, 81046
	 * The load delivered from HUC 03050105 should be 912,040 kg/yr
	 * 
	 * Anne Hoos: abhoos@usgs.gov, 615) 837-4760
	 * Sparrow Modeler for the Florida model.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void investigateScenarioToSeeIfAnneHoosDataMatchesOurs() throws Exception {

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
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.HUC8, false);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		DataTableSet aggByHUC8Result = action.run();
		
		//Find the total value for HUC8 03050105
		//The 1st table contains ID info (state name, area, etc)
		//The 2nd table contains the source and total data
		DataTable infoDataTable = aggByHUC8Result.getTable(0);
		DataTable resultDataTable = aggByHUC8Result.getTable(1);
		
		//Find the total value for HUC8 03050105
		int huc8RowNumber = infoDataTable.findFirst(1, "03050105");
		double huc8Total = resultDataTable.getDouble(huc8RowNumber, resultDataTable.getColumnCount() - 1);
		
		
		//Calc the Delivery Fraction Hash
		CalcDeliveryFractionMap delFracAction = new CalcDeliveryFractionMap();
		delFracAction.setPredictData(SharedApplication.getInstance().getPredictData(TEST_MODEL_ID));
		delFracAction.setTargetReachIds(termReaches.asSet());
		ReachRowValueMap delFracHashMap = delFracAction.run();
		
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
	
	@Test
	public void cumulativeAreaShouldBeTheSameAsCatchAreaForShoreReach() throws Exception {

		Long TEST_REACH_ID = 81045L;
		
		
		//These are the term reaches from Anne's example calc
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(TEST_REACH_ID);
		int testReachRow = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID).getRowForReachID(TEST_REACH_ID);
		TerminalReaches termReaches = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		//Calc the HUC2 Aggregated result
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.HUC2, false);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		DataTableSet aggByHUC2Result = action.run();
		
		//Get the catchment and watershed area of the test reach
		LoadReachAttributes reachAttribAction = new LoadReachAttributes(TEST_MODEL_ID, TEST_REACH_ID);
		DataTable reachAttrib = reachAttribAction.run();
		Double reachDbCatchArea = reachAttrib.getDouble(0, 11);
		Double reachDbWatershedArea = reachAttrib.getDouble(0, 12);
		
		//Find the total value for HUC2
		//The 1st table contains ID info (state name, area, etc)
		//The 2nd table contains the source and total data
		DataTable infoDataTable = aggByHUC2Result.getTable(0);
		//DataTable resultDataTable = aggByHUC2Result.getTable(1);
		
		//Find the total value for HUC2 03
		int huc2RowNumber = infoDataTable.findFirst(1, "03");
		double huc2AggArea = infoDataTable.getDouble(huc2RowNumber, 2);
		
		
		assertEquals(reachDbCatchArea, reachDbWatershedArea, .00000001D);
		assertEquals(reachDbCatchArea, huc2AggArea, .00000001D);
			
	}

	@Test
	public void cumulativeAreaCheckForReachWith4ReachesUpstream() throws Exception {

		Long TEST_REACH_ID = 7571L;
		
		
		//These are the term reaches from Anne's example calc
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(TEST_REACH_ID);
		int testReachRow = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID).getRowForReachID(TEST_REACH_ID);
		TerminalReaches termReaches = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		//Calc the HUC2 Aggregated result
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.HUC2, false);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		DataTableSet aggByHUC2Result = action.run();
		
		//Get the catchment and watershed area of the test reach
		LoadReachAttributes reachAttribAction = new LoadReachAttributes(TEST_MODEL_ID, TEST_REACH_ID);
		DataTable reachAttrib = reachAttribAction.run();
		Double reachDbWatershedArea = reachAttrib.getDouble(0, 12);
		
		//Find the total value for HUC2
		//The 1st table contains ID info (state name, area, etc)
		//The 2nd table contains the source and total data
		DataTable infoDataTable = aggByHUC2Result.getTable(0);
		//DataTable resultDataTable = aggByHUC2Result.getTable(1);
		
		//Find the total value for HUC2 03
		int huc2RowNumber = infoDataTable.findFirst(1, "03");
		double huc2AggArea = infoDataTable.getDouble(huc2RowNumber, 2);
		
		
		assertEquals(reachDbWatershedArea, huc2AggArea, .0001D);
			
	}
	
	@Test
	public void cumulativeAreaCheckForReach16960() throws Exception {

		Long TEST_REACH_ID = 16960L;
		
		
		//These are the term reaches from Anne's example calc
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(TEST_REACH_ID);
		int testReachRow = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID).getRowForReachID(TEST_REACH_ID);
		TerminalReaches termReaches = new TerminalReaches(TEST_MODEL_ID, targetList);
		ModelReachAreaRelations reachToHuc2Relation = SharedApplication.getInstance().getModelReachAreaRelations(new ModelAggregationRequest(TEST_MODEL_ID, AggregationLevel.HUC2));
		ReachAreaRelations huc2sForReach = reachToHuc2Relation.getRelationsForReachRow(testReachRow);
		DataTable regionDetail = SharedApplication.getInstance().getHucsForModel(new ModelHucsRequest(TEST_MODEL_ID, HucLevel.HUC2));
		
		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		//Calc the HUC2 Aggregated result
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.HUC2, false);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		DataTableSet aggByHUC2Result = action.run();
		
		//Get the catchment and watershed area of the test reach
		LoadReachAttributes reachAttribAction = new LoadReachAttributes(TEST_MODEL_ID, TEST_REACH_ID);
		DataTable reachAttrib = reachAttribAction.run();
		Double reachDbWatershedArea = reachAttrib.getDouble(0, 12);
		
		//Find the total value for HUC2
		//The 1st table contains ID info (state name, area, etc)
		//The 2nd table contains the source and total data
		DataTable infoDataTable = aggByHUC2Result.getTable(0);
		//DataTable resultDataTable = aggByHUC2Result.getTable(1);
		
		

		//Find the total value 
		Long regionId = huc2sForReach.getRelations().get(0).getAreaId();
		int regionRow = regionDetail.getRowForId(regionId);	
		double huc2AggArea = infoDataTable.getDouble(regionRow, 2);
		
		
		assertEquals(1, huc2sForReach.getRelations().size());	//reach should be in on ly 1 huc2
		assertEquals(reachDbWatershedArea, huc2AggArea, .0001D);
			
	}
	
	/**
	 * The info table always has one reach for each region in the model (states in this case).
	 * The data table (values for each area) should have the same number of rows.
	 * Later these rows are filtered to remove the zero value rows.
	 * 
	 * The issue here was that, since the data table is build dynamically, its
	 * columns do not add values beyond where values were written to.  For instance,
	 * if the last state is West Virginia and no value is written for that state
	 * (ie, there are no reaches upstream of the terminal reaches in W. Virginia),
	 * then the table rows will not extend to the last row of the table.
	 * 
	 * @throws Exception 
	 */
	@Test
	public void infoTableAndDataTableShouldHaveSameNumberOfRows() throws Exception {

		Long TEST_REACH_ID = 5573L;
		
		//These are the term reaches from Anne's example calc
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(TEST_REACH_ID);
		TerminalReaches termReaches = new TerminalReaches(TEST_MODEL_ID, targetList);

		AdjustmentGroups adjustmentGroups = new AdjustmentGroups(SparrowTestBase.TEST_MODEL_ID);
		
		//Calc the HUC2 Aggregated result
		DeliveryReportRequest req = new DeliveryReportRequest(adjustmentGroups, termReaches, AggregationLevel.STATE, false);
		BuildTotalDeliveredLoadByUpstreamRegionReport action =
				new BuildTotalDeliveredLoadByUpstreamRegionReport(req);
		DataTableSet aggResult = action.run();
		
		
		DataTable infoDataTable = aggResult.getTable(0);
		DataTable resultDataTable = aggResult.getTable(1);
		
		assertEquals(infoDataTable.getRowCount(), resultDataTable.getRowCount());
		assertEquals(11, infoDataTable.getRowCount());
		assertEquals(11, resultDataTable.getRowCount());
		


			
	}
	
	
}

