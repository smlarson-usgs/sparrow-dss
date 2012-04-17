<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<% response.sendRedirect("index.jsp"); %>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Login - SPARROW Model Decision Support</title>
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />

</head>
<body>
    <%@ include file="header.jsp" %>
    <div style="padding: 1em">
    
	<h2>Welcome - You are now logged into the SPARROW DSS Application.</h2>
	<h4>You will be redirected to the home page in 5 seconds...</h4>
	
	<p><b>This is a provisional draft application for Internal USGS use only and is 
    		subject to change--Do not quote or release. Anticipated Release is August/September 2011.</b></p>
	
    </div>
    <%@ include file="footer.jsp" %>
</body>
</html>