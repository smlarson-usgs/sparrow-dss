package gov.usgswim.sparrow.service.deliveryreport;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.RequestParser;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.util.ParserHelper;

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
			
			String contextIDParam = request.getParameter("context-id");
			String mimeType = request.getParameter("mime-type");
			Integer contextID = Integer.parseInt(contextIDParam);
			
			
			ResponseFormat respFormat = new ResponseFormat();
			respFormat.setMimeType(mimeType);
			respFormat.setAttachment(true);
			if (respFormat.fileName == null) respFormat.fileName = ReportRequest.PC_EXPORT_FILENAME;
			
			req = new ReportRequest(contextID, respFormat);
			
			
			
		} else if (request.getMethod().equals("POST")) {
			
			req = super.parse(request);
			
		} else {
			throw new Exception("Unsupport HTTP method.");
		}

		return req;
	}

	public ReportRequest parse(XMLStreamReader in) throws Exception {
		ReportRequest pr = new ReportRequest();
		// pre-parse to set the stream at the appropriate spot before handing off to XMLParserComponent, which expects to be at its start tag.
		ParserHelper.parseToStartTag(in, ReportRequest.MAIN_ELEMENT_NAME);
		pr.parse(in);
		return pr;
	}

}
