package gov.usgswim.sparrow.parser;

import java.io.Serializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public interface XMLStreamParserComponent extends Serializable, Cloneable {
	public static final String ID_ATTR = "context-id";

	/**
	 * Partially parses the stream (up to but not after the end target tag) and
	 * fills in its attributes.
	 * 
	 * Note: All implementations of this method should really be synchronized,
	 * since it is really a replacement for a constructor.
	 * 
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 * 
	 * TODO There should be a custom XMLParseException to be thrown here
	 */
	public XMLStreamParserComponent parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException;

	public String getParseTarget();

	/**
	 * Returns true if the passed local name is a match to the parse target.
	 * 
	 * @return
	 */
	public boolean isParseTarget(String name);

	/**
	 * @return true if the component satisfies all the business rules
	 * 
	 * Note: isValid() = true <==> checkValidity() does not throw an exception.
	 */
	public boolean isValid();

	/**
	 * checkValidity() is called at the end of the parse method to check that
	 * the element has been populated correctly, that all required elements
	 * exist and enumerated values and value ranges are satisfied, etc. It
	 * performs the same function as isValid(), but returns a more detailed
	 * error message via the thrown XMLParseValidationException.
	 * 
	 * Note: isValid() = true <==> checkValidity() does not throw an exception.
	 */
	public void checkValidity() throws XMLParseValidationException;

}
