<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Delivery Report - SPARROW Model Decision Support</title>
	
	<script src="../jquery/jquery-1.7.2.js"></script>
	<script src="../jquery/jquery-ui-1.8.20.custom/js/jquery-ui-1.8.20.custom.min.js"></script>
	<script src="../jquery/jquery.timer/jquery.timer.js"></script>
	<script src="js/report.js"></script>

	
	<link rel="icon" href="favicon.ico" >

	<link href="../jquery/jquery-ui-1.8.20.custom/css/smoothness/jquery-ui-1.8.20.custom.css" rel="stylesheet" type="text/css"/>
	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="../css/custom.css" />
	<link type="text/css" rel="stylesheet" href="style/report.css" />
<%
//	String reportTypeStr = request.getParameter("report-type");
//	String reportFile = "";
//	if ("terminal".equals(reportTypeStr)) {
//		reportFile = "terminal_wrapper.jsp";
//	} else if ("region_agg".equals(reportTypeStr)) {
//		reportFile = "aggregate_wrapper.jsp";
//	} else {
//		throw new Exception("Unrecognized report-type parameter '" + reportTypeStr + "'");
//	}
%>
</head>
<body>
    <jsp:include page="../header.jsp" flush="true" />
		
<div id="tabs" class="content">
    <ul>
        <li><a href="#fragment-1"><span>Total Delivered Load Summary</span></a></li>
        <li><a href="#fragment-2"><span>Upstream Source Aggregations (by State and HUC)</span></a></li>
    </ul>
    <div id="fragment-1">
       <jsp:include page="terminal_wrapper.jsp" flush="true" />
    </div>
    <div id="fragment-2">
			<jsp:include page="aggregate_wrapper.jsp" flush="true" />
    </div>
</div>
    <jsp:include page="../footer.jsp" flush="true" />
</body>
</html>