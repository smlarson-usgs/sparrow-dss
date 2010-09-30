package gov.usgswim.sparrow.action;

import static gov.usgswim.sparrow.service.ConfiguredCache.DeliveryFraction;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredictData;
import static gov.usgswim.sparrow.service.ConfiguredCache.PredictResult;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.AreaOfInterest;
import gov.usgswim.sparrow.parser.BasicAnalysis;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.NominalComparison;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.junit.Test;


/**
 * This test is similar to the CalcDeliveryFractionTest, but it actually tests
 * the incremental delivered and total delivered values, not just the delivery
 * fraction.  Also, this test hits the CalcAnalysis action rather that the
 * CalcDeliveryFraction Action, so its more of an integrated/higher level test
 * of the derived values.
 * 
 * There is one 'hole' in this set of tests.  To save a bit of work, we did not
 * manually load all upstream values into the .tab files - we stopped at reach 9681.
 * For incremental tests, we turn off transport for 9681 allowing the test to
 * validate that all reaches not listed in the .tab are zero.  For total
 * comparisons where the upstream values are important we can't do that
 * w/o generating values that can't be matched to what you would be able to
 * see/validate in the UI, so we leave the transport for reach 9681 ON.
 * 
 * As a consequence, total comparison are not able to exhaustively exclude that
 * there may be non-upstream values which are non-zero, as well as upstream
 * reaches which could be incorrect.
 * 
 * @author eeverman
 */
public class CalcAnalysisDeliveryTest  extends SparrowUnitTest {
	
	static PredictData unmodifiedPredictData;
	static PredictData modifiedPredictData;
	
	static DataTable stdDelFracTo9682;
	static DataTable stdDelFracTo9674;
	static DataTable stdDelFracToBoth;
	
	static TerminalReaches target9682;
	static TerminalReaches target9674;
	static TerminalReaches targetBoth;
	
	@Override
	public void doOneTimeCustomSetup() throws Exception {
		
		
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
		super.doOneTimeCustomSetup();
		
		InputStream stdDelFracTo9682Stream = SparrowUnitTest.getResource(CalcDeliveryFractionTest.class, "stdDelFracTo9682", "tab");
		stdDelFracTo9682 = TabDelimFileUtil.readAsDouble(stdDelFracTo9682Stream, true, -1);
		
		InputStream stdDelFracTo9674Stream = SparrowUnitTest.getResource(CalcDeliveryFractionTest.class, "stdDelFracTo9674", "tab");
		stdDelFracTo9674 = TabDelimFileUtil.readAsDouble(stdDelFracTo9674Stream, true, -1);
		
		InputStream stdDelFracToBothStream = SparrowUnitTest.getResource(CalcDeliveryFractionTest.class, "stdDelFracToBoth", "tab");
		stdDelFracToBoth = TabDelimFileUtil.readAsDouble(stdDelFracToBothStream, true, -1);
		
		//Lets hack the predictData to Turn off transport for the two
		//reaches above reach 9681
		unmodifiedPredictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = unmodifiedPredictData.getTopo();
		SparseOverrideAdjustment adjTopo = new SparseOverrideAdjustment(topo);
		adjTopo.setValue(0d, unmodifiedPredictData.getRowForReachID(9619), gov.usgswim.sparrow.PredictData.IFTRAN_COL);
		adjTopo.setValue(0d, unmodifiedPredictData.getRowForReachID(9100), gov.usgswim.sparrow.PredictData.IFTRAN_COL);
		
		
		modifiedPredictData = new PredictDataImm(
				adjTopo.toImmutable(), unmodifiedPredictData.getCoef(),
				unmodifiedPredictData.getSrc(),
				unmodifiedPredictData.getSrcMetadata(),
				unmodifiedPredictData.getDelivery(),
				unmodifiedPredictData.getModel());
		
		//Targets set 1
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		target9682 = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		//Targets set 2
		targetList = new ArrayList<Long>();
		targetList.add(9674L);
		target9674 = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		//Targets set for both
		targetList = new ArrayList<Long>();
		targetList.add(9682L);
		targetList.add(9674L);
		targetBoth = new TerminalReaches(TEST_MODEL_ID, targetList);
		
	}
	
