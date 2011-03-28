package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.domain.UnitAreaType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing catchment areas.
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
		
		ResultSet rset = st.executeQuery();	//auto-closed
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		values.buildIndex(0);
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
