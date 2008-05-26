package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;


import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * Represents a single adjustment to a source.
 *
 * Note that an Adjustment is not an independent entity and thus does not override 
 * equals or the hashcode.  It does, however, provide a getStateHash method
 * which generates a repeatable hashcode representing the state of the
 * adjustment..  This method is a convenience to parent
 * classes who need to include the state of their adjustments in their hashcodes.
 */
public class ReachGroup implements XMLStreamParserComponent, Cloneable {
	public static final String MAIN_ELEMENT_NAME = "reach-group";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private boolean isEnabled;
	private String name;
	private String description;
	private String notes;
	
	private List<Adjustment> adjs = new ArrayList<Adjustment>();
	private List<Reach> reaches = new ArrayList<Reach>();
	
	// ================
	// INSTANCE METHODS
	// ================
	public ReachGroup parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {
		
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : 
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
						isEnabled = "true".equals(in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "enabled"));
						name = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "name");
						
					} else if ("notes".equals(localName)) {
						notes = ParserHelper.parseSimpleElementValue(in);
					} else if ("desc".equals(localName)) {
						description = ParserHelper.parseSimpleElementValue(in);
					} else if ("adjustment".equals(localName)) {

						//Lazy build the arrayList
						if (adjs == null) adjs = new ArrayList<Adjustment>();
						
						Adjustment adj = new Adjustment();
						adj.parse(in);
						adjs.add(adj);
						
					} else if ("logical-set".equals(localName)) {
						// ignore for now
						ParserHelper.ignoreElement(in);
					} else if ("reach".equals(localName)) {
						Reach r = new Reach();
						r.parse(in);
						reaches.add(r);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (isParseTarget(localName)) {
						
						//Wrap collections as unmodifiable
						if (reaches != null) {
							reaches = Collections.unmodifiableList(reaches);
						} else {
							reaches = Collections.emptyList();
						}
						
						if (adjs != null) {
							adjs = Collections.unmodifiableList(adjs);
						} else {
							adjs = Collections.emptyList();
						}

						return this; // we're done
					}
					// otherwise, error
					throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + getParseTarget());
					//break;
			}
		}
		throw new RuntimeException("tag <" + getParseTarget() + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
	
	//TODO:  We are copying immutable lists during the cloning.. OK?
	protected ReachGroup clone() throws CloneNotSupportedException {
		ReachGroup myClone = new ReachGroup();
		myClone.isEnabled = isEnabled;
		myClone.name = name;
		myClone.description = description;
		myClone.notes = notes;
		myClone.adjs = adjs;
		myClone.reaches = reaches;
		return myClone;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public boolean isEnabled() {
		return isEnabled;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public String getNotes() {
		return notes;
	}

	public List<Adjustment> getAdjustments() {
  	return adjs;
  }
	
	public List<Reach> getReaches() {
  	return reaches;
  }
	
	/**
	 * Returns a hashcode that fully represents the state of this adjustment.
	 * 
	 * This hashcode is not intended to be unique (others will have the same) and
	 * is not intended to be used for identity.
	 * @return
	 */
	public int getStateHash() {
		HashCodeBuilder hcb = new HashCodeBuilder(4383743, 7221);
		
		hcb.append(name);
		hcb.append(description);
		hcb.append(isEnabled);
		hcb.append(notes);
		
		if (adjs != null) {
			for (Adjustment adj : adjs) {
				hcb.append(adj.getStateHash());
			}
		}
		
		if (reaches != null) {
			for (Reach reach : reaches) {
				hcb.append(reach.getStateHash());
			}
		}
		
		return hcb.toHashCode();
		
	}
	
}
