package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Loads the distinct EDA Names for a model.
 * 
 * Currently used to populate the 'Find Reach' window w/ options for the user
 * to pick from.
 * 
 * @author eeverman
 */
public class LoadEDANameColumn extends Action<ColumnData> {
	
	/** Name of the query in the classname matched properties file */
	public static final String QUERY_NAME = "select";
	
	protected long modelId;
	
	public LoadEDANameColumn(long modelId) {
		super();
		this.modelId = modelId;
	}


	public LoadEDANameColumn() {
		super();
	}


	@Override
	public ColumnData doAction() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", this.modelId);

		PreparedStatement st = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);
		
		ResultSet rset = null;
		DataTableWritable attribs = null;

		rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		attribs = DataTableConverter.toDataTable(rset);
		
		return attribs.getColumn(0);
	}


	public long getModelId() {
		return modelId;
	}


	public void setModelId(long modelId) {
		this.modelId = modelId;
	}
	
}
