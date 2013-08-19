package gov.usgswim.service.pipeline;

import gov.usgs.webservices.framework.formatter.IFormatter;

import java.io.OutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Central organizer class for handling all relevant parts associated with a service
 *
 * @author ilinkuo
 *
 */
public interface Pipeline<T extends PipelineRequest> {

	// Highly suggested: subclasses should provide a no arg constructor.

	void dispatch(T o, HttpServletResponse response) throws Exception;

	void dispatch(T o, OutputStream response) throws Exception;

	T parse(HttpServletRequest request) throws Exception;

	T parse(String xmlRequest) throws Exception;

	void setXMLParamName(String xmlParamName);

	IFormatter getConfiguredJSONFormatter();

}
