<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Delivery Report - SPARROW Model Decision Support</title>
	<script src="js/report_parent.js"></script>
	
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />
	<link type="text/css" rel="stylesheet" href="css/report.css" />
	
	<%
	
	String tableUrl = "getDeliveryReport";
	String fullTableUrl = "http://localhost:8080/sparrow/getDeliveryReport?context-id=" + request.getParameter("context-id") + "&mime-type=xhtml_table&include-id-script=false";
	
	%>

</head>
<body>
    <%@ include file="header.jsp" %>
    
    <h1>Aggregate Downstream Reach Summary for Total Load</h1>
    <h3>Total Load for each selected downstream reach, detailed by source and reach.</h3>
    
 <%
 
 	response.getWriter().flush();
 
    URL url = new URL(fullTableUrl);
    BufferedReader in = new BufferedReader(
    new InputStreamReader(url.openStream()));
	StringBuffer table = new StringBuffer();
    String inputLine;
    while ((inputLine = in.readLine()) != null)
    	table.append(inputLine);
    in.close();
 
 %>
 
 <%= table %>
<%-- 	<jsp:include page="<%= tableUrl %>" flush="true" /> --%>
    
    <%@ include file="footer.jsp" %>
</body>
</html>