<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="gov.usgswim.sparrow.SparrowUtil, gov.usgswim.sparrow.SparrowUtil.UrlFeatures" %>
<%@ page import="java.net.*, java.io.*" %>
<%

	String contextId = request.getParameter("context-id");

	UrlFeatures pageRequestUrl = SparrowUtil.getRequestUrlFeatures(request);
	String tableName = "getDeliveryTerminalReport";
	String downloadReqParams = "context-id=" + request.getParameter("context-id") +
			"&include-zero-rows=true" +
			"&mime-type=csv";

	String downloadReqUrl = pageRequestUrl.getBaseUrlWithSlash() + tableName + "?" + downloadReqParams;

	String reportAsYield = "false";

%>
 <div>
		<div class="explanation">
			<div class="inset-figure cluster">
				<div class="wrap">
					<a href="javascript:openTerminalHelp();" title="Click to open more detailed documentation in a new window.">
						<img alt="Depiction of how values are calculated.  Click for more details." src="style/fig_t1.png" />
					</a>
				</div>
				<div class="caption"></div>
			</div>
			<h2>Total Delivered Load Summary</h2>
			<p>
				This report lists the total load delivered to each selected downstream reach, from their respective upstream watershed area.
				Total Delivered Loads are broken down by source.
				Downstream reaches are selected on the Downstream Tracking tab of the application.
				<a href="javascript:openTerminalHelp();" title="Click to open more detailed documentation in a new window.">Detailed documentation about how the load is calculated</a> is available.
				FAQs and additional documentation can be found <a href="javascript:openGenHelp();" title="Click to open documentation in a new window.">here</a>.
			</p>
		</div>
		<div class="columns-2">
			<div class="column">
				<div class="content">
					<h4>Report Load as:</h4>
					<form class="controls">
						<p class="input"><input type="radio" name="report-yield" <%= ("false".equals(reportAsYield))?"checked=\"checked\"":"" %> value="false" />Total Load</p>
						<p class="input"><input type="radio" name="report-yield" <%= ("true".equals(reportAsYield))?"checked=\"checked\"":"" %> value="true" />Yield</p>
					</form>
					<div class="download-area">
						<a class="button-link download-report" href="" title="Download the currently displayed report">Download as CSV</a>
					</div>
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
						<em>Selected Downstream Reaches</em> in the Sparrow DSS,
						so they do not correspond to what is shown on the map.
						To ensure the report and the map are based on the same
						set of downstream reaches, close this window, update the
						map and reopen the report from the Sparrow DSS application.
					</p>
					<p class="downstream-reaches-list"></p>
				</div>
			</div>
		</div>
		<div id="terminal-report-area" class="report-area">
			<div class="report-load-status">
				<img src="../images/wait.gif" />
				<h3 class="message">Report is loading...</h3>
			</div>
			<div class="report-table-area">
			</div>
		</div>
 </div>