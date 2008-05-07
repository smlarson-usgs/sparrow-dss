package gov.usgswim.sparrow.service.idbypoint;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.parser.ParserHelper;
import gov.usgswim.sparrow.parser.ResponseFormat;

import java.awt.Point;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;


public class IDByPointParser2 extends AbstractHttpRequestParser<IDByPointRequest2> {
	public IDByPointParser2() {
	}

	/**
	 * parse() overridden to handle the special case of input parameters being
	 * placed in query string, as IDByPoint is simple enough to invoke in this way.
	 * 
	 * @see gov.usgswim.service.AbstractHttpRequestParser#parse(javax.servlet.http.HttpServletRequest)
	 */
	public IDByPointRequest2 idParse(HttpServletRequest request) throws Exception {
		// Special handling for a GET request
		if ("GET".equals(request.getMethod()) && request.getParameter(getXmlParam()) == null) {
			String[] paramChain = parseExtraPath(request);

			if (paramChain.length == 1 && StringUtils.isNumeric(paramChain[0])) {

				Integer modelID = Integer.parseInt(paramChain[0]);
				int numResults = AbstractHttpRequestParser.parseParamAsLong(request, "result-count", 3L).intValue();
				if (numResults > 100) numResults = 100;

				Point.Double point = new Point.Double();	//required
				point.x = parseParamAsDouble(request, "long");
				point.y = parseParamAsDouble(request, "lat");

				IDByPointRequest2 result = new IDByPointRequest2(modelID, point, numResults);
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
	
	public IDByPointRequest2 parse(HttpServletRequest request) throws Exception {
		IDByPointRequest2 result = idParse(request);
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
	
	public IDByPointRequest2 parse(XMLStreamReader in) throws Exception {
		IDByPointRequest2 ibpRequest = new IDByPointRequest2();
		ParserHelper.parseToStartTag(in, IDByPointRequest2.MAIN_ELEMENT_NAME);
		return ibpRequest.parse(in);
	}

}
