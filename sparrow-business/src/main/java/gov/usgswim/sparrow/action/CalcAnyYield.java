package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.DivideColumnData;
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
	private final boolean includeLoadInName;
	public final static String YIELD_LOAD_NAME_DELIMITER = " for ";
	public CalcAnyYield(DataSeriesType seriesType, SparrowModel model,
			ColumnData loadValuesColumn, ColumnData catchmentAreaColumn) {

		this(seriesType, model,
			loadValuesColumn, catchmentAreaColumn,
			false);
	}
	public CalcAnyYield(DataSeriesType seriesType, SparrowModel model,
			ColumnData loadValuesColumn, ColumnData catchmentAreaColumn, boolean includeLoadInName) {

		this.seriesType = seriesType;
		this.model = model;
		this.loadValuesColumn = loadValuesColumn;
		this.catchmentAreaColumn = catchmentAreaColumn;
		this.includeLoadInName = includeLoadInName;
	}
	@Override
	public ColumnData doAction() throws Exception {

		SparrowColumnAttribsBuilder ca = new SparrowColumnAttribsBuilder();
		StringBuilder nameBuilder = new StringBuilder(
			getDataSeriesProperty(seriesType, false, "Yield")
			);
		if(includeLoadInName){
			nameBuilder.append(this.YIELD_LOAD_NAME_DELIMITER);
			nameBuilder.append(loadValuesColumn.getName());
		}

		ca.setName(nameBuilder.toString());
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
                String loadUnitStr = loadValuesColumn.getUnits();
		String areaUnitStr = catchmentAreaColumn.getUnits();

		//Check the model units - correct?
		if (! SparrowUnits.KG_PER_YEAR.equals( modelUnit )) {
			String msg = "The model units must be in " +
			SparrowUnits.KG_PER_YEAR.getUserName() +
			" in order to calculate " + seriesType.toString() + ".  Found instead: " +
			((modelUnit == null)?"null":modelUnit.getUserName());

			addValidationError(msg);
		}
                
		//Check the load units - correct?
		if (! SparrowUnits.KG_PER_YEAR.getUserName().equals( loadUnitStr )) {
			String msg = "The load units must be in " +
			SparrowUnits.KG_PER_YEAR.getUserName() +
			" in order to calculate " + seriesType.toString() + ".  Found instead: " +
			((loadUnitStr == null)?"null":loadUnitStr);

			addValidationError(msg);
		}

		if (! SparrowUnits.SQR_KM.getUserName().equals( areaUnitStr )) {
			String msg = "The area units must be in " +
                            SparrowUnits.SQR_KM.getUserName() +
                            " in order to calculate " + seriesType.toString() + ".  Found instead: " +
                            ((areaUnitStr == null)?"null":areaUnitStr);

			addValidationError(msg);
		}
	}


}
