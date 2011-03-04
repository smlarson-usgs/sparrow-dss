package gov.usgswim.sparrow.domain;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.Immutable;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;
/**
 * A single criteria suc as HUC2 = '01'.
 * 
 * Criteria instances live inside a LogicalSet which contains a model ID
 * reference, thus, Criteria does not have an ID ref.
 * 
 * @author eeverman
 * 
 */
@Immutable
public class Criteria implements XMLStreamParserComponent {

	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "criteria";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private Long modelID;
	private CriteriaType criteriaType;
	private String value;
	
	
	// ===========
	// CONSTRUCTOR
	// ===========
	public Criteria(Long modelID) {
		this.modelID = modelID;
	}
	
	public Criteria(Long modelID, CriteriaType criteriaType, String value) {
		this.modelID = modelID;
		this.criteriaType = criteriaType;
		this.value = value;
	}
	// ================
	// INSTANCE METHODS
	// ================
	public Criteria parse(XMLStreamReader in)
		throws XMLStreamException, XMLParseValidationException {
		
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isParseTarget(localName) && eventCode == START_ELEMENT) : 
			this.getClass().getSimpleName()
			+ " can only parse " + getParseTarget() + " elements.";
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
					if (isParseTarget(localName)) {

						String attrib = in.getAttributeValue("","attrib");
						String elemValue = ParserHelper.parseSimpleElementValue(in);
						
						CriteriaType type = CriteriaType.UNKNOWN.fromStringIgnoreCase(attrib);
						
						criteriaType = type;
						value = elemValue;
						
						checkValidity();
						return this; // we're done
						
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
			}
		}
		throw new XMLParseValidationException("tag <" + getParseTarget() + "> not closed. Unexpected end of stream?");
	}



	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}


	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			if (!hasCriteria()) {
				throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " must have non-empty criteria");
			}
		}
	}

	public boolean isValid() {
		return hasCriteria();
	}
	
	private boolean hasCriteria() {
		return (criteriaType != null && ! criteriaType.equals(CriteriaType.UNKNOWN) && value != null);
	}
	
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof Criteria) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(12497, 134343);
		hash.append(modelID).append(criteriaType).append(value);
		
		return hash.toHashCode();
	}

	
	// =================
	// GETTERS & SETTERS
	// =================
	public Long getModelID() {
		return modelID;
	}
	
	public CriteriaType getCriteriaType() {
		return criteriaType;
	}
	
	public String getValue() {
		return value;
	}




}


