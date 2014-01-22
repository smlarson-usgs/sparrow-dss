<!doctype html>
<html>
	<head>
		<jsp:include page="template_meta_tags.jsp" flush="true" />

		<link rel="stylesheet" href="css/left_only.css?rev=1" type="text/css">
		<link rel="stylesheet" href="css/static_custom.css?rev=1" type="text/css">
    	<link rel="stylesheet" href="css/landing_page.css?rev=1" type="text/css">
		<jsp:include page="template_ie7_sizer_fix.jsp" flush="true" />

		<%--  ExtJS Scripts --%>
		<script src="landing/js/ext/adapter/ext/ext-base.js?rev=1" type="text/javascript"></script>
		<script type="text/javascript" src="landing/js/ext/ext-all.js?rev=1"></script>
		<script type="text/javascript">
			Ext.BLANK_IMAGE_URL = 'landing/images/s.gif';<%-- Path to the blank image should point to a valid location on your server --%>
			<%-- Namespace your project's code --%>
			Ext.ns('Sparrow.index');
		</script>

		<%--  cookies script included b/c EXT cookies does not support session cookies --%>
		<script src="landing/js/ui/cookies.js?rev=1" type="text/javascript"></script>

		<script src="landing/js/ui/comp/Controller2.js?rev=1" type="text/javascript"></script>
		<script type="text/javascript">
			var CONTROLLER = new Sparrow.index.Controller({region : 'Any', parameter : 'Any'});
		</script>

		<script src="landing/js/excat/scripts/sarissa.js?rev=1" type="text/javascript"></script>
		<script src="landing/js/excat/scripts/sarissa_ieemu_xpath.js?rev=1"></script>
		<script src="landing/js/ui/comp/CSWClient.js?rev=1" type="text/javascript"></script>
		<script type="text/javascript">
			var CSWClient = new Sparrow.index.CSWClient();
		</script>

		<!-- Animation for changing model list -->
		<script type="text/javascript" src="landing/js/ui/animator.min.js"></script>

		<script src='js/Screencasts.js' type="text/javascript"></script>
        <script src="js/sparrow_rpc.js"></script>

		<%--  onReady File  --%>
		<script src='landing/js/ui/onReady2.js?rev=1' type="text/javascript"></script>

    	<title>SPARROW Decision Support System</title>

		<jsp:include page="template_page_tracking.jsp" flush="true" />

	</head>
	<body>

	<jsp:include page="header-unbalanced.jsp" flush="true" />
	<div id="page-content" class="area-1 area">
		<div id="doc-and-model-container" class="area-content">

			<div id="doc" class="columns-2">

				<div id="reference-links" class="column">
					<h4>Documentation and Further Reading</h4>
					<ul id="documentation-list">
						<li><a title="What is SPARROW?" href="http://pubs.usgs.gov/fs/2009/3019/pdf/fs_2009_3019.pdf" target="_blank">What is SPARROW?</a></li>
						<li><a title="What is the SPARROW Decision Support System?" href="dss.jsp" target="_blank">What is SPARROW Decision Support?</a></li>
						<li><a title="SPARROW Applications & Documentation" href="http://water.usgs.gov/nawqa/sparrow/" target="_blank">SPARROW Applications &amp; Documentation</a></li>
						<li><a title="SPARROW FAQs" href="faq.jsp" target="_blank">SPARROW DSS FAQs</a></li>
					</ul>
				</div>

				<div id="reference-videos" class="column">
					<h4>Tutorial Videos</h4>
					<select id="tutorial-video-select">
						<option value="">Select a video...</option>
						<option value="1tzeR4WkLv0">Working with Sources</option>
						<option value="5K1Smu7Q4Fc">Incremental Yield</option>
						<option value="zrycRF7MeG8">Selecting Downstream Outlets</option>
						<option value="UkC_76uq748">Changing Source Inputs</option>
						<option value="tHnxt2ORNQU">Incremental Yield to an Outlet</option>
						<option value="HG9S4D0Jjfc">Summarizing Delivered Load to Downstream Outlets</option>
					</select>
					<button id="tutorial-video-go-button" onclick="var vids=document.getElementById('tutorial-video-select');openScreencast(vids.children[vids.selectedIndex].value);">Watch now &gt;&gt;</button>
					<h4>Found a bug or have a comment?</h4>
					<p>
						Please send bugs, suggestions and questions to the
						<a title="Contact Email" href="mailto:sparrowdss@usgs.gov?subject=Sparrow%20Map%20Comments">SPARROW Decision Support System Administrator</a>.
					</p>
				 </div>
			</div><!-- /columns-2 -->
			<div id="model-metadata-container">
				<h3 class="divider">Selected Model</h3>
				<div id="metadata" class="">
					<div class="model-select-default-instructions">
						<h4>No model selected</h4>
						<p>Use the filter and selection list to the left to select a model.</p>
					</div>
				</div>
			</div>

		</div><!-- /area-content -->
		<!-- For the benefit of Internet Explorer, place no comments between these next two tags -->
	</div>
	<div class="area-2 area">
		<div id="model-navigation-container" class="area-content">
			<div id="search-by-geo-header">
				<h3>Find a Model by Geographic Location:</h3>
				<p><i>Select a region or state.  When a state is selected, all models containing that state are listed.</i></p>
			</div>
			<img src="landing/images/usa_map.gif" title="US States" alt="US States" width="460" height="270" usemap="#us_mapMap" />
			<select id="region-combo-input" name="region-combo-input" size="1" onChange="CONTROLLER.selectRegion(this);">
				<option value="Any">Any Region or State</option>
				<option value="National">National</option>
				<option value="MRB1">New England and Mid-Atlantic</option>
				<option value="MRB3">Great Lakes, Ohio, Upper Mississippi, and Souris-Red-Rainy</option>
				<option value="MRB2">South Atlantic-Gulf and Tennessee</option>
				<option value="MRB5">Lower Mississippi, Arkansas-White-Red, and Texas-Gulf</option>
				<option value="MRB4">Missouri</option>
				<option value="MRB7">Pacific Northwest</option>
				<option value="MRB8">California</option>
				<option value="MRB6">Rio Grande, Colorado, and Great Basin</option>
				<option value="">-- US States --</option>
				<option value="AL">Alabama</option>
				<option value="AZ">Arizona</option>
				<option value="AR">Arkansas</option>
				<option value="CA">California</option>
				<option value="CO">Colorado</option>
				<option value="CT">Connecticut</option>
				<option value="DE">Delaware</option>
				<option value="FL">Florida</option>
				<option value="GA">Georgia</option>
				<option value="ID">Idaho</option>
				<option value="IL">Illinois</option>
				<option value="IN">Indiana</option>
				<option value="IA">Iowa</option>
				<option value="KS">Kansas</option>
				<option value="KY">Kentucky</option>
				<option value="LA">Louisiana</option>
				<option value="ME">Maine</option>
				<option value="MD">Maryland</option>
				<option value="MA">Massachusetts</option>
				<option value="MI">Michigan</option>
				<option value="MN">Minnesota</option>
				<option value="MS">Mississippi</option>
				<option value="MO">Missouri</option>
				<option value="MT">Montana</option>
				<option value="NE">Nebraska</option>
				<option value="NV">Nevada</option>
				<option value="NH">New Hampshire</option>
				<option value="NJ">New Jersey</option>
				<option value="NM">New Mexico</option>
				<option value="NY">New York</option>
				<option value="NC">North Carolina</option>
				<option value="ND">North Dakota</option>
				<option value="OH">Ohio</option>
				<option value="OK">Oklahoma</option>
				<option value="OR">Oregon</option>
				<option value="PA">Pennsylvania</option>
				<option value="RI">Rhode Island</option>
				<option value="SC">South Carolina</option>
				<option value="SD">South Dakota</option>
				<option value="TN">Tennessee</option>
				<option value="TX">Texas</option>
				<option value="UT">Utah</option>
				<option value="VT">Vermont</option>
				<option value="VA">Virginia</option>
				<option value="WA">Washington</option>
				<option value="WV">West Virginia</option>
				<option value="WI">Wisconsin</option>
				<option value="WY">Wyoming</option>
			</select>

			<div id="search-by-const-header">
				<h3>Find a Model by Modeled Constituent:</h3>
			</div>
			<select id="constituent-combo-input" name="constituent-combo-input" size="1" onChange="CONTROLLER.selectParameter(this);">
				<option value="Any">Any</option>
				<option value="Nitrogen">Nitrogen</option>
				<option value="Phosphorus">Phosphorus</option>
				<option value="OC">Total Organic Carbon</option>
				<option value="SS">Suspended Sediment</option>
				<option value="DS">Total Dissolved Solids</option>
			</select>

			<div id="search-results-container">
				<h3 class="divider">Models matching your criteria <span class="note">(click a model to show details)</span></h3>
				<div id="csw-output">
					<div class="model-select-default-instructions">
						<h4>No models match your criteria</h4>
						<p>Change the region and constituent filters to look for other models.</p>
					</div>
				</div>
			</div>

		</div><!-- area-content -->

	</div><!--  area-2 -->

	<jsp:include page="footer-unbalanced.jsp" flush="true" />


