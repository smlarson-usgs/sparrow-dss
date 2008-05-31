package gov.usgswim.sparrow.cachefactory;

import org.apache.log4j.Logger;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import gov.usgs.webservices.framework.utils.TemporaryHelper;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictComputable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.PredictRequest;
import gov.usgswim.sparrow.PredictRunner;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.task.ComputableCache;

/**
 * This factory class creates adjusted source values on demand for an EHCache.
 * 
 * When the cache receives a get(AdjustmentGroups) call and it doesn't have a cache
 * entry for that request, the createEntry() method of this class is called
 * and the returned value is cached.
 * 
 * This class implements CacheEntryFactory, which plugs into the caching system
 * so that the createEntry() method is only called when a entry needs to be
 * created/loaded.
 * 
 * Caching, blocking, and de-caching are all handled by the caching system, so
 * that this factory class only needs to worry about building a new entity in
 * (what it can consider) a single thread environment.
 * 
 * @author eeverman
 *
 */
public class AdjustedSourceFactory implements CacheEntryFactory {
	
	public Object createEntry(Object adjustmentGroups) throws Exception {
		AdjustmentGroups groups = (AdjustmentGroups)adjustmentGroups;
		
		PredictData data = SharedApplication.getInstance().getPredictData(groups.getModelID());
		DataTable src = groups.adjust(data);

		return src.toImmutable();
	}

}
