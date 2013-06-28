package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.DivideColumnData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnAttribsBuilder;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.SparrowModel;

/**
 * This action creates a DataColumn containing the yield.
 * 
 * Yield is load divided by area.  The calculation is simple, but this central
 * action provides some validation of the types of data and units being provided.
 * 
 * Currently this action is not used on its own, but is simply delegated to
 * from other actions.
 * 
 * @author eeverman
 *
 */
public class CalcAnyYield extends Action<ColumnData> {

	//The dataseries this Action calculates
	private final DataSeriesType seriesType;//should be some type of DataSeriesType.incremental_yield;
	private final SparrowModel model;
	private final ColumnData loadValuesColumn;
	private final ColumnData catchmentAreaColumn;
	
	public CalcAnyYield(DataSeriesType seriesType, SparrowModel model,
			ColumnData loadValuesColumn, ColumnData catchmentAreaColumn) {
		
		this.seriesType = seriesType;
		this.model = model;
		this.loadValuesColumn = loadValuesColumn;
		this.catchmentAreaColumn = catchmentAreaColumn;
	}

	@Override
	public ColumnData doAction() throws Exception {
		
		SparrowColumnAttribsBuilder ca = new SparrowColumnAttribsBuilder();
		ca.setName(getDataSeriesProperty(seriesType, false) + " for " + loadValuesColumn.getName());
		ca.setDescription(getDataSeriesProperty(seriesType, true));
		ca.setUnits(SparrowUnits.KG_PER_SQR_KM_PER_YEAR.getUserName());
		
		ca.setModelId(model.getId());		
		ca.setConstituent(model.getConstituent());
		ca.setBaseDataSeriesType(seriesType.getBaseType());
		ca.setDataSeriesType(seriesType);
		
		DivideColumnData result =
			new DivideColumnData(loadValuesColumn, catchmentAreaColumn, ca.toImmutable());
		
		return result;
	}

	@Override
	protected void validate() {
		
		
		//Check for nulls
		if (seriesType == null || model == null || loadValuesColumn == null || catchmentAreaColumn == null) {
			addValidationError("All action parameters must be non-null");
			return;
		}
		
		int loadRowCnt = loadValuesColumn.getRowCount();
		int areaRowCnt = catchmentAreaColumn.getRowCount();
		
		//Column rows must match
		if (loadRowCnt != areaRowCnt) {
			addValidationError("The load values column has a different number of rows "
					+ "than the catchment area column.  For model: " + model.getId());
			return;
		}
		
		//Must be a yield data series
		if (
				seriesType.equals(DataSeriesType.incremental_yield) ||
				seriesType.equals(DataSeriesType.total_yield) ||
				seriesType.equals(DataSeriesType.incremental_delivered_yield) ||
				seriesType.equals(DataSeriesType.total_delivered_yield) ) {
	
			//all is ok
		} else {
			addValidationError("The dataseries '" + seriesType.name() + "' is not allowed for this action.");
			return;
		}
		
		
		SparrowUnits modelUnit = model.getUnits();
		String areaUnitStr = catchmentAreaColumn.getUnits();
		SparrowUnits areaUnit = SparrowUnits.parseUserName(areaUnitStr);
		
		//Check the units - correct?
		if (! model.getUnits().equals(SparrowUnits.KG_PER_YEAR)) {
			String msg = "The model units must be in " +
			SparrowUnits.KG_PER_YEAR.getUserName() +
			" in order to calculate " + seriesType.toString() + ".  Found instead: " + 
			((modelUnit == null)?"null":modelUnit.getUserName());
			
			addValidationError(msg);
		}
		
		if (! catchmentAreaColumn.getUnits().equals(SparrowUnits.SQR_KM.getUserName())) {
			String msg = "The area units must be in " +
			SparrowUnits.SQR_KM.getUserName() +
			" in order to calculate " + seriesType.toString() + ".  Found instead: " +
			((areaUnit == null)?"null":areaUnit.getUserName());
			
			addValidationError(msg);
		}
	}


}
