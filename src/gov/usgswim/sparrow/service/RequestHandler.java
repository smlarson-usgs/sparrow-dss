package gov.usgswim.sparrow.service;

import java.io.OutputStream;

/**
 * Implementations are capable of processing a generically defined request bean
 * and returning an output stream.
 */
public interface RequestHandler<T> {
	public void dispatch(T request, OutputStream out) throws Exception;
}
