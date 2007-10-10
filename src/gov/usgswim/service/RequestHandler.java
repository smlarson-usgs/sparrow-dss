package gov.usgswim.service;

import java.io.OutputStream;

/**
 * Implementations are capable of processing a generically defined request bean
 * and returning an output stream.
 * 
 * This interface is primarily used for testing.  See HttpRequestHandler for
 * an interface to use for http requests.
 */
public interface RequestHandler<T> {
	public void dispatch(T request, OutputStream out) throws Exception;
}
