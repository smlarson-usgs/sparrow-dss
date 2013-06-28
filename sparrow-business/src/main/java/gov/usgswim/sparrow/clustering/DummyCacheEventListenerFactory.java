package gov.usgswim.sparrow.clustering;

import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.event.CacheEventListener;
import net.sf.ehcache.event.CacheEventListenerFactory;

public class DummyCacheEventListenerFactory extends CacheEventListenerFactory {

	@Override
	public CacheEventListener createCacheEventListener(Properties properties) {
		return new DummyCacheEventListener();
	}
	
	public static class DummyCacheEventListener implements CacheEventListener{

		@Override
		 public Object clone() throws CloneNotSupportedException {
			return this;
		}

		@Override
		public void dispose() {
			// do nothing
		}

		@Override
		public void notifyElementEvicted(Ehcache cache, Element element) {
			// do nothing
		}

		@Override
		public void notifyElementExpired(Ehcache cache, Element element) {
			// do nothing
		}

		@Override
		public void notifyElementPut(Ehcache cache, Element element)
				throws CacheException {
			// do nothing
		}

		@Override
		public void notifyElementRemoved(Ehcache cache, Element element)
				throws CacheException {
			// do nothing
		}

		@Override
		public void notifyElementUpdated(Ehcache cache, Element element)
				throws CacheException {
			// do nothing
		}

		@Override
		public void notifyRemoveAll(Ehcache cache) {
			// do nothing
		}
		
	}

}
