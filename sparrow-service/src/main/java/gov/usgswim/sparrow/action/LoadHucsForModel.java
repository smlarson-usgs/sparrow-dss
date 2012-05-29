package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.HucLevel;
import gov.usgswim.sparrow.request.ModelHucsRequest;

import java.sql.ResultSet;
import java.util.HashMap;


/**
 * Loads all the HUCs for a given model.
 * 
 * Only HUCs that have reaches in the model will be returned.
 * 
 * Row ID:  NONE
 * Columns are in this order:
 * <ul>
 * <li>Code - Indexed.  The HUC Code, containing the same number of digits as the huc level (huc8 will have 8 digits)
 * <li>Name - The name of the HUC
 * </ul>
 * 
 * Sorted by HUC ID (the first column)
 *  
 * @author eeverman
 */
public class LoadHucsForModel extends Action<DataTable> {
	
	
	private static final String QUERY_NAME = "query";
	protected Long modelId;
	protected HucLevel hucLevel;
	
	
	public LoadHucsForModel(Long modelId, HucLevel hucLevel) {
		super();
		this.modelId = modelId;
		this.hucLevel = hucLevel;
	}
	
	public LoadHucsForModel(ModelHucsRequest request) {
		super();
		this.modelId = request.getModelID();
		this.hucLevel = request.getHucLevel();
	}



	@Override
	public DataTable doAction() throws Exception {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", modelId);
		params.put("HucLevel", hucLevel.getLevel());
		
		ResultSet rset = getROPSFromPropertiesFile(QUERY_NAME, getClass(), params).executeQuery();
		addResultSetForAutoClose(rset);
		
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset, false);
		values.setName("HUC" + hucLevel.getLevel() + "s for model");
		values.setDescription(
						"Complete list of all HUC" + hucLevel.getLevel() + "s in the model " + modelId);
		
		values.buildIndex(0);
		return values.toImmutable();
	}

	@Override
	protected void validate() {
		if (modelId == null) {
			this.addValidationError("The modelId parameter cannot be null");
		}
		
		if (hucLevel == null) {
			this.addValidationError("The hucLevel parameter cannot be null");
		}
	}





}
