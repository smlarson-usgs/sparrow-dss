package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnAttribsBuilder;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.SparrowUnits;
import gov.usgswim.sparrow.datatable.DivideColumnData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnAttribsBuilder;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.DataSeriesType;

/**
 * This action creates a DataColumn containing the Incremental yield
 * 
 * @author eeverman
 *
 */
public class CalcTotalYield extends Action<ColumnData> {

	//The dataseries this Action calculates
	private static final DataSeriesType seriesType = DataSeriesType.total_yield;
	
	
	protected PredictData predictData;
	
	protected PredictResult predictResult;
	
	protected ColumnData watershedAreaColumn;
	
	protected Integer sourceId;
	
	protected String msg;
	
	public CalcTotalYield() {
		//default
	}
	
	public CalcTotalYield(
			PredictData predictData, PredictResult predictResult,
			ColumnData watershedAreaColumn, Integer sourceId) {
		this.predictData = predictData;
		this.predictResult = predictResult;
		this.watershedAreaColumn = watershedAreaColumn;
		this.sourceId = sourceId;
	}

	@Override
	public ColumnData doAction() throws Exception {

		SparrowUnits modelUnit = predictData.getModel().getUnits();
		String areaUnitStr = watershedAreaColumn.getUnits();
		SparrowUnits areaUnit = SparrowUnits.parseUserName(areaUnitStr);
		
		//Check the units - correct?
		if (! predictData.getModel().getUnits().equals(SparrowUnits.KG_PER_YEAR)) {
			msg = "The model units must be in " +
			SparrowUnits.KG_PER_YEAR.getUserName() +
			" in order to calculate " + seriesType.toString() + ".  Found instead: " + 
			((modelUnit == null)?"null":modelUnit.getUserName());
			throw new Exception(msg);
		}
		
		if (! watershedAreaColumn.getUnits().equals(SparrowUnits.SQR_KM.getUserName())) {
			msg = "The area units must be in " +
			SparrowUnits.SQR_KM.getUserName() +
			" in order to calculate " + seriesType.toString() + ".  Found instead: " +
			((areaUnit == null)?"null":areaUnit.getUserName());
			throw new Exception(msg);
		}
		
		
		SparrowColumnAttribsBuilder ca = new SparrowColumnAttribsBuilder();
		ca.setName(getDataSeriesProperty(seriesType, false));
		ca.setDescription(getDataSeriesProperty(seriesType, true));
		ca.setUnits(SparrowUnits.KG_PER_SQR_KM_PER_YEAR.getUserName());
		
		ca.setModelId(predictData.getModel().getId());		
		ca.setConstituent(predictData.getModel().getConstituent());
		ca.setBaseDataSeriesType(seriesType.getBaseType());
		ca.setDataSeriesType(seriesType);

		//Find appropriate total column
		ColumnData decayedTotal = null;
		if (sourceId != null) {
			decayedTotal = predictResult.getColumn(
					predictResult.getTotalColForSrc(sourceId));
		} else {
			decayedTotal = predictResult.getColumn(
					predictResult.getTotalCol());
		}
		
		DivideColumnData result =
			new DivideColumnData(decayedTotal, watershedAreaColumn, ca.toImmutable());
		
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
		return watershedAreaColumn;
	}

	public void setCatchmentAreaColumn(ColumnData catchmentAreaColumn) {
		this.watershedAreaColumn = catchmentAreaColumn;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}





}
