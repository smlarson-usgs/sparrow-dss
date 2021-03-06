<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Strict//EN"
"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd">
<%@ page contentType="text/html;charset=ISO-8859-1"%>
<html>
  <head>
    <meta http-equiv="Content-Type" content="text/html; charset=ISO-8859-1"/>
    <title>Misc Test</title>
	 <link rel="icon" href="favicon.ico" />
  </head>
  <body>
		<form action="sp_predict/formpost" method="post" enctype="application/x-www-form-urlencoded">
			<fieldset title="Prediction Request 1">
				<label for="xml_input_1">Prediction Request 1</label>
				<p>
				This request contains an error - note the line precise error reporting.
				</p>
				<textarea id="xml_input_2" name="xmlreq" cols="120" rows="40">
&lt;sparrow-prediction-request&gt;
	&lt;!-- This file is way out of date - its more of an example of where I'd like to take this --&gt;
	&lt;processing-instructions&gt;

	&lt;/processing-instructions&gt;

	&lt;!-- Generic Container for any type of prediction --&gt;
	&lt;predict model-id="1"&gt;
		&lt;!--
		The type of prediction.  One of raw-value or change-from-nominal.
		Future versions may include options to compare two versions of a model, or
		a model at two different times (not sure how I'd store these in the db).
		So, the model-id is specified inside this element rather then at top level,
		since we may need multiple model ids depending on the calculation type.
		--&gt;
		&lt;raw-value&gt;

			&lt;!--
			A list of source adjustments.
			Order is signmificant, but there will be a priority to these.  For instance,
			all individual adjustments will take priority over gross adjustments.
			--&gt;
			&lt;source-adjustments&gt;
				&lt;!--
				Uniformly adjust these sources by the specified multiplier.
				Unadjusted sources are basically passed an implicite '1' value
				--&gt;
				&lt;gross-adjust src="1" coef=".25"/&gt;
				&lt;gross-adjust src="3" coef="2"/&gt;

				&lt;!-- Not implemented, but would this override the gross adj values or augment it? --&gt;
				&lt;criteria-adjust override-gross="true"&gt;
					&lt;gross-adjust coef=".25"/&gt;

					&lt;criteria&gt;
						&lt;group&gt;
							&lt;criteria table="metadata" field="state_code" type="equals"&gt;mn&lt;/criteria&gt;
							&lt;criteria table="metadata" field="elevation" type="gt"&gt;1000&lt;/criteria&gt;
						&lt;/group&gt;
						&lt;or/&gt;
						&lt;group&gt;
							&lt;criteria table="metadata" field="state_code" type="equals"&gt;mn&lt;/criteria&gt;
							&lt;criteria table="source" field="type" type="equals"&gt;point_source&lt;/criteria&gt;
						&lt;/group&gt;
					&lt;/criteria&gt;
				&lt;/criteria-adjust&gt;

				&lt;!--
				Specify the source value for a specific source at a specific reach.
				This is the highest priority and will override any other changes
				--&gt;
				&lt;specific-adjust src="1" reach="1242" value="7839"/&gt;
			&lt;/source-adjustments&gt;
		&lt;/raw-value&gt;
	&lt;/predict&gt;
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




	</body>
</html>