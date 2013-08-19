package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadStreamFlow;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A thin wrapper around an Action for EHCache CacheEntryFactory.
 * 
 * Caching, blocking, and de-caching are all handled by EHCache system.
 * 
 * @author klangsto
 */
public class StreamFlowFactory implements CacheEntryFactory {

	@Override
	public SparrowColumnSpecifier createEntry(Object inModelId) throws Exception {
		Long modelId = (Long)inModelId;
		SparrowColumnSpecifier dc = null;
		LoadStreamFlow lf = new LoadStreamFlow(modelId);
		dc = lf.run();
		return dc;
	}

}
