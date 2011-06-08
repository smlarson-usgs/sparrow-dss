<%@ page contentType="text/html;charset=windows-1252" %>
<%@ page import="gov.usgswim.sparrow.action.*, gov.usgswim.sparrow.service.SharedApplication, gov.usgswim.datatable.*, gov.usgswim.datatable.utils.*" %>
<%@ page import="java.util.*, java.sql.*" %>
<%@ page isELIgnored="false" %>
<%@ page autoFlush="true" %>


<%
Exception actException = null;
int size = -1;
try {
	HashMap<String, Object> params = new HashMap<String, Object>(1, 1);
	params.put("ModelId", "50");
	
	//Expected column types
	Class<?>[] colTypes = {Integer.class, Integer.class, Integer.class, Integer.class, Integer.class};
	
	Connection conn = SharedApplication.getInstance().getROConnection();
	
	String sql = Action.getText("SelectTopoData", LoadModelPredictData.class);
	
	//Let the other method do the magic
	PreparedStatement ps = getROPSFromString(conn, sql, params);
	
	ResultSet rset = ps.executeQuery();
	DataTableWritable result = DataTableConverter.toDataTable(rset, colTypes, true);
	

	rset.close();
	ps.close();
	conn.close();
	
	size = result.getRowCount();
} catch (Exception e) {
	actException = e;
}

%>

<%!

public PreparedStatement getROPSFromString(Connection conn,
		String sql, Map<String, Object> params)
		throws Exception {

	PreparedStatement statement = null;

	//Go through in order and get the variables, replace with question marks.
	Action.SQLString temp = Action.processSql(sql, params);
	sql = temp.sql.toString();
	
	//getNewRWPreparedStatement with the processed string
	statement = getNewROPreparedStatement(conn, sql);
	
	Action.assignParameters(statement, temp, params);
	
	return statement;
}

public PreparedStatement getNewROPreparedStatement(Connection conn, String sql) throws SQLException {
	
	PreparedStatement st =
		conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
		ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
	
	st.setFetchSize(200);
	
	return st;
}

%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=windows-1252"/>
		<title>Get Topo (rst closed)</title>
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