package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing flux (stream flow) information.

 * @author klangsto
 *
 */
public class LoadStreamFlow extends Action<SparrowColumnSpecifier> {
	
	protected long modelId;
	
	public LoadStreamFlow(long modelId) {
		super();
		this.modelId = modelId;
	}

	public LoadStreamFlow() {
		super();
	}

	@Override
	public SparrowColumnSpecifier doAction() throws Exception {
		
		String queryName = "LoadMeanQ";
		
		String sql = getText(queryName);
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset, null, true);
		
		//Set column attributes
		values.getColumns()[0].setName(getDataSeriesProperty(DataSeriesType.flux, false));
		values.getColumns()[0].setDescription(getDataSeriesProperty(DataSeriesType.flux, true));
		values.getColumns()[0].setUnits(SparrowUnits.CFS.getUserName());
		values.getColumns()[0].setProperty(TableProperties.DATA_SERIES.toString(), DataSeriesType.flux.name());
		values.getColumns()[0].setProperty(TableProperties.CONSTITUENT.toString(), "Water");
		

		SparrowColumnSpecifier retColumn = new SparrowColumnSpecifier(values.toImmutable(), 0, null, modelId);
		return retColumn;
		
	}

	@Override
	public Long getModelId() {
		return modelId;
	}

	public void setModelId(long modelId) {
		this.modelId = modelId;
	}

}
