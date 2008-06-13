package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

//TODO:  This class and all similar should implement comparable.
//TODO:  This class should throw an error if parse is called 2nd time.

/**
 * Represents a single reach as part of a PredictionContext.
 * A Reach has a model-specific Identifier that identifies a specific reach w/in a Model,
 * but the ID is not a DB PK.  Reaches may have one or more Adjustments that
 * are applied to the reach.
 * 
 * Note that a Reach is not an independent entity and thus does not override 
 * equals or the hashcode.  It does, however, provide a getStateHash method
 * which generates a repeatable hashcode representing the state of the
 * reach and its associated adjustments.  This method is a convenience to parent
 * classes who need to include the state of their reaches in their hashcodes.
 */
public class Reach implements XMLStreamParserComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -7230343316711453671L;

	public static final String MAIN_ELEMENT_NAME = "reach";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private Long id;	//The Model Specific ID of the reach. (not the db PK).
	
	//TODO: This should be a sorted set
	private List<Adjustment> adjs;	//List of one or more adjustments
	
	// ================
	// INSTANCE METHODS
	// ================
	public synchronized Reach parse(XMLStreamReader in)
		throws XMLStreamException, XMLParseValidationException {
		
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
						id = ParserHelper.parseAttribAsLong(in, "id");
					} else if ("adjustment".equals(localName)) {
						
						//Lazy build the arrayList
						if (adjs == null) adjs = new ArrayList<Adjustment>();
						
						Adjustment adj = new Adjustment();
						adj.parse(in);
						if (adj.isCoefficient()) {
							throw new XMLParseValidationException("an adjustment which is within a specific reach of a reach group may only be absolute");
						}
						adjs.add(adj);
						
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						
						//Wrap collection as unmodifiable
						if (adjs != null) {
							adjs = Collections.unmodifiableList(adjs);
						} else {
							adjs = Collections.emptyList();
						}
						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new XMLStreamException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
	
	//TODO:  I am just assigning the Adjustment array b/c it is unmodifiable.  Is that truely acceptable...?
	protected Reach clone() throws CloneNotSupportedException {
		Reach myClone = new Reach();
		myClone.id = id;
		myClone.adjs = adjs;
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
	public Long getId() {
		return id;
	}

	public List<Adjustment> getAdjustments() {
		return adjs;
	}

	/**
	 * Returns a hashcode that fully represents the state of this adjustment.
	 * 
	 * This hashcode is not intended to be unique (others will have the same) and
	 * is not intended to be used for identity.
	 * @return
	 */
	public int getStateHash() {

		HashCodeBuilder hcb = new HashCodeBuilder(133457, 29);
		hcb.append(id);

		if (adjs != null) {
			for (Adjustment adj : adjs) {
				hcb.append(adj.getStateHash());
			}
		}
		return hcb.toHashCode();
	}
	
}
