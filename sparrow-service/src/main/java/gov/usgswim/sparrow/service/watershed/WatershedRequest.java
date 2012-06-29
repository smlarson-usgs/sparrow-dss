package gov.usgswim.sparrow.service.watershed;

import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class WatershedRequest implements XMLStreamParserComponent,
		PipelineRequest {

	public static final String ELEMENT_MIME_TYPE = "mime-type";
	public static final String ELEMENT_MODEL_ID = "model-id";
	public static final String ELEMENT_WATERSHED_ID = "watershed-id";
	
	private static final long serialVersionUID = -1L;
	
	private ResponseFormat responseFormat;

	private Long modelId;
	private Long watershedId;
	

	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return false;
	}

	public static WatershedRequest parseStream(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {

		return null;
	}

	/**
	 * Constructs an empty instance.
	 */
	public WatershedRequest(Long modelId, Long watershedId, ResponseFormat responseFormat) {
			this.modelId = modelId;
			this.watershedId = watershedId;
			this.responseFormat = responseFormat;
	}
	





	// ================
	// INSTANCE METHODS
	// ================
	
	public Long getModelId() {
		return modelId;
	}

	public Long getWatershedId() {
		return watershedId;
	}
	
	public WatershedRequest parse(XMLStreamReader in)
			throws XMLStreamException, XMLParseValidationException {
		return null;
	}

	public String getParseTarget() {
		return "";
	}

	public boolean isParseTarget(String name) {
		return false;
	}

	public void checkValidity() throws XMLParseValidationException {

	}

	public boolean isValid() {
		return (modelId != null ^ watershedId != null);
	}

	public static ResponseFormat makeDefaultResponseFormat() {
		ResponseFormat result = new ResponseFormat();
		result.fileName = "Watershed";
		result.setMimeType("xml");
		result.setAttachment(true);
		return result;
	}

	@Override
	public String getXMLRequest() {
		return "";
	}

	@Override
	public void setXMLRequest(String request) {
		
	}

	@Override
	public void setResponseFormat(ResponseFormat respFormat) {
		this.responseFormat = respFormat;
	}

	public ResponseFormat getResponseFormat() {
		if (responseFormat == null) {
			setResponseFormat(makeDefaultResponseFormat());
		}
		return responseFormat;
	}


}
