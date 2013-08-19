package gov.usgs.webservices.framework.dataaccess;

import static javax.xml.stream.XMLStreamConstants.ATTRIBUTE;
import static javax.xml.stream.XMLStreamConstants.CDATA;
import static javax.xml.stream.XMLStreamConstants.COMMENT;
import static javax.xml.stream.XMLStreamConstants.END_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.ENTITY_REFERENCE;
import static javax.xml.stream.XMLStreamConstants.NAMESPACE;
import static javax.xml.stream.XMLStreamConstants.SPACE;
import static javax.xml.stream.XMLStreamConstants.START_DOCUMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgs.webservices.framework.utils.UsgsStAXUtils;

public enum XMLStreamReaderMethod {
	// TODO need to complete this with all XMLStreamReader methods
	 getNamespacePrefix(             new Integer[] {START_ELEMENT, END_ELEMENT, NAMESPACE}   ),
	 getNamespaceURI(                new Integer[] {START_ELEMENT, END_ELEMENT, NAMESPACE}   ),
	 getNamespaceCount(              new Integer[] {START_ELEMENT, END_ELEMENT, NAMESPACE}   ),
	 getPrefix(                              new Integer[] {START_ELEMENT, END_DOCUMENT}                             ),
	 getLocalName(                   new Integer[] {START_ELEMENT, END_ELEMENT}                              ),
	 getAttributeCount(              new Integer[] {START_ELEMENT, ATTRIBUTE}                                ),
	 getAttributeLocalName(  new Integer[] {START_ELEMENT, ATTRIBUTE}                                ),
	 getAttributeName(               new Integer[] {START_ELEMENT, ATTRIBUTE}                                ),
	 getAttributeNamespace(  new Integer[] {START_ELEMENT, ATTRIBUTE}                                ),
	 getElementText(                 new Integer[] {START_ELEMENT}                                                   ),
	 getAttributePrefix(             new Integer[] {START_ELEMENT, ATTRIBUTE}                                ),
	 getAttributeType(               new Integer[] {START_ELEMENT, ATTRIBUTE}                                ),
	 getAttributeValue(              new Integer[] {START_ELEMENT, ATTRIBUTE}                                ),
	 getText(                                new Integer[] {CDATA, COMMENT, ENTITY_REFERENCE, SPACE} ),
	 getTextCharacters(              new Integer[] {CDATA, COMMENT, ENTITY_REFERENCE, SPACE} ),
	 getTextLength(                  new Integer[] {CDATA, COMMENT, ENTITY_REFERENCE, SPACE} ),
	 getTextStart(                   new Integer[] {CDATA, COMMENT, ENTITY_REFERENCE, SPACE} ),
	 isAttributeSpecified(   new Integer[] {START_DOCUMENT, ATTRIBUTE}                               ),
	 require(                                new Integer[] {START_ELEMENT}                                                   ),
	 ;

	
	private int[] types;
	
	// ===========
	// CONSTRUCTOR
	// ===========
	 XMLStreamReaderMethod(Integer[] allowedEventTypes){
		types = new int[allowedEventTypes.length];
		for (int i = 0; i<allowedEventTypes.length; i++) {
			types[i] = allowedEventTypes[i];
		}
	}
	
	public boolean isAllowed(int currentEventType) {
		// TODO use a more efficient search algorithm
		for (int allowedType: types) {
			if (currentEventType == allowedType) {
				return true;
			}
		}
		return false;
	}
	
	public void check(int currentEventType) {
		// TODO use a more efficient search algorithm
		for (int allowedType: types) {
			if (currentEventType == allowedType) {
				return;
			}
		}
		throw new IllegalStateException(makeErrorMessage(UsgsStAXUtils.eventNames.get(currentEventType)));
	}
	
	public String makeErrorMessage(String stateName) {
		StringBuffer sb = new StringBuffer(this.name());
		sb.append("() may not be called when current state is ")
			.append(stateName);
		return sb.toString();
	}

}
