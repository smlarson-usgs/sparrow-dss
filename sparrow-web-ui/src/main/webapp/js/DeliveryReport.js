
function displayDeliverySummaryReport() {

		var newWindow = window.open('report/deliveryreport.jsp?context-id=' + Sparrow.SESSION.getMappedOrValidContextId(), '_blank',
		'resizable=1,location=0,status=1,scrollbars=1,width=1000,height=750');
		newWindow.focus();
}

function idDeliveryReach(reachId) {
	IDENTIFY.identifyReach(null, null, reachId, 4, true);
}
