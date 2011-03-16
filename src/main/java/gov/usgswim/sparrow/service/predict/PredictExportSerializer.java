package gov.usgswim.sparrow.service.predict;

import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.RowFilter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.DataColumn;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predict.filter.PredictExportAggFilter;
import gov.usgswim.sparrow.service.predict.filter.PredictExportFilter;

import javax.xml.stream.XMLStreamException;

public class PredictExportSerializer extends BasicXMLStreamReader {

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/prediction-response/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/prediction-response/v0_1.xsd";
	public static String T_PREFIX = "mod";
	private PredictExportRequest request;
	//private DataTable result;
	//private PredictData predictData;
	private RowFilter filter;
	private DataTable filterTable;
	
//	private PredictionContext predictContext;
//	private PredictResult adjPredictionResult;
//	private DataColumn dataColumn;
	
	
	private DataColumn adjDataColumn;
	private DataColumn nomDataColumn;
	private PredictResult adjPredictResult;
	private PredictResult nomPredictResult;
	private PredictData adjPredictData;
	private PredictData nomPredictData;
	
	/** Non-null PredictData instance used to find row IDs */
	private PredictData refPredictData;
	
	/** Non-null DataColumn instance used for cardinality IDs */
	private DataColumn refDataColumn;
	
	//
	protected ParseState state = new ParseState();

	// ===========
	// INNER CLASS
	// ===========
	protected class ParseState{
		protected int r = 0;
		public boolean isDataFinished() {
			return r >= refDataColumn.getTable().getRowCount();
		}
	};

	// ============
	// CONSTRUCTORS
	// ============	
	public PredictExportSerializer(PredictExportRequest request,
			DataColumn adjDataColumn, DataColumn nomDataColumn,
			PredictData adjPredictData, PredictData nomPredictData,
			PredictResult adjPredictResult, PredictResult nomPredictResult) throws Exception {
		
		super();
		this.request = request;
		
		this.adjDataColumn = adjDataColumn;
		this.nomDataColumn = nomDataColumn;
		this.adjPredictData = adjPredictData;
		this.nomPredictData = nomPredictData;
		this.adjPredictResult = adjPredictResult;
		this.nomPredictResult = nomPredictResult;
		
		
		if (adjPredictData == null && nomPredictData == null) {
			throw new IllegalArgumentException("adjPredictData and nomPredictData cannot both be null.");
		} else if (adjPredictData != null) {
			refPredictData = adjPredictData;
		} else {
			refPredictData = nomPredictData;
		}
		
		if (adjDataColumn == null && nomDataColumn == null) {
			throw new IllegalArgumentException("adjDataColumn and nomDataColumn cannot both be null.");
		} else if (adjDataColumn != null) {
			refDataColumn = adjDataColumn;
		} else {
			refDataColumn = nomDataColumn;
		}
		
		this.filter = createRowFilter();

	}

