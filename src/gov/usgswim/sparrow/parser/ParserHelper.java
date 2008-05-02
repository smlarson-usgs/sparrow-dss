package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.*;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public abstract class ParserHelper {

	public static String parseSimpleElementValue(XMLStreamReader in) throws XMLStreamException {
		assert(in.getEventType() == START_ELEMENT): "only start elements accepted";
		int currentEvent = in.next();
		
		String elementValue = null;
		if (currentEvent == CHARACTERS) {
			elementValue = in.getText();
			currentEvent = in.next();
		}
		assert(currentEvent == END_ELEMENT): "should now be the end element";
		
		return elementValue;
	}
	
	/**
	 * Ignores the current tag element by consuming the stream until the corresponding end tag. 
	 * This is not namespace nor context-sensitive. TODO make it so
	 * 
	 * @param in
	 * @throws XMLStreamException 
	 */
	public static void ignoreElement(XMLStreamReader in) throws XMLStreamException {
		assert(in.getEventType() == START_ELEMENT): "only start elements accepted";
		String ignoredElement = in.getLocalName();
		int currentEvent = in.getEventType();
		String currentElement = in.getLocalName();
		while (!currentElement.equals(ignoredElement) || currentEvent != END_ELEMENT) {
			currentEvent = in.next();
			currentElement = (START_ELEMENT == currentEvent || END_ELEMENT == currentEvent) ? in.getLocalName(): "invalid tag";
		}
		assert(currentElement.equals(ignoredElement) && currentEvent == END_ELEMENT);
	}
	
	/**
	 * Parses to the end tag. Useful for cleaning up when doing stream parsing.
	 * 
	 * @param in
	 * @throws XMLStreamException 
	 */
	public static void parseToEndTag(XMLStreamReader in, String... endTags) throws XMLStreamException {
		assert(endTags != null): "at least one endTag must be specified";
		int currentEvent = in.getEventType();
		String currentElement = (currentEvent == END_ELEMENT )? in.getLocalName(): "";
		while (!isInArray(endTags, currentElement) || currentEvent != END_ELEMENT) {
			currentEvent = in.next();
			currentElement = (currentEvent == END_ELEMENT )? in.getLocalName(): "";
		}
		assert(isInArray(endTags, currentElement) && currentEvent == END_ELEMENT);
		
	}
	
	/**
	 * Parses to the start tag. Useful for fast forwarding to important part of the stream.
	 * 
	 * @param in
	 * @throws XMLStreamException 
	 */
	public static void parseToStartTag(XMLStreamReader in, String startTag) throws XMLStreamException {
		assert(startTag != null && startTag.length() > 0): "a startTag must be specified";
		int currentEvent = in.getEventType();
		String currentElement = (currentEvent == START_ELEMENT)? in.getLocalName(): "";
		while ( currentEvent != XMLStreamConstants.START_ELEMENT || !currentElement.equals(startTag) ) {
			currentEvent = in.next();
			currentElement = (currentEvent == START_ELEMENT)? in.getLocalName(): "";
		}
		assert(currentElement.equals(startTag) && currentEvent == START_ELEMENT);
		
	}
	
	/**
	 * Determines whether a given string is in the String array.
	 * 
	 * @param stringList
	 * @param item
	 * @return
	 */
	public static boolean isInArray(String[] stringList, String item) {
		for (String listElement: stringList) {
			if (listElement.equals(item)) return true;
		}
		return false;
	}
}
