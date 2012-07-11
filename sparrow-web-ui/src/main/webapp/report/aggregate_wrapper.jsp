<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="gov.usgswim.sparrow.SparrowUtil" %>
<%@page import="gov.usgswim.sparrow.SparrowUtil.UrlFeatures" %>
<%@page import="java.net.*, java.io.*" %>
<%
	String regionType = "state";	//default
	if (request.getParameter("region-type") != null) {
		regionType = request.getParameter("region-type");
	}
	
	String contextId = request.getParameter("context-id");
	
	UrlFeatures pageRequestUrl = SparrowUtil.getRequestUrlFeatures(request);
	String tableName = "getDeliveryAggReport";
	String downloadReqParams = "context-id=" + request.getParameter("context-id") +
			"&region-type=" + regionType + 	
			"&include-zero-rows=false" + 					
			"&mime-type=csv";
	
	String downloadReqUrl = pageRequestUrl.getBaseUrlWithSlash() + tableName + "?" + downloadReqParams;
	
%>
 <div>
    <h2>Delivered Load from Selected Upstream Region</h2>
		<div class="explanation">
			<p>
				This report aggregates the load delivered to the downstream reaches by the upstream contributing region.
				For instance, if aggregating by state, the report would list the load that originates in each state 
				and arrives at selected downstream reaches.
				This is calculated by summing the incremental delivered load of each catchment within upstream region, 
				allocating partial contributions for reaches that have catchment area in multiple regions.
				<a href="javascript:openAggHelp();" title="Furthure details in a new window.">more details...</a>
			</p>
		</div>
		<div class="to-and-from-area columns-2">
			<div class="from-aggregate-area column">
				<div class="content">
				<h4>Aggregate upstream regions by</h4>
				<form class="controls columns-2">
					<div class="column">
					<p class="input"><input type="radio" name="region-type"<%= ("state".equals(regionType))?"checked=\"checked\"":"" %> value="state" />State</p>
					<!-- <p class="input"><input type="radio" name="region-type"<%= ("eda".equals(regionType))?"checked=\"checked\"":"" %> value="eda" />EDA</p> -->
					<p class="input"><input type="radio" name="region-type"<%= ("huc2".equals(regionType))?"checked=\"checked\"":"" %> value="huc2" />HUC 2</p>
					<p class="input"><input type="radio" name="region-type"<%= ("huc4".equals(regionType))?"checked=\"checked\"":"" %> value="huc4" />HUC 4</p>
					<p class="input"><input type="radio" name="region-type"<%= ("huc6".equals(regionType))?"checked=\"checked\"":"" %> value="huc6" />HUC 6</p>
					<p class="input"><input type="radio" name="region-type"<%= ("huc8".equals(regionType))?"checked=\"checked\"":"" %> value="huc8" />HUC 8</p>
					<input type="hidden" name="context-id" value="<%= contextId %>" />
					</div>
					<div class="column download-area">
						<a class="button-link download-report" href="<%= downloadReqUrl %>" title="Download the currently displayed report">Download as CSV</a>
					</div>
				</form>
				</div>
			</div>
			<div class="to-downstream-reaches-area column">
				<div class="content">
					<h4>Downstream Reaches Load is Delivered To</h4>
					<p>Load is delivered to the <em>Selected Downstream Reaches</em>,
						selected on the Downstream Tracking tab of the application.
						Currently these reaches are (click on a reach to identify it on the map):
					</p>
					<p class="downstream-reaches-out-of-sync">
						<img src="../images/small_alert_icon.png" alt="Warning Icon" />
						Careful!  These downstream reaches are no longer the
						<em>Selected Downstream Reaches</em> in the Sparrow DSS.  <a href="">More info...</a>
					</p>
					<p class="downstream-reaches-list"></p>
				</div>
			</div>
			<div class="export-area"></div>
		</div>
		<div class="report-area">
			<div class="report-load-status">
				<h3 class="message">
					<img src="../images/wait.gif" />
					<span class="message-text">Report is loading...</span>
				</h3>
			</div>
			<div class="report-table-area">
			</div>
		</div>
 </div>