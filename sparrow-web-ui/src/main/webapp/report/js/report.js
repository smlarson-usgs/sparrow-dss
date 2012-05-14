function openTerminalHelp() {
	
		var newWindow = window.open('terminal_report_help.jsp', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function openAggHelp() {
	
		var newWindow = window.open('aggregate_report_help.jsp', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();
}

function idDeliveryReach(reachId) {
	
	//Calls this function on the opener window (the page that opened this page - ie the main map application)
	window.opener.idDeliveryReach(reachId);
}

