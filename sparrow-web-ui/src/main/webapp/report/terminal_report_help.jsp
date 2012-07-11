<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Total Delivered Load Summary Report Help</title>
	<script src="js/report.js"></script>
	
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="../css/custom.css" />
	<link type="text/css" rel="stylesheet" href="style/report.css" />

</head>
<body>
	<jsp:include page="../header.jsp" flush="true" />
    
	<h1>Total Delivered Load Summary Report Help</h1>
	<div class="explination">
		<div class="section">
			<p>
				The Total Delivered Load Summary Report can be used to summarize the load 
				delivered to all of the downstream reaches selected in the Downstream tracking tab of the application. 
				These downstream reaches may be entering an estuary or a lake or crossing some boundary such as a state line. 
				The report lists the Total Delivered Load for each individual downstream reach and the cumulative 
				Total Delivered Load for all of the downstream reaches. 
				In all cases, the Total Delivered Load is broken down by source.
			</p>

			<p>
				Total Delivered Load is the total load (including load from upstream reaches) 
				that arrives at the downstream end of the selected downstream reaches, in units of the model per year.
				Two examples of typical usage of this report are shown below.
			</p>

			<div class="figure cluster">
				<h3>Fig. 1:  Example usage of load delivered to a lake</h3>
				<div class="wrap"><img src="style/fig_t1.png" /></div>
				<div class="caption">
					<p>
						Reaches A, B and C are selected as downstream reaches to represent loading to the lake. 
						Load that arrives at the end of these reaches (as shown by the orange circles)
						would be reported individually in a row in the report and would be aggregated in another row in the report, like the report in Figure 3.
					</p>
				</div>
			</div>

			<div class="figure cluster">
				<h3>Fig. 2:  Example usage of load crossing a political boundary</h3>
				<div class="wrap"><img src="style/fig_t2.png" /></div>
				<div class="caption">
					<p>
					Reaches A, B and C are selected as downstream reaches to represent the load leaving a political region, such as a state. 
					Load that arrives at the end of these reaches (as shown by the orange circles)
					would be reported individually in a row in the report and would be summed in another row in the report, like the report in Figure 3.
					</p>
					<p>
						Because rivers are broken into reaches based on hydrology, not political boundaries, 
						it is likely that the downstream end of the selected downstream reaches will not fall on the political boundary.
						Thus, some load from outside the region may be included in the reported values. 
						If an upstream area outside of the political boundary is contributing to the load at the downstream reaches, 
						consider using the Delivered Load by Upstream region instead.
					</p>
				</div>
			</div>

			<div class="figure cluster" style="width: 472px">
				<h3>Fig. 3:  Example Report for figures 1 & 2</h3>
				<div class="wrap"><img src="style/fig_t3.png" /></div>
				<div class="caption">
					<p>
						Figures 1 & 2 might result in a delivered load summary like this one. 
						The total load arriving the downstream end of each of the selected downstream reaches (A, B & C) 
						is shown in a separate row in the table and summed in the last row. 
						These loads are also broken down by source.
					</p>
				</div>
			</div>
		</div><!-- /section -->
		<div class="section">
			<p>
				It is important to understand that if any of the selected reaches are upstream of another selected reach
				(i.e. if two or more reaches are nested), some of the load will be counted more than once and the total row will be inaccurate.
				<strong>It is almost always incorrect to select downstream reaches such that one reach is upstream of another.</strong>
			</p>

			<div class="figure" style="width: 472px">
				<h3>Fig. 4:  Example of nested upstream reaches</h3>
				<div class="wrap"><img src="style/fig_t4.png" /></div>
				<div class="caption">
					<p>
						Reaches A & B have been selected as downstream reaches. 
						The total load arriving at the downstream end of reach B will be included as a row in the report, 
						as will the total load arriving at the downstream end of reach A, 
						which will include some load from reach B. 
						In this example, the total row in the report will not be the total load delivered to the lake, 
						since some of the load from reach B will have been included twice. By selecting only reach A as a downstream reach, 
						all of the upstream load that arrives at the downstream end of reach A, 
						including load that originates from reaches B, C, and all reaches upstream, 
						will be included in the load reported for reach A.
					</p>
				</div>
			</div>
		</div><!-- /section -->
		
	</div>
    
	<jsp:include page="../footer.jsp" flush="true" />
</body>
</html>