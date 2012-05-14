<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Login Error - SPARROW Model Decision Support</title>
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />

</head>
<body>
    <jsp:include page="header.jsp" flush="true" />
    <div style="padding: 1em">
    
	<h2 style="color: #600">Sorry, that was the wrong user name or password.  Please try again.</h2>
	<p>
	<em>Please Note:</em> This application has not yet been released for public
	use and is currently undergoing a review process prior to that release.
	Until formal public release, data, maps and other information presented in
	this application are not deemed usable for any purpose.
	No guarantee of data quality is made by NAWQA, the USGS or the Department
	of Interior. 
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