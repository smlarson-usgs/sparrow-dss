<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<html>
  <head>
    <jsp:include page="template_meta_tags.jsp" flush="true" />
		
    <title>Individual Reach Prediction Service Test</title>
    <link rel="icon" href="../favicon.ico" />
		
		<jsp:include page="template_page_tracking.jsp" flush="true" />
		
  </head>
  <body>

		<form action="sp_indivReachPredict/formpost" method="get" enctype="application/x-www-form-urlencoded">
			<fieldset title="Individual Reach Prediction Service ()">
				<table>
					<tr>
						<td><label for="context-id">context-id*</label></td>
						<td><input type="text" name="context-id"/><em>if you submit a context-id, make sure your context-id is defined first, via the <a href="predictContextServiceTest.jsp">Prediction Context Service</a>!</em></td>
					</tr>
					<tr>
						<td><label for="model">model</label></td>
						<td><input type="text" name="model"/></td>
					</tr>

					<tr>
						<td><label for="reachID">reach identifier</label></td>
						<td><input type="text" name="reachID"/></td>
					</tr>
				</table>
				<b>* </b><i>optional</i>
				<br />
				<input type="submit" value="Submit"/>
			</fieldset>
		</form>

	</body>
</html>