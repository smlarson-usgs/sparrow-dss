package gov.usgswim.sparrow.revised.engine;

import static gov.usgswim.sparrow.revised.engine.ComputationEngineBase.*;
import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.revised.CalculationResult;
import gov.usgswim.sparrow.revised.SparrowContext;
import gov.usgswim.sparrow.revised.ProductTypes.Product;
import gov.usgswim.sparrow.revised.request.SparrowRequest;
public class ComputationEngine {
	public final SparrowRequest request;
	public final SparrowContext context;

	public ComputationEngine(SparrowRequest request) {
		this.request = request;
		this.context = request.getContext();
	}

	public ComputationEngine(SparrowContext context) {
		this.request = null;
		this.context = context;
	}
	
	public CalculationResult loadWeightVariant(String variant) {
		return ComputationEngineBase.loadWeightVariant(variant, context);
	}
	
	public CalculationResult load(Product product) {
		return ComputationEngineBase.load(product, context);
	}
	
	public CalculationResult applyWeights(Product product) {
		return ComputationEngineBase.load(product, context);
	}
	
	public DataTable load(Product product, Product... weights) {
		return ComputationEngineBase.applyWeights(context, product, weights);
	}
	
	public ColumnData[] loadWeights(Product[] weights) {
		return ComputationEngineBase.loadWeights(weights, context);
	}
}
