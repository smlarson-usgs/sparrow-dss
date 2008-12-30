package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.sparrow.parser.ParserHelper;
import gov.usgswim.sparrow.parser.ResponseFormat;

import java.awt.Point;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;


public class IDByPointParser extends AbstractHttpRequestParser<IDByPointRequest> {
	public IDByPointParser() {
	}

	/**
	 * parse() overridden to handle the special case of input parameters being
	 * placed in query string, as IDByPoint is simple enough to invoke in this way.
	 * Example requests:
	 * <ul>
	 * 	<li>c/43245?lat=55&long=-90&json returns a json id by contextID and point request</li>
	 * 	<li>m/22?reachID=31170&pred returns an xml id by model and reachID request with prediction</li>
	 * </ul>
	 * @see gov.usgswim.service.AbstractHttpRequestParser#parse(javax.servlet.http.HttpServletRequest)
	 */
	public IDByPointRequest idParse(HttpServletRequest request) throws Exception {
		// Special handling for a GET request
		if ("GET".equals(request.getMethod()) && request.getParameter(getXmlParam()) == null) {
			String[] paramChain = parseExtraPath(request);

			if (paramChain == null ) {
				String modelParam = request.getParameter("model");
				String contextParam = request.getParameter("context");
				
				boolean isModelRequest = (modelParam != null);
				boolean isContextRequest = (contextParam != null);
				
				// This second parameter may be either a model or contextID.
				Long modelID = (isModelRequest)? Long.parseLong(modelParam): null;
				Integer contextID = (isContextRequest)? Integer.parseInt(contextParam): null;

				String reachIDString = request.getParameter("reach");
				Integer reachID = (reachIDString == null)? null: Integer.valueOf(reachIDString);
				
				Point.Double point = new Point.Double();
				if (reachID == null) { // if no reach id then it's a point
					point.x = parseParamAsDouble(request, "long");
					point.y = parseParamAsDouble(request, "lat");
				}

				
				String paramValue = request.getParameter("adj");
				boolean hasAdjustment = (paramValue != null);
				paramValue = request.getParameter("attrib");
				boolean hasAttribute = (paramValue != null);
				paramValue = request.getParameter("pred");
				boolean hasPrediction = (paramValue != null);
				
				String format = request.getParameter("format");
				String json = request.getParameter("json");

				IDByPointRequest result = null;
				if (isModelRequest) {
					result = (reachID == null)? new IDByPointRequest(modelID, point): new IDByPointRequest(modelID, reachID);
				} else if (isContextRequest) {
					result = (reachID == null)? new IDByPointRequest(contextID, point): new IDByPointRequest(contextID, reachID);
				}
				
				result.setXMLRequest(""); // no xml request, RESTlike
				result.setAdjustments(hasAdjustment);
				result.setAttributes(hasAttribute);
				result.setPredicted(hasPrediction);
				
				String mimetype = (json == null)? null: "json";
				mimetype = (format == null)? mimetype: format;
				ResponseFormat rf = IDByPointRequest.makeDefaultResponseFormat(mimetype);
				rf.setAttachment(false);
				result.setResponseFormat(rf);
				
				return result;
			}
			throw new Exception("The IDByPoint Request must contain exactly one argument as part of the URL - the model ID.");

		}
		// Normal POST requests are handled normally by parent class
		return super.parse(request);
	}
	
	@Override
	public IDByPointRequest parse(HttpServletRequest request) throws Exception {
		IDByPointRequest result = idParse(request);
		ResponseFormat respFormat = result.getResponseFormat();
		
		String mimeType = request.getParameter("mimetype");
		if (mimeType != null) {
			respFormat.setMimeType(mimeType);
		}
		if (respFormat.getMimeType() == null){
			respFormat.setMimeType("xml"); // defaults to xml
		}
		
		String compress = request.getParameter("compress");
		if (compress != null && compress.equals("zip")) {
			respFormat.setCompression("zip");
		}
		return result;
	}
	
	public IDByPointRequest parse(XMLStreamReader in) throws Exception {
		IDByPointRequest ibpRequest = new IDByPointRequest();
		ParserHelper.parseToStartTag(in, IDByPointRequest.MAIN_ELEMENT_NAME);
		return ibpRequest.parse(in);
	}

}
