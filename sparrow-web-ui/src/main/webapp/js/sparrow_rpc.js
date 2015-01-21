var appbase = location.host;
var contextroot = location.pathname.split('/')[1];
appbase += '/' + contextroot;

var modelSourcesCache;

var screenCastNameToVideoIdMap = {
	'Working with Sources' : '1tzeR4WkLv0',
	'Incremental Yield' : '5K1Smu7Q4Fc',
	'Selecting Downstream Outlets' : 'zrycRF7MeG8',
	'Changing Source Inputs' : 'UkC_76uq748',
	'Incremental Yield to an Outlet' : 'tHnxt2ORNQU',
	'Summarizing Delivered Load to Downstream Outlets' : 'HG9S4D0Jjfc'
};

var openScreencast = function(videoId){
	var newWindow = window.open('screencast.jsp?videoId=' + videoId, '_blank',
	   				'resizable=0,location=0,status=0,scrollbars=0,width=1280,height=780');
		newWindow.focus();
		return newWindow;
};

/**
 * Retrieve the model name and list of sources for the current model.
 */
function loadBasicModelInfo() {

	var xmlreq = ''
		+ '<?xml version="1.0" encoding="ISO-8859-1" ?>'
		+ '<sparrow-meta-request '
		+ '  xmlns="http://www.usgs.gov/sparrow/meta_request/v0_1" '
		+ '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
		+ '  <model id="' + model_id + '" >'
		+ '  </model>'
		+ '</sparrow-meta-request>'
		;

	// Send a request to the model service for a list of sources
	Ext.Ajax.request({
		method: 'POST',
		url: 'getSources',
		success: function(response,o) {
			var modelInfo = Ext.util.JSON.decode(response.responseText)["models"]["model"][0];

			Sparrow.SESSION.setModelName(modelInfo["name"]);
			Sparrow.SESSION.setThemeName(modelInfo["themeName"]);
			Sparrow.SESSION.setSourceList(modelInfo["sources"]["source"]);
			Sparrow.SESSION.setOriginalBoundNorth(modelInfo["bounds"]["@north"]);
			Sparrow.SESSION.setOriginalBoundEast(modelInfo["bounds"]["@east"]);
			Sparrow.SESSION.setOriginalBoundSouth(modelInfo["bounds"]["@south"]);
			Sparrow.SESSION.setOriginalBoundWest(modelInfo["bounds"]["@west"]);
			Sparrow.SESSION.setModelConstituent(modelInfo["constituent"]);
			Sparrow.SESSION.setModelUnits(modelInfo["units"]);
			try {
				Sparrow.SESSION.setDocUrl(modelInfo["url"]);
			} catch(e) {}

			Sparrow.SESSION.fireContextEvent('finished-loading-basic-model-info');
		},
		params: {
			xmlreq: xmlreq,
			mimetype: 'json'
		}
	});
}

function loadExternalResourceInfo() {
	Ext.Ajax.request({
		method: 'GET',
		url: 'GeoServerWMSEndPointService',
		success: function(r,o) {
			var ok = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'status');

			if (ok == 'OK') {
				var urlStr = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'entity');
				Sparrow.SESSION.setSpatialServiceEndpoint(urlStr);
				Sparrow.SESSION.fireContextEvent('finished-loading-external-resource-info');
			} else {
				Alert("Failed to load external connection information.");
			}
		}
	});
}

