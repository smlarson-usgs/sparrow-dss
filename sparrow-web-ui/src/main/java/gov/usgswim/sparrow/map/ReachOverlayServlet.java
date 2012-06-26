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
import org.apache.commons.lang.StringUtils;

/**
 * This proxy servlet parses the incoming parameters to construct an XML request
 * which is forwarded to a MapViewer instance.  It is assumed that the web.xml
 * will properly map the servlet...
 *
 *
 *
 */
public class ReachOverlayServlet extends SparrowProxyServlet {

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
		float mapScale = getMapScale(request);

		if (mapScale > 1) {
		String xmlRequest =
						ServletUtil.getAnyFileWithSubstitutions(this.getClass(), "_large_scale", "xml", params);
		} else if (mapScale > .5) {
		String xmlRequest =
						ServletUtil.getAnyFileWithSubstitutions(this.getClass(), "_medium_scale", "xml", params);
		} else {
		String xmlRequest =
						ServletUtil.getAnyFileWithSubstitutions(this.getClass(), "_small_scale", "xml", params);
		}
		String xmlRequest =
						ServletUtil.getAnyFileWithSubstitutions(this.getClass(), "_context", "xml", params);

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

		//We add one randome number that can be used in assemling a
		//unique style name
		paramList.add("random");
		paramList.add(new Long(System.currentTimeMillis()));


		return paramList.toArray();
	}
	
	protected float getMapScale(HttpServletRequest request) throws Exception {
		String bboxStr = ServletUtil.getString(request.getParameterMap(), "BBOX", true);
		String widthStr = ServletUtil.getString(request.getParameterMap(), "width", true);
		
		double[] bbox = splitBoundsString(bboxStr);
		double pixelWidth = Double.parseDouble(widthStr);
		
		double xMapUnitsSize = Math.abs(bbox[0] - bbox[2]);
		double xScreenInchSize = pixelWidth / 96d; //MV assumes a screen resolution of 96
		double scale = xMapUnitsSize / xScreenInchSize;
		
		return (float) scale;
	}


	
	public static double[] splitBoundsString(String bounds) throws Exception {
		String[] boundArray = bounds.split(",");
		
		if (boundArray.length != 4) {
			throw new Exception("The bounds string should be of the form 'left,lower,right,upper'.");
		}
		
		double[] boundDArray = new double[4];
		
		boundDArray[0] = Double.parseDouble(StringUtils.trimToNull(boundArray[0]));
		boundDArray[1] = Double.parseDouble(StringUtils.trimToNull(boundArray[1]));
		boundDArray[2] = Double.parseDouble(StringUtils.trimToNull(boundArray[2]));
		boundDArray[3] = Double.parseDouble(StringUtils.trimToNull(boundArray[3]));
		
		return boundDArray;
	}
	
}
