package gov.usgswim.sparrow.revised.steps;

import gov.usgswim.sparrow.revised.ProductTypes.Product;

import java.util.Arrays;

public class ApplyWeightsStep extends Step {
	public Product[] weights;

	public ApplyWeightsStep(Product product, Product[] weights) {
		super(Type.applyWeights, product);
		this.weights = weights;
	}

	@Override
	public String toString() {
		return String.format("applyWeights(%s) to  %s", Arrays.toString(weights), product.toString());
	}
}
