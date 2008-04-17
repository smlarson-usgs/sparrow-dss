package gov.usgswim.service;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface XMLStreamParserComponent {
	
	/**
	 * Partially parses the stream (up to the end target tag) and fills in its
	 * attributes.
	 * 
	 * @param in
	 * @return
	 * @throws XMLStreamException 
	 */
	public XMLStreamParserComponent parse(XMLStreamReader in) throws XMLStreamException;
	
	public String getParseTarget();

}
