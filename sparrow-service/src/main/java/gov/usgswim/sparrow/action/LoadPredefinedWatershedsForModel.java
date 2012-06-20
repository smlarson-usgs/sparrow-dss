package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.HucLevel;
import gov.usgswim.sparrow.request.ModelHucsRequest;

import java.sql.ResultSet;
import java.util.HashMap;


/**
 * Loads all the predefined watersheds (names, ids, description) for a given model.
 * 
 * Row ID:  The DB ID for the watershed.
 * 
 * Columns are in this order:
 * <ul>
 * <li>Name - The name of the EDA
 * <li>Description - Modeler provided description.
 * <li>Count - The number of reaches in this watershed
 * 
 * </ul>
 * 
 * Sorted by the Name column.
 *  
 * @author eeverman
 */
public class LoadPredefinedWatershedsForModel extends Action<DataTable> {
	
	private static final String QUERY_NAME = "query";
	protected Long modelId;
	
	
	public LoadPredefinedWatershedsForModel(Long modelId) {
		super();
		this.modelId = modelId;
	}

	@Override
	public DataTable doAction() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", modelId);
		
		ResultSet rset = getROPSFromPropertiesFile(QUERY_NAME, getClass(), params).executeQuery();
		addResultSetForAutoClose(rset);
		
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset, true);
		values.setName("Predefined Watersheds for model");
		values.setDescription(
						"Complete list of all the modeler defined watersheds for model " + modelId);
		
		//Not building an index - does not seem worth it since the list is relatively short
		
		return values.toImmutable();
	}

	@Override
	protected void validate() {
		if (modelId == null) {
			this.addValidationError("The modelId parameter cannot be null");
		}
	}


}
