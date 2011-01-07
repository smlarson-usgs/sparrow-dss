package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadUnitAreas;
import gov.usgswim.sparrow.request.CatchmentArea;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Loads data for catchment area.
 * 
 * @author klangsto
 *
 */
public class CatchmentAreaFactory implements CacheEntryFactory {

	@Override
	public DataTable createEntry(Object inCatchmentArea) throws Exception {
		CatchmentArea catchmentArea = (CatchmentArea)inCatchmentArea;
		DataTable dt = null;
		LoadUnitAreas lua = new LoadUnitAreas(
				catchmentArea.getModelID(), catchmentArea.getHucLevel(), catchmentArea.getCumulative());
		dt = lua.run();
		return dt;
	}

}
