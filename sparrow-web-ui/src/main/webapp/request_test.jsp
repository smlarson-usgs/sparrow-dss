<%@ page contentType="text/html;charset=windows-1252" %>
<%@ page import="java.io.*, java.net.*, java.util.Enumeration, java.util.Map.Entry, java.util.Locale" %>
<%@ page isELIgnored="false" %>
<%@ page autoFlush="true" %>

<%!
	// NOTE: This jsp does not follow jsp best practices and is not
	// intended to be used as a model of jsp development. This page
	// eschews the use of taglibs and jstl as these have to be set up via web.xml
	// This jsp page is essentially a servlet packaged as a jsp
	// page for the purpose of easy redeployment, as a jsp page can be dropped anywhere
	// without intermediate compilation and packaging steps.

	// ==========================================
	// Global/Servlet Class function declarations
	// ==========================================
	public String getRawURLContent(URL url) throws IOException {
		BufferedReader bin = new BufferedReader(
			new InputStreamReader( url.openStream() )
		);
		return streamToString(bin);
	};


	public String streamToString(BufferedReader br) throws java.io.IOException {
		final int CHUNK_SIZE = 50;
		try {
			StringBuffer data = new StringBuffer(500);
			char[] chunk = new char[CHUNK_SIZE];
			int charsRead = br.read(chunk, 0, CHUNK_SIZE);	//read first chunk

			while (charsRead > 0) {
				data.append(chunk, 0, charsRead);
				charsRead = br.read(chunk, 0, CHUNK_SIZE);
			}
			return data.toString();

		} finally {
			try { br.close(); } catch (IOException ioe) { /* Ignore */ }
		}
	};

	public String printError(Exception e) {
		CharArrayWriter charArrayWriter = new CharArrayWriter();
		PrintWriter printWriter = new PrintWriter(charArrayWriter, true);
		e.printStackTrace(printWriter);
		return "<b>Error Details:</b><br>" + e.getMessage() + "<br>" + charArrayWriter.toString() + "<br>";
	};

	static final String ADDRESS_RESOLVE_DISPLAY = "%s resolves to %s (%dms)";
	static final String ADDRESS_ERROR_DISPLAY = "Could not resolve '%s' (%dms). <br>";
	public String getAddressInfo(String address) {
		long stime = System.currentTimeMillis();

		try {
			InetAddress ia = InetAddress.getByName(address);
			String iaIP = ia.getHostAddress();
			return String.format(ADDRESS_RESOLVE_DISPLAY, address, iaIP, System.currentTimeMillis() - stime);
		} catch (Exception e) {
			return String.format(ADDRESS_ERROR_DISPLAY, address, System.currentTimeMillis() - stime) + printError(e);
		}
	}

	final static String KEY_VALUE_LISTING= "<li><b>%s</b> -- %s</li>";

//jsp:declaration
%>
<%	// jsp:scriptlet
	// ==========================
	// Handle incoming parameters
	// ==========================


	long stime;		//reused many times;
	int alternate = 1;		//just a flag to alternate bg colors.  Odd = clear, Even = beige

	// List of incoming parameters
	String contentRequest = request.getParameter("content_request");
	boolean isContentRequest =  contentRequest != null && contentRequest.length() > 0;
	String addressResolutionRequest = request.getParameter("address_resolve");
	boolean isAddressResolutionRequest = addressResolutionRequest != null && addressResolutionRequest.length() > 0;
	boolean isExternalToSelfRequest = "true".equalsIgnoreCase(request.getParameter("do_self_external_req"));
	boolean isLocalHostToSelfRequest = "true".equalsIgnoreCase(request.getParameter("do_self_local_req"));
	boolean isNested  = isLocalHostToSelfRequest || isExternalToSelfRequest;
	boolean hasResults = isContentRequest || isAddressResolutionRequest || isExternalToSelfRequest || isLocalHostToSelfRequest;

	//Cleaned up URLs:
	String userAddressResolve = request.getParameter("address_resolve");
	userAddressResolve = (userAddressResolve == null)? "": userAddressResolve;

	String userContentRequest = request.getParameter("content_request");
	userContentRequest = (userContentRequest == null)? "" : userContentRequest;

	// CSS Styles
	String DIVSTYLE = "margin-left: 30px";
	String TINTED_BG = "background-color: #ffeecc; ";
