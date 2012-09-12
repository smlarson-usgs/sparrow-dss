package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.HucLevel;
import gov.usgswim.sparrow.request.ModelHucsRequest;

import java.sql.ResultSet;
import java.util.HashMap;


/**
 * Loads all the EAD codes and names for a given model.
 * 
 * Only EDAs that have reaches in the model will be returned.
 * 
 * Row ID:  None.
 * Columns are in this order:
 * <ul>
 * <li>Name - The name of the EDA
 * <li>Code - The EDA Code
 * 
 * </ul>
 * 
 * Sorted by EDA Code (the 2nd column)
 *  
 * @author eeverman
 */
public class LoadEdasForModel extends Action<DataTable> {
	
	private static final String QUERY_NAME = "query";
	protected Long modelId;
	
	
	public LoadEdasForModel(Long modelId) {
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
		values = DataTableConverter.toDataTable(rset, false);
		values.setName("States for model");
		values.setDescription(
						"Complete list of all states that reaches have catchment area in, for model " + modelId);
		
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
