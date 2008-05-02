<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Format Test</title>
  </head>
  <body>
		
		<form action="sp_predictcontext/xmlreq" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Context Request 1">
				<label for="xml_input_1">Prediction Request Format</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?>
&lt;prediction-context
  xmlns="http://www.usgs.gov/sparrow/prediction-schema/v0_2"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	model-id="22">
		&lt;adjustment-groups conflicts="accumulate | supersede">
			&lt;reach-group enabled="true" name="Northern Indiana Plants"> 
				&lt;desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project&lt;/desc>
				&lt;notes>
					I initially selected HUC 01746286 and 01746289,
					but it looks like there are some others plants that need to be included.
					
					As a start, we are proposing a 10% reduction across the board,
					but we will tailor this later based on plant type.
				&lt;/notes>
				&lt;adjustment src="5" coef=".9"/>
				&lt;adjustment src="4" coef=".75"/>
				&lt;logical-set>	
					&lt;criteria attrib="huc8">01746286&lt;/criteria>
				&lt;/logical-set>
				&lt;logical-set>
					&lt;criteria attrib="huc8">01746289&lt;/criteria>
				&lt;/logical-set>
			&lt;/reach-group>
			
			&lt;reach-group enabled="false" name="Southern Indiana Fields">
				&lt;desc>Fields in Southern Indiana&lt;/desc>
				&lt;notes>
					The Farmer's Alminac says corn planting will be up 20% this year,
					which will roughly result in a 5% increase in the aggrecultural source.
					This is an estimate so I'm leaving it out of the runs created	for the EPA.
				&lt;/notes>
				&lt;adjustment src="1" coef="1.05"/>
				&lt;logical-set>
					&lt;criteria attrib="reach" relation="upstream">8346289&lt;/criteria>
				&lt;/logical-set>
				&lt;logical-set>
					&lt;criteria attrib="reach" relation="upstream">9374562&lt;/criteria>
				&lt;/logical-set>
			&lt;/reach-group>
			
			&lt;reach-group enabled="true" name="Illinois">
				&lt;desc>The entire state of Illinois&lt;/desc>
				&lt;notes>The Urban source for Illinois is predicted is to increase 20%.&lt;/notes>
				&lt;adjustment src="2" coef="1.2"/>
				&lt;logical-set>
					&lt;criteria attrib="state-code">il&lt;/criteria>
				&lt;/logical-set>
			&lt;/reach-group>
			
			&lt;reach-group enabled="true" name="Illinois">
				&lt;desc>Wisconsin River Plants&lt;/desc>
				&lt;notes>
					We know of 3 plants on the Wisconsin River which have announced improved
					BPM implementations.
				&lt;/notes>
				&lt;adjustment src="2" coef=".75"/>
				&lt;reach id="483947453">
					&lt;adjustment src="2" coef=".9"/>
				&lt;/reach>
				&lt;reach id="947839474">
					&lt;adjustment src="2" abs="91344"/>
				&lt;/reach>
			&lt;/reach-group>

		&lt;/adjustment-groups>
		&lt;analysis>
			&lt;select>

				&lt;data-series source="1" per="area">incremental&lt;/data-series>
				&lt;agg-function per="area">avg&lt;/agg-function> &lt;!-- rank would be rank of the group within the whole -->
				&lt;analytic-function partition="HUC6">rank-desc&lt;/analytic-function>

				&lt;nominal-comparison type="percent | absolute"/>
			&lt;/select>
			&lt;limit-to>contributors | terminals | area-of-interest&lt;/limit-to>

			&lt;group-by>HUC8&lt;/group-by>
			
		&lt;/analysis>
		
		&lt;terminal-reaches>
			&lt;reach>2345642&lt;/reach>
			&lt;reach>3425688&lt;/reach>
			&lt;reach>5235424&lt;/reach>
			or
			&lt;logical-set/>
		&lt;/terminal-reaches>
		
		&lt;area-of-interest>
			&lt;logical-set/>	
		&lt;/area-of-interest>

&lt;/prediction-context>
				</textarea>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv">csv
				<input type="checkbox" name="mimetype" value="tab">tab
				<input type="checkbox" name="mimetype" value="excel">excel
				<input type="checkbox" name="mimetype" value="json">json
				<input type="checkbox" name="echo" value="true">echo
				<input type="checkbox" name="compress" value="zip">zip
			</fieldset>
		</form>
<!-- 

		<form action="sp_model/xmlreq" method="post" enctype="application/x-www-form-urlencoded">
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
				<input type="checkbox" name="mimetype" value="csv">csv
				<input type="checkbox" name="mimetype" value="tab">tab
				<input type="checkbox" name="mimetype" value="excel">excel
				<input type="checkbox" name="mimetype" value="json">json
				<input type="checkbox" name="echo" value="true">echo
				<input type="checkbox" name="compress" value="zip">zip
			</fieldset>
		</form>
	
	<p>
			Requests the 7 closest reaches to lat/long 40/-100 in model 22<br>
			xml: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7</a><br>
			excel: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=excel">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=excel</a><br>
			
			csv:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=csv">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=csv</a><br>
			tab:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=tab">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=tab</a><br>
			json:<a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&mimetype=json">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;mimetype=json</a><br>

			echo: <a href="sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&echo=yes">sp_idpoint/22&#63;lat=40&amp;long=-100&amp;result-count=7&amp;echo=yes</a><br>
		
		</p>
		-->
	</body>
</html>