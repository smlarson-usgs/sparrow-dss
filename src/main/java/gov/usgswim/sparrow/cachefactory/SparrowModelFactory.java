package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadSparrowModels;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public class SparrowModelFactory implements CacheEntryFactory {

	@Override
	public Object createEntry(Object modelRequestCacheKey) throws Exception {
		ModelRequestCacheKey key = (ModelRequestCacheKey) modelRequestCacheKey;
		Object result = new LoadSparrowModels(key).run();
		return result;
	}

}
