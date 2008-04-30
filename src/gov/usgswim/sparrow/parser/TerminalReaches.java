package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

import gov.usgswim.service.ParserHelper;
import gov.usgswim.service.XMLStreamParserComponent;

public class TerminalReaches implements XMLStreamParserComponent, Serializable {

	private static final long serialVersionUID = 8804027069848411715L;
	private static final String REACHES_CHILD = "reach";
	public static final String MAIN_ELEMENT_NAME = "terminal-reaches";


	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	protected List<Integer> reachIDs = new ArrayList<Integer>();
	private String id;
	
	// ================
	// INSTANCE METHODS
	// ================
	public TerminalReaches parse(XMLStreamReader in) throws XMLStreamException {
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
						id = in.getAttributeValue(DEFAULT_NS_PREFIX, XMLStreamParserComponent.ID_ATTR);
					} else if (REACHES_CHILD.equals(localName)) {
						String reachID = ParserHelper.parseSimpleElementValue(in);
						reachIDs.add(Integer.parseInt(reachID));
					} else if ("logical-set".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						return this; // we're done
					} else if (REACHES_CHILD.equals(localName)) {
						
					} else {// otherwise, error
						throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	@Override
	public int hashCode() {
		HashCodeBuilder hashBuilder = new HashCodeBuilder(137, 1729).append(id);
		for (Integer idValue: reachIDs) {
			hashBuilder.append(idValue);
		}
		int hash = hashBuilder.toHashCode();
		return hash;
	}
	// =================
	// GETTERS & SETTERS
	// =================
	public List<Integer> getReachIDs(){
		return reachIDs;
	}

	public String getId() {
		return id;
	}
	
}
