package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadFlux;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Loads data for flux (avg. daily stream flow).
 * 
 * @author klangsto
 *
 */
public class FluxFactory implements CacheEntryFactory {

	@Override
	public DataTable createEntry(Object inModelId) throws Exception {
		Long modelId = (Long)inModelId;
		DataTable dt = null;
		LoadFlux lf = new LoadFlux(modelId);
		dt = lf.run();
		return dt;
	}

}
