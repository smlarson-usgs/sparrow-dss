package gov.usgswim.sparrow.map;

import gov.usgswim.sparrow.ServletUtil;
import gov.usgswim.sparrow.SparrowProxyServlet;
import gov.usgswim.sparrow.SparrowUtil;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.NumberUtils;

/**
 *
 *
 */
public class MapRequestServlet extends SparrowProxyServlet {

	private static final long serialVersionUID = 393203465650319560L;

	@Override
	public void doGet(HttpServletRequest request, HttpServletResponse response)
					throws ServletException, IOException {
		doPost(request, response);
	}

	@Override
	public Map<String, String> getCustomParameters(HttpServletRequest request) {
		Map<String, String> params = new HashMap<String, String>();
		String servlet_path = request.getServletPath();

		// see if request is for map image or legend image
		if ("/getMap".equals(servlet_path)) {
			try {
				params.put("xml_request", buildMapViewerRequest(request));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				this.log("Unable to create the request string", e);
			}
		} else if ("/getLegend".equals(servlet_path)) {
			params.put("xml_request", buildMapViewerLegendRequest(request));
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
		// Mapping data parameters
		String modelId = StringUtils.trimToNull(request.getParameter("model_id"));
		String contextId = StringUtils.trimToNull(request.getParameter("context_id"));
		
		//These two params are used to create the name of the predefined theme to use:
		//	[what_to_map]_[theme_name]
		//Using a separate theme name (instead of just a model ID) allows models
		//to share the same geometry.
		String themeName = StringUtils.trimToNull(request.getParameter("theme_name"));
		String whatToMap = StringUtils.trimToNull(request.getParameter("what_to_map"));
		String fullThemeName = whatToMap + "_" + themeName;
		fullThemeName = fullThemeName.toLowerCase();

		// Map tile boundary coordinates and image size
		String bBox = StringUtils.trimToNull(request.getParameter("BBOX"));
		String width = StringUtils.trimToNull(request.getParameter("width"));
		String height = StringUtils.trimToNull(request.getParameter("height"));
		
		float mapScale = SparrowUtil.getMapScale(bBox, width);
		
		//check values
		if (modelId == null && contextId == null) {
			bomb("model_id or context_id must be specified.");
		}
		
		if (modelId != null && ! NumberUtils.isDigits(modelId)) {
			bomb("model_id must be a number");
		}
		
		if (contextId != null && ! NumberUtils.isNumber(contextId)) {
			bomb("context_id must be a number");
		}
		
		if (themeName == null || whatToMap == null) {
			bomb("theme_name and what_to_map are required.");
		}
		
		if (! ("reach".equalsIgnoreCase(whatToMap) || "catch".equalsIgnoreCase(whatToMap))) {
			bomb("what_to_map must be 'reach' or 'catch'");
		}
		
		if (bBox == null) { bomb("bbox must not be null"); }
		
		if (! NumberUtils.isNumber(width) || ! NumberUtils.isNumber(height)) {
			bomb("width and height are required and must be numbers");
		}
		

		StringBuilder xmlreq = new StringBuilder("");
		xmlreq.append("<?xml version=\"1.0\" standalone=\"yes\"?>\n");
		
		/*
		 * Note:  format options for below are:
		 * GIF|GIF_URL|GIF_STREAM|JAVA_IMAGE|
		 * PNG_STREAM|PNG_URL|PNG8_STREAM|PNG8_URL|
		 * JPEG_STREAM|JPEG_URL|
		 * PDF_STREAM|PDF_URL| 
		 * SVG_STREAM|SVGZ_STREAM|SVGTINY_STREAM| SVG_URL|SVGZ_URL|SVGTINY_URL
		 */
		xmlreq.append("<map_request datasource=\"sparrow\" srid=\"8307\" width=\""
						+ width
						+ "\" height=\""
						+ height
						+ "\" transparent=\"true\"\n"
						+ "	bgcolor=\"#ffffff\" antialiase=\"false\" keepthemesorder=\"true\" format=\"PNG8_STREAM\">\n");
		xmlreq.append("<box><coordinates>" + bBox + "</coordinates></box>\n");


		xmlreq.append("<ns_data_provider provider_id=\"sparrowPredict\" time_out=\"200000\">\n");
		xmlreq.append("<parameters>");

		// if contextID is available, use that. Otherwise, use modelID
		if (contextId != null) {
			xmlreq.append("<parameter name=\"context-id\">" + contextId + "</parameter>");
		} else {
			xmlreq.append("<parameter name=\"model-id\">" + modelId + "</parameter>");
		}
		xmlreq.append("</parameters>\n");

		// Rendering parameters
		xmlreq.append(" <for_theme name=\"" + fullThemeName + "\" />\n");

		String renderStyle = request.getSession().getId();

		xmlreq.append("<custom_rendering_style name=\"" + renderStyle + "\" />\n");
		xmlreq.append("<join spatial_key_column=\"identifier\" />\n");
		xmlreq.append("</ns_data_provider>\n");
		
		xmlreq.append("<themes>\n");
		xmlreq.append("<theme name=\"" + fullThemeName + "\"></theme>");
		xmlreq.append("	</themes>\n<styles>\n");
		xmlreq.append(buildCustomRenderStyle(request, mapScale));
		xmlreq.append("</styles>\n</map_request>");
 
		return xmlreq.toString();
	}

	/**
	 *
	 * @param request
	 * @return
	 */
	public String buildMapViewerLegendRequest(HttpServletRequest request) {
		String renderStyle = request.getSession().getId();

		String title = "Sparrow Legend";

		String mapreq = "<?xml version=\"1.0\" standalone=\"yes\"?>"
						+ " <map_request "
						+ "     datasource=\"sparrow\" "
						+ "     bgcolor=\"#ffffff\" "
						+ "     antialiase=\"true\" "
						+ "     format=\"PNG8_STREAM\">"
						+ "   <legend "
						+ "       bgstyle=\"fill:#fffff8;fill-opacity:255;stroke:#ffffff\" "
						+ "       position=\"SOUTH_WEST\" "
						+ "       profile=\"MEDIUM\">" + "     <column>"
						+ "       <entry is_title=\"true\" text=\"" + title + "\"/>"
						+ "       <entry style=\"" + renderStyle + "\"/>" + "     </column>"
						+ "   </legend>";
		mapreq += "<styles>";
		mapreq += buildCustomRenderStyle(request, null);
		mapreq += "</styles>";
		mapreq += "</map_request>";

		return mapreq;
	}

	/**
	 * Generates XML style definitions for a MapViewer map tile request based on
	 * the list of bins and bin colors specified in the request.
	 *
	 * @param request The HTTP request containing the binning parameters.
	 * @return A style definition XML suitable for inclusion in a MapViewer
	 *         map request.
	 */
	protected String buildCustomRenderStyle(HttpServletRequest request, Float mapScale) {
		StringBuilder styleXml = new StringBuilder();
		
		String lineThickness = "5.0";
		if (mapScale != null) {
			if (mapScale > 4) {
				lineThickness = ".4";
			} else if (mapScale > 2) {
				lineThickness = ".5";
			} else if (mapScale > 1) {
				lineThickness = "1.0";
			} else if (mapScale > .5) {
				lineThickness = "1.5";
			} else if (mapScale > .25) {
				lineThickness = "2.0";
			} else if (mapScale > .12) {
				lineThickness = "2.5";
			} else if (mapScale > .06) {
				lineThickness = "3.0";
			} else {
				lineThickness = "4.0";
			}
			//System.out.println("Render thickness=" + lineThickness + " scale=" + mapScale);
		}


		// Get the user's session id for unique style naming
		String sessionId = request.getSession().getId();
		String[] binLowList = request.getParameter("binLowList").split(",");
		String[] binHighList = request.getParameter("binHighList").split(",");
		String[] binColorList = request.getParameter("binColorList").split(",");
		String whatToMap = request.getParameter("what_to_map");

		// Start advanced style def
		styleXml.append("<style name=\"" + sessionId + "\">\n");
		styleXml.append("<AdvancedStyle>\n");
		styleXml.append("<BucketStyle>\n");
		styleXml.append("<Buckets>\n");

		// Create a bucket for each bin range and tie it to a basic style/color
		for (int i = 0; i < binLowList.length; i++) {
			String low = binLowList[i];
			String high = binHighList[i];
			String styleName = sessionId + i;
			styleXml.append("<RangedBucket low=\"" + low + "\" high=\"" + high + "\" style=\"" + styleName + "\" />\n");
		}

		// End advanced style def
		styleXml.append("</Buckets>\n</BucketStyle>\n");
		styleXml.append("</AdvancedStyle>\n</style>\n");

		// Create an individual style for each bucket color
		for (int i = 0; i < binColorList.length; i++) {
			String color = "#" + binColorList[i];
			String styleName = sessionId + i;
			styleXml.append("<style name=\"" + styleName + "\">\n");

			if ("reach".equals(whatToMap)) {
				styleXml.append("<g class=\"line\" style=\"stroke-width:" + lineThickness + ";fill:" + color + ";\">\n");
				styleXml.append("<line class=\"base\" style=\"stroke-width:" + lineThickness + ";fill:" + color + ";\" />\n");
				styleXml.append("</g>\n");
			} else {
				styleXml.append("<g class=\"color\" style=\"stroke:" + color + ";fill:" + color + "\" />\n");
			}
			styleXml.append("</style>\n");
		}

		return styleXml.toString();
	}
	
	private void bomb(String message) {
		throw new RuntimeException("Could not generate map tile request: " + message);
	}

}
