package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.ServiceResponseOperation.GET;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.FAIL;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.OK;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.request.HUCRequest;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Accepts GET or POST requests for a HUC using the parameter name 'huc' to
 * indicate which HUC.
 * 
 * @author eeverman
 */
public class HUCService extends AbstractSparrowServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String HUC_PARAM_NAME = "huc";
	
	@Override
	protected void doActualGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//GET returns a single PredefinedSession based either on a
		//unique key in the form context/servletpath/unique_key
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
					HUC.class, GET);
		wrap.setStatus(FAIL);	//pessimistic...
		wrap.setMimeType(parseMime(req));
		

		HUCRequest hucReq = null;
		HUC result = null;
		String hucId = null;
		
		String extraPath = cleanExtraPath(req);
		
		
		//Build the HUCRequest
		if (extraPath != null) {
			hucId = extraPath;
			hucReq = new HUCRequest(hucId);
		} else if (getClean(req.getParameterMap(), HUC_PARAM_NAME) != null) {
			hucId = getClean(req.getParameterMap(), HUC_PARAM_NAME);
			hucReq = new HUCRequest(hucId);
		}
		
		
		//Try to load the HUC
		try {
			if (hucReq != null) {
				result = SharedApplication.getInstance().getHUC(hucReq);
			}
		} catch (Exception e) {
			wrap.setError(e);
		}
		
		
		//Send response
		if (result != null) {
			wrap.addEntity(result);
			wrap.setStatus(OK);
			wrap.setEntityId(null);	//Need to open this up for strings

			sendResponse(resp, wrap);
		} else {
			wrap.setMessage("Unable to retrieve the HUC '" + hucId + "'");

			sendResponse(resp, wrap);
		}


	}


	@Override
	protected void doActualPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doActualGet(req, resp);
	}
	
	
}
