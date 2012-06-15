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
		<div class="section">
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

			<div class="figure cluster">
				<h3>Fig. 1:  Example usage of load delivered to a lake</h3>
				<div class="wrap"><img src="style/fig1.png" /></div>
				<div class="caption">
					<p>
						Reaches A, B and C are selected as downstream reaches.
						Load that arrives at the <i>end</i> of these reaches
						(as shown by the orange circles)
						would be reported individually in a row in the report, like the
						report in Figure 3.
					</p>
					<p>
						Upstream diversions, like reach D, are handled correctly: Load from
						reach D would not be included in the load reported at reach C.
					</p>
				</div>
			</div>

			<div class="figure cluster">
				<h3>Fig. 2:  Example usage of load crossing a political boundary</h3>
				<div class="wrap"><img src="style/fig2.png" /></div>
				<div class="caption">
					<p>
						Reaches A, B and C are selected as downstream reaches to analyze the
						load leaving a political region.
						Rivers are broken into reaches based on hydrology,
						not political boundaries, so it is likely reach end points will not be
						on borders.  Thus, some load from outside the region will be included
						in the reported values.
						It may not be appropriate to include a reach such as C, since much of
						its load originates outside the region.  Instead, consider using the
						Delivered Load by Upstream region for this type of analysis.
					</p>
				</div>
			</div>

			<div class="figure cluster" style="width: 472px">
				<h3>Fig. 3:  Example Report for figures 1 & 2</h3>
				<div class="wrap"><img src="style/fig3.png" /></div>
				<div class="caption">
					<p>
						Figures 1 & 2 might result in a delivered load summary like this one.
						The total load arriving at the downstream end of each selected reach,
						A, B & C, is shown on a separate row in the table and totaled in the
						last row.  The load is also broken down by the amount attributable to
						each source in the model.
					</p>
				</div>
			</div>
		</div><!-- /section -->
		<div class="section">
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

			<div class="figure" style="width: 472px">
				<h3>Fig. 4:  Example of nested upstream reaches</h3>
				<div class="wrap"><img src="style/fig4.png" /></div>
				<div class="caption">
					<p>
						Reach A & B have been selected as downstream reaches.
						The result is that load arriving at the downstream end of reach B
						will be included twice in the total row of the report:
						Once at reach B and again as it arrives at the downstream end of reach A.
						By selecting <i>only</i> reach A as a downstream reach, all the upstream
						load that arrives at the downstream end of A, including load that
						originates from reaches B, C and all those reaches upstream of them,
						will be included in the load reported for reach A.
					</p>
				</div>
			</div>
		</div><!-- /section -->

		<p>
		Notes:
		Can we change 'Active Downstream Reaches' to 'Selected...'
		</p>
		
	</div>
    
	<jsp:include page="../footer.jsp" flush="true" />
</body>
</html>