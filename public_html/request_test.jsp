<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Test</title>
  </head>
  <body>
		<form action="sp_predict/xmlreq" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request">
				<label for="xml_input">XML Prediction Request</label>
				<textarea id="xml_input" name="xmlreq" cols="60" rows="40">
&lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot; ?&gt;
&lt;sparrow-prediction-request
  xmlns=&quot;http://www.usgs.gov/sparrow/prediction-request/v0_1&quot;
  xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;&gt;
  &lt;response&gt;
    &lt;data-series&gt;incremental&lt;/data-series&gt;
  &lt;/response&gt;
  &lt;value-prediction&gt;
    &lt;model-id&gt;1&lt;/model-id&gt;
    &lt;source-adjustments&gt;
      &lt;specific src=&quot;2&quot; reach=&quot;2049&quot; value=&quot;7.77&quot;/&gt;&lt;!-- VALUE WAS 0 --&gt;
      &lt;specific src=&quot;1&quot; reach=&quot;2050&quot; value=&quot;9.99&quot;/&gt;&lt;!-- VALUE WAS 1.4991568171 --&gt;
      &lt;specific src=&quot;3&quot; reach=&quot;285&quot; value=&quot;11.11&quot;/&gt;&lt;!-- VALUE WAS 0.407362758 --&gt;
    &lt;/source-adjustments&gt;
  &lt;/value-prediction&gt;
&lt;/sparrow-prediction-request&gt;
				</textarea>
				<input type="submit" name="submit" value="submit"/>
			</fieldset>
		</form>
	
	</body>
</html>