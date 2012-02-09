package gov.usgswim.sparrow.service.predict;

import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.NUMBER;
import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.STRING;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.filter.RowFilter;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.SparrowColumnSpecifier;
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

	private RowFilter filter;
	private DataTable filterTable;

	private DataTable watershedAreas;
	private DataTable huc8data;
	private SparrowColumnSpecifier adjDataColumn;
	private SparrowColumnSpecifier nomDataColumn;
	private PredictResult adjPredictResult;
	private PredictResult nomPredictResult;
	private PredictData adjPredictData;
	private PredictData nomPredictData;
	private DataTable reachIdAttribs = null;
	private DataTable reachStatsTable = null;
	private String exportDescription;
	
	//
	protected ParseState state = new ParseState();

	// ===========
	// INNER CLASS
	// ===========
	protected class ParseState{
		protected int r = 0;
		public boolean isDataFinished() {
			return r >= adjDataColumn.getTable().getRowCount();
		}
	};

	// ============
	// CONSTRUCTORS
	// ============	
	public PredictExportSerializer(PredictExportRequest request,
			SparrowColumnSpecifier adjDataColumn, SparrowColumnSpecifier nomDataColumn,
			PredictData adjPredictData, PredictData nomPredictData,
			PredictResult adjPredictResult, PredictResult nomPredictResult, 
			DataTable waterShedAreasColumn, DataTable huc8data, DataTable reachIdAttribs,
			DataTable reachStatsTable, String exportDescription) throws Exception {
		
		super();
		this.request = request;
		
		this.adjDataColumn = adjDataColumn;
		this.nomDataColumn = nomDataColumn;
		this.adjPredictData = adjPredictData;
		this.nomPredictData = nomPredictData;
		this.adjPredictResult = adjPredictResult;
		this.nomPredictResult = nomPredictResult;
		this.watershedAreas = waterShedAreasColumn;
		this.huc8data = huc8data;
		this.reachIdAttribs = reachIdAttribs;
		this.reachStatsTable = reachStatsTable;
		this.exportDescription = exportDescription;
		
		
		if (adjDataColumn == null) {
			throw new IllegalArgumentException("adjDataColumn cannot be null - this is the main mapped value data.");
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
					.addAttribute("rowCount", Integer.toString(adjDataColumn.getRowCount())));
					//.addAttribute("columnCount", Integer.toString(result.getColumnCount())));	We don't really know the column count
			{
				
				if (exportDescription != null && exportDescription.length() > 0) {
					addOpenTag("description");
					events.add(new BasicTagEvent(CDATA, exportDescription));
					addCloseTag("description");
				}
				
				addOpenTag("columns");
				{
					//reach info, HUC8 and watershed area
					events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Basic Reach Info"));
					events.add(makeNonNullBasicTag("col", "").addAttribute("name", "Watershed Area (" + watershedAreas.getUnits(1) + ")").addAttribute("type", NUMBER));
					events.add(makeNonNullBasicTag("col", "").addAttribute("name", "HUC8").addAttribute("type", STRING));
					addCloseTag("group");
					
					//Add a group for the mapped value
					if (adjDataColumn != null || nomDataColumn != null) {

						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Mapped Value"));

						if (adjDataColumn != null) {
							String name = "Mapped Value: " + adjDataColumn.getTable().getName(adjDataColumn.getColumn());
							//name += " (" + adjDataColumn.getTable().getProperty(adjDataColumn.getColumn(), "constituent") + ")";
							name += " (" + adjDataColumn.getTable().getUnits(adjDataColumn.getColumn()) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
						}
						
						if (nomDataColumn != null) {
							String name = "Original Mapped Value: " + nomDataColumn.getTable().getName(nomDataColumn.getColumn());
							//name += " (" + nomDataColumn.getTable().getProperty(nomDataColumn.getColumn(), "constituent") + ")";
							name += " (" + nomDataColumn.getTable().getUnits(nomDataColumn.getColumn()) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
						}

						addCloseTag("group");
					}

					//Add a group for the adjusted source columns
					if (adjPredictData != null) {
						
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Adjusted Source Values"));
						for (int i = 0; i < adjPredictData.getSrc().getColumnCount(); i++) {
							String name = "Adj Source: " + adjPredictData.getSrc().getName(i);
							name += " (" + adjPredictData.getSrc().getProperty(i, "constituent") + ")";
							name += " (" + adjPredictData.getSrc().getUnits(i) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
						}
						addCloseTag("group");
					}
					
					//Add a group for the adjusted predict columns
					if (adjPredictResult != null) {
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Adjusted Predicted Values"));
						
						int srcCount = adjPredictResult.getSourceCount();
						String nameSuffix = " (Adjusted)";
						
						writePredictDataStartHeaders(adjPredictResult, adjPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1, nameSuffix);
						writePredictDataStartHeaders(adjPredictResult, adjPredictResult.getFirstTotalColForSrc(), srcCount + 1, nameSuffix);
						
						addCloseTag("group");
					}

					//Add a group for the NONadjusted source columns
					if (nomPredictData != null) {

						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Original Source Values"));
						for (int i = 0; i < nomPredictData.getSrc().getColumnCount(); i++) {
							String name = "Original Source: " + nomPredictData.getSrc().getName(i);
							name += " (" + nomPredictData.getSrc().getProperty(i, "constituent") + ")";
							name += " (" + nomPredictData.getSrc().getUnits(i) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
						}
						addCloseTag("group");
					}

					//Add a group for the NONadjusted predict result columns
					if (nomPredictResult != null) {
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Original Predicted Values"));
						
						int srcCount = nomPredictResult.getSourceCount();
						String nameSuffix = " (Original)";
						
						writePredictDataStartHeaders(nomPredictResult, nomPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1, nameSuffix);
						writePredictDataStartHeaders(nomPredictResult, nomPredictResult.getFirstTotalColForSrc(), srcCount + 1, nameSuffix);

						addCloseTag("group");
					}
					
					//Add a group for identification columns
					if (reachIdAttribs != null) {
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Reach Identification Attributes"));
						
						for (int i = 0; i < reachIdAttribs.getColumnCount(); i++) {
							String name = reachIdAttribs.getName(i);
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", STRING));
						}
						addCloseTag("group");
					}
					
					if (reachStatsTable != null) {
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Reach Statistics"));
						
						for (int i = 0; i < reachStatsTable.getColumnCount(); i++) {
							String name = reachStatsTable.getName(i);
							name = name + " (" + reachStatsTable.getUnits(i) + ")";
							events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
						}
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
			Long rowId = adjDataColumn.getIdForRow(state.r);
			rowEvent.addAttribute("id", rowId.toString());
			
			events.add(rowEvent);
			{
				//Column 1 by def.  See LoadUnitArea class
				addBasicTag("c", watershedAreas.getValue(state.r, 1).toString());
				
				//Column 0 by def.  See LoadHUCTable
				addBasicTag("c", huc8data.getValue(state.r, 0).toString());
				
				if (adjDataColumn != null) {
					addBasicTag("c", adjDataColumn.getTable().getValue(state.r, adjDataColumn.getColumn()).toString());
				}
				
				if (nomDataColumn != null) {
					addBasicTag("c", nomDataColumn.getTable().getValue(state.r, nomDataColumn.getColumn()).toString());
				}

				if (adjPredictData != null) {
					for (int c = 0; c < adjPredictData.getSrc().getColumnCount(); c++) {
						addBasicTag("c", adjPredictData.getSrc().getString(state.r, c));
					}
				}

				if (adjPredictResult != null) {
					int srcCount = adjPredictResult.getSourceCount();
					
					writePredictData(adjPredictResult, adjPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1);
					writePredictData(adjPredictResult, adjPredictResult.getFirstTotalColForSrc(), srcCount + 1);
				}

				
				if (nomPredictData != null) {
					for (int c = 0; c < nomPredictData.getSrc().getColumnCount(); c++) {
						addBasicTag("c", nomPredictData.getSrc().getString(state.r, c));
					}
				}

				if (nomPredictResult != null) {
					int srcCount = nomPredictResult.getSourceCount();
					
					writePredictData(nomPredictResult, nomPredictResult.getFirstDecayedIncrementalColForSrc(), srcCount + 1);
					writePredictData(nomPredictResult, nomPredictResult.getFirstTotalColForSrc(), srcCount + 1);
				}
				
				if (reachIdAttribs != null) {
					for (int c = 0; c < reachIdAttribs.getColumnCount(); c++) {
						addBasicTag("c", reachIdAttribs.getString(state.r, c));
					}
				}
				
				if (reachStatsTable != null) {
					for (int c = 0; c < reachStatsTable.getColumnCount(); c++) {
						addBasicTag("c", reachStatsTable.getString(state.r, c));
					}
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
			events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
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
			addBasicTag("c", result.getString(state.r, c));
		}
	}
	
	

	@Override
	public void close() throws XMLStreamException {

	}
    
    protected RowFilter createRowFilter() throws Exception {
        Integer contextId = request.getContextID();
        PredictionContext context = SharedApplication.getInstance().getPredictionContext(contextId);
        if (request.getBbox() == null) {
            this.filterTable = null;
            return new RowFilter() {
                public boolean accept(DataTable table, int rowNum) {
                    return true;
                }
            };
        } else if (context.getAnalysis().isAggregated()) {
            this.filterTable = adjDataColumn.getTable();
            return new PredictExportAggFilter(context, request.getBbox());
        } else {
            this.filterTable = adjDataColumn.getTable();
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

