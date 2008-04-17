package gov.usgswim.service;

import static javax.xml.stream.XMLStreamConstants.*;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class ParserHelper {

	public static String parseSimpleElementValue(XMLStreamReader in) throws XMLStreamException {
		assert(in.getEventType() == START_DOCUMENT): "only start elements accepted";
		int currentEvent = in.next();
		
		String elementValue = null;
		if (currentEvent == CHARACTERS) {
			elementValue = in.getText();
			currentEvent = in.next();
		}
		assert(currentEvent == END_ELEMENT): "should now be the end element";
		
		return elementValue;
	}
}
