<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<%
	response.setHeader("Cache-Control", "max-age=43200, public");
	response.setHeader("Expires", "");
	response.setHeader("Pragma", "");
%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Cache Test 2 - SPARROW Model Decision Support</title>
</head>
<body>
    <%@ include file="header.jsp" %>
    <div style="padding: 1em">
    
    <a href="cache_test_1.jsp">Go to 1</a>
	<img src="ext_js/resources/images/access/form/checkbox.gif" />
	<img src="ext_js/resources/images/access/form/exclamation.gif" />
    </div>
    <%@ include file="footer.jsp" %>
</body>
</html>