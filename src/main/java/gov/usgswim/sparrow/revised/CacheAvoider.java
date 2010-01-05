package gov.usgswim.sparrow.revised;

import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.revised.request.SparrowRequest;

public class CacheAvoider {

	public static PredictResult avoidResultCache(SparrowRequest request) {
		// Temporary hack to get this to compile
		//PredictResult adjResult = SharedApplication.getInstance().getPredictResult(context.getTargetContextOnly());
		return null;
	}

	public static PredictResult avoidAnalysisCache(SparrowRequest request) {
		return null;
		//PredictResult result = SharedApplication.getInstance().getAnalysisResult(context);
	}
}
