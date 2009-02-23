package gov.usgswim.sparrow.parser;

import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.aggregator.AggregationRunner;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.builder.HashCodeBuilder;

public class PredictionContext implements XMLStreamParserComponent {

	private static final long serialVersionUID = -5343918321449313545L;
	public static final String MAIN_ELEMENT_NAME = "prediction-context";
//	public static final String ADJUSTMENT_GROUPS = "adjustment-groups";

	
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
	
	/**
	 * Constructs an empty instance.
	 */
	public PredictionContext() {
	
	}
	/**
	 * Constructs a fully configured instance.
	 * 
	 * @param modelID
	 * @param ag
	 * @param anal
	 * @param tr
	 * @param aoi
	 * @return
	 */
	public PredictionContext(Long modelID, AdjustmentGroups ag, Analysis anal,
				TerminalReaches tr, AreaOfInterest aoi) {
		
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
	
	private transient AdjustmentGroups adjustmentGroups;
	private transient Analysis analysis;
	private transient TerminalReaches terminalReaches;
	private transient AreaOfInterest areaOfInterest;
	

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
					} else if (Analysis.isTargetMatch(localName)) {
						this.analysis = Analysis.parseStream(in);
						analysisID = (analysis == null)? null: analysis.getId();
					} else if (AreaOfInterest.isTargetMatch(localName)) {
						this.areaOfInterest = AreaOfInterest.parseStream(in, modelID);
						areaOfInterestID = (areaOfInterest == null)? null: areaOfInterest.getId();
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
        int dataColIndex = -1;	//The index of the data column
        DataTable dataTable = null;		//The table containing the data column

        Select select = getAnalysis().getSelect();
        DataSeriesType type = select.getDataSeries();
        // Handled DataSeriesType: total, incremental, incremental_yield, total_concentration, source_values
        
        if (type.isResultBased()) {

            //We will try to get result-based series out of the analysis cache
            PredictResult result = SharedApplication.getInstance().getAnalysisResult(this);

            switch (type) {
                case total:
                    if (select.getSource() != null) {
                        dataColIndex = result.getTotalColForSrc(select.getSource().longValue());
                    } else {
                        dataColIndex = result.getTotalCol();
                    }
                    break;
                    
                case incremental:
                    if (select.getSource() != null) {
                        dataColIndex = result.getIncrementalColForSrc(select.getSource().longValue());
                    } else {
                        dataColIndex = result.getIncrementalCol();
                    }
                    break;
                    
                case incremental_yield:
                    if (select.getSource() != null) {
                        dataColIndex = result.getIncrementalColForSrc(select.getSource().longValue());
                    } else {
                        dataColIndex = result.getIncrementalCol();
                    }
                    break;
                    
                case total_concentration:
                    if (select.getSource() != null) {
                        dataColIndex = result.getTotalColForSrc(select.getSource().longValue());
                    } else {
                        dataColIndex = result.getTotalCol();
                    }
                    break;
                    
                default:
                    throw new Exception("No data-series was specified in the analysis section");
            }

            dataTable = result;

        } else {

            //Get the predict data, which is what this series is based on
            PredictData nomPredictData = SharedApplication.getInstance().getPredictData(this.getModelID());

            switch (type) {
                case source_value:
                    if (select.getSource() != null) {

                        dataColIndex = nomPredictData.getSourceIndexForSourceID(select.getSource());

                        DataTable adjSrc = SharedApplication.getInstance().getAdjustedSource(this.getAdjustmentGroups());

                        // Check for aggregation and run if necessary
                        if (getAnalysis().getGroupBy() != null && !"".equals(getAnalysis().getGroupBy())) {
                            AggregationRunner aggRunner = new AggregationRunner(this);
                            adjSrc = aggRunner.doAggregation(adjSrc);
                        }

                        if (select.getNominalComparison().isNone()) {

                            dataTable = adjSrc;

                        } else {
                            DataTable nomSrcData = nomPredictData.getSrc();

                            // Check for aggregation and run if necessary
                            if (getAnalysis().getGroupBy() != null && !"".equals(getAnalysis().getGroupBy())) {
                                AggregationRunner aggRunner = new AggregationRunner(this);
                                nomSrcData = aggRunner.doAggregation(nomSrcData);
                            }

                            //working w/ either a percent or absolute comparison
                            dataTable = new DataTableCompare(nomSrcData, adjSrc,
                                    select.getNominalComparison().equals(ComparisonType.absolute));
                        }
                    } else {
                        throw new Exception("The data series 'source_value' requires a source ID to be specified.");
                    }
                    break;
                default:
                    throw new Exception("No data-series was specified in the analysis section");
            }
        }

        return new DataColumn(dataTable, dataColIndex);
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

		myClone.adjustmentGroups = (adjustmentGroups == null)? null: adjustmentGroups.clone();
		myClone.analysis = (analysis == null)? null: analysis.clone();
		myClone.terminalReaches = (terminalReaches == null)? null: terminalReaches.clone();
		myClone.areaOfInterest = (areaOfInterest == null)? null: areaOfInterest.clone();

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
	
	/**
	 * An inner class to bundle a DataTable and a column index together so that
	 * it is possible to return these two together for methods returning the
	 * data column.
	 * 
	 * @author eeverman
	 *
	 */
	public class DataColumn {
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
