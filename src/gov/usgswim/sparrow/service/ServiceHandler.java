package gov.usgswim.sparrow.service;

import java.io.IOException;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * Low-level interface that defines a service capable of taking an incoming
 * xml stream and returning a response outputsteam.
 * 
 * This interface will not really be used - most services will use the
 * HttpServiceHandler.
 */
public interface ServiceHandler {
	public void dispatch(XMLStreamReader in, OutputStream out) throws XMLStreamException, IOException;
	
}
