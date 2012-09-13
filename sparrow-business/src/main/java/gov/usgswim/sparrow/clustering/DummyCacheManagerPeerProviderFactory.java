package gov.usgswim.sparrow.clustering;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

import net.sf.ehcache.CacheException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.distribution.CacheManagerPeerProvider;
import net.sf.ehcache.distribution.CacheManagerPeerProviderFactory;

public class DummyCacheManagerPeerProviderFactory extends
		CacheManagerPeerProviderFactory {

	@Override
	public CacheManagerPeerProvider createCachePeerProvider(
			CacheManager cacheManager, Properties properties) {
		return new DummyCacheManagerPeerProvider();
	}

	public static class DummyCacheManagerPeerProvider implements CacheManagerPeerProvider {

		@Override
		public void dispose() throws CacheException {
			// do nothing

		}

		@Override
		public String getScheme() {
			return "";
		}

		@Override
		public long getTimeForClusterToForm() {
			return 1;
		}

		@Override
		public void init() {
			// do nothing
		}

		@Override
		public List listRemoteCachePeers(Ehcache cache) throws CacheException {
			return Collections.EMPTY_LIST;
		}

		@Override
		public void registerPeer(String nodeId) {
			//do nothing
		}

		@Override
		public void unregisterPeer(String nodeId) {
			//do nothing
		}

	}
}
