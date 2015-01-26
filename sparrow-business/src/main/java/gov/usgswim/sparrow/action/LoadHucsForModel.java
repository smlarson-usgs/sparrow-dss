package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.HucLevel;
import gov.usgswim.sparrow.request.ModelHucsRequest;

import java.sql.ResultSet;
import java.util.HashMap;


/**
 * Loads all the HUCs for a given model.
 * 
 * Only HUCs that have reaches in the model will be returned.
 * 
 * Row ID:  Hashcode of the HUC Code
 * Columns are in this order:
 * <ul>
 * <li>Name - The name of the HUC
 * <li>Code - The HUC Code, containing the same number of digits as the huc level (huc8 will have 8 digits)
 * 
 * </ul>
 * 
 * Sorted by HUC ID (the 2nd column)
 *  
 * @author eeverman
 */
public class LoadHucsForModel extends Action<DataTable> {
	
	private static final int HUC_NAME_COL = 0;
	private static final int HUC_CODE_COL = 1;
	
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
		
		//Build the index based on the hashcode of the HUC Code
		for (int r = 0; r < values.getRowCount(); r++) {
			values.setRowId(values.getString(r, HUC_CODE_COL).hashCode(), r);
		}
		
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


	@Override
	public Long getModelId() {
		return modelId;
	}


}
