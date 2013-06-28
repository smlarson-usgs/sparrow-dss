package gov.usgswim.sparrow.clustering;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Status;
import net.sf.ehcache.distribution.CacheManagerPeerListener;
import net.sf.ehcache.distribution.CacheManagerPeerListenerFactory;

public class DummyCacheManagerPeerListenerFactory extends
		CacheManagerPeerListenerFactory {

	@Override
	public CacheManagerPeerListener createCachePeerListener(
			CacheManager cacheManager, Properties properties) {
		return new DummyCacheManagerPeerListener();
	}
	
	public static class DummyCacheManagerPeerListener implements CacheManagerPeerListener {

		@Override
		public void attemptResolutionOfUniqueResourceConflict()
				throws IllegalStateException, CacheException {
			// do nothing
		}

		@Override
		public List getBoundCachePeers() {
			return Collections.EMPTY_LIST;
		}

		@Override
		public String getScheme() {
			return "";
		}

		@Override
		public String getUniqueResourceIdentifier() {
			return "";
		}

		@Override
		public void dispose() throws CacheException {
			// do nothing
		}

		@Override
		public Status getStatus() {
			return Status.STATUS_SHUTDOWN;
		}

		@Override
		public void init() throws CacheException {
			// do nothing
		}

		@Override
		public void notifyCacheAdded(String cacheName) {
			// do nothing
		}

		@Override
		public void notifyCacheRemoved(String cacheName) {
			// do nothing
		}

	}
}
