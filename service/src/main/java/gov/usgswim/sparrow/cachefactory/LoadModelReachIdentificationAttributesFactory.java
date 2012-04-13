package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadModelReachIdentificationAttributes;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 *
 * @author eeverman
 */
public class LoadModelReachIdentificationAttributesFactory implements CacheEntryFactory {


	@Override
	public DataTable createEntry(Object modelId) throws Exception {
		Long id = (Long)modelId;
		DataTable result = null;
		
		LoadModelReachIdentificationAttributes action = new LoadModelReachIdentificationAttributes(id);
		
		result = action.run();
		
		return result;
	}

}
