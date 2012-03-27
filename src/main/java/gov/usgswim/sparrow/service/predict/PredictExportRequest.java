package gov.usgswim.sparrow.service.predict;

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

public class PredictExportRequest implements XMLStreamParserComponent,
		PipelineRequest {

	private static final long serialVersionUID = -53439131354L;
	public static final String MAIN_ELEMENT_NAME = "sparrow-report-request";
	public static final String PC_EXPORT_FILENAME = "predict_export";

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	public static PredictExportRequest parseStream(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {

		PredictExportRequest per = new PredictExportRequest();
		return per.parse(in);
	}

	/**
	 * Constructs an empty instance.
	 */
	public PredictExportRequest() {

	}

	/**
	 * Construct an instance w/ basic options (used for GET requests)
	 */
	public PredictExportRequest(Integer contextID, ResponseFormat respFormat,
			String bbox) {
		this.contextID = contextID;
		this.responseFormat = respFormat;
		this.bbox = bbox;
	}

	public PredictExportRequest(Long modelID, ResponseFormat respFormat,
			String bbox) {
		this.modelID = modelID;
		this.responseFormat = respFormat;
		this.bbox = bbox;
	}

	// ===============
	// INSTANCE FIELDS
	// ===============
	private String xmlRequest;
	private ResponseFormat responseFormat;
	private Integer contextID;
	private PredictionContext context;

	private Long modelID;
	private String bbox;
	private boolean includeReachIdAttribs = false;
	private boolean includeReachStatAttribs = false;
	private boolean includeOrgSource = false;
	private boolean includeAdjSource = false;
	private boolean includeOrgPredict = false;
	private boolean includeAdjPredict = false;

	// ================
	// INSTANCE METHODS
	// ================
	public PredictExportRequest parse(XMLStreamReader in)
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

				} else if ("bbox".equals(localName)) {
					bbox = ParserHelper.parseSimpleElementValue(in);
				} else if ("response-content".equals(localName)) {
					// do nothing - just a container
				} else if ("id-attributes".equals(localName)) {
					includeReachIdAttribs = true;
					ParserHelper.ignoreElement(in);
				} else if ("stat-attributes".equals(localName)) {
					includeReachStatAttribs = true;
					ParserHelper.ignoreElement(in);
				} else if ("original-source-values".equals(localName)) {
					includeOrgSource = true;
					ParserHelper.ignoreElement(in);
				} else if ("adjusted-source-values".equals(localName)) {
					includeAdjSource = true;
					ParserHelper.ignoreElement(in);
				} else if ("original-predicted-values".equals(localName)) {
					includeOrgPredict = true;
					ParserHelper.ignoreElement(in);
				} else if ("adjusted-predicted-values".equals(localName)) {
					includeAdjPredict = true;
					ParserHelper.ignoreElement(in);
				} else if ("columns".equals(localName)) {
					ParserHelper.ignoreElement(in);
				} else if ("binning".equals(localName)) {
					ParserHelper.ignoreElement(in);
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

	/**
	 * True to include the original (unadjusted) source values.
	 * @return
	 */
	public boolean isIncludeOrgSource() {
		return includeOrgSource;
	}
	
	/**
	 * True to include the user adjusted source values.
	 * @return
	 */
	public boolean isIncludeAdjSource() {
		return includeAdjSource;
	}

	/**
	 * True to include the original predicted values, unmodified by any user
	 * adjustments to the sources.
	 * @return
	 */
	public boolean isIncludeOrgPredict() {
		return includeOrgPredict;
	}
	
	/**
	 * True to include the predicted values that result from the user adjusted
	 * sources.  This would normally be the key set of data, in addition to the
	 * specific data series that the user is mapping, that would be expected
	 * in the export.
	 * @return
	 */
	public boolean isIncludeAdjPredict() {
		return includeAdjPredict;
	}

	public boolean isIncludeReachIdAttribs() {
		return includeReachIdAttribs;
	}

	public String getXMLRequest() {
		return xmlRequest;
	}

	public void setXMLRequest(String request) {
		xmlRequest = request;
	}

	public void setResponseFormat(ResponseFormat respFormat) {
		this.responseFormat = respFormat;
		responseFormat.fileName = PC_EXPORT_FILENAME;
	}

	public ResponseFormat getResponseFormat() {
		if (responseFormat == null) {
			setResponseFormat(new ResponseFormat());
		}
		return responseFormat;
	}

	public String getBbox() {
		return bbox;
	}

	public Long getModelID() {
		return modelID;
	}

	public void setModelID(Long modelID) {
		this.modelID = modelID;
	}

	public boolean isIncludeReachStatAttribs() {
		return includeReachStatAttribs;
	}

}