	@Test
	public void checkComparisonTextFiles() throws Exception {

		//
		//delivery to 9682 data
		//check columns
		assertEquals(0, stdDelFracTo9682.getColumnByName("IDENTIFIER").intValue());
		assertEquals(8, stdDelFracTo9682.getColumnByName("DEL_FRAC").intValue());
		//Row 0
		assertEquals(9682, stdDelFracTo9682.getInt(0, 0).intValue());
		assertEquals(1d, stdDelFracTo9682.getDouble(0, 8).doubleValue(), .00000001d);
		//Row 16 (the last)
		assertEquals(9681, stdDelFracTo9682.getInt(16, 0).intValue());
		assertEquals(0.983450541889348d, stdDelFracTo9682.getDouble(16, 8).doubleValue(), .00000001d);
		assertEquals(3681479.3438204400d, stdDelFracTo9682.getDouble(16, 18).doubleValue(), .00000001d);
		//Col 19 (the last)
		assertEquals(40.3493621349d, stdDelFracTo9682.getDouble(8, 19).doubleValue(), .00000001d);
		//Random value somewhere in the middle that keeps reporting zero
		assertEquals(25104.75677d, stdDelFracTo9682.getDouble(9, 12).doubleValue(), .00000001d);

		
		
		//
		//delivery to 9674 data
		//check columns
		assertEquals(0, stdDelFracTo9674.getColumnByName("IDENTIFIER").intValue());
		assertEquals(8, stdDelFracTo9674.getColumnByName("DEL_FRAC").intValue());
		//Row 0
		assertEquals(9674, stdDelFracTo9674.getInt(0, 0).intValue());
		assertEquals(1d, stdDelFracTo9674.getDouble(0, 8).doubleValue(), .00000001d);
		//Row 7 (the last)
		assertEquals(9679, stdDelFracTo9674.getInt(7, 0).intValue());
		assertEquals(0.844398102645677d, stdDelFracTo9674.getDouble(7, 8).doubleValue(), .00000001d);
	}
	

	//All Text columns we are interested in here
	/*
	AREA
	INC_LOAD
	TOTAL_LOAD
	INC_LOAD_1
	TOTAL_LOAD_1
	INC_DEL_FLUX
	TOTAL_DEL_FLUX
	INC_DEL_YIELD
	INC_DEL_FLUX_1
	TOTAL_DEL_FLUX_1
	INC_DEL_YIELD_1
	*/
	
	//////////////////////////////////
	// Tests for delivery to 9682
	//////////////////////////////////

	@Test
	public void testIncDeliveredTo9682() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, null, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9682, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9682.getColumnByName("INC_DEL_FLUX");
		
