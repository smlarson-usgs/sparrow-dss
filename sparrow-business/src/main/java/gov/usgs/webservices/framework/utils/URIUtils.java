package gov.usgs.webservices.framework.utils;

import gov.usgs.webservices.framework.logging.LoggingUtils;

public abstract class URIUtils {
	public static final char SEPARATOR = '/';
	
	
	/**
	 * Check (approximately) that the argument is like that returned from HttpServlet.getRequestURI
	 * @param uri
	 */
	public static void checkURIFormat(String uri) {
		if (uri.indexOf('?') > -1 || uri.indexOf(' ') > -1 || !uri.startsWith("/")) {
			throw new IllegalArgumentException("[" + uri 
					+ "] is not in the format of a uri returned from HttpServlet.getRequestURI().");
		}
	}

	public static String parseQueryString(String requestURI) {
		int index = requestURI.indexOf('?');
		if ( index > -1) {
			String result =
			requestURI.substring(index + 1, requestURI.length());
			return result;
		}
		return "";
	}
	
	public static String parseHandler(String requestURI) {
		return requestURI.substring(requestURI.lastIndexOf("/") + 1);
	}
	
	/**
	 * @param uri
	 * @param servletPath
	 * @return whatever req.getPathInfo returns if the req.getRequestURI = uri
	 *         and req.getServletPath = servlet
	 */
	public static String computePathInfo(String uri, String servletPath) {
		uri = (uri == null || uri.length() == 0)? "/": uri;
		servletPath = (servletPath == null || servletPath.length() == 0)? "/": servletPath;
		if (uri.charAt(0) != '/') {
			uri = '/' + uri;
		}
		if (servletPath.charAt(0) != '/') {
			servletPath = '/' + servletPath;
		}
		//
		assert uri.charAt(0) == '/' : "uri has been massaged to begin with /";
		assert servletPath.charAt(0) == '/' : "servletPath has been massaged to begin with /";
		//
		int index = uri.lastIndexOf(servletPath);
		if (index >= 0) {
			String result = uri.substring(index + servletPath.length());
			if (result.length() > 0  && result.charAt(0) == '/') {
				return result;
			}
		}
		return "";
	}
	
	public static String computeRemainder(String uri, String serviceObjectPath) {
		return computePathInfo(uri, serviceObjectPath);
	}
	
	public static String removeLeadingSlash(String uri) {
		if (uri == null || uri.length() == 0 || uri.charAt(0) != '/') {
			return uri;
		}
		return uri.substring(1);
	}

	public static String[] splitURIIgnoreEmpty(String remainderURI) {
		String term = removeLeadingSlash(remainderURI.trim());
		return (term.length() == 0)? null : term.split("/");
	}

	
	/**
	 * Returns whether parentUrl1 is an ancestor of DescendantUrl2, equal, or a
	 * descendant of DescendantUrl2, or non-comparable. For example, http://abc
	 * is an ancestor of http://abc/def/ghi
	 * 
	 * @param ancestorUrl1
	 * @param DescendantUrl2
	 * @return 1, 0 , or -1. -2 if noncomparable
	 * 
	 */
	public static int compare(String ancestorUrl1, String DescendantUrl2) {
		assert(ancestorUrl1 != null && DescendantUrl2 != null);
		if (ancestorUrl1.equals(DescendantUrl2)) {
			return 0;
		}
		// Note that "/" has to be appended because we don't want http://ab to
		// be an ancestor of http://abc/def (it's an uncle, though).
		if (ancestorUrl1.length() < DescendantUrl2.length()
				&& DescendantUrl2.startsWith(ancestorUrl1 + "/")) {
			return 1;
		} else if (ancestorUrl1.length() > DescendantUrl2.length()
				&& ancestorUrl1.startsWith(DescendantUrl2 + "/")) {
			return -1;
		}
		return -2;
	}
	
	/**
	 * Returns true if parentUrl1 is an ancestor of DescendantUrl2
	 * @param ancestorUrl1
	 * @param descendantUrl2
	 * @return
	 */
	public static boolean isAncestorOf(String ancestorUrl1, String descendantUrl2) {
		assert(ancestorUrl1 != null && descendantUrl2 != null);
		return ancestorUrl1.length() < descendantUrl2.length()
				&& descendantUrl2.startsWith(ancestorUrl1 + "/");
	}
	
	/**
	 * Returns true if parentUrl1 is an ancestor of or equal to DescendantUrl2 
	 * @param ancestorUrl1
	 * @param descendantUrl2
	 * @return
	 */
	public static boolean isAncestorOrSelf(String ancestorUrl1, String descendantUrl2) {
		assert(ancestorUrl1 != null && descendantUrl2 != null);
		return ancestorUrl1.equals(descendantUrl2) || descendantUrl2.startsWith(ancestorUrl1 + "/");
	}
	
	public static String getParent(String url) {
		assert(url != null && url.length() > 0);
		int pos = url.lastIndexOf('/');
		return url.substring(0, pos);
	}

	/**
	 * @param fullName
	 * @return parent_name/item_name, e.g. parseQualifiedName("/WQX/Organization/Activity") == "Organization/Activity"
	 */
	public static String parseQualifiedName(String fullName) {
		String result = fullName;
		try {
			int lastPos = fullName.lastIndexOf(URIUtils.SEPARATOR);
			int nextToLastPos = fullName.lastIndexOf(URIUtils.SEPARATOR, lastPos - 1);
			return fullName.substring(nextToLastPos + 1);
		} catch (Exception e) {
			LoggingUtils.error("Tried to parseQualifiedName for [" + fullName + "]", e);
		}
		return result;
	
	}


}
