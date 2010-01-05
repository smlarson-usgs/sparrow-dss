package gov.usgswim.sparrow.revised.steps;

import gov.usgswim.sparrow.revised.ProductTypes.Product;

public class LoadStep extends Step {
	public LoadStep(Product product) {
		super(Step.Type.load, product);
	}
}
