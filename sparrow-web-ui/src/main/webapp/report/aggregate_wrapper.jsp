<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="gov.usgswim.sparrow.SparrowUtil, gov.usgswim.sparrow.SparrowUtil.UrlFeatures" %>
<%@ page import="java.net.*, java.io.*" %>
    <h1>SPARROW DSS Total Delivered Load, Aggregated by Upstream Region</h1>
		<div class="explination">
			<p>
				This report aggregates the load delivered to the downstream reaches by the upstream contributing region.
				For instance, if aggregating by state, the report would list the load that originates in each state 
				and arrives at selected downstream reaches.
				This is calculated by summing the incremental delivered load of each catchment within upstream region, 
				allocating partial contributions for reaches that have catchment area in multiple regions.
				<a href="javascript:openAggHelp();" title="Furthure details in a new window.">more details...</a>
			</p>
		</div>
		<div class="controls">
			<div class="export"></div>
			<div class="agg-region">
				<form>
					<p class="input"><input type="radio" name="region-type" value="state" />State</p>
				</form>
			</div>
			<div class="downstream-reaches"></div>
		</div>
 <%
 
 	UrlFeatures pageRequestUrl = SparrowUtil.getRequestUrlFeatures(request);
	String tableName = "getDeliveryAggReport";
	String tableParams = "context-id=" + request.getParameter("context-id") +
			"&region-type=" + request.getParameter("region-type") + 	
			"&include-zero-rows=false" + 					
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