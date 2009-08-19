/**
 *
 */
package gov.usgs.webservices.framework.utils;

import static javax.xml.stream.XMLStreamConstants.*;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ParseState{
	public int depth;
	public XMLStreamReader stream;
	public StringBuilder content;

	public ParseState(XMLStreamReader in){this.stream = in;}

	public int next() throws XMLStreamException {
		int result = stream.next();
		switch(result) {
			case START_ELEMENT:
				depth++;
				break;
			case END_ELEMENT:
				depth--;
				break;
		}
		return result;
	}

	public boolean isOnRootChildStart() {
		return (depth == 1) && (stream.getEventType() == START_ELEMENT);
	}
	public boolean isOnRootGrandChildStart() {
		return (depth == 2) && (stream.getEventType() == START_ELEMENT);
	}

	public boolean isOnRoot() {
		return (depth == 0) && (stream.getEventType()) == START_ELEMENT;
	}

	public boolean isOnRootChildEnd() {
		return (depth == 0) && (stream.getEventType() == END_ELEMENT);
	}

	public void parseToNextRootChildStart() throws XMLStreamException {
		while (stream.hasNext()) {
			next();
			if (isOnRootChildStart()) {
				return;
			}
		}
	}

	public void parseToRootChildEnd() throws XMLStreamException {
		assert(isOnRootChildStart() || isOnRootGrandChildStart()): "These are the only allowed states to call this method";
		if (isOnRootChildStart()) {
			// Refresh the content
			content = new StringBuilder();
		} else { // isOnRootGrandChildStart() == true
			// Just continue recording
			writeCurentEvent(stream, content);
		}

		while (stream.hasNext()) {
			next();
			if (isOnRootChildEnd()) {
				return;
			}
			writeCurentEvent(stream, content);
		}
	}

	public static void writeCurentEvent(XMLStreamReader in, StringBuilder record) {
		int current = in.getEventType();
		// TODO adjust this so that the result is valid xml. Escape the appropriate 5 entities
		switch(current) {
			case START_ELEMENT:
				if (in.getAttributeCount() == 0) {
					record.append("<" + in.getLocalName() + ">");
				} else {
					record.append("<" + in.getLocalName());
					for (int i=0; i<in.getAttributeCount(); i++) {
						record.append(" " + in.getAttributeName(i) + "=\"" + in.getAttributeValue(i)+ "\"" );
					}
				}
				break;
			case END_ELEMENT:
				record.append("</" + in.getLocalName() + ">");
				break;
			case CHARACTERS:
				String text = in.getText();
				text = (text == null)? "" : text.trim();
				record.append(text);
				break;
		}

	}

	public void parseToRootChildEndOrGrandChildStart() throws XMLStreamException {
		assert(isOnRootChildStart()): "Parsing to the end should only be called when on a start element of a root child";
		content = new StringBuilder();
		while (stream.hasNext()) {
			next();
			if (isOnRootChildEnd() || isOnRootGrandChildStart()) {
				return;
			}
			writeCurentEvent(stream, content);
		}
	}

	public boolean isGrandChildAListElement() {
		return isOnRootGrandChildStart() && (stream.getAttributeValue("", "id") != null);
	}

	public void parseToRootGrandChildEnd() {
		// TODO Auto-generated method stub

	}

	public void parseToNextGrandChildListElementOrEnd() {
		// TODO Auto-generated method stub

	}

}