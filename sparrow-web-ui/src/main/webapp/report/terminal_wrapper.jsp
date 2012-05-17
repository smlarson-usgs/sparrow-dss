<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="gov.usgswim.sparrow.SparrowUtil, gov.usgswim.sparrow.SparrowUtil.UrlFeatures" %>
<%@ page import="java.net.*, java.io.*" %>
    <h2>SPARROW DSS Total Delivered Load Summary</h2>
		<h3>Total Delivered Load for each watershed of the selected downstream reaches, detailed by source and reach.</h3>
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
 <%
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
 
 %>