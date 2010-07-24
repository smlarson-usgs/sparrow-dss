package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing flux (stream flow) information.

 * @author klangsto
 *
 */
public class LoadFlux extends Action<DataTable> {
	
	protected long modelId;
	
	public LoadFlux(long modelId) {
		super();
		this.modelId = modelId;
	}

	public LoadFlux() {
		super();
	}

	@Override
	protected DataTable doAction() throws Exception {
		
		String queryName = "LoadMeanQ";
		String fluxColumnName = "Average Daily Flux";
		
		String sql = getText(queryName);
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();	//auto-closed
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		values.buildIndex(0);
		values.getColumns()[1].setUnits("cu ft/s");
		values.getColumns()[1].setName(fluxColumnName);

		return values.toImmutable();
		
	}

	public long getModelId() {
		return modelId;
	}

	public void setModelId(long modelId) {
		this.modelId = modelId;
	}

}
