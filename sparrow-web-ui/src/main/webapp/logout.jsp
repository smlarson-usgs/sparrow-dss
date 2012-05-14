<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Login - SPARROW Model Decision Support</title>
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />

</head>
<body>
    <jsp:include page="header.jsp" flush="true" />
    <div style="padding: 1em">
    
    <% 
    session.invalidate();
    %>
	<h2>You have been logged out.</h2>
	<h3><a title="back to home page" href="index.jsp">Take me all the way back to the home page >></a></h3>
    <jsp:include page="footer.jsp" flush="true" />
</body>
</html>