var IDENTIFY = new (function(){
	var self = this;
	var idRequest;
	var clicked_lat, clicked_lon;



	/**
	 * user had identify tool selected and clicked on map
	 */
	this.identifyPoint = function(event, map) {
		event = event || window.event;

		//calc lat/lon of mouse click event
		var coordsPx = JMap.util.getRelativeCoords(event, map.pane);
		var ll = map.getLatLonFromScreenPixel(coordsPx.x, coordsPx.y);

		//destroy any previous popup stuff
		if (map.identifyPopup) map.identifyPopup.kill();

		self.identifyReach(ll.lat,ll.lon, null, 0, true);
	};

	this.identifyPoint.cursor = "cursor-identify-point";


	/**
	 * Initiates a request to the reach identify service.  This function can make a
	 * request based on the user clicking a specific point on the map (lat/lon), or
	 * by the reach id (reachId).
	 *
	 * animOpts is defined in svn_overlay
	 */
	this.identifyReach = function(lat, lon, reachId, animOpts, showInfo) {
		if(! Sparrow.SESSION.isMapping()) {
			getContextIdAsync({
		    	callback: function() {
					IDENTIFY.identifyReachWithContextId(lat, lon, reachId, animOpts, showInfo);
				}
			});
		} else {
			IDENTIFY.identifyReachWithContextId(lat, lon, reachId, animOpts, showInfo);
		}
	};
	
	this.identifyReachWithContextId = function(lat, lon, reachId, animOpts, showInfo) {

		if (Ext.isNumber(animOpts)){
			// user gave a standard option set
			animOpts = SvgOverlay.standardAnimateOptions[animOpts];
		}

		if (animOpts.showPleaseWait) { IDENTIFY_REACH_SPINNER.show(); }

		var xmlreq = ''
			+ '<sparrow-id-request xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" '
			+ 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
			+ '<context-id>' + Sparrow.SESSION.getMappedOrValidContextId() + '</context-id>';

		// Identify by reach id or point clicked on the map
		if (!reachId) {
			xmlreq += '<point lat="' + lat + '" long="' + lon + '" />';
			clicked_lat = lat;
			clicked_lon = lon;
		} else {
			xmlreq += '<reach id="' + reachId + '" />';
		}

		// Request adjustments and attributes from the service
		xmlreq += ''
			+ '<content>'
			+ '<adjustments /><attributes />'
			+ '</content>'
			+ '<response-format><mime-type>json</mime-type></response-format>'
			+ '</sparrow-id-request>';

		this.abort();
		idRequest = Ext.Ajax.request({
			method: 'POST',
			url: 'getIdentify',
			success: function(r,o) {
				this.loadIdentifyReach(r,animOpts, showInfo);
				if (animOpts.showPleaseWait)
					IDENTIFY_REACH_SPINNER.hide();
			},
			failure: function(r,o) {
				Ext.Msg.alert('Warning', 'identify failed');
				if (animOpts.showPleaseWait)
					IDENTIFY_REACH_SPINNER.hide();
			},
			params: {
				xmlreq: xmlreq,
				mimetype: 'json'
			},
			scope: this
		});

	};

	/**
	 * Callback function for the Reach Identify service.  This function receives the
	 * callback and decodes the response.  It then delegates to the render function.
	 */
	this.loadIdentifyReach =  function(response, animateOptions, showInfo) {
	    var srcJSON = response.responseText;
	    var id_JSON = Ext.util.JSON.decode(srcJSON);

	    self.renderIdentify(id_JSON, animateOptions, showInfo);

	    idRequest = null;
	};


	/**
	 * Renders the reach information contained within reachResponse to the user
	 * interface.  Calculates and draws a bounding box around the reach, places a
	 * marker at the point clicked, and opens the Reach Identify window.
	 */
	this.renderIdentify = function(reachResponse, animateOptions, showInfo) {

		{ // Check the status of the response
			var status = reachResponse["sparrow-id-response"]["status"];

			if (status != "OK") {
				// If failed, display message to user and return
				var message = reachResponse["sparrow-id-response"]["message"];
    			IDENTIFY_REACH_SPINNER.hide();
				Ext.Msg.alert('Identify', message);
	        	return;
			}
		}

		Sparrow.SESSION.setReachIdOverlayRequested(true, null, reachResponse['sparrow-id-response']['results']['result'][0]['identification']['id']);
		
		if (showInfo) {
			renderReachIdentifyWindow(reachResponse);
		}

	    clicked_lat = null;
	    clicked_lon = null;

	};

	/**
	 * user had identify calibration tool selected and clicked on map
	 */
	this.identifyCalibrationSite = function(event, map) {
		event = event || window.event;

		//calc lat/lon of mouse click event
		var coordsPx = JMap.util.getRelativeCoords(event, map.pane);
		var ll = map.getLatLonFromScreenPixel(coordsPx.x, coordsPx.y);
		var model_id = Sparrow.SESSION.PredictionContext["@model-id"];

		//destroy any previous popup stuff
		if (map.identifyPopup) map.identifyPopup.kill();

		self.identifyCalibSite(ll.lat, ll.lon, model_id);
	};
	this.identifyCalibrationSite.cursor = "cursor-identify-point";

	this.identifyCalibSite = function(lat, lon, model_id) {
		IDENTIFY_CALIB_SITE_SPINNER.show();
		Ext.Ajax.request({
	    	method: 'POST',
	    	url: 'getCalibrationSite',
	    	success: function(r,o) {
	    		this.loadIdentifyCalibrationSite(r);
	    			IDENTIFY_CALIB_SITE_SPINNER.hide();
	    	},
	    	failure: function(r,o) {
	    		Ext.Msg.alert('Warning', 'identify failed');
	    			IDENTIFY_CALIB_SITE_SPINNER.hide();
	    	},
	    	params: {
	    		lat: lat,
	    		lon: lon,
	    		model_id: model_id
	    	},
	    	scope: this
	    });
	};
	this.loadIdentifyCalibrationSite =  function(response) {
		var responseObj = response.responseXML.lastChild;
		if(responseObj.getElementsByTagName('stationId')[0]) {
		    var stationId =  responseObj.getElementsByTagName('stationId')[0].firstChild.nodeValue;
		    var stationName = responseObj.getElementsByTagName('stationName')[0] ? responseObj.getElementsByTagName('stationName')[0].firstChild.nodeValue : '';
		    var reachId = responseObj.getElementsByTagName('reachId') ? responseObj.getElementsByTagName('reachId')[0].firstChild.nodeValue : '';
		    var reachName =  responseObj.getElementsByTagName('reachName') ? responseObj.getElementsByTagName('reachName')[0].firstChild.nodeValue : '';
		    var lat = responseObj.getElementsByTagName('latitude') ? parseFloat(responseObj.getElementsByTagName('latitude')[0].firstChild.nodeValue) : '';
		    var lon = responseObj.getElementsByTagName('longitude') ? parseFloat(responseObj.getElementsByTagName('longitude')[0].firstChild.nodeValue) : '';
		    var actual = responseObj.getElementsByTagName('actualValue') ? parseFloat(responseObj.getElementsByTagName('actualValue')[0].firstChild.nodeValue) : '';
		    var predict = responseObj.getElementsByTagName('predictedValue') ? parseFloat(responseObj.getElementsByTagName('predictedValue')[0].firstChild.nodeValue) : '';

		    //getPixelFromLatLon
		    //getLatLonFromPixel
		    //Draw the marker square
		    var pixelRadius = 7;
		    var originalPixel = map1.getPixelFromLatLon(lon, lat);
		    var leftPoint = map1.getLatLonFromPixel(originalPixel.x, originalPixel.y-pixelRadius);
		    var topPoint = map1.getLatLonFromPixel(originalPixel.x+pixelRadius, originalPixel.y);
		    var rightPoint = map1.getLatLonFromPixel(originalPixel.x, originalPixel.y+pixelRadius);
		    var bottomPoint = map1.getLatLonFromPixel(originalPixel.x-pixelRadius, originalPixel.y);

		    var marker = new JMap.svg.PolyLine({
	        	points: [
	        	         leftPoint.lon, leftPoint.lat,
	        	         topPoint.lon, topPoint.lat,
	        	         rightPoint.lon, rightPoint.lat,
	        	         bottomPoint.lon, bottomPoint.lat,
	        	         leftPoint.lon, leftPoint.lat],
	        	properties: {
	    	    	stroke: 'red',
	    	    	'stroke-width': '2px',
	    	    	fill: 'white',
	    	    	'fill-opacity': 0.1,
	    	    	'stroke-linejoin': 'round'
	    	    }
	        });
		    map1.drawShape(marker);
		    var window = new Ext.Window({
		    	width: 300,
		    	height: 250,
		    	resizable: false,
		    	title: 'Station ID: '+ stationId,
		    	padding: 5,
		    	html: //TODO maybe use a template
		    		"Station ID: "+ stationId + "<br/>"+
		    		"Station Name: "+ stationName + "<br/>"+
		    		"Reach: "+ reachName+ " (ID: "+reachId+")" + "<br/>"+
		    		"Latitude: "+ lat + "<br/>"+
		    		"Longitude: "+ lon + "<br/>" +
		    		"Measured Total Load (kg&middot;year<sup>-1</sup>): " + actual + "<br/>" +
		    		"Predicted Total Load (kg&middot;year<sup>-1</sup>): " + predict + "<br/>" +
		    		"Measured / Predicted: " + Math.round((actual/predict)*1000)/1000
		    	,
		    	listeners: {
		    		close: function() {
		    			map1._SVGManager.removeShape(marker);
		    		}
		    	},
		    	scope: this
		    });
		    window.show();
		}
	};





	this.abort = function(){
		Ext.Ajax.abort(idRequest);
	}

})();


