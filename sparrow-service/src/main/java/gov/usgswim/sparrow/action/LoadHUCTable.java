package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.AggregationLevel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing HUC8 areas.
 *  
 *   The returned table has one column:
 *  <ul>
 *  <li>Column 0 : The HUC8 ID, ie, '01234567' as a String
 *  </ul>
 *  
 *  This table is sorted in predictData order for the specified model.
 *  
 * @author eeverman
 */
public class LoadHUCTable extends Action<DataTable> {
	
	protected long modelId;
	
	public LoadHUCTable(long modelId) {
		super();
		this.modelId = modelId;
	}



	@Override
	public DataTable doAction() throws Exception {
		String sql = getText("HUC_8");
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		values.getColumns()[0].setName("HUC8");
		values.getColumns()[0].setDescription("HUC8");
		return values.toImmutable();
	}

	public long getModelId() {
		return modelId;
	}

	public void setModelId(long modelId) {
		this.modelId = modelId;
	}
}
