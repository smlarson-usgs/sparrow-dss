package gov.usgswim.sparrow.parser;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.Immutable;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;
/**
 * Simple bean class to hold logical-set identification serving as a key to
 * cached logical-set set of reaches.
 * 
 * @author eeverman
 * 
 */
@Immutable
public class LogicalSet implements XMLStreamParserComponent {

	private static final long serialVersionUID = 4020487873395320955L;
	public static final String MAIN_ELEMENT_NAME = "logical-set";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private Map<String, String> criteria;
	private final long modelID;
	
	// ===========
	// CONSTRUCTOR
	// ===========
	public LogicalSet(long modelID) {
		this.modelID = modelID;
		this.criteria = new HashMap<String, String>();
	}
	
	public LogicalSet(long modelID, Map<String, String> crit) {
		this.modelID = modelID;
		// Have to make this unmodifiable, and a different reference from the
		// original otherwise there's gonna be trouble.
		this.criteria = Collections.unmodifiableMap(crit);
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
					} else if ("criteria".equals(localName)) {
						if (criteria.size() > 0) {
							throw new XMLParseValidationException("only one criteria allowed per logical-set, at the moment");
						}
						String attrib = in.getAttributeValue("","attrib");
						String value = ParserHelper.parseSimpleElementValue(in);
						criteria.put(attrib, value);
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (isParseTarget(localName)) {
						// fix the criteria
						criteria = Collections.unmodifiableMap(criteria);

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

//		if (obj != null && obj instanceof LogicalSet) {
//			if (obj == null) return false;
//			LogicalSet other = (LogicalSet) obj;
//			if (this.modelID != other.modelID) return false;
//			if (this.criteria == null && other.criteria == null) return true;
//			if (this.criteria == null || other.criteria == null) return false;
//			return this.criteria.equals(other.criteria); // Note that map.equals compares only on the value of map entries
//		} else {
//			return false;
//		}
	}

	@Override
	public synchronized int hashCode() {
		HashCodeBuilder hash = new HashCodeBuilder(12497, 134343);
		hash.append(modelID).append(criteria);
		
		return hash.toHashCode();
		
//		if (criteria != null) {
//			// Note that map.hashCode() is generated only based on values of its entries.
//			return criteria.hashCode() + Long.valueOf(modelID%161).intValue();
//		} else {
//			return Long.valueOf(modelID%161).intValue(); // need to return int but modleID is long.
//		}
	}
	
	// =================
	// GETTERS & SETTERS
	// =================

	public Map<String, String> getCriteria() {
		return Collections.unmodifiableMap(criteria); // unmodifyable copy
	}

	public long getModelID() {
		return modelID;
	}

}


