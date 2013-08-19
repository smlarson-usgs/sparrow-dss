package gov.usgswim.sparrow.action;

import gov.usgs.cida.datatable.ColumnData;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.SparrowModel;

/**
 * This action creates a DataColumn containing the Incremental yield
 * 
 * @author eeverman
 *
 */
public class CalcIncrementalYield extends Action<ColumnData> {

	//The dataseries this Action calculates
	private static final DataSeriesType seriesType = DataSeriesType.incremental_yield;
	
	
	protected SparrowModel model;
	protected PredictResult predictResult;
	protected ColumnData catchmentAreaColumn;
	protected Integer sourceId;
	
	
	//Action initiated values
	private transient CalcAnyYield calcAnyYieldAction;
	
	public CalcIncrementalYield() {
		//default
	}
	
	public CalcIncrementalYield(
			SparrowModel model, PredictResult predictResult,
			ColumnData catchmentAreaColumn, Integer sourceId) {		
		this.model = model;
		this.predictResult = predictResult;
		this.catchmentAreaColumn = catchmentAreaColumn;
		this.sourceId = sourceId;
	}

	@Override
	public ColumnData doAction() throws Exception {
		ColumnData result = calcAnyYieldAction.run();
		return result;
	}

	@Override
	protected void initFields() throws Exception {
		
		//Find appropriate decayed incremental column
		ColumnData decayedInc = null;
		if (sourceId != null) {
			decayedInc = predictResult.getColumn(
					predictResult.getDecayedIncrementalColForSrc(sourceId));
		} else {
			decayedInc = predictResult.getColumn(
					predictResult.getDecayedIncrementalCol());
		}
		
		calcAnyYieldAction = new CalcAnyYield(seriesType, model,
				decayedInc, catchmentAreaColumn);
	}

	@Override
	protected void validate() {
		if (model == null || predictResult == null || catchmentAreaColumn == null) {
			this.addValidationError("All the constructor arguements (except sourceId must be non-null");
		}
	}

}
