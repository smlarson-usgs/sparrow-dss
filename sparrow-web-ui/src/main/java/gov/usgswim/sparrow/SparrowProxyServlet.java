package gov.usgswim.sparrow;

import gov.usgs.cida.config.DynamicReadOnlyProperties;
import java.util.HashSet;
import java.util.Set;

import gov.usgs.cida.proxy.ProxyServlet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.ServletConfig;

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

	@Override
	protected String getConfigFromAnywhere(ServletConfig config, DynamicReadOnlyProperties props, String name, String defaultValue) {
		return props.getProperty(name, defaultValue);
	}

	@Override
	protected DynamicReadOnlyProperties initializeProperties(ServletConfig config) {
		Map<String, String> servletParamMap = readServletInitParams(config);
		Map<String, String> contextParamMap = new HashMap();
		
		//Popuplate the context params
		Enumeration contextParams = config.getServletContext().getInitParameterNames();
		while (contextParams.hasMoreElements()) {
			String name = contextParams.nextElement().toString();
			String val = config.getServletContext().getInitParameter(name);
			contextParamMap.put(name, val);
		}
		
		//Merge the two maps into contextParamMap.
		//Servlet specific params will override context params
		contextParamMap.putAll(servletParamMap);
		
		// DynamicReadOnlyProperties is used to perform any needed dynamic property substitution of ${} with nesting
		DynamicReadOnlyProperties props = new DynamicReadOnlyProperties(contextParamMap);
		props.addJNDIContexts(DynamicReadOnlyProperties.DEFAULT_JNDI_CONTEXTS);
		return props;
	}

	
}
