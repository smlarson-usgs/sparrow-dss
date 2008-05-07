package gov.usgswim.service;

import gov.usgswim.service.pipeline.PipelineRequest;

import javax.servlet.http.HttpServletRequest;

/**
 * Accepts and incomming HTTPRequest and converts it to an application specific
 * request object.  Implementors of this interface must provide a no-argument
 * constructor and must be threadsafe.
 */
public interface HttpRequestParser<T extends PipelineRequest> extends RequestParser<T> {
	/**
	 * Parses an HttpServletRequest into an application specific request object.
	 * 
	 * Must be threadsafe.
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public T parse(HttpServletRequest request) throws Exception;
	
	/**
	 * Sets the name of the parameter containing the XML request.
	 * 
	 * Some implementations may choose to ignore this.  Must be threadsafe.
	 * @param paramName
	 */
	public void setXmlParam(String paramName);
	
	/**
	 * Returns the name of the parameter which contains the XML request data.
	 * 
	 * Some implementations may choose to ignore this.  Must be threadsafe.
	 * @return
	 */
	public String getXmlParam();

	
}
