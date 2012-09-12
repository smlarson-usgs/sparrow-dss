package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.ColumnDataFromTable;
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
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import org.junit.Before;
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
public class CalcIncrementalYieldTest  extends SparrowTestBaseWithDBandCannedModel50 {
	
	PredictData predictData;
	PredictResult predictResult;
	ColumnData catchmentAreaColumn;
	
	@Before
	public void setup() {
		
		
		//Set up contexts
		AdjustmentGroups adjustments = new AdjustmentGroups(TEST_MODEL_ID);
		
		predictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		predictResult = SharedApplication.getInstance().getPredictResult(adjustments);
		UnitAreaRequest catchAreaReq = new UnitAreaRequest(TEST_MODEL_ID, AggregationLevel.NONE, false);
		DataTable catchmentAreaTable = SharedApplication.getInstance().getCatchmentAreas(catchAreaReq);
		catchmentAreaColumn = new ColumnDataFromTable(catchmentAreaTable, 1);
	}
	
	@Test
	public void checkValuesForTotalIncYield() throws Exception {

		
		//Calc from Action
		CalcIncrementalYield calcIncYield = new CalcIncrementalYield(
				predictData.getModel(), predictResult, catchmentAreaColumn, null);
		ColumnData calcYieldResultCol = calcIncYield.run();
		
		//Get canned predict results
		PredictData cannedPredictData = getTestModelPredictData();
		PredictResult cannedResult = getTestModelPredictResult();
		ColumnData cannedInc = cannedResult.getColumn(cannedResult.getDecayedIncrementalCol());
		DivideColumnData cannedYield = new DivideColumnData(cannedInc, catchmentAreaColumn, null);
		
		//Check against canned result (HOLE:  The catchment area data could be loading
		//incorrectly causing both to be wrong.
		assertTrue(compareColumns(cannedYield, calcYieldResultCol, false, false, .0000001d));
		
		//Some really simple checks - we don't have canned result data for this
		//to really verify against
		assertTrue(calcYieldResultCol.getMaxDouble() > 1000D);
		assertTrue(calcYieldResultCol.getMinDouble() == 0D);
		assertEquals(cannedResult.getRowCount(), calcYieldResultCol.getRowCount().intValue());
		
		
		//Check named metadata
		assertEquals(
				Action.getDataSeriesProperty(DataSeriesType.incremental_yield, false),
				calcYieldResultCol.getName());
		assertEquals(
				Action.getDataSeriesProperty(DataSeriesType.incremental_yield, true),
				calcYieldResultCol.getDescription());
		assertEquals(
				SparrowUnits.KG_PER_SQR_KM_PER_YEAR.toString(), calcYieldResultCol.getUnits());
		
		//Check metadata properties
		assertEquals(TEST_MODEL_ID.toString(),
				calcYieldResultCol.getProperty(TableProperties.MODEL_ID.toString()));
		assertEquals(cannedPredictData.getModel().getConstituent(),
				calcYieldResultCol.getProperty(TableProperties.CONSTITUENT.toString()));
		assertEquals(DataSeriesType.incremental_yield.getBaseType().name(),
				calcYieldResultCol.getProperty(TableProperties.DATA_TYPE.toString()));
	}
	
	@Test
	public void checkValuesForSingleSourceIncYield() throws Exception {
		
		//Calc from Action
		CalcIncrementalYield calcIncYield = new CalcIncrementalYield(
				predictData.getModel(), predictResult, catchmentAreaColumn, 1);
		ColumnData calcYieldResultCol = calcIncYield.run();
		
		
		//Get canned predict results
		PredictResult cannedResult = getTestModelPredictResult();
		ColumnData cannedInc = cannedResult.getColumn(cannedResult.getDecayedIncrementalColForSrc(1));
		DivideColumnData cannedYield = new DivideColumnData(cannedInc, catchmentAreaColumn, null);
		
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
	public void compareActionResultToCalcAnalysisResultForTotalInc() throws Exception {
		
		//
		//Set up context
		AdjustmentGroups adjustments = new AdjustmentGroups(TEST_MODEL_ID);
		BasicAnalysis analysis = new BasicAnalysis(DataSeriesType.incremental_yield,
				null, null, null);
		PredictionContext yieldContext = new PredictionContext(TEST_MODEL_ID,
				adjustments, analysis, null, null, null);
		
		//Calc via analysis
		CalcAnalysis calcAnalysis = new CalcAnalysis();
		calcAnalysis.setContext(yieldContext);
		SparrowColumnSpecifier calcAnalysisResult = calcAnalysis.run();
		ColumnData calcAnalysisResultCol = calcAnalysisResult.getTable().getColumn(calcAnalysisResult.getColumn());
		
		//Calc from Action
		CalcIncrementalYield calcIncYield = new CalcIncrementalYield(
				predictData.getModel(), predictResult, catchmentAreaColumn, null);
		ColumnData calcYieldResultCol = calcIncYield.run();

		
		assertTrue(
				compareColumns(calcAnalysisResultCol, calcYieldResultCol, true, true, .000000000001d));
	}
	
	

	
}

