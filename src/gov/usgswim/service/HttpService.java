package gov.usgswim.service;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

/**
 * Implementations are capable of processing a generically defined request bean
 * and returning an HttpServletResponse.
 * 
 * This extends the functionality of RequestHandler - primarily allowing the
 * handler to set the MIME and and http headers.
 */
public interface HttpService<T> extends Service<T> {

	//TODO [IK] It would be nice to get the 'firstrow' param out.  Flattening is specific to the XML anyway... 
	public XMLStreamReader getXMLStreamReader(T o, boolean isNeedsCompleteFirstRow) throws Exception;
	
	//public void dispatch(T request, HttpServletResponse response) throws Exception;

}
