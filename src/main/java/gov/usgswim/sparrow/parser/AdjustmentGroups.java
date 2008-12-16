package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.adjustment.ColumnCoefAdjustment;
import gov.usgswim.datatable.adjustment.SparseCoefficientAdjustment;
import gov.usgswim.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.sparrow.PredictData;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class AdjustmentGroups implements XMLStreamParserComponent {

	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "adjustment-groups";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	public static AdjustmentGroups parseStream(XMLStreamReader in, Long modelID)
	throws XMLStreamException, XMLParseValidationException {

		AdjustmentGroups ag = new AdjustmentGroups(modelID);
		return ag.parse(in);
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	private Long modelID;
	private List<ReachGroup> reachGroups = new ArrayList<ReachGroup>();
	private ReachGroup defaultGroup;
	private ReachGroup individualGroup;
	private Integer id;
	private String conflicts;	//This should be an enum

	//TODO: Parse should attempt to find the AG in the cache if it gets a ID.

	/**
	 * Constructor requires a modelID
	 */
	public AdjustmentGroups(Long modelID) {
		this.modelID = modelID;
	}
	

	// ================
	// INSTANCE METHODS
	// ================
	public AdjustmentGroups parse(XMLStreamReader in)
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
						//id = ParserHelper.parseAttribAsInt(in, XMLStreamParserComponent.ID_ATTR, false);
						conflicts = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "conflicts");				
					} else if (ReachGroup.isTargetMatch(localName)) {
						ReachGroup rg = new ReachGroup(modelID);
						rg.parse(in);
						reachGroups.add(rg);
					} else if (DefaultGroupParser.isTargetMatch(localName)) {
					    // TODO: we should check for only 1 default group
						DefaultGroupParser dg = new DefaultGroupParser(modelID);
						defaultGroup = dg.parse(in);
					} else if (IndividualGroup.isTargetMatch(localName)) {
					    // TODO: we should check for only 1 individual group
					    IndividualGroup ig = new IndividualGroup(modelID);
					    individualGroup = ig.parse(in);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					//break;
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

	@Override
	public AdjustmentGroups clone() throws CloneNotSupportedException {
		AdjustmentGroups myClone = new AdjustmentGroups(modelID);
		// clone the ReachGroups
		myClone.reachGroups = new ArrayList<ReachGroup>(reachGroups.size());
		for (ReachGroup reachGroup: reachGroups) {
			myClone.reachGroups.add(reachGroup.clone());
		}
		if (defaultGroup != null) {
			myClone.defaultGroup = defaultGroup.clone();
		}
		if (individualGroup != null) {
		    myClone.individualGroup = individualGroup.clone();
		}
		myClone.conflicts = conflicts;

		return myClone;
	}

	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	public boolean equals(Object obj) {
		if (obj instanceof AdjustmentGroups) {
			return obj.hashCode() == hashCode();
		} else {
			return false;
		}
	}

	public synchronized int hashCode() {
		if (id == null) {
			HashCodeBuilder hash = new HashCodeBuilder(17, 13);
			hash.append(modelID);
			hash.append(conflicts);

			if (defaultGroup != null) {
				hash.append(defaultGroup.getStateHash());
			}

			if (reachGroups != null && reachGroups.size() > 0) {
				for (ReachGroup rg: reachGroups) {
					hash.append(rg.getStateHash());
				}
			}
			
			if (individualGroup != null) {
			    hash.append(individualGroup.getStateHash());
			}

			id = hash.toHashCode();
		} 

		return id;
	}

	/**
	 * Actually does the adjustment, returning sparse view that wraps the source
	 * data in the passed PredictData.
	 * 
	 * @param source
	 * @param srcIndex
	 * @param reachIndex
	 * @throws Exception
	 */
	public DataTable adjust(PredictData data) throws Exception {

		DataTable adjusted = data.getSrc();	//start assuming there are no adjustments

		//Do model-wide adjustments first.  Any further adjustments will accumulate/override as appropriate
		if (defaultGroup != null && defaultGroup.getAdjustments().size() > 0 && defaultGroup.isEnabled()) {

			ColumnCoefAdjustment colAdj = new ColumnCoefAdjustment(adjusted);
			adjusted = colAdj;

			for (Adjustment adj: defaultGroup.getAdjustments()) {
				Double coef = adj.getCoefficient();
				Integer srcId = adj.getSource();

				//Logic check...
				if (coef == null || srcId == null) {
					throw new Exception("For a global adjustment, a source and coefficient must be specified");
				}

				colAdj.setColumnMultiplier(data.getSourceIndexForSourceID(srcId), coef);

			}
		}

        //Two places to adjust:  SparseCoeff allows coeff adjustments to individual
        //reaches, SparesOverride, which wraps coef, allows absolute value adjustments
        //to individual reaches.
        SparseCoefficientAdjustment coefAdj = new SparseCoefficientAdjustment(adjusted);
        SparseOverrideAdjustment overAdj = new SparseOverrideAdjustment(coefAdj);

		//Loop thru ReachGroups to do adjustments
		//Here we are assuming conflict accumulate
		if (reachGroups != null && reachGroups.size() > 0) {

			for (ReachGroup rg: reachGroups) {
				if (rg.isEnabled()) {

					List<Adjustment> adjustments = rg.getAdjustments();
					
					// Apply ReachGroup-wide adjustments to all reaches in the combined reaches.
					if (adjustments != null) {
						// Look up corresponding source indices for each adjustment to save some lookups
						int[] adjSourceColumn = new int[rg.getAdjustments().size()];
						for (int i=0; i<adjustments.size(); i++) {
							adjSourceColumn[i] = data.getSourceIndexForSourceID(rg.getAdjustments().get(i).getSource());
						}
						
						// Apply the adjustments to each reach in the combined logical and explicit reaches
						for (Long reachID: rg.getCombinedReachIDs()) {
							int row = data.getRowForReachID(reachID);
							for (int i=0; i<adjustments.size(); i++) {
								applyAdjustmentToReach(adjustments.get(i), row, adjSourceColumn[i], coefAdj, overAdj);
							}
						}
					}
				}
			}
		}
		
		// Do individual reach adjustments
		if (individualGroup != null
		        && individualGroup.getExplicitReaches().size() > 0
		        && individualGroup.isEnabled()) {
		    
		    // Iterate over the explicit set of reaches and apply adjustments
            for (ReachElement r: individualGroup.getExplicitReaches()) {
                int row = data.getRowForReachID(r.getId());
                // Apply the adjustments specified for just this reach (if any)
                // Note:  getAdjustments() never returns null
                for (Adjustment adj: r.getAdjustments()) {
                    Integer srcId = adj.getSource();
                    applyAdjustmentToReach(adj, row, data.getSourceIndexForSourceID(srcId), coefAdj, overAdj);
                }
            }
        }

        adjusted = overAdj; //resulting adjustment
        return adjusted;
    }

	private void applyAdjustmentToReach(Adjustment adj, int row, int col, SparseCoefficientAdjustment coefAdj, SparseOverrideAdjustment overAdj) throws Exception {
		Double coef = adj.getCoefficient();
		
		if (coef != null) {

			//if a coef already exists, the new coef is the product of the existing and the new

			Number existingCoef = coefAdj.getCoef(row , col);
			if (existingCoef != null) {
				coef = coef.doubleValue() * existingCoef.doubleValue();
			}

			coefAdj.setValue(coef, row, col);
		} else {
			Double abs = adj.getAbsolute();
			overAdj.setValue(abs, row, col);
		}
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
	public Integer getId() {
		return hashCode();
	}

	public Long getModelID() {
		return modelID;
	}

	public List<ReachGroup> getReachGroups() {
		return reachGroups;
	}

	public String getConflicts() {
		return conflicts;
	}

	/**
	 * May return null
	 * @return
	 */
	public ReachGroup getDefaultGroup() {
		return defaultGroup;
	}
	
	public ReachGroup getIndividualGroup() {
	    return individualGroup;
	}
}
