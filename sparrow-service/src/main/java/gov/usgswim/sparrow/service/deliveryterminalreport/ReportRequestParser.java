package gov.usgswim.sparrow.service.deliveryterminalreport;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.RequestParser;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.util.ParserHelper;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

public class ReportRequestParser 
		extends AbstractHttpRequestParser<ReportRequest> 
		implements RequestParser<ReportRequest>  {
	

	@Override
	public ReportRequest parse(HttpServletRequest request)
			throws Exception {

		ReportRequest req = null;
		
		if (request.getMethod().equals("GET")) {
			
			//debugParams(request);
			
			String mimeType = request.getParameter(ReportRequest.ELEMENT_MIME_TYPE);
			Long contextID = parseParamAsLong(request, ReportRequest.ELEMENT_CONTEXT_ID, true);
			boolean includeIdScript = true;
			ReportRequest.ReportType reportType = ReportRequest.ReportType.terminal;
			
			ResponseFormat respFormat = new ResponseFormat();
			respFormat.setMimeType(mimeType);
			respFormat.setAttachment(true);
			if (respFormat.fileName == null) respFormat.fileName = ReportRequest.PC_EXPORT_FILENAME;
			
			req = new ReportRequest(contextID.intValue(), reportType, null, respFormat, includeIdScript);
			
			
		} else if (request.getMethod().equals("POST")) {
			
			req = super.parse(request);
			
		} else {
			throw new Exception("Unsupport HTTP method.");
		}

		return req;
	}

	public ReportRequest parse(XMLStreamReader in) throws Exception {
		ReportRequest pr = new ReportRequest(ReportRequest.ReportType.region_agg, true);
		// pre-parse to set the stream at the appropriate spot before handing off to XMLParserComponent, which expects to be at its start tag.
		ParserHelper.parseToStartTag(in, ReportRequest.MAIN_ELEMENT_NAME);
		pr.parse(in);
		return pr;
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
