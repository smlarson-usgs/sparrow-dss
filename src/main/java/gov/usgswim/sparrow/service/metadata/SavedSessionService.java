package gov.usgswim.sparrow.service.metadata;

import static gov.usgswim.sparrow.service.ServiceResponseMimeType.JSON;
import static gov.usgswim.sparrow.service.ServiceResponseMimeType.XML;
import static gov.usgswim.sparrow.service.ServiceResponseOperation.CREATE;
import static gov.usgswim.sparrow.service.ServiceResponseOperation.GET;
import static gov.usgswim.sparrow.service.ServiceResponseOperation.UPDATE;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.FAIL;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.OK;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.UNKNOWN;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.request.PredefinedSessionUniqueRequest;
import gov.usgswim.sparrow.service.AbstractSparrowServlet;
import gov.usgswim.sparrow.service.ServiceResponseMimeType;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.ServletResponseParser;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;


/**
 * @author ilinkuo
 *
 */
public class SavedSessionService extends AbstractSparrowServlet {

	private static final long serialVersionUID = 1L;
	
	
	/** Boolean http param flag.  If true, only return the contextString
	 * from the GET operation.  To enable this option, pass 'true' (case 
	 * insensative).
	 */
	public static final String RETURN_CONTENT_ONLY_PARAM_NAME = "content_only";
	
	/**
	 * The GET method operates in two modes:
	 * context/uniqueCode returns the json content of the predefined session.
	 * context?params... returns a list of predefined sessions (names, descriptions,
	 * etc) w/o the actual json content.  The params are basically filter criteria,
	 * which could be a uniqueCode (thus filtering to a single record).
	 * 
	 * For cases where a uniqueCode is specified, the RETURN_CONTENT_ONLY_PARAM_NAME
	 * parameter can be passed as 'true', which will return only the JSON
	 * content of the predefinedSession.
	 * 
	 * 
	 */
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//GET returns a single PredefinedSession based either on a
		//unique key in the form context/servletpath/unique_key
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
					IPredefinedSession.class, GET);
		wrap.setStatus(FAIL);	//pessimistic...
		wrap.setMimeType(parseMime(req));
		
		//Special parameters
		boolean contentOnly = parseBoolean(req.getParameter(RETURN_CONTENT_ONLY_PARAM_NAME));
		String extraPath = cleanExtraPath(req);
		
		//A request for the PS that will be built up based on criteria
		PredefinedSessionRequest psReq = null;
		PredefinedSessionUniqueRequest psUniqueReq = null;
		
		//List of sessions to be returned
		List<IPredefinedSession> sesss = null;
		


		if (extraPath != null) {
			//we have extra path, so this is a request for a single record
			//identified by an ID.
			try {
				Long id = Long.parseLong(extraPath);
				psUniqueReq = new PredefinedSessionUniqueRequest(id);
			} catch (Exception e) {
				wrap.setMessage("Could not parse requested id '" + extraPath + "'");
				sendResponse(resp, wrap);
				return;
			}
		} else {
			//Parse all params into full criteria request
			psReq = parseFilterSessionRequest(req.getParameterMap());
			psUniqueReq = parseUniqueSessionRequest(req.getParameterMap());
		}
		
		
		
		try {
			if (psUniqueReq != null && psUniqueReq.isPopulated()) {
				sesss = SharedApplication.getInstance().getPredefinedSessions(psUniqueReq);
			} else {
				sesss = SharedApplication.getInstance().getPredefinedSessions(psReq);
			}
		} catch (Exception e) {
			wrap.setError(e);
			wrap.setMessage("Unable to retrieve PredefinedSession(s) from the db.");

			sendResponse(resp, wrap);
			return;
		}
		
		
		if (sesss.size() == 1 && contentOnly) {

			//This is a special request for only the JSON content of a BLOB
			//field that contains the JSON-ified version of a serialized
			//client state.  It must be encoded as UTF-8.  I think that is
			//a requirement...
			wrap.setMimeType(JSON);
			wrap.setEncoding("UTF-8");
			if (sesss != null && sesss.size() == 1) {
				resp.setContentType(wrap.getMimeType().toString());
				resp.setCharacterEncoding(wrap.getEncoding());
				resp.getWriter().write(sesss.get(0).getContextString());
				return;
			} else {
				wrap.setMessage("Could not find any records matching request.");
				sendResponse(resp, wrap);
				return;
			}

		} else {
			wrap.addAllEntities(sesss);
			wrap.setStatus(OK);
			if (sesss.size() == 1) {
				wrap.setEntityId(sesss.get(0).getId());
			}
			sendResponse(resp, wrap);
		}
	}


	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		doPut(req, resp);
	}
	
	
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
				IPredefinedSession.class, UPDATE);
		wrap.setStatus(FAIL);	//pessimistic...
	
		ServletResponseParser requestContent = new ServletResponseParser(req);
		Object entity = requestContent.getAsObject();
		
		if (requestContent.getErrorMessage() == null) {
			wrap.setMimeType(requestContent.getType());
		} else {
			wrap.setMessage(requestContent.getErrorMessage());
			sendResponse(resp, wrap);
			return;
		}
		
		
		IPredefinedSession postedPredefinedSession = (IPredefinedSession)entity;
		
		if (postedPredefinedSession.getId() == null) {
			wrap.setOperation(CREATE);
		}
		
		IPredefinedSession updatedPredefinedSession = null;
		
		try {
			updatedPredefinedSession =
				SharedApplication.getInstance().savePredefinedSession(postedPredefinedSession);
		} catch (Exception e) {
			wrap.setMessage("Could not create or update the PredefinedSession");
			wrap.setError(e);
			sendResponse(resp, wrap);
			return;
		}
		
		//send back w/ a wrapper
		wrap.addEntity(updatedPredefinedSession);
		wrap.setEntityId((updatedPredefinedSession==null) ? null : updatedPredefinedSession.getId());
		wrap.setStatus(OK);
		sendResponse(resp, wrap);
	}
	
	public static PredefinedSessionRequest parseFilterSessionRequest(Map params) {

		PredefinedSessionRequest req = null;
		
		Long modelId = getLong(params, "modelId");
		Boolean approved = getBoolean(params, "approved");
		PredefinedSessionType type = parsePredefinedSessionType(params, "type");
		String groupName = getClean(params, "groupName");
		
		req = new PredefinedSessionRequest(modelId, approved, type, groupName);

		return req;
	}
	
	public static PredefinedSessionUniqueRequest parseUniqueSessionRequest(Map params) {

		PredefinedSessionUniqueRequest req = null;
		
		Long id = getLong(params, "id");
		String uniqueCode = getClean(params, "uniqueCode");
		
		if (uniqueCode != null) {
			req = new PredefinedSessionUniqueRequest(uniqueCode);
		} else if (id != null) {
			req = new PredefinedSessionUniqueRequest(id);
		} else {
			req = new PredefinedSessionUniqueRequest();
		}
		
		return req;
	}
	
	private static PredefinedSessionType parsePredefinedSessionType(Map params, String key) {
		PredefinedSessionType type = null;
		try {
			String t = getClean(params, key);
			type = PredefinedSessionType.valueOf(t);
		} catch (Exception e) {
			//OK if not specified
		}
		return type;
	}
	
}
