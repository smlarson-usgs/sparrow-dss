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
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Prediction Request 1</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<textarea id="xml_input_2" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?>
&lt;sparrow-prediction-request
  xmlns="http://www.usgs.gov/sparrow/prediction-request/v0_1"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	
	&lt;predict model-id="22">
		&lt;change-from-nominal type="perc_change">
			&lt;source-adjustments>
				&lt;!-- Sort order: 2 -->&lt;gross-src src="4" coef="2"/>
				&lt;!-- Sort order: 1 -->&lt;gross-src src="1" coef=".5"/>
				&lt;!-- Sort order: 4 -->&lt;specific src="2" reach="1787602" value="7.77"/>&lt;!-- VALUE WAS 315.819 -->
				&lt;!-- Sort order: 3 -->&lt;specific src="1" reach="1787601" value="9.99"/>&lt;!-- VALUE WAS 5432.3354442 -->
			&lt;/source-adjustments>
		&lt;/change-from-nominal>
	&lt;/predict>
	
	&lt;response-options>
		&lt;result-filter>
			&lt;near-point result-count="5">
				&lt;point lat="40" long="-100"/>
			&lt;/near-point>
		&lt;/result-filter>
		&lt;result-content>
			&lt;data-series>incremental&lt;/data-series>
		&lt;/result-content>
	&lt;/response-options>
	
&lt;/sparrow-prediction-request>
				</textarea>
				<input type="submit" name="submit" value="submit"/>
			</fieldset>
		</form>
		
	</body>
</html>