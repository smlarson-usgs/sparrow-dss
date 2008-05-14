package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

//TODO:  Not sure what this class is for....
public class Content implements XMLStreamParserComponent {
	public static final String MAIN_ELEMENT_NAME = "content";
	public static final String ADJUSTMENTS_CHILD = "adjustments";
	public static final String ATTRIBUTES_CHILD = "attributes";
	public static final String PREDICTED_CHILD = "predicted";
	public static final String ALL_CHILD = "all";
	
	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	public static Content parseStream(XMLStreamReader in) throws XMLStreamException {
		Content content = new Content();
		return content.parse(in);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private boolean hasAdjustments;
	private boolean hasAttributes;
	private boolean hasPredicted;
	
	// ================
	// INSTANCE METHODS
	// ================
	public Content parse(XMLStreamReader in) throws XMLStreamException {
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
						// do nothing
					} else if (ADJUSTMENTS_CHILD.equals(localName)) {
						hasAdjustments = true;
						ParserHelper.ignoreElement(in);
					} else if (ATTRIBUTES_CHILD.equals(localName)) {
						hasAttributes = true;
						ParserHelper.ignoreElement(in);
					} else if (PREDICTED_CHILD.equals(localName)) {
						hasPredicted = true;
						ParserHelper.ignoreElement(in);
					} else if (ALL_CHILD.equals(localName)) {
						hasAdjustments = true;
						hasAttributes = true;
						hasPredicted = true;
						ParserHelper.ignoreElement(in);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						return this; // we're done
					}
					// otherwise, error
					throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}


	// =================
	// GETTERS & SETTERS
	// =================
	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean hasAdjustments() {
		return hasAdjustments;
	}

	public boolean hasAttributes() {
		return hasAttributes;
	}

	public boolean hasPredicted() {
		return hasPredicted;
	}

}
