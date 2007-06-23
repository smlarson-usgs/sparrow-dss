package gov.usgswim.sparrow.service;

import java.io.IOException;

import java.io.OutputStream;

import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface ServiceHandler {
	public void dispatch(XMLStreamReader in, OutputStream out) throws XMLStreamException, IOException;
	
}
