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
    
	<h1>SPARROW DSS Total Delivered Load Summary</h1>
	<div class="explination">
		<p>
			The Total Delivered Load Summary Report can be used to view the individual
			and combined contribution of all the selected downstream reaches.
			The <strong>Total for all Reaches</strong> summary row at the bottom of the
			report is particularly useful for seeing the cumulative effect of all
			reaches emptying into a lake or crossing a political boundary.
			The report lists each downstream reach,
			as selected in the Downstream Tracking tab of the application.
			For each reach, the Total Load from that reach's watershed
			(i.e., the total load from all upstream reaches) is broken down by source.
		</p>
		
		<p>
			Total Delivered Load is the total load (including load from upstream reaches)
			that leaves the downstream end of a reach that arrives at the downstream end
			of the active downstream reaches, in units of mass (kg) per year.
			Since the report lists only the downstream reaches the load arrives at,
			the total delivered load is equal to the total load for these reaches.
		</p>
		
		<p>
			Two examples of typical usage of this report are shown below.
			For both examples, the report might look like the Sample Total Delivered Load Summary.
		</p>

		<p>Fig 1.  Reaches entering a lake.  3 reaches, ABC.</p>
		<p>Fig 2.  Reaches crossing a boundary.  3 reaches, ABC.</p>
		<p>Fig 3.  Reach Totals (a simplified report showing reaches AB & C)</p>

		<p>
			It is important to understand that if any of the selected reaches are
			upstream of another selected reach, the total row will be inaccurate.
			For instance, if reach A and reach B are selected as downstream reaches 
			and A is upstream of B, the total load arriving at the downstream end of
			both reaches will be included in the total,
			which will result in some of that load being counted twice.
			<strong>It is almost always incorrect to select downstream reaches such
			that one reach is upstream of the other.</strong>
		</p>

		<p>
			Fig 3.  Reaches entering a Lake, plus an upstream reach.
			A upstream of B. Don't do this.  W/ total example.
		</p>

		<p>
		Notes:
		Can we change 'Active Downstream Reaches' to 'Selected...'
		</p>
		
	</div>
    
	<jsp:include page="../footer.jsp" flush="true" />
</body>
</html>