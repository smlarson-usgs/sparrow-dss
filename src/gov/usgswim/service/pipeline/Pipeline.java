package gov.usgswim.service.pipeline;

import gov.usgswim.service.HttpRequestHandler;

import javax.servlet.http.HttpServletResponse;

public interface Pipeline {

	void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception;

	void setHandler(HttpRequestHandler handler);

}
