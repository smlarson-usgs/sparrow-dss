package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.ParserHelper;
import gov.usgswim.service.XMLStreamParserComponent;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class PredictionContext implements XMLStreamParserComponent {
	public static final String MAIN_ELEMENT_NAME = "prediction-context";
	public static final String ADJUSTMENT_GROUPS = "adjustment-groups";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private String modelID;
	private String adjustmentGroupConflicts;
	private List<ReachGroup> reachGroups = new ArrayList<ReachGroup>();
	private TerminalReaches terminalReaches;
	private Analysis analysis;

	// ================
	// INSTANCE METHODS
	// ================
	public PredictionContext parse(XMLStreamReader in) throws XMLStreamException {
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : this
			.getClass().getSimpleName()
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
					if (isTargetMatch(localName)) {
						modelID = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "model-id");
					} else if (ADJUSTMENT_GROUPS.equals(localName)) {
						adjustmentGroupConflicts = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "conflicts");
						// advance
						in.next();
					} else if (ReachGroup.isTargetMatch(localName)) {
						ReachGroup rg = new ReachGroup();
						rg.parse(in);
						reachGroups.add(rg);
					} else if (TerminalReaches.isTargetMatch(localName)) {
						TerminalReaches tr = new TerminalReaches();
						tr.parse(in);
						this.terminalReaches = tr;
					} else if (Analysis.isTargetMatch(localName)) {
						Analysis analysis = new Analysis();
						analysis.parse(in);
						this.analysis = analysis;
					} else if ("area-of-interest".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						return this; // we're done
					} else if (ADJUSTMENT_GROUPS.equals(localName)){
						//keep going;
					} else {
						// otherwise, error
						throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}
	
	// =================
	// GETTERS & SETTERS
	// =================
	
	public String getAdjustmentGroupConflicts() {
		return adjustmentGroupConflicts;
	}

	public Analysis getAnalysis() {
		return analysis;
	}

	public String getModelID() {
		return modelID;
	}

	public List<ReachGroup> getReachGroups() {
		return reachGroups;
	}

	public TerminalReaches getTerminalReaches() {
		return terminalReaches;
	}
}
