package gov.usgswim.sparrow.service.deliveryaggreport;

import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.NUMBER;
import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.STRING;
import gov.usgswim.sparrow.service.deliveryterminalreport.ReportRequest;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.datatable.DataTableSet;
import gov.usgswim.datatable.RelationType;
import gov.usgswim.datatable.impl.DataTableSetCoord;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.TableProperties;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.domain.SparrowModel;
import gov.usgswim.sparrow.service.SharedApplication;

import java.text.DecimalFormat;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

public class ReportSerializer extends BasicXMLStreamReader {

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/sparrow-report/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/sparrow-report/v0_1.xsd";
	public static String TARGET_MAIN_ELEMENT_NAME = "sparrow-report";
	public static String T_PREFIX = "mod";
	
	//Hardcoded columns in the source data
	public static final int ENTITY_NAME_COL = 0;	//index of the reach name in the reportTable
	public static final int ENTITY_USER_CODE_COL = 1;	//index of the EDA code in the reportTable
	
	public static final int FIRST_SOURCE_COL = 3;	//index of the first column containing a source value in the reportTable
	
	private ReportRequest request;

	private DataTableSet data;
	private SparrowModel model;
	private int columnToDetermineIfARowIsEmpty;	//index of the total column in the reportTable
	private String exportDescription;
	private DecimalFormat[] numberFormat;
	
	
	//
	protected ParseState state = new ParseState();

	// ===========
	// INNER CLASS
	// ===========
	protected class ParseState{
		protected int r = 0;
		public boolean isDataFinished() {
			return r >= data.getRowCount();
		}
	};

	// ============
	// CONSTRUCTORS
	// ============	
	
	/**
	 * If the request species that zero value rows should not be included, the
	 * column specified by the columnToDetermineIfARowIsEmpty is checked to see if
	 * the value is non-zero (the row field of the Coord is ignored).
	 * 
	 * @param request
	 * @param reportTableSet
	 * @param exportDescription
	 * @param columnToDetermineIfARowIsEmpty The numberic column which, if zero, should make the row be considered empty.
	 * @throws Exception 
	 */
	public ReportSerializer(ReportRequest request, DataTableSet reportTableSet,
			String exportDescription, DataTableSetCoord columnCoordToDetermineIfARowIsEmpty) throws Exception {
		
		super();
		
		if (reportTableSet == null) {
			throw new IllegalArgumentException("The reportTable cannot be null - this is the main exported data.");
		}
		
		
		this.request = request;
		this.data = reportTableSet;
		this.exportDescription = exportDescription;
		this.columnToDetermineIfARowIsEmpty = columnCoordToDetermineIfARowIsEmpty.col;
//		this.sourceCount = reportTableSet.getColumnCount() - 4;  //4 non-source columns
//		totalCol = reportTableSet.getColumnCount() - 2;
//		relPercentCol = reportTableSet.getColumnCount() - 1;
		numberFormat = new DecimalFormat[reportTableSet.getColumnCount()];
		
		PredictionContext pc = request.getContext();
		
		if (pc == null) {
			pc = SharedApplication.getInstance().getPredictionContext(request.getContextID());
		}
		
		PredictData pd = SharedApplication.getInstance().getPredictData(pc.getModelID());
		model = pd.getModel();
		
		
		//Create number formats for each number column
		for (int c = 0; c < data.getColumnCount(); c++) {
			
			if (data.getDataType(c) != null && Number.class.isAssignableFrom(data.getDataType(c))) {
				
				String formatStr = "##0";
				int precision = 0;
				String precisionStr = data.getProperty(c, TableProperties.PRECISION.toString());
				if (precisionStr != null) {
					try {
						precision = Integer.parseInt(precisionStr);
						precision-=2;	//Decrease the precision a bit for the report
					} catch (Exception e) {
						//ignore
					}
				}
				
				if (precision > 0) formatStr += ".";
				for (int i=0; i<precision; i++) {
					formatStr += "0";
				}
				
				
				numberFormat[c] = new DecimalFormat(formatStr);
			}
		}
		
		

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

		if (this.request.isIncludeIdScript()) {
			events.add(
				new BasicTagEvent(PROCESSING_INSTRUCTION, "report-format")
				.addAttribute("includeIdScript", "true"));
		}
				
		// opening element
		events.add(new BasicTagEvent(START_DOCUMENT));
		events.add(new BasicTagEvent(START_ELEMENT, TARGET_MAIN_ELEMENT_NAME).addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE + " " + TARGET_NAMESPACE_LOCATION));
		
