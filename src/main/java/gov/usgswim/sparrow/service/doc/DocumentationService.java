package gov.usgswim.sparrow.service.doc;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;

// Specs:
// Take a field/item id and a model name
// Return the general info and the model-specific info

/**
 * @author ilinkuo
 *
 */
public class DocumentationService extends HttpServlet {

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
