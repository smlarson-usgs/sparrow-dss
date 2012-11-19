<!doctype html><%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
    <head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" > 
		
		<meta name="description" content ="SPARROW Decision Support tool - an online version of the SPARROW Models with support for scenario testing."> 
		<meta name="author" content="See http://water.usgs.gov/nawqa/sparrow for authorship of the SPARROW model.  Site and online capabilities created by CIDA, http://cida.usgs.gov"> 
			<!--required for formal publications and recommended for all other pages by the USGS web standards--> 
		<meta name="keywords" content="SPARROW, Decision Support, USGS, CIDA, Map"> 
		<meta name="publisher" content="Middleton Data Center, Wisconsin Science Center"> 
		<meta name="abstract" content=""> 
			<!--required for formal publications and recommended for all other pages by the USGS web standards--> 
		<meta name="created" content="	Date indicating when page or document collection was first published. This date 
			corresponds to the formal date of publication appearing on a print publication"> 
			<!--Format: "YYYYMMDD".  Required for formal publications and recommended for all other pages by the USGS web standards--> 
		<meta name="revised" content="The date the page content was last revised formally"> 
			<!--Format: "YYYYMMDD".  Required for formal publications.  Not to be confused with "Last Monification" date--> 
		<meta name="expires" content="Date indicating when page or document collection becomes obsolete and should be 
			replaced or abandoned"> 
			<!--Format: "YYYYMMDD".  Optional but recommended.  "Never" and "NA" (not applicable) are also valid--> 
		<meta name="review" content="Date indicating when a page or document collection is due for review"> 
			<!--Format: "YYYYMMDD". Optional but recommended.--> 		
		<meta name="country" content="USA">
	
		<link rel="icon" href="favicon.ico" type="image/x-icon">
	
		<%
			String baseUrl = "http://water.usgs.gov/nawqa/sparrow/dss/";
			String screencastUrl = baseUrl + "screencast.jsp";
		%>
		
		<link rel="stylesheet" href="css/full_width.css?rev=1" type="text/css">
    	<link rel="stylesheet" href="css/static_custom.css?rev=1" type="text/css">
    	<link rel="stylesheet" href="css/dss_page.css?rev=1" type="text/css">
		<!--[if lte IE 7]>
		<style type="text/css">
		#sizer {
			width:expression(document.body.clientWidth > 1440 ? "500px" : "78%" );
		}
		
		<%-- 
		Fix for ugly extra padding and button format:
		http://latrine.dgx.cz/the-stretched-buttons-problem-in-ie
		--%>
		button, new-empty-session button { padding: .1em .2em !important; margin: .2em; overflow: visible; }

		</style>
		<![endif]-->
		<title>SPARROW Decision Support System</title>
    </head>
    <body>


		<!--
			The SIZER, EXPANDER, and WRAPPER are used to make the content of the page
			'springy', so that it expands with the browser window and maintains some padding. --> 
		<div id="sizer"> 
			<div id="expander"> 
				<div id="wrapper" class="clearfix"> 
					<div id="header"> 
            			<div id="banner-area"> 
							<h1>US Geological Survey</h1><!-- Not actually visible unless printed --> 
							<div id="usgs-header-logo"><a href="http://www.usgs.gov" title="Link to the US Geological Survey main web page"><img alt="USGS Logo" src="images/usgs_logo_small.jpg" ></a></div> 
							<div id="usgsPrintCommHeader" class="print-only"> 
								<h3 id="printCommType">Web Page Hardcopy</h3> 
								<p class="hide">The section 'Web Page Hardcopy' is only visible when printed.  Ignore if viewing with style sheets turrned off</p> 
								<p id="printCommDate"> 
									<script type="text/javascript">document.write(new Date().toLocaleString());</script> 
								</p> 
								<p id="printCommPrintFrom">Printed From: <script type="text/javascript">document.write(document.location.href);</script></p> 
								<p> 
									This print version of the page is optimized to print only the
									content portions of the web page you were viewing and is not
									intended to have the same formatting as the original page.
								</p> 
							</div> 
						</div>
						<div id="ccsa-area"> 
							<h4 class="access-help">Top Level USGS Links</h4> 
							<ul> 
								<li><a href="http://www.usgs.gov/" title="Link to main USGS page">USGS Home</a></li> 
								<li><a href="http://www.usgs.gov/ask/index.html" title="Link to main USGS contact page">Contact USGS</a></li> 
								<li><a href="http://search.usgs.gov/" title="Link to main USGS search (not publications search)">Search USGS</a></li> 
							</ul> 
						</div> 
		            </div><!-- End content --> 
		            <h2 id="site-title">SPARROW Decision Support System</h2> 
					<div id="quick-links" class="access-help"> 
						<h4>Quick Page Navigation</h4> 
							<ul title="links to portions of this page.  Details:  Not normally visible and intended for screen readers.  Page layout has the content near top. Links opening new windows are noted in titles."> 
							<li><a href="#page-content" title="Main content of this page.  Starts with the pages name.">Page Main Content</a></li> 
							<li><a href="#site-top-links" title="Short list of top pages within the site.  Before page content.">Top Pages Within This Site</a></li> 
							<li><a href="#site-full-links" title="Complete list of page within the site.  After page content.">All Pages Within This Site</a></li> 
							<li><a href="#full-navigation" title="Pages within the site and external links.  After page content.">All Site Pages Plus External Links</a></li> 
							<li><a href="#validation-info" title="HTML and CSS validation links for this page.  After page content.">HTML and CSS Validation Info</a></li> 
							<li><a href="#footer" title="Mainenance info, general USGS links.  Bottom of page, after content.">Misc. Page Info</a></li> 
						</ul> 
					</div> 
					<div id="page-area-container" class="area-container"> 
						<div id="page-content" class="area-1 area"> 
							<div id="doc-and-model-container" class="area-content"> 
					


		<div id="dss-description">
			<div class="preserve-format">
				<div id="overall-snapshot">
					<img src="landing/images/overall_sparrow.png" alt="SPARROW DSS map user interface"/>
					<p>
					Snapshot of the SPARROW Decision Support (Sparrow DSS) user interface, showing the national nitrogen model.
					</p>
				</div>
				<p>
				The SPARROW <b>D</b>ecision <b>S</b>upport <b>S</b>ystem (SPARROW DSS) provides access to national, regional, and basin-wide SPARROW models
				(<a href="http://water.usgs.gov/nawqa/sparrow/">Spatially Referenced Regressions On Watershed attributes</a>)
				for water managers, researchers, and the general public.
				Models are available for a variety of water-quality constituents and time periods.
				For each model, users can:
				</p>
				
				<ul>
				<li>Map predictions of long-term average water-quality conditions (loads, yields, concentrations) and source contributions by stream reach and catchment</li>
				<li>Track transport to downstream receiving waters, such as reservoirs and estuaries</li>
				<li>Evaluate management source-reduction scenarios</li>
				<li>Overlay land use, shaded relief, street-level data, states, counties, and hydrologic units.</li>
				</ul>
				
				<p>
				Differences among models for the same constituent, time period,
				and geographical area reflect an evolution of the model applications,
				technology, and geospatial data as described in supporting documentation.
				</p>

			</div>
			
			<div class="preserve-format">
				<h4>Further Reading</h4>
				<ul>
					<li><a title="Journal article with much more detail on SPARROW DSS" href="http://onlinelibrary.wiley.com/doi/10.1111/j.1752-1688.2011.00573.x/abstract" target="_blank">SPARROW Decision Support JAWRA journal article</a></li>
					<li><a title="What is SPARROW?" href="http://pubs.usgs.gov/fs/2009/3019/pdf/fs_2009_3019.pdf">What is SPARROW?</a></li>
					<li><a title="SPARROW Applications & Documentation" href="http://water.usgs.gov/nawqa/sparrow/">SPARROW Applications &amp; Documentation</a></li>
					<li><a title="SPARROW FAQs" href="http://water.usgs.gov/nawqa/sparrow/FAQs/faq.html">SPARROW FAQs</a></li>
				</ul>
			</div>
		</div>

							</div><!-- area-content -->

						</div><!--  area-2 -->

					</div><!-- end of #page-area-container --> 
					
