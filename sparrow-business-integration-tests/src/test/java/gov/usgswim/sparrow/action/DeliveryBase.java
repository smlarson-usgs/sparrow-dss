package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.test.SparrowTestBase;
import static gov.usgswim.sparrow.service.ConfiguredCache.*;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgs.cida.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.*;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.service.ConfiguredCache;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.junit.Test;


/**
 * This is a base class for doing delivery based tests.
 *
 * There is one 'hole' in this setup.  To save a bit of work, we did not
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
public class DeliveryBase  extends SparrowTestBaseWithDBandCannedModel50 {

	protected static PredictData unmodifiedPredictData;
	protected static PredictData modifiedPredictData;

	protected static DataTable stdDelFracTo9682;
	protected static DataTable stdDelFracTo9674;
	protected static DataTable stdDelFracToBoth;

	protected static TerminalReaches target9682;
	protected static TerminalReaches target9674;
	protected static TerminalReaches targetBoth;

	@Override
	public void doBeforeClassSingleInstanceSetup() throws Exception {


		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
		super.doBeforeClassSingleInstanceSetup();

		BufferedReader stdDelFracTo9682Stream = new BufferedReader(new InputStreamReader(
				SparrowTestBase.getResource(DeliveryBase.class, "stdDelFracTo9682", "tab")
			));
		stdDelFracTo9682 = TabDelimFileUtil.readAsDouble(stdDelFracTo9682Stream, true, -1);

		BufferedReader stdDelFracTo9674Stream = new BufferedReader(new InputStreamReader(
				SparrowTestBase.getResource(DeliveryBase.class, "stdDelFracTo9674", "tab")
			));
		stdDelFracTo9674 = TabDelimFileUtil.readAsDouble(stdDelFracTo9674Stream, true, -1);

		BufferedReader stdDelFracToBothStream = new BufferedReader(new InputStreamReader(
				SparrowTestBase.getResource(DeliveryBase.class, "stdDelFracToBoth", "tab")
			));
		stdDelFracToBoth = TabDelimFileUtil.readAsDouble(stdDelFracToBothStream, true, -1);

		//Lets hack the predictData to Turn off transport for the two
		//reaches above reach 9681
		unmodifiedPredictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		DataTable topo = unmodifiedPredictData.getTopo();
		SparseOverrideAdjustment adjTopo = new SparseOverrideAdjustment(topo);
		adjTopo.setValue(0d, unmodifiedPredictData.getRowForReachID(9619), gov.usgswim.sparrow.PredictData.TOPO_IFTRAN_COL);
		adjTopo.setValue(0d, unmodifiedPredictData.getRowForReachID(9100), gov.usgswim.sparrow.PredictData.TOPO_IFTRAN_COL);


		modifiedPredictData = new PredictDataImm(
				new TopoDataComposit(adjTopo), unmodifiedPredictData.getCoef(),
				unmodifiedPredictData.getSrc(),
				unmodifiedPredictData.getSrcMetadata(),
				unmodifiedPredictData.getDelivery(),
				unmodifiedPredictData.getModel());

		//Targets set 1
		List<String> targetList = new ArrayList<String>();
		targetList.add("9682");
		target9682 = new TerminalReaches(TEST_MODEL_ID, targetList);

		//Targets set 2
		targetList = new ArrayList<String>();
		targetList.add("9674");
		target9674 = new TerminalReaches(TEST_MODEL_ID, targetList);

		//Targets set for both
		targetList = new ArrayList<String>();
		targetList.add("9682");
		targetList.add("9674");
		targetBoth = new TerminalReaches(TEST_MODEL_ID, targetList);

		ConfiguredCache.TerminalReaches.put(target9682.getId(), target9682);
		ConfiguredCache.TerminalReaches.put(target9682.getId(), target9674);
		ConfiguredCache.TerminalReaches.put(target9682.getId(), targetBoth);
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
				SparrowColumnSpecifier result, PredictData predictData, boolean missingDataShouldBeZero) {

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

	/**
	 * Switches the values in the cache to reflect the unmodified prediction context.
	 * i.e., it puts the standard model 50 into the cache.
	 */
	protected void switchToUnmodifiedPredictData() {
			Ehcache pdc = SparrowCacheManager.getInstance().getEhcache(PredictData.name());
			Element e  = new Element(TEST_MODEL_ID, unmodifiedPredictData);
			pdc.put(e);

			SparrowCacheManager.getInstance().getEhcache(PredictResult.name()).removeAll();
			SparrowCacheManager.getInstance().getEhcache(DeliveryFraction.name()).removeAll();

			assertNull(SharedApplication.getInstance().getDeliveryFraction(target9674, true));
			assertNull(SharedApplication.getInstance().getDeliveryFraction(target9682, true));
	}

	/**
	 * Switches the predict data in the cache to the modified copy, which
	 * has transport turned off above reach 9681.
	 */
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
					" to a target reach or reaches.", null);

			debugTableRow(delivery, row, "Delivery Fraction Data");
		}
	}

	public void debugReach(PredictData pd, Long reachId) throws Exception {
		int row = pd.getRowForReachID(reachId);

		if (pd.getModel() != null && pd.getModel().getId() != null) {
			log.error("Debugging Reach " + reachId + " for model " + pd.getModel().getId());
		} else {
			log.error("Debugging Reach " + reachId + " for model [unknown model]");
		}

		debugTableRow(pd.getTopo(), row, "Topo Data");
		debugTableRow(pd.getSrc(), row, "Source Data");
		debugTableRow(pd.getCoef(), row, "Coef Data");
		debugTableRow(pd.getDelivery(), row, "Delivery Data");
	}

	public void debugTableRow(DataTable table, int row, String description) throws Exception {
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

