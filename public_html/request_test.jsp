<%@ page contentType="text/html;charset=windows-1252"%>
<%@ page import="java.io.*"%>
<%@ page import="java.net.*, java.io.CharArrayWriter, java.io.PrintWriter, java.util.Enumeration, java.util.Map.Entry"%>
<%
	
	String IS_NESTED_PARAM = "is_nested_req";
	String TINTED_BG = "background-color: #ffeecc; ";
	long stime;		//reused many times;
	int alternate = 1;		//just a flag to alternate bg colors.  Odd = clear, Even = beige
	
	String isNestedStr = request.getParameter(IS_NESTED_PARAM);
	boolean isNested = "true".equalsIgnoreCase(isNestedStr);
	
	//Cleaned up URLs:
	
	String userAddressResolve = request.getParameter("address_resolve");
	if (userAddressResolve == null) userAddressResolve = "infotrek.er.usgs.gov";
	
	String userContentRequest = request.getParameter("content_request");
	if (userContentRequest == null) userContentRequest = "";

%>
<% if (! isNested) { %>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
	"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
		<title>Request Test Page</title>
	</head>
<body>
	<h1>Request Testing Page</h1>
	<p><a href="index.jsp" title="Back to the index page">Home &gt;&gt;</a></p>
<% } %>
<% response.flushBuffer(); %>

