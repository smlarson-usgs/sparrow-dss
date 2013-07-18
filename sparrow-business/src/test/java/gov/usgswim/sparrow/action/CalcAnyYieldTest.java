package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.impl.StandardNumberColumnDataWritable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.service.SharedApplication;
import org.junit.Assert;

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
public class CalcAnyYieldTest extends SparrowTestBaseWithDBandCannedModel50 {

	private final static double comp_err = .000000001d;
	PredictData predictData;
	StandardNumberColumnDataWritable areaCol;
	StandardNumberColumnDataWritable loadCol;

	@Before
	public void setup() {

		predictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);

		//
		areaCol = new StandardNumberColumnDataWritable();
		areaCol.setUnits(SparrowUnits.SQR_KM.getUserName());
		areaCol.setName("Area");
		areaCol.setValue(1D, 0);
		areaCol.setValue(2D, 1);
		areaCol.setValue(3D, 2);

		//
		loadCol = new StandardNumberColumnDataWritable();
		loadCol.setUnits(SparrowUnits.KG_PER_YEAR.getUserName());
		loadCol.setName("Load");
		loadCol.setValue(10D, 0);
		loadCol.setValue(200D, 1);
		loadCol.setValue(3000D, 2);
	}

	@Test
	public void checkValues() throws Exception {
		CalcAnyYield calcAnyYield = new CalcAnyYield(
				DataSeriesType.total_yield,
				predictData.getModel(),
				loadCol,
				areaCol,
				true);

		ColumnData result = calcAnyYield.run();
		Assert.assertTrue(calcAnyYield.getValidationErrors().length == 0);

		assertEquals(10D, result.getDouble(0), comp_err);
		assertEquals(100D, result.getDouble(1), comp_err);
		assertEquals(1000D, result.getDouble(2), comp_err);
	}

	@Test
	public void checkAreaUnitValidation() throws Exception {

		areaCol.setUnits("Something else");

		CalcAnyYield calcAnyYield = new CalcAnyYield(
				DataSeriesType.total_yield,
				predictData.getModel(),
				loadCol,
				areaCol,
				true);

		ColumnData result = calcAnyYield.run();
		Assert.assertTrue(calcAnyYield.getValidationErrors().length == 1);
	}

	@Test
	public void checkLoadUnitValidation() throws Exception {

		loadCol.setUnits("Something else");

		CalcAnyYield calcAnyYield = new CalcAnyYield(
				DataSeriesType.total_yield,
				predictData.getModel(),
				loadCol,
				areaCol,
				true);

		ColumnData result = calcAnyYield.run();
		Assert.assertTrue(calcAnyYield.getValidationErrors().length == 1);
	}

	@Test
	public void checkMismatchedRowCountValidation() throws Exception {

		loadCol.setValue(10000D, 3);

		CalcAnyYield calcAnyYield = new CalcAnyYield(
				DataSeriesType.total_yield,
				predictData.getModel(),
				loadCol,
				areaCol,
				true);

		ColumnData result = calcAnyYield.run();
		Assert.assertTrue(calcAnyYield.getValidationErrors().length == 1);
	}

	@Test
	public void checkWrongDataseriesValidation() throws Exception {

		CalcAnyYield calcAnyYield = new CalcAnyYield(
				DataSeriesType.incremental_area, /* bad series */
				predictData.getModel(),
				loadCol,
				areaCol,
				true);

		ColumnData result = calcAnyYield.run();
		Assert.assertTrue(calcAnyYield.getValidationErrors().length == 1);
	}

	@Test
	public void checkResultColNameIsVerbose() throws Exception {
		CalcAnyYield calcAnyYield = new CalcAnyYield(
				DataSeriesType.total_yield,
				predictData.getModel(),
				loadCol,
				areaCol,
				true);

		ColumnData result = calcAnyYield.run();

		String expectedName =
				Action.getDataSeriesProperty(DataSeriesType.total_yield, false)
				+ CalcAnyYield.YIELD_LOAD_NAME_DELIMITER
				+ loadCol.getName();

		assertEquals(result.getName(), expectedName);
	}

	@Test
	public void checkResultColNameIsNonVerbose() throws Exception {
		CalcAnyYield calcAnyYield = new CalcAnyYield(
				DataSeriesType.total_yield,
				predictData.getModel(),
				loadCol,
				areaCol,
				false);

		ColumnData result = calcAnyYield.run();

		String expectedName =
				Action.getDataSeriesProperty(DataSeriesType.total_yield, false);

		assertEquals(result.getName(), expectedName);

		//
		//Check the default is the same
		calcAnyYield = new CalcAnyYield(
				DataSeriesType.total_yield,
				predictData.getModel(),
				loadCol,
				areaCol);

		result = calcAnyYield.run();
		assertEquals(result.getName(), expectedName);

	}
}
