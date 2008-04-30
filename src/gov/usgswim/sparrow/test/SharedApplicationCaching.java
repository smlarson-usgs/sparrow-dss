package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.service.SharedApplication;
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
	
	public void test1() {
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

}
