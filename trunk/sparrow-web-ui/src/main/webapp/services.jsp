<!doctype html>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="gov.usgswim.sparrow.SparrowUtil" %>
<%@page import="gov.usgswim.sparrow.SparrowUtil.UrlFeatures" %>
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
			UrlFeatures urlFeatures = SparrowUtil.getRequestUrlFeatures(request);
			String baseUrl = urlFeatures.getBaseUrlWithSlash();
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
				<style type="text/css">
			@page {  }
			table { border-collapse:collapse; border-spacing:0; empty-cells:show }
			td, th { vertical-align:top; font-size:12pt;}
			h1, h2, h3, h4, h5, h6 { clear:both }
			ol, ul { margin:0; padding:0;}
			li { list-style: none; margin:0; padding:0;}
			li span { clear: both; line-height:0; width:0; height:0; margin:0; padding:0; }
			span.footnodeNumber { padding-right:1em; }
			span.annotation_style_by_filter { font-size:95%; font-family:Arial; background-color:#fff000;  margin:0; border:0; padding:0;  }
			* { margin:0;}
			.P1 { font-size:12pt; font-family:Times New Roman; writing-mode:page; }
			.T1 { color:#8b26c9; }
			.T2 { color:#000000; }
			.T3 { color:#000096; }
			.T4 { color:#f5844c; }
			.T5 { color:#ff8040; }
			.T6 { color:#993300; }
			.T7 { color:#0099cc; }
			.T8 { color:#006400; }
	</style>

		<title>SPARROW Decision Support System</title>
		
		<jsp:include page="template_page_tracking.jsp" flush="true" />
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
					


		<p> The user interface of the SPARROW DSS application is built on top of a service
			architecture that can be used independently of the user interface by other applications.
			For example, a researcher could create a Sparrow model scenario with complex adjustments
			to individual reaches, and automate requesting those model results in a machine readable
			format. This is typically done as a two step process: </p>
		<ol>
			<li>Register a <i>Prediction Context</i> which contains all the source adjustments for
				the scenario</li>
			<li>Request an export of the scenarios results</li>
		</ol>
		
		<p>
			To register a prediction context, submit a Prediction Context XML document to:<br />
			<code><%= baseUrl + "getContextId"%></code><br/>
			The context can be submitted as the body of the POST request without a parameter name.
			The response back will contain a context-id to use to request the export.
		</p>
		
		<p>
			To request the export, submit a export request referencing the context-id to:<br />
			<code><%= baseUrl + "getPredict"%></code><br/>
			The XML export request must be POSTed as a parameter named <i>xmlreq</i> 
		</p>
		
		
		<p>For simplicity, these two steps can be optionally be combined into a single request
			and submitted to <i>getPredict</i>:</p>
		<p class="P1"><span class="T1">&lt;?xml version="1.0" encoding="UTF-8" ?&gt;</span><span
				class="T2"><br /></span><span class="T3">&lt;sparrow-report-request</span><span
				class="T4"> xmlns</span><span class="T5">=</span><span class="T6"
				>"http://www.usgs.gov/sparrow/prediction-schema/v0_2"</span><span class="T2"
				><br /></span><span class="T4">        </span><span class="T7">xmlns:xsi</span><span
				class="T5">=</span><span class="T6"
				>"http://www.w3.org/2001/XMLSchema-instance"</span><span class="T3">&gt;</span><span
				class="T2"><br /><br />        </span><span class="T8">&lt;!--</span><span
				class="T2"><br /></span><span class="T8">                The PredictionContext
				defines which model is used for the export,</span><span class="T2"
				><br /></span><span class="T8">                the adjustments made to the reaches,
				and the dataseries.</span><span class="T2"><br /></span><span class="T8">       
				--&gt;</span><span class="T2"><br />        </span><span class="T3"
				>&lt;PredictionContext</span><span class="T4"> xmlns</span><span class="T5"
				>=</span><span class="T6"
				>"http://www.usgs.gov/sparrow/prediction-schema/v0_2"</span><span class="T2"
				><br /></span><span class="T4">                </span><span class="T7"
				>xmlns:xsi</span><span class="T5">=</span><span class="T6"
				>"http://www.w3.org/2001/XMLSchema-instance"</span><span class="T4">
				model-id</span><span class="T5">=</span><span class="T6">"50"</span><span class="T3"
				>&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;adjustmentGroups</span><span class="T4"> conflicts</span><span class="T5"
				>=</span><span class="T6">"accumulate"</span><span class="T3">&gt;</span><span
				class="T2"><br /><br />                        </span><span class="T8"
				>&lt;!--</span><span class="T2"><br /></span><span class="T8">                     
				  The same adjustment can be applied to to a set of reaches</span><span class="T2"
				><br /></span><span class="T8">                        by creating a
				reachGroup.</span><span class="T2"><br /></span><span class="T8">                   
				    --&gt;</span><span class="T2"><br />                        </span><span
				class="T3">&lt;reachGroup</span><span class="T4"> enabled</span><span class="T5"
				>=</span><span class="T6">"true"</span><span class="T4"> name</span><span class="T5"
				>=</span><span class="T6">"Any name you want"</span><span class="T3"
				>&gt;</span><span class="T2"><br />                                </span><span
				class="T3">&lt;desc</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                                </span><span
				class="T3">&lt;notes</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                                </span><span
				class="T3">&lt;adjustment</span><span class="T4"> src</span><span class="T5"
				>=</span><span class="T6">"3"</span><span class="T4"> coef</span><span class="T5"
				>=</span><span class="T6">"2"</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                                </span><span
				class="T8">&lt;!-- These are the 1st two reaches --&gt;</span><span class="T2"
				><br />                                </span><span class="T3">&lt;reach</span><span
				class="T4"> id</span><span class="T5">=</span><span class="T6">"9190"</span><span
				class="T4"> </span><span class="T3">/&gt;</span><span class="T2"><br />             
				                  </span><span class="T3">&lt;reach</span><span class="T4">
				id</span><span class="T5">=</span><span class="T6">"6887"</span><span class="T4"
				> </span><span class="T3">/&gt;</span><span class="T2"><br />                       
				</span><span class="T3">&lt;/reachGroup&gt;</span><span class="T2"><br /><br />     
				                  </span><span class="T8">&lt;!-- </span><span class="T2"
				><br /></span><span class="T8">                        Adjustments can be applied to
				individual reaches by creating</span><span class="T2"><br /></span><span class="T8"
				>                        an individualGroup and adding each reach to be adjusted to
				the</span><span class="T2"><br /></span><span class="T8">                       
				group.  Any number of reaches can be added to the individualGroup</span><span
				class="T2"><br /></span><span class="T8">                        but there can be
				only one individualGroup.</span><span class="T2"><br /></span><span class="T8">     
				                  --&gt;</span><span class="T2"><br />                       
				</span><span class="T3">&lt;individualGroup</span><span class="T4">
				enabled</span><span class="T5">=</span><span class="T6">"true"</span><span
				class="T3">&gt;</span><span class="T2"><br />                               
				</span><span class="T3">&lt;reach</span><span class="T4"> id</span><span class="T5"
				>=</span><span class="T6">"9682"</span><span class="T3">&gt;</span><span class="T2"
				><br />                                        </span><span class="T3"
				>&lt;adjustment</span><span class="T4"> src</span><span class="T5">=</span><span
				class="T6">"3"</span><span class="T4"> abs</span><span class="T5">=</span><span
				class="T6">"37172"</span><span class="T4"> </span><span class="T3">/&gt;</span><span
				class="T2"><br />                                        </span><span class="T3"
				>&lt;adjustment</span><span class="T4"> src</span><span class="T5">=</span><span
				class="T6">"2"</span><span class="T4"> coef</span><span class="T5">=</span><span
				class="T6">".5"</span><span class="T4"> </span><span class="T3">/&gt;</span><span
				class="T2"><br />                                </span><span class="T3"
				>&lt;/reach&gt;</span><span class="T2"><br />                               
				</span><span class="T3">&lt;reach</span><span class="T4"> id</span><span class="T5"
				>=</span><span class="T6">"664460"</span><span class="T3">&gt;</span><span
				class="T2"><br />                                        </span><span class="T8"
				>&lt;!-- </span><span class="T2"><br /></span><span class="T8">                     
				                  Reaches can have multiple adjustments of either</span><span
				class="T2"><br /></span><span class="T8">                                       
				'abs' (absolute) or 'coef' (coefficent)</span><span class="T2"><br /></span><span
				class="T8">                                        --&gt;</span><span class="T2"
				><br />                                        </span><span class="T3"
				>&lt;adjustment</span><span class="T4"> src</span><span class="T5">=</span><span
				class="T6">"3"</span><span class="T4"> abs</span><span class="T5">=</span><span
				class="T6">"172"</span><span class="T4"> </span><span class="T3">/&gt;</span><span
				class="T2"><br />                                        </span><span class="T3"
				>&lt;adjustment</span><span class="T4"> src</span><span class="T5">=</span><span
				class="T6">"2"</span><span class="T4"> coef</span><span class="T5">=</span><span
				class="T6">".65"</span><span class="T4"> </span><span class="T3">/&gt;</span><span
				class="T2"><br />                                </span><span class="T3"
				>&lt;/reach&gt;</span><span class="T2"><br />                        </span><span
				class="T3">&lt;/individualGroup&gt;</span><span class="T2"><br />               
				</span><span class="T3">&lt;/adjustmentGroups&gt;</span><span class="T2"
				><br /><br /><br />                </span><span class="T3"
				>&lt;analysis&gt;</span><span class="T2"><br /></span><span class="T2">             
				          </span><span class="T3">&lt;dataSeries</span><span class="T4">
				source</span><span class="T5">=</span><span class="T6">"3"</span><span class="T3"
				>&gt;</span><span class="T2">incremental</span><span class="T3"
				>&lt;/dataSeries&gt;</span><span class="T2"><br />                </span><span
				class="T3">&lt;/analysis&gt;</span><span class="T2"><br />               
				</span><span class="T3">&lt;terminalReaches</span><span class="T4"> </span><span
				class="T3">/&gt;</span><span class="T2"><br />                </span><span
				class="T3">&lt;areaOfInterest</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;nominalComparison</span><span class="T4"> type</span><span class="T5"
				>=</span><span class="T6">"percent_change"</span><span class="T4"> </span><span
				class="T3">/&gt;</span><span class="T2"><br />        </span><span class="T3"
				>&lt;/PredictionContext&gt;</span><span class="T2"><br /><br />        </span><span
				class="T8">&lt;!-- The response-content defines what data is included in the export
				--&gt;</span><span class="T2"><br />        </span><span class="T3"
				>&lt;response-content&gt;</span><span class="T2"><br />                </span><span
				class="T8">&lt;!--  </span><span class="T2"><br /></span><span class="T8">         
				      The export can contain several option sets of columns by</span><span
				class="T2"><br /></span><span class="T8">                including the tags below.
				 The export can become quite</span><span class="T2"><br /></span><span class="T8"> 
				              large as optional columns are added, however.</span><span class="T2"
				><br /></span><span class="T8">                </span><span class="T2"
				><br /></span><span class="T8">                These options correspond the the
				export options in</span><span class="T2"><br /></span><span class="T8">             
				  the DSS application and a complete key is contained</span><span class="T2"
				><br /></span><span class="T8">                at the top of the export itself as a
				comment.</span><span class="T2"><br /></span><span class="T8">               
				--&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;id-attributes</span><span class="T4"> </span><span class="T3">/&gt;</span><span
				class="T2"><br />                </span><span class="T3"
				>&lt;stat-attributes</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;original-source-values</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;adjusted-source-values</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;original-predicted-values</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;adjusted-predicted-values</span><span class="T4"> </span><span class="T3"
				>/&gt;</span><span class="T2"><br />        </span><span class="T3"
				>&lt;/response-content&gt;</span><span class="T2"><br /><br />        </span><span
				class="T3">&lt;response-format&gt;</span><span class="T2"><br />               
				</span><span class="T8">&lt;!--</span><span class="T2"><br /></span><span class="T8"
				>                        Export formats available</span><span class="T2"
				><br /></span><span class="T8">                        The most human readable is
				'xml'.</span><span class="T2"><br /></span><span class="T8">                       
				The most machine readable is 'csv' or 'tsv'.</span><span class="T2"
				><br /></span><span class="T8">                        Other formats may be
				supported.</span><span class="T2"><br /></span><span class="T8">               
				--&gt;</span><span class="T2"><br />                </span><span class="T3"
				>&lt;mime-type&gt;</span><span class="T2">xml</span><span class="T3"
				>&lt;/mime-type&gt;</span><span class="T2"><br />        </span><span class="T3"
				>&lt;/response-format&gt;</span><span class="T2"><br /></span><span class="T3"
				>&lt;/sparrow-report-request&gt;</span><span class="T2"><br /></span></p>

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