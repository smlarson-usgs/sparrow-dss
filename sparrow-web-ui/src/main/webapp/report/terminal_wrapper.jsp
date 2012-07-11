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
	
%>
 <div>
		<div class="explanation">
			<div class="figure cluster">
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
				<a href="javascript:openTerminalHelp();" title="Click to open more detailed documentation in a new window.">detailed documentation</a>
				is available.
			</p>
			<p class="downstream-reaches-out-of-sync">
				<img src="../images/small_alert_icon.png" alt="Warning Icon" />
				Careful!  These downstream reaches are no longer the
				<em>Selected Downstream Reaches</em> in the Sparrow DSS.  <a href="">More info...</a>
			</p>
		</div>
		<div class="download-area">
			<a class="button-link download-report" href="<%= downloadReqUrl %>" title="Download the currently displayed report">Download as CSV</a>
		</div>
		
		<div id="terminal-report-area" class="report-area">
			<div class="report-load-status">
				<img src="../images/wait.gif" />
				<h3 class="message">Report is loading...</h3>
			</div>
			<div class="report-table-area">
 <!--
	UrlFeatures pageRequestUrl = SparrowUtil.getRequestUrlFeatures(request);
	String tableName = "getDeliveryTerminalReport";
	String tableParams = "context-id=" + request.getParameter("context-id") +	
			"&include-zero-rows=true" + 					
			"&mime-type=xhtml_table";
	
	String tableUrl = pageRequestUrl.getBaseUrlWithSlash() + tableName + "?" + tableParams;
 
 	response.getWriter().flush();
 
    URL url = new URL(tableUrl);
    BufferedReader in = new BufferedReader(
    new InputStreamReader(url.openStream()));
		//StringBuffer table = new StringBuffer();
    String inputLine;
    while ((inputLine = in.readLine()) != null)
    	out.write(inputLine);
    in.close();
 
 -->
			</div>
		</div>
 </div>