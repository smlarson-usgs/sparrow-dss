package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


/**
 * TODO:  This is likely fast and better than what we had before, but we will
 * need sorting to be specific for each column (names by names, codes by codes).
 * Thus, I think this needs to be two tables.
 * Loads a table containing EDA names and codes attributes.
 * 
 * TODO:  Not yet wired into the application.
 * @author eeverman
 *
 */
public class LoadEDAAttributes extends Action<DataTable> {
	
	/** Name of the query in the classname matched properties file */
	public static final String QUERY_NAME = "attributesSQL";
	
	protected long modelId;
	
	public LoadEDAAttributes(long modelId) {
		super();
		this.modelId = modelId;
	}


	public LoadEDAAttributes() {
		super();
	}


	@Override
	public DataTable doAction() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", this.modelId);

		PreparedStatement st = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
		
		ResultSet rset = null;
		DataTableWritable attribs = null;

		rset = st.executeQuery();	//auto-closed
		attribs = DataTableConverter.toDataTable(rset);
		
		return attribs;
	}


	public long getModelId() {
		return modelId;
	}


	public void setModelId(long modelId) {
		this.modelId = modelId;
	}
	
}
