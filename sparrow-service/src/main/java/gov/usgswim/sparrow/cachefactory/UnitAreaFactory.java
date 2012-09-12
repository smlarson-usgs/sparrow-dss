package gov.usgswim.sparrow.cachefactory;

import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadUnitAreas;
import gov.usgswim.sparrow.request.UnitAreaRequest;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * Loads data for the unit area, which may be a catchment or huc.
 * 
 * @author klangsto
 *
 */
public class UnitAreaFactory implements CacheEntryFactory {

	@Override
	public DataTable createEntry(Object inCatchmentArea) throws Exception {
		UnitAreaRequest catchmentArea = (UnitAreaRequest)inCatchmentArea;
		DataTable dt = null;
		LoadUnitAreas lua = new LoadUnitAreas(
				catchmentArea.getModelID(), catchmentArea.getHucLevel(), catchmentArea.getCumulative());
		dt = lua.run();
		return dt;
	}

}
