package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.ColumnFromTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.UncertaintySeries;
import gov.usgswim.sparrow.datatable.HucLevel;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SingleColumnCoefDataTable;
import gov.usgswim.sparrow.datatable.StdErrorEstTable;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.DataColumn;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.WeightRunner;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import java.util.Set;


/**
 *  Calculates the analysis.
 *  The basic process followed in this class is:
 * <ul>
 * <li>Run the prediction and if needed the nominal prediction by calling getPredictResult()
 * <li>Do the analysis based on the Analysis section of the PredictionContext.
 * </ul>
 * @author eeverman
 *
 */
public class CalcAnalysis extends Action<DataColumn>{

	protected PredictionContext context;
	
	
	public PredictionContext getContext() {
		return context;
	}

	public void setContext(PredictionContext context) {
		this.context = context;
	}
	
	@Override
	protected DataColumn doAction() throws Exception {
		Analysis analysis = context.getAnalysis();

		DataColumn unAggResult = getDataColumn(context);
		DataTable aggResult = null;

		if (analysis.isAggregated()) {
			AggregationRunner aggRunner = new AggregationRunner(context);
			aggResult = aggRunner.doAggregation(unAggResult.getTable());
			// Aggregation can handle weighting underneath
		} else if (analysis.isWeighted()) {
			aggResult = WeightRunner.doWeighting(context, unAggResult.getTable());
		} else {
			aggResult = unAggResult.getTable();
		}

		return new DataColumn(aggResult, unAggResult.getColumn(), context.getId());
	}

