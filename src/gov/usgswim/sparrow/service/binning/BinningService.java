package gov.usgswim.sparrow.service.binning;

import gov.usgswim.service.HttpService;
import gov.usgswim.sparrow.cachefactory.BinningRequest.BIN_TYPE;
import gov.usgswim.sparrow.service.SharedApplication;

import javax.xml.stream.XMLStreamReader;

public class BinningService implements HttpService<BinningRequest> {
    public XMLStreamReader getXMLStreamReader(BinningRequest o, boolean isNeedsCompleteFirstRow)
    throws Exception {
        Integer predictContextId = o.getContextId();
        Integer binCount = o.getBinCount();
        BIN_TYPE binType = o.getBinType();
        
        gov.usgswim.sparrow.cachefactory.BinningRequest request
            = new gov.usgswim.sparrow.cachefactory.BinningRequest(predictContextId, binCount, binType);
        
        double[] bins = SharedApplication.getInstance().getDataBinning(request);
        BinningSerializer ser = new BinningSerializer(o, bins);
        
        return ser;
    }
    
    public void shutDown() {
    }
}
