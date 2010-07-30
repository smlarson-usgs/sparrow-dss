package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.impl.ColumnFromTable;
import gov.usgswim.sparrow.datatable.ColumnAttribsBuilder;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.datatable.SingleValueDoubleColumnData;
import gov.usgswim.sparrow.parser.DataColumn;

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
		
		ColumnAttribsBuilder concAttribs = new ColumnAttribsBuilder();
		concAttribs.setName("Concentration");
		concAttribs.setDescription("The total load of the constituent divided " +
				"by the stream flow.");
		concAttribs.setUnits("mg/L");
		
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
