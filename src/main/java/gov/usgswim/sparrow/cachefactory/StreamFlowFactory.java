package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadStreamFlow;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Loads data for flux (avg. daily stream flow).
 * 
 * @author klangsto
 *
 */
public class StreamFlowFactory implements CacheEntryFactory {

	@Override
	public DataTable createEntry(Object inModelId) throws Exception {
		Long modelId = (Long)inModelId;
		DataTable dt = null;
		LoadStreamFlow lf = new LoadStreamFlow(modelId);
		dt = lf.run();
		return dt;
	}

}
