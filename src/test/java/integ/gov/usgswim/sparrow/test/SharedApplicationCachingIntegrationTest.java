package gov.usgswim.sparrow.test;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ComparePercentageView;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;

public class SharedApplicationCachingIntegrationTest extends TestCase {

	LifecycleListener lifecycle = new LifecycleListener();

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null);
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();

		lifecycle.contextDestroyed(null);
	}

	/*
	public void testBasic() {
		SharedApplication sa = SharedApplication.getInstance();
		CacheManager.getInstance().clearAll();
		Ehcache c = CacheManager.getInstance().getEhcache(SharedApplication.SERIALIZABLE_CACHE);

		//change to 1 second for disk eviction
		c.getCacheConfiguration().setDiskExpiryThreadIntervalSeconds(1);

		//Load numbers 1 - 10000
		for (int i = 1; i < 10000; i++) {
	    sa.putSerializable(new Long(i));
    }

		//Retrieve numbers 1 - 10000
		for (int i = 1; i < 10000; i++) {
	    assertEquals(new Long(i), sa.getSerializable(new Integer(i)));
    }


		//System.out.println(c.toString());
		//System.out.println(c.getStatistics().toString());

		try {
	    Thread.sleep(1200);
    } catch (InterruptedException e) {
    	System.out.println("**** Sleep interupted *****");
    }

		//Retrieve numbers 1 - 10000
		for (int i = 1; i < 10000; i++) {
	    assertEquals(new Long(i), sa.getSerializable(new Integer(i)));
    }


		//System.out.println(c.toString());
		//System.out.println(c.getStatistics().toString());

	}

	 */

	/**
	 * Do we keep items in cache across a restart?
	 * NOT CURRENTLY WORKING
	 */
//	public void testAcrossRestart() {
//		SharedApplication sa = SharedApplication.getInstance();
//		CacheManager.getInstance().clearAll();
//		@SuppressWarnings("unused")
//		Ehcache c = CacheManager.getInstance().getEhcache(SharedApplication.SERIALIZABLE_CACHE);
//
//		//change to 1 second for disk eviction
//
//		//Load numbers 1 - 10000
//		for (int i = 1; i < 210; i++) {
//			sa.putSerializable(new Long(i));
//		}
//
//		//restarting the cache
//		lifecycle.contextDestroyed(null);
//		lifecycle.contextInitialized(null);
//
//		//Retrieve numbers 1 - 10000
//		for (int i = 1; i < 210; i++) {
//			assertEquals(new Long(i), sa.getSerializable(new Integer(i)));
//		}
//
//	}




	/**
	 * Compares the calculated prediction results of the cache (via the PredictResultFactory)
	 * to the canned results from a file.
	 */
	public void testBasicPredictionValues() throws Exception {
		SharedApplication sa = SharedApplication.getInstance();
		CacheManager.getInstance().clearAll();
		PredictionContext context = new PredictionContext(21L, null, null, null, null, null);

		DataTable result = sa.getPredictResult(context);
		ComparePercentageView comp = buildPredictionComparison(result);

		for (int i = 0; i < comp.getColumnCount(); i++)  {
			System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
		}

		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);
	}


	public void doFullCompare(PredictData expect, PredictData data) throws Exception {

		DataTableCompare comp = null;	//used for all comparisons

		comp = new DataTableCompare(expect.getCoef(), data.getCoef(), true);
		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);

		comp = new DataTableCompare(expect.getDelivery(), data.getDelivery(), true);
		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);

		comp = new DataTableCompare(expect.getSrc(), data.getSrc(), true);
		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);

		// This test no longer applicable as getSrcMetadata returns primarily text columns
//		comp = new DataTableCompare(expect.getSrcMetadata(), data.getSrcMetadata(), true);
//		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);


		comp = new DataTableCompare(expect.getTopo(), data.getTopo(), true);
		assertEquals(0d, comp.getMaxDouble(), 0.000000000000001d);
		for (int i = 0; i < comp.getColumnCount(); i++)  {
			System.out.println("col " + i + " error: " + comp.getMaxDouble(i));
			int row = comp.getMaxInt(i);
//			System.out.println("id: " + expect.getTopo().getIdForRow(row));
			System.out.println("expected: " + expect.getTopo().getValue(row, i));
			System.out.println("found: " + data.getTopo().getValue(row, i));
		}

	}

	protected ComparePercentageView buildPredictionComparisonOld(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};

		ComparePercentageView comp = new ComparePercentageView(toBeCompared, data, DEFAULT_COMP_COLUMN_MAP, false);

		return comp;
	}

	protected ComparePercentageView buildPredictionComparison(DataTable toBeCompared) throws Exception {
		InputStream fileStream = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict.txt");
		DataTable data = TabDelimFileUtil.readAsDouble(fileStream, true, -1);
		int[] DEFAULT_COMP_COLUMN_MAP =
			new int[] {40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 39, 15};

		ComparePercentageView comp = new ComparePercentageView(toBeCompared, data, DEFAULT_COMP_COLUMN_MAP, false);

		return comp;
	}

}
