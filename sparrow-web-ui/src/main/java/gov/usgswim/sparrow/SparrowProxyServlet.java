package gov.usgswim.sparrow;

import java.util.HashSet;
import java.util.Set;

import gov.usgs.cida.proxy.ProxyServlet;

public class SparrowProxyServlet extends ProxyServlet {
	private HashSet<String> ignoredHeaderNames;
	
	public static final String HTTP_SET_COOKIE_NAME = "set-cookie";
	
	/**
	 * Override to 
	 */
	@Override
	protected Set<String> getIgnoredHeaders() {
		if (ignoredHeaderNames == null) {
			Set<String> superSet = super.getIgnoredHeaders();
			ignoredHeaderNames = new HashSet<String>(7);
			ignoredHeaderNames.addAll(superSet);

			//Two 'real' cookie headers
			ignoredHeaderNames.add(HTTP_COOKIE_NAME);
			ignoredHeaderNames.add(HTTP_SET_COOKIE_NAME);
		}

		return ignoredHeaderNames;
	}

	
}
