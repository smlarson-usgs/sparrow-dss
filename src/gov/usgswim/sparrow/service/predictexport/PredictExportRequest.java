package gov.usgswim.sparrow.service.predictexport;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ParserHelper;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class PredictExportRequest implements XMLStreamParserComponent, PipelineRequest {

	private static final long serialVersionUID = -53439131354L;
	public static final String MAIN_ELEMENT_NAME = "sparrow-report-request";
	public static final String PC_EXPORT_FILENAME = "predict_export";
	
	private String xmlRequest;
	private ResponseFormat responseFormat;
	
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
	public PredictExportRequest(Integer contextID, ResponseFormat respFormat, String bbox) {
		this.contextID = contextID;
		this.respFormat = respFormat;
		this.bbox = bbox;
	}

	
	// ===============
	// INSTANCE FIELDS
	// ===============
	private Integer contextID;
	private ResponseFormat respFormat;
	private String bbox;
	private boolean includeReachAttribs = true;
	private boolean includeSource = true;
	private boolean includePredict = true;

	// ================
	// INSTANCE METHODS
	// ================
	public PredictExportRequest parse(XMLStreamReader in)
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
						//nothing to do
					} else if ("prediction-context".equals(localName)) {
						contextID = ParserHelper.parseAttribAsInt(in, "context-id", true);
						ParserHelper.ignoreElement(in);
					} else if (ResponseFormat.isTargetMatch(localName)) {
						
						respFormat = ResponseFormat.parseStream(in);
						if (respFormat.fileName == null) respFormat.fileName = PC_EXPORT_FILENAME;
						
					} else if ("bbox".equals(localName)) {
						bbox = ParserHelper.parseSimpleElementValue(in);
					} else if ("response-content".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else if ("columns".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else if ("binning".equals(localName)) {
						ParserHelper.ignoreElement(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						checkValidity();
						respFormat = (respFormat == null)? makeDefaultResponseFormat(): respFormat;
						return this; // we're done
					} else {
						// otherwise, error
						throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					//break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
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
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " must contain a context id.");
		}
	}

	public boolean isValid() {
		return contextID != null;
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

	public ResponseFormat getRespFormat() {
  	return respFormat;
  }
	
	public boolean isIncludeSource() {
  	return includeSource;
  }

	public boolean isIncludePredict() {
  	return includePredict;
  }

	public boolean isIncludeReachAttribs() {
  	return includeReachAttribs;
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

}
