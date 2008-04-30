package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.service.ParserHelper;
import gov.usgswim.service.XMLStreamParserComponent;

public class Analysis implements XMLStreamParserComponent {
	private static final String GROUP_BY_CHILD = "group-by";
	private static final String LIMIT_TO_CHILD = "limit-to";
	public static final String MAIN_ELEMENT_NAME = "analysis";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private String groupBy;
	private String limitTo;


	
	// ================
	// INSTANCE METHODS
	// ================
	public Analysis parse(XMLStreamReader in) throws XMLStreamException {
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
					} else if ("select".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else if (LIMIT_TO_CHILD.equals(localName)) {
						limitTo = ParserHelper.parseSimpleElementValue(in);
					} else if (GROUP_BY_CHILD.equals(localName)) {
						groupBy = ParserHelper.parseSimpleElementValue(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
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

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	// =================
	// GETTERS & SETTERS
	// =================
	public String getLimitTo(){
		return limitTo;
	}
	
	public String getGroupBy(){
		return groupBy;
	}
}