	/**
	 * Centralized method to get a reference to the data table and a column in it
	 * for use any place we need to access the data column. The data column will
	 * be used for the map coloring
	 *
	 * @return
	 * @throws Exception
	 */
	public DataColumn getDataColumn(PredictionContext context) throws Exception {
		int dataColIndex = -1;	//The index of the data column
		DataTable dataTable = null;		//The table containing the data column

		//This will be created anyway, so just grab reference here
		PredictData nominalPredictData = SharedApplication.getInstance().getPredictData(context.getModelID());
		
		DataSeriesType type = context.getAnalysis().getDataSeries();
		Integer source = context.getAnalysis().getSource();
		ColumnData delFracColumn = null;	//will be populated if required
		
		
		//populate delivery if needed
		if (type.isDeliveryRequired()) {
			//avoid cache for now
			// PredictResult result = SharedApplication.getInstance().getAnalysisResult(this);
			TerminalReaches tReaches = context.getTerminalReaches();

			assert(tReaches != null) : "client should not submit a delivery request without reaches";
			Set<Long> targetReaches = tReaches.asSet();
			
			delFracColumn = SharedApplication.getInstance().getDeliveryFraction(tReaches);
		}


		// Handled DataSeriesType: total, incremental, incremental_yield, total_concentration, source_values
		if (type.isDeliveryBased()) {

			switch(type) {
				case delivered_fraction: {
					dataColIndex = 0; // only a single column for delivery fraction as it is not source dependent
					
					SimpleDataTable sdt = new SimpleDataTable(
							new ColumnData[] {delFracColumn}, "Delivery Fraction",
							"A single column table containing the delivery fraction" +
							" to a target reach or reaches.", null, null
						);
					
					dataTable = sdt;
					break;
				}

				default:
					throw new Exception("No dataSeries was specified in the analysis section");
			}

		} else if (type.isPredictionBased()) {

			//We will try to get result-based series out of the analysis cache
			PredictResult result = SharedApplication.getInstance().getPredictResult(context.getAdjustmentGroups());
			UncertaintySeries impliedUncertaintySeries = null;
			DataTable predictionBasedResult = null;

			switch (type) {
				case total: // intentional fall-through
				case total_std_error_estimate:
				case total_concentration:
				case total_delivered_flux:
					//
					//Note:  All TOTAL type series fall through to this point
					//
					if (source != null) {
						dataColIndex = result.getTotalColForSrc(source.longValue());
						impliedUncertaintySeries = UncertaintySeries.TOTAL_PER_SOURCE;
					} else {
						dataColIndex = result.getTotalCol();
						impliedUncertaintySeries = UncertaintySeries.TOTAL;
					}
					
					
					if (type.equals(DataSeriesType.total_delivered_flux)) {
						//Create a new datatable that overlays the delFracColumn
						//on top of the column selected above
						SingleColumnCoefDataTable view = new SingleColumnCoefDataTable(
								result, delFracColumn, dataColIndex);
						
						predictionBasedResult = view;
					} else {
						predictionBasedResult = result;
					}
					
					break;

				case incremental: // intentional fall-through
				case incremental_std_error_estimate:
				case incremental_yield:
				case incremental_delivered_flux: // here, I think
				case incremental_delivered_yield: // here, I think
					//
					//Note:  All INC type series fall through to this point
					//
					if (source != null) {
						dataColIndex = result.getIncrementalColForSrc(source.longValue());
						impliedUncertaintySeries = UncertaintySeries.INCREMENTAL_PER_SOURCE;
					} else {
						dataColIndex = result.getIncrementalCol();
						impliedUncertaintySeries = UncertaintySeries.INCREMENTAL;
					}
					
					if (type.equals(DataSeriesType.incremental_yield)) {
						// incremental yield = incremental flux / catchment area
						LoadUnitAreas lua = new LoadUnitAreas(context.getModelID(), HucLevel.HUC_NONE, false);
						DataTable catchmentAreaTable = lua.run();
						ColumnData catchmentAreaColumn = new ColumnFromTable(catchmentAreaTable, 1);
						SingleColumnCoefDataTable view = new SingleColumnCoefDataTable(
								result, catchmentAreaColumn, dataColIndex, true);
						predictionBasedResult = view;
					} else if (type.equals(DataSeriesType.incremental_delivered_flux)
							|| type.equals(DataSeriesType.incremental_delivered_yield)) {

						// incremental delivered flux =
						// Delivery Fraction X Incremental Flux X Instream Delivery
						// This is built w/ two coef table views:  delFrac X inc Flux,
						// then that result times Instream Delivery.
						
						//Grab the instream delivery (called decay) as a column
						ColumnData incDeliveryCol = new ColumnFromTable(nominalPredictData.getDelivery(), PredictData.INSTREAM_DECAY_COL);
						

						// Delivery Fraction X Incremental Flux
						SingleColumnCoefDataTable incTimesDelFrac = new SingleColumnCoefDataTable(
								result, delFracColumn, dataColIndex);
						

						// The above result X Instream Delivery
						SingleColumnCoefDataTable incDeliveredFlux = new SingleColumnCoefDataTable(
								incTimesDelFrac, incDeliveryCol, dataColIndex);
												
						if (type.equals(DataSeriesType.incremental_delivered_yield)) {
							// inc. del. yield is inc. del. flux / catchment area
							LoadUnitAreas lua = new LoadUnitAreas(context.getModelID(), HucLevel.HUC_NONE, false);
							DataTable catchmentAreaTable = lua.run();
							ColumnData catchmentAreaColumn = new ColumnFromTable(catchmentAreaTable, 1);
							SingleColumnCoefDataTable incDeliveredYield = new SingleColumnCoefDataTable(
									incDeliveredFlux, catchmentAreaColumn, dataColIndex, true);
							predictionBasedResult = incDeliveredYield;
						}
						else {
							predictionBasedResult = incDeliveredFlux;
						}
						
					} else {
						predictionBasedResult = result;
					}
					
					
					
					break;
				default:
					throw new Exception("No dataSeries was specified in the analysis section");
			}

			if (type.isStandardErrorEstimateBased()) {
				UncertaintyDataRequest req = new UncertaintyDataRequest(
						context.getModelID(), impliedUncertaintySeries, source);
				UncertaintyData errData = SharedApplication.getInstance().getStandardErrorEstimateData(req);

				//Construct a datatable that calculates the error for each
				//value on demand.
				predictionBasedResult = new StdErrorEstTable(result, errData,
						dataColIndex, true, 0d);

			}


			dataTable = predictionBasedResult;

		} else {
			
			// not delivery based, prediction based, or standard error estimate based

			switch (type) {
				case source_value:
					if (source != null) {

						//Get the predict data, which uses the PredictData interface
						PredictData nomPredictData = SharedApplication.getInstance().getPredictData(context.getModelID());
						dataColIndex = nomPredictData.getSourceIndexForSourceID(source);

						dataTable = SharedApplication.getInstance().getAdjustedSource(context.getAdjustmentGroups());

					} else {
						throw new Exception("The data series 'source_value' requires a source ID to be specified.");
					}
					break;
				case catch_area:
					LoadUnitAreas lua = new LoadUnitAreas(context.getModelID(), HucLevel.HUC_NONE, false);
					dataTable = lua.run();
					dataColIndex = 1;
					break;
				default:
					throw new Exception("No dataSeries was specified in the analysis section");
			}
		}

		return new DataColumn(dataTable, dataColIndex, context.getId());
	}
}
