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
    <h2>Total Delivered Load Summary</h2>
		<h3>Total load delivered to each active downstream reach, from their respective upstream watershed area.</h3>
		<div class="explanation">
			<p>
				This report lists each downstream reach, as selected in the Downstream Tracking tab of the application.
				For each reach, the Total Delivered Load from that reach's watershed 
				(i.e., the total load from all upstream reaches) is broken down by source.
				Total Delivered Load is the load that arrives at the downstream end of a selected downstream reach,
				in units of mass (kg) per year.
				<a href="javascript:openTerminalHelp();" title="Furthure details in a new window.">More details...</a>
			</p>
			<p class="downstream-reaches-out-of-sync">
				<img src="../images/small_alert_icon.png" alt="Warning Icon" />
				Careful!  These downstream reaches are no longer the
				<em>Active Downstream Reaches</em> in the Sparrow DSS.  <a href="">More info...</a>
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