/**
 * Initiates a request to the Reach Identify service to update the Predicted
 * Values tab.  This will be based on the current mapped state, not the valid state.
 */
function updateReachPredictions(reachId) {
	var xmlreq = ''
		+ '<sparrow-id-request '
		+ 'xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" '
		+ 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
		+ '<context-id>' + Sparrow.SESSION.getMappedOrValidContextId() + '</context-id>'
		+ '<reach id="' + reachId + '" />'
		+ '<content><predicted /></content>'
		+ '<response-format><mime-type>json</mime-type></response-format>'
		+ '</sparrow-id-request>'
		;

	//document.getElementById('debug').innerHTML = xmlreq.replace(/</g,'&lt;');
	Ext.Ajax.request({
		method: 'POST',
		url: 'getIdentify',
		success: renderPredictionsTabOnly,
		params: {
			xmlreq: xmlreq,
			mimetype: 'json'
		}
	});
}

/**
 * Callback function for the Reach Identify service when initiated by the
 * updateReachPredictions() function.  This function receives the callback,
 * decodes the response, and delegates rendering.
 */
function renderPredictionsTabOnly(response, options) {
    var srcJSON = response.responseText;
    var id_JSON = Ext.util.JSON.decode(srcJSON);

    // TODO: Should we delegate this through the ReachIdentify window instead?
    Ext.getCmp('predictedValuesTabGrid').refresh(id_JSON);
    renderGraph(id_JSON);
}

/**
 * Renders the Graph tab in the Reach Identify window.  Pulls the data out of
 * the prediction response, and builds URLs to Google Charts to display the
 * graphs.
 *
 * TODO: Move this rendering to the GraphPanel object.
 */
