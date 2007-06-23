package gov.usgswim.sparrow.service;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface HttpServiceHandler {
	public void dispatch(XMLStreamReader in, HttpServletResponse response) throws XMLStreamException, IOException;
}
