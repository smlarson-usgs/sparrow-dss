package gov.usgswim.sparrow.service.metadata;

import static gov.usgswim.sparrow.util.SparrowResourceUtils.lookupModelID;
import static gov.usgswim.sparrow.service.ServiceResponseMimeType.*;
import static gov.usgswim.sparrow.service.ServiceResponseStatus.*;
import static gov.usgswim.sparrow.service.ServiceResponseOperation.*;

import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.domain.IPredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSession;
import gov.usgswim.sparrow.domain.PredefinedSessionBuilder;
import gov.usgswim.sparrow.domain.PredefinedSessionType;
import gov.usgswim.sparrow.request.PredefinedSessionRequest;
import gov.usgswim.sparrow.request.PredefinedSessionUniqueRequest;
import gov.usgswim.sparrow.service.ServiceResponseMimeType;
import gov.usgswim.sparrow.service.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * @author ilinkuo
 *
 */
public class SavedSessionService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	
	
	/** Boolean http param flag.  If true, only return the contextString
	 * from the GET operation.  To enable this option, pass 'true' (case 
	 * insensative).
	 */
	public static final String RETURN_CONTENT_ONLY_PARAM_NAME = "content_only";
	
	public static final String REQUESTED_MIME_TYPE_PARAM_NAME = "mime_type";
	
	public static final String XML_SUBMIT_PARAM_NAME = "xml_req";
	
	public static final String JSON_SUBMIT_PARAM_NAME = "json_req";

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



	/**
	 * Puts a new PredefinedSession in the db, or updates an existing.
	 */
	/*
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
			
			//sendXML(resp, strResponse.toString());
		}
	}
	*/


	@Override
	protected void doPut(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {

		ServiceResponseWrapper wrap = new ServiceResponseWrapper(
				IPredefinedSession.class, UPDATE);
		wrap.setStatus(FAIL);	//pessimistic...
		wrap.setMimeType(parseMime(req));
	
		Object entity = null;
		String xml = StringUtils.trimToNull(req.getParameter(XML_SUBMIT_PARAM_NAME));
		String json = StringUtils.trimToNull(req.getParameter(JSON_SUBMIT_PARAM_NAME));
		
		if (xml != null) {
			entity = getXMLXStream().fromXML(xml);
			wrap.setMimeType(XML);
		} else if (json != null) {
			entity = getJSONXStream().fromXML(xml);
			wrap.setMimeType(JSON);
		} else {
			wrap.setStatus(FAIL);
			wrap.setMessage("No request content found");
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
		wrap.setEntityId(updatedPredefinedSession.getId());
		wrap.setStatus(OK);
		sendResponse(resp, wrap);
	}
	
	public static PredefinedSessionRequest parseFilterSessionRequest(Map params) {

		PredefinedSessionRequest req = null;
		
		Long modelId = getLong(params, "modelId");
		Boolean approved = getBoolean(params, "approved");
		PredefinedSessionType type = getPredefinedSessionType(params, "type");
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
	
	protected void sendResponse(HttpServletResponse resp,
			ServiceResponseWrapper wrap) throws IOException {
		
		XStream xs = null;
		resp.setCharacterEncoding(wrap.getEncoding());
		resp.setContentType(wrap.getMimeType().toString());

		switch (wrap.getMimeType()) {
		case XML: 
			xs = getXMLXStream();
			break;
		case JSON:
			xs = getJSONXStream();
			break;
		default:
			throw new RuntimeException("Unknown MIMEType.");
		}
		
		xs.toXML(wrap, resp.getWriter());
	}
	

	
	protected ServiceResponseMimeType parseMime(HttpServletRequest req) {
		String mimeStr = StringUtils.trimToNull(
				req.getParameter(REQUESTED_MIME_TYPE_PARAM_NAME));
		
		ServiceResponseMimeType type = ServiceResponseMimeType.parse(mimeStr);
		
		if (type != null) {
			return type;
		} else {
			Enumeration heads = req.getHeaders("Accept");
			
			while (heads.hasMoreElements()) {
				String a = heads.nextElement().toString();
				type = ServiceResponseMimeType.parse(a);
				if (type != null) return type;
			}
			
		}
		
		//Couldn't find a type - use the default
		return XML;
	}
	
	/**
	 * Trims the extraPath to remove the leading slash and completely trims it
	 * to null.
	 * 
	 * @param req
	 * @return
	 */
	protected String cleanExtraPath(HttpServletRequest req) {
		String extraPath = StringUtils.trimToNull(req.getPathInfo());
		
		
		if (extraPath != null) {
			if (extraPath.startsWith("/")) {
				extraPath = StringUtils.trimToNull(extraPath.substring(1));
			}
		}
		
		return extraPath;
	}
	
	
	protected boolean parseBoolean(String value) {
		value = StringUtils.trimToNull(value);
		return ("true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value));
	}
	
	protected static XStream getJSONXStream() {
		XStream xs = new XStream(new JsonHierarchicalStreamDriver());
        xs.setMode(XStream.NO_REFERENCES);
        xs.processAnnotations(ServiceResponseWrapper.class);
        return xs;
	}
	
	protected static XStream getXMLXStream() {
		XStream xs = new XStream(new StaxDriver());
        xs.setMode(XStream.NO_REFERENCES);
        xs.processAnnotations(ServiceResponseWrapper.class);
        return xs;
	}
	
}
