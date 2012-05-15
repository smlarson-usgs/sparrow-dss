package gov.usgswim.sparrow.service.deliveryterminalreport;

import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.NUMBER;
import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.STRING;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.TableProperties;

import java.text.DecimalFormat;

import javax.xml.stream.XMLStreamException;

public class ReportSerializer extends BasicXMLStreamReader {

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/sparrow-report/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/sparrow-report/v0_1.xsd";
	public static String TARGET_MAIN_ELEMENT_NAME = "sparrow-report";
	public static String T_PREFIX = "mod";
	
	//Hardcoded columns in the source data
	public static final int REACH_NAME_COL = 0;	//index of the reach name in the reportTable
	public static final int EDA_CODE_COL = 1;	//index of the EDA code in the reportTable
	public static final int FIRST_SOURCE_COL = 2;	//index of the first column containing a source value in the reportTable
	
	private ReportRequest request;

	private DataTable data;
	private PredictData predictData;
	private int sourceCount;	//The number of sources in the reportTable
	private int totalCol;	//index of the total column in the reportTable
	private String exportDescription;
	private Double[] columnTotal;	//Total value for each column
	private DecimalFormat[] numberFormat;
	
	
	//
	protected ParseState state = new ParseState();

	// ===========
	// INNER CLASS
	// ===========
	protected class ParseState{
		protected int r = 0;
		public boolean isDataFinished() {
			return r >= (data.getRowCount() + 1);	//One extra row for column totals
		}
	};

	// ============
	// CONSTRUCTORS
	// ============	
	public ReportSerializer(ReportRequest request, DataTable reportTable,
			PredictData predictData, String exportDescription) throws Exception {
		
		super();
		
		if (reportTable == null) {
			throw new IllegalArgumentException("The reportTable cannot be null - this is the main exported data.");
		}
		
		
		this.request = request;
		this.data = reportTable;
		this.exportDescription = exportDescription;
		this.predictData = predictData;
		sourceCount = predictData.getSrcMetadata().getRowCount();
		totalCol = reportTable.getColumnCount() - 1;
		columnTotal = new Double[reportTable.getColumnCount()];
		numberFormat = new DecimalFormat[reportTable.getColumnCount()];
		
		//Init columnTotal to have zero for actual source or total columns
		//Other columns will have null
		for (int i=FIRST_SOURCE_COL; i<=totalCol; i++) {
			columnTotal[i] = 0d;
		}
		
		//Create number formats for each number column
		for (int c = 0; c < data.getColumnCount(); c++) {
			
			if (data.getDataType(c) != null && Number.class.isAssignableFrom(data.getDataType(c))) {
				
				String formatStr = "##0";
				int precision = 2;
				String precisionStr = data.getProperty(c, TableProperties.PRECISION.toString());
				if (precisionStr != null) {
					try {
						precision = Integer.parseInt(precisionStr);
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

		// opening element
		events.add(new BasicTagEvent(START_DOCUMENT));
		events.add(new BasicTagEvent(START_ELEMENT, TARGET_MAIN_ELEMENT_NAME).addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE + " " + TARGET_NAMESPACE_LOCATION));
		
		addOpenTag("response");
		{
			
			events.add(
					
					//Note:  One extra row for column totals
					new BasicTagEvent(START_ELEMENT, "metadata")
					.addAttribute("rowCount", Integer.toString(data.getRowCount() + 1))
					.addAttribute("columnCount", Integer.toString(data.getColumnCount())));
			{
				
				if (exportDescription != null && exportDescription.length() > 0) {
					addOpenTag("description");
					events.add(new BasicTagEvent(CDATA, exportDescription));
					addCloseTag("description");
				}
				
				addOpenTag("columns");
				{
					
					//Reach name and EDA code
					events.add(new BasicTagEvent(START_ELEMENT, "group"));
					String name = data.getName(REACH_NAME_COL);
					events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", STRING));
					name = data.getName(EDA_CODE_COL);
					events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", STRING));
					addCloseTag("group");
					
					
					//column for each individual source
					events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Total Delivered Load by Source"));
					writeSourceColumnHeadersHeaders(predictData);
					addCloseTag("group");

					events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Total"));
					String totalColName = "Total for all Sources";
					events.add(makeNonNullBasicTag("col", "").addAttribute("name", totalColName).addAttribute("type", NUMBER));
					addCloseTag("group");
					
					
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
	
	protected boolean readPossiblyEmptyRow() {

		boolean isAddingEvents = false;	//returned as true if we added an event

		if (!state.isDataFinished()) {
			if (state.r < data.getRowCount()) {
				//standard row
				
				if ((request.isIncludeZeroTotalRows()) || data.getDouble(state.r, totalCol) != 0 ) {

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

							if (columnTotal[c] != null) {
								columnTotal[c] = columnTotal[c] + val;
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
			} else {
				//This is the last row - add the totals
				
				BasicTagEvent rowEvent = new BasicTagEvent(START_ELEMENT, "r");
				rowEvent.addAttribute("type", "total");
				events.add(rowEvent);

				for (int c = 0; c < data.getColumnCount(); c++) {
					
					if (columnTotal[c] == null) {
						addBasicTag("c", null);
					} else if (numberFormat[c] != null) {
						addBasicTag("c", numberFormat[c].format(columnTotal[c]));
					} else {
						addBasicTag("c", columnTotal[c].toString());
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
	
	/**
	 * Writes the column definitions for the PredictData columns
	 * @param result
	 * @param colCount
	 * @param nameSuffix
	 */
	protected void writeSourceColumnHeadersHeaders(PredictData basePredictData) {
		
		for (int i = 0; i < sourceCount; i++) {
			//source columns just use the name of the source
			String name = basePredictData.getSrcMetadata().getString(i, 2);
			events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
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

