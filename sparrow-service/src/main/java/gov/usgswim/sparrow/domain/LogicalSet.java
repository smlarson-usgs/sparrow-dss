package gov.usgswim.sparrow.domain;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.Immutable;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;
/**
 * Simple bean class to hold logicalSet identification serving as a key to
 * cached logicalSet set of reaches.
 * 
 * @author eeverman
 * 
 */
@Immutable
public class LogicalSet implements XMLStreamParserComponent {

	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "logicalSet";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private List<Criteria> criteria = new ArrayList<Criteria>();
	private Long modelID;
	
	// ===========
	// CONSTRUCTOR
	// ===========
	public LogicalSet(long modelID) {
		this.modelID = modelID;
		this.criteria = new ArrayList<Criteria>();
	}
	
	public LogicalSet(long modelID, List<Criteria> criteria) {
		this.modelID = modelID;
		// Have to copy the list to prevent external mods.
		this.criteria = new ArrayList<Criteria>(criteria.size());
		this.criteria.addAll(criteria);
	}
	// ================
	// INSTANCE METHODS
	// ================
	public LogicalSet parse(XMLStreamReader in)
		throws XMLStreamException, XMLParseValidationException {

		criteria.clear(); // this prevents parse from running twice, as well as after initialiazation of criteria

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
						continue;
					} else if (Criteria.isTargetMatch(localName)) {
						if (criteria.size() > 0) {
							throw new XMLParseValidationException("only one criteria allowed per logicalSet, at the moment");
						}
						
						Criteria c = new Criteria(modelID);
						c.parse(in);
						criteria.add(c);
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (isParseTarget(localName)) {
						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + getParseTarget());
					//break;
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
		return (criteria != null && criteria.size() > 0);
	}
	
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof LogicalSet) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(12497, 134343);
		hash.append(modelID).append(criteria);
		
		return hash.toHashCode();
	}
	
	// =================
	// GETTERS & SETTERS
	// =================

	public List<Criteria> getCriteria() {
		return Collections.unmodifiableList(criteria); // unmodifyable
	}

	public Long getModelID() {
		return modelID;
	}

}


