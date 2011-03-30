package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.ServiceResponseOperation.GET;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.FAIL;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.OK;
import gov.usgswim.sparrow.domain.HUC;
import gov.usgswim.sparrow.domain.ReachGeometry;
import gov.usgswim.sparrow.request.HUCRequest;
import gov.usgswim.sparrow.request.ReachID;

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
public class ReachWatershedService extends AbstractSparrowServlet {

	private static final long serialVersionUID = 1L;
	
	public static final String REACH_ID_PARAM_NAME = "reach_id";
	public static final String MODEL_ID_PARAM_NAME = "model_id";
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//GET returns a single PredefinedSession based either on a
		//unique key in the form context/servletpath/unique_key
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
					ReachGeometry.class, GET);
		wrap.setStatus(FAIL);	//pessimistic...
		wrap.setMimeType(parseMime(req));
		

		ReachID reachReq = null;
		ReachGeometry result = null;
		
		String extraPath = cleanExtraPath(req);
		

		Long id = getLong(req.getParameterMap(), REACH_ID_PARAM_NAME);
		Long modelId = getLong(req.getParameterMap(), MODEL_ID_PARAM_NAME);
		
		if (id == null || modelId == null) {
			wrap.setMessage("Missing  'reach_id' and/or 'model_id' parameter(s)");
			sendResponse(resp, wrap);
		} else {
		
		
			//Try to load
			try {
				reachReq = new ReachID(modelId, id);
				result = (ReachGeometry) ConfiguredCache.ReachWatershed.get(reachReq, false);
			} catch (Exception e) {
				wrap.setError(e);
			}
			
			
			//Send response
			if (result != null) {
				wrap.addEntity(result);
				wrap.setStatus(OK);
				wrap.setEntityId(result.getId());	//Need to open this up for strings
	
				sendResponse(resp, wrap);
			} else {
				wrap.setMessage("Unable to retrieve the reach '" + reachReq + "', model '" + modelId + "'");
				sendResponse(resp, wrap);
			}
		}


	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doGet(req, resp);
	}
	
	
}
