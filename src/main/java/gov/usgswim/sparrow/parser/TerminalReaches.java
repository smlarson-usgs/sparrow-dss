package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import gov.usgswim.sparrow.util.ParserHelper;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class TerminalReaches implements XMLStreamParserComponent {

	private static final long serialVersionUID = 8804027069848411715L;
	private static final String REACHES_CHILD = "reach";
	public static final String MAIN_ELEMENT_NAME = "terminalReaches";


	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	public static TerminalReaches parseStream(XMLStreamReader in, Long modelID) throws XMLStreamException, XMLParseValidationException {
		TerminalReaches tr = new TerminalReaches(modelID);
		return tr.parse(in);
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	private Long modelID;
	protected List<Long> reachIDs = new ArrayList<Long>();
	private Integer id;

	/**
	 * Constructor requires a modelID
	 */
	public TerminalReaches(Long modelID) {
		this.modelID = modelID;
	}

	// ================
	// INSTANCE METHODS
	// ================
	public TerminalReaches parse(XMLStreamReader in) throws XMLStreamException, XMLParseValidationException {
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
                    } else if (ReachElement.isTargetMatch(localName)) {
                        ReachElement r = new ReachElement();
                        r.parse(in);
						reachIDs.add(r.getId());
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
					} else if (REACHES_CHILD.equals(localName)) {

					} else {// otherwise, error
						throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					break;
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
	 * Returns the terminal reaches as a set.
	 * @return Set or reach IDs
	 */
	public Set<Long> asSet() {
		// [IK] Why don't we just return the List reachIDs or just make reachIDs
		// a set? The second option doesn't work because we want a deterministic
		// hashcode function as we loop over the reachIDs. The first doesn't work
		// because we want independence of reach id order.
		Set<Long> targetReaches = new HashSet<Long>();
		for (Long reach: reachIDs) {
			targetReaches.add(reach);
		}
		return targetReaches;
	}
	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TerminalReaches) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {
		if (id == null) {
			HashCodeBuilder hashBuilder = new HashCodeBuilder(137, 1729);

			hashBuilder.append(modelID);
			for (Long idValue: reachIDs) {
				hashBuilder.append(idValue);
			}
			int hash = hashBuilder.toHashCode();

			id = hash;
		}

		return id;
	}

	@Override
	public TerminalReaches clone() throws CloneNotSupportedException {
		TerminalReaches myClone = new TerminalReaches(modelID);
		myClone.reachIDs = new ArrayList<Long>(reachIDs.size());
		for (Long reachID: reachIDs) {
			myClone.reachIDs.add(reachID);
		}

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
	public List<Long> getReachIDs(){
		//TODO: [ee] This should be wrapped as an immutable (same for all maps)
		return reachIDs;
	}

	public Long getModelID() {
		return modelID;
	}

	public Integer getId() {
		return hashCode();
	}



}
