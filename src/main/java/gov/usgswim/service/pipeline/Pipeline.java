package gov.usgswim.service.pipeline;

import java.io.OutputStream;

import gov.usgs.webservices.framework.formatter.IFormatter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Central organizer class for handling all relevant parts associated with a service
 * 
 * @author ilinkuo
 *
 */
public interface Pipeline {
	
	// Highly suggested: subclasses should provide a no arg constructor.

	void dispatch(PipelineRequest o, HttpServletResponse response) throws Exception;
	
	void dispatch(PipelineRequest o, OutputStream response) throws Exception;

	PipelineRequest parse(HttpServletRequest request) throws Exception;
	
	PipelineRequest parse(String xmlRequest) throws Exception;

	void setXMLParamName(String xmlParamName);

	IFormatter getConfiguredJSONFormatter();

}
