package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.HucLevel;
import gov.usgswim.sparrow.request.ModelHucsRequest;

import java.sql.ResultSet;
import java.util.HashMap;


/**
 * Loads all the predefined watersheds (names, ids, description) for a given model.
 * 
 * Row ID:  ReachId - The reach identifier (identifier column, not the db id)
 * 
 * Columns are in this order:
 * <ul>
 * <li>Name
 * </ul>
 * 
 * Sorted by the reach identifier.
 *  
 * @author eeverman
 */
public class LoadPredefinedWatershedReachesForModel extends Action<DataTable> {
	
	private static final String QUERY_NAME = "query";
	protected Long watershedId;
	
	
	public LoadPredefinedWatershedReachesForModel(Long watershedId) {
		super();
		this.watershedId = watershedId;
	}

	@Override
	public DataTable doAction() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("WatershedId", watershedId);
		
		ResultSet rset = getROPSFromPropertiesFile(QUERY_NAME, getClass(), params).executeQuery();
		addResultSetForAutoClose(rset);
		
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset, true);
		values.setName("Reaches for Predefined Watershed");
		values.setDescription(
						"Complete list of all the reaches in the modeler defined watershed " + watershedId);
		
		//Not building an index - does not seem worth it since the list is relatively short
		
		return values.toImmutable();
	}

	@Override
	protected void validate() {
		if (watershedId == null) {
			this.addValidationError("The watershedId parameter cannot be null");
		}
	}


}
