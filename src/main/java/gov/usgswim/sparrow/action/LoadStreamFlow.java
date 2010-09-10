package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing flux (stream flow) information.

 * @author klangsto
 *
 */
public class LoadStreamFlow extends Action<DataColumn> {
	
	protected long modelId;
	
	public LoadStreamFlow(long modelId) {
		super();
		this.modelId = modelId;
	}

	public LoadStreamFlow() {
		super();
	}

	@Override
	public DataColumn doAction() throws Exception {
		
		String queryName = "LoadMeanQ";
		
		String sql = getText(queryName);
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();	//auto-closed
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset, null, true);
		
		//Set column attributes
		values.getColumns()[0].setName(getDataSeriesProperty(DataSeriesType.flux, false));
		values.getColumns()[0].setDescription(getDataSeriesProperty(DataSeriesType.flux, true));
		values.getColumns()[0].setUnits(SparrowUnits.CFS.getUserName());
		values.getColumns()[0].setProperty(TableProperties.DATA_SERIES.getPublicName(), DataSeriesType.flux.name());
		values.getColumns()[0].setProperty(TableProperties.CONSTITUENT.getPublicName(), "Water");
		

		DataColumn retColumn = new DataColumn(values.toImmutable(), 0, null, modelId);
		return retColumn;
		
	}

	public long getModelId() {
		return modelId;
	}

	public void setModelId(long modelId) {
		this.modelId = modelId;
	}

}
