package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Dedicated NoComparison Singleton.
 * 
 * @author eeverman
 */
public class NoComparison extends Comparison {

	private static final long serialVersionUID = 1L;
	private final int cachedHash;
	
	public static final NoComparison NO_COMPARISON = new NoComparison();
	
	public static NoComparison getInstance() {
		return NO_COMPARISON;
	}


	/** No instances - use the getInstance() method */
	private NoComparison() {
		cachedHash = buildHashCode();
	};


	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof NoComparison) {
			return true;
		}
		return false;
	}
	
	/**
	 * Creates a unique and repeatable hash for just the fields within this class.
	 * Subclasses should either include these fields or
	 * include the hash value from this function.
	 */
	@Override
	public synchronized int hashCode() {
		return cachedHash;
	}
	
	private int buildHashCode() {
		int hash = new HashCodeBuilder(38923, 23476331).toHashCode();
		return hash;
	}

	//
	// Although this extends Comparison, it does not implement the parse method.
	// Rather than throw a Runtime Exception, it seems appropriate to return
	// null/false for these methods.
	//
	@Override
	public XMLStreamParserComponent parse(XMLStreamReader in) {
		return null;
	}

	@Override
	public String getParseTarget() {
		return null;
	}

	@Override
	public boolean isParseTarget(String name) {
		return false;
	}


}
