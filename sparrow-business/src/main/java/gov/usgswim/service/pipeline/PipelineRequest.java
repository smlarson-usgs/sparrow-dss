package gov.usgswim.service.pipeline;

import gov.usgswim.sparrow.parser.ResponseFormat;

public interface PipelineRequest {
	
	/**
	 * Return the request xml, whether in the body or in a post parameter.
	 * @return
	 */
	public String getXMLRequest();
	public void setXMLRequest(String request);
	
	public ResponseFormat getResponseFormat();
	public void setResponseFormat(ResponseFormat respFormat);
}
