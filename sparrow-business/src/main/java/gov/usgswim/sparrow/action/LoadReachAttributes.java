package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnDataWritable;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.DataTableWritable;
import gov.usgs.cida.datatable.utils.DataTableConverter;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;
import java.util.List;
import java.util.Arrays;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;


/**
 * Loads a table containing reach attributes.
 * TODO:  The headreach info is returned as null, which converts to zero.
 * (This seems to be a data issue)
 * @author eeverman
 *
 */
public class LoadReachAttributes extends Action<DataTable> {

	/** Name of the query in the classname matched properties file */
	public static final String QUERY_NAME = "attributesSQL";

	protected long modelId;
	protected long reachId;



	public LoadReachAttributes(long modelId, long reachId) {
		super();
		this.modelId = modelId;
		this.reachId = reachId;
	}



	public LoadReachAttributes() {
		super();
	}



	@Override
	public DataTable doAction() throws Exception {
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("ModelId", this.modelId);
		params.put("ReachId", this.reachId);

		PreparedStatement st = getROPSFromPropertiesFile(QUERY_NAME, this.getClass(), params);

		ResultSet rset = null;
		DataTableWritable attribs = null;

		rset = st.executeQuery();
		addResultSetForAutoClose(rset);
		attribs = DataTableConverter.toDataTable(rset);

		//Set column props
		ColumnDataWritable[] cols = attribs.getColumns();
		final String PRECISION = TableProperties.PRECISION.toString();
		final String DOC_ID = TableProperties.DOC_ID.toString();
		cols[8].setUnits(SparrowUnits.METERS.getUserName());	//Reach Length
		cols[8].setProperty(PRECISION, "1");

		cols[9].setUnits(SparrowUnits.CFS.getUserName());	//Mean Flow
		cols[9].setProperty(PRECISION, "2");
		cols[10].setUnits(SparrowUnits.FPS.getUserName());	//Mean Velocity
		cols[10].setProperty(PRECISION, "2");
		cols[11].setUnits(SparrowUnits.SQR_KM.getUserName());	//Incremental Area
		cols[11].setProperty(PRECISION, "2");
		cols[11].setProperty(DOC_ID, "CommonTerms.Incremental Area");
		cols[12].setUnits(SparrowUnits.SQR_KM.getUserName());	//Total Contributing Area
		cols[12].setProperty(PRECISION, "2");
		cols[12].setProperty(DOC_ID, "CommonTerms.Total Contributing Area");
		cols[13].setUnits(SparrowUnits.SQR_KM.getUserName());	//Total Upstream Area
		cols[13].setProperty(PRECISION, "2");
		cols[13].setProperty(DOC_ID, "CommonTerms.Total Upstream Area");

		cols[14].setUnits(SparrowUnits.FRACTION.getUserName());	//Incremental Delivery
		cols[14].setProperty(PRECISION, "6");
		cols[15].setUnits(SparrowUnits.FRACTION.getUserName());	//TOTAL Delivery
		cols[15].setProperty(PRECISION, "6");
		cols[16].setUnits(SparrowUnits.FRACTION.getUserName());	//Split Fraction
		cols[16].setProperty(PRECISION, "2");

		return attribs;
	}



	@Override
	public Long getModelId() {
		return modelId;
	}



	public void setModelId(long modelId) {
		this.modelId = modelId;
	}



	public long getReachId() {
		return reachId;
	}



	public void setReachId(long reachId) {
		this.reachId = reachId;
	}


}
