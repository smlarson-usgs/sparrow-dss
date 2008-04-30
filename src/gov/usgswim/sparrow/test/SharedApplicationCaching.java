package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.task.ComputableCache;
import junit.framework.TestCase;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

public class SharedApplicationCaching extends TestCase {
	
	CacheManager cacheManager;
	
	protected void setUp() throws Exception {
		super.setUp();
		cacheManager = CacheManager.getInstance();
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		
		cacheManager.clearAll();
	}
	
	public void testBasic() {
		SharedApplication sa = SharedApplication.getInstance();
		Cache c = CacheManager.getInstance().getCache(SharedApplication.SERIALIZABLE_CACHE);
		
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
	    // TODO Auto-generated catch block
    	System.out.println("**** Sleep interupted *****");
    }
		
		//Retrieve numbers 1 - 10000
		for (int i = 1; i < 10000; i++) {
	    assertEquals(new Long(i), sa.getSerializable(new Integer(i)));
    }
		
		
		//System.out.println(c.toString());
		//System.out.println(c.getStatistics().toString());
		
	}
	
	public void testPredictDataCache() throws Exception {
		SharedApplication sa = SharedApplication.getInstance();
		Cache c = CacheManager.getInstance().getCache(SharedApplication.PREDICT_DATA_CACHE);
		
		PredictData pdEHCache = sa.getPredictData(22L);
		ComputableCache<Long, PredictData> pdCache = sa.getPredictDatasetCache();
		PredictData pdCustomCache = pdCache.compute(22L);
		
		doFullCompare(pdCustomCache, pdEHCache);
		
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
				System.out.println("id: " + expect.getTopo().getIdForRow(row));
				System.out.println("expected: " + expect.getTopo().getValue(row, i));
				System.out.println("found: " + data.getTopo().getValue(row, i));
			}
			
		}

}
