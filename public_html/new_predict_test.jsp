<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Test</title>
  </head>
  <body>
		
		<form action="sp_predict" method="get" enctype="application/x-www-form-urlencoded">
			<fieldset title="Exporting a prediction">
				<label for="xml_input_1">Getting a Prediction Export</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<input type="text" name="context-id" size="26"/>
				<input type="submit" name="submit" value="submit"/>
				<input type="radio" name="mime-type" value="csv">csv
				<input type="radio" name="mime-type" value="tab">tab
				<input type="radio" name="mime-type" value="excel">excel
				<input type="radio" name="mime-type" value="json">json
		<!-- 	<input type="checkbox" name="compress" value="zip">zip -->	
			</fieldset>
		</form>
		
	
	</body>
</html>