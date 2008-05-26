package gov.usgswim.sparrow.parser;


import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

//TODO: This interface should extend serializable
public interface XMLStreamParserComponent {
	public static final String ID_ATTR = "context-id";
	/**
	 * Partially parses the stream (up to but not after the end target tag) and
	 * fills in its attributes.
	 * 
	 * Note:  All implementations of this method should really be synchronized,
	 * since it is really a replacement for a constructor.
	 * 
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 */
	public XMLStreamParserComponent parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException;
	
	public String getParseTarget();
	
	/**
	 * Returns true if the passed local name is a match to the parse target.
	 * @return
	 */
	public boolean isParseTarget(String name);

}
