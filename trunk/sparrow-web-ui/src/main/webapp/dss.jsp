<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<html>
<head>
<jsp:include page="template_meta_tags.jsp" flush="true" />
<jsp:include page="template_ie7_sizer_fix.jsp" flush="true" />
	


		<title>About the SPARROW Decision Support System</title>
		
		<link rel="stylesheet" href="css/one_column.css?rev=1" type="text/css">
		<link rel="stylesheet" href="css/static_custom.css?rev=1" type="text/css">
		<link rel="stylesheet" href="css/dss_page.css?rev=1" type="text/css">
		
<jsp:include page="template_page_tracking.jsp" flush="true" />
</head>
<body>
<jsp:include page="template_body_header.jsp" flush="true" />

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
					<li><a title="SPARROW DSS FAQs" href="faq.jsp">SPARROW DSS FAQs</a></li>
				</ul>
			</div>
		</div>
	</div>
</div>

	<jsp:include page="template_body_footer.jsp" flush="true" />
</body>
</html>