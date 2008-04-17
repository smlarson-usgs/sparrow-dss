package gov.usgswim.service.pipeline;

import gov.usgswim.service.HttpRequestHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface Pipeline {

	void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception;

	/**
	 * @param handler
	 * @deprecated -- set handler on construction
	 */
	void setHandler(HttpRequestHandler handler);
	
	PipelineRequest parse(HttpServletRequest request) throws Exception;

	void setXMLParamName(String xmlParamName);

}