<div id="usgs-footer-panel" class="${param['footer-class']}">
    <div id="footer" style="width: 100%; margin-right: -1em;">
        <div id="usgs-policy-links">
            <h4 class="access-help">USGS Policy Information Links</h4>
            <ul class="hnav">
                <li><a href="http://www.usgs.gov/accessibility.html" title="USGS web accessibility policy">Accessibility</a></li>
                <li><a href="http://www.usgs.gov/foia/" title="USGS Freedom of Information Act information">FOIA</a></li>
                <li><a href="http://www.usgs.gov/privacy.html" title="USGS privacy policies">Privacy</a></li>
                <li><a href="http://www.usgs.gov/policies_notices.html" title="USGS web policies and notices">Policies and Notices</a></li>
            </ul>
        </div><!-- end usgs-policy-links -->
        <div class="content">
            <div id="page-info">
                <p id="footer-doi-links">
                    <span class="vcard">
                        <a class="url fn org" href="http://www.doi.gov/" title="Link to the main DOI web site">U.S. Department of the Interior</a>
                        <span class="adr">
                            <span class="street-address">1849 C Street, N.W.</span><br />
                            <span class="locality">Washington</span>, <span class="region">DC</span>
                            <span class="postal-code">20240</span>
                        </span>
                        <span class="tel">202-208-3100</span>
                    </span><!-- vcard --> | 
                    <span class="vcard">
                        <a class="url fn org" href="http://www.usgs.gov" title="Link to the main USGS web site">U.S. Geological Survey</a>
                        <span class="adr">
                            <span class="post-office-box">Box 25286</span><br />
                            <span class="locality">Denver</span>, <span class="region">CO</span>
                            <span class="postal-code">8022</span>
                        </span>
                    </span><!-- vcard -->
                </p>
	            <p id="footer-page-url">URL: <a href="http://water.usgs.gov/nawqa/sparrow/dss/">http://water.usgs.gov/nawqa/sparrow/dss/</a></p>
	            <p id="footer-contact-info">Page Contact Information: <a title="Contact Email" href="mailto:sparrowdss@usgs.gov?subject=Sparrow Map Comments">SPARROW DSS Administrator</a></p>
	            <p id="footer-page-modified-info">Page Last modified: ${buildTime} <span id="versionInfo">(Version: ${pom.version} (${timestamp}) - ${deployment_profile})</span></p>
            </div><!-- /page-info -->
            <div id="gov-buttons">
                <a title="link to the official US Government web portal" href="http://firstgov.gov/">
                    <img src="images/footer_graphic_firstGov.jpg" alt="FirstGov button"/>
                </a>
                <a title="Link to Take Pride in America, a volunteer organization that helps to keep America's public lands beautiful." href="http://www.takepride.gov/">
                    <img src="images/footer_graphic_takePride.jpg" alt="Take Pride in America button"/>
                </a>
            </div><!-- /gov-buttons -->
        </div><!-- /content -->
    </div><!-- /footer -->
</div><!-- /footer panel -->
					
				</div> <!-- /wrapper --> 
			</div> <!-- /expander --> 
		</div> <!-- /sizer --> 
		
	    
	</body> 

</html>