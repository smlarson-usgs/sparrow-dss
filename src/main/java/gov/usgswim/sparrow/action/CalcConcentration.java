package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.ColumnFromTable;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.ColumnAttribsBuilder;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.datatable.SingleValueDoubleColumnData;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;

/**
 * This action creates a ColumnData containing the concentration of the
 * passed data.
 * 
 * @author eeverman
 *
 */
public class CalcConcentration extends Action<DataColumn> {


	private final static double CONVERSION_FACTOR = .0011198d;

	protected DataColumn baseData;
	protected DataColumn streamFlowData;
	protected String msg = null;
	
	public void setBaseData(DataColumn baseData) {
		this.baseData = baseData;
	}

	public void setStreamFlowData(DataColumn streamFlowData) {
		this.streamFlowData = streamFlowData;
	}
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	protected DataColumn doAction() throws Exception {
		// total conc. is (total load / (stream flow)) * 0.0011198
		
		//For our conversion to work properly, we are depending on some specific
		//units, so we check...
		if (! SparrowUnits.KG_PER_YEAR.isSame(baseData.getUnits())) {
			throw new Exception("The base units must be in 'kg/year' or the conversion to mg/L will not be correct.");
		}
		
		if (! SparrowUnits.CFS.isSame(streamFlowData.getUnits())) {
			throw new Exception("The streamflow units must be in 'cu ft/s' or the conversion to mg/L will not be correct.");
		}
		
		ColumnAttribsBuilder concAttribs = new ColumnAttribsBuilder();
		concAttribs.setName(getDataSeriesProperty(DataSeriesType.total_concentration, false));
		concAttribs.setDescription(getDataSeriesProperty(DataSeriesType.total_concentration, true));
		concAttribs.setUnits(SparrowUnits.MG_PER_L.getUserName());
		
		ColumnAttribsBuilder coefAttribs = new ColumnAttribsBuilder();
		coefAttribs.setName("Conversion Coef");
		coefAttribs.setUnits("Unknown");
		
		SingleValueDoubleColumnData conversionCoefCol =
			new SingleValueDoubleColumnData(CONVERSION_FACTOR, baseData.getRowCount(), coefAttribs);
		
		//A single column of data
		ColumnData flowColumn = new ColumnFromTable(streamFlowData.getTable(), streamFlowData.getColumn());
		
		SingleColumnCoefDataTable baseXflow =
			new SingleColumnCoefDataTable(baseData.getTable(), flowColumn, baseData.getColumn(), null, true);
		
		SingleColumnCoefDataTable result = new SingleColumnCoefDataTable(baseXflow, conversionCoefCol, baseData.getColumn(), concAttribs);
		
		DataColumn resultCol = new DataColumn(result, baseData.getColumn(), baseData.getContextId());
		
		return resultCol;
	}

}
