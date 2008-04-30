package gov.usgswim.service;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface XMLStreamParserComponent {
	public static final String ID_ATTR = "id";
	/**
	 * Partially parses the stream (up to but not after the end target tag) and
	 * fills in its attributes.
	 * 
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 */
	public XMLStreamParserComponent parse(XMLStreamReader in) throws XMLStreamException;
	
	public String getParseTarget();

}
