
function displayDeliveryData(forContextId) {
	
		var newWindow = window.open('deliveryreport.jsp?context-id=' + context_id + '&mime-type=xhtml_table&include-id-script=false', '_blank', 
		'resizable=1,location=0,status=1,scrollbars=1,width=500,height=640');
		newWindow.focus();


//	Ext.Ajax.request({
//		url: 'getDeliveryReport?context-id=' + context_id + '&mime-type=html',
//		method: 'GET',
//		success: function(resp, opts) {
//            var helpFrame = document.getElementById('delivery-report-content');
//            helpFrame.innerHTML = '';
//            Ext.DomHelper.insertFirst(helpFrame, resp.responseText);
//            Ext.getCmp('delivery-report-frame').show();	
//
//		},
//        failure: function(resp, opts) {
//
//            var helpFrame = document.getElementById('delivery-content-panel');
//            helpFrame.innerHTML = '';
//            Ext.DomHelper.insertFirst(helpFrame, 'Information cannot be retrieved.');
//            Ext.getCmp('delivery-report-frame').show();
//        }
//	});
}

function idDeliveryReach(reachId) {
	IDENTIFY.identifyReach(null, null, reachId, 4, true);
}
