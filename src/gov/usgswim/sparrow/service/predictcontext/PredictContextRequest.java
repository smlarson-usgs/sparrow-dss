package gov.usgswim.sparrow.service.predictcontext;

import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ResponseFormat;

public class PredictContextRequest implements PipelineRequest {

	private String xmlRequest;
	private ResponseFormat responseFormat;
	private PredictionContext predictionContext;
	
	/**
	 * @param pc
	 */
	public PredictContextRequest(PredictionContext pc) {
		this.predictionContext = pc;
		this.responseFormat = new ResponseFormat();
		responseFormat.setMimeType("xml");
		responseFormat.setAttachment(false);
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public String getXMLRequest() {
		return xmlRequest;
	}

	public void setXMLRequest(String request) {
		xmlRequest = request;		
	}

	public void setResponseFormat(ResponseFormat respFormat) {
		this.responseFormat = respFormat;
		responseFormat.fileName = "model";
	}
	
	public ResponseFormat getResponseFormat() {
		if (responseFormat == null) {
			setResponseFormat(new ResponseFormat());
		}
		return responseFormat;
	}

	public PredictionContext getPredictionContext() {
		return predictionContext;
	}

	public void setPredictionContext(PredictionContext predictionContext) {
		this.predictionContext = predictionContext;
	}


}