<!-- Fields required for history management -->
<form id="history-form" style="display: none !important;">
	<input type="hidden" id="x-history-field" />
	<iframe id="x-history-frame"></iframe>
</form>
<map name="us_mapMap">
<area shape="poly" coords="132,70,127,106,178,113,180,76" onclick="CONTROLLER.selectRegion('WY')" alt="Wyoming">
<area shape="poly" coords="274,61,269,70,282,101,306,99,304,79,300,72,283,61" onclick="CONTROLLER.selectRegion('WI')" alt="Wisconsin">
<area shape="poly" coords="357,123,352,133,358,145,370,142,383,120,367,122" onclick="CONTROLLER.selectRegion('WV')" alt="West Virginia">
<area shape="poly" coords="43,19,45,35,54,38,54,44,71,45,89,50,98,25,62,16" onclick="CONTROLLER.selectRegion('WA')" alt="Washington">
<area shape="poly" coords="349,155,369,143,387,118,398,129,403,143" onclick="CONTROLLER.selectRegion('VA')" alt="Virginia">
<area shape="rect" coords="399,45,415,56" onclick="CONTROLLER.selectRegion('VT')" alt="Vermont">
<area shape="poly" coords="104,94,124,100,124,107,140,113,134,148,94,143" onclick="CONTROLLER.selectRegion('UT')" alt="Utah">
<area shape="poly" coords="151,201,175,230,186,222,226,264,234,239,260,224,257,188,236,187,209,179,209,162,184,160,182,203" onclick="CONTROLLER.selectRegion('TX')" alt="Texas">
<area shape="poly" coords="293,160,290,174,337,168,346,162,353,155" onclick="CONTROLLER.selectRegion('TN')" alt="Tennessee">
<area shape="poly" coords="184,66,236,67,236,97,182,93" onclick="CONTROLLER.selectRegion('SD')" alt="South Dakota">
<area shape="poly" coords="379,186,391,173,379,168,367,167,356,167,349,170,372,190" onclick="CONTROLLER.selectRegion('SC')" alt="South Carolina">
<area shape="rect" coords="434,90,447,100" onclick="CONTROLLER.selectRegion('RI')" alt="Rhode Island">
<area shape="poly" coords="360,98,363,120,403,113,406,106,404,97,399,90" onclick="CONTROLLER.selectRegion('PA')" alt="Pennsylvania">
<area shape="poly" coords="45,35,27,76,78,89,89,53,55,46,50,41" onclick="CONTROLLER.selectRegion('OR')" alt="Oregon">
<area shape="poly" coords="186,155,254,158,255,185,237,186,210,177,210,160,186,158" onclick="CONTROLLER.selectRegion('OK')" alt="Oklahoma">
<area shape="poly" coords="331,103,344,107,359,100,362,118,351,132,334,131" onclick="CONTROLLER.selectRegion('OH')" alt="Ohio">
<area shape="poly" coords="184,35,184,64,235,66,231,38" onclick="CONTROLLER.selectRegion('ND')" alt="North Dakota">
<area shape="poly" coords="339,170,356,155,406,143,405,159,392,171,374,165,360,164" onclick="CONTROLLER.selectRegion('NC')" alt="North Carolina">
<area shape="poly" coords="366,96,372,80,389,78,392,69,397,61,408,58,409,74,412,88,409,96,400,88" onclick="CONTROLLER.selectRegion('NY')" alt="New York">
<area shape="poly" coords="134,150,132,203,138,200,180,203,183,155" onclick="CONTROLLER.selectRegion('NM')" alt="New Mexico">
<area shape="rect" coords="418,103,434,113" onclick="CONTROLLER.selectRegion('NJ')" alt="New Jersey">
<area shape="rect" coords="434,64,452,76" onclick="CONTROLLER.selectRegion('NH')" alt="New Hampshire">
<area shape="poly" coords="57,86,78,90,103,94,91,143,86,160,51,113" onclick="CONTROLLER.selectRegion('NV')" alt="Nevada">
<area shape="poly" coords="180,96,180,111,195,114,196,121,244,123,237,100,215,98" onclick="CONTROLLER.selectRegion('NE')" alt="Nebraska">
<area shape="poly" coords="102,25,182,35,182,72,133,67,124,70,117,70,109,61,104,42" onclick="CONTROLLER.selectRegion('MT')" alt="Montana">
<area shape="poly" coords="244,121,278,122,296,152,290,158,281,158,256,158" onclick="CONTROLLER.selectRegion('MO')" alt="Missouri">
<area shape="poly" coords="290,174,308,174,311,215,300,216,297,211,285,210,285,185" onclick="CONTROLLER.selectRegion('MS')" alt="Mississippi">
<area shape="poly" coords="233,38,237,66,238,92,277,92,267,66,281,46" onclick="CONTROLLER.selectRegion('MN')" alt="Minnesota">
<area shape="poly" coords="285,58,315,54,329,61,344,88,338,103,317,103,311,80" onclick="CONTROLLER.selectRegion('MI')" alt="Michigan">
<area shape="rect" coords="435,78,454,87" onclick="CONTROLLER.selectRegion('MA')" alt="Massachusetts">
<area shape="rect" coords="426,128,448,137" onclick="CONTROLLER.selectRegion('MD')" alt="Maryland">
<area shape="poly" coords="431,70,424,53,431,27,441,31,452,47" onclick="CONTROLLER.selectRegion('ME')" alt="Maine">
<area shape="poly" coords="259,193,261,203,261,211,262,224,287,230,301,228,297,212,281,212,283,193" onclick="CONTROLLER.selectRegion('LA')" alt="Louisiana">
<area shape="poly" coords="295,158,306,148,318,144,330,137,334,132,349,134,358,148,349,152" onclick="CONTROLLER.selectRegion('KY')" alt="Kentucky">
<area shape="poly" coords="195,123,247,128,254,155,194,152" onclick="CONTROLLER.selectRegion('KS')" alt="Kansas">
<area shape="poly" coords="237,93,276,93,282,106,278,120,244,119" onclick="CONTROLLER.selectRegion('IA')" alt="Iowa">
<area shape="poly" coords="308,106,331,103,331,131,325,139,308,147" onclick="CONTROLLER.selectRegion('IN')" alt="Indiana">
<area shape="poly" coords="287,103,306,101,307,119,307,137,303,148,297,152,287,140,281,121,283,113" onclick="CONTROLLER.selectRegion('IL')" alt="Illinois">
<area shape="poly" coords="99,25,81,89,124,98,127,70,117,72,112,70,104,52" onclick="CONTROLLER.selectRegion('ID')" alt="Idaho">
<area shape="poly" coords="330,173,341,196,339,210,368,209,372,193,351,171" onclick="CONTROLLER.selectRegion('GA')" alt="Georgia">
<area shape="poly" coords="318,215,343,212,368,210,392,245,391,260,365,245,365,223,354,218,338,222" onclick="CONTROLLER.selectRegion('FL')" alt="Florida">
<area shape="rect" coords="417,114,434,126" onclick="CONTROLLER.selectRegion('DE')" alt="Delaware">
<area shape="poly" coords="412,86,414,97,426,91,425,86" onclick="CONTROLLER.selectRegion('CT')" alt="Connecticut">
<area shape="poly" coords="143,109,136,148,190,152,195,115" onclick="CONTROLLER.selectRegion('CO')" alt="Colorado">
<area shape="poly" coords="27,77,25,103,29,129,36,151,59,178,81,181,86,163,50,113,57,86" onclick="CONTROLLER.selectRegion('CA')" alt="California">
<area shape="poly" coords="256,159,257,190,281,190,287,173,289,159" onclick="CONTROLLER.selectRegion('AR')" alt="Arkansas">
<area shape="poly" coords="93,144,134,151,127,204,81,185" onclick="CONTROLLER.selectRegion('AZ')" alt="Arizona">
<area shape="poly" coords="311,174,330,171,338,198,339,210,316,215,317,212,312,211,312,179" onclick="CONTROLLER.selectRegion('AL')" alt="Alabama">
</map>

	</body>
</html>