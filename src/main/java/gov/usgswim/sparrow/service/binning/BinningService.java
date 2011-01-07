package gov.usgswim.sparrow.service.binning;

import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.request.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.service.SharedApplication;

import java.math.BigDecimal;

import javax.xml.stream.XMLStreamReader;

public class BinningService implements HttpService<BinningServiceRequest> {
    public XMLStreamReader getXMLStreamReader(BinningServiceRequest o, boolean isNeedsCompleteFirstRow)
    throws Exception {
        Integer predictContextId = o.getContextId();
        Integer binCount = o.getBinCount();
        BIN_TYPE binType = o.getBinType();
        
        gov.usgswim.sparrow.request.BinningRequest request
            = new gov.usgswim.sparrow.request.BinningRequest(predictContextId, binCount, binType);
        
        BigDecimal[] bins = SharedApplication.getInstance().getDataBinning(request);
        BinningSerializer ser = new BinningSerializer(o, bins);
        
        return ser;
    }
    
    public void shutDown() {
    }
}
