package gov.usgswim.sparrow.parser;

/**
 * An exception thrown when parsing results in an invalid object (a logic error),
 * or when the XML is missing or contains unrecognizable elements.
 * 
 * @author eeverman
 */
public class XMLParseValidationException extends Exception {

	private static final long serialVersionUID = 8675863905261919974L;

	public XMLParseValidationException() {
		//default call to superclass
	}

	public XMLParseValidationException(String message) {
		super(message);
	}

	public XMLParseValidationException(Throwable cause) {
		super(cause);
	}

	public XMLParseValidationException(String message, Throwable cause) {
		super(message, cause);
	}

}
