package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.ColumnAttribsBuilder;
import gov.usgswim.sparrow.datatable.DivideColumnData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;

/**
 * This action creates a DataColumn containing the Incremental yield
 * 
 * @author eeverman
 *
 */
public class CalcIncrementalYield extends Action<ColumnData> {

	//The dataseries this Action calculates
	private static final DataSeriesType seriesType = DataSeriesType.incremental_yield;
	
	
	protected PredictData predictData;
	
	protected PredictResult predictResult;
	
	protected ColumnData catchmentAreaColumn;
	
	protected Integer sourceId;
	
	protected String msg;
	
	public CalcIncrementalYield() {
		//default
	}
	
	public CalcIncrementalYield(
			PredictData predictData, PredictResult predictResult,
			ColumnData catchmentAreaColumn, Integer sourceId) {
		this.predictData = predictData;
		this.predictResult = predictResult;
		this.catchmentAreaColumn = catchmentAreaColumn;
		this.sourceId = sourceId;
	}

	@Override
	public ColumnData doAction() throws Exception {

		SparrowUnits modelUnit = predictData.getModel().getUnits();
		String areaUnitStr = catchmentAreaColumn.getUnits();
		SparrowUnits areaUnit = SparrowUnits.parseUserName(areaUnitStr);
		
		//Check the units - correct?
		if (! predictData.getModel().getUnits().equals(SparrowUnits.KG_PER_YEAR)) {
			msg = "The model units must be in " +
			SparrowUnits.KG_PER_YEAR.getUserName() +
			" in order to calculate Incremental Yield.  Found instead: " + 
			((modelUnit == null)?"null":modelUnit.getUserName());
			throw new Exception(msg);
		}
		
		if (! catchmentAreaColumn.getUnits().equals(SparrowUnits.SQR_KM.getUserName())) {
			msg = "The area units must be in " +
			SparrowUnits.SQR_KM.getUserName() +
			" in order to calculate Incremental Yield.  Found instead: " +
			((areaUnit == null)?"null":areaUnit.getUserName());
			throw new Exception(msg);
		}
		
		
		ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
		ca.setName(getDataSeriesProperty(seriesType, false));
		ca.setDescription(getDataSeriesProperty(seriesType, true));
		ca.setUnits(SparrowUnits.KG_PER_SQR_KM_PER_YEAR.getUserName());
		
		ca.setProperty(TableProperties.MODEL_ID.getPublicName(), predictData.getModel().getId().toString());		
		ca.setProperty(TableProperties.CONSTITUENT.getPublicName(), predictData.getModel().getConstituent());
		ca.setProperty(TableProperties.DATA_TYPE.getPublicName(), seriesType.getBaseType().name());
		ca.setProperty(TableProperties.DATA_SERIES.getPublicName(), seriesType.name());
		ca.setProperty(TableProperties.ROW_AGG_TYPE.getPublicName(), TableProperties.ROW_LEVEL.getPublicName());
		
		ColumnData decayedInc = null;
		if (sourceId != null) {
			decayedInc = predictResult.getColumn(
					predictResult.getDecayedIncrementalColForSrc(sourceId));
		} else {
			decayedInc = predictResult.getColumn(
					predictResult.getDecayedIncrementalCol());
		}
		
		DivideColumnData result =
			new DivideColumnData(decayedInc, catchmentAreaColumn, ca.toImmutable());
		
		return result;
	}

	@Override
	protected String getPostMessage() {
		return msg;
	}
	
	public PredictData getPredictData() {
		return predictData;
	}

	public void setPredictData(PredictData predictData) {
		this.predictData = predictData;
	}

	public PredictResult getPredictResult() {
		return predictResult;
	}

	public void setPredictResult(PredictResult predictResult) {
		this.predictResult = predictResult;
	}

	public ColumnData getCatchmentAreaColumn() {
		return catchmentAreaColumn;
	}

	public void setCatchmentAreaColumn(ColumnData catchmentAreaColumn) {
		this.catchmentAreaColumn = catchmentAreaColumn;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}





}