		doComparison(stdDelFracTo9682, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	@Test
	public void testInc_1DeliveredTo9682() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9682, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9682.getColumnByName("INC_DEL_FLUX_1");
		
		doComparison(stdDelFracTo9682, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	/**
	 * Uses the unmodified data so that transport goes above reach 9681 (as
	 * per normal).  This means that we can't validate reaches above 9681 or
	 * that reaches not upstream of the target are zero.  We can, however,
	 * spot check a few.
	 */
	@Test
	public void testTotalDeliveredTo9682() throws Exception {
		
		switchToUnmodifiedPredictData();

		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_delivered_flux, null, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9682, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9682.getColumnByName("TOTAL_DEL_FLUX");
		
		doComparison(stdDelFracTo9682, ssCol, result, unmodifiedPredictData, false);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
		
		if (log.isDebugEnabled()) {
			log.debug("***** testTotalDeliveredTo9682 Debug Output *****");
			debugReach(TEST_MODEL_ID, 9686L, target9682);
			debugReach(TEST_MODEL_ID, 9681L, target9682);
			debugReach(TEST_MODEL_ID, 9100L, target9682);
		}
		
		//Some spot checks of other reaches that should all be zero.
		DataTable tab = result.getTable();
		assertEquals(0d, result.getDouble(tab.getRowForId(9675L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8272L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8269L)), 0d);
	}
	
	/**
	 * Uses the unmodified data so that transport goes above reach 9681 (as
	 * per normal).  This means that we can't validate reaches above 9681 or
	 * that reaches not upstream of the target are zero.  We can, however,
	 * spot check a few.
	 */
	@Test
	public void testTotal_1DeliveredTo9682() throws Exception {
		
		switchToUnmodifiedPredictData();

		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9682, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9682.getColumnByName("TOTAL_DEL_FLUX_1");
		
		doComparison(stdDelFracTo9682, ssCol, result, unmodifiedPredictData, false);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
		
		//Some spot checks of other reaches that should all be zero.
		DataTable tab = result.getTable();
		assertEquals(0d, result.getDouble(tab.getRowForId(8272L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8269L)), 0d);
	}
	
	//////////////////////////////////
	// Tests for delivery to 9674
	//////////////////////////////////

	@Test
	public void testIncDeliveredTo9674() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, null, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9674, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9674.getColumnByName("INC_DEL_FLUX");
		
		doComparison(stdDelFracTo9674, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	@Test
	public void testInc_1DeliveredTo9674() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9674, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9674.getColumnByName("INC_DEL_FLUX_1");
		
		//This fails for reach 9685 which is zero in the spreadsheet, but should
		//probably not be.  Run query to get full value.
		doComparison(stdDelFracTo9674, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	/**
	 * Uses the unmodified data so that transport goes above reach 9681 (as
	 * per normal).  This means that we can't validate reaches above 9681 or
	 * that reaches not upstream of the target are zero.  We can, however,
	 * spot check a few.
	 */
	@Test
	public void testTotalDeliveredTo9674() throws Exception {
		
		switchToUnmodifiedPredictData();

		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_delivered_flux, null, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9674, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9674.getColumnByName("TOTAL_DEL_FLUX");
		
		doComparison(stdDelFracTo9674, ssCol, result, unmodifiedPredictData, false);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
		
		//Some spot checks of other reaches that should all be zero.
		DataTable tab = result.getTable();
		assertEquals(0d, result.getDouble(tab.getRowForId(8272L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8269L)), 0d);
	}
	
	/**
	 * Uses the unmodified data so that transport goes above reach 9681 (as
	 * per normal).  This means that we can't validate reaches above 9681 or
	 * that reaches not upstream of the target are zero.  We can, however,
	 * spot check a few.
	 */
	@Test
	public void testTotal_1DeliveredTo9674() throws Exception {
		
		switchToUnmodifiedPredictData();

		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9674, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracTo9674.getColumnByName("TOTAL_DEL_FLUX_1");
		
		doComparison(stdDelFracTo9674, ssCol, result, unmodifiedPredictData, false);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
		
		//Some spot checks of other reaches that should all be zero.
		DataTable tab = result.getTable();
		assertEquals(0d, result.getDouble(tab.getRowForId(9675L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8272L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8269L)), 0d);
	}
	

	//////////////////////////////////
	// Tests for delivery to BOTH 9682 & 9674
	//////////////////////////////////

	@Test
	public void testIncDeliveredToBoth() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, null, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracToBoth.getColumnByName("INC_DEL_FLUX");
		
		doComparison(stdDelFracToBoth, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	@Test
	public void testInc_1DeliveredToBoth() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracToBoth.getColumnByName("INC_DEL_FLUX_1");
		
		//This fails for reach 9685 which is zero in the spreadsheet, but should
		//probably not be.  Run query to get full value.
		doComparison(stdDelFracToBoth, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	/**
	 * Uses the unmodified data so that transport goes above reach 9681 (as
	 * per normal).  This means that we can't validate reaches above 9681 or
	 * that reaches not upstream of the target are zero.  We can, however,
	 * spot check a few.
	 */
	@Test
	public void testTotalDeliveredToBoth() throws Exception {
		
		switchToUnmodifiedPredictData();

		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_delivered_flux, null, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracToBoth.getColumnByName("TOTAL_DEL_FLUX");
		
		doComparison(stdDelFracToBoth, ssCol, result, unmodifiedPredictData, false);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
		
		//Some spot checks of other reaches that should all be zero.
		DataTable tab = result.getTable();
		assertEquals(0d, result.getDouble(tab.getRowForId(8272L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8269L)), 0d);
	}
	
	/**
	 * Uses the unmodified data so that transport goes above reach 9681 (as
	 * per normal).  This means that we can't validate reaches above 9681 or
	 * that reaches not upstream of the target are zero.  We can, however,
	 * spot check a few.
	 */
	@Test
	public void testTotal_1DeliveredToBoth() throws Exception {
		
		switchToUnmodifiedPredictData();

		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NominalComparison.getNoComparisonInstance());
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		DataColumn result = action.run();

		int ssCol = stdDelFracToBoth.getColumnByName("TOTAL_DEL_FLUX_1");
		
		doComparison(stdDelFracToBoth, ssCol, result, unmodifiedPredictData, false);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
		
		//Some spot checks of other reaches that should all be zero.
		DataTable tab = result.getTable();
		assertEquals(0d, result.getDouble(tab.getRowForId(8272L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8269L)), 0d);
	}
	/**
	 * Does the comparison.
	 * 
	 * @param stdData
	 * @param stdCol
	 * @param result
	 * @param predictData
	 * @param missingDataShouldBeZero	If a row is not present in the std data,
	 * 	should the actual value be zero?
	 */
	protected void doComparison(DataTable stdData, int stdCol,
				DataColumn result, PredictData predictData, boolean missingDataShouldBeZero) {
		
		log.debug("Comparing results w/ " + result.getRowCount() + " rows.");
		result.getRowCount();
		assertEquals(predictData.getTopo().getRowCount(), result.getRowCount());
		result.getIdForRow(8320);
		
		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;
		
		for (int r=0; r<result.getRowCount(); r++) {
			Double expectedVal = getSpreadsheetDelFracForPredictDataRow(
					stdData, predictData, r, stdCol);
			double actualVal = result.getDouble(r);
			
			if (expectedVal != null) {
				inSheet++;
				
				if (comp(expectedVal, actualVal)) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdData, predictData, r, expectedVal, actualVal);
				}
			} else {
				notInSheet++;
				if (missingDataShouldBeZero && actualVal != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdData, predictData, r, expectedVal, actualVal);
				}
			}
		}
		
		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);
		
		assertEquals(0, noMatch + expectedZeroNoMatch);
	}
	
	public boolean comp(double expect, double compare) {
		final double REQUIRED_FRACTION = .00001d;	//comp fraction
		
		if (expect == 0d) {
			return compare == 0;
		} else {
			double diff = Math.abs(compare - expect);
			double baseValue = Math.abs(expect);
			double frac = diff / baseValue;
			return frac < REQUIRED_FRACTION;
		}
	}
	
	protected void switchToUnmodifiedPredictData() {
			Ehcache pdc = SparrowCacheManager.getInstance().getEhcache(PredictData.name());
			Element e  = new Element(TEST_MODEL_ID, unmodifiedPredictData);
			pdc.put(e);

			SparrowCacheManager.getInstance().getEhcache(PredictResult.name()).removeAll();
			SparrowCacheManager.getInstance().getEhcache(DeliveryFraction.name()).removeAll();
			
			assertNull(SharedApplication.getInstance().getDeliveryFraction(target9674, true));
			assertNull(SharedApplication.getInstance().getDeliveryFraction(target9682, true));
	}
	
	protected void switchToModifiedPredictData() {
		Ehcache pdc = SparrowCacheManager.getInstance().getEhcache(PredictData.name());
		Element e  = new Element(TEST_MODEL_ID, modifiedPredictData);
		pdc.put(e);

		SparrowCacheManager.getInstance().getEhcache(PredictResult.name()).removeAll();
		SparrowCacheManager.getInstance().getEhcache(DeliveryFraction.name()).removeAll();
		
		assertNull(SharedApplication.getInstance().getDeliveryFraction(target9674, true));
		assertNull(SharedApplication.getInstance().getDeliveryFraction(target9682, true));
}
	
	protected void debugComparisons(int match, int noMatch, int inSheet, int notInSheet, int expectedZeroNoMatch) {
		log.debug("Delivery to reach ID 9682 results: (" + (noMatch + expectedZeroNoMatch) + " bad values)");
		log.debug("** " +  inSheet + " rows w/ IDs matching the validation data, " 
				+ match + "/" + noMatch + " matching/non-matching values.");
		log.debug("** " + notInSheet + " rows where in the model, but not in the validation data.");
		log.debug("** Of those, " +  expectedZeroNoMatch + " had non-zero values (should be 0).");
	}
	
	protected Double getSpreadsheetDelFracForPredictDataRow(
			DataTable spreadsheet, PredictData predictData, int predictDataRow, int ssCol) {
		
		//This is to work around a bug where the mutable version of the datatables
		//can only search on identical datatypes, thus, the row ID must be a
		//double.
		Long rowIdLong = predictData.getTopo().getIdForRow(predictDataRow);
		Double rowId = rowIdLong.doubleValue();
		int ssRow = spreadsheet.findFirst(0, rowId);
		
		if (ssRow > -1) {
			return spreadsheet.getDouble(ssRow, ssCol);
		} else {
			return null;
		}
	}
	
	protected void writeBadMatch(DataTable spreadsheet, PredictData predictData,
			int predictDataRow, Double expectedDelFrac, Double actualDelFrac) {
		
		log.error("-- Comp Fail --");
		log.error("Row in Predict Data: " + predictDataRow);
		log.error("rowId: " + predictData.getTopo().getIdForRow(predictDataRow));
		log.error("Expected vs Actual: " + expectedDelFrac + " / " + actualDelFrac);
	}
	
	public void debugReach(Long modelId, Long reachId, TerminalReaches terminalReaches) throws Exception {
		PredictData pd = SharedApplication.getInstance().getPredictData(modelId);
		int row = pd.getRowForReachID(reachId);
		
		debugReach(pd, reachId);
		
		if (terminalReaches != null) {
			ColumnData delFracColumn = SharedApplication.getInstance().getDeliveryFraction(terminalReaches);
			
			SimpleDataTable delivery = new SimpleDataTable(
					new ColumnData[] {delFracColumn}, "Delivery Fraction",
					"A single column table containing the delivery fraction" +
					" to a target reach or reaches.", null, null
				);
			
			debugTable(delivery, row, "Delivery Fraction Data");
		}
	}
	
	public void debugReach(PredictData pd, Long reachId) throws Exception {
		int row = pd.getRowForReachID(reachId);
		
		if (pd.getModel() != null && pd.getModel().getId() != null) {
			log.error("Debugging Reach " + reachId + " for model " + pd.getModel().getId());
		} else {
			log.error("Debugging Reach " + reachId + " for model [unknown model]");
		}
		
		debugTable(pd.getTopo(), row, "Topo Data");
		debugTable(pd.getSrc(), row, "Source Data");
		debugTable(pd.getCoef(), row, "Coef Data");
		debugTable(pd.getDelivery(), row, "Delivery Data");
	}
	
	public void debugTable(DataTable table, int row, String description) throws Exception {
		log.debug(" - - " + description + " - - ");
		
		if (table != null) {
			if (row < table.getRowCount()) {
				for (int col = 0; col < table.getColumnCount(); col++) {
					String label = table.getName(col);
					if (label == null || label.equals("")) {
						label = "(" + Integer.toString(col) + ")";
					}
					
					log.debug(label + " " + table.getString(row, col));
				}
			} else {
				log.debug("[Row " + row +
						" requested, however, the table only contains " +
						table.getRowCount() + " rows.]");
			}
		} else {
			log.debug("[This table was null]");
		}
	}
	
	
	
}