		addOpenTag("response");
		{
			
			events.add(
					
					new BasicTagEvent(START_ELEMENT, "metadata")
					.addAttribute("rowCount", Integer.toString(data.getRowCount()))
					.addAttribute("columnCount", Integer.toString(data.getColumnCount())));
			{
				
				if (exportDescription != null && exportDescription.length() > 0) {
					addOpenTag("description");
					events.add(new BasicTagEvent(CDATA, exportDescription));
					addCloseTag("description");
				}
				
				addOpenTag("columns");
				{
					
					
					for (int t = 0; t < (data.getTableCount()); t++) {
						//Loop through all tables except the last one
						events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", data.getTableName(t)).addAttribute("count", new Integer(data.getTableColumnCount(t)).toString()));
						writeColumnHeaders(data.getTable(t), 0, data.getTableColumnCount(t));
						addCloseTag("group");
					}
					
				}
				addCloseTag("columns");
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
		boolean aRowOfEventsHaveBeenAdded = false;
		while (!aRowOfEventsHaveBeenAdded && ! this.state.isDataFinished()) {
			aRowOfEventsHaveBeenAdded = readPossiblyEmptyRow();
		}
	}
	
	/**
	 * version of readRow that returns true if it actually added events to the
	 * event queue.  Since some rows are filtered out, this is needed so that we
	 * don't allow a call to read the next row to put no events in queue.
	 * @return 
	 */
	protected boolean readPossiblyEmptyRow() {

		boolean isAddingEvents = false;	//returned as true if we added an event
		
		if (!state.isDataFinished()) {
			
			if ((request.isIncludeZeroTotalRows()) || data.getDouble(state.r, columnToDetermineIfARowIsEmpty) != 0 ) {

								
				BasicTagEvent rowEvent = new BasicTagEvent(START_ELEMENT, "r");

				Long rowId = data.getIdForRow(state.r);
				rowEvent.addAttribute("id", rowId.toString());

				events.add(rowEvent);

				for (int c = 0; c < data.getColumnCount(); c++) {

					if (data.getDataType(c) != null && Number.class.isAssignableFrom(data.getDataType(c))) {

						Double val = data.getDouble(state.r, c);
						if (val == null) {
							addBasicTag("c", null);
						} else if (numberFormat[c] != null) {
							addBasicTag("c", numberFormat[c].format(val));
						} else {
							addBasicTag("c", val.toString());
						}


					} else {

						String val = data.getString(state.r, c);
						if (val == null) val = "";

						addBasicTag("c", val);

					}


				}

				addCloseTag("r");
				events.add(new BasicTagEvent(SPACE));

				isAddingEvents = true;

			}
						
		}
		
		state.r++;
		return isAddingEvents;
	}
	
	protected void writeColumnHeaders(DataTable dataTable, int firstCol, int upToButNotIncludedColumn) {
		
		for (int i = firstCol; i < upToButNotIncludedColumn; i++) {
			//source columns just use the name of the source
			String name = dataTable.getName(i);
			String type = dataTable.getDataType(i).getSimpleName();
			String units = dataTable.getUnits(i);
			
			Set<String> props = dataTable.getPropertyNames(i);
			
			BasicTagEvent event = makeNonNullBasicTag("col", "");
			event.addAttribute("name", name);
			event.addAttribute("type", type);
			event.addAttribute("unit", units);
			
			if (props.size() > 0) {
				for (String s : props) {
					event.addAttribute(s, dataTable.getProperty(i, s));
				}
				
			}
			
			events.add(event);
		}
	}
//	
//	protected boolean isSourceCol(int columnIndex) {
//		return (columnIndex >= FIRST_SOURCE_COL && columnIndex < totalCol);
//	}
	
	

	@Override
	public void close() throws XMLStreamException {

	}

	// ==========================
	// SIMPLE GETTERS AND SETTERS
	// ==========================
	public String getTargetNamespace() {
		return TARGET_NAMESPACE;
	}
}

