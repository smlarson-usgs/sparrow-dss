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

		<form method="get" action="../sp_findReachSupport/formpost" enctype="application/x-www-form-urlencoded">
			<fieldset>
				<legend><b>Find Reach Support(GET) get EDA Names</b></legend>
				<input type="text" name="model"/>
				<input type="hidden" name="get" value="name"/>

				<input type="submit" name="Get edanames" value="post"/>

			</fieldset>
		</form>
		<form method="get" action="../sp_findReachSupport/formpost" enctype="application/x-www-form-urlencoded">
			<fieldset>
				<legend><b>Find Reach Support(GET) get EDA Codes</b></legend>
				<input type="text" name="model"/>
				<input type="hidden" name="get" value="code"/>

				<input type="submit" name="Get edacodes" value="post"/>

			</fieldset>
		</form>
	</body>
</html>