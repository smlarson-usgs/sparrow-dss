<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>ID By Point Test</title>
    <link rel="icon" href="favicon.ico" >
  </head>
  <body>
		<!-- 
		<p>
			Requests the 7 closest reaches to lat/long 40/-100 in model 22<br>
			xml: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7</a><br>
			excel: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=excel">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=excel</a><br>
			
			csv:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=csv">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=csv</a><br>
			tab:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=tab">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=tab</a><br>
			json:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=json">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=json</a><br>
		
		</p>
		
		<p>
			Same as above, but there is an internal limit to 100 reaches, so the 200 will be ignored<br>
			xml: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200</a><br>
			excel: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=excel">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=excel</a><br>
			csv:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=csv">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=csv</a><br>
			tab:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=tab">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=tab</a><br>
			json:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&mimetype=json">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=200&amp;mimetype=json</a><br>

		
		</p>
		
		<p>
			The Search for nearest reaches is only performed in a 4 deg. by 4 deg. box
			around the point, so a request like this will probably return zero points.<br>
			xml: <a href="sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7">sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7</a><br>
			excel: <a href="sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=excel">sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=excel</a><br>
			csv: <a href="sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=csv">sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=csv</a><br>
			tab: <a href="sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=tab">sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=tab</a><br>
			json: <a href="sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&mimetype=json">sp_idpoint/22&#63;lat=4&amp;long=-4&amp;result-count=7&amp;mimetype=json</a><br>


		</p>
		
		 -->
	<form action="sp_idpoint/formpost" method="post" enctype="application/x-www-form-urlencoded">
		<fieldset title="IDByPoint Request 1">
			<label for="xml_input_1">new IDByPoint Request 1</label>
			<p>
			National Model w/ gross and specific adjustments.
			</p>
			<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
&lt;?xml version=&quot;1.0&quot; encoding=&quot;ISO-8859-1&quot; ?&gt;
&lt;sparrow-id-request
  xmlns=&quot;http://www.usgs.gov/sparrow/id-point-request/v0_2&quot;
	xmlns:xsi=&quot;http://www.w3.org/2001/XMLSchema-instance&quot;&gt;
	
	&lt;!-- The user does not have a prediction context yet, so we use the model-id here --&gt;
	&lt;model-id&gt;22&lt;/model-id&gt;
	&lt;!--  if prediction context were available, it would look like this
		&lt;context-id&gt;720751343&lt;/context-id&gt; --&gt; 
	
	&lt;!-- Where the user clicked --&gt;
	&lt;point lat=&quot;39.5&quot; long=&quot;-76.68&quot;/&gt;
	&lt!-- &lt;reach id="3074"/&gt; or try a reach rather than a point --&gt;
	&lt;!-- Ask for just the info on the adjustments tab. --&gt;
	&lt;content&gt;
		&lt;adjustments/&gt;
		&lt;!-- &lt;attributes/&gt; uncomment this to get attributes --&gt;
		&lt;!-- &lt;predicted/&gt;  uncomment this to get predicted --&gt;
	&lt;/content&gt;
	
	&lt;!-- Get the response back as JSON --&gt;
	&lt;response-format&gt;
		&lt;mime-type&gt;XML&lt;/mime-type&gt;
	&lt;/response-format&gt;
&lt;/sparrow-id-request&gt;
			</textarea>
			<input type="submit" name="submit" value="submit"/>
			<input type="checkbox" name="mimetype" value="JSON">json
		</fieldset>
	</form>
	</body>
</html>