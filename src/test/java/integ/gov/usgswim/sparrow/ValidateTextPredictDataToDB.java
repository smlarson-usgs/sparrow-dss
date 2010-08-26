package gov.usgswim.sparrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.datatable.filter.ColumnRangeFilter;
import gov.usgswim.datatable.filter.FilteredDataTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataImm;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.SparrowUnitTest;
import gov.usgswim.sparrow.action.LoadModelPredictDataFromFile;
import gov.usgswim.sparrow.clustering.SparrowCacheManager;
import gov.usgswim.sparrow.datatable.DataTableCompare;
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

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.ObjectUtils;
import org.apache.log4j.Level;
import org.junit.BeforeClass;
import org.junit.Test;
import static gov.usgswim.sparrow.service.ConfiguredCache.*;


/**
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
public class ValidateTextPredictDataToDB  extends SparrowDBTest {
	
	private static PredictData dbPredictData;
	private static PredictData txtPredictData;

	
	@Override
	public void doSetup() throws Exception {
		
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
		
		LoadModelPredictDataFromFile action = new LoadModelPredictDataFromFile(TEST_MODEL_ID);

		//Lets hack the predictData to Turn off transport for reach ID 9681
		dbPredictData = SharedApplication.getInstance().getPredictData(TEST_MODEL_ID);
		txtPredictData = action.run();
	}
	
	@Test
	public void checkComparisonTextFiles() throws Exception {
		
		//////////////////////////////////////////////////////
		// Check the source metadata
		//////////////////////////////////////////////////////
		DataTable dbSrcMeta = dbPredictData.getSrcMetadata();
		DataTable txtSrcMeta = txtPredictData.getSrcMetadata();
		
		//We don't really care about the db id column, so we're stripping it out.
		ColumnRangeFilter dbColFilter = new ColumnRangeFilter(1, dbSrcMeta.getColumnCount() - 1);
		dbSrcMeta = new FilteredDataTable(dbSrcMeta, null, dbColFilter);
		ColumnRangeFilter txtColFilter = new ColumnRangeFilter(1, txtSrcMeta.getColumnCount() - 1);
		txtSrcMeta = new FilteredDataTable(txtSrcMeta, null, txtColFilter);
		
		DataTableCompare srcMetaComp = new DataTableCompare(dbSrcMeta, txtSrcMeta, true);
		
		assertTrue(compareTables(dbSrcMeta, txtSrcMeta, .00000001d, true));
		assertTrue(Math.abs(srcMetaComp.getMaxDouble()) < .00000001d);
		
		//////////////////////////////////////////////////////
		// Check topo
		//////////////////////////////////////////////////////
		DataTable dbTopo = dbPredictData.getTopo();
		DataTable txtTopo = txtPredictData.getTopo();
		
		//We don't really care about the db id column, so we're stripping it out.
		dbColFilter = new ColumnRangeFilter(1, dbTopo.getColumnCount() - 1);
		dbTopo = new FilteredDataTable(dbTopo, null, dbColFilter);
		txtColFilter = new ColumnRangeFilter(1, txtTopo.getColumnCount() - 1);
		txtTopo = new FilteredDataTable(txtTopo, null, txtColFilter);
		
		DataTableCompare topoComp = new DataTableCompare(dbTopo, txtTopo, true);
		assertTrue(compareTables(dbTopo, txtTopo, .00000001d, true));
		assertTrue(Math.abs(topoComp.getMaxDouble()) < .00000001d);
		
		//////////////////////////////////////////////////////
		// Check delivery
		//////////////////////////////////////////////////////
		DataTable dbDel = dbPredictData.getDelivery();
		DataTable txtDel = txtPredictData.getDelivery();
		
		DataTableCompare delComp = new DataTableCompare(dbDel, txtDel, true);
		//The text delivery table has IDs, db does not.  not an issue.
		assertTrue(compareTables(dbDel, txtDel, .00000001d, false));
		assertTrue(Math.abs(delComp.getMaxDouble()) < .00000001d);
		
		//////////////////////////////////////////////////////
		// Check coef
		//////////////////////////////////////////////////////
		DataTable dbCoef = dbPredictData.getCoef();
		DataTable txtCoef = txtPredictData.getCoef();
		
		DataTableCompare delCoef = new DataTableCompare(dbCoef, txtCoef, true);
		//TODO:  The db loader is failing to get the IDs in, why?
		assertTrue(compareTables(dbCoef, txtCoef, .00000001d, false));
		assertTrue(Math.abs(delCoef.getMaxDouble()) < .00000001d);
	}
	
	/**
	 * Compares two datatables, returning true if they are equal.
	 * 
	 * Any mismatched values are logged as errors. (log.error)
	 * 
	 * @param expected
	 * @param actual
	 * @return
	 */
	public boolean compareTables(DataTable expected, DataTable actual, double tolerance, boolean checkIndex) {
		boolean match = true;
		boolean checkRowId = false;
		
		if (checkIndex) {
			if (expected.hasRowIds() && actual.hasRowIds()) {
				checkRowId = true;
			} else if (expected.hasRowIds()) {
				match = false;
				log.error("The base table has row IDs, but the actual table does not");
			} else if (actual.hasRowIds()) {
				match = false;
				log.error("The actual table has row IDs, but the expected table does not");
			}
		}
		
		for (int r = 0; r < expected.getRowCount(); r++) {
			
			if (checkRowId) {
				//Check row ID
				Long orgId = expected.getIdForRow(r);
				Long newId = actual.getIdForRow(r);
				if (! compareValues(orgId, newId, 0d)) {
					match = false;
					log.error("Mismatch row ID: " + r + ") [" + orgId + "] [" + newId + "]");
				}
			}
			
			for (int c = 0; c < expected.getColumnCount(); c++) {
				Object orgValue = expected.getValue(r, c);
				Object newValue = actual.getValue(r, c);
				
				if (! compareValues(orgValue, newValue, tolerance)) {
					match = false;
					log.error("Mismatch : " + r + "," + c + ") [" + orgValue + "] [" + newValue + "]");
				}
				//assertEquals(original.getValue(r, c), newVersion.getValue(r, c));
			}
		}
		
		return match;
	}
	
	/**
	 * Returns true if the values are considered equal.
	 * 
	 * If both values are numbers, they are compared as doubles and considered
	 * equals if their abs distance is less than the tolerance.
	 * 
	 * @param v1
	 * @param v2
	 * @param tolerance
	 * @return
	 */
	public boolean compareValues(Object v1, Object v2, double tolerance) {
		if (v1 == null && v2 == null) {
			return true;
		} else if (v1 == null || v2 == null) {
			return false;
		} else {
			if (v1 instanceof Number && v2 instanceof Number) {
				Number n1 = (Number) v1;
				Number n2 = (Number) v2;
				double diff = Math.abs(n1.doubleValue() - n2.doubleValue());
				if (Double.isInfinite(diff) || Double.isNaN(diff)) {
					return false;
				} else {
					return diff <= tolerance;
				}
				
			} else {
				return ObjectUtils.equals(v1, v2);
			}
		}
	}
	
}

