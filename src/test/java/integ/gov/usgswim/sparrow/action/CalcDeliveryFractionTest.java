package gov.usgswim.sparrow.action;

import static org.junit.Assert.assertEquals;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.datatable.impl.SparseDoubleColumnData;
import gov.usgswim.datatable.impl.StandardDoubleColumnData;
import gov.usgswim.sparrow.DeliveryRunner;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.BeforeClass;
import org.junit.Test;

/**
 * This test was created to recreate a calc error where delivery based calcs
 * (like total delivered flux) were resulting in mapped values identical
 * to the base data (i.e. total flux).
 * 
 * @author eeverman
 * TODO: This should really use a canned project, rather than MRB2
 */
public class CalcDeliveryFractionTest extends SparrowDBTest {
	
	static PredictData unmodifiedPredictData;
	static PredictData predictData;
	
	static DataTable stdData;
	static DataTable stdDelFracTo9682;
	static DataTable stdDelFracTo9674;
	static DataTable stdDelFracToBoth;
	
	@Override
	public void doSetup() throws Exception {
		
		InputStream baseDataStream = SparrowUnitTest.getResource(CalcDeliveryFractionTest.class, "data", "tab");
		stdData = TabDelimFileUtil.readAsDouble(baseDataStream, true, -1);
		
		InputStream stdDelFracTo9682Stream = SparrowUnitTest.getResource(CalcDeliveryFractionTest.class, "stdDelFracTo9682", "tab");
		stdDelFracTo9682 = TabDelimFileUtil.readAsDouble(stdDelFracTo9682Stream, true, -1);
		
		InputStream stdDelFracTo9674Stream = SparrowUnitTest.getResource(CalcDeliveryFractionTest.class, "stdDelFracTo9674", "tab");
		stdDelFracTo9674 = TabDelimFileUtil.readAsDouble(stdDelFracTo9674Stream, true, -1);
		
		InputStream stdDelFracToBothStream = SparrowUnitTest.getResource(CalcDeliveryFractionTest.class, "stdDelFracToBoth", "tab");
		stdDelFracToBoth = TabDelimFileUtil.readAsDouble(stdDelFracToBothStream, true, -1);
		
		//Lets hack the predictData to Turn off transport for reach ID 9681
		unmodifiedPredictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = unmodifiedPredictData.getTopo();
		SparseOverrideAdjustment adjTopo = new SparseOverrideAdjustment(topo);
		adjTopo.setValue(0d, unmodifiedPredictData.getRowForReachID(9681), PredictData.IFTRAN_COL);
		predictData = new PredictDataImm(
				adjTopo, unmodifiedPredictData.getCoef(),
				unmodifiedPredictData.getSrc(),
				unmodifiedPredictData.getSrcMetadata(),
				unmodifiedPredictData.getDelivery(),
				unmodifiedPredictData.getModel());
		
	}
	
	//TODO:  THis is really obsolete, just need to yank the other delivery out.
	@Test
	public void testComparison() throws Exception {
		
		
		List<Long> targets = new ArrayList<Long>();
		targets.add(9682L);
		Set<Long> targetSet = new HashSet<Long>();
		targetSet.addAll(targets);
		
		DeliveryRunner dr = new DeliveryRunner(predictData);
		DataTable dataTableOld = dr.calculateReachTransportFractionDataTable(targetSet);
		
		CalcDeliveryFraction action = new CalcDeliveryFraction();
		action.setPredictData(predictData);
		action.setTargetReachIds(targetSet);
		ColumnData deliveryFracNew = action.run();
		
		SimpleDataTable dataTableNew = new SimpleDataTable(
			new ColumnData[] {deliveryFracNew}, "new", "new", null, null
		);
		
		DataTableCompare compare = new DataTableCompare(dataTableOld, dataTableNew, true);
		
		int match = 0;
		int noMatch = 0;
		for (int r=0; r<compare.getRowCount(); r++) {
			if (compare.getDouble(r, 0).equals(0d)) {
				match++;
			} else {
				noMatch++;
			}
		}
		
		log.debug("Old vs New Comparison: Matches: " + match + ", non-matching: " + noMatch);
	}
	
	@Test
	public void testFracFactoryTo9682() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		CalcDeliveryFraction action = new CalcDeliveryFraction();
		action.setPredictData(predictData);
		action.setTargetReachIds(targets.asSet());
		ColumnData deliveryFrac = action.run();
		
