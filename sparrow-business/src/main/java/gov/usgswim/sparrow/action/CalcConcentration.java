package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnAttribsBuilder;
import gov.usgs.cida.datatable.ColumnData;
import gov.usgs.cida.datatable.impl.ColumnDataFromTable;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.datatable.SingleValueDoubleColumnData;
import gov.usgswim.sparrow.domain.DataSeriesType;

/**
 * This action creates a ColumnData containing the concentration of the
 * passed data.
 * 
 * @author eeverman
 *
 */
public class CalcConcentration extends Action<SparrowColumnSpecifier> {


	private final static double CONVERSION_FACTOR = .0011198d;

	protected SparrowColumnSpecifier baseData;
	protected SparrowColumnSpecifier streamFlowData;
	protected String msg = null;
	
	public void setBaseData(SparrowColumnSpecifier baseData) {
		this.baseData = baseData;
	}

	public void setStreamFlowData(SparrowColumnSpecifier streamFlowData) {
		this.streamFlowData = streamFlowData;
	}
	
	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	@Override
	public SparrowColumnSpecifier doAction() throws Exception {
		// total conc. is (total load / (stream flow)) * 0.0011198
		
		//For our conversion to work properly, we are depending on some specific
		//units, so we check...
		if (! SparrowUnits.KG_PER_YEAR.isSame(baseData.getUnits())) {
			throw new Exception("The base units must be in 'kg/year' or the conversion to mg/L will not be correct.");
		}
		
		if (! SparrowUnits.CFS.isSame(streamFlowData.getUnits())) {
			throw new Exception("The streamflow units must be in 'cu ft/s' or the conversion to mg/L will not be correct.");
		}
		
		if (! baseData.hasRowIds()) {
			throw new Exception("The baseData must have row IDs or Identify will not work for this series.");
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
		ColumnData flowColumn = new ColumnDataFromTable(streamFlowData.getTable(), streamFlowData.getColumn());
		
		SingleColumnCoefDataTable baseXflow =
			new SingleColumnCoefDataTable(baseData.getTable(), flowColumn, baseData.getColumn(), null, true);
		
		SingleColumnCoefDataTable result = new SingleColumnCoefDataTable(baseXflow, conversionCoefCol, baseData.getColumn(), concAttribs);
		
		SparrowColumnSpecifier resultCol = new SparrowColumnSpecifier(result, baseData.getColumn(), baseData.getContextId(), baseData.getModelId());
		
		return resultCol;
	}

}
