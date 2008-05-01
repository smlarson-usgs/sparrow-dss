package gov.usgswim.service;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.parser.Analysis;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class AbstractXMLStreamParserComponent implements
		XMLStreamParserComponent {
	protected final String mainElementName;

	protected AbstractXMLStreamParserComponent(String mainElement) {
		this.mainElementName = mainElement;
	}
	
	protected abstract boolean isMatch(String name);
	
	// ================
	// INSTANCE METHODS
	// ================
	public Analysis parse(XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isMatch(localName) && eventCode == START_ELEMENT) : 
			this.getClass().getSimpleName()
			+ " can only parse " + mainElementName + " elements.";
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
					if (mainElementName.equals(localName)) {
					} else if ("select".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else if ("limit-to".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else if ("group-by".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + mainElementName);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (mainElementName.equals(localName)) {
						return this; // we're done
					}
					// otherwise, error
					throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + mainElementName);
					//break;
			}
		}
		throw new RuntimeException("tag <" + mainElementName + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return mainElementName;
	}
}
