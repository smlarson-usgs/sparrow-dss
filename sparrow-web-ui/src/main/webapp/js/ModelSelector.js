function loadModels() {
    var xmlreq = ''
        + '<?xml version="1.0" encoding="ISO-8859-1" ?>'
        + '<sparrow-meta-request '
        + '  xmlns="http://www.usgs.gov/sparrow/meta_request/v0_1" '
        + '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
        + '  <model public="$public$" archived="$archived$" approved="$approved$" />'
        + '</sparrow-meta-request>'
        ;

    
    var nonpublic = false;
    var nonapproved = false;
    var archived = false;
    
    if (document.getElementById('model-controls-area')) {
    	nonpublic = document.getElementById('model-controls-show-nonpublic').checked
    	nonapproved = document.getElementById('model-controls-show-nonapproved').checked
    	archived = document.getElementById('model-controls-show-archived').checked	
    }
    
    xmlreq = xmlreq.replace("$public$", !nonpublic);
    xmlreq = xmlreq.replace("$approved$", !nonapproved);	
    xmlreq = xmlreq.replace("$archived$", archived);	
    
    
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
	    '<div class="model-item clearfix">' +
	    	'<div class="model-screenshot-area">' +
	    		'<a href="map.jsp?model=' + model['@id'] + '&bbox=' + bbox + '">' +
	    			'<img src="images/model_screens/ss_model_' + model['@id'] + '.png" alt="model screen shot" class="model-screenshot"/>' +
	    		'</a>' +
	    	'</div>' +
			'<div class="model-description-area">' + 
				'<h3 class="model-title"><a href="map.jsp?model=' + model['@id'] + '&bbox=' + bbox + '">' + model['name'] + '</a></h3>' +
				'<div class="model-dateadded">Added: ' + model['dateAdded'] + '</div>' +
				'<div class="model-description">' + model['description'] + '</div>' +
			'</div>';

    	if (model.sessions && model.sessions.session) {
    		html += '<div class="model-predefined-session-area"><h3>Predefined Scenarios</h3><ul>';
    		var sessions =  model.sessions.session;
    		for (var j = 0; j < sessions.length; j++){
    			var session = sessions[j];
    			html += '<li>'
    			html += '<h4><a href="map.jsp?model=' + model['@id'] + '&session=' + session['@key'] + '">' + session['@name'] + '</a></h4>';
    			html += '<p>' + session['@description'] + '</p>';
    			html += '</li>';
    		}
    		html += '</ul></div>';

    	}


    	html += '</div>';
    	if (i < Math.floor(modelResponse.models.model.length / 2)) {
    		mcol1.innerHTML += html;
    	} else {
    		mcol2.innerHTML += html;
    	}
    }
}

