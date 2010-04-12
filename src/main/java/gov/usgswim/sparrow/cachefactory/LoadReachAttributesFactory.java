package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.action.LoadReachAttributes;
import net.sf.ehcache.constructs.blocking.CacheEntryFactory;

/**
 * A EHCache wrapper around the LoadReachAttributes Action.
 * 
 * @author eeverman
 *
 */
public class LoadReachAttributesFactory implements CacheEntryFactory {
	
	@Override
	public DataTable createEntry(Object reachID) throws Exception {
		ReachID rid = (ReachID)reachID;
		
		LoadReachAttributes action = new LoadReachAttributes();
		action.setModelId(rid.getModelID());
		action.setReachId(rid.getReachID());
		DataTable result = action.run();
		
		return result.toImmutable();
	}

}
