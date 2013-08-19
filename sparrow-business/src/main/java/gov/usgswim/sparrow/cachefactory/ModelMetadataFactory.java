package gov.usgswim.sparrow.cachefactory;

import java.util.List;

import gov.usgswim.sparrow.action.LoadModelMetadata;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.request.ModelRequestCacheKey;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

public class ModelMetadataFactory implements CacheEntryFactory {

	@Override
	public List<SparrowModel> createEntry(Object modelRequestCacheKey) throws Exception {
		ModelRequestCacheKey key = (ModelRequestCacheKey) modelRequestCacheKey;
		List<SparrowModel> result = new LoadModelMetadata(key).run();
		return result;
	}

}
