<%@ page import="gov.usgswim.sparrow.SparrowUtil" %>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" > 
		
		<%--
		Tell newer versions of IE to pretend they are IE9.
		IE8 Does not work for rendering SVG, so versions 8 and older get warning messages.
		--%>
		<meta http-equiv="x-ua-compatible" content="IE=10" >
		
		<meta name="description" content ="SPARROW Decision Support tool - an online version of the SPARROW Models with support for scenario testing."> 
		<meta name="author" content="See http://water.usgs.gov/nawqa/sparrow for authorship of the SPARROW model.  Site and online capabilities created by CIDA, http://cida.usgs.gov"> 
			<!--required for formal publications and recommended for all other pages by the USGS web standards--> 
		<meta name="keywords" content="SPARROW, Decision Support, USGS, CIDA, Map"> 
		<meta name="publisher" content="Middleton Data Center, Wisconsin Science Center"> 
	
		<meta name="country" content="USA">

	
		<link rel="icon" href="<%= SparrowUtil.getRequestUrlFeatures(request).getBaseUrlWithoutSlash() %>/favicon.ico" type="image/x-icon">
