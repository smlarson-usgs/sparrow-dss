<!doctype html>
<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@page import="gov.usgswim.sparrow.SparrowUtil" %>
<%@page import="gov.usgswim.sparrow.SparrowUtil.UrlFeatures" %>
<html>
    <head>
		<jsp:include page="template_meta_tags.jsp" flush="true" />
	
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

		<jsp:include page="header-unbalanced.jsp" flush="true" />
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

					
	<jsp:include page="footer-unbalanced.jsp" flush="true" /> 
</body> 
</html>