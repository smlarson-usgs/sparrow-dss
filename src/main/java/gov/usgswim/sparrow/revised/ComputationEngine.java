package gov.usgswim.sparrow.revised;

import gov.usgswim.sparrow.revised.EngineInstructions.CalculationStage;
import gov.usgswim.sparrow.revised.ProductTypes.Product;
import gov.usgswim.sparrow.revised.steps.AggregatePreviousResultStep;
import gov.usgswim.sparrow.revised.steps.ApplyWeightsStep;
import gov.usgswim.sparrow.revised.steps.CompareStep;
import gov.usgswim.sparrow.revised.steps.Step;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction.Levels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
public class ComputationEngine {


	public ComputationEngine(SparrowRequest request) {
		// TODO Auto-generated constructor stub
	}

	/**
	 * Entry method for processing a request to the engine
	 * @param request
	 * @return
	 */
	public Object process(SparrowRequest request) {
		Action mainAction = determineAction(request);
		return process(mainAction, request);

	}

	public Object process(Action action, SparrowRequest request) {
		Collection<Need> needs = action.determinePrereqs(request);
		Map<Need, Object> prereqs = fulfill(needs);
		Object product = action.doAction(prereqs);
		return product;
	}

	public Object processSourceRequest(Action action, SparrowRequest request) {
		// Does request need all sources, or single
		assert(action instanceof LoadPredictDataAction);
		Collection<Need> needs = new ArrayList<Need>();
		{
			SparrowRequest needRequest = request.asDataRequest();
			needs.add(new Need(needRequest, null, ProductTypes.PREDICT_DATA));
		}
		Map<Need, Object> prereqs = fulfill(needs);
		Object product = action.doAction(prereqs);

		return product;

	}

	private Map<Need, Object> fulfill(Collection<Need> needs) {
		HashMap<Need, Object> result = new HashMap<Need, Object>();

		for (Need need: needs) {
			Action localAction = need.action;
			Object product = process(localAction, need.request);
			result.put(need, product);
		}
		return result;
	}

	private Action determineAction(SparrowRequest request) {
		// In most cases, will return EXPORT or NSDATASET or bins
		// in test cases, will return other types
		// Perhaps in future, return reach info or single reach prediction
		return null;
	}
//	LOAD_PREDICTDATA,
//	LOAD_ANCILDATA,
//	LOAD_ERROR,
//	CALCULATE_FLUX_INCREMENTALS,
//	CALCULATE_FLUX_CUMULATIVE,
//	CALCULATE_DELIVERY_FRACTION,
//	CALCULATE_WEIGHT,
//	APPLY_WEIGHTS, // this is not like the others....
//	AGGREGATE;
	public static PredictDataR loadData(Product product) {
		return null;
	}

	public static Object load(Product product) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Object calculate(Product product) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Object applyWeights(Product product, Product... weights) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Object aggregate(Object result, Object agData, Levels level, AggregationFunction aggregationFunction) {
		return null;
	}

	public static Object compare(EngineInstructions basePlan,
			EngineInstructions comparingPlan, Object compareOptions) {
		Object base = execute(basePlan);
		Object compare = execute(comparingPlan);
		//... TODO some more branching based on options;
		return null;
	}
	public static Object execute(EngineInstructions plan) {
		Object lastResult = null;
		for (CalculationStage stage: plan.stages) {
			Step step = stage.step;
			Object result = null;
			switch(stage.step.type) {
				case applyWeights:
					Product[] weights = ((ApplyWeightsStep) step).weights;
					result = applyWeights(step.product, weights);
					break;
				case calculate:
					result = calculate(step.product);
					break;
				case load:
					result = load(step.product);
					break;
				case aggregate:
					AggregatePreviousResultStep agStep = (AggregatePreviousResultStep) step;
					result = aggregate(lastResult, load(ProductTypes.AGGREGATION_DATA.get()), agStep.agLevel, agStep.agFunct);
					break;
				case compare:
					CompareStep cs = (CompareStep) step;
					result = compare(cs.basePlan, cs.comparingPlan, cs.compareOptions);
					break;
			}
			lastResult = result;
		}
		return lastResult;
	}


}