function renderGraph(id_JSON) {

	if (document.getElementById('gcapi-warn')) {
		//The test above is a generic test if the graph contents are rendered -
		//Can't really modify them if not yet init'ed by EXT.

	    var predictions_sections = id_JSON["sparrow-id-response"].results.result[0].predicted.data.section;
	    var adjustments = Sparrow.SESSION.hasEnabledAdjustments();

	    for (var i = predictions_sections.length - 1; i >= 1; i--) {
	        var orig_url = '';
	        var orig_vals = '';
	        var orig_col = '';
	        var adj_url = '';
	        var adj_vals = '';
	        var adj_col = '';
	        var orig_adj_col = '';
	        var label_url = '';
	        var orig_label_url = '';
	        var adj_label_url = '';
	        var max_val = 0;
	        var legend_html = '<table id="graph_legend_table"><tr><th colspan="'+((adjustments) ? '6': '4')+'">Sources</th></tr>';

	        if(adjustments)
	        	legend_html += "<tr><td>Orig.</td><td>Adj.</td><td></td><td>Orig.</td><td>Adj.&nbsp;</td><td></td></tr>";
	        //find sum of values for the pie chart (need to do percentage of total)
	        var orig_total = 0;
	        var adj_total = 0;
	        var predictions_rows = predictions_sections[i]["r"];
	        for (var j = 0; j < predictions_rows.length - 1; j++) {
	            var predictions_cols = predictions_rows[j]["c"];
	            orig_total += parseFloat(predictions_cols[4]);
	            adj_total += parseFloat(predictions_cols[5]);
	        }

	        var getColor = function(indx, adj) {
	        	if(adj) return Sparrow.config.GraphColorArray[indx % Sparrow.config.GraphColorArray.length][1];
	        	return Sparrow.config.GraphColorArray[indx % Sparrow.config.GraphColorArray.length][0];
	        };

	        var getColorHtml = function(indx, adj) {
	        	var html = "<td style='width: 10px; height: 10px; background-color: #"+getColor(indx, false)+";'>&nbsp;</td>";
	        	if(adj)
	        		html += "<td style='width: 10px; height: 10px; background-color: #"+getColor(indx, true)+";'>&nbsp;</td>";
	        	return html;
	        };

	        //get values for bar charts
	        for (var j = 0; j < predictions_rows.length - 1; j++) {
	            var predictions_cols = predictions_rows[j]["c"];

	            //Get the source name, removing ' Total Load' from the end if present
	            var TL = " total load";
	            var valueLabel = predictions_cols[0];
	            var lcValueLabel = valueLabel.toLowerCase();
	            var tlIndex = lcValueLabel.lastIndexOf(TL);

	            if (tlIndex > -1 && valueLabel.length == tlIndex + TL.length) {
	            	//' total load' is the last portion of the src name
	            	valueLabel = valueLabel.substr(0, valueLabel.length - TL.length);
	            }

	            if(!(j%2)) {
	            	legend_html += '<tr>';
	            }
	            legend_html += getColorHtml(j, adjustments)+"<td>" + valueLabel + "</td>";

	            if(j % 2 || j === predictions_rows.length-2) {
	            	legend_html += '</tr>';
	            }

	            orig_label_url+= (Math.round(predictions_cols[4]/orig_total*1000)/10)+"%|";
	            orig_url += (parseFloat(predictions_cols[4]) / orig_total) + ",";
	            orig_vals += predictions_cols[4] + ",";
	            orig_col += getColor(j, false)+"|";

	            adj_label_url+= (Math.round(predictions_cols[5]/adj_total*1000)/10)+"%|";
	            adj_url += (parseFloat(predictions_cols[5]) / adj_total) + ",";
	            adj_vals += predictions_cols[5] + ",";
	            adj_col += getColor(j, true)+"|";

	            //find max val in data for graph scaling
	            if (parseFloat(predictions_cols[4]) > max_val) max_val = parseFloat(predictions_cols[4]);
	            if (parseFloat(predictions_cols[5]) > max_val) max_val = parseFloat(predictions_cols[5]);
	        }

	        legend_html += "</table>";
	        orig_label_url = orig_label_url.substring(0,orig_label_url.length-1);
	        adj_label_url = adj_label_url.substring(0,adj_label_url.length-1);
	        orig_url = orig_url.substring(0,orig_url.length-1);
	        adj_url = adj_url.substring(0,adj_url.length-1);
	        orig_vals = orig_vals.substring(0,orig_vals.length-1);
	        adj_vals = adj_vals.substring(0,adj_vals.length-1);
	        orig_col = orig_col.substring(0,orig_col.length-1);
	        adj_col = adj_col.substring(0,adj_col.length-1);
	        orig_adj_col = orig_col +","+adj_col;

	        //set src of graph images
	    	var chart_width = 170 + (predictions_rows.length-1)*56;
	    	var chart_height = 50 + (predictions_rows.length-1)*30;
	        if (chart_width > 1000) {
	        	document.getElementById('gcapi-warn').style.display = 'block';
	        	document.getElementById('gcapi-graphs').style.display = 'none';
	        } else {
	        	document.getElementById('gcapi-warn').style.display = 'none';
	        	document.getElementById('gcapi-graphs').style.display = 'block';
	        	var prettyUnits = Sparrow.USGS.prettyPrintUnitsForGoogleQueryParam(Sparrow.SESSION.getModelUnits());
	        	if(Sparrow.SESSION.hasEnabledAdjustments()) {
	        		document.getElementById('src_total_adj').style.display = 'block';
	        		document.getElementById('src_bvg').src = 'http://chart.apis.google.com/chart?chco='+ orig_adj_col +'&chtt=Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source (' + prettyUnits + ')&chs='+chart_width+'x'+chart_height+'&cht=bvg&chd=t:' + orig_vals + '|' + adj_vals + '&chl=' + label_url + '&chds=0,' + max_val + '&chxt=y&chxr=0,0,' + max_val;
	            	document.getElementById('src_total_orig').src = 'http://chart.apis.google.com/chart?chco='+ orig_col +'&chtt=Share of Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source - Original&chs=420x140&cht=p&chd=t:' + orig_url + '&chl=' + orig_label_url;
	            	document.getElementById('src_total_adj').src = 'http://chart.apis.google.com/chart?chco='+ adj_col +'&chtt=Share of Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source - Adjusted&chs=420x140&cht=p&chd=t:' + adj_url + '&chl=' + adj_label_url;
	            	document.getElementById('src_graph_legend').innerHTML = legend_html;
	        	}
	        	else {
	        		chart_width = chart_width / 2;
	        		document.getElementById('src_total_adj').style.display = 'none';
	        		document.getElementById('src_bvg').src = 'http://chart.apis.google.com/chart?chco='+ orig_col +'&chtt=Total Load by Source (' + prettyUnits + ')&chs='+chart_width+'x130&cht=bvg&chd=t:' + orig_vals + '&chl=' + label_url + '&chds=0,' + max_val + '&chxt=y&chxr=0,0,' + max_val;
	            	document.getElementById('src_total_orig').src = 'http://chart.apis.google.com/chart?chco='+ orig_col +'&chtt=Share of Total ' + Sparrow.SESSION.getModelConstituent() + ' Load by Source&chs=420x140&cht=p&chd=t:' + orig_url + '&chl=' + orig_label_url;
	            	document.getElementById('src_graph_legend').innerHTML = legend_html;
	        	}
	        }
	    }

	}
}

