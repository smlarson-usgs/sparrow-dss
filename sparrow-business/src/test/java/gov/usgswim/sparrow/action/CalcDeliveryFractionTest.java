package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgs.cida.datatable.impl.SparseDoubleColumnData;
import gov.usgs.cida.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.*;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.ReachRowValueMap;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.junit.Test;

/**
 * Compares calculated delivery fractions to hand calculated values stored in
 * a few text files.  The network is 'cut' at reach 9681 by turning off transport
 * at that reach so that the delivery below that point is a small set of reaches
 * that can be calculated by hand.
 *
 * @author eeverman
 */
public class CalcDeliveryFractionTest extends SparrowTestBase {

	static PredictData unmodifiedPredictData;
	static PredictData pdTranportOffAbove9681;
	static PredictData pdShoreReachAbove9681;

	static DataTable stdData;
	static DataTable stdDelFracTo9682;
	static DataTable stdDelFracTo9674;
	static DataTable stdDelFracToBoth;

	static final double COMP_ERROR = .0000001d;

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//log.setLevel(Level.DEBUG);
		super.doOneTimeCustomSetup();

		BufferedReader baseDataStream = new BufferedReader(new InputStreamReader(
				SparrowTestBase.getResource(CalcDeliveryFractionTest.class, "data", "tab")
			));
		stdData = TabDelimFileUtil.readAsDouble(baseDataStream, true, -1);

		BufferedReader stdDelFracTo9682Stream = new BufferedReader(new InputStreamReader(
				SparrowTestBase.getResource(CalcDeliveryFractionTest.class, "stdDelFracTo9682", "tab")
			));
		stdDelFracTo9682 = TabDelimFileUtil.readAsDouble(stdDelFracTo9682Stream, true, -1);

		BufferedReader stdDelFracTo9674Stream = new BufferedReader(new InputStreamReader(
				SparrowTestBase.getResource(CalcDeliveryFractionTest.class, "stdDelFracTo9674", "tab")
			));
		stdDelFracTo9674 = TabDelimFileUtil.readAsDouble(stdDelFracTo9674Stream, true, -1);

		BufferedReader stdDelFracToBothStream = new BufferedReader(new InputStreamReader(
				SparrowTestBase.getResource(CalcDeliveryFractionTest.class, "stdDelFracToBoth", "tab")
			));
		stdDelFracToBoth = TabDelimFileUtil.readAsDouble(stdDelFracToBothStream, true, -1);

		unmodifiedPredictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);

		//Hack the predictData to Turn off transport for the two reaches above reach 9681
		DataTable topo = unmodifiedPredictData.getTopo();
		SparseOverrideAdjustment topoTransportOffAbove9681 = new SparseOverrideAdjustment(topo);
		topoTransportOffAbove9681.setValue(0d, unmodifiedPredictData.getRowForReachID(9619), PredictData.TOPO_IFTRAN_COL);
		topoTransportOffAbove9681.setValue(0d, unmodifiedPredictData.getRowForReachID(9100), PredictData.TOPO_IFTRAN_COL);

		//Hack the predictData to make the two reaches above reach 9681 to be shoreline reaches
		SparseOverrideAdjustment topoShoreReachbove9681 = new SparseOverrideAdjustment(topo);
		topoShoreReachbove9681.setValue(1d, unmodifiedPredictData.getRowForReachID(9619), PredictData.TOPO_SHORE_REACH_COL);
		topoShoreReachbove9681.setValue(1d, unmodifiedPredictData.getRowForReachID(9100), PredictData.TOPO_SHORE_REACH_COL);


		pdTranportOffAbove9681 = new PredictDataImm(
				new TopoDataComposit(topoTransportOffAbove9681), unmodifiedPredictData.getCoef(),
				unmodifiedPredictData.getSrc(),
				unmodifiedPredictData.getSrcMetadata(),
				unmodifiedPredictData.getDelivery(),
				unmodifiedPredictData.getModel());

		pdShoreReachAbove9681 = new PredictDataImm(
				new TopoDataComposit(topoShoreReachbove9681), unmodifiedPredictData.getCoef(),
				unmodifiedPredictData.getSrc(),
				unmodifiedPredictData.getSrcMetadata(),
				unmodifiedPredictData.getDelivery(),
				unmodifiedPredictData.getModel());

	}

	@Test
	public void testFracFactoryTo9682_NoTransportDataSet() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(pdTranportOffAbove9681, targetList);
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();

		ReachRowValueMap delHash = hashAction.run();

		delAction.setPredictData(pdTranportOffAbove9681);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();

		//check metadata of delivery fraction
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.delivered_fraction, false), deliveryFrac.getName());
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.delivered_fraction, true), deliveryFrac.getDescription());
		assertEquals(SparrowUnits.FRACTION.getUserName(), deliveryFrac.getUnits());
		assertEquals(unmodifiedPredictData.getModel().getConstituent(), deliveryFrac.getProperty(TableProperties.CONSTITUENT.toString()));


		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;

		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracTo9682, pdTranportOffAbove9681, r);
			double actualFrac = deliveryFrac.getDouble(r);

			if (expectedFrac != null) {
				inSheet++;

				if (Math.abs(expectedFrac - actualFrac) < COMP_ERROR) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracTo9682, pdTranportOffAbove9681, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracTo9682, pdTranportOffAbove9681, r, expectedFrac, actualFrac);
				}
			}
		}

		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);

		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);

		assertEquals(StandardDoubleColumnData.class, deliveryFrac.getClass());

		//Check that the del frac for the 'turned off' reaches is zero
		assertEquals(0d, deliveryFrac.getDouble(pdTranportOffAbove9681.getTopo().getRowForId(9619L)), COMP_ERROR);
		assertEquals(0d, deliveryFrac.getDouble(pdTranportOffAbove9681.getTopo().getRowForId(9100L)), COMP_ERROR);
	}

	/**
	 * The effect of making the 9619 and 9100 shoreline reaches (instead of turning
	 * off their transport) should be the same:  Delivery calcs should stop tracing
	 * upstream and not include these two reaches.
	 *
	 * @throws Exception
	 */
	@Test
	public void testFracFactoryTo9682_ShoreReachDataSet() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(pdShoreReachAbove9681, targetList);
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();

		ReachRowValueMap delHash = hashAction.run();

		delAction.setPredictData(pdShoreReachAbove9681);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();

		//check metadata of delivery fraction
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.delivered_fraction, false), deliveryFrac.getName());
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.delivered_fraction, true), deliveryFrac.getDescription());
		assertEquals(SparrowUnits.FRACTION.getUserName(), deliveryFrac.getUnits());
		assertEquals(unmodifiedPredictData.getModel().getConstituent(), deliveryFrac.getProperty(TableProperties.CONSTITUENT.toString()));


		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;

		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracTo9682, pdShoreReachAbove9681, r);
			double actualFrac = deliveryFrac.getDouble(r);

			if (expectedFrac != null) {
				inSheet++;

				if (Math.abs(expectedFrac - actualFrac) < COMP_ERROR) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracTo9682, pdShoreReachAbove9681, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracTo9682, pdShoreReachAbove9681, r, expectedFrac, actualFrac);
				}
			}
		}

		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);

		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);

		//Since upstream tracing stops at a shore reach, there are much fewer reaches
		//in the upstream list, allowing the sparse column to be used.
		assertEquals(SparseDoubleColumnData.class, deliveryFrac.getClass());

		//Check that the del frac for the 'turned off' reaches is zero
		assertEquals(0d, deliveryFrac.getDouble(pdShoreReachAbove9681.getTopo().getRowForId(9619L)), COMP_ERROR);
		assertEquals(0d, deliveryFrac.getDouble(pdShoreReachAbove9681.getTopo().getRowForId(9100L)), COMP_ERROR);
	}

	@Test
	public void testFracFactoryTo9674_NoTransportDataSet() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9674L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(
			pdTranportOffAbove9681,
			targetList
			);
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();

		ReachRowValueMap delHash = hashAction.run();

		delAction.setPredictData(pdTranportOffAbove9681);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();


		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;

		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracTo9674, pdTranportOffAbove9681, r);
			double actualFrac = deliveryFrac.getDouble(r);

			if (expectedFrac != null) {
				inSheet++;

				if (Math.abs(expectedFrac - actualFrac) < COMP_ERROR) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracTo9674, pdTranportOffAbove9681, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracTo9674, pdTranportOffAbove9681, r, expectedFrac, actualFrac);
				}
			}
		}

		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);

		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);

		assertEquals(StandardDoubleColumnData.class, deliveryFrac.getClass());

		//Check that the del frac for the 'turned off' reaches is zero
		assertEquals(0d, deliveryFrac.getDouble(pdTranportOffAbove9681.getTopo().getRowForId(9619L)), COMP_ERROR);
		assertEquals(0d, deliveryFrac.getDouble(pdTranportOffAbove9681.getTopo().getRowForId(9100L)), COMP_ERROR);
	}


	@Test
	public void testFracFactoryTo9674_ShoreReachDataSet() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9674L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(
			pdShoreReachAbove9681,
			targetList
			);
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();

		ReachRowValueMap delHash = hashAction.run();

		delAction.setPredictData(pdShoreReachAbove9681);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();


		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;

		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracTo9674, pdShoreReachAbove9681, r);
			double actualFrac = deliveryFrac.getDouble(r);

			if (expectedFrac != null) {
				inSheet++;

				if (Math.abs(expectedFrac - actualFrac) < COMP_ERROR) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracTo9674, pdShoreReachAbove9681, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracTo9674, pdShoreReachAbove9681, r, expectedFrac, actualFrac);
				}
			}
		}

		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);

		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);

		assertEquals(SparseDoubleColumnData.class, deliveryFrac.getClass());

		//Check that the del frac for the 'turned off' reaches is zero
		assertEquals(0d, deliveryFrac.getDouble(pdShoreReachAbove9681.getTopo().getRowForId(9619L)), COMP_ERROR);
		assertEquals(0d, deliveryFrac.getDouble(pdShoreReachAbove9681.getTopo().getRowForId(9100L)), COMP_ERROR);
	}

	@Test
	public void testFracFactoryToBoth_NoTransportDataSet() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		targetList.add(9674L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(
			pdTranportOffAbove9681,
			targetList
			);
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();

		ReachRowValueMap delHash = hashAction.run();

		delAction.setPredictData(pdTranportOffAbove9681);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();

		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;

		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracToBoth, pdTranportOffAbove9681, r);
			double actualFrac = deliveryFrac.getDouble(r);

			if (expectedFrac != null) {
				inSheet++;

				if (Math.abs(expectedFrac - actualFrac) < COMP_ERROR) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracToBoth, pdTranportOffAbove9681, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracToBoth, pdTranportOffAbove9681, r, expectedFrac, actualFrac);
				}
			}
		}

		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);

		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);

		assertEquals(StandardDoubleColumnData.class, deliveryFrac.getClass());

		//Check that the del frac for the 'turned off' reaches is zero
		assertEquals(0d, deliveryFrac.getDouble(pdTranportOffAbove9681.getTopo().getRowForId(9619L)), COMP_ERROR);
		assertEquals(0d, deliveryFrac.getDouble(pdTranportOffAbove9681.getTopo().getRowForId(9100L)), COMP_ERROR);
	}


	@Test
	public void testFracFactoryToBoth_ShoreReachDataSet() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		targetList.add(9674L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(
			pdShoreReachAbove9681,
			targetList
			);
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();

		ReachRowValueMap delHash = hashAction.run();

		delAction.setPredictData(pdShoreReachAbove9681);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();

		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;

		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracToBoth, pdShoreReachAbove9681, r);
			double actualFrac = deliveryFrac.getDouble(r);

			if (expectedFrac != null) {
				inSheet++;

				if (Math.abs(expectedFrac - actualFrac) < COMP_ERROR) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracToBoth, pdShoreReachAbove9681, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracToBoth, pdShoreReachAbove9681, r, expectedFrac, actualFrac);
				}
			}
		}

		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);

		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);

		assertEquals(SparseDoubleColumnData.class, deliveryFrac.getClass());

		//Check that the del frac for the 'turned off' reaches is zero
		assertEquals(0d, deliveryFrac.getDouble(pdShoreReachAbove9681.getTopo().getRowForId(9619L)), COMP_ERROR);
		assertEquals(0d, deliveryFrac.getDouble(pdShoreReachAbove9681.getTopo().getRowForId(9100L)), COMP_ERROR);
	}

	@Test
	public void testTargetWith4UpstreamReaches() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9687L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(
			pdTranportOffAbove9681,
			targetList
			);
		CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();

		ReachRowValueMap delHash = hashAction.run();

		delAction.setPredictData(pdTranportOffAbove9681);
		delAction.setDeliveryFractionHash(delHash);
		ColumnData deliveryFrac = delAction.run();

		//Some stats
		int nonZeroCount = 0;

		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			double actualFrac = deliveryFrac.getDouble(r);

			if (actualFrac != 0d) {
				nonZeroCount++;
			}
		}

		assertEquals(
				"There should 5 non-zero values", 5, nonZeroCount);

		assertEquals(SparseDoubleColumnData.class, deliveryFrac.getClass());
	}



	@Test
	public void checkComparisonTextFiles() throws Exception {

		//
		//standard data
		//check columns
		assertEquals(0, stdData.getColumnByName("IDENTIFIER").intValue());
		assertEquals(7, stdData.getColumnByName("IFTRAN").intValue());
		//Row 0
		assertEquals(9674, stdData.getInt(0, 0).intValue());
		assertEquals(0d, stdData.getInt(0, 7).doubleValue(), COMP_ERROR);
		//Row 24 (the last)
		assertEquals(658420, stdData.getInt(24, 0).intValue());
		assertEquals(1d, stdData.getInt(24, 7).doubleValue(), COMP_ERROR);

		//
		//delivery to 9682 data
		//check columns
		assertEquals(0, stdDelFracTo9682.getColumnByName("IDENTIFIER").intValue());
		assertEquals(8, stdDelFracTo9682.getColumnByName("DEL_FRAC").intValue());
		//Row 0
		assertEquals(9682, stdDelFracTo9682.getInt(0, 0).intValue());
		assertEquals(1d, stdDelFracTo9682.getDouble(0, 8).doubleValue(), COMP_ERROR);
		//Row 16 (the last)
		assertEquals(9681, stdDelFracTo9682.getInt(16, 0).intValue());
		assertEquals(0.983450541889348d, stdDelFracTo9682.getDouble(16, 8).doubleValue(), COMP_ERROR);

		//
		//delivery to 9674 data
		//check columns
		assertEquals(0, stdDelFracTo9674.getColumnByName("IDENTIFIER").intValue());
		assertEquals(8, stdDelFracTo9674.getColumnByName("DEL_FRAC").intValue());
		//Row 0
		assertEquals(9674, stdDelFracTo9674.getInt(0, 0).intValue());
		assertEquals(1d, stdDelFracTo9674.getDouble(0, 8).doubleValue(), COMP_ERROR);
		//Row 7 (the last)
		assertEquals(9679, stdDelFracTo9674.getInt(7, 0).intValue());
		assertEquals(0.844398102645677d, stdDelFracTo9674.getDouble(7, 8).doubleValue(), COMP_ERROR);
	}

	//Using a shore reach seems to throw the application into a loop
	@Test
	public void testShorelineReachInfiniteLoopCalculationError() throws Exception {
		//Create a terminalReach list containing the reach they all drain to:  7571
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(81140L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(
			unmodifiedPredictData,
			targetList
			);
		ReachRowValueMap delHash = hashAction.run();

		assertEquals(1, delHash.size());


		//CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();
	}

	@Test
	public void testShorelinePlusUpstreamReachDelFracCalculationErrorBasedOnAnnsData() throws Exception {
		//Create a terminalReach list containing the reach they all drain to:  7571
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(6194L);
		targetList.add(81045L);

		CalcDeliveryFractionMap hashAction = new CalcDeliveryFractionMap(
			unmodifiedPredictData,
			targetList
			);
		ReachRowValueMap delHash = hashAction.run();

		for (Entry<Integer, Float> entry : delHash.entrySet()) {
			assertTrue(entry.getValue() <= 1F);
		}


		//CalcDeliveryFractionColumnData delAction = new CalcDeliveryFractionColumnData();
	}


	protected void debugComparisons(int match, int noMatch, int inSheet, int notInSheet, int expectedZeroNoMatch) {
		log.debug("Delivery to reach ID 9682 results: (" + (noMatch + expectedZeroNoMatch) + " bad values)");
		log.debug("** " +  inSheet + " rows w/ IDs matching the validation data, "
				+ match + "/" + noMatch + " matching/non-matching values.");
		log.debug("** " + notInSheet + " rows where in the model, but not in the validation data.");
		log.debug("** Of those, " +  expectedZeroNoMatch + " had non-zero values (should be 0).");
	}

	protected Double getSpreadsheetDelFracForPredictDataRow(
			DataTable spreadsheet, PredictData predictData, int predictDataRow) {

		//This is to work around a bug where the mutable version of the datatables
		//can only search on identical datatypes, thus, the row ID must be a
		//double.
		Double rowId = predictData.getTopo().getIdForRow(predictDataRow).doubleValue();
		int ssRow = spreadsheet.findFirst(0, rowId);

		if (ssRow > -1) {
			return spreadsheet.getDouble(ssRow, 8);
		} else {
			return null;
		}
	}

	protected void writeBadMatch(DataTable spreadsheet, PredictData predictData,
			int predictDataRow, Double expectedDelFrac, Double actualDelFrac) {

		log.debug("-- Comp Fail --");
		log.debug("Row in Predict Data: " + predictDataRow);
		log.debug("rowId: " + predictData.getTopo().getIdForRow(predictDataRow));
		log.debug("Expected vs Actual: " + expectedDelFrac + " / " + actualDelFrac);
	}

}

