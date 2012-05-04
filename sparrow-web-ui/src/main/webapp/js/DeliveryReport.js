
function displayDeliveryTerminalReport(forContextId) {
	
		var newWindow = window.open('deliveryreport.jsp?context-id=' + context_id + '&mime-type=xhtml_table&include-id-script=true&report-type=terminal', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function displayDeliveryStateReport(forContextId) {
	
		var newWindow = window.open('deliveryreport.jsp?context-id=' + context_id + '&mime-type=xhtml_table&include-id-script=false&report-type=state', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function idDeliveryReach(reachId) {
	IDENTIFY.identifyReach(null, null, reachId, 4, true);
}
