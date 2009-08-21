<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
	<head>
 		<meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
		<title>Saved Session Test</title>
		<link rel="icon" href="../favicon.ico" />
	</head>
	<body>
		<form action="../sp_session/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Saved Session Request">
				<label for="model">[required] model:</label>
				<input name="model" type="text" />
				<br/>
				<label for="session">[optional] session:</label>
				<input name="session" type="text" />
				<br/>
				<input type="submit" name="submit" value="submit"/>
				<br/>
				<a href="testResults/jsonify.json">result as of 2009-08-16</a> <i>Note: Use 32 for the model, as it's the only one with anything in it</i>
			</fieldset>
		</form>
	</body>
</html>