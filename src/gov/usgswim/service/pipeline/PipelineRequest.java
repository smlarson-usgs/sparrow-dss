package gov.usgswim.service.pipeline;

import gov.usgswim.service.ResponseFormat;

public interface PipelineRequest {
	
	public String getXMLRequest();
	public void setXMLRequest(String request);

	public void setEcho(String echo);
	public void setEcho(boolean echo);
	public boolean isEcho();
	
	public ResponseFormat getResponseFormat();
	public void setResponseFormat(ResponseFormat respFormat);
}
