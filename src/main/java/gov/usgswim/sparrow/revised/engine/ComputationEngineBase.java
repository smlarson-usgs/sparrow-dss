package gov.usgswim.sparrow.revised.engine;

import gov.usgswim.datatable.ColumnData;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.revised.CalculationResult;
import gov.usgswim.sparrow.revised.CalculationStage;
import gov.usgswim.sparrow.revised.EngineInstructions;
import gov.usgswim.sparrow.revised.ProductTypes;
import gov.usgswim.sparrow.revised.SparrowContext;
import gov.usgswim.sparrow.revised.ProductTypes.Product;
import gov.usgswim.sparrow.revised.request.SparrowRequest;
import gov.usgswim.sparrow.revised.steps.AggregatePreviousResultStep;
import gov.usgswim.sparrow.revised.steps.ApplyWeightsStep;
import gov.usgswim.sparrow.revised.steps.CompareStep;
import gov.usgswim.sparrow.revised.steps.Step;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction;
import gov.usgswim.sparrow.revised.steps.aggregation.AggregationFunction.Levels;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
public class ComputationEngineBase {


	public final SparrowRequest request;
	public final SparrowContext context;

	public ComputationEngineBase(SparrowRequest request) {
		this.request = request;
		this.context = request.getContext();
	}

	public ComputationEngineBase(SparrowContext context) {
		this.request = null;
		this.context = context;
	}

	// ===========================
	// PROCESSING METHODS (STATIC)
	// ===========================



	public static CalculationResult load(Product product, SparrowContext context) {
		assert(product != null);
		CalculationResult result = new CalculationResult();
		Connection conn = null;
		try {
//			conn = getConnection();
			switch(product.type) {
				case PREDICT_DATA:
//					PredictData data = DataLoader.loadModelDataOnly(conn, context.getModelId());
					//PredictData data = DataResourceLoader.loadModelData(context.getModelId());
					//TODO:  I'm removing the resource loader's ability to load models,
					//so this functionality will be broken.
					PredictData data = null;
					result.predictData = data;
					result.table = retrievePredictDataVariant(data, product.variant);
					break;
				case WEIGHT:
 					result = loadWeightVariant(product.variant, context);
					break;
				case ANCIL_DATA:
					//break;
				default:
					throw new UnsupportedOperationException("loading of " + product.type.name() + " not currently supported");
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			if (conn != null) {
				try { conn.close(); } catch (SQLException e) {}
			}
		}
		return result;
	}



	public static CalculationResult loadWeightVariant(String variant, SparrowContext context) {
		if (variant == null ) return null;

		CalculationResult temp = null;
		if ("yield".equals(variant)) {

		}
		if ("delivery".equals(variant)) {

		}
		if ("concentration".equals(variant)) {

		}
		if ("iftran".equals(variant)) {

		}
		if ("coef".equals(variant)) {
			return load(ProductTypes.PREDICT_DATA.get("coef"), context);
		}
		if ("decay".equals(variant)) {
		}
		return null;
	}


	public static CalculationResult calculate(Product product, SparrowContext context) {
		assert(product != null);
		CalculationResult result = new CalculationResult();
		try {

			switch(product.type) {
				case INCREMENTAL_FLUX:
					CalculationResult tempResult = load(ProductTypes.PREDICT_DATA.get("source"), context );
					// TODO make incremental calculation a special case of applyWeights
					result.table = FluxRunner.calculateIncrementalFlux(tempResult.predictData);

//					DataTable incrementalFlux =
					break;
				default:
					throw new UnsupportedOperationException("calculate() not curruently applicable to " + product.type.name() );
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
//			if (conn != null) {
//				try { conn.close(); } catch (SQLException e) {}
//			}
		}
		return result;
	}

	public static DataTable applyWeights(SparrowContext context, Product product, Product... weights) {
		assert(product.isLoadable()): "Temporary restriction on products, to be removed later";
		CalculationResult result = null;
		if (product.isLoadable()) {
			result = load(product, context);
		}
		ColumnData[] weightData = loadWeights(weights, context);
		return null;
	}

	public static ColumnData[] loadWeights(Product[] weights, SparrowContext context) {
		List<ColumnData> loadedWeights= new ArrayList<ColumnData>();
		for (Product product: weights) {
			assert(product.type == ProductTypes.WEIGHT);
			CalculationResult result = load(product, context);
			loadedWeights.add(result.column);
		}
		return loadedWeights.toArray(new ColumnData[0]);
	}

	public static Object aggregate(Object result, Object agData, Levels level, AggregationFunction aggregationFunction) {
		return null;
	}

	public static Object compare(EngineInstructions basePlan,
			EngineInstructions comparingPlan, Object compareOptions) {
		Object base = executePlan(basePlan);
		Object compare = executePlan(comparingPlan);
		//... TODO some more branching based on options;
		return null;
	}


	public static CalculationResult executePlan(EngineInstructions plan) {
		CalculationResult lastResult = null;
		SparrowContext context = plan.request.getContext();
		for (CalculationStage stage: plan.getStages()) {
			CalculationResult stageResult = new CalculationResult();
			stageResult.prevResult = lastResult;
			stage.setResult(stageResult);
			Step step = stage.step;
			switch(stage.step.operation) {
				case applyWeights:
					Product[] weights = ((ApplyWeightsStep) step).weights;
					stageResult.table = applyWeights(context, step.product, weights );
					break;
				case calculate:
					stageResult.table = calculate(step.product, context).table;
					break;
				case load:
					stageResult.predictData = load(step.product, context).predictData;
					break;
				case aggregate:
					AggregatePreviousResultStep agStep = (AggregatePreviousResultStep) step;
					stageResult.table = (DataTable) aggregate(lastResult, load(ProductTypes.AGGREGATION_DATA.get(), context), agStep.agLevel, agStep.agFunct);
					break;
				case compare:
					CompareStep cs = (CompareStep) step;
					stageResult.table = (DataTable) compare(cs.basePlan, cs.comparingPlan, cs.compareOptions);
					break;
			}
			lastResult = stageResult;
		}
		if (plan.getTransformer() == null) return lastResult;
		return plan.getTransformer().transform(lastResult, plan.getSource());

	}

	// =====================
	// STATIC HELPER METHODS
	// =====================

	/**
	 * TODO remove this to somewhere else
	 * @deprecated
	 * @return
	 */
	private static Connection getConnection() {
		try {
			return SharedApplication.getInstance().getConnection();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new RuntimeException("Unable to obtain connection", e);
		}
	}

	// ================
	// INSTANCE METHODS
	// ================
	public CalculationResult load(Product product) {
		return load(product, context);
	}

	protected static DataTable retrievePredictDataVariant(PredictData data,
			String variant) {
		if (variant == null) return null;
		if ("source".equals(variant)) return data.getSrc();
		if ("topo".equals(variant)) return data.getTopo();
		if ("coef".equals(variant)) return data.getCoef();
		if ("sourceMetadata".equals(variant)) return data.getSrcMetadata();
		if ("decay".equals(variant)) return data.getDelivery();
		return null;
	}
}
