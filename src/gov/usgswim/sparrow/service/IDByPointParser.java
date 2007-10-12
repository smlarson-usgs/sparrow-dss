package gov.usgswim.sparrow.service;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.HttpRequestParser;

import javax.servlet.http.HttpServletRequest;

public class IDByPointParser extends AbstractHttpRequestParser<IDByPointRequest> {
	public IDByPointParser() {
	}

	//TODO: FINISH PARSER
	public IDByPointRequest parse(HttpServletRequest request) throws Exception {
		String path = request.getPathInfo();
		
		
		
		return null;
	}
}
