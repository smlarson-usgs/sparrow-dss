package gov.usgswim.sparrow.service.binning;

import static gov.usgs.cida.sparrow.service.util.ServiceResponseOperation.CALCULATE;
import static gov.usgs.cida.sparrow.service.util.ServiceResponseStatus.FAIL;
import static gov.usgs.cida.sparrow.service.util.ServiceResponseStatus.OK;
import gov.usgs.cida.binning.domain.BinSet;
import gov.usgs.cida.binning.domain.BinType;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class BinningService extends AbstractSparrowServlet {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doActualGet(HttpServletRequest httpReq, HttpServletResponse resp)
			throws ServletException, IOException {

		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
				BinSet.class, CALCULATE);
		wrap.setStatus(FAIL); // pessimistic...
		wrap.setMimeType(parseMime(httpReq));

		Map params = httpReq.getParameterMap();

        BinningRequest binReq = null;
        Integer contextId = getInteger(params, "context-id");
        Integer binCount = getInteger(params, "bin-count");
        String binTypeStr = getClean(params, "bin-type");
        
        BinType binType = BinType.EQUAL_COUNT;
    
        if ("EQUAL_RANGE".equalsIgnoreCase(binTypeStr)) {
        	binType = BinType.EQUAL_RANGE;
        }
        
        if (contextId == null || binCount == null || binType == null) {
        	wrap.setMessage("One of the required parameters is missing or unrreadable.");
        	sendResponse(resp, wrap);
        	return;
        }
            
        binReq = new BinningRequest(contextId, binCount, binType);

		try {
			BinSet binSet = SharedApplication.getInstance().getDataBinning(binReq);
			
			if (binSet != null) {
				wrap.addEntity(binSet);
				wrap.setStatus(OK);
			} else {
				wrap.setMessage("An error occured processing the request");
			}
		} catch (Exception e) {
			wrap.setError(e);
			wrap.setMessage("Unable to retrieve PredefinedSession(s) from the db.");
		}


		sendResponse(resp, wrap);

	}

	@Override
	protected void doActualPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doActualGet(req, resp);
	}
	
}
