package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.HucLevel;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.parser.DataSeriesType;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing catchment areas.
 *  
 * @author eeverman
 */
public class LoadUnitAreas extends Action<DataTable> {
	
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
	public DataTable doAction() throws Exception {
		
		String queryName = null;
		String areaColName = null;
		String areaColDesc = null;
		String areaColDataSeries = null;

		switch (hucLevel) {
		case HUC_NONE:
			if (cumulative == false) {
				queryName = "LoadCatchArea";
				areaColName = getDataSeriesProperty(DataSeriesType.catch_area, false);
				areaColDesc = getDataSeriesProperty(DataSeriesType.catch_area, true);
				areaColDataSeries = DataSeriesType.catch_area.name();
			} else {
				queryName = "LoadCumCatchArea";
				areaColName = getDataSeriesProperty(DataSeriesType.cumulative_catch_area, false);
				areaColDesc = getDataSeriesProperty(DataSeriesType.cumulative_catch_area, true);
				areaColDataSeries = DataSeriesType.cumulative_catch_area.name();
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
		values.getColumns()[1].setUnits(SparrowUnits.SQR_KM.toString());
		values.getColumns()[1].setName(areaColName);
		values.getColumns()[1].setDescription(areaColDesc);
		values.getColumns()[1].setProperty(TableProperties.DATA_SERIES.getPublicName(), areaColDataSeries);
		values.getColumns()[1].setProperty(TableProperties.CONSTITUENT.getPublicName(), "land area");

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
