package gov.usgswim.sparrow.service.help;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// Specs:
// Take a field/item id and a model name
// Return the general info and the model-specific info

/**
 * @author ilinkuo
 *
 */
public class HelpService extends HttpServlet {

	private static final long serialVersionUID = 1L;
	protected static Map<Integer, Object>modelMetadata = new HashMap<Integer, Object>();

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
		modelID =  (modelID == null)? lookupModelID(modelString): modelID;
		if (modelID == null) {
			throw new ServletException("nonexistent model " + modelString);
		}
		itemID =  (itemID == null)? lookupItem(modelID, itemString): itemID;
		// TODO Auto-generated method stub
		PrintWriter out = resp.getWriter();
		out.write("hello world");
		out.flush();
	}

	private Integer lookupItem(Integer modelID, String itemString) {
//		modelInfo = modelMetadata.
		return null;
	}

	private void retrieveModelMetadata(Integer id) {
//		Object mmd = modelMetadata.get(id);
//		if (mmd == null) {
//			D
//		}
	}

	public static Integer lookupModelID(String modelString) {
//		return SparrowResourceUtils.modelIndex.findFirst(0, modelString);
		return 0;
	}

	public static Integer parseInt(String value) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException e) {
			return null;
		}
	}


}
