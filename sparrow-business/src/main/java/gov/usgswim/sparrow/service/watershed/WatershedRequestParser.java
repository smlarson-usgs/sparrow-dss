package gov.usgswim.sparrow.service.watershed;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.RequestParser;
import gov.usgswim.sparrow.parser.ResponseFormat;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

public class WatershedRequestParser 
		extends AbstractHttpRequestParser<WatershedRequest> 
		implements RequestParser<WatershedRequest>  {
	

	@Override
	public WatershedRequest parse(HttpServletRequest request)
			throws Exception {

		WatershedRequest req = null;
		
		if (request.getMethod().equals("GET") || request.getMethod().equals("POST")) {
			
			//debugParams(request);
			
			String mimeType = request.getParameter(WatershedRequest.ELEMENT_MIME_TYPE);
			Long modelID = parseParamAsLong(request, WatershedRequest.ELEMENT_MODEL_ID, false);
			Long watershedID = parseParamAsLong(request, WatershedRequest.ELEMENT_WATERSHED_ID, false);

			
			ResponseFormat respFormat = WatershedRequest.makeDefaultResponseFormat();
			respFormat.setMimeType(mimeType);
			
			req = new WatershedRequest(modelID, watershedID, respFormat);
		}

		return req;
	}

	public WatershedRequest parse(XMLStreamReader in) throws Exception {
		return null;
	}
	
	public void debugParams(HttpServletRequest request) {
		Enumeration en = request.getParameterNames();
		
		String url = request.getRequestURL().toString();
		System.out.println(url);
		
		while (en.hasMoreElements()) {
			String name = en.nextElement().toString();
			String[] vals = request.getParameterValues(name);
			for (String v : vals) {
				System.out.println(name + ":" + v);
			}
			
		}
	}

}
