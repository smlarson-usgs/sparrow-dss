/**
 *
 */
package gov.usgswim.sparrow.revised.steps;

import gov.usgswim.sparrow.revised.EngineInstructions;
import gov.usgswim.sparrow.revised.ProductTypes.Product;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction.Levels;

public class Step{
	public static enum Type{
		load, calculate, applyWeights, compare, aggregate;
	}

	public final Step.Type operation;
	public Product product;



	protected Step(Step.Type type, Product product) {
		this.operation = type;
		this.product = product;
	}

	@Override public String toString() {return operation.name()+ "(" + product.toString() + ")";};
	// ==========================
	// STATIC CONVENIENCE METHODS
	// ==========================
	public static Step load(Product product) {
		return new LoadStep(product);
	}

	public static Step calculate(Product product) {
		return new CalculateStep(product);
	}

	public static Step applyWeights(Product product, Product... weights) {
		return new ApplyWeightsStep(product, weights);
	}


	public static Step compare(EngineInstructions basePlan,
			EngineInstructions comparingPlan, Object compareOptions) {
		return new CompareStep(basePlan, comparingPlan, compareOptions);
	}

	public static Step aggregatePreviousResult(Levels agLevel, AggregationFunction agFunct) {
		return new AggregatePreviousResultStep(agLevel, agFunct);
	}
}