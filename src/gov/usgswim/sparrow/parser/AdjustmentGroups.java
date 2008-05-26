package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;

import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableWritable;
import gov.usgswim.datatable.adjustment.ColumnCoefAdjustment;
import gov.usgswim.sparrow.PredictData;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class AdjustmentGroups implements XMLStreamParserComponent, Serializable, Cloneable {

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
	private DefaultGroup defaultGroup;
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
						id = ParserHelper.parseAttribAsInt(in, XMLStreamParserComponent.ID_ATTR, false);
						conflicts = in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "conflicts");				
					}  else if (ReachGroup.isTargetMatch(localName)) {
						ReachGroup rg = new ReachGroup();
						rg.parse(in);
						reachGroups.add(rg);
					} else if (DefaultGroup.isTargetMatch(localName)) {
						DefaultGroup dg = new DefaultGroup();
						dg.parse(in);
						defaultGroup = dg;
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
		
		myClone.defaultGroup = (DefaultGroup) defaultGroup.clone();
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
			HashCodeBuilder hash = new HashCodeBuilder(324163, 823);
			hash.append(modelID);
			hash.append(conflicts);
			hash.append(defaultGroup);
			
			if (reachGroups != null && reachGroups.size() > 0) {
				for (ReachGroup rg: reachGroups) {
					hash.append(rg.getStateHash());
				}
				
			}
			
			id = hash.toHashCode();
		} 
		
		return id;
	}
	
	/**
	 * Actually does the adjustment, returning sparse view of the underlying data.
	 * @param source
	 * @param srcIndex
	 * @param reachIndex
	 * @throws Exception
	 */
	public DataTable adjust(PredictData data) throws Exception {
		
		DataTable adjusted = null;
		
		//Do model-wide adjustments
		if (defaultGroup != null && defaultGroup.getAdjustments().size() > 0) {

			ColumnCoefAdjustment colAdj = new ColumnCoefAdjustment(data.getSrc());
			adjusted = colAdj;
			
			for (Adjustment adj: defaultGroup.getAdjustments()) {
				Double coef = adj.getCoefficient();
				Integer srcId = adj.getSource();
				
				//Logic check...
				if (coef == null || srcId == null) {
					throw new Exception("For a global adjustment, a source and coefficient must be specified");
				}
				
				colAdj.setColumnMultiplier(mapSourceId(srcId, data.getSrcMetadata()), coef);
				
			}
			
		}
		
		if (adjusted != null) {
			return adjusted;
		} else {
			return data.getSrc();
		}
		
		/*
		 
		Code copied from the old Adjustment class....
		switch (_type) {
			case GROSS_SRC_ADJUST: {
				if (source instanceof ColumnCoefAdjustment) {
					ColumnCoefAdjustment data = (ColumnCoefAdjustment)source;
//					data.setColumnMultiplier(column, multiplierValue)
					data.setColumnMultiplier(mapSourceId(_srcId, srcIndex), getValue());

				} else {
					throw new Exception("Expecting instance of RowColumnCoefAdjustment");
				}
				break;
			}
			case SPECIFIC_ADJUST: {
				if (source instanceof DataTableWritable) {
					DataTableWritable data = (DataTableWritable)source;

					int reachRow = reachIndex.findFirst(0, Integer.valueOf(_reachId));

					if (reachRow != -1) {
						data.setValue(Double.valueOf(_val), reachRow, mapSourceId(_srcId, srcIndex));
					} else {
						throw new Exception("Reach ID #" + _reachId + " not found");
					}
				} else {
					throw new Exception("Expecting instance of SparseOverrideAdjustment");
				}
				break;
			}
			default:
				throw new Exception("Unsupported Adjustment type '" + _type);
		}
		
		*/

	}
	
	/**
	 * Maps a source id to its column index in the src data.
	 * 
	 * If there is no source id map, it is assumed that there are no IDs for the sources (i.e.,
	 * the prediction is being run from a text file), and ID are auto generated
	 * such that the first column of the sources is given an id of 1 (not zero).
	 * 
	 * See the PredictionDataSet class, which implements the same strategy (and should
	 * be kept in sync).
	 * @param id
	 * @return
	 * @throws Exception
	 */
	public int mapSourceId(Integer id, DataTable srcMetadata) throws Exception {
		if (srcMetadata != null) {

			int i = srcMetadata.getRowForId(id.longValue());

			if (i > -1) {
				// Running from database, so has a sourceid table
				return i;
			} else  {
				throw new Exception ("Source for id " + id + " not found");
			}
		} else {
			// Running from text file so assume columns in order
			//In this case, id '1' is column zero.
			if (id > 0) {
				return id.intValue() - 1;
			} else {
				throw new Exception("Invalid source id " + id + ", which must be greater then zero.");
			}
		}
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
	public DefaultGroup getDefaultGroup() {
  	return defaultGroup;
  }
}
