package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public abstract class Comparison implements XMLStreamParserComponent {

	private static final long serialVersionUID = 1L;

	// ===============
	// INSTANCE FIELDS
	// ===============
	protected ComparisonType comparisonType = ComparisonType.none;

	public Comparison() {};

	/**
	 * Utility method to parse Analysis instances w/o needing to known what type
	 * they are.  Primarily used for testing.
	 * @param in
	 * @return
	 * @throws XMLStreamException
	 * @throws XMLParseValidationException
	 */
	public static Comparison parseAnyAnalysis(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {
		
		String localName = in.getLocalName();
		
		if (NominalComparison.isTargetMatch(localName)) {
			NominalComparison a = new NominalComparison();
			try {
				a.parse(in);
			} catch (XMLParseValidationException e) {
				//Continue, ignoring a validation exception
			}
			return a;
		} else if (SourceShareComparison.isTargetMatch(localName)) {
			SourceShareComparison a = new SourceShareComparison();
			try {
				a.parse(in);
			} catch (XMLParseValidationException e) {
				//Continue, ignoring a validation exception
			}
			return a;
		} else if (AdvancedComparison.isTargetMatch(localName)) {
			AdvancedComparison a = new AdvancedComparison();
			try {
				a.parse(in);
			} catch (XMLParseValidationException e) {
				//Continue, ignoring a validation exception
			}
			return a;
		} else {
			throw new XMLParseValidationException("tag <" + localName
					+ "> not a valid type of comparison.");
		}
		
	}

	public void checkValidity() throws XMLParseValidationException {
		
		//comparisonType is required
		if (comparisonType == null) {
			throw new XMLParseValidationException(
			"A comparisonType is required.");
		}
		
	}

	public boolean isValid() {
		try {
			checkValidity();
			return true;
		} catch (XMLParseValidationException e) {
			return false;
		}
	}


	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public abstract boolean equals(Object obj);

	/**
	 * Creates a unique and repeatable hash for just the fields within this class.
	 * Subclasses should either include these fields or
	 * include the hash value from this function.
	 */
	@Override
	public synchronized int hashCode() {
		int hash = new HashCodeBuilder(137, 1729).
		append(comparisonType.ordinal()).
		toHashCode();
		return hash;
	}
	
	// =================
	// GETTERS & SETTERS
	// =================

	public ComparisonType getComparisonType() {
		return comparisonType;
	}
	
	public Integer getId() {
		return hashCode();
	}

}