<%!

	public String getRawURLContent(URL url) throws IOException {
		BufferedReader bin = new BufferedReader(
			new InputStreamReader( url.openStream() )
		);
		
		
		return streamToString(bin);
	}


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
		
			try {
				br.close();
			} catch (IOException ioe) { /* Ignore */ }
			
		}
	}
	
	public String printError(Exception e) {
		CharArrayWriter charArrayWriter = new CharArrayWriter(); 
		PrintWriter printWriter = new PrintWriter(charArrayWriter, true); 
		e.printStackTrace(printWriter); 
		return "<b>Error Details:</b><br>" + e.getMessage() + "<br>" + charArrayWriter.toString() + "<br>";
	}
	
	public String getAddressInfo(String address) {
		long stime = System.currentTimeMillis();
		
		try {
			InetAddress ia = InetAddress.getByName(address);
			String iaIP = ia.getHostAddress();
			String info = address + " resolves to " + iaIP + " ( " + (System.currentTimeMillis() - stime) + "ms)";
			return info;
		} catch (Exception e) {
			String info = "Could not resolve '" + address + "' ( " + (System.currentTimeMillis() - stime) + "ms).  <br>";
			info = info + printError(e);
			return info;
		}
	}

 %>
	<ul>
		<li><a href="#incoming">Properties of the incoming request</a></li>
		<li><a href="#network">Network Related System Properties</a></li>
		<li><a href="#allSystem">All system properties</a></li>
		<li><a href="#allEnvironment">All environment properties</a></li>
		<li><a href="#allCookies">All cookies</a></li>
	</ul>

	<a name="incoming"></a>
	<h4>This JSP page saw these properties of the incoming request:</h4>
	<ul>
		<li>getServerName() : <%=request.getServerName()%></li>
		<li>getHeader("Host") : <%=request.getHeader("Host")%></li>
		<li>getHeaderNames() : <% 		
			Enumeration headers = request.getHeaderNames();
			while (headers.hasMoreElements()) {
				String name = (String)headers.nextElement();
				out.println("<br/>" + name + " : " + request.getHeader(name));
			}%></li>
		<li>getServerPort() : <%=request.getServerPort()%></li>
		<li>getRemoteAddr() : <%=request.getRemoteAddr()%></li>
		<li>getContextPath() : <%=request.getContextPath()%></li>
		<li>getRequestURI() : <%=request.getRequestURI()%></li>
		<li>getRequestURL() : <%=request.getRequestURL()%></li>
		<li>getProtocol() : <%=request.getProtocol()%></li>
		<li>getScheme() : <%=request.getScheme()%></li>
		<li>getServletPath() : <%=request.getServletPath()%></li>
	</ul>

	<a name="network"></a>
	<h4>Network Related System Properties taken (mostly) from <a title="Sys properties ref" href="http://java.sun.com/j2se/1.5.0/docs/guide/net/properties.html">here</a></h4>
	<ul>
		<li>System.getProperty("mdc_local.host"): <%= System.getProperty("mdc_local.host") %></li>
		<li>System.getProperty("java.net.preferIPv4Stack"): <%= System.getProperty("java.net.preferIPv4Stack") %></li>
		<li>System.getProperty("networkaddress.cache.ttl"): <%= System.getProperty("networkaddress.cache.ttl") %></li>
		<li>System.getProperty("networkaddress.cache.negative.ttl"): <%= System.getProperty("networkaddress.cache.negative.ttl") %></li>
		<li>System.getProperty("http.proxyHost <%= System.getProperty("http.proxyHost") %></li>
		<li>System.getProperty("http.proxyPort"): <%= System.getProperty("http.proxyPort") %></li>
		<li>System.getProperty("http.nonProxyHosts"): <%= System.getProperty("http.nonProxyHosts") %></li>

	</ul>
	
	<a name="allSystem"></a>
	<h4>All System Properties</h4>
	<ul>
		<% 
			for (Entry entry: System.getProperties().entrySet()) {
				out.println("<li><b>" + entry.getKey() + "</b> -- " + entry.getValue() + "</li>");
			}
		%>
	</ul>
	
	<a name="allEnvironment"></a>
	<h4>All Environment Variables</h4>
	<ul>
		<% 
			for (Entry entry: System.getenv().entrySet()) {
				out.println("<li><b>" + entry.getKey() + "</b> -- " + entry.getValue() + "</li>");
			}
		%>
	</ul>

	<a name="allCookies"></a>
	<h4>All Cookies</h4>
	<ul>
		<% 

			for (Cookie cookie: request.getCookies()) {
				out.println("<li><ul>");
				out.println("<li><b>cookie name</b>= " + cookie.getName() + "</li>");
				out.println("<li><b>value</b>= " + cookie.getValue() + "</li>");
				out.println("<li><b>domain</b>= " + cookie.getDomain() + "</li>");
				out.println("<li><b>maxAge(sec)</b>= " + cookie.getMaxAge() + "</li>");
				out.println("</ul></li>");
			}
			
		%>
	</ul>
	
	<% if (isNested) { %>
	<h5>-- This is a nested request, so skipping the form and other actions --</h5>
	<% } else { %>
	
	<h4>A few address resolutions:</h4>
	<ul>
		<li><%= getAddressInfo("localhost") %></li>
		<li><%= getAddressInfo("infotrek.er.usgs.gov") %></li>
		<li><%= getAddressInfo("maptrek.er.usgs.gov") %></li>
		<li><%= getAddressInfo("google.com") %></li>
		<li><%= getAddressInfo(request.getServerName()) %></li>
	</ul>
	<hr>
	<form action="<%=request.getRequestURI()%>">
		<div>
			<label for="do_self_local_req_id">Make an internal request to this same page using 'localhost'?</label><input id="do_self_local_req_id" type="checkbox" name="do_self_local_req" value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_self_local_req")))?"checked=\"checked\"":""%>>
		</div>
		<div>
			<label for="do_self_external_req_id">Make an external request to this same page using '<%=request.getServerName()%>'?</label><input id="do_self_external_req_id" type="checkbox" name="do_self_external_req" value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_self_external_req")))?"checked=\"checked\"":""%>>
		</div>
		<div>
			<label for="address_resolve_id">Attempt to resolve an IP for: </label><input id="address_resolve_id" type="text" name="address_resolve" value="<%= userAddressResolve %>" size="60">
			<label for="do_address_resolve_id">Enable:</label><input id="do_address_resolve_id" type="checkbox" name="do_address_resolve" value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_address_resolve")))?"checked=\"checked\"":"" %>>
		</div>
		<div>
			<label for="content_request_id">Retrieve the content of: </label><input id="content_request_id" type="text" name="content_request" value="<%= userContentRequest %>" size="60">
			<label for="do_content_request_id">Enable:</label><input id="do_content_request_id" type="checkbox" name="do_content_request" value="true" <%= ("true".equalsIgnoreCase(request.getParameter("do_content_request")))?"checked=\"checked\"":"" %>>
		</div>
		<div><input type="submit"></div>
	</form>
	<hr>
	
	<% if ("true".equalsIgnoreCase(request.getParameter("do_self_local_req"))) { %>
	<%
	URL url = new URL("http", "localhost", request.getServerPort(), request.getRequestURI() + "?" + IS_NESTED_PARAM + "=true");
	stime = System.currentTimeMillis();
	%>
	<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
	<h4>Content of a GET request to : <%=url.toExternalForm()%></h4>
	<%= getRawURLContent(url) %>
	<h4>Total Time to complete request was <%= System.currentTimeMillis() - stime %>ms</h4>
	</div>
	<hr>
	<% } %>
	
	<% if ("true".equalsIgnoreCase(request.getParameter("do_self_external_req"))) { %>
	<%
	URL url = new URL("http", request.getServerName(), request.getServerPort(), request.getRequestURI() + "?" + IS_NESTED_PARAM + "=true");
	stime = System.currentTimeMillis();
	%>
	<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
	<h4>Content of a GET request to : <%=url.toExternalForm()%></h4>
	<%= getRawURLContent(url) %>
	<h4>Total Time to complete request was <%= System.currentTimeMillis() - stime %>ms</h4>
	</div>
	<hr>
	<% } %>
	
	<% if ("true".equalsIgnoreCase(request.getParameter("do_address_resolve")) && !"".equals(request.getParameter("address_resolve"))) { %>
	<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
	<h4><%= getAddressInfo(request.getParameter("address_resolve")) %></h4>
	</div>
	<hr>
	<% } %>
	
	<% if ("true".equalsIgnoreCase(request.getParameter("do_content_request")) && !"".equals(request.getParameter("content_request"))) { %>
	<%
	URL url = new URL(request.getParameter("content_request"));
	stime = System.currentTimeMillis();
	%>
	<div style="<%= ((alternate+=1) % 2 == 0)?TINTED_BG:"" %>">
	<h4>Content of a GET request to : <%=url.toExternalForm()%></h4>
	<%= getRawURLContent(url) %>
	<h4>Total Time to complete request was <%= System.currentTimeMillis() - stime %>ms</h4>
	</div>
	<hr>
	<% } %>
	
	
	<% } %>

<% if (! isNested) { %>
 </body>
</html>
<% } %>