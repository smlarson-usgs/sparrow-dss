function loadModels() {
    var xmlreq = ''
        + '<?xml version="1.0" encoding="ISO-8859-1" ?>'
        + '<sparrow-meta-request '
        + '  xmlns="http://www.usgs.gov/sparrow/meta_request/v0_1" '
        + '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
        + '  <model public="false" archived="false" approved="false" />'
        + '</sparrow-meta-request>'
        ;

    // Send a request to the model service for a list of public models
    Ext.Ajax.request({
        url: 'getModels',
        params: 'xmlreq=' + xmlreq + '&mimetype=json',
        success: function(result, request) {
    		renderModelList(result.responseText);
        },
        failure: function(response, options) {
            Ext.MessageBox.alert('Failed', 'Unable to connect to SPARROW.');
        }
    });
}

/*
 * Callback function for the model request.  This function renders the model
 * select box adding an option for each model returned.
 */
function renderModelList(modelResponse) {

    // Pull back the response
    modelResponse = Ext.util.JSON.decode(modelResponse);

    var mcol1 = document.getElementById('models-col1');
    mcol1.innerHTML = '';
    var mcol2 = document.getElementById('models-col2');
    mcol2.innerHTML = '';
    for (var i = 0; i < modelResponse.models.model.length; i++) {
    	var model = modelResponse.models.model[i];
    	var bbox = model.bounds["@west"] + ',' + model.bounds["@south"] + ',' + model.bounds["@east"] + ',' + model.bounds["@north"];
    	var html =
	    '<div class="model-item clearfix" style="display:block">' +
			'<img src="images/model_screens/ss_model_' + model['@id'] + '.png" alt="model screen shot" class="model-screenshot"/>' +
			'<div style="padding-left: 180px">' + 
			'<div class="model-title"><a href="map.jsp?model=' + model['@id'] + '&bbox=' + bbox + '">' + model['name'] + '</a></div>' +
			'<div class="model-dateadded">Added: ' + model['dateAdded'] + '</div>' +
			'<div class="model-description">' + model['description'] + '</div>';

    	if (model.sessions && model.sessions.session) {
    		html += '<br/><div><u>Predefined Sessions:</u></div><ul>';
    		var sessions =  model.sessions.session;
    		for (var j = 0; j < sessions.length; j++){
    			var session = sessions[j];
    			html += '<li><a href="map.jsp?model=' + model['@id'] + '&session=' + session['@key'] + '">' + session['@key'] + '</a></li>';
    		}
    		html += '</ul>';

    	}


    	html += '</div></div>';
    	if (i < Math.floor(modelResponse.models.model.length / 2)) {
    		mcol1.innerHTML += html;
    	} else {
    		mcol2.innerHTML += html;
    	}
    }
}

