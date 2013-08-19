package gov.usgswim.sparrow;

import java.net.URL;
import javax.servlet.http.HttpServletRequest;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author eeverman
 */
public class SparrowUtil {

	
	public static UrlFeatures getRequestUrlFeatures(HttpServletRequest request) throws Exception {
		 
				String proxiedUrl = request.getHeader("x-request-url");
				
				UrlFeatures features = new UrlFeatures();
//				String requestServerName = null;
//				String requestServerPort = null;
//				String contextPath = null;
				
				if (proxiedUrl != null && proxiedUrl.length() > 0) {
					URL url = new URL(proxiedUrl);
					String path = url.getPath();
					if (path.startsWith("/")) path = path.substring(1);
					int firstSlash = path.indexOf("/");
					if (firstSlash > -1) {
						path = path.substring(0, firstSlash);
					}
					
					features.contextPath = "/" + path;
					features.serverName = url.getHost();
					
					int port = url.getPort();
					
					if (port > 80) {
						features.intServerPort = port;
						features.serverPort = Integer.toString(port);
					} else {
						features.intServerPort = 80;
						features.serverPort = "";
					}
					
				} else {
					features.contextPath = request.getContextPath();
					features.serverName = request.getServerName();
					
					int port = request.getServerPort();
					if (port > 80) {
						features.intServerPort = port;
						features.serverPort = Integer.toString(port);
					} else {
						features.intServerPort = 80;
						features.serverPort  = "";
					}
					
				}
				
				return features;
	}
	
	public static class UrlFeatures {
		public String serverName;
		public String serverPort;
		public String contextPath;
		public int intServerPort;
		
		
		public String getBaseUrlWithoutSlash() {
			String serverToUse = "http://";
			if (intServerPort > 80) {
				serverToUse = serverToUse + serverName + ":" + serverPort + contextPath;
			} else {
				serverToUse = serverToUse + serverName + contextPath ;
			}
			return serverToUse;
		}
				
		public String getBaseUrlWithSlash() {
			return getBaseUrlWithoutSlash() + "/";
		}
	}
	
	/**
	 * Calculates a map scale for a given bounding box size and image size.
	 * From the MapViewer documentation:
	 * 
	 * <quote>
	 * Map scale is expressed as units in the user's data space that are represented
	 * by 1 inch on the screen or device. Map scale for MapViewer is actually the
	 * denominator value in a popular method of representing map scale as 1/n, where:
	 *  * 1, the numerator, is 1 unit (1 inch for MapViewer) on the displayed map.
	 *  * n, the denominator, is the number of units of measurement
	 * (for example, decimal degrees, meters, or miles) represented by 1 unit
	 * (1 inch for MapViewer) on the displayed map.
	 * </quote>
	 * 
	 * The method returns the 'n' value.  Thus, the returned 'n' values will
	 * be larger as the user zooms 'out' to see a larger area.
	 * 
	 * Note that MapViewer always assumes there are 96 pixels in an inch.
	 * 
	 * 
	 * @param bboxString A string containing a bbox (left, bottom, right, top)
	 * @param pixelWidthOrHeightString Number of pixels on a side (assuming the sides are the same)
	 * @return
	 * @throws Exception 
	 */
	public static float getMapScale(String bboxString, String pixelWidthOrHeightString) throws Exception {
		
		double[] bbox = splitBoundsString(bboxString);
		double pixelWidth = Double.parseDouble(pixelWidthOrHeightString);
		
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
