package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.DeliveryRunner;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.UncertaintySeries;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.StdErrorEstTable;
import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.parser.PredictionContext.DataColumn;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.WeightRunner;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import java.util.Set;

import org.apache.log4j.Logger;


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
public class CalcAnalysis extends Action<PredictionContext.DataColumn>{


	protected static Logger log =
		Logger.getLogger(CalcAnalysis.class); //logging for this class

	protected PredictionContext context;
	
	
	public PredictionContext getContext() {
		return context;
	}

	public void setContext(PredictionContext context) {
		this.context = context;
	}
	
	@Override
	protected PredictionContext.DataColumn doAction() throws Exception {
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

		return new PredictionContext.DataColumn(aggResult, unAggResult.getColumn());
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

		DataSeriesType type = context.getAnalysis().getDataSeries();
		Integer source = context.getAnalysis().getSource();


		// Handled DataSeriesType: total, incremental, incremental_yield, total_concentration, source_values
		if (type.isDeliveryBased()) {
			//avoid cache for now
			// PredictResult result = SharedApplication.getInstance().getAnalysisResult(this);
			TerminalReaches tReaches = context.getTerminalReaches();

			assert(tReaches != null) : "client should not submit a delivery request without reaches";
			Set<Long> targetReaches = tReaches.asSet();

			PredictData nominalPredictData = SharedApplication.getInstance().getPredictData(context.getModelID());
			DeliveryRunner dr = new DeliveryRunner(nominalPredictData);

			switch(type) {
				case delivered_fraction:
					dataColIndex = 0; // only a single column for delivery fraction as it is not source dependent
					// TODO get from cache
					dataTable = dr.calculateReachTransportFractionDataTable(targetReaches);
					break;
				case total_delivered_flux:

					//PredictResult result = SharedApplication.getInstance().getAnalysisResult(this);
					PredictResult result = dr.calculateDeliveredFlux(context);
					dataTable = result;
					// NOTE: must handle aggregation and comparison before this stage
					// Note that comparison does not make sense for delivered
					if (source != null) {
						dataColIndex = result.getTotalColForSrc(source.longValue());
					} else {
						dataColIndex = result.getTotalCol();
					}
					break;
				case incremental_delivered_flux:
					//result = SharedApplication.getInstance().getAnalysisResult(this);
					result = dr.calculateDeliveredFlux(context);
					dataTable = result;
					if (source != null) {
						dataColIndex = result.getIncrementalColForSrc(source.longValue());
					} else {
						dataColIndex = result.getIncrementalCol();
					}
					break;
				case incremental_delivered_yield:
					//result = SharedApplication.getInstance().getAnalysisResult(this);
					result = dr.calculateDeliveredFlux(context);
					dataTable = result;
					if (source != null) {
						dataColIndex = result.getIncrementalColForSrc(source.longValue());
					} else {
						dataColIndex = result.getIncrementalCol();
					}
					break;
				default:
					throw new Exception("No dataSeries was specified in the analysis section");
			}

		} else if (type.isPredictionBased()) {

			//We will try to get result-based series out of the analysis cache
			PredictResult result = SharedApplication.getInstance().getPredictResult(context);
			UncertaintySeries impliedUncertaintySeries = null;

			switch (type) {
				case total: // intentional fall-through
				case total_std_error_estimate:
				case total_concentration:
				case total_delivered_flux:
					if (source != null) {
						dataColIndex = result.getTotalColForSrc(source.longValue());
						impliedUncertaintySeries = UncertaintySeries.TOTAL_PER_SOURCE;
					} else {
						dataColIndex = result.getTotalCol();
						impliedUncertaintySeries = UncertaintySeries.TOTAL;
					}
					break;

				case incremental: // intentional fall-through
				case incremental_std_error_estimate:
				case incremental_yield:
				case incremental_delivered_flux: // here, I think
				case incremental_delivered_yield: // here, I think
					if (source != null) {
						dataColIndex = result.getIncrementalColForSrc(source.longValue());
						impliedUncertaintySeries = UncertaintySeries.INCREMENTAL_PER_SOURCE;
					} else {
						dataColIndex = result.getIncrementalCol();
						impliedUncertaintySeries = UncertaintySeries.INCREMENTAL;
					}
					break;
				case delivered_fraction:
					// ignore source
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
				dataTable = new StdErrorEstTable(result, errData,
						dataColIndex, true, 0d);

			} else {
				dataTable = result;
			}




		} else {



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
				default:
					throw new Exception("No dataSeries was specified in the analysis section");
			}
		}

		return new PredictionContext.DataColumn(dataTable, dataColIndex);
	}
}
