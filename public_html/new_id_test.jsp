<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>ID By Point Test</title>
  </head>
  <body>
		
		<p>
			Requests the 7 closest reaches to lat/long 40/-100 in model 22<br>
			xml: <a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7</a><br>
			excel: <a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=excel">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=excel</a><br>
			
			csv:<a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=csv">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=csv</a><br>
			tab:<a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=tab">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=tab</a><br>
			json:<a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=json">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=json</a><br>
		
		</p>
		
		<p>
			Same as above, but there is an internal limit to 100 reaches, so the 200 will be ignored<br>
			xml: <a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200</a><br>
			excel: <a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=excel">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=excel</a><br>
			csv:<a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=csv">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=csv</a><br>
			tab:<a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=tab">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=tab</a><br>
			json:<a href="sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=json">sp_idpoint2/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=json</a><br>

		
		</p>
		
		<p>
			The Search for nearest reaches is only performed in a 4 deg. by 4 deg. box
			around the point, so a request like this will probably return zero points.<br>
			xml: <a href="sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7">sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7</a><br>
			excel: <a href="sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=excel">sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=excel</a><br>
			csv: <a href="sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=csv">sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=csv</a><br>
			tab: <a href="sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=tab">sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=tab</a><br>
			json: <a href="sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=json">sp_idpoint2/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=json</a><br>


		</p>
	<form action="sp_idpoint2/formpost" method="post" enctype="application/x-www-form-urlencoded">
		<fieldset title="IDByPoint Request 1">
			<label for="xml_input_1">Prediction Request 1</label>
			<p>
			National Model w/ gross and specific adjustments.
			</p>
			<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
&lt;sparrow-id-request
  xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
	&lt;!-- Eliminated the result-count from the root element - it only makes sense to return one reach -->
	
	&lt;!-- model-id attribute added as a temporary hack -->
	&lt;prediction-context context-id="12312" model-id="22"/>
	
	&lt;!-- Request can include one of these elements: -->
	&lt;point lat="40" long="-100"/> or &lt;reach>1234874&lt;/reach>
	
	&lt;content>
		&lt;!-- The response document can include from zero to all of the following: -->
		&lt;adjustments/>
		&lt;attributes/>
		&lt;predicted/>
		&lt;!-- an 'all' element is also accepted -->
	&lt;/content>
	
	&lt;!--
	This is the same response-format element used in other documents.
	There are lots of other added parts, but application/json or
	applicatoin/xml are really the only two possible values for this docuemnt.
	-->
	&lt;response-format>
		&lt;mime-type>xml&lt;/mime-type>
	&lt;/response-format>
&lt;/sparrow-id-request>
			</textarea>
			<input type="submit" name="submit" value="submit"/>
			<input type="checkbox" name="mimetype" value="json">json
		</fieldset>
	</form>
	</body>
</html>