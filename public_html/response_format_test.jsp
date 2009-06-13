<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Format Test</title>
    <link rel="icon" href="favicon.ico" />
  </head>
  <body>

		<form action="sp_predict/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Prediction Request Format</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;sparrow-prediction-request
  xmlns="http://www.usgs.gov/sparrow/prediction-request/v0_1"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"&gt;

	&lt;predict model-id="22"&gt;
		&lt;change-from-nominal type="perc_change"&gt;
			&lt;source-adjustments&gt;
				&lt;!-- Sort order: 2 --&gt;&lt;gross-src src="4" coef="2"/&gt;
				&lt;!-- Sort order: 1 --&gt;&lt;gross-src src="1" coef=".5"/&gt;
				&lt;!-- Sort order: 4 --&gt;&lt;specific src="2" reach="1787602" value="7.77"/&gt;&lt;!-- VALUE WAS 315.819 --&gt;
				&lt;!-- Sort order: 3 --&gt;&lt;specific src="1" reach="1787601" value="9.99"/&gt;&lt;!-- VALUE WAS 5432.3354442 --&gt;
			&lt;/source-adjustments&gt;
		&lt;/change-from-nominal&gt;
	&lt;/predict&gt;
	&lt;!-- zipped, full mimetype specification used --&gt;
	&lt;response-format name="pre-configured format name" compress="zip"&gt;
		&lt;mime-type&gt;text/csv&lt;/mime-type&gt;
		&lt;template&gt;beige&lt;/template&gt;
		&lt;params&gt;
			&lt;param name="gov.usgswim.WordGenerator.marin-top"&gt;ignore me&lt;/param&gt;
		&lt;/params&gt;
	&lt;/response-format&gt;
&lt;/sparrow-prediction-request&gt;
				</textarea>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv"/>csv
				<input type="checkbox" name="mimetype" value="tab"/>tab
				<input type="checkbox" name="mimetype" value="excel"/>excel
				<input type="checkbox" name="mimetype" value="json"/>json
				<input type="checkbox" name="compress" value="zip"/>zip
			</fieldset>
		</form>


		<form action="sp_model/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Model Request 1">
				<label for="xml_input_1">Model Request Format</label>
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
	&lt;!--  no compression, csv short name used --&gt;
	&lt;response-format name="pre-configured format name" &gt;
		&lt;mime-type&gt;csv&lt;/mime-type&gt;
		&lt;template&gt;beige&lt;/template&gt;
		&lt;params&gt;
			&lt;param name="gov.usgswim.WordGenerator.marin-top"&gt;ignore me for now&lt;/param&gt;
		&lt;/params&gt;
	&lt;/response-format&gt;
&lt;/sparrow-meta-request&gt;
				</textarea>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv"/>csv
				<input type="checkbox" name="mimetype" value="tab"/>tab
				<input type="checkbox" name="mimetype" value="excel"/>excel
				<input type="checkbox" name="mimetype" value="json"/>json
				<input type="checkbox" name="compress" value="zip"/>zip
			</fieldset>
		</form>

	<p>
			Requests the 7 closest reaches to lat/long 40/-100 in model 22<br/>
			xml: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7</a><br/>
			excel: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=excel">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=excel</a><br/>

			csv:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=csv">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=csv</a><br/>
			tab:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=tab">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=tab</a><br/>
			json:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=json">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=json</a><br/>

		</p>
	</body>
</html>