package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.io.Serializable;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class AreaOfInterest implements XMLStreamParserComponent {

	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "area-of-interest";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	public static AreaOfInterest parseStream(XMLStreamReader in, Long modelID) throws XMLStreamException, XMLParseValidationException {
		AreaOfInterest aoi = new AreaOfInterest(modelID);
		return aoi.parse(in);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private Long modelID;
	private Integer id;
	
	/**
	 * Constructor requires a modelID
	 */
	public AreaOfInterest(Long modelID) {
		this.modelID = modelID;
	}
	
	// ================
	// INSTANCE METHODS
	// ================
	public AreaOfInterest parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
						id = ParserHelper.parseAttribAsInt(in, XMLStreamParserComponent.ID_ATTR, false);
					} else if ("logical-set".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						checkValidity();
						return this; // we're done
					} else {// otherwise, error
						throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					//break;
			}
		}
		throw new XMLParseValidationException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
	
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
  public boolean equals(Object obj) {
	  if (obj instanceof AreaOfInterest) {
	  	return obj.hashCode() == hashCode();
	  } else {
	  	return false;
	  }
  }
  
	public synchronized int hashCode() {
		if (id == null) {
			HashCodeBuilder hash = new HashCodeBuilder(137, 1729);
			
			hash.append(modelID);
	
			id = hash.toHashCode();
		}
		return id;
	}
	
	@Override
	public AreaOfInterest clone() throws CloneNotSupportedException {
		AreaOfInterest myClone = new AreaOfInterest(modelID);
		return myClone;
	}

	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " is not valid");
		}
	}

	public boolean isValid() {
		return true;
	}
	
	// =================
	// GETTERS & SETTERS
	// =================

	public Integer getId() {
		return hashCode();
	}
	
	public Long getModelID() {
		return modelID;
	}

}
