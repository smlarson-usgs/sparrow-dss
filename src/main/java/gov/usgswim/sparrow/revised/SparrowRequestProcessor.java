package gov.usgswim.sparrow.revised;

import static gov.usgswim.sparrow.revised.steps.Step.applyWeights;
import static gov.usgswim.sparrow.revised.steps.Step.calculate;
import static gov.usgswim.sparrow.revised.steps.Step.compare;
import static gov.usgswim.sparrow.revised.steps.Step.load;
import static gov.usgswim.sparrow.revised.EngineInstructions.Transformation.TO_NSDATASET;
import static gov.usgswim.sparrow.revised.ProductTypes.CUMULATIVE_FLUX;
import static gov.usgswim.sparrow.revised.ProductTypes.DELIVERY_FRACTION;
import static gov.usgswim.sparrow.revised.ProductTypes.INCREMENTAL_FLUX;
import static gov.usgswim.sparrow.revised.ProductTypes.PREDICT_DATA;
import static gov.usgswim.sparrow.revised.ProductTypes.WEIGHT;
import gov.usgswim.sparrow.revised.engine.ComputationEngineBase;
import gov.usgswim.sparrow.revised.request.SparrowRequest;
import gov.usgswim.sparrow.revised.steps.Step;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction;

public class SparrowRequestProcessor {
	/**
	 * Enum mapping the UI data series to actual ComputationEngineBase operations
	 * @author ilinkuo
	 *
	 */
	public static enum DataSeriesTypes {
		SOURCE,
		INCR_FLUX,
		TOTAL_FLUX,
		INCREMENTAL_YIELD,
		CONCENTRATION,
		//
		DELIVERY_FRACTION,
		INCREMENTAL_DELIVERED_FLUX,
		TOTAL_DELIVERED_FLUX,
		INCREMENTAL_DELIVERED_YIELD,
		//
		TOTAL_STANDARD_ERR_ESTIMATE,
		INCREMENTAL_STANDARD_ERR_ESTIMATE;
	}

	public EngineInstructions createExecutionPlan(SparrowRequest request) {
		if (request.requiresComparison()) {
			return processComparison(request);
		} else if (request.requiresAggregation()){
			return createAggregationExecutionPlan(request);
		} else {
			// This is a simple, basic calculation
			DataSeriesTypes baseDataSeriesType = request.getDataSeries();
			EngineInstructions plan = new EngineInstructions(request);
			switch(baseDataSeriesType) {
				case SOURCE:
					plan.addStep( load(PREDICT_DATA.get()) ).cacheStepResultIn("PredictDataCache");
					break;
				case INCR_FLUX:
					plan.addStep( calculate(INCREMENTAL_FLUX.get()) );
					break;
				case TOTAL_FLUX:
					plan.addStep( calculate(CUMULATIVE_FLUX.get()) );
					break;
				case INCREMENTAL_YIELD:
					plan.addStep( applyWeights(INCREMENTAL_FLUX.get(), WEIGHT.get("yield")) );
					break;
				case CONCENTRATION:
					plan.addStep( applyWeights(CUMULATIVE_FLUX.get(), WEIGHT.get("concentration")) );
					break;
				case DELIVERY_FRACTION:
					plan.addStep( calculate(DELIVERY_FRACTION.get()) );
					break;
				case INCREMENTAL_DELIVERED_FLUX:
					plan.addStep( applyWeights(INCREMENTAL_FLUX.get(), WEIGHT.get("delivery_fraction"), WEIGHT.get("instream_decay_coef"), WEIGHT.get("iftran")) );
					break;
				case TOTAL_DELIVERED_FLUX:
					plan.addStep( applyWeights(CUMULATIVE_FLUX.get(), WEIGHT.get("delivery_fraction"), WEIGHT.get("upstream_decay_coef"), WEIGHT.get("iftran")) );
					break;
				case INCREMENTAL_DELIVERED_YIELD:
					plan.addStep( applyWeights(INCREMENTAL_FLUX.get(), WEIGHT.get("delivery_fraction"), WEIGHT.get("instream_decay_coef"), WEIGHT.get("iftran"), WEIGHT.get("yield")) );
					break;
				case TOTAL_STANDARD_ERR_ESTIMATE:
					plan.addStep( applyWeights(CUMULATIVE_FLUX.get(), WEIGHT.get("ERROR for source")) );
					break;
				case INCREMENTAL_STANDARD_ERR_ESTIMATE:
					plan.addStep( applyWeights(INCREMENTAL_FLUX.get(), WEIGHT.get("ERROR for source")) );
					break;
			}
			return plan;
		}
	}

	public CalculationResult process(SparrowRequest request) {
		EngineInstructions plan = createExecutionPlan(request);
		plan.addTransformation(TO_NSDATASET, request.getSource());
		return ComputationEngineBase.executePlan(plan);

	}

	public EngineInstructions createAggregationExecutionPlan(SparrowRequest request) {
		assert(request.requiresAggregation());
		AggregationFunction.Levels agLevel = request.getAggregationLevel();
		AggregationFunction agFunct = request.getAggregationFunction();
		// load aggregation data (h2,4,6,8,reach)
		EngineInstructions plan = createExecutionPlan(request.stripAggregation());
		plan.addStep(Step.aggregatePreviousResult(agLevel, agFunct));
		return plan;
	}

	private EngineInstructions processComparison(SparrowRequest request) {
		Object compareOptions = request.getCompareOptions();
		SparrowRequest baseRequest = request.makeBaseRequest();
		SparrowRequest comparingRequest = request.makeComparingRequest();
		EngineInstructions basePlan = createExecutionPlan(baseRequest);
		EngineInstructions comparingPlan = createExecutionPlan(comparingRequest);

		EngineInstructions plan = new EngineInstructions(request);
		plan.addStep(compare(basePlan, comparingPlan, compareOptions));
		return plan;
	}

}
