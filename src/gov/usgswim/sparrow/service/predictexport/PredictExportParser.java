package gov.usgswim.sparrow.service.predictexport;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.RequestParser;
import gov.usgswim.sparrow.parser.ParserHelper;
import gov.usgswim.sparrow.parser.ResponseFormat;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

public class PredictExportParser 
	extends AbstractHttpRequestParser<PredictExportRequest> 
	implements RequestParser<PredictExportRequest>  {

	@Override
	public PredictExportRequest parse(HttpServletRequest request)
			throws Exception {

		PredictExportRequest req = null;
		
		if (request.getMethod().equals("GET")) {
			
			String modelParam = request.getParameter("model");
			String contextIDParam = request.getParameter("context-id");
			String mimeType = request.getParameter("mime-type");
			String bbox = request.getParameter("bbox");
			
			boolean isModelRequest = (modelParam != null);
			boolean isContextRequest = (contextIDParam != null);
			
			Long modelID = (isModelRequest)? Long.parseLong(modelParam): null;
			Integer contextID = (isContextRequest)? Integer.parseInt(contextIDParam): null;
			
			
			ResponseFormat respFormat = new ResponseFormat();
			respFormat.setMimeType(mimeType);
			if (respFormat.fileName == null) respFormat.fileName = PredictExportRequest.PC_EXPORT_FILENAME;
			
			if (isContextRequest) {
				req = new PredictExportRequest( contextID, respFormat, bbox);
			} else if (isModelRequest) {
				req = new PredictExportRequest( modelID, respFormat, bbox);
			}
			
			
			
		} else if (request.getMethod().equals("POST")) {
			
			req = super.parse(request);
			
		} else {
			throw new Exception("Unsupport HTTP method.");
		}

		return req;
	}

	public PredictExportRequest parse(XMLStreamReader in) throws Exception {
		PredictExportRequest pr = new PredictExportRequest();
		// pre-parse to set the stream at the appropriate spot before handing off to XMLParserComponent, which expects to be at its start tag.
		ParserHelper.parseToStartTag(in, PredictExportRequest.MAIN_ELEMENT_NAME);
		pr.parse(in);
		return pr;
	}

}
