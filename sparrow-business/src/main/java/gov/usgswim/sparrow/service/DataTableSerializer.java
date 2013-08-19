package gov.usgswim.sparrow.service;

import gov.usgswim.sparrow.service.deliveryterminalreport.*;
import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.NUMBER;
import static gov.usgs.webservices.framework.formatter.SparrowFlatteningFormatter.STRING;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgs.cida.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.datatable.TableProperties;

import java.text.DecimalFormat;

import javax.xml.stream.XMLStreamException;

public class DataTableSerializer extends BasicXMLStreamReader {

	public static String DEFAULT_TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/sparrow-data-table/v0_1";
	public static String DEFAULT_TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/sparrow-data-table/v0_1.xsd";
	public static String DEFAULT_TARGET_MAIN_ELEMENT_NAME = "sparrow-data-table";

	private DataTable data;
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
			return r >= (data.getRowCount());
		}
	};

	// ============
	// CONSTRUCTORS
	// ============	
	public DataTableSerializer(DataTable data, String exportDescription) throws Exception {
		
		super();
		
		if (data == null) {
			throw new IllegalArgumentException("The DataTable cannot be null - this is the main exported data.");
		}

		this.data = data;
		this.exportDescription = exportDescription;
		numberFormat = new DecimalFormat[data.getColumnCount()];
		
		
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
		this.setDefaultNamespace(DEFAULT_TARGET_NAMESPACE);
		addNamespace(XMLSCHEMA_NAMESPACE, XMLSCHEMA_PREFIX);

		// opening element
		events.add(new BasicTagEvent(START_DOCUMENT));
		events.add(new BasicTagEvent(START_ELEMENT, DEFAULT_TARGET_MAIN_ELEMENT_NAME).addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", DEFAULT_TARGET_NAMESPACE + " " + DEFAULT_TARGET_NAMESPACE_LOCATION));
		
		addOpenTag("response");
		{
			
			events.add(
					
					//Note:  One extra row for column totals
					new BasicTagEvent(START_ELEMENT, "metadata")
					.addAttribute("rowCount", Integer.toString(data.getRowCount()))
					.addAttribute("columnCount", Integer.toString(data.getColumnCount())));
			{
				
				//Use either the specified description or the one that comes with the data table
				String exportReadme = exportDescription;
				if (exportReadme == null) {
					exportReadme = data.getDescription();
				}
				
				if (exportReadme != null && exportReadme.length() > 0) {
					addOpenTag("description");
					events.add(new BasicTagEvent(CDATA, exportReadme));
					addCloseTag("description");
				}
				
				addOpenTag("columns");
				{
					
					//Basic Data
					events.add(new BasicTagEvent(START_ELEMENT, "group").addAttribute("name", "Main Data"));
					
					for (int c = 0; c < data.getColumnCount(); c++) {
						String name = data.getName(c);
						String type = data.getDataType(c).getSimpleName();
						events.add(makeNonNullBasicTag("col", "").addAttribute("name", name).addAttribute("type", type));
					}

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
		addCloseTag(DEFAULT_TARGET_MAIN_ELEMENT_NAME);
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
			
				//standard row
				boolean includeThisRow = true;
				
				if (includeThisRow) {

					BasicTagEvent rowEvent = new BasicTagEvent(START_ELEMENT, "r");

					Long rowId = data.getIdForRow(state.r);
					rowEvent.addAttribute("id", rowId.toString());

					events.add(rowEvent);

					for (int c = 0; c < data.getColumnCount(); c++) {

						if (data.getDataType(c) != null && Integer.class == data.getDataType(c)) {

							Integer val = data.getInt(state.r, c);
							if (val == null) {
								addBasicTag("c", null);
							} else {
								addBasicTag("c", val.toString());
							}
						} else if (data.getDataType(c) != null && Number.class.isAssignableFrom(data.getDataType(c))) {
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
	

	@Override
	public void close() throws XMLStreamException {

	}

	// ==========================
	// SIMPLE GETTERS AND SETTERS
	// ==========================
	public String getTargetNamespace() {
		return DEFAULT_TARGET_NAMESPACE;
	}
}

