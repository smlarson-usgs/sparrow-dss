package gov.usgswim.sparrow.service.deliveryreport;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ReportRequest implements XMLStreamParserComponent,
		PipelineRequest {

	private static final long serialVersionUID = -1L;
	public static final String MAIN_ELEMENT_NAME = "sparrow-report-request";
	public static final String PC_EXPORT_FILENAME = "sparrow-report";
	
	public static final String ELEMENT_MIME_TYPE = "mime-type";
	public static final String ELEMENT_CONTEXT_ID = "context-id";
	public static final String ELEMENT_INCLUDE_ID_SCRIPT = "include-id-script";
	public static final String ELEMENT_REPORT_TYPE = "report-type";
	
	public enum ReportType {
		terminal,
		state
	}
	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private String xmlRequest;
	private ResponseFormat responseFormat;
	private Integer contextID;
	private PredictionContext context;
	private boolean includeIdScript;
	private ReportType reportType;
	

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	public static ReportRequest parseStream(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {

		ReportRequest per = new ReportRequest();
		return per.parse(in);
	}

	/**
	 * Constructs an empty instance.
	 */
	public ReportRequest() {

	}

	/**
	 * Construct an instance w/ basic options (used for GET requests)
	 */
	public ReportRequest(Integer contextID, ReportType reportType, ResponseFormat respFormat, boolean includeIdScript) {
		this.contextID = contextID;
		this.reportType = reportType;
		this.responseFormat = respFormat;
		this.includeIdScript = includeIdScript;
	}



	// ================
	// INSTANCE METHODS
	// ================
	public ReportRequest parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {

		String localName = in.getLocalName();
		int eventCode = in.getEventType();
		assert (isTargetMatch(localName) && eventCode == START_ELEMENT) : this
				.getClass().getSimpleName()
				+ " can only parse "
				+ MAIN_ELEMENT_NAME + " elements.";
		boolean isStarted = false;

		while (in.hasNext()) {
			if (isStarted) {
				// Don't advance past the first element.
				eventCode = in.next();
			} else {
				isStarted = true;
			}

			// Main event loop -- parse until corresponding target end tag
			// encountered.
			switch (eventCode) {
			case START_ELEMENT:

				localName = in.getLocalName();

				if (isTargetMatch(localName)) {
					// nothing to do
				} else if ("PredictionContext".equals(localName)) {
					contextID = ParserHelper.parseAttribAsInt(in, "context-id", false);
					
					if (contextID != null) {
						//Ignore the rest of the tag
						ParserHelper.ignoreElement(in);
					} else {
						//No context ID, so assume we have the context inline
						context = PredictionContext.parseStream(in);
					}
					
				} else if (ResponseFormat.isTargetMatch(localName)) {

					responseFormat = ResponseFormat.parseStream(in);
					if (responseFormat.fileName == null)
						responseFormat.fileName = PC_EXPORT_FILENAME;
					
				} else if (ELEMENT_REPORT_TYPE.equals(localName)) {

					String s = ParserHelper.parseAttribAsString(in, ELEMENT_REPORT_TYPE, true);
					try {
						reportType = ReportType.valueOf(s);
					} catch (Exception e) {
						throw new XMLParseValidationException("Unrecognized report-type");
					}

				} else {
					throw new RuntimeException(
							"unrecognized child element of <" + localName
									+ "> for " + MAIN_ELEMENT_NAME);
				}
				break;
			case END_ELEMENT:
				localName = in.getLocalName();
				if (MAIN_ELEMENT_NAME.equals(localName)) {
					checkValidity();
					responseFormat = (responseFormat == null) ? makeDefaultResponseFormat()
							: responseFormat;
					return this; // we're done
				} else if ("response-content".equals(localName)) {
					// ignore - just a container element
				} else {
					// otherwise, error
					throw new RuntimeException("unexpected closing tag of </"
							+ localName + ">; expected  " + MAIN_ELEMENT_NAME);
				}
				// break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME
				+ "> not closed. Unexpected end of stream?");
	}

	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isParseTarget(String name) {
		return MAIN_ELEMENT_NAME.equals(name);
	}

	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME
					+ " must contain a context id.");
		}
	}

	public boolean isValid() {
		return (contextID != null || context != null);
	}

	private ResponseFormat makeDefaultResponseFormat() {
		ResponseFormat result = new ResponseFormat();
		result.fileName = PC_EXPORT_FILENAME;
		result.setMimeType("xml");
		return result;
	}

	public Integer getContextID() {
		return contextID;
	}
	
	public PredictionContext getContext() {
		return context;
	}

	public ResponseFormat getRespFormat() {
		return responseFormat;
	}

	public String getXMLRequest() {
		return xmlRequest;
	}

	public ResponseFormat getResponseFormat() {
		if (responseFormat == null) {
			setResponseFormat(new ResponseFormat());
		}
		return responseFormat;
	}
	
	public ReportType getReportType() {
		return reportType;
	}

	@Override
	public void setXMLRequest(String request) {
		this.xmlRequest = request;
		
	}

	@Override
	public void setResponseFormat(ResponseFormat respFormat) {
		this.responseFormat = respFormat;
		
	}

	public boolean isIncludeIdScript() {
		return includeIdScript;
	}

}
