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
import gov.usgswim.sparrow.datatable.PredictResultCompare;
import gov.usgswim.sparrow.datatable.PredictResultImm;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.DataLoader;
import gov.usgswim.task.ComputableCache;
import gov.usgswim.sparrow.parser.ComparisonType;
import java.util.Arrays;

/**
 * This factory loads a list of reach ID for a given model and HUC.
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
public class ReachesByHUCFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(ReachesByHUCFactory.class); //logging for this class
	
	public Object createEntry(Object reachesByHUCRequest) throws Exception {
		ReachesByHUCRequest request = (ReachesByHUCRequest)reachesByHUCRequest;

		String huc = request.getHuc();
		long modelID = request.getModelID();
		int hucLevel = request.getHUCLevel();	//should be 2, 4, 6, or 8
		
		int[] reachIDs = null;
		//load the ids from a query
		//....
		
		//returns the named query out of the props file with a name mathcing this class
		this.getText("FindReaches", new String[] {"name", "value"});
		
		return reachIDs;
	}
}
