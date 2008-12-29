<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Format Test</title>
    <link rel="icon" href="../favicon.ico" >
  </head>
  <body>
  
		<form action="../sp_echojs/formpost/echo" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Echo JSON Request 1">
				<label for="xml_input_1">Echo Test 1: JSON as attachment call via ../sp_echojs post</label>
				<p>
				Any text is fine, even non JSON
				</p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
				This is the story of Goldilocks and the three bears
				</textarea>
				<br/>
				<input type="submit" name="submit" value="submit"/>
				<a href="testResults/echo_test_1_result.json">result as of 2008-12-20</a>
			</fieldset>
		</form>
		
		
		<form action="../sp_predict/formpost/xmlecho" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Echo Test 2: XML Service call via ../sp_predict post</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
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
	&lt;!-- zipped, full mimetype specification used --&gt;
	&lt;response-format name="pre-configured format name" compress="zip"&gt;
		&lt;mime-type&gt;text/csv&lt;/mime-type&gt;
		&lt;template&gt;beige&lt;/template&gt;
		&lt;params&gt;
			&lt;param name="gov.usgswim.WordGenerator.marin-top"&gt;ignore me&lt;/param&gt;
		&lt;/params&gt;
	&lt;/response-format&gt;
&lt;/sparrow-prediction-request>
				</textarea>
				<br/>
				<input type="submit" name="submit" value="submit"/>
				<a href="testResults/echo_test_2_result.xml">result as of 2008-12-20</a>
			</fieldset>
		</form>

	<form action="../sp_model/formpost/xmlecho" method="post" enctype="application/x-www-form-urlencoded">
		<fieldset title="Model Request 1">
			<label for="xml_input_1">Echo Test 3: XML request via ../sp_model</label>
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
			<br/>
			<input type="submit" name="submit" value="submit"/>
			<a href="testResults/echo_test_3_result.xml">result as of 2008-12-20</a>
		</fieldset>
	</form>

<hr/>
		<form action="../sp_predict/formpost/jsonecho" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Echo Test 4: JSON Service call via ../sp_predict post</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
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
	&lt;!-- zipped, full mimetype specification used --&gt;
	&lt;response-format name="pre-configured format name" compress="zip"&gt;
		&lt;mime-type&gt;text/csv&lt;/mime-type&gt;
		&lt;template&gt;beige&lt;/template&gt;
		&lt;params&gt;
			&lt;param name="gov.usgswim.WordGenerator.marin-top"&gt;ignore me&lt;/param&gt;
		&lt;/params&gt;
	&lt;/response-format&gt;
&lt;/sparrow-prediction-request>
				</textarea>
				<br/>
				<input type="submit" name="submit" value="submit"/>
				<a href="testResults/echo_test_4_result.json">result as of 2008-12-20</a>
			</fieldset>
		</form>

	<form action="../sp_model/formpost/jsonecho" method="post" enctype="application/x-www-form-urlencoded">
		<fieldset title="Model Request 1">
			<label for="xml_input_1">Echo Test 5: JSON request via ../sp_model</label>
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
			<br/>
			<input type="submit" name="submit" value="submit"/>
			<a href="testResults/echo_test_5_result.json">result as of 2008-12-20</a>
		</fieldset>
	</form>

	</body>
</html>