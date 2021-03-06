package gov.usgswim.service;


/**
 * Implementations are capable of processing a generically defined request bean
 * and returning an output stream.
 * 
 * This interface is primarily used for testing.  See HttpService for
 * an interface to use for http requests.
 */
public interface Service<T> {
//	public void dispatch(T request, OutputStream out) throws Exception;
	public void shutDown();
}
