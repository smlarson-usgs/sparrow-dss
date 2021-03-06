package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.PredictDataBuilder;
import gov.usgswim.sparrow.action.CalcPrediction;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.service.SharedApplication;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

import org.apache.log4j.Logger;

/**
 * This factory class creates a PredictResult on demand for an EHCache.
 * 
 * When the cache receives a get(predictContext) call and it doesn't have a cache
 * entry for that request, the createEntry() method of this class is called
 * and the returned value is cached.
 * 
 * The basic process followed in this class is:
 * <ul>
 * <li>load the PredictData for the specified model, pulling it from the cache
 * <li>adjust the PredictData as spec'ed in the PredictRequest
 * <li>run the prediction using the adjusted data
 * </ul>
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
public class PredictResultFactory implements CacheEntryFactory {
	protected static Logger log =
		Logger.getLogger(PredictResultFactory.class); //logging for this class
	
	@Override
	public PredictResult createEntry(Object adjustmentGroups) throws Exception {
		AdjustmentGroups adjs = (AdjustmentGroups) adjustmentGroups;
		
		PredictData nomPredictData = SharedApplication.getInstance().getPredictData(adjs.getModelID());
		PredictData adjPredictData = nomPredictData;	//assume no adjustments
		
		DataTable adjustedSources = SharedApplication.getInstance().getAdjustedSource(adjs);
		
		PredictDataBuilder mutable = nomPredictData.getBuilder();
		mutable.setSrc(adjustedSources);
		adjPredictData = mutable.toImmutable();

		CalcPrediction adjPredict = new CalcPrediction(adjPredictData);
		PredictResult result = adjPredict.run();
		

		return (PredictResult) result.toImmutable();
	}

}
