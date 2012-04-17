<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Help Request</title>
     <link rel="icon" href="../favicon.ico" />
  </head>
  <body>
  		<fieldset>
			<legend>Lookup item</legend>
  			<form action="../sp_help/lookup" method="get">
				<label for="model">model: </label><input name="model" id="model" type="text" />
				<label for="item">item: </label><input name="item" id="item" type="text" />
				<br/><input type="submit" value="submit"/>
			</form>
  		</fieldset>
  		<fieldset>
			<legend>Get all simple keys</legend>
  			<form action="../sp_help/getSimpleKeys" method="get">
				<label for="model">model: </label><input name="model" id="model" type="text" />
				<br/><input type="submit" value="submit"/>
			</form>
  		</fieldset>
  		<fieldset>
			<legend>Get all list keys</legend>
  			<form action="../sp_help/getListKeys" method="get">
				<label for="model">model: </label><input name="model" id="model" type="text" />
				<br/><input type="submit" value="submit"/>
			</form>
  		</fieldset>
  		<fieldset>
			<legend>Get List</legend>
  			<form action="../sp_help/getList" method="get">
				<label for="model">model: </label><input name="model" id="model" type="text" />
				<label for="listKey">list key: </label><input name="listKey" id="listKey" type="text" />
				<br/><input type="submit" value="submit"/>
			</form>
  		</fieldset>
  		The source of this information can be found in the model.xml file under the appropriate model
  		in the folder https://wisvn.er.usgs.gov/repos/dev/usgs/sparrow/dss/core/trunk/src/main/resources/models.
  		Feel free to alter it and check it in, though unless you run locally, the information won't be
  		available until the next deploy.
	</body>
</html>