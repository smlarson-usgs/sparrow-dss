package gov.usgswim.sparrow.service.metadata;

import gov.usgswim.sparrow.util.SparrowResourceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/* Specs:
 * Take a model ID/name & session id/name
 * Return JSON object
 *
 * Take a model ID/name
 * Return list of associated sessions
 *
 * Implementation Note: Sessions are saved as model resources
 */

/**
 * @author ilinkuo
 *
 */
public class SavedSessionService extends HttpServlet {

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		String model = req.getParameter("model");
//		modelID =
//		String session = req.getParameter("session");
//
//		PrintWriter out = resp.getWriter();
//		if (model == null) {
//			out.write("model is a required parameter");
//			out.flush();
//			return;
//		}
//		if (session == null) {
//			model
//			// get
//			sessions = getSessions(model);
//		}
		// TODO Auto-generated method stub
		PrintWriter out = resp.getWriter();
		out.write("hello world");
		out.flush();
	}

	public Set<Object> getSessions(Long modelID) {
		Properties props = SparrowResourceUtils.loadResourceAsProperties(modelID, "sessions.properties");
		return props.keySet();
	}

	public Object getSession(Long modelID, String sessionNameOrId) {
		Properties props = SparrowResourceUtils.loadResourceAsProperties(modelID, "sessions.properties");
		return props.get(modelID.toString());
	}


}
