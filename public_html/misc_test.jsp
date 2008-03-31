<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Misc Test</title>
  </head>
  <body>
		<form action="sp_predict/xmlreq" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Prediction Request 1</label>
				<p>
				This request contains an error - note the line precise error reporting.
				</p>
				<textarea id="xml_input_2" name="xmlreq" cols="120" rows="40">
&lt;sparrow-prediction-request>
	&lt;!-- This file is way out of date - its more of an example of where I'd like to take this -->
	&lt;processing-instructions>

	&lt;/processing-instructions>
	
	&lt;!-- Generic Container for any type of prediction -->
	&lt;predict model-id="1">
		&lt;!--
		The type of prediction.  One of raw-value or change-from-nominal.
		Future versions may include options to compare two versions of a model, or
		a model at two different times (not sure how I'd store these in the db).
		So, the model-id is specified inside this element rather then at top level,
		since we may need multiple model ids depending on the calculation type.
		-->
		&lt;raw-value>
			
			&lt;!--
			A list of source adjustments.
			Order is signmificant, but there will be a priority to these.  For instance,
			all individual adjustments will take priority over gross adjustments.
			-->
			&lt;source-adjustments>
				&lt;!--
				Uniformly adjust these sources by the specified multiplier.
				Unadjusted sources are basically passed an implicite '1' value
				-->
				&lt;gross-adjust src="1" coef=".25"/>
				&lt;gross-adjust src="3" coef="2"/>
				
				&lt;!-- Not implemented, but would this override the gross adj values or augment it? -->
				&lt;criteria-adjust override-gross="true">
					&lt;gross-adjust coef=".25"/>
					
					&lt;criteria>
						&lt;group>
							&lt;criteria table="metadata" field="state_code" type="equals">mn&lt;/criteria>
							&lt;criteria table="metadata" field="elevation" type="gt">1000&lt;/criteria>
						&lt;/group>
						&lt;or/>
						&lt;group>
							&lt;criteria table="metadata" field="state_code" type="equals">mn&lt;/criteria>
							&lt;criteria table="source" field="type" type="equals">point_source&lt;/criteria>
						&lt;/group>
					&lt;/criteria>
				&lt;/criteria-adjust>
				
				&lt;!--
				Specify the source value for a specific source at a specific reach.
				This is the highest priority and will override any other changes
				-->
				&lt;specific-adjust src="1" reach="1242" value="7839"/>
			&lt;/source-adjustments>
		&lt;/raw-value>
	&lt;/predict>
&lt;/sparrow-prediction-request>
				</textarea>
				<input type="submit" name="submit" value="submit"/>
				<input type="checkbox" name="mimetype" value="csv">csv
				<input type="checkbox" name="mimetype" value="tab">tab
				<input type="checkbox" name="mimetype" value="excel">excel
				<input type="checkbox" name="echo" value="true">echo
				<input type="checkbox" name="unzip" value="yes" checked="checked">unzip
				
			</fieldset>
		</form>

		

	
	</body>
</html>