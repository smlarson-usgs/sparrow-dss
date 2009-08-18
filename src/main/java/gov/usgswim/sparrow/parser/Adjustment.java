package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

//TODO:  This class and all similar should implement comparable.
//TODO:  This class should throw an error if parse is called 2nd time.

/**
 * Represents a single adjustment to a source.
 *
 * Note that an Adjustment is not an independent entity and thus does not override 
 * equals or the hashcode.  It does, however, provide a getStateHash method
 * which generates a repeatable hashcode representing the state of the
 * adjustment..  This method is a convenience to parent
 * classes who need to include the state of their adjustments in their hashcodes.
 */
public class Adjustment implements XMLStreamParserComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -3978431841426344605L;
	public static final String MAIN_ELEMENT_NAME = "adjustment";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private Integer src;	//The Model Specific source ID (not any sort of db ID for the source)
	private Double abs;		//A new value for the source, overriding any coef's applied
	private Double coef;	//A coefficient to multiply the source by. 
	
	// ================
	// INSTANCE METHODS
	// ================
	public synchronized Adjustment parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException{
		
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : 
			this.getClass().getSimpleName()
			+ " can only parse " + MAIN_ELEMENT_NAME + " elements.";
		boolean isStarted = false;

		while (in.hasNext()) {
			if (isStarted) {
				// Don't advance past the first element.
				eventCode = in.next();
			} else {
				isStarted = true;
			}

			// Main event loop -- parse until corresponding target end tag encountered.
			switch (eventCode) {
				case START_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						src = ParserHelper.parseAttribAsInt(in, "src");
						abs = ParserHelper.parseAttribAsDouble(in, "abs", false);
						coef = ParserHelper.parseAttribAsDouble(in, "coef", false);
						
						if (abs == null && coef == null) {
							throw new XMLParseValidationException("Either a Absolute (abs) value or a Coefficient (coef) value must be specified for an adjustment.");
						} else if (abs != null && coef != null) {
							throw new XMLParseValidationException("Cannot specify both an Absolute (abs) value and a Coefficient (coef) value for an adjustment.");
						}

					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new XMLStreamException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
	
	@Override
	protected Adjustment clone() throws CloneNotSupportedException {
		Adjustment myClone = new Adjustment();
		myClone.src = src;
		myClone.abs = abs;
		myClone.coef = coef;
		
		return myClone;
	}
	
	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " is not valid");
		}
	}

	public boolean isValid() {
		return true;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public Integer getSource() {
		return src;
	}

	public Double getAbsolute() {
		return abs;
	}

	public Double getCoefficient() {
		return coef;
	}

	public boolean isAbsolute() {
		return abs != null;
	}

	public boolean isCoefficient() {
		return coef != null;
	}
	
	/**
	 * Returns a hashcode that fully represents the state of this adjustment.
	 * 
	 * This hashcode is not intended to be unique (others will have the same) and
	 * is not intended to be used for identity.
	 * @return
	 */
	public int getStateHash() {
		int hash = new HashCodeBuilder(37, 13).
		append(src).
		append(coef).
		append(abs).
		toHashCode();
		return hash;
	}

	
}
