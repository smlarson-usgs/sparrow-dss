<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Model Test</title>
  </head>
  <body>
		
		<form action="sp_model/xmlreq" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Model Request 1">
				<label for="xml_input_1">Model Request 1</label>
				<p>
				National Model w/ gross and specific adjustments.
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
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv">csv
				<input type="checkbox" name="mimetype" value="tab">tab
				<input type="checkbox" name="mimetype" value="excel">excel
			</fieldset>
		</form>
		
		<form action="sp_predict/xmlreq" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 2">
				<label for="xml_input_2">Prediction Request 2</label>
				<p>
				Slightly changed from Request 1 to see the result of a specific reach change.
				</p>
				<textarea id="xml_input_2" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;sparrow-meta-request
  xmlns="http://www.usgs.gov/sparrow/meta_request/v0_1"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;
	&lt;model public="false" archived="true" approved="false"&gt;
	&lt;/model&gt;
&lt;/sparrow-meta-request&gt;
				</textarea>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv">csv
				<input type="checkbox" name="mimetype" value="tab">tab
				<input type="checkbox" name="mimetype" value="excel">excel
			</fieldset>
		</form>
	
	</body>
</html>