package gov.usgswim.sparrow.service.predict;

import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_NAMESPACE;
import static gov.usgswim.sparrow.service.AbstractSerializer.XMLSCHEMA_PREFIX;
import gov.usgs.webservices.framework.dataaccess.BasicTagEvent;
import gov.usgs.webservices.framework.dataaccess.BasicXMLStreamReader;
import gov.usgswim.datatable.DataTable;
import gov.usgswim.sparrow.PredictData;
import gov.usgswim.sparrow.cachefactory.AggregateIdLookupKludge;
import gov.usgswim.sparrow.datatable.PredictResult;

import gov.usgswim.sparrow.service.SharedApplication;
import javax.xml.stream.XMLStreamException;

public class PredictExportSerializer extends BasicXMLStreamReader{

	public static String TARGET_NAMESPACE = "http://www.usgs.gov/sparrow/prediction-response/v0_1";
	public static String TARGET_NAMESPACE_LOCATION = "http://www.usgs.gov/sparrow/prediction-response/v0_1.xsd";
	public static String T_PREFIX = "mod";
	private PredictExportRequest request;
	private DataTable result;
	private PredictData predictData;
	//
	protected ParseState state = new ParseState();

	// ===========
	// INNER CLASS
	// ===========
	protected class ParseState{
		protected int r = 0;
		public boolean isDataFinished() {
			return r >= result.getRowCount();
		}
	};

	// ============
	// CONSTRUCTORS
	// ============	
	public PredictExportSerializer(PredictExportRequest request, DataTable result, PredictData predictData) {
		super();
		this.request = request;
		this.result = result;
		this.predictData = predictData;
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
		events.add(new BasicTagEvent(START_ELEMENT, "sparrow-prediction-response")
		.addAttribute(XMLSCHEMA_PREFIX, XMLSCHEMA_NAMESPACE, "schemaLocation", TARGET_NAMESPACE + " " + TARGET_NAMESPACE_LOCATION));

		addOpenTag("response");
		{
			events.add(new BasicTagEvent(START_ELEMENT, "metadata")
			.addAttribute("rowCount", Integer.toString(result.getRowCount()))
			.addAttribute("columnCount", Integer.toString(result.getColumnCount())));
			{
				addOpenTag("columns");
				{

					if (request.isIncludeReachAttribs()) {

						//Add a group for the source columns
						events.add(new BasicTagEvent(START_ELEMENT, "group")
						.addAttribute("name", "Reach Attributes"));

						for(int i=0; i<predictData.getSys().getColumnCount(); i++) {
							events.add(makeNonNullBasicTag("col", "")
									.addAttribute("name", predictData.getSys().getName(i))
									.addAttribute("type", "number"));
						}

						addCloseTag("group");
					}

					if (request.isIncludeSource()) {

						//Add a group for the source columns
						events.add(new BasicTagEvent(START_ELEMENT, "group")
						.addAttribute("name", "Source Values"));

						for(int i=0; i<predictData.getSrc().getColumnCount(); i++) {
						    String name = predictData.getSrc().getName(i);
						    name += " (" + predictData.getSrc().getProperty(i, "constituent") + ")";
						    name += " (" + predictData.getSrc().getUnits(i) + ")";
						    
							events.add(makeNonNullBasicTag("col", "")
									.addAttribute("name", name)
									.addAttribute("type", "Number"));
						}

						addCloseTag("group");
					}

					if (request.isIncludePredict()) {
						events.add(new BasicTagEvent(START_ELEMENT, "group")
						.addAttribute("name", "Predicted Values"));

						for(int i=0; i<result.getColumnCount(); i++) {
						    String name = result.getName(i);
						    name += " (" + result.getProperty(i, PredictResult.CONSTITUENT_PROP) + ")";
						    name += " (" + result.getUnits(i) + ")";
						    
							events.add(makeNonNullBasicTag("col", "")
									.addAttribute("name", name)
									.addAttribute("type", "Number"));
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

	protected void readRow(){
		if (!state.isDataFinished()) {
			// read the row
                    BasicTagEvent rowEvent = new BasicTagEvent(START_ELEMENT, "r");
                    if (predictData.getSrc().getProperty("aggLevelKludge") != null) {
                        // Kludge the id into the row - temporary
                        String aggLevel = predictData.getSrc().getProperty("aggLevelKludge");
                        AggregateIdLookupKludge kludge = SharedApplication.getInstance().getAggregateIdLookup(aggLevel);
                        String id = kludge.lookupId(result.getIdForRow(state.r));
                        rowEvent.addAttribute("id", id);
                    } else {
                        // Get the id the old (better) way
                        rowEvent.addAttribute("id", Long.valueOf(result.getIdForRow(state.r)).toString());
                    }
                    
			events.add(rowEvent);
			{				
				if (request.isIncludeReachAttribs()) {
					for (int c = 0; c < predictData.getSys().getColumnCount(); c++)  {
						addNonNullBasicTag("c", predictData.getSys().getString(state.r, c));
					}
				}

				if (request.isIncludeSource()) {
					for (int c = 0; c < predictData.getSrc().getColumnCount(); c++)  {
						addNonNullBasicTag("c", predictData.getSrc().getString(state.r, c));
					}
				}

				if (request.isIncludePredict()) {
					for (int c = 0; c < result.getColumnCount(); c++)  {
						addNonNullBasicTag("c", result.getString(state.r, c));
					}
				}
				
			}
			addCloseTag("r");
			events.add(new BasicTagEvent(SPACE));

		} 
		state.r++;
	}

	@Override
	public void close() throws XMLStreamException {
		// not much needs to be done. no resources to release
		result = null;
		request = null;
		predictData = null;
	}

	// ==========================
	// SIMPLE GETTERS AND SETTERS
	// ==========================
	public String getTargetNamespace() {
		return TARGET_NAMESPACE;
	}
}

