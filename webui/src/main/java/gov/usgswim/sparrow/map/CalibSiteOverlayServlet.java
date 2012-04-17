package gov.usgswim.sparrow.map;

import gov.usgs.cida.proxy.ProxyServlet;
import gov.usgswim.sparrow.SparrowProxyServlet;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This proxy servlet parses the incoming parameters to construct an XML request
 * which is forwarded to a MapViewer instance.  It is assumed that the web.xml
 * will properly map the servlet...
 *
 *
 *
 */
public class CalibSiteOverlayServlet extends SparrowProxyServlet {

	private static final long serialVersionUID = 1L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
		doPost(request, response);
	}


	@Override
	public Map<String, String> getCustomParameters(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();

		try {
			params.put("xml_request", buildMapViewerRequest(request));
		} catch (Exception e) {
			this.log("Unable to create the mapviewer XML request string", e);
			throw new RuntimeException(e);
		}

		return params;
	}

	/**
	 * Generates a MapViewer request XML for a map tile. The request is based on
	 * fields submitted in the HTTP request.
	 *
	 * @param request
	 *            The HTTP request containing the map tile parameters.
	 * @return A MapViewer request XML.
	 * @throws IOException
	 */
	public String buildMapViewerRequest(HttpServletRequest request) throws Exception {

		Object[] params = parseParams(request);

		String xmlRequest =
						ServletUtil.getAnyFileWithSubstitutions(this.getClass(), null, "xml", params);

		return xmlRequest;

	}

	public Object[] parseParams(HttpServletRequest request) throws Exception {
		HashMap<String, Object> map = new HashMap<String, Object>();
		Map params = request.getParameterMap();
		ArrayList<Object> paramList = new ArrayList<Object>(5);

		paramList.add("model_id");
		paramList.add(ServletUtil.getLong(params, "model_id", true));

		paramList.add("BBOX");
		paramList.add(ServletUtil.getString(params, "BBOX", true));

		paramList.add("width");
		paramList.add(ServletUtil.getDouble(params, "width", true));

		paramList.add("height");
		paramList.add(ServletUtil.getDouble(params, "height", true));


		return paramList.toArray();
	}

}
