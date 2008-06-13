package gov.usgswim.sparrow.service.idbypoint;

import static javax.xml.stream.XMLStreamConstants.END_ELEMENT;
import static javax.xml.stream.XMLStreamConstants.START_ELEMENT;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ParserHelper;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.parser.XMLStreamParserComponent;

import java.awt.Point;
import java.awt.geom.Point2D.Double;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class IDByPointRequest implements XMLStreamParserComponent, PipelineRequest {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5107786782665328203L;
	public static final String ID_BY_POINT_FILENAME = "idByPoint";
	public static final String MAIN_ELEMENT_NAME = "sparrow-id-request";
	public static final String MODELID_CHILD = "model-id";
	public static final String CONTEXTID_CHILD = "context-id";
	public static final String POINT_CHILD = "point";
	public static final String REACH_CHILD = "reach";
	public static final String CONTENT_CHILD = "content";
	public static final String ADJUSTMENTS_CHILD = "adjustments";
	public static final String ATTRIBUTES_CHILD = "attributes";
	public static final String PREDICTED_CHILD = "predicted";
	public static final String ALL_CHILD = "all";


	// =============================
	// PUBLIC STATIC UTILITY METHODS
	// =============================
	public static boolean isTargetMatch(String tagName) {
		return MAIN_ELEMENT_NAME.equals(tagName);
	}

	//These two are mutually exclusive 
	private Integer contextID;
	private Long modelID;

	//These two are mutually exclusive 
	private Point.Double point;
	private Integer reachID;

	private boolean adjustments = false;
	private boolean attributes = false;
	private boolean predicted = false;

	// PipelineRequest fields
	private String xmlRequest;
	//private int numberOfResults;
	private ResponseFormat respFormat;

	// ============
	// CONSTRUCTORS
	// ============
	public IDByPointRequest() {

	}

	public IDByPointRequest(Long modelID, Double point) {
		this.modelID = modelID;
		this.point = point;
	}

	public IDByPointRequest(Long modelID, Integer reachID) {
		this.modelID = modelID;
		this.reachID = reachID;
	}

	public IDByPointRequest(Integer contextID, Double point) {
		this.contextID = contextID;
		this.point = point;

	}

	public IDByPointRequest(Integer contextID, Integer reachID) {
		this.contextID = contextID;
		this.reachID = reachID;
	}


	// ================
	// INSTANCE METHODS
	// ================
	public IDByPointRequest parse(XMLStreamReader in)
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
						//Nothing to do for the root element
					} else if (CONTEXTID_CHILD.equals(localName)) {
						this.contextID = Integer.decode( ParserHelper.parseSimpleElementValue(in) );
					} else if (MODELID_CHILD.equals(localName)) {
						modelID = Long.decode( ParserHelper.parseSimpleElementValue(in) );
					} else if (POINT_CHILD.equals(localName)) {

						point = new Point.Double();	//required
						point.x = ParserHelper.parseAttribAsDouble(in, "long");
						point.y = ParserHelper.parseAttribAsDouble(in, "lat");
						ParserHelper.ignoreElement(in);
					} else if (REACH_CHILD.equals(localName)) {
						reachID = ParserHelper.parseAttribAsInt(in, "id");
						ParserHelper.ignoreElement(in);
					} else if (CONTENT_CHILD.equals(localName)) {
						//do nothing - just a container
					} else if (ADJUSTMENTS_CHILD.equals(localName)) {
						adjustments = true;
						ParserHelper.ignoreElement(in);
					} else if (ATTRIBUTES_CHILD.equals(localName)) {
						attributes = true;
						ParserHelper.ignoreElement(in);
					} else if (PREDICTED_CHILD.equals(localName)) {
						predicted = true;
						ParserHelper.ignoreElement(in);
					} else if (ALL_CHILD.equals(localName)) {
						adjustments = true;
						attributes = true;
						predicted = true;
						ParserHelper.ignoreElement(in);
					} else if (ResponseFormat.isTargetMatch(localName)) {
						this.respFormat = ResponseFormat.parseStream(in);
					} else {
						throw new RuntimeException("unrecognized child element of <" + localName + "> for " + MAIN_ELEMENT_NAME);
					}
					break;
				case END_ELEMENT:
					localName = in.getLocalName();
					if (MAIN_ELEMENT_NAME.equals(localName)) {
						respFormat = (respFormat == null)? makeDefaultResponseFormat(null): respFormat;
						if  (respFormat.fileName == null) {
							respFormat.fileName = ID_BY_POINT_FILENAME;
						}
						checkValidity();
						return this; // we're done
					} else if (CONTENT_CHILD.equals(localName)) {
						//ignore - just a container element
					} else {
						// otherwise, error
						throw new RuntimeException("unexpected closing tag of </" + localName + ">; expected  " + MAIN_ELEMENT_NAME);
					}
					//break;
			}
		}
		throw new RuntimeException("tag <" + MAIN_ELEMENT_NAME + "> not closed. Unexpected end of stream?");
	}

	public static ResponseFormat makeDefaultResponseFormat(String mimetype) {
		ResponseFormat result = new ResponseFormat();
		result.fileName = ID_BY_POINT_FILENAME;
		result.setMimeType((mimetype == null)? "xml": mimetype);
		return result;
	}


	public void checkValidity() throws XMLParseValidationException {
		if (!isValid()) {
			// throw a custom error message depending on the error
			throw new XMLParseValidationException(MAIN_ELEMENT_NAME + " is not valid");
		}
	}

	public boolean isValid() {
		return true;
	}

	// =================
	// GETTERS & SETTERS
	// =================
	public String getParseTarget() {
		return MAIN_ELEMENT_NAME;
	}

	public boolean isParseTarget(String name) {
		// TODO Auto-generated method stub
		return MAIN_ELEMENT_NAME.equals(name);
	}

	public java.lang.Double getLatitude() {
		return point.y;
	}

	public java.lang.Double getLongitude() {
		return point.x;
	}

	public Integer getReachID() {
		return reachID;
	}

	public ResponseFormat getResponseFormat() {
		return respFormat;
	}

	public String getXMLRequest() {
		return xmlRequest;
	}

	public void setResponseFormat(ResponseFormat respFormat) {
		this.respFormat = respFormat;		
	}

	public void setXMLRequest(String request) {
		this.xmlRequest = request;		
	}
	public Point.Double getPoint() {
		return point;
	}

	public Long getModelID() {
		return modelID;
	}


	public Integer getContextID() {
		return contextID;
	}


	public boolean hasAdjustments() {
		return adjustments;
	}


	public boolean hasAttributes() {
		return attributes;
	}


	public boolean hasPredicted() {
		return predicted;
	}


	public ResponseFormat getRespFormat() {
		return respFormat;
	}

	public void setAdjustments(boolean adjustments) {
		this.adjustments = adjustments;
	}

	public void setAttributes(boolean attributes) {
		this.attributes = attributes;
	}

	public void setPredicted(boolean predicted) {
		this.predicted = predicted;
	}


}
