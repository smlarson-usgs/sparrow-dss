package gov.usgswim.sparrow.action;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.impl.ColumnFromTable;
import gov.usgswim.datatable.impl.SimpleDataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.UncertaintyData;
import gov.usgswim.sparrow.UncertaintyDataRequest;
import gov.usgswim.sparrow.UncertaintySeries;
import gov.usgswim.sparrow.cachefactory.CatchmentArea;
import gov.usgswim.sparrow.datatable.ColumnAttribsBuilder;
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
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;


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

		if (analysis.isAggregated()) {
			AggregationRunner aggRunner = new AggregationRunner(context);
			DataTable aggResult = aggRunner.doAggregation(unAggResult.getTable());
			return new DataColumn(aggResult, unAggResult.getColumn(), context.getId());

		} else {
			return unAggResult;
		}
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
//			Set<Long> targetReaches = tReaches.asSet();
			
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
					//All TOTAL type series fall through to this point
					
					
					//All total series share the ability to be about a specific
					//source.
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
						
						ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
						ca.setName("Total Delivered Flux");
						ca.setDescription("The load arriving at the " + 
								"bottom of this reach that will arrive at the " + 
								"bottom of the specified target reach(es)");
						
						SingleColumnCoefDataTable view = new SingleColumnCoefDataTable(
								result, delFracColumn, dataColIndex, ca);
						
						predictionBasedResult = view;
					} else if (type.equals(DataSeriesType.total_concentration)){
						// total conc. is total load / flux(stream flow).
						
						ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
						ca.setName("Concentration");
						ca.setDescription("The flux of the constituent divided " +
								"by the stream flow.");
						ca.setUnits("mg/L");
						
						DataTable fluxTable = SharedApplication.getInstance().getFlux(context.getModelID());
						ColumnData fluxColumn = new ColumnFromTable(fluxTable, 1);
						SingleColumnCoefDataTable view = new SingleColumnCoefDataTable(result, fluxColumn, dataColIndex, ca, true);
						predictionBasedResult = view;
					} else {
						predictionBasedResult = result;
					}
					
					break;

				case incremental: // intentional fall-through
				case decayed_incremental:
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
						// assume decayed inc. flux
						
						ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
						ca.setName("Incremental Yield");
						ca.setDescription("Incremental Flux (decayed) divided by the catchment area.");
						ca.setUnits("kg/sqr km");
						
						ColumnData instDecay = new ColumnFromTable(
								nominalPredictData.getDelivery(), PredictData.INSTREAM_DECAY_COL);
						SingleColumnCoefDataTable decayedFlux = 
							new SingleColumnCoefDataTable(result, instDecay, dataColIndex, null);
						
						CatchmentArea catchArea = new CatchmentArea(context.getModelID(), HucLevel.HUC_NONE, false);
						DataTable catchmentAreaTable = SharedApplication.getInstance().getCatchmentAreas(catchArea);
						ColumnData catchmentAreaColumn = new ColumnFromTable(catchmentAreaTable, 1);
						SingleColumnCoefDataTable view = new SingleColumnCoefDataTable(
								decayedFlux, catchmentAreaColumn, dataColIndex, ca, true);
						
						predictionBasedResult = view;
						
					} else if (type.equals(DataSeriesType.decayed_incremental)) {
						
						// decayed inc. flux is inc. flux multiplied by instream decay
						
						ColumnAttribsBuilder ca = new ColumnAttribsBuilder();
						ca.setName("Incremental Flux (Decayed)");
						ca.setDescription("Incremental Flux, decayed to the end of the reach.");
						ca.setUnits(null);	//default is correct
						
						ColumnData instDecay = new ColumnFromTable(
								nominalPredictData.getDelivery(), PredictData.INSTREAM_DECAY_COL);
						SingleColumnCoefDataTable decayedFlux = 
							new SingleColumnCoefDataTable(result, instDecay, dataColIndex, ca);
						
						predictionBasedResult = decayedFlux;
						
					} else if (type.equals(DataSeriesType.incremental_delivered_flux)
							|| type.equals(DataSeriesType.incremental_delivered_yield)) {

						// incremental delivered flux =
						// Delivery Fraction X Incremental Flux X Instream Delivery
						// This is built w/ two coef table views:  delFrac X inc Flux,
						// then that result times Instream Delivery.
						
						ColumnAttribsBuilder DelFluxCa = new ColumnAttribsBuilder();
						DelFluxCa.setName("Incremental Delivered Flux");
						DelFluxCa.setDescription("Flux from an individual reach that arrives at the downstream end of the target.");
						DelFluxCa.setUnits(null);	//default is correct
						
						
						
						//Grab the instream delivery (called decay) as a column
						ColumnData incDeliveryCol = new ColumnFromTable(nominalPredictData.getDelivery(), PredictData.INSTREAM_DECAY_COL);
						

						// Delivery Fraction X Incremental Flux
						// Column is slightly misnamed, but is never used.
						SingleColumnCoefDataTable incTimesDelFrac = new SingleColumnCoefDataTable(
								result, delFracColumn, dataColIndex, null);
						

						// The above result X Instream Delivery
						SingleColumnCoefDataTable incDeliveredFlux = new SingleColumnCoefDataTable(
								incTimesDelFrac, incDeliveryCol, dataColIndex, DelFluxCa);
												
						if (type.equals(DataSeriesType.incremental_delivered_yield)) {
							// inc. del. yield is inc. del. flux / catchment area
							
							ColumnAttribsBuilder DelYieldCa = new ColumnAttribsBuilder();
							DelYieldCa.setName("Incremental Delivered Yield");
							DelYieldCa.setDescription("Flux divided by area for " +
									"an individual reach, that arrives at the " +
									"downstream end of the target");
							DelYieldCa.setUnits("Del Yield Units");
							
							CatchmentArea ca = new CatchmentArea(context.getModelID(), HucLevel.HUC_NONE, false);
							DataTable catchmentAreaTable = SharedApplication.getInstance().getCatchmentAreas(ca);
							ColumnData catchmentAreaColumn = new ColumnFromTable(catchmentAreaTable, 1);
							SingleColumnCoefDataTable incDeliveredYield = new SingleColumnCoefDataTable(
									incDeliveredFlux, catchmentAreaColumn, dataColIndex, DelYieldCa, true);
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
					CatchmentArea ca = new CatchmentArea(context.getModelID(), HucLevel.HUC_NONE, false);
					dataTable = SharedApplication.getInstance().getCatchmentAreas(ca);
					dataColIndex = 1;
					break;
				case flux:
					dataTable = SharedApplication.getInstance().getFlux(context.getModelID());
					dataColIndex = 1;
					break;
				default:
					throw new Exception("No dataSeries was specified in the analysis section");
			}
		}

		return new DataColumn(dataTable, dataColIndex, context.getId());
	}
}
