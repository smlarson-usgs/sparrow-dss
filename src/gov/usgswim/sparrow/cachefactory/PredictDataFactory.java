package gov.usgswim.sparrow.cachefactory;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DataLoader;

public class PredictDataFactory implements CacheEntryFactory {

	public Object createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		
		return DataLoader.loadMinimalPredictDataSet(SharedApplication.getInstance().getConnection(), id.intValue()).toImmutable();
	}

}