/**
 * Gets the id for the current PredictionContext.
 *
 * returns bool indicates whether or not function passed pre-conditions for retrieving context
 * 
 * options: (passed in and the contextId is set on completion)
 * 	scope		: Set to this by caller
 *	callback	: method to call when complete
 *	callbackChain : Array of sequentioal callbacks
 *	errorHandler : method to call if there is an exception (in the case of a chain, the same one for all)
 *	----
 *	These are values set on the options by this method
 *	contextId	: The ID registered or, if the context is up to date, the current context ID.
 *	contextXml	: the XML document containing the new context
 *	errorMessage : If the call failed, a message...
 */
function getContextIdAsync(options) {
	
	//Callbacks are defined either as options.callback or as an array in options.callbackChain
	//in which item 0 is the next callback to be called.
	//.callback takes precidence over .callbackChain and .callback will be removed after it is used.
	var callback;
	if (options.callback) {
		callback = options.callback;
		delete options.callback;	//remove so later chained calls happen as normal
	} else if (options.callbackChain) {
		callback = options.callbackChain.shift();
	}
	

    if (Sparrow.SESSION.isChangedSinceLastMarkedState() || ! Sparrow.SESSION.isLastValidLastValidContextFullySpecified) {
		
		if (! Sparrow.SESSION.isValidContextState()) {
			if (options.errorHandler) {
				options.errorMessage = 'The currently selected data options are not valid:<br/>' + Sparrow.SESSION.getInvalidContextStateMessage();
				options.errorHandler.call(this, null, options);
			} else {
				Ext.Msg.alert('Warning', 'The currently selected data options are not valid:<br/>' + Sparrow.SESSION.getInvalidContextStateMessage());
			}
		} else {
			
			Ext.Ajax.request({
				method: 'POST',
				url: 'getContextId',
				params: {
					xmlreq: Sparrow.SESSION.getPredictionContextAsXML()
				},
				success: function(r,o) {
					var xmlDoc = r.responseXML;
					var contextId = xmlDoc.childNodes[0].getAttribute("context-id");
	
	
					//Store series info as part of the last valid state
					var lastValidSeriesData = {
						seriesConstituent: Sparrow.utils.getFirstXmlElementValue(xmlDoc, 'constituent'),
						seriesUnits: Sparrow.utils.getFirstXmlElementValue(xmlDoc, 'units'),
						seriesName: Sparrow.utils.getFirstXmlElementValue(xmlDoc, 'name'),
						seriesDescription: Sparrow.utils.getFirstXmlElementValue(xmlDoc, 'description'),
						rowCount: Sparrow.utils.getFirstXmlElementValue(xmlDoc, 'rowCount'),
					};
					Sparrow.SESSION.markValidState(contextId, lastValidSeriesData);

					options.contextXml = xmlDoc;
					options.contextId = contextId;

					if (callback) callback.call(this, options);
				},
				failure: function(r,o) {

					if (options.errorHandler) {
						options.errorMessage = 'Failed getting context id';
						options.errorHandler.call(this, r, options);
					} else {
						Ext.Msg.alert('Warning', 'Failed getting context id');
					}
				}
			});
		}
		

    } else {
		options.contextId = Sparrow.SESSION.getLastValidContextId();
        if (callback) callback.call(this, options);
    }
}

