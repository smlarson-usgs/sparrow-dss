package gov.usgswim.sparrow.service.predict;

import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;

public class IndividualReachPredictionRequest {
	public final Integer contextId;
	public final Long model;
	public final Long reachId;


	public IndividualReachPredictionRequest(String contextId, String model,
			String reachId) {
		this.contextId = (contextId != null && contextId.length() > 0)? Integer.parseInt(contextId): null;
		this.model = (model != null && model.length() > 0)? Long.parseLong(model): null;
		this.reachId = (reachId != null && reachId.length() > 0)? Long.parseLong(reachId): null;	}


	public PredictionContext retrievePredictionContext() {
		PredictionContext pc = null;
		if (contextId != null) {
			pc = SharedApplication.getInstance().getPredictionContext(contextId);
		} else {
			AdjustmentGroups adjGroup = new AdjustmentGroups(model);
			pc = new PredictionContext(model, adjGroup, null, null, null, null);
		}
		return pc;
	}

}
