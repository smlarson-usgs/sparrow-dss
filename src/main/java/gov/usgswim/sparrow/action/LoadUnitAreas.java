package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.datatable.HucLevel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing reach attributes.

 * @author eeverman
 *
 */
public class LoadUnitAreas extends Action<DataTable> {
	
	/** Name of the query in the classname matched properties file */
	//public static final String QUERY_NAME = "attributesSQL";
	
	protected long modelId;
	protected HucLevel hucLevel = HucLevel.HUC_NONE;	//Default to individual reaches
	protected boolean cumulative = false;
	
	
	
	public LoadUnitAreas(long modelId, HucLevel hucLevel, boolean cumulative) {
		super();
		this.modelId = modelId;
		this.cumulative = cumulative;
		
		//has validation
		setHucLevel(hucLevel);
	}



	public LoadUnitAreas() {
		super();
	}



	@Override
	protected DataTable doAction() throws Exception {
		
		String queryName = null;
		String areaColumnName = null;
		
		switch (hucLevel) {
		case HUC_NONE:
			if (cumulative == false) {
				queryName = "LoadCatchArea";
				areaColumnName = "Catchment Area";
			} else {
				queryName = "LoadCumCatchArea";
				areaColumnName = "Cumulative Catchment Area";
			}
			break;
		
		default:
			throw new IllegalStateException("Unexpected huc level.  Not supported");
		}
		
		String sql = getText(queryName);
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();	//auto-closed
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		values.buildIndex(0);
		values.getColumns()[1].setUnits("sqr km");
		values.getColumns()[1].setName(areaColumnName);

		return values.toImmutable();
		
	}



	public long getModelId() {
		return modelId;
	}



	public void setModelId(long modelId) {
		this.modelId = modelId;
	}



	public HucLevel getHucLevel() {
		return hucLevel;
	}



	public void setHucLevel(HucLevel hucLevel) {
		
		
		if (! hucLevel.equals(HucLevel.HUC_NONE)) {
			throw new IllegalArgumentException("Only HUC_NONE is currently supported");
		}
		
		this.hucLevel = hucLevel;
	}



	public boolean isCumulative() {
		return cumulative;
	}



	public void setCumulative(boolean cumulative) {
		this.cumulative = cumulative;
	}



}
