package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.action.LoadModelReachAreaRelations;
import gov.usgswim.sparrow.action.LoadStatesForModel;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.domain.reacharearelation.ModelReachAreaRelations;
import gov.usgswim.sparrow.request.ModelAggregationRequest;
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
	public ModelReachAreaRelations createEntry(Object modelAggregationRequest) throws Exception {

		
		ModelReachAreaRelations result = null;
		ModelAggregationRequest req = (ModelAggregationRequest)modelAggregationRequest;
		LoadModelReachAreaRelations action = new LoadModelReachAreaRelations(req);
		
		result = action.run();
		
		return result;
	}

}
