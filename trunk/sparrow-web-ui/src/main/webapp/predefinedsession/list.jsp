<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
  <head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>List Predefined Sessions</title>
	<link rel="icon" href="../favicon.ico" />

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="../css/custom.css" />

	<script type="text/javascript" src="../ext_js/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="../ext_js/ext-all.js"></script>
	<script type="text/javascript" src="../js/PredefinedSessionAdmin.js"></script>
	<script type="text/javascript">
		Ext.onReady(loadAllModels);
	 </script>
  </head>
  <body>
		<%@ include file="../header.jsp" %>
		<h1>List Predefined Session</h1>
		
		<table id="pre-session-admin-list">
			<thead>
				<tr>
					<th>Model</th>
					<th>Code</th>
					<th>Name</th>
					<th>Group</th>
					<th>Type</th>
					<th>Approved</th>
					<th>Sort</th>
					<th>Add By</th>
					<th>Add Date</th>
					<th>Description</th>
				</tr>
			</thead>
			<tbody id="pre-session-admin-list-body">
			
			</tbody>
		</table>
		<div class="logout-link"><a href="logout.jsp" title="logout">~~ logout ~~</a></div>
		<%@ include file="../footer.jsp" %>
	</body>
</html>