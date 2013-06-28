<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Binning Service Test</title>
    <link rel="icon" href="../favicon.ico" />
  </head>
  <body>

		<form action="../sp_binning/formpost/echo" method="get" enctype="application/x-www-form-urlencoded">
			<fieldset title="Binning Service ()">
				<table>
					<tr>
						<td><label for="context-id">context-id</label></td>
						<td><input type="text" name="context-id"/><em>make sure your context-id is defined!</em></td>
					</tr>

					<tr>
						<td><label for="bin-count">bin count</label></td>
						<td><input type="text" name="bin-count"/></td>
					</tr>

					<tr>
						<td></td>
						<td></td>
					</tr>
				</table>
				
				<p>
				<select name="bin-type" >
					<option value="EQUAL_COUNT">EQUAL_COUNT</option>
					<option value="EQUAL_RANGE">EQUAL_RANGE</option>
				</select>
				<br />
				<input type="submit" name="submit" value="post"/>
				</p>
			</fieldset>
		</form>

	</body>
</html>