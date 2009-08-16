package gov.usgswim.sparrow.service.metadata;

import java.io.IOException;
import java.io.PrintWriter;

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
		// TODO Auto-generated method stub
		PrintWriter out = resp.getWriter();
		out.write("hello world");
		out.flush();
	}
}
