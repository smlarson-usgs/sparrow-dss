package gov.usgswim.sparrow.test;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ComparePercentageView;
import gov.usgswim.sparrow.AdjustmentSet;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.PredictResult;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.TabDelimFileUtil;

import java.io.InputStream;

import junit.framework.TestCase;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;

public class SharedApplicationCaching extends TestCase {
	
	LifecycleListener lifecycle = new LifecycleListener();
	
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null);
	}

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
	 */
	public void testAcrossRestart() {
		SharedApplication sa = SharedApplication.getInstance();
		CacheManager.getInstance().clearAll();
		Ehcache c = CacheManager.getInstance().getEhcache(SharedApplication.SERIALIZABLE_CACHE);
		
		//change to 1 second for disk eviction

		//Load numbers 1 - 10000
		for (int i = 1; i < 210; i++) {
	    sa.putSerializable(new Long(i));
    }
		
		//restarting the cache
		lifecycle.contextDestroyed(null);
		lifecycle.contextInitialized(null);
		
		//Retrieve numbers 1 - 10000
		for (int i = 1; i < 210; i++) {
	    assertEquals(new Long(i), sa.getSerializable(new Integer(i)));
    }
		
	}
	
	/*
	public void testPredictDataCache() throws Exception {
		SharedApplication sa = SharedApplication.getInstance();
		
		PredictData pdEHCache = sa.getPredictData(22L);
		ComputableCache<Long, PredictData> pdCache = sa.getPredictDatasetCache();
		PredictData pdCustomCache = pdCache.compute(22L);
		
		doFullCompare(pdCustomCache, pdEHCache);
		
	}
	*/
	
	/**
	 * Compares the calculated prediction results of the cache (via the PredictResultFactory)
	 * to the canned results from a file.
	 */
	public void testBasicPredictionValues() throws Exception {
		SharedApplication sa = SharedApplication.getInstance();
		CacheManager.getInstance().clearAll();
		PredictRequest pr = new PredictRequest(1L, AdjustmentSet.EMPTY_ADJUSTMENTSET);
		
		
		//ComputableCache<PredictRequest, PredictResult> pdCache = SharedApplication.getInstance().getPredictResultCache();
		//PredictResult orgResult = pdCache.compute(pr);
		
		DataTable result = sa.getPredictResult(pr);
		
		ComparePercentageView comp = buildPredictionComparison(result);

		for (int i = 0; i < comp.getColumnCount(); i++)  {
			System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
		}

		assertEquals(0d, comp.findMaxCompareValue(), 0.004d);
		

	}

	

	@SuppressWarnings("deprecation")
  public void doFullCompare(PredictData expect, PredictData data) throws Exception {

			DataTableCompare comp = null;	//used for all comparisons

			comp = new DataTableCompare(expect.getCoef(), data.getCoef());
			assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

			comp = new DataTableCompare(expect.getDecay(), data.getDecay());
			assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

			comp = new DataTableCompare(expect.getSrc(), data.getSrc());
			assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

			comp = new DataTableCompare(expect.getSrcIds(), data.getSrcIds());
			assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

			comp = new DataTableCompare(expect.getSys(), data.getSys());
			assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);

			comp = new DataTableCompare(expect.getTopo(), data.getTopo());
			assertEquals(0d, comp.findMaxCompareValue(), 0.000000000000001d);
			for (int i = 0; i < comp.getColumnCount(); i++)  {
				System.out.println("col " + i + " error: " + comp.findMaxCompareValue(i));
				int row = comp.findMaxCompareRow(i);
//				System.out.println("id: " + expect.getTopo().getIdForRow(row));
				System.out.println("expected: " + expect.getTopo().getValue(row, i));
				System.out.println("found: " + data.getTopo().getValue(row, i));
			}
			
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
