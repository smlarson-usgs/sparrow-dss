package gov.usgswim.sparrow.domain;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.request.ReachClientId;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.ParserHelper;

import java.util.*;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.ArrayUtils;
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
	public static final String MAIN_ELEMENT_NAME = "reachGroup";

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
	
	/**
	 * Completely specified constructor.
	 * 
	 * @param modelID
	 * @param isEnabled
	 * @param name
	 * @param description
	 * @param notes
	 * @param adjs
	 * @param reaches
	 * @param logicalSets
	 */
	public ReachGroup(long modelID, boolean isEnabled,
			String name, String description, String notes,
			List<Adjustment> adjs, List<ReachElement> reaches, 
			List<LogicalSet> logicalSets) {
		this.modelID = modelID;
		this.isEnabled = isEnabled;
		this.name = name; 
		this.description = description;
		this.notes = notes;
		this.adjs = adjs;
		this.reaches = reaches;
		this.logicalSets = logicalSets;	
	}

	protected long modelID;
	protected boolean isEnabled;
	protected String name;
	protected String description;
	protected String notes;

	protected List<Adjustment> adjs = new ArrayList<Adjustment>();
	protected List<ReachElement> reaches = new ArrayList<ReachElement>();
	protected List<LogicalSet> logicalSets;
	protected transient List<long[]> reachIDsByLogicalSets; // transient as this is fetched from cache

	// search
	protected transient Set<Long> containedReachIDs; // temporary storage as convenience for searching

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
					} else if (ReachElement.isTargetMatch(localName)) {
						
						UnadjustableReachElement ur = new UnadjustableReachElement();
						ur.parse(in);
						reaches.add(ur);
						
						
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


	@Override
	public ReachGroup clone() throws CloneNotSupportedException {
		// DONE: We are copying immutable maps during the cloning.. OK?
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
	 * WARNING: do not call this method until all the reaches have been added to
	 * the group, as it will make subsequent search behavior incorrect.
	 */
	public boolean contains(long reachID) throws Exception {
		return getOrBuildCombinedReachIds().contains(reachID);
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

	public List<ReachElement> getExplicitReaches() {
		return Collections.unmodifiableList(reaches);
	}
	
	/**
	 * Get the IDENTIFIERS for all the explicit reaches in the group.
	 * 
	 * Explicit reaches are ones added individually (i.e. include reach #99),
	 * as opposed to reaches added as a logical set (i.e. all reaches in HUC a 4).
	 * 
	 * @return
	 * @throws Exception 
	 */
	public List<Long> getExplicitReachIds() throws Exception {
		List<Long> list = new ArrayList<Long>();
		
		if (reaches != null && !reaches.isEmpty()) {
			ArrayList<ReachClientId> clientIds = new ArrayList<ReachClientId>(reaches.size());

			for (ReachElement reach : reaches) {
				ReachClientId rci = new ReachClientId(modelID, reach.getId());
				clientIds.add(rci);
			}

			list = SharedApplication.getInstance().getReachFullIdAsLong(clientIds);
			
		}
		
		return Collections.unmodifiableList(list);
	}

	/**
	 * @param i
	 * @return reachIds for the ith logicalSet
	 * TODO:  This should be based on passing in a single LogicalSet instance...
	 */
	public long[] getLogicalReachIDs(int i) {
		if (reachIDsByLogicalSets == null) {
			if (logicalSets != null && i>=0 && logicalSets.size()>i) {

				// valid request but reachIDsByLogicalSets not yet populated.
				// Create an empty List for reachIDs of the correct size, filled with nulls;
				reachIDsByLogicalSets = new ArrayList<long[]>(logicalSets.size());
				for (int j=0; j<logicalSets.size(); j++) {
					reachIDsByLogicalSets.add(null);
				}
			}
		}

		// reachIDs are only fetched at the time that they are requested
		long[] result = reachIDsByLogicalSets.get(i);
		if (result == null) {
			LogicalSet ls = logicalSets.get(i);
			
			assert(ls.getCriteria().size() < 2);
			
			if (ls.getCriteria().size() == 1) {
				result = SharedApplication.getInstance().getReachesByCriteria(ls.getCriteria().get(0));
			} else if (ls.getCriteria().size() == 0) {
				result = ArrayUtils.EMPTY_LONG_ARRAY;
			} else {
				throw new RuntimeException("Only a single criteria is expected in a logical set.");
			}
			
			reachIDsByLogicalSets.set(i, result);
		}
		return reachIDsByLogicalSets.get(i);
	}

	public Set<Long> getCombinedReachIDs() throws Exception {
	  return getOrBuildCombinedReachIds();
	}
	
	private synchronized Set<Long> getOrBuildCombinedReachIds() throws Exception {
		
		if (containedReachIDs == null) {
			// Use a Set to avoid adding a reach more than once (because of logical sets)
			Set<Long> combinedIDs = new HashSet<Long>();
			
			// Start with explicit reaches
			combinedIDs.addAll(getExplicitReachIds());

			// add each of the logical reaches
			if (logicalSets != null) {
				for (int logicalSetIndex = 0; logicalSetIndex < logicalSets.size(); logicalSetIndex++) {
					long[] logicalSetIDs = getLogicalReachIDs(logicalSetIndex);

					if (logicalSetIDs != null) {
						for (int j = 0; j < logicalSetIDs.length; j++) {
							combinedIDs.add(logicalSetIDs[j]);
						}
					}
				}
			}
			
			containedReachIDs = Collections.unmodifiableSet(combinedIDs);
		}
		
		return containedReachIDs;
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
			for (ReachElement reach : reaches) {
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
