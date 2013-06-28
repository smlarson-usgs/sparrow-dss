package gov.usgswim.sparrow.service.help;

import gov.usgs.webservices.framework.utils.SmartXMLProperties;
import gov.usgswim.sparrow.util.SparrowResourceUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

// Specs:
// Take a field/item id and a model name
// Return the general info and the model-specific info

/**
 * @author ilinkuo
 *
 */
public class HelpService extends HttpServlet {
	protected static Logger log =
		Logger.getLogger(HelpService.class); //logging for this class
	
	private static final long serialVersionUID = 1L;
	private static final String HELP_RESPONSE= "<help-response model=\"%s\">"
	+ " <status>OK</status>"
	+ " <message></message>"
	+ "	<request-type>%s</request-type>"
	+ "	<params>"
	+ "		<param>%s</param>"
	+ "	</params>"
	+ "	%s"
	+ "</help-response>";
	private static final String ITEM="<item><![CDATA[%s]]></item>";
	private static final String KEYS="<keys>%s</keys>";
	private static final String KEY="<key>%s</key>"; // repeatable
	private static final String LIST="<list>%s</list>";

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
		String result = null;
		String type = null;
		String response = null;
		
		try {
			Long modelID = Long.parseLong(modelString);

			modelID = (modelID == null)? lookupModelID(modelString): modelID;
			if (modelID == null) {
				throw new ServletException("nonexistent model " + modelString);
			}
			//itemID =  (itemID == null)? lookupItem(modelID, itemString): itemID;


			String pathInfo = req.getPathInfo();


			if (pathInfo.contains("lookup")) {
				type = "lookup";
				result = lookupItem(modelID, itemString);
			} else if (pathInfo.contains("getSimpleKeys")) {
				type = "getSimpleKeys";
				result = getSimpleKeys(modelID);
			} else if (pathInfo.contains("getListKeys")) {
				type = "getListKeys";
				result = getListKeys(modelID);
			} else if (pathInfo.contains("getList")) {
				type = "getList";
				itemString = req.getParameter("listKey");
				result = getList(modelID, itemString);
			}
		} catch (Exception e) {
			response = String.format(HELP_RESPONSE,
					modelString,
					type,
					itemString,
					"<error>Error during request: " + e.getMessage() + "</error>"
				);
			log.error("Exception in HelpService", e);
		}

		response = String.format(HELP_RESPONSE,
				modelString,
				type,
				itemString,
				result
			);

		resp.setContentType("application/xml");
		PrintWriter out = resp.getWriter();
		out.write(response);
		out.flush();
	}

	private String lookupItem(Long modelID, String itemString) throws Exception {
		String result = SparrowResourceUtils.lookupMergedHelp(modelID, itemString, "div");
		return (result == null)? null: String.format(ITEM, result);
	}

	public String getSimpleKeys(Long modelId) throws Exception {
		SmartXMLProperties help = SparrowResourceUtils.retrieveModelHelp(modelId);
		return asKeyXML(help.keySet());
	}

	public String getListKeys(Long modelId) throws Exception {
		SmartXMLProperties help = SparrowResourceUtils.retrieveModelHelp(modelId);
		return asKeyXML(help.listKeySet());
	}

	private String asKeyXML(Set<String> set) {
		StringBuilder result = new StringBuilder();
		for (String key: set){
			result.append(String.format(KEY, key));
		}
		return (result.length() == 0)? null: String.format(KEYS, result);
	}

	public String getList(Long modelId, String key ) throws Exception {
		SmartXMLProperties help = SparrowResourceUtils.retrieveModelHelp(modelId);
		if (help.isListKey(key)) {
			String result = help.get(key);
			return (result == null)? null: String.format(LIST, result);
		}
		return null;
	}



	/**
	 * Is this ever used??
	 * @param modelString
	 * @return 
	 */
	public static Integer lookupModelID(String modelString) {
//		return SparrowResourceUtils.modelIndex.findFirst(0, modelString);
		return 0;
	}


}
