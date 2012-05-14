<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01//EN" "http://www.w3.org/TR/html4/strict.dtd">
<%@ page contentType="text/html; charset=utf-8"%>
<%@ page import="gov.usgswim.sparrow.ActivityLogFilter, gov.usgswim.sparrow.ActivityLogFilter.*" %>
<%@ page import="java.util.*" %>
<html>
<head>
	<meta http-equiv="Content-Type" content="text/html; charset=utf-8"/>
	<title>Usage Statistics - SPARROW Model Decision Support</title>
	<link rel="icon" href="favicon.ico" >

	<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />
	<link type="text/css" rel="stylesheet" href="http://infotrek.er.usgs.gov/docs/usgs_vis_std/style/usgs_style_main.css" />
	<link type="text/css" rel="stylesheet" href="css/custom.css" />
	
	<style type="text/css">
		table { border-collapse:collapse; }
		table, th, td { border: 1px solid black; }
		/* table th, table td { padding: .1em -2em; } */
		thead th {
			-moz-transform: rotate(-90deg); -webkit-transform: rotate(-90deg); transform: rotate(-90deg);
			height: 20em; width: 4em;
		}
		tbody td { width: 4em; }
		
		.usage-block { width: 100%; height: 1.5em; position: relative; }
		
		.usage-block .numerical-results { position: absolute; width: 100%; height: 100%; z-index: 2; }
		.usage-block .numerical-results:hover { display: block; z-index: 3 !important; /* promote z so child stats is on top */ }
		.usage-block .numerical-results:hover .stat-details { display: block; z-index: 4 !important; }
		.usage-block .total-count { text-align: center; }

		.usage-block .numerical-results:hover stat-details { display: block !important; }
		.usage-block .stat-details {
			display: none; position: absolute; top: 90%; left: 90%; z-index: 3;
			width: 18em; padding: .6em;
			background-color: #fff5ab; border: 1px black; box-shadow: 10px 10px 5px rgba(0,0,0,0.5); border-radius: 5px;
		}
		
		.usage-block .load-flag { position: absolute; width: 50%; height: 100%; z-index: 1; }
		.usage-block .speed-flag { position: absolute; left: 50%; width: 50%; height: 100%; z-index: 1; }
		
		.usage-block .good { background-color: #8ade56; }
		.usage-block .ok { background-color: #ded756; }
		.usage-block .warn { background-color: #de7d56; }
	</style>

</head>
<body>
    <jsp:include page="header.jsp" flush="true" />
    <div style="padding: 1em">
    <%

    RequestSetIterator rsi = ActivityLogFilter.getRequestSetIterator();

    String[] paths = rsi.getPaths();
    %>
    
    <h1>SPARROW DSS Usage Statistics</h1>
    
    <p>
    Each cell displays the number of requests received for a url during the time increment.
    The colors of each cell are divided left and right.  The left half indicates if the
    service is seeing a heavy load, the right half indicates if the service is returning slowly.
    </p>
    <p>
    <em>Heavy load</em> is load in excess of 1 request per second for a url.
    <em>Slow response</em> is any response for the url that takes longer than six seconds.
    Heavy loads or slow responses are marked in red, half amounts are marked in yellow.
    </p>
    
    <h3>Total number of requests in the stat history <%= rsi.getTotalRequestCount() %> (there is a limited number that are kept)</h3>
    <table>
    	<thead><%-- Generate Table Header --%>
    		<tr>
    			<th>Time Ago:</th>
    <% for (int i=0; i< paths.length; i++) { %>
    			<th><%= paths[i] %></th>
    <% } %>
    		</tr>
    	</thead>
    	<tbody>
    		
    <%
    	int secondTimeIncrement = 15;
    	int millisTimeIncrement = 1000 * secondTimeIncrement;
    	int maxMillisTimeAgo = millisTimeIncrement * 1000;	//Don't list requests older than 4.16 hours
    
    	RequestSet[] requestSets = rsi.getNextResultSetArray(millisTimeIncrement);
	    while (requestSets != null && rsi.getTimeAgoMillis() < maxMillisTimeAgo) {
	    	int heavyLoadThreshold = secondTimeIncrement;	//one per second
	    	int slowResponseThreshold = 6;	//6 seconds is slow
    %>
    		<tr>
    			<th><%= rsi.getTimeAgo() %></th>
    <%
    		for (int i=0; i< requestSets.length; i++) {
    			RequestSet rs = requestSets[i];
    			int cnt = rs.getCount();
    			double maxTime = rs.getMaxDuration();
    			String loadClass = "none";
    			String speedClass = "none";
    			
    			if (cnt > 0 && cnt < heavyLoadThreshold / 2) {
    				loadClass = "good";
    			} else if (cnt >= heavyLoadThreshold / 2 && cnt < heavyLoadThreshold) {
    				loadClass = "ok";
    			} else if (cnt >= heavyLoadThreshold) {
    				loadClass = "warn";
    			}
    			
    			
    			if (maxTime > 0 && maxTime < slowResponseThreshold / 2) {
    				speedClass = "good";
    			} else if (maxTime >= slowResponseThreshold / 2 && maxTime < slowResponseThreshold) {
    				speedClass = "ok";
    			} else if (maxTime >= slowResponseThreshold) {
    				speedClass = "warn";
    			}
    %>
    			<td>
    				<div class="usage-block">
    					<div class="numerical-results">
    						<div class="total-count"><%= cnt %></div>
    						<div class="stat-details">Total Requests: <%= cnt %><br />average time (sec): <%= rs.getAvgDuration() %><br />max time (sec): <%=maxTime %><br />min time (sec): <%= rs.getMinDuration() %></div>
    					</div>
    					<div class="<%= loadClass %> load-flag"></div>
    					<div class="<%= speedClass %> speed-flag"></div>
    				</div>  			
    			</td>
    <% 		} %>
    		
    		</tr>
    <% 
    		requestSets = rsi.getNextResultSetArray(millisTimeIncrement);
    	}
    %>
    	</tbody>
    </table>
    


    </div>
    <jsp:include page="footer.jsp" flush="true" />
</body>
</html>