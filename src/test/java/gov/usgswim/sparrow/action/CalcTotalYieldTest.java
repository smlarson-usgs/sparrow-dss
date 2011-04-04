package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.ColumnDataFromTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.DivideColumnData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.BasicAnalysis;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.UnitAreaType;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;


/**
 * Calculates the Incremental Yield.
 * 
 * Basic calc is: Incremental Load / Catchment Area
 * Incremental Load is the decayed load.
 * 
 * @author eeverman
 *
 */
public class CalcTotalYieldTest  extends SparrowTestBaseWithDBandCannedModel50 {
	
	PredictData predictData;
	PredictResult predictResult;
	ColumnData watershedAreaColumn;
	
	@Before
	public void setup() {
		
		
		//Set up contexts
		AdjustmentGroups adjustments = new AdjustmentGroups(TEST_MODEL_ID);
		
		predictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		predictResult = SharedApplication.getInstance().getPredictResult(adjustments);
		UnitAreaRequest watershedAreaReq = new UnitAreaRequest(TEST_MODEL_ID, UnitAreaType.HUC_NONE, true);
		DataTable watershedAreaTable = SharedApplication.getInstance().getCatchmentAreas(watershedAreaReq);
		watershedAreaColumn = new ColumnDataFromTable(watershedAreaTable, 1);
	}
	
	@Test
	public void checkValuesForTotalYield() throws Exception {

		
		//Calc from Action
		CalcTotalYield calcTotalYield = new CalcTotalYield(
				predictData, predictResult, watershedAreaColumn, null);
		ColumnData calcYieldResultCol = calcTotalYield.run();
		
		
		//Get canned predict results
		PredictData cannedPredictData = getTestModelPredictData();
		PredictResult cannedResult = getTestModelPredictResult();
		ColumnData cannedTotal = cannedResult.getColumn(cannedResult.getTotalCol());
		DivideColumnData cannedCalcedYield = new DivideColumnData(cannedTotal, watershedAreaColumn, null);
		
		//Check against canned result (HOLE:  The catchment area data could be loading
		//incorrectly causing both to be wrong.
		assertTrue(compareColumns(cannedCalcedYield, calcYieldResultCol, false, false, .0000001d));
		
		//Some really simple checks - we don't have canned result data for this
		//to really verify against
		assertTrue(calcYieldResultCol.getMaxDouble() > 1000D);
		assertTrue(calcYieldResultCol.getMinDouble() < 6D);
		assertEquals(cannedResult.getRowCount(), calcYieldResultCol.getRowCount().intValue());
		
		//Compare to canned
		//Not working b/c Ann has the wrong units.  Bummer.
//		DataTable allPredict = getTestModelCompleteResult();
		//...compare total yield to canned result
//		ColumnData cannedTotalYield = allPredict.getColumn(allPredict.getColumnByName("total_yield"));
//		assertTrue(compareColumns(cannedTotalYield, calcYieldResultCol, false, false, .0000001d));
		
		
		
		//Check named metadata
		assertEquals(
				Action.getDataSeriesProperty(DataSeriesType.total_yield, false),
				calcYieldResultCol.getName());
		assertEquals(
				Action.getDataSeriesProperty(DataSeriesType.total_yield, true),
				calcYieldResultCol.getDescription());
		assertEquals(
				SparrowUnits.KG_PER_SQR_KM_PER_YEAR.toString(), calcYieldResultCol.getUnits());
		
		//Check metadata properties
		assertEquals(TEST_MODEL_ID.toString(),
				calcYieldResultCol.getProperty(TableProperties.MODEL_ID.toString()));
		assertEquals(cannedPredictData.getModel().getConstituent(),
				calcYieldResultCol.getProperty(TableProperties.CONSTITUENT.toString()));
		assertEquals(DataSeriesType.total_yield.getBaseType().name(),
				calcYieldResultCol.getProperty(TableProperties.DATA_TYPE.toString()));
	}
	
	@Test
	public void checkValuesForSingleSourceIncYield() throws Exception {
		
		//Calc from Action
		CalcTotalYield calcTotalYield = new CalcTotalYield(
				predictData, predictResult, watershedAreaColumn, 1);
		ColumnData calcYieldResultCol = calcTotalYield.run();
		
		
		//Get canned predict results
		PredictResult cannedResult = getTestModelPredictResult();
		ColumnData cannedTotal = cannedResult.getColumn(cannedResult.getTotalColForSrc(1));
		DivideColumnData cannedYield = new DivideColumnData(cannedTotal, watershedAreaColumn, null);
		
		//Check against canned result (HOLE:  The catchment area data could be loading
		//incorrectly causing both to be wrong.
		assertTrue(compareColumns(cannedYield, calcYieldResultCol, false, false, .0000001d));
		
		//Some really simple checks - we don't have canned result data for this
		//to really verify against
		assertTrue(calcYieldResultCol.getMaxDouble() > 1000D);
		assertTrue(calcYieldResultCol.getMinDouble() == 0D);
		assertEquals(cannedResult.getRowCount(), calcYieldResultCol.getRowCount().intValue());
	}
	
	@Test
	public void compareActionResultToCalcAnalysisResultForTotalYield() throws Exception {
		
		//
		//Set up context
		AdjustmentGroups adjustments = new AdjustmentGroups(TEST_MODEL_ID);
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.total_yield,
				null, null, null);
		PredictionContext yieldContext = new PredictionContext(TEST_MODEL_ID,
				adjustments, analysis, null, null, null);
		
		//Calc via analysis
		CalcAnalysis calcAnalysis = new CalcAnalysis();
		calcAnalysis.setContext(yieldContext);
		SparrowColumnSpecifier calcAnalysisResult = calcAnalysis.run();
		ColumnData calcAnalysisResultCol = calcAnalysisResult.getTable().getColumn(calcAnalysisResult.getColumn());
		
		//Calc from Action
		CalcTotalYield calcTotalYield = new CalcTotalYield(
				predictData, predictResult, watershedAreaColumn, null);
		ColumnData calcYieldResultCol = calcTotalYield.run();

		
		assertTrue(
				compareColumns(calcAnalysisResultCol, calcYieldResultCol, true, true, .000000000001d));
	}
	
	

	
}

