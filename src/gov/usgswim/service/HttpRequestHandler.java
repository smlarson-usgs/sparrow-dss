package gov.usgswim.service;

import javax.xml.stream.XMLStreamReader;

/**
 * Implementations are capable of processing a generically defined request bean
 * and returning an HttpServletResponse.
 * 
 * This extends the functionality of RequestHandler - primarily allowing the
 * handler to set the MIME and and http headers.
 */
public interface HttpRequestHandler<T> extends RequestHandler<T> {


	public XMLStreamReader getXMLStreamReader(T o, boolean isNeedsCompleteFirstRow) throws Exception;
	
	
//	public void dispatch(T request, HttpServletResponse response) throws Exception;
	// TODO remove the need for this by better use of generics, stumbling block is simplehttprequest class
//	public void dispatch(PipelineRequest request, HttpServletResponse response) throws Exception;
	

}
