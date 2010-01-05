package gov.usgswim.sparrow.revised.steps;

import gov.usgswim.sparrow.revised.ProductTypes;
import gov.usgswim.sparrow.revised.ProductTypes.Product;

public class CalculateStep extends Step {
	public CalculateStep(Product product) {
		super(Step.Type.calculate, product);
	}
}
