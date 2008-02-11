<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<%@ page import="java.net.*, java.io.CharArrayWriter, java.io.PrintWriter"%>
<%@ page import="javax.naming.*, java.util.ArrayList, java.sql.*, javax.sql.*"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>JDBC Test</title>
  </head>

<%

	String CHECKED = "checked=\"checked\"";	//used to activate checkboxes
%>
<%!


	public Object[] listJNDI() {
	
		try {
			// Create the initial context
			Context ctx = new InitialContext();
			
			NamingEnumeration ne = ctx.listBindings("jdbc");
			ArrayList list = new ArrayList();
			//TreeSet set = new TreeSet();
			
			while(ne.hasMore()) {
				Binding b = (Binding) ne.next();
				list.add(b.getName());
				//set.add(b.getName() + " : " + b.getObject().getClass().getName());
			}
			
			// close the context
			ctx.close();
			
			return list.toArray();
		
		} catch (NamingException e) {
			return new String[] {"**A naming exception occured - unable to list**"};
		}
	}
	
	/*
	 * Clean the table name and builds a query
	 */
	public String getQuery(String tableName) {
		//clean up table name
		if ("".equals(tableName)) {
			tableName = "dual";
		} else {
			if (tableName.indexOf(" ") > 0 || tableName.indexOf(";") > 0 || tableName.indexOf(",") > 0) {
				tableName = "dual";
			}
		}
		
		String query = "SELECT COUNT(*) FROM (SELECT * FROM " + tableName + ")";
		
		return query;
	}
	
	public String[] runQuery(Context ctx, String jndiName, String query) {
	
		Connection conn = null;
		ResultSet rset = null;
		
		try {
			DataSource jdbcSource = (DataSource) ctx.lookup(jndiName);
			conn = jdbcSource.getConnection();
			Statement statement = conn.createStatement();
			rset = statement.executeQuery(query);
			
			if (rset.isBeforeFirst() && rset.isAfterLast()) {
				return new String[] {"** NO RECORDS FOUND **"};
			} else {
				ArrayList<String> list = new ArrayList<String>();
				
				while (rset.next()) {
					list.add(rset.getString(1));
				}
			
				return list.toArray(new String[] {});
			}
							
			
		} catch (Exception e) {
			return new String[] {"** EXCEPTION WHILE QUERYING: " + e.getMessage() + " **"};
		} finally {
			try {
				if (rset != null) rset.close();
				if (conn != null) conn.close();
			} catch (Exception ee) {
				/* Ignore */
			}
		}
	}


%>
	
	
  <body>
		<h1>JDBC Resources Test Page</h1>
		<p><a href="index.jsp" title="Back to the index page">Home &gt;&gt;</a></p>

		<p>
		Below is a list of all visible JNDI connections for this application.
		To test a JNDI Connection, check the box next to that connection and click
		the submit button.  By default, the query:
		<code>
		SELECT COUNT(*) FROM (SELECT * FROM dual)
		</code>
		is used.  Alternatly, you can supply a table name which replaces
		<code>dual</code> in the query.
		</p>
		<form action="<%=request.getRequestURI()%>" style="border: solid 1px;">
			<ul>
			<%
				Object[] jndiList = listJNDI();
				Context ctx = new InitialContext();
				long stime = 0;	//time at the start of the query

				for (Object j : jndiList) {
				
					//Determine if it is to be run
					boolean doRun = "true".equals(request.getParameter(j.toString()));
				
					//Find and  clean the table name
					String table = request.getParameter(j.toString() + "__TABLE");
					if (table == null) table = "";
					
					
			%>
				<li>
					<div>
					<input type="checkbox" id="<%= j.toString() %>__ID" value="true" name="<%= j.toString() %>" <%= (doRun)?CHECKED:"" %>>
					<label for="<%= j.toString() %>__ID"><%= j.toString() %></label>
					Table: <input type="text" size="20" name="<%= j.toString() %>__TABLE" value="<%= table %>">
					</div>
					<%
						if (doRun) {
						
							String query = getQuery(table);
							stime = System.currentTimeMillis();
							String[] results = runQuery(ctx, "jdbc/" + j.toString(), query);
					%>
					<div style="border: dotted 1px #cccccc">
					<p>
						Query: <code><%= query %></code><br>
						Response in <%= System.currentTimeMillis() - stime %>ms:
					</p>
					<ul>
					<% for (String s : results) { %>
					<li><%= s %></li>
					<% } %>
					</ul>
					</div>
					<% } %>
				</li>
			<% } %>
			
			</ul>
			<input type="submit">
		</form>
		
	
	
	</body>
</html>