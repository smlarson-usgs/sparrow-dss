package gov.usgswim.sparrow.parser;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.service.SharedApplication;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
public class ReachGroup implements XMLStreamParserComponent {
	/**
	 * 
	 */
	private static final long serialVersionUID = -5679697265841687562L;
	public static final String MAIN_ELEMENT_NAME = "reach-group";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	// ===========
	// CONSTRUCTOR
	// ===========
	public ReachGroup(long modelID) {
		this.modelID = modelID;
	}
	
	private boolean isEnabled;
	private String name;
	private String description;
	private String notes;
	
	private List<Adjustment> adjs = new ArrayList<Adjustment>();
	private List<Reach> reaches = new ArrayList<Reach>();
	private List<LogicalSet> logicalSets;
	private transient List<List<Long>> reachIDsByLogicalSets; // transient as this is fetched from cache
	
	// search
	private Set<Long> containedReachIDs;
	private long modelID;
	
	// ================
	// INSTANCE METHODS
	// ================
	public ReachGroup parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {
		
		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isParseTarget(localName) && eventCode == START_ELEMENT) : 
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
					} else if (Adjustment.isTargetMatch(localName)) {

						//Lazy build the arrayList
						if (adjs == null) adjs = new ArrayList<Adjustment>();
						
						Adjustment adj = new Adjustment();
						adj.parse(in);
						adjs.add(adj);
						
					} else if (LogicalSet.isTargetMatch(localName)) {

						if (logicalSets == null) logicalSets = new ArrayList<LogicalSet>();

						LogicalSet ls = new LogicalSet(modelID);
						ls.parse(in);
						logicalSets.add(ls);
					} else if (Reach.isTargetMatch(localName)) {
						Reach r = new Reach();
						r.parse(in);
						reaches.add(r);
					} else {
						throw new XMLParseValidationException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
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
						
						if (logicalSets != null) {
							logicalSets = Collections.unmodifiableList(logicalSets);
						} else {
							logicalSets = Collections.emptyList();
						}

						checkValidity();
						return this; // we're done
					}
					// otherwise, error
					throw new XMLParseValidationException("unexpected closing tag of </" + localName + ">; expected  " + getParseTarget());
					//break;
			}
		}
		throw new XMLParseValidationException("tag <" + getParseTarget() + "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}
	
	
	public ReachGroup clone() throws CloneNotSupportedException {
		// DONE: We are copying immutable lists during the cloning.. OK?
		ReachGroup myClone = new ReachGroup(modelID);
		myClone.isEnabled = isEnabled;
		myClone.name = name;
		myClone.description = description;
		myClone.notes = notes;
		myClone.adjs = adjs; // immutable
		myClone.reaches = reaches; // immutable
		myClone.logicalSets = logicalSets; // immutable
		// Deliberately NOT copying reachIDsByLogicalSets, relying on
		// late-binding code in getLogicalReachIDs to fetch from cache.
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
	
	// ==============
	// SEARCH METHODS
	// ==============
	/**
	 * @param reachID
	 * @return
	 * 
	 * WARNING: do not call this method until all the reaches have been added to the group, as it will make subsequent search behavior incorrect
	 */
	public boolean contains(long reachID) {
		if (reaches == null) return false;
		// late initialize as necessary
		if (containedReachIDs == null) {
			containedReachIDs = new HashSet<Long>();
			for (Reach reach: reaches) {
				containedReachIDs.add(reach.getId());
			}
		}
		return containedReachIDs.contains(reachID);
	}
	
	public boolean contains(Reach reach) {
		return contains(reach.getId());
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

	public List<LogicalSet> getLogicalSets() {
		return logicalSets;
	}

	public long getModelID() {
		return modelID;
	}

	public List<Reach> getExplicitReaches() {
		return reaches;
	}
	
	/**
	 * @param i
	 * @return reachIds for the ith logical-set
	 */
	public List<Long> getLogicalReachIDs(int i) {
		if (reachIDsByLogicalSets == null) {
			if (logicalSets != null && i>=0 && logicalSets.size()>i) {

				// valid request but reachIDsByLogicalSets not yet populated.
				// Create an empty List for reachIDs of the correct size, filled with nulls;
				reachIDsByLogicalSets = new ArrayList<List<Long>>(logicalSets.size());
				for (int j=0; j<logicalSets.size(); j++) {
					reachIDsByLogicalSets.add(null);
				}
			}
		}
		
		// reachIDs are only fetched at the time that they are requested
		List<Long> result = reachIDsByLogicalSets.get(i);
		if (result == null) {
			result = SharedApplication.getInstance().getReachesByCriteria(logicalSets.get(i));
			reachIDsByLogicalSets.set(i, result);
		}
		return reachIDsByLogicalSets.get(i);
	}
	
	public List<Long> getCombinedReachIDs(){
		List<Long> result = new ArrayList<Long>();
		// Start with explicit reaches
		if (reaches != null) {
			for (Reach reach: reaches) {
				result.add(reach.getId());
			}
		}
		
		// add each of the logical reaches
		for (int i=0; i<logicalSets.size(); i++) {
			int m = result.size();
			List<Long> lr = getLogicalReachIDs(i);
//			int n = lr.size();
//			result.addAll(lr);
//			if (result.size() < m+n) {
//				// TODO log this
//				System.out.println("WARNING: " + (m+n-result.size()) + " overlapping reaches ");
//			}
			//  potential fix for nullpointer issue.
			if (lr != null) {
				int n = lr.size();
				result.addAll(lr);
				if (result.size() < m+n) {
					// TODO log this
					System.out.println("WARNING: " + (m+n-result.size()) + " overlapping reaches ");
				}
			}
		}
		
		return result;
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
		
		if (logicalSets != null) {
			for (LogicalSet ls: logicalSets) {
				hcb.append(ls.hashCode());
			}
		}
		
		// reachIDsByLogicalSets deliberately ignored
		
		return hcb.toHashCode();
		
	}
	
}
