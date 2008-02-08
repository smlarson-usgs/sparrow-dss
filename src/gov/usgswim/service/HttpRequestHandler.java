package gov.usgswim.service;

import java.io.OutputStream;

import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.Data2D;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.service.PredictSerializer2;
import gov.usgswim.sparrow.service.PredictServiceRequest;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLStreamReader;

/**
 * Implementations are capable of processing a generically defined request bean
 * and returning an HttpServletResponse.
 * 
 * This extends the functionality of RequestHandler - primarily allowing the
 * handler to set the MIME and and http headers.
 */
public interface HttpRequestHandler<T> extends RequestHandler<T> {
	public void dispatch(T request, HttpServletResponse response) throws Exception;

	public XMLStreamReader getXMLStreamReader(T o, boolean isNeedsCompleteFirstRow) throws Exception;
	
	// TODO remove the need for this by better use of generics, stumbling block is simplehttprequest class
	public void dispatch(PipelineRequest request, HttpServletResponse response) throws Exception;
	
	

}
