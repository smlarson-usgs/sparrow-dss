<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="gov.usgswim.sparrow.AtomReaderUtil" %>
<html>
<head>
	<jsp:include page="template_meta_tags.jsp" flush="true" />
	<jsp:include page="template_ie7_sizer_fix.jsp" flush="true" />
	
	<title>SPARROW DSS - Frequently Asked Questions</title>

	<link rel="stylesheet" href="css/one_column.css?rev=1" type="text/css">
	<link rel="stylesheet" href="css/static_custom.css?rev=1" type="text/css">
	
	<jsp:include page="template_page_tracking.jsp" flush="true" />
	
</head>
<body>
	<jsp:include page="template_body_header.jsp" flush="true" />
		<div id="page-content" class="area-1 area"> 
			<div id="faq-container" class="area-content">

				<h1>Frequently Asked Questions for the SPARROW DSS Tool</h1>
				<%= AtomReaderUtil.getAtomFeedContentOnlyAsString("https://my.usgs.gov/confluence/createrssfeed.action?types=page&spaces=conf_all&title=SPARROW_DSS_DOCS_FAQ&labelString=sparrow_dss_docs_faq&excludedSpaceKeys=&sort=modified&maxResults=1&timeSpan=3650&showContent=true") %>
			</div>
		</div>
	<jsp:include page="template_body_footer.jsp" flush="true" />
</body>
</html>