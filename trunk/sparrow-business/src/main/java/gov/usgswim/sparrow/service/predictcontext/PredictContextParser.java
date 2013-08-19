package gov.usgswim.sparrow.service.predictcontext;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.RequestParser;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.parser.ResponseFormat;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

public class PredictContextParser 
	extends AbstractHttpRequestParser<PredictContextRequest> 
	implements RequestParser<PredictContextRequest>  {

	@Override
	public PredictContextRequest parse(HttpServletRequest request)
			throws Exception {

		
		PredictContextRequest result = super.parse(request);
		ResponseFormat respFormat = result.getResponseFormat();
		
		String mimeType = request.getParameter("mimetype");
		if (mimeType != null) {
			respFormat.setMimeType(mimeType);
		}

		String compress = request.getParameter("compress");
		if (compress != null && compress.equals("zip")) {
			respFormat.setCompression("zip");
		}
		return result;
	}

	public PredictContextRequest parse(XMLStreamReader in) throws Exception {
		PredictionContext pc = new PredictionContext();
		// pre-parse to set the stream at the appropriate spot before handing off to XMLParserComponent, which expects to be at its start tag.
		ParserHelper.parseToStartTag(in, PredictionContext.MAIN_ELEMENT_NAME);
		pc.parse(in);
		return new PredictContextRequest(pc);
	}

}
