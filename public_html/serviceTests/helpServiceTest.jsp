<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Request Format Test</title>
     <link rel="icon" href="../favicon.ico" />
  </head>
  <body>

		<form action="../sp_jsonify/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="JSONify Prediction Context Requ est">
				<label for="xml_input_1">Prediction Request Format</label>
				<p>
				National Model w/ gross and specific adjustments.
				</p>
				<textarea id="xml_input_1" name="xmlreq" cols="120" rows="20">
&lt;?xml version="1.0" encoding="ISO-8859-1" ?&gt;
&lt;prediction-context
  xmlns="http://www.usgs.gov/sparrow/prediction-schema/v0_2"
	xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	model-id="22"&gt;
		&lt;adjustment-groups conflicts="accumulate | supersede"&gt;
			&lt;reach-group enabled="true" name="Northern Indiana Plants"&gt;
				&lt;desc&gt;Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project&lt;/desc&gt;
				&lt;notes&gt;
					I initially selected HUC 01746286 and 01746289,
					but it looks like there are some others plants that need to be included.

					As a start, we are proposing a 10% reduction across the board,
					but we will tailor this later based on plant type.
				&lt;/notes&gt;
				&lt;adjustment src="5" coef=".9"/&gt;
				&lt;adjustment src="4" coef=".75"/&gt;
				&lt;logical-set&gt;
					&lt;criteria attrib="huc8"&gt;01746286&lt;/criteria&gt;
				&lt;/logical-set&gt;
				&lt;logical-set&gt;
					&lt;criteria attrib="huc8"&gt;01746289&lt;/criteria&gt;
				&lt;/logical-set&gt;
			&lt;/reach-group&gt;

			&lt;reach-group enabled="false" name="Southern Indiana Fields"&gt;
				&lt;desc&gt;Fields in Southern Indiana&lt;/desc&gt;
				&lt;notes&gt;
					The Farmer's Alminac says corn planting will be up 20% this year,
					which will roughly result in a 5% increase in the aggrecultural source.
					This is an estimate so I'm leaving it out of the runs created	for the EPA.
				&lt;/notes&gt;
				&lt;adjustment src="1" coef="1.05"/&gt;
				&lt;logical-set&gt;
					&lt;criteria attrib="reach" relation="upstream"&gt;8346289&lt;/criteria&gt;
				&lt;/logical-set&gt;
				&lt;logical-set&gt;
					&lt;criteria attrib="reach" relation="upstream"&gt;9374562&lt;/criteria&gt;
				&lt;/logical-set&gt;
			&lt;/reach-group&gt;

			&lt;reach-group enabled="true" name="Illinois"&gt;
				&lt;desc&gt;The entire state of Illinois&lt;/desc&gt;
				&lt;notes&gt;The Urban source for Illinois is predicted is to increase 20%.&lt;/notes&gt;
				&lt;adjustment src="2" coef="1.2"/&gt;
				&lt;logical-set&gt;
					&lt;criteria attrib="state-code"&gt;il&lt;/criteria&gt;
				&lt;/logical-set&gt;
			&lt;/reach-group&gt;

			&lt;reach-group enabled="true" name="Illinois"&gt;
				&lt;desc&gt;Wisconsin River Plants&lt;/desc&gt;
				&lt;notes&gt;
					We know of 3 plants on the Wisconsin River which have announced improved
					BPM implementations.
				&lt;/notes&gt;
				&lt;adjustment src="2" coef=".75"/&gt;
				&lt;reach id="483947453"&gt;
					&lt;adjustment src="2" coef=".9"/&gt;
				&lt;/reach&gt;
				&lt;reach id="947839474"&gt;
					&lt;adjustment src="2" abs="91344"/&gt;
				&lt;/reach&gt;
			&lt;/reach-group&gt;

		&lt;/adjustment-groups&gt;
		&lt;analysis&gt;
			&lt;select&gt;

				&lt;data-series source="1" per="area"&gt;incremental&lt;/data-series&gt;
				&lt;agg-function per="area"&gt;avg&lt;/agg-function&gt; &lt;!-- rank would be rank of the group within the whole --&gt;
				&lt;analytic-function partition="HUC6"&gt;rank-desc&lt;/analytic-function&gt;

				&lt;nominal-comparison type="percent | absolute"/&gt;
			&lt;/select&gt;
			&lt;limit-to&gt;contributors | terminals | area-of-interest&lt;/limit-to&gt;

			&lt;group-by&gt;HUC8&lt;/group-by&gt;

		&lt;/analysis&gt;

		&lt;terminal-reaches&gt;
			&lt;reach&gt;2345642&lt;/reach&gt;
			&lt;reach&gt;3425688&lt;/reach&gt;
			&lt;reach&gt;5235424&lt;/reach&gt;
			or
			&lt;logical-set/&gt;
		&lt;/terminal-reaches&gt;

		&lt;area-of-interest&gt;
			&lt;logical-set/&gt;
		&lt;/area-of-interest&gt;

&lt;/prediction-context&gt;
				</textarea>
				<br/>
				<input type="submit" name="submit" value="submit"/>
				<br/>
				<a href="testResults/jsonify.json">result as of 2008-12-20</a>
			</fieldset>
		</form>

	</body>
</html>