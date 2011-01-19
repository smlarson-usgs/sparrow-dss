package gov.usgswim.sparrow.service.metadata;

import static gov.usgswim.sparrow.util.SparrowResourceUtils.lookupModelID;

import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

/**
 * @author ilinkuo
 *
 */
public class SavedSessionService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	private static final String OPERATION_SAVE = "SAVE";
	private static final String OPERATION_DELETE = "DELETE";

	// ================
	// Instance Methods
	// ================
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		//GET returns a single PredefinedSession based either on a
		//unique key in the form context/servletpath/unique_key
		
		String code = req.getPathInfo();
		
		
		if (code != null && code.startsWith("/")) {
			code = code.substring(1);
		}
		
		code = StringUtils.trimToNull(code);
		
//		if (code == null) {
//			sendErrorResponse(resp, "No session code was found - use the form url/code to request", null);
//			return;
//		}
		
		if (code != null) {
		
			PredefinedSessionRequest psReq = new PredefinedSessionRequest(code);
			IPredefinedSession sess = null;
			try {
				sess = SharedApplication.getInstance().getPredefinedSessions(psReq).get(0);
			} catch (Exception e) {
				sendErrorResponse(resp, "Error fetching the session '" + code + "'", e);
				return;
			}
			
			if (sess != null) {
				resp.setContentType("text/json");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(sess.getContextString());
			} else {
				sendErrorResponse(resp, "PredefinedSession '" + code + "' was not found", null);
				return;
			}
			
		} else {
			
			Map params = req.getParameterMap();
			PredefinedSessionRequest psReq = parseParameters(params);
			
			StringBuilder strResponse = retrieveAllSavedSessionsXML(psReq);
			sendXML(resp, strResponse.toString());
		}
	}



	/**
	 * Puts a new PredefinedSession in the db, or updates an existing.
	 */
	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Map params = req.getParameterMap();
		PredefinedSessionBuilder s = new PredefinedSessionBuilder();
		
		//s.setId(getLong(params, "id"));	//I don't think we want this set
		s.setUniqueCode(getClean(params, "code"));
		s.setModelId(getLong(params, "modelId"));
		s.setPredefinedSessionType(getPredefinedSessionType(params, "type"));
		s.setApproved(getBoolean(params, "approved"));
		
		s.setName(getClean(params, "name"));
		s.setDescription(getClean(params, "description"));
		s.setSortOrder(getInteger(params, "sort_order"));
		s.setContextString(getClean(params, "context_string"));
		//s.setAddDate();	//date is not populated from UI
		s.setAddBy(getClean(params, "add_by"));
		s.setAddNote(getClean(params, "add_note"));
		s.setAddContactInfo(getClean(params, "add_contact_info"));
		s.setGroupName(getClean(params, "group_name"));
		
		try {
			IPredefinedSession savedS =
				SharedApplication.getInstance().savePredefinedSession(s);
			
			if (savedS != null) {
				String strResponse = Action.getTextWithParamSubstitution("ResponseOK", this.getClass(),
						"SessionId", savedS.getUniqueCode(), "ModelId", savedS.getModelId(),
						"Operation", OPERATION_SAVE, "db-id", savedS.getId().toString());
				
				resp.setContentType("text/xml");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(strResponse);
			} else {
				String strResponse = Action.getTextWithParamSubstitution("ResponseFail", this.getClass(),
						"Operation", OPERATION_SAVE, "Message", "Could not save the session");
				
				resp.setContentType("text/xml");
				resp.setCharacterEncoding("UTF-8");
				resp.getWriter().write(strResponse);
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			String strResponse = Action.getTextWithParamSubstitution("ResponseFail", this.getClass(),
					"Operation", OPERATION_SAVE, "Message", "Error while saving the session");
			
			sendXML(resp, strResponse.toString());
		}
	}



	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		
		doPut(req, resp);
	}
	
	public static PredefinedSessionRequest parseParameters(Map params) {

		PredefinedSessionRequest req = null;
		
		Long modelId = getLong(params, "modelId");
		Boolean approved = getBoolean(params, "approved");
		PredefinedSessionType type = getPredefinedSessionType(params, "type");
		String groupName = getClean(params, "groupName");
		String uniqueCode = getClean(params, "uniqueCode");
		
		if (uniqueCode != null) {
			req = new PredefinedSessionRequest(uniqueCode);
		} else {
			req = new PredefinedSessionRequest(modelId, approved, type, groupName);
		}
		
		return req;
	}
	
	/**
	 * Returns a single, cleaned value from a parameter map.
	 * 
	 * The value is trimmed to null.
	 * 
	 * @param params
	 * @param key
	 */
	private static String getClean(Map params, String key) {
		Object v = params.get(key);
		if (v != null) {
			String[] vs = (String[]) v;
			if (vs.length > 0) {
				return StringUtils.trimToNull(vs[0]);
			}
		}
		return null;
	}
	
	private static Long getLong(Map params, String key) {
		String s = getClean(params, key);
		if (s != null) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}
	
	private static Integer getInteger(Map params, String key) {
		String s = getClean(params, key);
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}
	
	private static Boolean getBoolean(Map params, String key) {
		String s = getClean(params, key);
		if (s != null) {
			return ("T".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s));
		}
		return null;
	}
	
//	private static Date getDate(Map params, String key) {
//		String s = getClean(params, key);
//		if (s != null) {
//			
//			return ("T".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s));
//		}
//		return null;
//	}
	
	private static PredefinedSessionType getPredefinedSessionType(Map params, String key) {
		PredefinedSessionType type = null;
		try {
			String t = getClean(params, key);
			type = PredefinedSessionType.valueOf(t);
		} catch (Exception e) {
			//OK if not specified
		}
		return type;
	}
	
	public static StringBuilder retrieveAllSavedSessionsXML(PredefinedSessionRequest request) throws ServletException {
		StringBuilder result = new StringBuilder();
		List<IPredefinedSession> results;
		try {
			results = SharedApplication.getInstance().getPredefinedSessions(request);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		

		result.append("<sessions>\n");
		for (IPredefinedSession entry : results) {
			result.append("  <session key=\"" + entry.getUniqueCode() + "\">\n");
			result.append("    <name>" + entry.getName() + "</name>\n");
			result.append("    <description>" + entry.getDescription() + "</description>\n");
			result.append("    <groupName>" + entry.getGroupName() + "</groupName>\n");
			result.append("    <type>" + entry.getPredefinedSessionType().name() + "</type>\n");
			result.append("  </session>\n");
		}
		result.append("</sessions>");

		return result;
	}
	
	private static void sendErrorResponse(HttpServletResponse resp, String msg, Exception cause) {
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
		resp.setContentType("text/plain");
		try {
			resp.getWriter().println(msg);
			if (cause != null) {
				resp.getWriter().println("Cause: " + cause.getMessage());
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void sendXML(HttpServletResponse resp, String xml) throws IOException {
		resp.setContentType("text/xml");
		resp.setCharacterEncoding("UTF-8");
		resp.getWriter().write(xml);
	}

}
