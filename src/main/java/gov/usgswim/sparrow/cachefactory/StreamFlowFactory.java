package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.sparrow.action.LoadStreamFlow;
import gov.usgswim.sparrow.parser.DataColumn;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Loads data for stream flow.
 * 
 * @author klangsto
 *
 */
public class StreamFlowFactory implements CacheEntryFactory {

	@Override
	public DataColumn createEntry(Object inModelId) throws Exception {
		Long modelId = (Long)inModelId;
		DataColumn dc = null;
		LoadStreamFlow lf = new LoadStreamFlow(modelId);
		dc = lf.run();
		return dc;
	}

}
