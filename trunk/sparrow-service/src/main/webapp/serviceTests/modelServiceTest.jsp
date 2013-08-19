<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
	"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
	<title>Model Test</title>
	<link rel="icon" href="../favicon.ico" />
</head>
<body>

	<form action="../sp_model/formpost" method="post" enctype="application/x-www-form-urlencoded">
		<fieldset title="Model Request 1">
		<label for="xml_input_1">Model Request 1</label>
		<p>
			Return all public non-archived approved SPARROW models
		</p>
		<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;sparrow-meta-request
		xmlns="http://www.usgs.gov/sparrow/meta_request/v0_1"
		xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;
	&lt;model public="true" archived="false" approved="true"&gt;
		&lt;source/&gt;
	&lt;/model&gt;
&lt;/sparrow-meta-request&gt;
		</textarea>
		<br/>
		<input type="submit" name="submit" value="submit"/>
		<input type="checkbox" name="mimetype" value="csv"/>csv
		<input type="checkbox" name="mimetype" value="tab"/>tab
		<input type="checkbox" name="mimetype" value="excel"/>excel
		<input type="checkbox" name="mimetype" value="json"/>json
		<input type="checkbox" name="compress" value="zip"/>zip

		<br/>
		<a href="testResults/model.xml">result as of 2008-12-20</a>
		</fieldset>
	</form>


</body>
</html>