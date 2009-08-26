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
	private String rootChild;
	private boolean isListElement;
	private String listElementName;
	private String id;

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

	// ===========================
	// CURRENT STATE QUERY METHODS
	// ===========================
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

	public boolean isInList() {
		// if a list element has been set, then it's in a list
		return isListElement;
	}

	public boolean isOnListElementStart() {
		return isOnRootGrandChildStart() && (stream.getAttributeValue("", "id") != null);
	}

	public boolean isOnListElementEnd() {
		return (depth == 1) && (stream.getEventType() == END_ELEMENT);
	}

	// ============================
	// CURRENT STATE CHANGE METHODS
	// ============================

	public void setAsRootChild(String rootChildName) {
		rootChild = rootChildName;
		isListElement = false;
	}

	public void clearRootChild() {
		rootChild = null;
		isListElement = false;
	}

	public void setAsListElement() {
		isListElement = true;
		listElementName = stream.getLocalName();
		id = stream.getAttributeValue("", "id");
	}

	// =============
	// PARSE METHODS
	// =============
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


	public void parseToRootChildEndOrListElementStart() throws XMLStreamException {
		assert(isOnRootChildStart()): "Parsing to the end should only be called when on a start element of a root child";
		content = new StringBuilder();
		boolean stopAtRootGrandChild = true;
		while (stream.hasNext()) {
			next();
			if ( isOnRootGrandChildStart() && stopAtRootGrandChild ) {
				if (isOnListElementStart()) return;
				stopAtRootGrandChild = false;
			}
			if (isOnRootChildEnd()) {
				return;
			}
			writeCurentEvent(stream, content);
		}
	}


	public void parseToListElementEnd() throws XMLStreamException {
		assert(isOnListElementStart()): "Only call this from the beginning of a List Element";
		content = new StringBuilder();
		//writeCurentEvent(stream, content);
		while (stream.hasNext()) {
			next();
			if (isOnListElementEnd()) {
				return;
			}
			writeCurentEvent(stream, content);
		}
	}

	public void parseToNextListElementOrRootChildEnd() throws XMLStreamException {
		assert(isOnListElementEnd() || isOnListElementStart()):
			"Only call this from the end or beginning of a List Element";

		while (stream.hasNext()) {
			next();
			if (isOnListElementStart()) {
				content = new StringBuilder();
				return;
			}
			writeCurentEvent(stream, content);
			if (isOnListElementEnd()) {
				return;
			}
			if (isOnRootChildEnd()) {
				content = new StringBuilder(); // clear the StringBuffer
				return;
			}
		}
	}

	// =============
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
					record.append(">");
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

	// =====================

	public String getRootChildName() {
		return (isListElement)? rootChild: null;
	}

	public String getListElementName() {
		return (isListElement)? listElementName + "." + id: null;
	}

	public StringBuilder getContentAsNode() {
		if (isListElement) {
			StringBuilder result = new StringBuilder();
			result.append("<" + listElementName + " id=\"" + id + "\">");
			result.append(content);
			result.append("</" + listElementName);
			return result;
		}
		String rootChildStartTag = "<" + rootChild + ">";
		content.insert(0, rootChildStartTag);
		content.append("</" + rootChild + ">");
		return content;

	}

	public String getId() {
		return id;
	}



}