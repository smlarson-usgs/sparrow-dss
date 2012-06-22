<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="gov.usgswim.sparrow.service.SharedApplication, gov.usgswim.sparrow.monitor.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Delivery Report - SPARROW Model Decision Support</title>
	
<!--	<script src="../jquery/jquery-1.7.2.js"></script>
	<script src="../jquery/jquery-ui-1.8.20.custom/js/jquery-ui-1.8.20.custom.min.js"></script>

	-->
	<link rel="icon" href="favicon.ico" >

	<link href="../jquery/jquery-ui-1.8.20.custom/css/smoothness/jquery-ui-1.8.20.custom.css" rel="stylesheet" type="text/css"/>
	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="../css/custom.css" />
	<link type="text/css" rel="stylesheet" href="monitor.css" />
<% MonitorHtmlUtil renderer = new MonitorHtmlUtil(); %>
</head>
<body>

<h1>Completed Complex Requests</h1>
<%= renderer.buildHtml(SharedApplication.getInstance().getCompletedComplexRequests()) %>

</body>
</html>