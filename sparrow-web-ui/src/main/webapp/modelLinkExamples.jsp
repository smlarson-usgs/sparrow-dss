<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
    <title>Model Link Examples - SPARROW Model Decision Support</title>
    <link rel="icon" href="favicon.ico" >
    <link type="text/css" rel="stylesheet" href="css/usgs_style_main.css" />
    <link type="text/css" rel="stylesheet" href="css/custom.css" />
    <%
        // Build the URL to the current SPARROW deployment
        String host = request.getHeader("Host");
        String context = request.getContextPath();
        String url = "http://" + host + context + "/map.jsp";
    %>
</head>
<body>
    <jsp:include page="header.jsp" flush="true" />
    <div>
        <p class="paragraph">
	        It is possible to link to SPARROW from other pages while specifying a
	        particular model to load as can be seen by the following hyperlinks:
        </p>
        <p class="paragraph">
	        <a href="<%= url %>?model=21">Chesapeake Bay V2</a><br />
	        <a href="<%= url %>?model=22">National Model</a><br />
        </p>
        <p class="paragraph">
            HTML for the above links:
        </p>
        <pre class="code">
        &lt;a href="<%= url %>?model=21"&gt;Chesapeake Bay V2&lt;/a&gt;
        &lt;a href="<%= url %>?model=22"&gt;National Model&lt;/a&gt;</pre>
    </div>
    <jsp:include page="footer.jsp" flush="true" />
</body>
</html>