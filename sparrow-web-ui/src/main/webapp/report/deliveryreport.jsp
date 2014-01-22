<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<jsp:include page="../template_meta_tags.jsp" flush="true" />

	<title>Delivery Report - SPARROW Model Decision Support</title>

	<script src="../jquery/jquery-1.7.2.js"></script>
	<script src="../jquery/jquery-ui-1.8.20.custom/js/jquery-ui-1.8.20.custom.min.js"></script>
	<script src="../jquery/jquery.timer/jquery.timer.js"></script>
	<script src="js/report.js?rev=3"></script>

	<link href="../jquery/jquery-ui-1.8.20.custom/css/smoothness/jquery-ui-1.8.20.custom.css" rel="stylesheet" type="text/css"/>

	<link type="text/css" rel="stylesheet" href="../css/full_width.css?rev=3">
	<link type="text/css" rel="stylesheet" href="../css/static_custom.css?rev=3">
	<link type="text/css" rel="stylesheet" href="style/report.css?rev=3" />
	<jsp:include page="../template_ie7_sizer_fix.jsp" flush="true" />

	<jsp:include page="../template_page_tracking.jsp" flush="true" />
</head>
<body>
<jsp:include page="../header-unbalanced.jsp" flush="true" />
	<div id="page-content" class="area-1 area">
		<div class="area-content">
			<div id="tabs" class="content">
				<ul>
					<li><a href="#aggregate-report-container"><span>Delivered Load from Selected Upstream Regions</span></a></li>	
					<li><a href="#terminal-report-container"><span>Total Delivered Load Summary</span></a></li>
				</ul>
				<div id="terminal-report-container" class="report-container">
						<jsp:include page="terminal_wrapper.jsp" flush="true" />
				</div>
				<div id="aggregate-report-container" class="report-container">
					<jsp:include page="aggregate_wrapper.jsp" flush="true" />
				</div>
			</div>


		</div>
	</div>
<jsp:include page="../footer-unbalanced.jsp" flush="true" />
</body>
</html>