package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.DataSeriesType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing flux (stream flow) information.

 * @author klangsto
 *
 */
public class LoadStreamFlow extends Action<DataTable> {
	
	protected long modelId;
	
	public LoadStreamFlow(long modelId) {
		super();
		this.modelId = modelId;
	}

	public LoadStreamFlow() {
		super();
	}

	@Override
	protected DataTable doAction() throws Exception {
		
		String queryName = "LoadMeanQ";
		String colName = "Stream Flow";
		String colDesc = "Averaged flow of the stream reach";
		
		String sql = getText(queryName);
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();	//auto-closed
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		
		//Set column attributes
		values.getColumns()[1].setName(colName);
		values.getColumns()[1].setDescription(colDesc);
		values.getColumns()[1].setUnits("cu ft/s");
		values.getColumns()[1].setProperty(TableProperties.DATA_SERIES.getPublicName(), DataSeriesType.flux.name());
		values.getColumns()[1].setProperty(TableProperties.CONSTITUENT.getPublicName(), "Water");
		

		return values.toImmutable();
		
	}

	public long getModelId() {
		return modelId;
	}

	public void setModelId(long modelId) {
		this.modelId = modelId;
	}

}
