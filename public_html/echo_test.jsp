<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Format Test</title>
  </head>
  <body>
		
		<form action="sp_predict/formpost/xmlecho" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Echo XML Service call via sp_predict post</label>
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
				<input type="submit" name="submit" value="submit"/>
			</fieldset>
		</form>

	<form action="sp_model/formpost/xmlecho" method="post" enctype="application/x-www-form-urlencoded">
		<fieldset title="Model Request 1">
			<label for="xml_input_1">Echo XML request via sp_model</label>
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
			<input type="checkbox" name="mimetype" value="json">json
			<input type="checkbox" name="compress" value="zip">zip
		</fieldset>
	</form>

<hr/>
		<form action="sp_predict/formpost/jsonecho" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Echo JSON Service call via sp_predict post</label>
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
				<input type="submit" name="submit" value="submit"/>
			</fieldset>
		</form>

	<form action="sp_model/formpost/jsonecho" method="post" enctype="application/x-www-form-urlencoded">
		<fieldset title="Model Request 1">
			<label for="xml_input_1">Echo JSON request via sp_model</label>
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
			<input type="checkbox" name="mimetype" value="json">json
			<input type="checkbox" name="compress" value="zip">zip
		</fieldset>
	</form>

	</body>
</html>