		//check metadata of delivery fraction
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.delivered_fraction, false), deliveryFrac.getName());
		assertEquals(Action.getDataSeriesProperty(DataSeriesType.delivered_fraction, true), deliveryFrac.getDescription());
		assertEquals(SparrowUnits.FRACTION.getUserName(), deliveryFrac.getUnits());
		assertEquals(unmodifiedPredictData.getModel().getConstituent(), deliveryFrac.getProperty(TableProperties.CONSTITUENT.getPublicName()));

		
		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;
		
		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracTo9682, predictData, r);
			double actualFrac = deliveryFrac.getDouble(r);
			
			if (expectedFrac != null) {
				inSheet++;
				
				if (Math.abs(expectedFrac - actualFrac) < .0000000001d) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracTo9682, predictData, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracTo9682, predictData, r, expectedFrac, actualFrac);
				}
			}
		}
		
		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);
		
		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);
		
		assertEquals(StandardDoubleColumnData.class, deliveryFrac.getClass());
		
	}
	
	@Test
	public void testFracFactoryTo9674() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9674L);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		CalcDeliveryFraction action = new CalcDeliveryFraction();
		action.setPredictData(predictData);
		action.setTargetReachIds(targets.asSet());
		ColumnData deliveryFrac = action.run();
		

		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;
		
		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracTo9674, predictData, r);
			double actualFrac = deliveryFrac.getDouble(r);
			
			if (expectedFrac != null) {
				inSheet++;
				
				if (Math.abs(expectedFrac - actualFrac) < .0000000001d) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracTo9674, predictData, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracTo9674, predictData, r, expectedFrac, actualFrac);
				}
			}
		}
		
		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);
		
		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);
		
		assertEquals(StandardDoubleColumnData.class, deliveryFrac.getClass());
	}
	
	@Test
	public void testFracFactoryToBoth() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9682L);
		targetList.add(9674L);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		CalcDeliveryFraction action = new CalcDeliveryFraction();
		action.setPredictData(predictData);
		action.setTargetReachIds(targets.asSet());
		ColumnData deliveryFrac = action.run();
		
		//Some stats
		int inSheet = 0;
		int notInSheet = 0;
		int match = 0;
		int noMatch = 0;
		int expectedZeroNoMatch = 0;
		
		for (int r=0; r<deliveryFrac.getRowCount(); r++) {
			Double expectedFrac = getSpreadsheetDelFracForPredictDataRow(
					stdDelFracToBoth, predictData, r);
			double actualFrac = deliveryFrac.getDouble(r);
			
			if (expectedFrac != null) {
				inSheet++;
				
				if (Math.abs(expectedFrac - actualFrac) < .0000000001d) {
					match++;
				} else {
					noMatch++;
					writeBadMatch(stdDelFracToBoth, predictData, r, expectedFrac, actualFrac);
				}
			} else {
				notInSheet++;
				if (actualFrac != 0d) {
					expectedZeroNoMatch++;
					writeBadMatch(stdDelFracToBoth, predictData, r, expectedFrac, actualFrac);
				}
			}
		}
		
		debugComparisons(match, noMatch, inSheet, notInSheet, expectedZeroNoMatch);
		
		assertEquals(
				"There should not be any delivery fractions " +
				"(expected vs actual) which do not match", 0, noMatch + expectedZeroNoMatch);
		
		assertEquals(StandardDoubleColumnData.class, deliveryFrac.getClass());
	}
	
	@Test
	public void testTargetWith4UpstreamReaches() throws Exception {
		List<Long> targetList = new ArrayList<Long>();
		targetList.add(9687L);
		TerminalReaches targets = new TerminalReaches(TEST_MODEL_ID, targetList);
		
		CalcDeliveryFraction action = new CalcDeliveryFraction();
		action.setPredictData(predictData);
		action.setTargetReachIds(targets.asSet());
		ColumnData deliveryFrac = action.run();
		
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
		assertEquals(1d, stdData.getInt(0, 7).doubleValue(), .00000001d);
		//Row 24 (the last)
		assertEquals(658420, stdData.getInt(24, 0).intValue());
		assertEquals(1d, stdData.getInt(24, 7).doubleValue(), .00000001d);
		
		//
		//delivery to 9682 data
		//check columns
		System.out.println("--" + stdDelFracTo9682.getName(0) + "--");
		assertEquals(0, stdDelFracTo9682.getColumnByName("IDENTIFIER").intValue());
		assertEquals(8, stdDelFracTo9682.getColumnByName("DEL_FRAC").intValue());
		//Row 0
		assertEquals(9682, stdDelFracTo9682.getInt(0, 0).intValue());
		assertEquals(1d, stdDelFracTo9682.getDouble(0, 8).doubleValue(), .00000001d);
		//Row 16 (the last)
		assertEquals(9681, stdDelFracTo9682.getInt(16, 0).intValue());
		assertEquals(0.983450541889348d, stdDelFracTo9682.getDouble(16, 8).doubleValue(), .00000001d);
		
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

