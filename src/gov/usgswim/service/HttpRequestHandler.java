package gov.usgswim.service;

import javax.servlet.http.HttpServletResponse;

/**
 * Implementations are capable of processing a generically defined request bean
 * and returning an HttpServletResponse.
 * 
 * This extends the functionality of RequestHandler - primarily allowing the
 * handler to set the MIME and and http headers.
 */
public interface HttpRequestHandler<T> extends RequestHandler<T> {
	public void dispatch(T request, HttpServletResponse response) throws Exception;
}
