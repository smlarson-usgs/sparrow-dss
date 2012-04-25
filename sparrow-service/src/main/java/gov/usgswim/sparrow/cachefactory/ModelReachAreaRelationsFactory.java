package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.LoadModelReachAreaRelations;
import gov.usgswim.sparrow.action.LoadStatesForModel;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class ModelReachAreaRelationsFactory implements CacheEntryFactory {


	@Override
	public ModelReachAreaRelations createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		ModelReachAreaRelations result = null;
		PredictData pd = SharedApplication.getInstance().getPredictData(id);
		
		LoadModelReachAreaRelations action = new LoadModelReachAreaRelations(pd);
		
		result = action.run();
		
		return result;
	}

}
