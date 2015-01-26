package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.AreaType;
import gov.usgswim.sparrow.SparrowUnits;
import static gov.usgswim.sparrow.action.Action.getDataSeriesProperty;
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
 *  <li>Column 0 : Long.  The ID of associated entity for that row.  For reach
 *  level data this will be the reach Identifier.  For HUC level data it will
 *  be the HUC ID.
 *  <li>Column 1 : Double.  The area, based on the type of area requested.
 *  </ul>
 *
 *  For reach related rows, they are returned in PredictData order.
 *
 * @author eeverman
 */
public class LoadUnitAreas extends Action<DataTable> {

	protected long modelId;
	protected AreaType areaType;

	/** Summary message for completed action */
	private StringBuffer message = new StringBuffer();



	public LoadUnitAreas(long modelId, AreaType areaType) {
		super();
		this.modelId = modelId;
		this.areaType = areaType;
	}



	public LoadUnitAreas() {
		super();
	}



	@Override
	public DataTable doAction() throws Exception {

		String sql = getText(areaType.name());
		PreparedStatement st = getNewROPreparedStatement(sql);

		st.setLong(1, modelId);

		ResultSet rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		DataTableWritable values = null;
		values = DataTableConverter.toDataTable(rset, new Class[]{Long.class, Double.class}, false);
		values.buildIndex(0);
		values.getColumns()[1].setUnits(SparrowUnits.SQR_KM.toString());
		values.getColumns()[1].setName(areaType.getName());
		values.getColumns()[1].setDescription(areaType.getDescription());
		values.getColumns()[1].setProperty(TableProperties.DATA_SERIES.toString(), areaType.name());
		values.getColumns()[1].setProperty(TableProperties.CONSTITUENT.toString(), "land area");


		message.append("Loaded UnitAreas for " + modelId + " AreaCalcType " + areaType + NL);
		message.append("  Total Rows: " + values.getRowCount() + NL);

		return values.toImmutable();

	}



	@Override
	public Long getModelId() {
		return modelId;
	}



	public void setModelId(long modelId) {
		this.modelId = modelId;
	}



	public AreaType getAreaType() {
		return areaType;
	}



	public void setAreaType(AreaType areaType) {
		this.areaType = areaType;
	}

	@Override
	protected String getPostMessage() {
		return message.toString();
	}

}
