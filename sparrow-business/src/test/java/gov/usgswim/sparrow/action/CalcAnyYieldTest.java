package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.impl.ColumnDataFromTable;
import gov.usgswim.sparrow.AreaType;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.SparrowUnits;
import static gov.usgswim.sparrow.action.Action.getDataSeriesProperty;
import gov.usgswim.sparrow.datatable.DivideColumnData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.BasicAnalysis;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.AggregationLevel;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import org.junit.Before;
import org.junit.Test;


/**
 * Calculates the Incremental Yield.
 *
 * Basic calc is: Incremental Load / Catchment Area
 *
 * @author eeverman
 *
 */
public class CalcAnyYieldTest  extends SparrowTestBaseWithDBandCannedModel50 {

	PredictData predictData;
	PredictResult predictResult;
	ColumnData catchmentAreaColumn;

	@Before
	public void setup() {


		//Set up contexts
		AdjustmentGroups adjustments = new AdjustmentGroups(TEST_MODEL_ID);

		predictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		predictResult = SharedApplication.getInstance().getPredictResult(adjustments);
		UnitAreaRequest catchAreaReq = new UnitAreaRequest(TEST_MODEL_ID, AreaType.TOTAL_CONTRIBUTING);
		DataTable catchmentAreaTable = SharedApplication.getInstance().getCatchmentAreas(catchAreaReq);
		catchmentAreaColumn = new ColumnDataFromTable(catchmentAreaTable, 1);
	}

	@Test
	public void checkResultColumnNameIsCorrect() throws Exception {
		DataTable deliveryData = predictData.getDelivery();
		int loadColumnIndex = 1;
		ColumnData loadColumn = deliveryData.getColumn(loadColumnIndex);
		SparrowModel model = predictData.getModel();
		String loadColumnName = loadColumn.getName();
		DataSeriesType seriesType = DataSeriesType.total_yield;
		String baseExpectedName = Action.getDataSeriesProperty(seriesType, false, "Yield");
		System.out.println(baseExpectedName);
		CalcAnyYield calcAnyYield = new CalcAnyYield(
			seriesType,
			model,
			loadColumn,
			catchmentAreaColumn,
			true
			);
		ColumnData calcYieldResultCol = calcAnyYield.run();
		assertEquals(calcYieldResultCol.getName(),
			baseExpectedName + CalcAnyYield.YIELD_LOAD_NAME_DELIMITER + loadColumnName);

		calcAnyYield = new CalcAnyYield(
			seriesType,
			model,
			loadColumn,
			catchmentAreaColumn,
			false
			);
		calcYieldResultCol = calcAnyYield.run();
		assertEquals(calcYieldResultCol.getName(),
			baseExpectedName);

		//test that the constructor sets no load name in the result column name
		calcAnyYield = new CalcAnyYield(
			seriesType,
			model,
			loadColumn,
			catchmentAreaColumn
			);
		calcYieldResultCol = calcAnyYield.run();
		assertEquals(calcYieldResultCol.getName(),
			baseExpectedName);

	}

}

