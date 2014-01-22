<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<jsp:include page="../template_meta_tags.jsp" flush="true" />

	<title>Delivered Load from Selected Upstream Regions Report Help</title>
	
	<script src="js/report.js"></script>
	<link type="text/css" rel="stylesheet" href="../css/one_column.css?rev=2">
	<link type="text/css" rel="stylesheet" href="../css/static_custom.css?rev=2">
	<link type="text/css" rel="stylesheet" href="style/report.css?rev=2" />
	<jsp:include page="../template_ie7_sizer_fix.jsp" flush="true" />
	
	<jsp:include page="../template_page_tracking.jsp" flush="true" />
</head>
<body>
	<jsp:include page="../header-unbalanced.jsp" flush="true" />
	
		<div id="page-content" class="area-1 area"> 
			<div class="area-content">
    
				<h1>Delivered Load from Selected Upstream Regions Report Help</h1>
			<div class="explanation">
				<div class="section">
					<p>
		The Aggregated Delivered Load Report can be used to analyze the upstream regions that contribute to the Total Delivered Load in each selected downstream reach.
		For instance, if aggregating by state, the report would list the load that originates in each state and is delivered to the downstream end of each of the selected downstream reaches.
					</p>

					<p>
		To understand the values shown in this report,
		it is helpful to understand how they are calculated.
		The calculation roughly follows these steps:</p>
			<ol>
						<li>All reaches upstream of a selected downstream reach are identified.</li>
						<li>For each upstream reach, incremental delivered load is calculated by multiplying
							incremental load by the delivery fraction, which is the fraction of incremental load delivered to the selected downstream reach(es).
						</li>
						<li>Reaches are grouped by the upstream region they are in.  For instance, if the selected region type is state, all the reaches in the same state will be grouped together.</li>
						<li>The incremental delivered loads for all the reaches in the same group (i.e. HUC or State) are summed.</li>

						<li>If a reach is partially in two or more regions (this happens when states are chosen as the upstream region type),
							the reach's incremental delivered load is area corrected for each region:
							The incremental delivered load is multiplied by the fraction of the reach's catchment area that falls within the region being calculated.
						</li>
						<li>Similarly, the watershed area of each region is the sum of
							the catchment areas of all the reaches in each region that drain
							to the target reach(es) and is area corrected for reaches in multiple regions.
							Thus, the watershed area displayed in the table will usually be less than
							the area of the region itself.
						</li>
			</ol>

					<div class="figure cluster" style="width:450px;">
						<h3>Fig. 1: Representation of an Delivered Load from Selected Upstream Regions report, aggregated by HUC</h3>
						<div class="wrap"><img src="style/fig_a1.png" /></div>
						<div class="caption">
							<p>
								This example shows how load would be aggregated by Hydrological Unit Codes, or HUCs.
								Reach A is selected as the downstream reach (as shown by the orange circle). 
								To summarize the load delivered from HUC 010103 to the downstream end of reach A, 
								the incremental delivered load for reaches G, J, K and L are summed. 
								Reaches B, C, D, E, F, H, and I are not included.
							</p>
							<p>
								Since HUCs are built up from individual reach drainage areas (catchments), 
								reaches are always completely within the HUC that contains them. 
								This differs from state and other political boundaries, which usually cut across reach catchment areas.
							</p>
						</div>
					</div>
					<div class="figure cluster" style="width:450px;">
						<h3>Fig. 2: Representation of an Delivered Load from Selected Upstream Regions report, aggregated by State</h3>
						<div class="wrap"><img src="style/fig_a2.png" /></div>
						<div class="caption">
							<p>
								This example shows how load would be aggregated by State. 
								Reach A is selected as the downstream reach (as shown by the orange circle). 
								To summarize the load delivered from the given State to the downstream end of reach A, 
								the incremental delivered load for reaches B, C, D, E, F, G, H, J, K and L 
								(and others that may be upstream but not shown) are summed. Reach I is not included.
							</p>
							<p>
								Since some reaches are partially in multiple states, 
								the fraction of the catchment area within the given State is used to attribute the incremental delivered load to the State. 
								In this example, the entire catchment of reach G is within the State, 
								so the entire incremental delivered load from reach G would be included. 
								Approximately 60% of the catchment of reach B is within the State, 
								so the incremental delivered load of reach B would be multiplied by 0.6.
							</p>
						</div>
					</div>

				</div>
			</div>
    
		</div>
	</div>
	<jsp:include page="../footer-unbalanced.jsp" flush="true" />
</body>
</html>