
function getGeneralHelp(key) {
	//nasty global variable
	getHelpFromService(model_id, key);
}

function getHelpFromService(modelId, key) {

	Ext.Ajax.request({
		url: 'helpService',
		method: 'POST',
		params: {
			model: modelId,
			item: key
		},
		success: function(resp, opts) {
            var helpFrame = document.getElementById('help-content');
            helpFrame.innerHTML = '';
            s = resp.responseXML.getElementsByTagName('item')[0].firstChild.nodeValue;
            Ext.DomHelper.insertFirst(helpFrame, s);
            Ext.getCmp('help-frame').show();
		},
        failure: function(resp, opts) {
            var helpTitle = document.getElementById('help-title');
            helpTitle.innerHTML = '<div>Error</div>';

            var helpFrame = document.getElementById('help-content');
            helpFrame.innerHTML = '';
            Ext.DomHelper.insertFirst(helpFrame, 'Information cannot be retrieved.');
            Ext.getCmp('help-frame').show();
        }
	});
}