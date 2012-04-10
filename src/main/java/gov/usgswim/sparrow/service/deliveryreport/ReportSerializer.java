package gov.usgswim.sparrow.service.deliveryreport;

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
import gov.usgswim.sparrow.service.predict.filter.PredictExportAggFilter;
import gov.usgswim.sparrow.service.predict.filter.PredictExportFilter;

import javax.xml.stream.XMLStreamException;

public class ReportSerializer extends BasicXMLStreamReader {

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/sparrow-report/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/sparrow-report/v0_1.xsd";
	public static String TARGET_MAIN_ELEMENT_NAME = "sparrow-report";
	public static String T_PREFIX = "mod";
	
	private ReportRequest request;

	private DataTable data;
	private PredictData predictData;
	private String exportDescription;
	private double[] columnTotal;	//Total value for each column
	
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
		columnTotal = new double[reportTable.getColumnCount()];

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
					//column for each individual source
					events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Total Delivered Load by Source"));
					
					writeDataTableStartHeaders(data, predictData, data.getColumnCount() - 1);
					
					addCloseTag("group");

					events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Total"));
					String totalColName = data.getName(data.getColumnCount() - 1);
					totalColName += " (" + data.getUnits(data.getColumnCount() - 1) + ") ";
					totalColName += "for all sources";
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

		if (!state.isDataFinished()) {
			if (state.r < data.getRowCount()) {
				BasicTagEvent rowEvent = new BasicTagEvent(START_ELEMENT, "r");
				
				Long rowId = data.getIdForRow(state.r);
				rowEvent.addAttribute("id", rowId.toString());
				
				events.add(rowEvent);

				for (int c = 0; c < data.getColumnCount(); c++) {
					
					Double val = data.getDouble(state.r, c);
					if (val == null) val = 0D;
					
					addBasicTag("c", val.toString());
					columnTotal[c] = columnTotal[c] + val;
				}

				addCloseTag("r");
				events.add(new BasicTagEvent(SPACE));
			} else {
				//This is the last row - add the totals
				
				BasicTagEvent rowEvent = new BasicTagEvent(START_ELEMENT, "r");
				events.add(rowEvent);

				for (int c = 0; c < data.getColumnCount(); c++) {
					addBasicTag("c", Double.toString(columnTotal[c]));
				}

				addCloseTag("r");
				events.add(new BasicTagEvent(SPACE));
			}

		}
		state.r++;
	}
	
	/**
	 * Writes the column definitions for the PredictData columns
	 * @param result
	 * @param colCount
	 * @param nameSuffix
	 */
	protected void writeDataTableStartHeaders(DataTable result, PredictData basePredictData, int colCount) {
		for (int i = 0; i < colCount; i++) {
			String name = result.getName(i);
			name += " (" + result.getUnits(i) + ") ";
			name += "for " + basePredictData.getSrcMetadata().getString(i, 2);
			events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", NUMBER));
		}
	}
	
	

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

