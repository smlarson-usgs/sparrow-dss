package gov.usgswim.sparrow.service.binning;

import gov.usgswim.service.AbstractHttpRequestParser;
import gov.usgswim.service.RequestParser;
import gov.usgswim.sparrow.request.BinningRequest.BIN_TYPE;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLStreamReader;

public class BinningParser extends AbstractHttpRequestParser<BinningServiceRequest> 
implements RequestParser<BinningServiceRequest>  {
    
    @Override
	public BinningServiceRequest parse(HttpServletRequest request) throws Exception {
        BinningServiceRequest req = null;
        
        if (request.getMethod().equals("GET")) {
            String contextId = request.getParameter("context-id");
            String binCount = request.getParameter("bin-count");
            String binType = request.getParameter("bin-type");
            
            BIN_TYPE binTypeValue = BIN_TYPE.EQUAL_COUNT;
            if ("EQUAL_RANGE".equals(binType)) {
                binTypeValue = BIN_TYPE.EQUAL_RANGE;
            }
            
            req = new BinningServiceRequest(Integer.valueOf(contextId), Integer.valueOf(binCount), binTypeValue);
        } else {
            throw new Exception("Unsupported HTTP method.");
        }
        
        return req;
    }

    public BinningServiceRequest parse(XMLStreamReader in) throws Exception {
        throw new Exception("Unsupported request format.");
        /*
        BinningServiceRequest br = new BinningServiceRequest();
        // pre-parse to set the stream at the appropriate spot before handing off to XMLParserComponent, which expects to be at its start tag.
        ParserHelper.parseToStartTag(in, PredictExportRequest.MAIN_ELEMENT_NAME);
        br.parse(in);
        return br;
        */
    }
}
