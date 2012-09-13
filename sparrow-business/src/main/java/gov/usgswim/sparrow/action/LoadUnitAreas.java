package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.AggregationLevel;

import java.sql.PreparedStatement;
import java.sql.ResultSet;


/**
 *  Loads a table containing unit areas.
 *  
 *  The returned table has two columns:
 *  <ul>
 *  <li>Column 0 : The ID of associated entity for that row.  For reach
 *  level data this will be the reach Identifier.  For HUC level data it will
 *  be the HUC ID.
 *  <li>Column 1 : The area, based on the type of area requested.
 *  </ul>
 *  
 *  For reach related rows, they are returned in PredictData order.
 *  
 * @author eeverman
 */
public class LoadUnitAreas extends Action<DataTable> {
	
	protected long modelId;
	protected AggregationLevel hucLevel = AggregationLevel.NONE;	//Default to individual reaches
	protected boolean cumulative = false;
	
	/** Summary message for completed action */
	private StringBuffer message = new StringBuffer();
	
	
	
	public LoadUnitAreas(long modelId, AggregationLevel hucLevel, boolean cumulative) {
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
		case NONE:
		case REACH:
			if (cumulative == false) {
				queryName = "LoadCatchArea";
				areaColName = getDataSeriesProperty(DataSeriesType.catch_area, false);
				areaColDesc = getDataSeriesProperty(DataSeriesType.catch_area, true);
				areaColDataSeries = DataSeriesType.catch_area.name();
			} else {
				queryName = "LoadCumCatchArea";
				areaColName = getDataSeriesProperty(DataSeriesType.watershed_area, false);
				areaColDesc = getDataSeriesProperty(DataSeriesType.watershed_area, true);
				areaColDataSeries = DataSeriesType.watershed_area.name();
			}
			break;
		
		default:
			throw new IllegalStateException("Unexpected huc level.  Not supported");
		}
		
		String sql = getText(queryName);
		PreparedStatement st = getNewROPreparedStatement(sql);
		
		st.setLong(1, modelId);
		
		ResultSet rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset);
		values.buildIndex(0);
		values.getColumns()[1].setUnits(SparrowUnits.SQR_KM.toString());
		values.getColumns()[1].setName(areaColName);
		values.getColumns()[1].setDescription(areaColDesc);
		values.getColumns()[1].setProperty(TableProperties.DATA_SERIES.toString(), areaColDataSeries);
		values.getColumns()[1].setProperty(TableProperties.CONSTITUENT.toString(), "land area");

		
		message.append("Loaded UnitAreas for " + modelId + " HucLevel " + hucLevel + " cumulative " + cumulative + NL);
		message.append("  Total Rows: " + values.getRowCount() + NL);
		
		return values.toImmutable();
		
	}



	public long getModelId() {
		return modelId;
	}



	public void setModelId(long modelId) {
		this.modelId = modelId;
	}



	public AggregationLevel getHucLevel() {
		return hucLevel;
	}



	public void setHucLevel(AggregationLevel hucLevel) {
		
		
		if (! (hucLevel.equals(AggregationLevel.NONE) || hucLevel.equals(AggregationLevel.REACH))) {
			throw new IllegalArgumentException("Only NONE or REACH is currently supported for unit areas");
		}
		
		this.hucLevel = hucLevel;
	}



	public boolean isCumulative() {
		return cumulative;
	}



	public void setCumulative(boolean cumulative) {
		this.cumulative = cumulative;
	}

	@Override
	protected String getPostMessage() {
		return message.toString();
	}


}
