package gov.usgswim.sparrow.revised;

import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.service.SharedApplication;

public class CacheAvoider {

	public static PredictResult avoidResultCache(PredictionContext2 context) {
		// Temporary hack to get this to compile
		//PredictResult adjResult = SharedApplication.getInstance().getPredictResult(context.getTargetContextOnly());
		return null;
	}

	public static PredictResult avoidAnalysisCache(PredictionContext2 context) {
		return null;
		//PredictResult result = SharedApplication.getInstance().getAnalysisResult(context);
	}
}
