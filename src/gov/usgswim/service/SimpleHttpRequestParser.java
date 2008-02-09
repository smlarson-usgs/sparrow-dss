package gov.usgswim.service;

import gov.usgswim.ThreadSafe;
import gov.usgswim.service.pipeline.PipelineRequest;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

/**
 * A basic implementation of HttpRequestParser that attempts to return an
 * XMLStreamReader as the request object.
 */
@ThreadSafe
//public class SimpleHttpRequestParser extends AbstractHttpRequestParser<PipelineRequest> {
//IK: is this class used? Cannot find refernces to it.
public class SimpleHttpRequestParser  {
	public SimpleHttpRequestParser() {
	}

	public XMLStreamReader parse(HttpServletRequest request) throws Exception {
//		return (XMLStreamReader) getXMLStream(request);
		return null;
	}

	public XMLStreamReader parse(XMLStreamReader in) {
		return in;
	}

	public XMLStreamReader parse(String in) throws Exception {
//		return getXMLStream(in);
		return null;
	}

//	public PipelineRequest parseForPipeline(HttpServletRequest request) throws Exception {
//		
//		throw new UnsupportedOperationException("may have to refactor interfaces");
//	}
}