/**
 * Registers a data layer to be mapped, based on the passed context ID.
 * This method will set the WmsDataLayerName so that it can be refered to for mapping.
 * 
 * @param options.contextId - The context ID to register
 * @param options.callback - A function to call when complete
 * @return false if the registration failed, otherwise the callback is called.
 */
function confirmOrAdjustCurrentContextBins(options) {
	
	//Callbacks are defined either as options.callback or as an array in options.callbackChain
	//in which item 0 is the next callback to be called.
	//.callback takes precidence over .callbackChain and .callback will be removed after it is used.
	var callback;
	if (options.callback) {
		callback = options.callback;
		delete options.callback;	//remove so later chained calls happen as normal
	} else if (options.callbackChain) {
		callback = options.callbackChain.shift();
	}
	
	var contextId = options.contextId;
	if (! contextId) contextId = Sparrow.SESSION.getLastValidContextId();
	
	var urlParams = 'context-id=' + contextId;

	var bins = Sparrow.SESSION.getBinData()['functionalBins'];
	//PermanentMapState["binning"]["bins"];
	urlParams += '&binLowList=' + Ext.pluck(bins, 'low').join();
	urlParams += '&binHighList=' + Ext.pluck(bins, 'high').join();
	urlParams = encodeURI(urlParams);
				
	Ext.Ajax.request({
		url: 'confirmBins',
		method: 'GET',
		params: urlParams,
		scope: this,
		success: function(response, o) {
			var allValuesInABin = response.responseXML.lastChild.getElementsByTagName('entity')[0].firstChild.nodeValue;
			var allValuesInASingleBin = response.responseXML.lastChild.getElementsByTagName('entity')[1].firstChild.nodeValue;

			if(allValuesInABin != 'true' || allValuesInASingleBin != 'true'){
				var msg = '<br/>Would you like to adjust the bins to include the entire set of results on the map using <b><i>' + Sparrow.SESSION.getAutoBinTypeName() + '</i></b> bins?';

				if(allValuesInABin=='false') msg = '- There are results not included in your custom bins that will not be displayed on the map.<br/>' + msg;
				if(allValuesInASingleBin=='false') msg = '- All results fall into a single bin.<br/>' + msg;

				Ext.Msg.confirm(
					'Potential Binning Issues',
					msg,
					function(userResp){
						if(userResp == 'yes') {
							options.callback = callback;
							generateBins(options);
						} else {
							if (callback) callback.call(this, options);
						}
					}
				);
			} else {
				if (callback) callback.call(this, options);
			}
			
		},
		failure: function(response, o) {
			Ext.Msg.alert('Warning', 'Failed to confirm bins');
		},
		timeout: 40000
	});
};

/**
 * Generates bins for the current valid context.
 *
 * returns bool indicates whether or not function passed pre-conditions for retrieving context
 * 
 * options: (passed in and the contextId is set on completion)
 * 	scope		: Set to this by caller
 *	callback	: method to call when complete
 *	callbackChain : Array of sequential callbacks
 *	errorHandler : method to call if there is an exception (in the case of a chain, the same one for all)
 *	----
 *	These are values set on the options by this method
 *	binData	: The parsed as an object bin data, based on the server response xml.
 *	errorMessage : If the call failed, a message...
 */
function generateBins(options) {
	
	//Callbacks are defined either as options.callback or as an array in options.callbackChain
	//in which item 0 is the next callback to be called.
	//.callback takes precidence over .callbackChain and .callback will be removed after it is used.
	var callback;
	if (options.callback) {
		callback = options.callback;
		delete options.callback;	//remove so later chained calls happen as normal
	} else if (options.callbackChain) {
		callback = options.callbackChain.shift();
	}
	
	var bucketCount = (options.binCount)?options.binCount:Sparrow.SESSION.getBinCount();
	var bucketType = (options.binType)?options.binType:Sparrow.SESSION.getAutoBinType();
	var contextId = options.contextId;
	if (! contextId) contextId = Sparrow.SESSION.getLastValidContextId();
	
	Ext.Ajax.request({
		url: 'getBins',
		method: 'GET',
		params: 'context-id=' + contextId + '&bin-count=' + bucketCount + '&bin-type=' + bucketType,
		scope: this,
		success: function(response, o) {
			var binValues = Sparrow.ui.parseBinDataResponse(response.responseXML);
			options.binData = binValues;
			
			//If later chain calls fail, how can we fix?
			if (callback) callback.call(this, options);
		},
		failure: function(r,o) {

			if (options.errorHandler) {
				options.errorMessage = "Failed getting bins from the server";
				options.errorHandler.call(this, r, options);
			} else {
				Ext.Msg.alert('Warning', "Failed getting bins from the server");
			}
		},
		timeout: 40000
	});
};


