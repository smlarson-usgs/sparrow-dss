
function displayDeliverySummaryReport(forContextId) {
	
		var newWindow = window.open('report/deliveryreport.jsp?context-id=' + context_id, '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=900,height=700');
		newWindow.focus();
}

function idDeliveryReach(reachId) {
	IDENTIFY.identifyReach(null, null, reachId, 4, true);
}
