package gov.usgswim.sparrow.domain;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgs.cida.datatable.DataTable;
import gov.usgs.cida.datatable.adjustment.ColumnCoefAdjustment;
import gov.usgs.cida.datatable.adjustment.SparseCoefficientAdjustment;
import gov.usgs.cida.datatable.adjustment.SparseOverrideAdjustment;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.parser.DefaultGroupParser;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

/**
 * A direct child of PredictionContext that contains all of the adjustments
 * to be applied to the model.
 * 
 * Three sets of ReachGroups are held by this class, which each contain
 * different type of adjustment applications:
 * <ul>
 * <li>reachGroups contains a list of ReachGroups, one for each group the user creates.
 * <li>defaultGroup contains model-wide adjustments.  For instance, a default
 * adjustment could be applied to adjust source 1 for every reach in the model.
 * <li>individualGroup contains individual reaches which each have a separate
 * adjustment applied.
 * </ul>
 * @author eeverman
 *
 */
public class AdjustmentGroups implements XMLStreamParserComponent {

	private static final long serialVersionUID = 1L;
	public static final String MAIN_ELEMENT_NAME = "adjustmentGroups";
	
	public static enum ADJUSTMENT_CONFLICTS {
		accumulate, override
	}
	
	

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
	private Integer id;
	
	private Long modelID;
	private String conflicts;	//This should be an enum
	private List<ReachGroup> reachGroups = new ArrayList<ReachGroup>();
	
	/** Model-wide adjustment */
	private ReachGroup defaultGroup;
	
	/** Group containing individual reaches to be adjusted. */
	private ReachGroup individualGroup;



	/**
	 * Fully spec'ed constructor.
	 * 
	 * @param id
	 * @param modelID
	 * @param conflicts
	 * @param reachGroups
	 * @param defaultGroup
	 * @param individualGroup
	 */
	public AdjustmentGroups(Long modelID, String conflicts,
			List<ReachGroup> reachGroups, ReachGroup defaultGroup,
			ReachGroup individualGroup) {
		this.modelID = modelID;
		this.conflicts = conflicts;
		this.reachGroups = reachGroups;
		this.defaultGroup = defaultGroup;
		this.individualGroup = individualGroup;
	}

	/**
	 * Its OK to build and use a model ID-only instance.  It will just have
	 * zero adjustments.
	 * Constructor requires a modelID
	 */
	public AdjustmentGroups(Long modelID) {
		this.modelID = modelID;
		this.conflicts = ADJUSTMENT_CONFLICTS.accumulate.toString();		//arbitrary, but didn't want to leave null
	}


	//TODO: Parse should attempt to find the AG in the cache if it gets a ID.

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
	
	public AdjustmentGroups getNoAdjustmentVersion() {
		AdjustmentGroups myClone = new AdjustmentGroups(modelID);
		myClone.conflicts = "accumulate";	//Arbitrary - it doesn't matter.
		return myClone;
	}

	/**
	 * Consider two instances the same if they have the same calculated hashcodes
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof AdjustmentGroups) {
			return obj.hashCode() == hashCode();
		}
		return false;
	}

	@Override
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
	
	/**
	 * 
	 * @return true if any groups or individual reaches contained in those groups has adjustments made to it.
	 */
	public boolean hasAdjustments() {
		if(defaultGroup != null) {
			if(defaultGroup.getAdjustments().size()>0) return true;
			for(ReachElement r : defaultGroup.getExplicitReaches()){
				if(r.getAdjustments().size() > 0) {
					return true;
				}
			}
		}
		
		if(individualGroup != null) {
			if(individualGroup.getAdjustments().size()>0) return true;
			for(ReachElement r : individualGroup.getExplicitReaches()){
				if(r.getAdjustments().size() > 0) {
					return true;
				}
			}
		}
		
		for(ReachGroup g : reachGroups){
			if(g.getAdjustments().size()>0) return true;
			for(ReachElement r : g.getExplicitReaches()){
				if(r.getAdjustments().size() > 0) {
					return true;
				}
			}
		}
		
		return false;
	}
}
