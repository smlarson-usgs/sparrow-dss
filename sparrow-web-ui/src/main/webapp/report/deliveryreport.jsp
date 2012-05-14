<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Delivery Report - SPARROW Model Decision Support</title>
	<script src="js/report.js"></script>
	
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="../css/custom.css" />
	<link type="text/css" rel="stylesheet" href="style/report.css" />
<%
	String reportTypeStr = request.getParameter("report-type");
	String reportFile = "";
	if ("terminal".equals(reportTypeStr)) {
		reportFile = "terminal_wrapper.jsp";
	} else if ("region_agg".equals(reportTypeStr)) {
		reportFile = "aggregate_wrapper.jsp";
	} else {
		throw new Exception("Unrecognized report-type parameter '" + reportTypeStr + "'");
	}
%>
</head>
<body>
    <jsp:include page="../header.jsp" flush="true" />
		<jsp:include page="<%= reportFile %>" flush="true" />
    <jsp:include page="../footer.jsp" flush="true" />
</body>
</html>