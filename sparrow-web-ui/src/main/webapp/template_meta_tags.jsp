<%@ page import="gov.usgswim.sparrow.SparrowUtil" %>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" > 
		
		<%--
		Tell newer versions of IE to pretend they are IE8 (or 7 if needed.)
		The version of EXT used in the project is not aware of versions beyond
		IE8, so EXT assumes its running in IE6 and pops up warning messages.
		This also seems to more reliably turn off IE compatability mode, preventing
		JS errors that would occur otherwise.
		--%>
		<meta http-equiv="x-ua-compatible" content="IE=7,9" >
		
		<meta name="description" content ="SPARROW Decision Support tool - an online version of the SPARROW Models with support for scenario testing."> 
		<meta name="author" content="See http://water.usgs.gov/nawqa/sparrow for authorship of the SPARROW model.  Site and online capabilities created by CIDA, http://cida.usgs.gov"> 
			<!--required for formal publications and recommended for all other pages by the USGS web standards--> 
		<meta name="keywords" content="SPARROW, Decision Support, USGS, CIDA, Map"> 
		<meta name="publisher" content="Middleton Data Center, Wisconsin Science Center"> 
	
		<meta name="country" content="USA">

	
		<link rel="icon" href="<%= SparrowUtil.getRequestUrlFeatures(request).getBaseUrlWithoutSlash() %>/favicon.ico" type="image/x-icon">
