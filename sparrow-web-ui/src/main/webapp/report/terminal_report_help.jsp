<!DOCTYPE html>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ page import="java.net.*, java.io.*" %>
<html>
<head>
	<jsp:include page="../template_meta_tags.jsp" flush="true" />

	<title>Total Delivered Load Summary Report Help</title>

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

		<h1>Total Delivered Load Summary Report Help</h1>
		<div class="explanation">
			<div class="section">
				<h3>Typical Usage and Examples</h3>
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
				<h3>Avoid Nested Downstream Reaches</h3>
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
			<div class="section">
				<h3>Total Contributing Area - Fractioned Based on Stream Flow</h3>
				<p>
					This report (and other part of this application) uses <i>Total Contributing Area</i>. For a selected reach, the <i>Total Upstream Area</i> is the area of land upstream of the reach, irrespective of if it contributes to the reach or not. The is sometimes called the watershed area, drainage basin or catchment. <i>Total Contributing Area</i> is the land area that contributes load to the reach. Land area that is upstream of a reach but does not contribute, is not included (e.g. an upstream area connected by a dry stream bed).
					Both the <i>Total Contributing Area</i> and the <i>Total Upstream Area</i> accounts for splits (also called diversions) in the river network, where an upstream reach splits into two downstream reaches, as shown in Fig. 5.
				</p>
				<p>
					Total Contributing Watershed Area is calculated as follows:
				</p>
				<ol>
					<li>Select a reach to calculate the Fractioned Watershed Area for and start with the incremental area of that reach.</li>
					<li>
						For each reach immediately upstream, multiply the upstream reach's incremental area by the fraction of the load that enters the downstream reach (called just <i>fraction</i>)
						and add these areas to the incremental area of the selected reach.
						The fraction of load entering the downstream reach is 1 unless there is a diversion.
					</li>
					<li>
						Repeat this process, finding all upstream reaches and adding their fraction-multiplied area.
						The <i>fraction</i> propagates upstream, such that if there is a 50/50 split at the very first reach,
						all upstream reach areas are multiplied by .5.
						Additional upstream splits are multiplicative, thus, if there was a second 50/50 split further upstream,
						areas above that point would be multiplied by .25.
					</li>
				</ol>
				<p>
					Fractioning the watershed areas allow for more consistent yield numbers and are
					a truer representation of the total land area 'responsible' for the load
					arriving at a reach.  <strong>They will, however, not match other published watershed
						areas which do not fraction upstream areas by stream flow.</strong>
				</p>

				<div class="figure" style="width: 472px">
					<h3>Fig. 5:  Example of nested upstream reaches</h3>
					<div class="wrap"><img src="style/fig_t5.png" /></div>
					<div class="caption">
						<p>
							This sample calculation how Fractioned Watershed Area would be calculated
							for the simple case of reaches that have only one upstream reach.
							Note that adding the Fractioned Watershed Areas together does not
							result in double counting the area.
						</p>
					</div>
				</div>
			</div><!-- /section -->

		</div>

</div>
</div>
<jsp:include page="../footer-unbalanced.jsp" flush="true" />
</body>
</html>