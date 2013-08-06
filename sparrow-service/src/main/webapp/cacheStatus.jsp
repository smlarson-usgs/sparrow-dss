<%@ page
	import="java.io.*, java.net.*, java.sql.*, javax.naming.*, javax.sql.DataSource, java.util.List, java.util.ArrayList"%>
<%@ page
	import="gov.usgswim.sparrow.service.*, gov.usgswim.sparrow.cachefactory.EhCacheConfigurationReader"%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>ehcache Status</title>
    <link rel="icon" href="../favicon.ico" />
  </head>
  <body>
	<pre>
<%
			String showDetailsString = request.getParameter("details");
			boolean showDetails = (showDetailsString != null ) && (showDetailsString.equals("show details"));
			out.println(EhCacheConfigurationReader.listDistributedCacheStatus(showDetails).toString());
%>
	</pre>
	<br/>
	<form action="distributedCacheStatus.jsp" method="get">
	<%
		//out.println(showDetailsString + " " + showDetails);
		// toggle the details button
		if (showDetails) {
			out.println("<input type='submit' value='hide details' name='details'/>");
		} else {
			out.println("<input type='submit' value='show details' name='details'/>");
		}
	
	%>
	</form>
</body>
</html>