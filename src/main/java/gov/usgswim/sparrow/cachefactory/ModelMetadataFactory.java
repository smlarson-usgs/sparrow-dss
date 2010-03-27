package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadModelMetadata;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public class ModelMetadataFactory implements CacheEntryFactory {

	@Override
	public Object createEntry(Object modelRequestCacheKey) throws Exception {
		ModelRequestCacheKey key = (ModelRequestCacheKey) modelRequestCacheKey;
		Object result = new LoadModelMetadata(key).run();
		return result;
	}

}
