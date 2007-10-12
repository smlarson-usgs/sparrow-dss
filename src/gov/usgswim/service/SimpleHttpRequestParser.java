package gov.usgswim.service;

import gov.usgswim.ThreadSafe;

import javax.servlet.http.HttpServletRequest;

import javax.xml.stream.XMLStreamReader;

/**
 * A basic implementation of HttpRequestParser that attempts to return an
 * XMLStreamReader as the request object.
 */
@ThreadSafe
public class SimpleHttpRequestParser extends AbstractHttpRequestParser<XMLStreamReader> {
	public SimpleHttpRequestParser() {
	}

	public XMLStreamReader parse(HttpServletRequest request) throws Exception {
		return getXMLStream(request);
	}
	
}
