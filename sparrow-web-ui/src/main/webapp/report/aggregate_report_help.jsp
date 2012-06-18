<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Delivery Report - SPARROW Model Decision Support</title>
	<script src="js/report.js"></script>
	
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="../css/custom.css" />
	<link type="text/css" rel="stylesheet" href="style/report.css" />

</head>
<body>
	<jsp:include page="../header.jsp" flush="true" />
    
	<h1>SPARROW DSS Total Delivered Load, Aggregated by Upstream Region</h1>
	<div class="explination">
		<div class="section">
			<p>
			The Aggregated Total Delivered Load Report can be used to analyze where load,
			reaching a set of downstream reaches of interest, originates from.
			</p>

			<p>
			The Aggregated Total Delivered Load Report displays the amount of load
			originating from each upstream region and is delivered to any of the selected
			downstream reaches.
			For instance, if aggregating by state,
			the report would list the load that originates in each state and arrives at
			any of the selected downstream reaches.
			</p>

			<p>
			To understand the values shown in this report,
			it is helpful to understand how they are calculated.
			The calculation roughly follows these steps:</p>
			<ol>
				<li>All the reaches upstream of any of the selected downstream reaches are identified</li>
				<li>For each upstream reach, incremental delivered load is calculated,
					by multiplying each reach's incremental load by the delivery fraction for
					that reach to all downstream reaches.
				</li>
				<li>For each region (i.e. States or HUCs) upstream of the selected downstream reaches,
					the incremental load of all reaches in that region are summed.
				</li>
				<li>If a reach is partially in two or more regions
					(this happens when states are chosen as the upstream region),
					the reach's incremental delivered load is multiplied by the fraction of
					reach's catchment area that falls within the region before adding to the region total.
				</li>
			</ol>
			
			<div class="figure cluster" style="width:450px;">
				<h3>Fig. 1:  Representation of a state aggregated report</h3>
				<div class="wrap"><img src="style/fig_a1.png" /></div>
				<div class="caption">
					<p>
						Reach A is selected as the downstream reach.
						Only load that is delivered to the <i>downstream end</i> of reach A
						(as shown by the orange circle)
						will be included in the report.
					</p>
					<p>
						In this example, the load is grouped by the state it originated in.
						Since some reaches are partially in multiple states, the fraction of
						the drainage area (catchment area) within a given state is used to
						attribute the load to the state.
					</p>
					<p>
						For the highlighted state shown,
						the entire catchment area of reach G is within the
						state, so all of reach G's incremental delivered load to reach A would be
						included in that state's total delivered load.
					</p>
					<p>
						Many other reaches are on state borders, so portions of the
						catchment areas are within multiple states.
						For reach B it looks like approximately 75% of the catchment area
						is within the highlighted state.
						Thus, the incremental delivered load of reach B to reach A would be
						multiplied by .75 to add to the state's delivered load total.
					</p>
				</div>
			</div>

			<p>
				Fig 2:  Simple drawing of a HUC:  all catchements are completely within
				the HUC.
			</p>
		</div>
	</div>
    
	<jsp:include page="../footer.jsp" flush="true" />
</body>
</html>