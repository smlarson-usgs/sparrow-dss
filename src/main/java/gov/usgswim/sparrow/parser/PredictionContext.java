package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.service.SharedApplication;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * The entire info needed to run a prediction and analyze the results.
 *
 * TODO: Serialization does not seem to be setup correctly.  Weren't the IDs
 * supposed to allow the main components to be placed in other caches so that
 * this would only hold onto the IDs?  Seems like that original idea might no
 * longer be needed, since there is not really anything to associate with those
 * caches.
 *
 * @author eeverman
 *
 */
public class PredictionContext implements XMLStreamParserComponent {

	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "PredictionContext";


	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	public static PredictionContext parseStream(XMLStreamReader in)
	throws XMLStreamException, XMLParseValidationException {

		PredictionContext ag = new PredictionContext();
		return ag.parse(in);
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	private Integer id;
	private Long modelID;
	private Integer adjustmentGroupsID;
	private Integer analysisID;
	private Integer terminalReachesID;
	private Integer areaOfInterestID;
	private Integer comparisonID;

	private transient AdjustmentGroups adjustmentGroups;
	private transient Analysis analysis;
	private transient TerminalReaches terminalReaches;
	private transient AreaOfInterest areaOfInterest;

	//This is not transient - see no reason to cache this separately.
	private Comparison comparison;

	// ============
	// CONSTRUCTORS
	// ============
	/**
	 * Constructs an empty instance.
	 */
	public PredictionContext() {
		// empty constructor
	}
	/**
	 * Constructs a fully configured instance.
	 *
	 * @param modelID
	 * @param ag adjustment groups
	 * @param anal analysis
	 * @param tr terminal reaches
	 * @param aoi area of interest
	 * @return
	 */
	public PredictionContext(Long modelID, AdjustmentGroups ag, Analysis anal,
			TerminalReaches tr, AreaOfInterest aoi, Comparison comp) {

		this.modelID = modelID;

		if (ag != null) {
			this.adjustmentGroups = ag;
			this.adjustmentGroupsID = ag.getId();
		}

		if (anal != null) {
			this.analysis = anal;
			this.analysisID = anal.getId();
		}

		if (tr != null) {
			this.terminalReaches = tr;
			this.terminalReachesID = tr.getId();
		}

		if (aoi != null) {
			this.areaOfInterest = aoi;
			this.areaOfInterestID = aoi.getId();
		}

		if (comp != null) {
			this.comparison = comp;
			this.comparisonID = comp.getId();
		} else {
			this.comparison = NominalComparison.getNoComparisonInstance();
		}

	}

	// ================
	// INSTANCE METHODS
	// ================
	public PredictionContext parse(XMLStreamReader in)
	throws XMLStreamException, XMLParseValidationException {

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
						modelID = (modelIdString == null || modelIdString.length() == 0)? null: Long.valueOf(modelIdString);

						String idString = in.getAttributeValue(DEFAULT_NS_PREFIX, XMLStreamParserComponent.ID_ATTR);
						id = (idString == null || idString.length() == 0)? null: Integer.valueOf(idString);
					}// the following are all children matches
					else if (AdjustmentGroups.isTargetMatch(localName)) {
						this.adjustmentGroups = AdjustmentGroups.parseStream(in, modelID);
						adjustmentGroupsID = (adjustmentGroups == null)? null: adjustmentGroups.getId();
					} else if (TerminalReaches.isTargetMatch(localName)) {
						this.terminalReaches = TerminalReaches.parseStream(in, modelID);
						terminalReachesID = (terminalReaches == null)? null: terminalReaches.getId();
					} else if (AdvancedAnalysis.isTargetMatch(localName)) {
						this.analysis = AdvancedAnalysis.parseStream(in);
						analysisID = (analysis == null)? null: analysis.getId();
					} else if (BasicAnalysis.isTargetMatch(localName)) {
						this.analysis = BasicAnalysis.parseStream(in);
						analysisID = (analysis == null)? null: analysis.getId();
					} else if (AreaOfInterest.isTargetMatch(localName)) {
						this.areaOfInterest = AreaOfInterest.parseStream(in, modelID);
						areaOfInterestID = (areaOfInterest == null)? null: areaOfInterest.getId();
					} else if (NominalComparison.isTargetMatch(localName)) {
						NominalComparison comp = new NominalComparison();
						this.comparison = comp.parse(in);
						comparisonID = (comparison == null)? null: comparison.getId();
					} else if (AdvancedComparison.isTargetMatch(localName)) {
						AdvancedComparison comp = new AdvancedComparison();
						this.comparison = comp.parse(in);
						comparisonID = (comparison == null)? null: comparison.getId();
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						// TODO [IK] Might want to calculate PC id here.
						// TODO [eric] If the ID is unavailable because this is
						// a new PContext, when in the object lifecycle should
						// id be calculated and populated? Here? on cache.put()?

						if (comparison == null) {
							comparison = NominalComparison.getNoComparisonInstance();
							comparisonID = comparison.getId();
						}

						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
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
	 * Centralized method to get a reference to the data table and a column in it
	 * for use any place we need to access the data column. The data column will
	 * be used for the map coloring
	 *
	 * @return
	 * @throws Exception
	 */
	public DataColumn getDataColumn() throws Exception {

		if (ComparisonType.none.equals(comparison.getComparisonType())) {
			return SharedApplication.getInstance().getAnalysisResult(this);
		} else {
			return SharedApplication.getInstance().getComparisonResult(this);
		}
	}

	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PredictionContext) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
	public synchronized int hashCode() {
		if (id == null) {

			int hash = new HashCodeBuilder(13, 16661).
			append(modelID).
			append(adjustmentGroupsID).
			append(analysisID).
			append(terminalReachesID).
			append(areaOfInterestID).
			append(comparisonID).
			toHashCode();
			id = hash;
		}

		return id;
	}

	/**
	 * A simple clone method, caveat emptor as it doesn't deal with transient
	 * children.
	 *
	 * @see java.lang.Object#clone()
	 */
	@Override
	public PredictionContext clone() throws CloneNotSupportedException {

		PredictionContext myClone = new PredictionContext();
		myClone.modelID = modelID;
		myClone.adjustmentGroupsID = adjustmentGroupsID;
		myClone.analysisID = analysisID;
		myClone.terminalReachesID = terminalReachesID;
		myClone.areaOfInterestID = areaOfInterestID;
		myClone.comparisonID = comparisonID;

		myClone.adjustmentGroups = (adjustmentGroups == null)? null: adjustmentGroups.clone();
		myClone.analysis = (analysis == null)? null: analysis.clone();
		myClone.terminalReaches = (terminalReaches == null)? null: terminalReaches.clone();
		myClone.areaOfInterest = (areaOfInterest == null)? null: areaOfInterest.clone();
		myClone.comparison = comparison;	//immutable

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
	public PredictionContext clone(AdjustmentGroups ag, Analysis anal, TerminalReaches tr, AreaOfInterest aoi) throws CloneNotSupportedException {
		PredictionContext myClone = this.clone();
		// TODO [IK] log error conditions appropriately. Return null if error?
		// TODO [eric] Determine error behavior. Suggest return null if error.

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

		if (areaOfInterestID != null && aoi != null && aoi.getId().equals(areaOfInterestID)) {
			myClone.areaOfInterest = aoi;
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
	// ===========
	// KEY METHODS
	// ===========
	public PredictionContext getTargetContextOnly() {
		return new PredictionContext(modelID, null, null, terminalReaches, null, comparison);
	}

	public PredictionContext getAdjustedContextOnly() {
		return new PredictionContext(modelID, adjustmentGroups, null, null, null, comparison);
	}

	public PredictionContext getNoComparisonVersion() {
		return new PredictionContext(modelID, adjustmentGroups, analysis,
				terminalReaches, areaOfInterest, NominalComparison.NO_COMPARISON);
	}

	public PredictionContext getNoAdjustmentVersion() {
		return new PredictionContext(modelID, new AdjustmentGroups(modelID), analysis,
				terminalReaches, areaOfInterest, comparison);
	}


	// =================
	// GETTERS & SETTERS
	// =================
	public Analysis getAnalysis() {
		return analysis;
	}

	public Long getModelID() {
		return modelID;
	}

	public TerminalReaches getTerminalReaches() {
		return terminalReaches;
	}

	public AdjustmentGroups getAdjustmentGroups() {
		return adjustmentGroups;
	}

	public Integer getId() {
		return hashCode();
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

	public Integer getComparisonID() {
		return comparisonID;
	}

	public Comparison getComparison() {
		return comparison;
	}



	/**
	 * An inner class to bundle a DataTable and a column index together so that
	 * it is possible to return these two together for methods returning the
	 * data column.
	 *
	 * @author eeverman
	 *
	 */
	public static class DataColumn {
		private final DataTable table;
		private final int column;

		public DataColumn(DataTable table, int column) {
			this.table = table;
			this.column = column;
		}

		public DataTable getTable() {
			return table;
		}

		public int getColumn() {
			return column;
		}

	}


}
