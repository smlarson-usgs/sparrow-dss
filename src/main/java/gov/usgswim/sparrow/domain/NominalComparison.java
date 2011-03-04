package gov.usgswim.sparrow.domain;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class NominalComparison extends Comparison {

	private static final long serialVersionUID = 1L;
	
	public static final String MAIN_ELEMENT_NAME = "nominalComparison";
	
	public static final NominalComparison NO_COMPARISON = new NominalComparison();

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	public static NominalComparison getNoComparisonInstance() {
		return NO_COMPARISON;
	}

	// ===============
	// INSTANCE FIELDS
	// ===============

	public NominalComparison() {};

	public NominalComparison parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
						String comparisonString =
							ParserHelper.parseAttribAsString(in, "type", false);
						comparisonType = ComparisonType.valueOf(comparisonString);
					} else {
						throw new XMLParseValidationException(
								"unrecognized child element of <"
								+ localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException(
							"unexpected closing tag of </"
							+ localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new XMLParseValidationException("tag <"
				+ MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}


	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NominalComparison) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}


	//Handled by base Comparison class.  Override if fields are added.
	//public synchronized int hashCode()

	@Override
	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	@Override
	public boolean isParseTarget(String name) {
		return isTargetMatch(name);
	}


}
