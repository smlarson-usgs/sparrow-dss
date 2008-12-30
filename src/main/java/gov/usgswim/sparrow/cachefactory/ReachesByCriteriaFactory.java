package gov.usgswim.sparrow.cachefactory;

import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableUtils;
import gov.usgswim.sparrow.parser.LogicalSet;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.apache.log4j.Logger;

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
public class ReachesByCriteriaFactory extends AbstractCacheFactory {
	protected static Logger log =
		Logger.getLogger(ReachesByCriteriaFactory.class); //logging for this class
	
	public List<Long> createEntry(Object logicalSetCriteria) throws Exception {
		LogicalSet ls = (LogicalSet) logicalSetCriteria;

		assert(ls.getCriteria() != null && ls.getCriteria().size() == 1): "for now, we're only handling logical-sets with a single criteria";
		String criteriaAttrib = ls.getCriteria().keySet().iterator().next(); 
		String criteriaValue = ls.getCriteria().get(criteriaAttrib);
		
		// Branching code based on criteria type
		if (criteriaAttrib.startsWith("huc")) {
			String huc = criteriaValue;
			long modelID = ls.getModelID();
			int hucLevel = huc.length();
			assert(hucLevel == 2 ||  hucLevel == 4 || hucLevel == 6 || hucLevel == 8): "should be 2, 4, 6, or 8";


			String query = "Select identifier from model_attrib_vw where "
				+ criteriaAttrib + " = '" + huc + "' and sparrow_model_id = " + modelID;
			DataTableWritable dt = SharedApplication.queryToDataTable(query);

			Long[] arrayResult = DataTableUtils.getWrappedLongColumn(dt, 0);
			List<Long> reachIDs = Collections.unmodifiableList(Arrays.asList(arrayResult));
			return reachIDs;
		}

		log.error("Can not find reaches for Criteria:" + criteriaAttrib + "-" + criteriaValue);
		return null;
	}
}
