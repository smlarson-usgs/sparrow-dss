package gov.usgswim.sparrow.cachefactory;

import java.sql.Connection;

import net.sf.ehcache.constructs.blocking.CacheEntryFactory;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DataLoader;

/**
 * Loads data the data needed to run predictions for a SPARROW model.
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
public class PredictDataFactory implements CacheEntryFactory {

	public Object createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		PredictData result = null;
		Connection conn = SharedApplication.getInstance().getConnection();
		
		try {
			result = DataLoader.loadMinimalPredictDataSet(conn, id.intValue()).toImmutable();
		} finally {
			SharedApplication.closeConnection(conn, null);
		}
		
		return result;
	}

}
