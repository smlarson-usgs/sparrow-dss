package gov.usgswim.sparrow.service.hucs;

import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ResponseFormat;

public class HucsForReachRequest implements PipelineRequest{
	
	public Long reachID;
	private ResponseFormat respFormat;
	public Integer modelID;
	public String attributeName;

	public HucsForReachRequest(Integer modelID, Long reachID, String attName) {
		this.modelID = modelID;
		this.reachID = reachID;
		this.attributeName = attName;
		
		// Initialize response format
		respFormat = new ResponseFormat();
		respFormat.setAttachment(false);
		respFormat.setMimeType("json");

	}

	public ResponseFormat getResponseFormat() {

		return respFormat;
	}

	public String getXMLRequest() {
		return null; // REST-only, no XML
	}

	public void setResponseFormat(ResponseFormat respFormat) {
		this.respFormat = respFormat;
	}

	public void setXMLRequest(String request) {
		// ignore, as this is REST-only, no XML
	}

}
