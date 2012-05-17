<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@page import="gov.usgswim.sparrow.SparrowUtil, gov.usgswim.sparrow.SparrowUtil.UrlFeatures" %>
<%@ page import="java.net.*, java.io.*" %>
    <h2>SPARROW DSS Total Delivered Load, Aggregated by Upstream Region</h2>
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
				<h3>Aggregate upstream regions by:</h3>
				<form>
					<p class="input"><input type="radio" name="region-type" value="state" />State (currently the only option)</p>
				</form>
				</div>
			</div>
			<div class="to-downstream-reaches-area column">
				<div class="content">
					<h3>Downstream Reaches Load is Delivered To</h3>
					<p>Load is delivered to the <em>Active Downstream Reaches</em> selected 
						on the Downstream Tracking tab of the application.
						Currently these reaches are (click on a reach to identify it on the map):
					</p>
					<p class="downstream-reaches-out-of-sync">
						<img src="../images/small_alert_icon.png" alt="Warning Icon" />
						Careful!  These downstream reaches are no longer the
						<em>Active Downstream Reaches</em> in the Sparrow DSS.  <a href="">More info...</a>
					</p>
					<p class="downstream-reaches-list"></p>
				</div>
			</div>
			<div class="export-area"></div>
		</div>
 <%
 
 	UrlFeatures pageRequestUrl = SparrowUtil.getRequestUrlFeatures(request);
	String tableName = "getDeliveryAggReport";
	String tableParams = "context-id=" + request.getParameter("context-id") +
			"&region-type=state" + 	
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