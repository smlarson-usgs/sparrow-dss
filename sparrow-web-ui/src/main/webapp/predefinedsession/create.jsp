<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=UTF-8"%>
<html>
  <head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Create / Edit Predefined Session</title>
	<link rel="icon" href="../favicon.ico" />

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="../css/custom.css" />
	<link rel="stylesheet" type="text/css" href="../webjars/extjs/3.4.1.1/resources/css/ext-all.css" />
	<!--<link rel="stylesheet" type="text/css" href="../ext_js/resources/css/visual/form.css" />--> 
	
	<script type="text/javascript" src="../webjars/extjs/3.4.1.1/adapter/ext/ext-base.js"></script>
	<script type="text/javascript" src="../webjars/extjs/3.4.1.1/ext-all.js"></script>
	<script type="text/javascript" src="../js/PredefinedSessionAdmin.js"></script>
	<script type="text/javascript" src="../js/USGSUtils.js"></script>
	<script type="text/javascript">
	    window.onload = function() {
	        if (Sparrow.ui.loadSessionName) {
	    		Ext.Ajax.request({
	    			method: 'GET',
	    			url: 'listPredefSessions',
	    			success: function(r,o){
	    				Sparrow.ui.render_ui(r.responseText);
	    			},
	    			params: {
	    				uniqueCode: Sparrow.ui.loadSessionName
	    			}
	    		});
	        } else {
	        	configForEditPage();
	        }
	    };
	 </script>
  </head>
  <body>
		<%@ include file="../header.jsp" %>
		<h1>Create / Edit a Predefined Session</h1>
		
		<div id="create_edit_form"></div>
		

		<div class="logout-link"><a href="logout.jsp" title="logout">~~ logout ~~</a></div>
		<%@ include file="../footer.jsp" %>
	</body>
</html>