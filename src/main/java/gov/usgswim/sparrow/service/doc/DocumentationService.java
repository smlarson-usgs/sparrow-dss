package gov.usgswim.sparrow.service.doc;

import gov.usgswim.sparrow.util.DataResourceLoader;

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
		String modelString = req.getParameter("model");
		String itemString = req.getParameter("item");
		Integer modelID = parseInt(modelString);
		Integer itemID = parseInt(itemString);
		modelID =  (modelID == null)? lookup(modelString): modelID;
		itemID =  (itemID == null)? lookup(modelID, itemString): itemID;
		// TODO Auto-generated method stub
		PrintWriter out = resp.getWriter();
		out.write("hello world");
		out.flush();
	}

	private Integer lookup(Integer modelID, String itemString) {
		// TODO Auto-generated method stub
		return null;
	}

	public static Integer lookup(String modelString) {
		return DataResourceLoader.modelIndex.findFirst(0, modelString);
	}

	public static Integer parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}


}
