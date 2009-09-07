<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
	<head>
    	<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    	<title>ID By Point Test</title>
      <link rel="icon" href="../favicon.ico" />
	</head>
	<body>


		<form method="post" action="../sp_idpoint/formpost" enctype="application/x-www-form-urlencoded">
			<fieldset>
				<legend><b>Identify By Point (POST)</b></legend>

				<h4>Identify by Point</h4>
				<p>Note that a preliminary prediction context request will probably need to be submitted first, otherwise this may fail. </p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
<sparrow-id-request xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<context-id>1943959558</context-id>
	<point lat="42.34130859375" long="-95.767822265625"/>
	<content>
		<adjustments/>
		<attributes/>
	</content>
	<response-format>
		<mime-type>xml</mime-type>
	</response-format>
</sparrow-id-request>
				</textarea>
				<input type="submit" name="submit" value="post"/>

			</fieldset>
		</form>
<!-- TODO Make this into a get request
		<form method="get" action="../sp_idpoint" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Context Request 1">
				<label for="xml_input_1">identify Request Format</label>
				<p>Identify by Point</p>
				<p>Note that a preliminary prediction context request will probably need to be submitted first, otherwise this may fail. </p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
<sparrow-id-request xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	<context-id>1943959558</context-id>
	<point lat="42.34130859375" long="-95.767822265625"/>
	<content>
		<adjustments/>
		<attributes/>
	</content>
	<response-format>
		<mime-type>xml</mime-type>
	</response-format>
</sparrow-id-request>
				</textarea>
				<input type="submit" name="submit" value="get"/>

			</fieldset>
		</form>
 -->
	</body>
</html>