%>


<%-- START OUTPUTTING PAGE CONTENT --%>
<% if (!isNested) {
	// OUTPUT THE THE HEADER
%>
	<%-- PAGE HEADER --%>
	<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">
	<html>
		<head>
			<meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
			<title>Request Test Page</title>
		</head>
	<%-- END PAGE HEADER --%>
	<body>
		<h1>Request Testing Page</h1>
		<p><a href="index.html" title="Back to the index page">Home &gt;&gt;</a></p>

<% } %>
	<%-- MAIN PAGE CONTENT --%>
	<ul>
		<li><a href="#incoming">Properties of the incoming request</a></li>
		<li><a href="#network">Network Related System Properties</a></li>
		<li><a href="#allSystem">All system properties</a></li>
		<li><a href="#allEnvironment">All environment properties</a></li>
		<li><a href="#allCookies">All cookies</a></li>
		<li><a href="#requestForm">Request Form</a></li>
		<% if (hasResults) { %>
			<li><a href="#requestResults">Request Results</a></li>
		<% } %>
	</ul>

	<div style="<%= DIVSTYLE %>">
		<a name="incoming"></a>
		<h4>This JSP page saw these properties of the incoming request:</h4>
		<ul>
			<li><b>getHeaderNames()</b> : <ul>
				<%
					Enumeration<String> headers = request.getHeaderNames();
					while (headers.hasMoreElements()) {
						String name = headers.nextElement();
						out.println(String.format(KEY_VALUE_LISTING, name, request.getHeader(name)));
					}
				%>
				</ul>
			</li>
			<li><b>getAttributeNames()</b> : <ul>
				<%
					Enumeration<String> attribs = request.getAttributeNames();
					while (attribs.hasMoreElements()) {
						String name = attribs.nextElement();
						out.println(String.format(KEY_VALUE_LISTING, name, request.getAttribute(name).toString()));
					}
				%>
				</ul>
			</li>

			<%
				out.println(String.format(KEY_VALUE_LISTING, "getAuthType()", request.getAuthType()));
				out.println(String.format(KEY_VALUE_LISTING, "getContextPath()", request.getContextPath()));
				out.println(String.format(KEY_VALUE_LISTING, "getDateHeader(\"If-Modified-Since\")", request.getDateHeader("If-Modified-Since")));
				out.println(String.format(KEY_VALUE_LISTING, "getMethod()", request.getMethod()));

				out.println(String.format(KEY_VALUE_LISTING, "getPathInfo()", request.getPathInfo()));
				out.println(String.format(KEY_VALUE_LISTING, "getPathTranslated()", request.getPathTranslated()));

				out.println(String.format(KEY_VALUE_LISTING, "getQueryString()", request.getQueryString()));

				out.println(String.format(KEY_VALUE_LISTING, "getRequestedSessionId()", request.getRequestedSessionId()));
				out.println(String.format(KEY_VALUE_LISTING, "getRequestURI()", request.getRequestURI()));
				out.println(String.format(KEY_VALUE_LISTING, "getRequestURL()", request.getRequestURL()));


				out.println(String.format(KEY_VALUE_LISTING, "getServletPath()", request.getServletPath()));

				out.println(String.format(KEY_VALUE_LISTING, "getUserPrincipal()", request.getUserPrincipal()));
				out.println(String.format(KEY_VALUE_LISTING, "isRequestedSessionIdFromCookie()", request.isRequestedSessionIdFromCookie()));
				out.println(String.format(KEY_VALUE_LISTING, "isRequestedSessionIdFromURL()", request.isRequestedSessionIdFromURL()));
				out.println(String.format(KEY_VALUE_LISTING, "isRequestedSessionIdValid()", request.isRequestedSessionIdValid()));
			%>

			<li><b>getLocales()</b> : <ul>
				<%
					Enumeration<Locale> locales = request.getLocales();
					while (locales.hasMoreElements()) {
						Locale locale = locales.nextElement();
						out.println(String.format(KEY_VALUE_LISTING, locale.getDisplayName(), locale.toString()));
					}
				%>
				</ul>
			</li>

			<%
				out.println(String.format(KEY_VALUE_LISTING, "getCharacterEncoding()", request.getCharacterEncoding()));
				out.println(String.format(KEY_VALUE_LISTING, "getContentLength()", request.getContentLength()));
				out.println(String.format(KEY_VALUE_LISTING, "getContentType()", request.getContentType()));

				out.println(String.format(KEY_VALUE_LISTING, "getLocale()", request.getLocale().toString()));
				out.println(String.format(KEY_VALUE_LISTING, "getLocalName()", request.getLocalName()));
				out.println(String.format(KEY_VALUE_LISTING, "getLocalAddr()", request.getLocalAddr()));
				out.println(String.format(KEY_VALUE_LISTING, "getLocalPort()", request.getLocalPort()));

				out.println(String.format(KEY_VALUE_LISTING, "getProtocol()", request.getProtocol()));
				out.println(String.format(KEY_VALUE_LISTING, "getRemoteAddr()", request.getRemoteAddr()));
				out.println(String.format(KEY_VALUE_LISTING, "getRemoteUser()", request.getRemoteUser()));
				out.println(String.format(KEY_VALUE_LISTING, "getRemoteHost()", request.getRemoteHost()));
				out.println(String.format(KEY_VALUE_LISTING, "getRemotePort()", request.getRemotePort()));

				out.println(String.format(KEY_VALUE_LISTING, "getScheme()", request.getScheme()));
				out.println(String.format(KEY_VALUE_LISTING, "getServerName()", request.getServerName()));
				out.println(String.format(KEY_VALUE_LISTING, "getServerPort()", request.getServerPort()));
				out.println(String.format(KEY_VALUE_LISTING, "isSecure()", request.isSecure()));


			%>

		</ul>
	</div>

	<div style="<%= DIVSTYLE %>">
		<a name="network"></a>
		<h4>Network Related System Properties taken (mostly) from <a title="Sys properties ref" href="http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html">here</a></h4>
		<ul>
			<li>System.getProperty("<b>mdc_local.host</b>"): <%= System.getProperty("mdc_local.host") %></li>
			<li>System.getProperty("<b>java.net.preferIPv4Stack</b>"): <%= System.getProperty("java.net.preferIPv4Stack") %></li>
			<li>System.getProperty("<b>networkaddress.cache.ttl</b>"): <%= System.getProperty("networkaddress.cache.ttl") %></li>
			<li>System.getProperty("<b>networkaddress.cache.negative.ttl</b>"): <%= System.getProperty("networkaddress.cache.negative.ttl") %></li>
			<li>System.getProperty("<b>http.proxyHost</b>"): <%= System.getProperty("http.proxyHost") %></li>
			<li>System.getProperty("<b>http.proxyPort</b>"): <%= System.getProperty("http.proxyPort") %></li>
			<li>System.getProperty("<b>http.nonProxyHosts</b>"): <%= System.getProperty("http.nonProxyHosts") %></li>
		</ul>
	</div>

	<div style="<%= DIVSTYLE %>">
		<a name="allSystem"></a>
		<h4>All System Properties</h4>
		<ul>
			<%
				for (Entry<Object, Object> entry: System.getProperties().entrySet()) {
					out.println(String.format(KEY_VALUE_LISTING,entry.getKey(), entry.getValue()));
				}
			%>
		</ul>
	</div>

	<div style="<%= DIVSTYLE %>">
		<a name="allEnvironment"></a>
		<h4>All Environment Variables</h4>
		<ul>
			<%
				for (Entry<String, String> entry: System.getenv().entrySet()) {
					out.println(String.format(KEY_VALUE_LISTING, entry.getKey(), entry.getValue()));
				}
			%>
		</ul>
	</div>

	<div style="<%= DIVSTYLE %>">
		<a name="allCookies"></a>
		<h4>All Cookies</h4>
		<ul>
			<%
				Cookie[] cookies = request.getCookies();
				if (cookies != null){
					for (Cookie cookie: cookies) {
						out.println("<li><ul>");
						out.println(String.format(KEY_VALUE_LISTING, "cookie name",cookie.getName()));
						out.println(String.format(KEY_VALUE_LISTING, "value",cookie.getValue()));
						out.println(String.format(KEY_VALUE_LISTING, "domain",cookie.getDomain()));
						out.println(String.format(KEY_VALUE_LISTING, "maxAge(sec)",cookie.getMaxAge()));
						out.println("</ul></li>");
					}
				}
			%>
		</ul>
	</div>

	<%-- END MAIN PAGE CONTENT --%>
	<hr>
	<% if (isNested) { %>
	<h5>-- This is a nested request, so skipping the form and other actions --</h5>
	<% } else { // BEGIN NON-NESTED CONTENT %>
		<hr>
		<%-- Request Testing Form --%>
		<a name="requestForm"/>

		<h4>A few address resolutions:</h4>
		<ul>
			<li><%= getAddressInfo("localhost") %></li>
			<li><%= getAddressInfo("infotrek.er.usgs.gov") %></li>
			<li><%= getAddressInfo("maptrek.er.usgs.gov") %></li>
			<li><%= getAddressInfo("google.com") %></li>
			<li><%= getAddressInfo(request.getServerName()) %></li>
		</ul>


		<form action="<%=request.getRequestURI()%>">
			<fieldset><legend>Choose at most one of the following:</legend>
				<input id="do_self_local_req_id" type="checkbox" name="do_self_local_req"
					value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_self_local_req")))?"checked=\"checked\"":""%>>
				<label for="do_self_local_req_id">Make an internal request to this same page using 'localhost'?</label>

				<br>
				<input id="do_self_external_req_id" type="checkbox" name="do_self_external_req"
					value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_self_external_req")))?"checked=\"checked\"":""%>>
				<label for="do_self_external_req_id">Make an external request to this same page using external server: '<%=request.getServerName()%>'?</label>

				<br>
				<input id="do_address_resolve_id" type="checkbox" name="do_address_resolve"
					value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_address_resolve")))?"checked=\"checked\"":"" %>>
				<label for="address_resolve_id">Attempt to resolve an IP for: </label>
				<input id="address_resolve_id" type="text" name="address_resolve"
					value="<%= userAddressResolve %>" size="60"> e.g. infotrek.er.usgs.gov

				<br>
				<input id="do_content_request_id" type="checkbox" name="do_content_request"
					value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_content_request")))?"checked=\"checked\"":"" %>>
				<label for="content_request_id">Retrieve the content of: </label>
				<input id="content_request_id" type="text" name="content_request"
					value="<%= userContentRequest %>" size="60"> e.g. http://google.com

				<br>
				<input type="submit">
			</fieldset>
		</form>
		<%-- END Request Testing Form --%>

		<%-- Display Request Results --%>
		<% if (isLocalHostToSelfRequest) {
			URL url = new URL("http", "localhost", request.getServerPort(), request.getRequestURI());
			stime = System.currentTimeMillis();
		%>
			<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
				<h4>Content of a GET request to : <%=url.toExternalForm()%></h4>
				<%= getRawURLContent(url) %>
				<h4>Total Time to complete request was <%= System.currentTimeMillis() - stime %>ms</h4>
			</div>
			<hr>
		<% }
		if (isExternalToSelfRequest) {
			URL url = new URL("http", request.getServerName(), request.getServerPort(), request.getRequestURI());
			stime = System.currentTimeMillis();
		%>
			<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
				<h4>Content of a GET request to : <%=url.toExternalForm()%></h4>
				<%= getRawURLContent(url) %>
				<h4>Total Time to complete request was <%= System.currentTimeMillis() - stime %>ms</h4>
			</div>
			<hr>
		<% }
		if (isAddressResolutionRequest) { %>
			<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
				<h4><%= getAddressInfo(addressResolutionRequest) %></h4>
			</div>
			<hr>
		<% }
		if (isContentRequest) {
			URL url = new URL(contentRequest);
			stime = System.currentTimeMillis();
		%>
			<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
				<a name="requestResults"/>
				<h4>Content of a GET request to : <%=url.toExternalForm()%></h4>
				<%= getRawURLContent(url) %>
				<h4>Total Time to complete request was <%= System.currentTimeMillis() - stime %>ms</h4>
			</div>
			<hr>
		<% } %>
		<%-- END Display Request Results --%>

	<% } // END NON-NESTED CONTENT %>

<% if (! isNested) { %>
	</body></html>
<% } %>