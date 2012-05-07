package gov.usgswim.sparrow;

import java.net.URL;
import javax.servlet.http.HttpServletRequest;

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
}
