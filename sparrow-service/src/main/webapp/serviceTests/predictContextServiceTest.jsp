<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
	<head>
		<title>Prediction Context Service Test</title>
		<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css"/>
		<link rel="icon" href="../favicon.ico"/>
	</head>
	<body>
		<h2 id="site-title">SPAtially Referenced Regressions On Watershed
Attributes (SPARROW) Model Decision Support</h2>
		<!--<h3 id="internal-only">For Internal USGS Access Only</h3>-->
		<!-- /header -->
		<div id="body">
			<form action="../sp_predictcontext/formpost" method="post" enctype="application/x-www-form-urlencoded">
				<fieldset title="Prediction Context Request 1">
					<label for="xml_input_1">Prediction Request Format</label>
					<p>National Model w/ gross and specific adjustments.</p>
					<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">

				</textarea>
					<input type="submit" name="submit" value="submit"/>
					<!-- 
				<input type="checkbox" name="mimetype" value="csv">csv
				<input type="checkbox" name="mimetype" value="tab">tab
				<input type="checkbox" name="mimetype" value="excel">excel
				<input type="checkbox" name="mimetype" value="json">json
				<input type="checkbox" name="compress" value="zip">zip
				 -->
				</fieldset>
			</form>
		</div>
	</body>
</html>
