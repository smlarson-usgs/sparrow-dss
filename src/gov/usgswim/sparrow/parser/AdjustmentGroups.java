package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.XMLStreamParserComponent;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class AdjustmentGroups implements XMLStreamParserComponent, Serializable, Cloneable {
	public static final String MAIN_ELEMENT_NAME = "adjustment-groups";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}
	
	public static AdjustmentGroups parseStream(XMLStreamReader in) throws XMLStreamException {
		AdjustmentGroups ag = new AdjustmentGroups();
		return ag.parse(in);
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private List<ReachGroup> reachGroups = new ArrayList<ReachGroup>();
	private Integer id;
	private String conflicts;
	
	// ================
	// INSTANCE METHODS
	// ================
	public AdjustmentGroups parse(XMLStreamReader in) throws XMLStreamException {
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
						conflicts = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "conflicts");				
					}  else if (ReachGroup.isTargetMatch(localName)) {
						ReachGroup rg = new ReachGroup();
						rg.parse(in);
						reachGroups.add(rg);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						return this; // we're done
					}
					// otherwise, error
					throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	@Override
	public AdjustmentGroups clone() throws CloneNotSupportedException {
		AdjustmentGroups myClone = new AdjustmentGroups();
		// clone the ReachGroups
		myClone.reachGroups = new ArrayList<ReachGroup>(reachGroups.size());
		for (ReachGroup reachGroup: reachGroups) {
			myClone.reachGroups.add(reachGroup.clone());
		}
		
		myClone.id = id;
		myClone.conflicts = conflicts;

		return myClone;
	}

	
	// =================
	// GETTERS & SETTERS
	// =================
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public List<ReachGroup> getReachGroups() {
		return reachGroups;
	}

	public String getConflicts() {
		return conflicts;
	}
}
