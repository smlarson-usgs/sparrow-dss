package gov.usgswim.sparrow.service.predict;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ReachElement;
import gov.usgswim.sparrow.domain.ReachGroup;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class PredictExportRequest implements XMLStreamParserComponent, PipelineRequest {

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
	public PredictExportRequest(Integer contextID, ResponseFormat respFormat, String bbox) {
		this.contextID = contextID;
		this.responseFormat = respFormat;
		this.bbox = bbox;
	}


	public PredictExportRequest(Long modelID, ResponseFormat respFormat, String bbox) {
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
	private Long modelID;
	private String bbox;
	private boolean includeReachAttribs = false;
	private boolean includeSource = false;
	private boolean includePredict = false;
	private boolean hasAdjustments = false;

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
					} else if ("PredictionContext".equals(localName)) {
						contextID = ParserHelper.parseAttribAsInt(in, "context-id", true);
						ParserHelper.ignoreElement(in);
						AdjustmentGroups g;
						try {
							g = SharedApplication.getInstance().getPredictionContext(contextID).getAdjustmentGroups();
							if(g != null) {
								if(g.getIndividualGroup().getAdjustments().size()>0) hasAdjustments = true;
								for(ReachElement r : g.getIndividualGroup().getExplicitReaches()){
									if(r.getAdjustments().size() > 0) {
										hasAdjustments = true;
									}
								}
								if(g.getReachGroups().size()>0) {
									for(ReachGroup r : g.getReachGroups()){
										if(r.getAdjustments().size()>0) hasAdjustments = true;
									}
								}
							}
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					} else if (ResponseFormat.isTargetMatch(localName)) {

						responseFormat = ResponseFormat.parseStream(in);
						if (responseFormat.fileName == null) responseFormat.fileName = PC_EXPORT_FILENAME;

					} else if ("bbox".equals(localName)) {
						bbox = ParserHelper.parseSimpleElementValue(in);
					} else if ("response-content".equals(localName)) {
                        // do nothing - just a container
                    } else if ("attributes".equals(localName)) {
                        includeReachAttribs = true;
                        ParserHelper.ignoreElement(in);
                    } else if ("source-values".equals(localName)) {
                        includeSource = true;
                        ParserHelper.ignoreElement(in);
                    } else if ("predicted".equals(localName)) {
                        includePredict = true;
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
						responseFormat = (responseFormat == null)? makeDefaultResponseFormat(): responseFormat;
						return this; // we're done
                                        } else if ("response-content".equals(localName)) {
                                            // ignore - just a container element
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
		return responseFormat;
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
	
	public boolean hasAdjustments() {
		return hasAdjustments;
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

	

}
