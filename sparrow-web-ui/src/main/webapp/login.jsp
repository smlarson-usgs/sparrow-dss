<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
	<jsp:include page="template_meta_tags.jsp" flush="true" />
	
	<title>Login - SPARROW Model Decision Support</title>
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />
	<jsp:include page="template_ie7_sizer_fix.jsp" flush="true" />
	
	<jsp:include page="template_page_tracking.jsp" flush="true" />
</head>
<body>
    <jsp:include page="header.jsp" flush="true" />
    <div style="padding: 1em">
    
	<h2>Please Login to the SPARROW DSS Application</h2>
	<p>
	<em>Please Note:</em> This is a provisional draft application for Internal USGS 
	use only and is subject to change--Do not quote or release. Anticipated Release is August/September 2011.
	</p>
	<form method="POST"  action="j_security_check">
	
	<table border="0"><tr>
	<td>Enter the username: </td><td>
	
	<input type="text" name="j_username" size="15">
	
	</td>
	</tr>
	<tr>
	<td>Enter the password: </td><td>
	
	<input type="password" name="j_password" size="15">
	
	</td>
	</tr>
	<tr>
	<td> <input type="submit" value="Submit"> </td>
	</tr>
	</table>
	
	</form>

    </div>
    <jsp:include page="footer.jsp" flush="true" />
</body>
</html>