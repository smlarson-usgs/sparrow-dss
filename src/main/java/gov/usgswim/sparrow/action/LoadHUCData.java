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
public class LoadHUCData extends Action<DataTable> {
	
	protected long modelId;
	protected UnitAreaType hucLevel;
	
	
	
	public LoadHUCData(long modelId, UnitAreaType hucLevel) {
		super();
		this.modelId = modelId;
		//has validation
		setHucLevel(hucLevel);
	}



	@Override
	public DataTable doAction() throws Exception {
		
		String queryName = hucLevel.name();
		String colName = hucLevel.toString();
		String colDesc = hucLevel.toString();
		
		String sql = getText(queryName);
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();	//auto-closed
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		values.buildIndex(0);
		values.getColumns()[1].setName(colName);
		values.getColumns()[1].setDescription(colDesc);
		return values.toImmutable();
	}



	public long getModelId() {
		return modelId;
	}



	public void setModelId(long modelId) {
		this.modelId = modelId;
	}



	public UnitAreaType getHucLevel() {
		return hucLevel;
	}



	public void setHucLevel(UnitAreaType hucLevel) {
		if (! hucLevel.equals(UnitAreaType.HUC_2) &&
				! hucLevel.equals(UnitAreaType.HUC_4) &&
				! hucLevel.equals(UnitAreaType.HUC_6) &&
				! hucLevel.equals(UnitAreaType.HUC_8)) {
			throw new IllegalArgumentException("Only HUC_2-8 is currently supported");
		}
		this.hucLevel = hucLevel;
	}
}