	// ================
	// INSTANCE METHODS (for pull parsing)
	// ================
	/* Override because there's no resultset
	 * @see gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader#readNext()
	 */
	@Override
	public void readNext() throws XMLStreamException {
		try {
			if (!isStarted) {
				documentStartAction();
			}
			readRow();
			if (state.isDataFinished()) {
				if (isStarted && !isEnded) {
					// Only output footer if the document was actually started
					// and the footer has not been output.
					documentEndAction();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new XMLStreamException(e);
		}
	}

	@Override
	protected BasicTagEvent documentStartAction() {
		super.documentStartAction();
		// add the namespaces
		this.setDefaultNamespace(TARGET_NAMESPACE);
		addNamespace(XMLSCHEMA_NAMESPACE, XMLSCHEMA_PREFIX);

		// opening element
		events.add(new BasicTagEvent(START_DOCUMENT));
		events.add(new BasicTagEvent(START_ELEMENT, "sparrow-prediction-response").addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE + " " + TARGET_NAMESPACE_LOCATION));

		addOpenTag("response");
		{
			
			events.add(
					new BasicTagEvent(START_ELEMENT, "metadata")
					.addAttribute("rowCount", Integer.toString(refDataColumn.getTable().getRowCount())));
					//.addAttribute("columnCount", Integer.toString(result.getColumnCount())));	We don't really know the column count
			{
				addOpenTag("columns");
				{
					
					//Add a group for the mapped value
					if (adjDataColumn != null || nomDataColumn != null) {

						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Mapped Value"));

						if (adjDataColumn != null) {
							String name = "Adjusted Mapped Value: " + adjDataColumn.getTable().getName(adjDataColumn.getColumn());
							//name += " (" + adjDataColumn.getTable().getProperty(adjDataColumn.getColumn(), "constituent") + ")";
							name += " (" + adjDataColumn.getTable().getUnits(adjDataColumn.getColumn()) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", "Number"));
						}
						
						if (adjDataColumn != null) {
							String name = "Nominal Mapped Value: " + nomDataColumn.getTable().getName(nomDataColumn.getColumn());
							//name += " (" + nomDataColumn.getTable().getProperty(nomDataColumn.getColumn(), "constituent") + ")";
							name += " (" + nomDataColumn.getTable().getUnits(nomDataColumn.getColumn()) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", "Number"));
						}

						addCloseTag("group");
					}

					//Add a group for the adjusted source columns
					if (request.isIncludeSource() && adjPredictData != null) {
						
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Adjusted Source Values"));
						for (int i = 0; i < adjPredictData.getSrc().getColumnCount(); i++) {
							String name = "Adj Source: " + adjPredictData.getSrc().getName(i);
							name += " (" + adjPredictData.getSrc().getProperty(i, "constituent") + ")";
							name += " (" + adjPredictData.getSrc().getUnits(i) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", "Number"));
						}
						addCloseTag("group");
					}
					
					//Add a group for the adjusted predict columns
					if (request.isIncludePredict() && adjPredictResult != null) {
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Adjusted Predicted Values"));
						
						int srcCount = adjPredictResult.getSourceCount();
						String nameSuffix = " (Adjusted)";
						
						writePredictDataStartHeaders(adjPredictResult, adjPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1, nameSuffix);
						writePredictDataStartHeaders(adjPredictResult, adjPredictResult.getFirstTotalColForSrc(), srcCount + 1, nameSuffix);
						
						addCloseTag("group");
					}
					
					//Add a group for the NONadjusted source columns
					if (request.isIncludeSource() && nomPredictData != null) {

						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Nominal Source Values"));
						for (int i = 0; i < nomPredictData.getSrc().getColumnCount(); i++) {
							String name = "Nom. Source: " + nomPredictData.getSrc().getName(i);
							name += " (" + nomPredictData.getSrc().getProperty(i, "constituent") + ")";
							name += " (" + nomPredictData.getSrc().getUnits(i) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", "Number"));
						}
						addCloseTag("group");
					}

					//Add a group for the NONadjusted predict result columns
					if (request.isIncludePredict() && nomPredictResult != null) {
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Nominal Predicted Values"));
						
						int srcCount = nomPredictResult.getSourceCount();
						String nameSuffix = " (Unadjusted)";
						
						writePredictDataStartHeaders(nomPredictResult, nomPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1, nameSuffix);
						writePredictDataStartHeaders(nomPredictResult, nomPredictResult.getFirstTotalColForSrc(), srcCount + 1, nameSuffix);

						addCloseTag("group");
					}
					
					addCloseTag("columns");
				}
				addCloseTag("metadata");
				addOpenTag("data");
			}
		}
		return null;
	}

	@Override
	protected void documentEndAction() {
		super.documentEndAction();
		addCloseTag("data");
		addCloseTag("response");
		addCloseTag("sparrow-prediction-response");
		events.add(new BasicTagEvent(END_DOCUMENT));
	}

	protected void readRow() {
	    while (!state.isDataFinished() && !filter.accept(filterTable, state.r)) {
	        state.r++;
	    }
		if (!state.isDataFinished()) {
			BasicTagEvent rowEvent = new BasicTagEvent(START_ELEMENT, "r");
			
			//Aggregated rows are not working right now...
			Long rowId = refPredictData.getIdForRow(state.r);
			rowEvent.addAttribute("id", rowId.toString());

			events.add(rowEvent);
			{
				
				if (adjDataColumn != null) {
					addNonNullBasicTag("c", adjDataColumn.getTable().getValue(state.r, adjDataColumn.getColumn()).toString());
				}
				
				if (nomDataColumn != null) {
					addNonNullBasicTag("c", nomDataColumn.getTable().getValue(state.r, nomDataColumn.getColumn()).toString());
				}

				if (request.isIncludeSource() && adjPredictData != null) {
					for (int c = 0; c < adjPredictData.getSrc().getColumnCount(); c++) {
						addNonNullBasicTag("c", adjPredictData.getSrc().getString(state.r, c));
					}
				}

				if (request.isIncludePredict() && adjPredictResult != null) {
					int srcCount = adjPredictResult.getSourceCount();
					
					writePredictData(adjPredictResult, adjPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1);
					writePredictData(adjPredictResult, adjPredictResult.getFirstTotalColForSrc(), srcCount + 1);
				}
				
				if (request.isIncludeSource() && nomPredictData != null) {
					for (int c = 0; c < nomPredictData.getSrc().getColumnCount(); c++) {
						addNonNullBasicTag("c", nomPredictData.getSrc().getString(state.r, c));
					}
				}

				if (request.isIncludePredict() && nomPredictResult != null) {
					int srcCount = nomPredictResult.getSourceCount();
					
					writePredictData(nomPredictResult, nomPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1);
					writePredictData(nomPredictResult, nomPredictResult.getFirstTotalColForSrc(), srcCount + 1);
				}

			}
			addCloseTag("r");
			events.add(new BasicTagEvent(SPACE));

		}
		state.r++;
	}
	
	/**
	 * Writes the column definitions for the PredictResult columns
	 * @param result
	 * @param startCol
	 * @param colCount
	 * @param nameSuffix
	 */
	protected void writePredictDataStartHeaders(PredictResult result, int startCol, int colCount, String nameSuffix) {
		for (int i = startCol; i < (startCol + colCount); i++) {
			String name = result.getName(i) + nameSuffix;
			name += " (" + result.getUnits(i) + ")";
			events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", "Number"));
		}
	}
	
	/**
	 * Writes the column data for the PredictResult columns
	 * @param result
	 * @param startCol
	 * @param colCount
	 * @param nameSuffix
	 */
	protected void writePredictData(PredictResult result, int startCol, int colCount) {
		for (int c = startCol; c < (startCol + colCount); c++) {
			addNonNullBasicTag("c", result.getString(state.r, c));
		}
	}
	
	

	@Override
	public void close() throws XMLStreamException {

	}
    
    protected RowFilter createRowFilter() throws Exception {
        Integer contextId = request.getContextID();
        PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);
        if (request.getBbox() == null) {
            this.filterTable = refPredictData.getTopo();
            return new RowFilter() {
                public boolean accept(DataTable table, int rowNum) {
                    return true;
                }
            };
        } else if (context.getAnalysis().isAggregated()) {
            this.filterTable = refDataColumn.getTable();
            return new PredictExportAggFilter(context, request.getBbox());
        } else {
            this.filterTable = refPredictData.getTopo();
            return new PredictExportFilter(context.getModelID(), request.getBbox());
        }
    }

	// ==========================
	// SIMPLE GETTERS AND SETTERS
	// ==========================
	public String getTargetNamespace() {
		return TARGET_NAMESPACE;
	}
}

