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

</head>
<body>
	<jsp:include page="../header.jsp" flush="true" />
    
	<h1>SPARROW DSS Total Delivered Load, Aggregated by Upstream Region</h1>
	<div class="explination">
		<p>
			More details...
		</p>
	</div>
    
	<jsp:include page="../footer.jsp" flush="true" />
</body>
</html>