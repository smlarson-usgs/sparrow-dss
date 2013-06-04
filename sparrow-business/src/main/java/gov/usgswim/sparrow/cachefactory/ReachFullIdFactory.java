package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.CalcAdjustedSources;
import gov.usgswim.sparrow.action.ReachFullIdAction;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ReachFullId;
import gov.usgswim.sparrow.request.ReachClientId;
import gov.usgswim.sparrow.service.SharedApplication;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * This factory class creates adjusted source values on demand for an EHCache.
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
public class ReachFullIdFactory implements CacheEntryFactory {
	@Override
	public ReachFullId createEntry(Object clientReachId) throws Exception {
		ReachClientId clientId = (ReachClientId)clientReachId;
		
		ReachFullIdAction action = new ReachFullIdAction(clientId);
		return action.run();
	}

}