/**
 * Registers a data layer to be mapped, based on the passed context ID.
 *
 * options: (passed in and the contextId is set on completion)
 * 	scope		: Set to this by caller
 *	callback	: method to call when complete
 *	callbackChain : Array of sequential callbacks
 *	errorHandler : method to call if there is an exception (in the case of a chain, the same one for all)
 *	----
 *	These are values set on the options by this method
 *	dataLayer	: An object w/ separate properties for datalayerWmsUrl, flowlineLayerName & catchLayerName
 *	errorMessage : If the call failed, a message...
 */
function registerDataLayer(options) {
	
	var callback;
	if (options.callback) {
		callback = options.callback;
		delete options.callback;	//remove so later chained calls happen as normal
	} else if (options.callbackChain) {
		callback = options.callbackChain.shift();
	}
	
	var contextId = options.contextId;
	if (! contextId) contextId = Sparrow.SESSION.getLastValidContextId();
	
	if (! Sparrow.SESSION.isRegisteredDataLayer(contextId)) {
		//not already registered
		
		var onLocalErr = function(r) {
			if (options.errorHandler) {
				options.errorMessage = "Failed registering the data map layer with the map server";
				options.errorHandler.call(this, r, options);
			} else {
				Ext.Msg.alert('Warning', "Failed registering the data map layer with the map server");
			}
		};

		Ext.Ajax.request({
			method: 'GET',
			url: 'RegisterMapLayerService?context-id=' + contextId +'&projected-srs=EPSG:4326',
			success: function(r,o) {
				var ok = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'Status');

				if (ok == 'OK') {

					options.dataLayer = new Object();
					options.dataLayer.dataLayerWmsUrl = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'EndpointUrl');
					options.dataLayer.flowlineDataLayerName = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'FlowLayerName');
					options.dataLayer.catchDataLayerName = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'CatchLayerName');

					if (callback) callback.call(this, options);
				} else {
					onLocalErr.call(this, r, options);
				}
			},
			failure: onLocalErr
		});
		
	} else {
		//already registered - move on
		options.dataLayer = null;
		if (callback) callback.call(this, options);
	}
};

function saveNewState(options) {
	
	var callback;
	if (options.callback) {
		callback = options.callback;
		delete options.callback;	//remove so later chained calls happen as normal
	} else if (options.callbackChain) {
		callback = options.callbackChain.shift();
	}		
	
	if (Sparrow.SESSION.isBinAuto()) {
		if (options.binData) {
			Sparrow.SESSION.setBinData(options.binData, false);
		} else {
			//This is the case when the context has not changed.
		}
	} else if (options.binData) {
		//Custom bins, but the user has requested that the values be adjusted
		options.binData['binColors'] = null;	//rm colors (keep user's previous colors)
		Sparrow.SESSION.setBinData(options.binData, false);
	}
	
	Sparrow.SESSION.setCurrentDataLayerInfo(options.contextId, options.dataLayer);
	
	if (callback) callback.call(this, options);
}

/**
 * Async fetches the auto-generated bins for the current map state and calls the
 * passed handler when complete.
 * 
 * The handler is passed an options object with these fields:
 * options.binData - The requested binData object
 * 
 * If options.binData is undefined, the call failed and a user msg was displayed.
 * 
 * This should be used instead of calling generateBins directly, since this auto-
 * registers the context ID as needed.
 * 
 * Bin data is not saved:  only returned to the caller.
 * 
 * @param callBackHandler function called when complete.  Options are passed
 * @param scope Scope passed through to callback handler as part of options:  options.scope.
 * @returns binData
 * 
 */
function fetchAutoBins(callBackHandler, binType, binCount, scope) {

	//Standard options structure, pulling all possible return values together
	//for the call chain.
	var options = new Object();
	options.contextId = null;
	options.binType = binType;
	options.binCount = binCount;
	options.scope = scope;
	options.binData = null;	//See SparrowUIContext for object definition
	
	if (Sparrow.SESSION.isChangedSinceLastMarkedState()) {

		options.callbackChain = [generateBins, callBackHandler];
		getContextIdAsync(options);
		
	} else {

		options.contextId = Sparrow.SESSION.getLastMappedContextId();
		options.callbackChain = [callBackHandler];
		generateBins(options);
	}
}
/**
 * Add the sparrow datalayer to the map
 */
