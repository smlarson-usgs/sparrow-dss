package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.sparrow.parser.ParserHelper;
import gov.usgswim.sparrow.parser.ResponseFormat;

import java.awt.Point;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;


public class IDByPointParser extends AbstractHttpRequestParser<IDByPointRequest> {
	public IDByPointParser() {
	}

	/**
	 * parse() overridden to handle the special case of input parameters being
	 * placed in query string, as IDByPoint is simple enough to invoke in this way.
	 * 
	 * @see gov.usgswim.service.AbstractHttpRequestParser#parse(javax.servlet.http.HttpServletRequest)
	 */
	public IDByPointRequest idParse(HttpServletRequest request) throws Exception {
		// Special handling for a GET request
		if ("GET".equals(request.getMethod()) && request.getParameter(getXmlParam()) == null) {
			String[] paramChain = parseExtraPath(request);

			if (paramChain.length == 1 && StringUtils.isNumeric(paramChain[0])) {

				Long modelID = Long.parseLong(paramChain[0]);

				//TODO:  There are four IDByPointRequest constructors that can be used here - currently only handling one possiblity
				Point.Double point = new Point.Double();	//required
				point.x = parseParamAsDouble(request, "long");
				point.y = parseParamAsDouble(request, "lat");

				IDByPointRequest result = new IDByPointRequest(modelID, point);
				result.setXMLRequest(""); // no xml request, RESTlike
				return result;
			} else {
				throw new Exception("The IDByPoint Request must contain exactly one argument as part of the URL - the model ID.");
			}

		} else {
			// Normal POST requests are handled normally by parent class
			return super.parse(request);
		}
	}
	
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
