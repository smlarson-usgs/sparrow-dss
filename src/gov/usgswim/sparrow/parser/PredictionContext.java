package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.XMLStreamParserComponent;

import java.io.Serializable;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class PredictionContext implements XMLStreamParserComponent, Serializable, Cloneable {

	private static final long serialVersionUID = -5343918321449313545L;
	public static final String MAIN_ELEMENT_NAME = "prediction-context";
	public static final String ADJUSTMENT_GROUPS = "adjustment-groups";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	private Integer id;
	private Integer modelID;
	private Integer adjustmentGroupsID;
	private Integer analysisID;
	private Integer terminalReachesID;
	private Integer areaOfInterestID;
	
	private transient AdjustmentGroups adjustmentGroups;
	private transient Analysis analysis;
	private transient TerminalReaches terminalReaches;
	private AreaOfInterest areaOfInterest;
	

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
						String modelIdString = in.getAttributeValue(DEFAULT_NS_PREFIX, "model-id");
						modelID = (modelIdString == null)? null: Integer.valueOf(modelIdString);
						
						String idString = in.getAttributeValue(DEFAULT_NS_PREFIX, XMLStreamParserComponent.ID_ATTR);
						id = (idString == null)? null: Integer.valueOf(idString);
					} else if (AdjustmentGroups.isTargetMatch(localName)) {
						AdjustmentGroups ag = new AdjustmentGroups();
						ag.parse(in);
						this.adjustmentGroups = ag;
						adjustmentGroupsID = (ag == null)? null: ag.getId();
					} else if (TerminalReaches.isTargetMatch(localName)) {
						TerminalReaches tr = new TerminalReaches();
						tr.parse(in);
						this.terminalReaches = tr;
						terminalReachesID = (tr == null)? null: tr.getId();
					} else if (Analysis.isTargetMatch(localName)) {
						Analysis analysis = new Analysis();
						analysis.parse(in);
						this.analysis = analysis;
						analysisID = (analysis == null)? null: analysis.getId();
					} else if (AreaOfInterest.isTargetMatch(localName)) {
						AreaOfInterest aoi = new AreaOfInterest();
						aoi.parse(in);
						this.areaOfInterest = aoi;
						areaOfInterestID = (areaOfInterest == null)? null: areaOfInterest.getId();
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
	
	public int hashCode() {
		int hash = new HashCodeBuilder(13, 16661).
		append(modelID).
		append(adjustmentGroupsID).
		append(analysisID).
		append(terminalReachesID).
		append(areaOfInterestID).
		toHashCode();
		return hash;
	}

	/* A simple clone method, caveat emptor as it doesn't deal with transient children.
	 * @see java.lang.Object#clone()
	 */
	public PredictionContext clone() throws CloneNotSupportedException {
		PredictionContext myClone = new PredictionContext();
		myClone.modelID = modelID;
		myClone.adjustmentGroupsID = adjustmentGroupsID;
		myClone.analysisID = analysisID;
		myClone.terminalReachesID = terminalReachesID;
		myClone.areaOfInterestID = areaOfInterestID;
		myClone.adjustmentGroups = adjustmentGroups.clone();
		myClone.analysis = analysis.clone();
		myClone.terminalReaches = terminalReaches.clone();

		return myClone;
	}
	
	/**
	 * Clones with supplied transient children. Does not clone supplied children.
	 * 
	 * @param ag
	 * @param anal
	 * @param tr
	 * @return
	 * @throws CloneNotSupportedException
	 */
	public PredictionContext clone(AdjustmentGroups ag, Analysis anal, TerminalReaches tr) throws CloneNotSupportedException {
		PredictionContext myClone = this.clone();
		
		// populate the transient children only if necessary & correct
		if (adjustmentGroupsID != null && ag != null && ag.getId().equals(adjustmentGroupsID)) {
			myClone.adjustmentGroups = ag;
		}
		
		if (analysisID != null && anal != null && anal.getId().equals(analysisID)) {
			myClone.analysis = anal;
		}
		
		if (terminalReachesID != null && tr != null && tr.getId().equals(terminalReachesID)) {
			myClone.terminalReaches = tr;
		}	

		return myClone;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public Analysis getAnalysis() {
		return analysis;
	}

	public Integer getModelID() {
		return modelID;
	}

	public TerminalReaches getTerminalReaches() {
		return terminalReaches;
	}

	public AdjustmentGroups getAdjustmentGroups() {
		return adjustmentGroups;
	}

	public Integer getId() {
		return id;
	}

	public Integer getAdjustmentGroupsID() {
		return adjustmentGroupsID;
	}

	public Integer getAnalysisID() {
		return analysisID;
	}

	public Integer getAreaOfInterestID() {
		return areaOfInterestID;
	}

	public Integer getTerminalReachesID() {
		return terminalReachesID;
	}

	public AreaOfInterest getAreaOfInterest() {
		return areaOfInterest;
	}
}
