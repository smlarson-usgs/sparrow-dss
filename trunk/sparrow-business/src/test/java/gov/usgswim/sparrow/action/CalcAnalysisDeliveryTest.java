package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.*;

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
public class CalcAnalysisDeliveryTest  extends DeliveryBase {
	

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
				target9682, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

		int ssCol = stdDelFracTo9682.getColumnByName("INC_DEL_FLUX");
		
		doComparison(stdDelFracTo9682, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	@Test
	public void testInc_1DeliveredTo9682() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9682, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				target9682, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				target9682, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				target9674, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

		int ssCol = stdDelFracTo9674.getColumnByName("INC_DEL_FLUX");
		
		doComparison(stdDelFracTo9674, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	@Test
	public void testInc_1DeliveredTo9674() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				target9674, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				target9674, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				target9674, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

		int ssCol = stdDelFracToBoth.getColumnByName("INC_DEL_FLUX");
		
		doComparison(stdDelFracToBoth, ssCol, result, modifiedPredictData, true);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
	}
	
	@Test
	public void testInc_1DeliveredToBoth() throws Exception {
		
		switchToModifiedPredictData();
		
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_delivered_flux, 1, null, null);
		PredictionContext context = new PredictionContext(TEST_MODEL_ID, new AdjustmentGroups(TEST_MODEL_ID), analysis,
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

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
				targetBoth, new AreaOfInterest(TEST_MODEL_ID), NoComparison.NO_COMPARISON);
		
		CalcAnalysis action = new CalcAnalysis();
		action.setContext(context);
		SparrowColumnSpecifier result = action.run();

		int ssCol = stdDelFracToBoth.getColumnByName("TOTAL_DEL_FLUX_1");
		
		doComparison(stdDelFracToBoth, ssCol, result, unmodifiedPredictData, false);
		assertEquals(SingleColumnCoefDataTable.class, result.getTable().getClass());
		
		//Some spot checks of other reaches that should all be zero.
		DataTable tab = result.getTable();
		assertEquals(0d, result.getDouble(tab.getRowForId(8272L)), 0d);
		assertEquals(0d, result.getDouble(tab.getRowForId(8269L)), 0d);
	}
	
	
	
}

