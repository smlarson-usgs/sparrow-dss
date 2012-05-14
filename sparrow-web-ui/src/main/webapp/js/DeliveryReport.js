
function displayDeliveryTerminalReport(forContextId) {
	
		var newWindow = window.open('report/deliveryreport.jsp?context-id=' + context_id + '&mime-type=xhtml_table&report-type=terminal', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function displayDeliveryStateReport(forContextId) {
	
		var newWindow = window.open('report/deliveryreport.jsp?context-id=' + context_id + '&mime-type=xhtml_table&report-type=region_agg&region-type=state', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function idDeliveryReach(reachId) {
	IDENTIFY.identifyReach(null, null, reachId, 4, true);
}
