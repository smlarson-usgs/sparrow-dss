<%@ page contentType="text/html;charset=windows-1252" %>
<%@ page import="gov.usgswim.sparrow.action.*, gov.usgswim.sparrow.request.PredictionContextRequest, gov.usgswim.sparrow.domain.PredictionContext" %>
<%@ page import="java.util.List" %>
<%@ page isELIgnored="false" %>
<%@ page autoFlush="true" %>

<%

Exception actException = null;

PredictionContextRequest pcReq = new PredictionContextRequest(437701782);
PredictionContextHandler act = new PredictionContextHandler(pcReq);
int size = 0;
try {
	List<PredictionContext> pcList = act.run();
	size = pcList.size();
} catch (Exception e) {
	actException = e;
}

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
		<title>Get Context (rst closed)</title>
	</head>
<%-- END PAGE HEADER --%>
<body>
	<h1>Get Context (rst closed)</h1>
	<p>Found <%= size %> records.</p>
	
	<% if (actException != null) { %>
	<p>An exception occurred:</p>
	<p><%= actException.getMessage() %></p>
	<% } %>
</body>
</html>