function make_map() {

	//Standard options structure, pulling all possible return values together
	//for the call chain.
	var options = new Object();
	options.contextId = null;
	options.binData = null;	//See SparrowUIContext for object definition
	options.dataLayer = new Object();
	options.dataLayer.datalayerWmsUrl = null;
	options.dataLayer.flowlineLayerName = null;
	options.dataLayer.catchLayerName = null;
	
	//TODO:  We could add an error handler that says we failed to map.
	
	if (Sparrow.SESSION.isContextChangedSinceLastMap()) {
		//Need to register context and layer
		if (Sparrow.SESSION.isBinAuto()) {
			options.callbackChain = [generateBins, registerDataLayer, saveNewState, addDataLayer];
		} else {
			options.callbackChain = [confirmOrAdjustCurrentContextBins, registerDataLayer, saveNewState, addDataLayer];
		}
		
		getContextIdAsync(options);	//Adds the dataLayer if successful
	} else {
		//Context and layer already registered
		
		
		//Since we skip fetching the context id from server, set it here so other
		//chained methods can find it
		options.contextId = Sparrow.SESSION.getLastMappedContextId();
		
		if (Sparrow.SESSION.isBinAuto()) {
			
			if (Sparrow.SESSION.isAutoBinDataCurrent()) {
				//No need to re-gen autobin data
				options.callbackChain = [addDataLayer];
				saveNewState(options);
			} else {
				//Prev map had custom bins, now auto, so regen bins
				options.callbackChain = [saveNewState, addDataLayer];
				generateBins(options);
			}

		} else {
	
			if (Sparrow.SESSION.isCustomBinDataCurrent()) {
				options.callbackChain = [addDataLayer];
				saveNewState(options);	//Bins may have change since last map - reconfirm
			} else {
				options.callbackChain = [saveNewState, addDataLayer];
				confirmOrAdjustCurrentContextBins(options);	//Bins may have change since last map - reconfirm
			}

		}
	}
	
}

/**
 * When adding the data layer, we always assume that the lastValidContext is
 * up to date and will be what we map from.  This method then promotes the last
 * valid state to the last mapped state.
 */
function addDataLayer() {

	//Internal ID used for the map layer
	var mappedValueLayerID = Sparrow.config.layers.mainDataLayer.id,
        //get parameters to create base url for sparrow data layer
        what_to_map = Sparrow.SESSION.PermanentMapState["what_to_map"],
        bins = Sparrow.SESSION.getBinData()["functionalBins"],
		binsAreDefault = Sparrow.SESSION.isBinAuto(),
        boundedFlag = !Sparrow.SESSION.getBinData()["boundUnlimited"][0].low,
        colors = Sparrow.SESSION.getBinData()["binColors"],
        binParams,
		dataLayerInfo = Sparrow.SESSION.getDataLayerInfo(),
        dataLayerWmsUrl = dataLayerInfo.dataLayerWmsUrl,	
        gsUrl = dataLayerInfo['dataLayerWmsUrl'].substring(0, dataLayerWmsUrl.length - 3),
        sldUrl,
        workspace,
        layerName,
        wsAndLayerName,
		isReusableLayers,
        splitWsAndLayerName;

	
	if (what_to_map === "reach") {
		wsAndLayerName = dataLayerInfo.flowlineDataLayerName;
	} else {
		wsAndLayerName = dataLayerInfo.catchDataLayerName;
	}
    
    binParams = 'binLowList=' + Ext.pluck(bins, 'low').join();
	binParams += '&binHighList=' + Ext.pluck(bins, 'high').join();
	binParams += '&binColorList=' + colors.join();
	binParams += '&bounded=' + boundedFlag;
    
    splitWsAndLayerName = wsAndLayerName.split(':');
    workspace = splitWsAndLayerName[0];
    layerName = splitWsAndLayerName[1];
	isReusableLayers = (workspace.indexOf("reusable") > -1);
    sldUrl = gsUrl + 'rest/sld/workspace/';
    sldUrl += workspace;
    sldUrl += '/layer/';
    sldUrl += layerName;
    sldUrl += '/' + what_to_map + '.sld?' + binParams;
	
	var layerParams = {
			
		format: "image/png8",
		zDepth: Sparrow.config.layers.mainDataLayer.zDepth,
		id: mappedValueLayerID,
		scaleMin: Sparrow.config.layers.mainDataLayer.scaleMin,
		scaleMax: Sparrow.config.layers.mainDataLayer.scaleMax,
		baseUrl: dataLayerWmsUrl + "?",
		legendUrl: 'getLegend?' + binParams,
		title: wsAndLayerName,
		name: wsAndLayerName,
		layersUrlParam: wsAndLayerName,
		isHiddenFromUser: true,
		description: Sparrow.config.layers.mainDataLayer.title,
		opacity: Sparrow.SESSION.getDataLayerOpacity()
	};
	
	if (isReusableLayers && binsAreDefault) {
		//options for a tile-cache layer (format_options ignored, but the server
		//is config'ed to use the ones listed here for ref)
		layerParams['customParams'] = {
			tiled: "true"
			//format_options: 'antialiasing:none;quantizer:octree;'
		};
	} else {
		layerParams['customParams'] = {
			//A non-tile-cache layer, which must spec style and format options
			sld: sldUrl,
			format_options: 'antialiasing:none;quantizer:octree;'
		};
	}

    map1.layerManager.unloadMapLayer(mappedValueLayerID);
    map1.appendLayer(
    	new JMap.web.mapLayer.WMSLayer(layerParams)
    );

	Sparrow.SESSION.markMappedState(Sparrow.SESSION.getLastValidContextId(), Sparrow.SESSION.getLastValidSeriesData());
	
	// Update the legend (uses infor from the marked map state above)
    var legendEl = Ext.get('legend');
    Ext.DomHelper.overwrite(legendEl, '');
    Sparrow.ui.renderLegend();
	
	
    Sparrow.SESSION.fireContextEvent('map-updated-and-synced');

}






