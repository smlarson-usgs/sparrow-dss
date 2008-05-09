package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.XMLStreamParserComponent;

import java.io.Serializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class AreaOfInterest implements XMLStreamParserComponent, Serializable, Cloneable {

	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "area-of-interest";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	public static AreaOfInterest parseStream(XMLStreamReader in) throws XMLStreamException {
		AreaOfInterest aoi = new AreaOfInterest();
		return aoi.parse(in);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private Integer id;
	
	// ================
	// INSTANCE METHODS
	// ================
	public AreaOfInterest parse(XMLStreamReader in) throws XMLStreamException {
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
						String idString = in.getAttributeValue(DEFAULT_NS_PREFIX, XMLStreamParserComponent.ID_ATTR);
						id = (idString == null)? null: Integer.valueOf(idString);
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
					} else {// otherwise, error
						throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					//break;
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
		int hash = hashBuilder.toHashCode();
		return hash;
	}
	
	@Override
	public AreaOfInterest clone() throws CloneNotSupportedException {
		AreaOfInterest myClone = new AreaOfInterest();
		myClone.id = id;
		return myClone;
	}

	// =================
	// GETTERS & SETTERS
	// =================

	public Integer getId() {
		return id;
	}

}
