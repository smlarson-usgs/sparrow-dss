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

var modelSourcesAreInCache = function(id) {
	if(!modelSourcesCache) {
		modelSourcesCache = new Array();
	}
    for (var i = 0; i < modelSourcesCache.length; i++) {
        if (modelSourcesCache[i]["@id"] == model_id) {
        	return true;
        }
    }
    return false;
}

/**
 * Retrieve the model name and list of sources for the current model.
 */
function getModel() {
    if (modelSourcesAreInCache(model_id)) {
        // Pull it out of cache
        renderModel();
    } else {
        // Make a request to the model service for all of the sources
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
        	success: renderModel,
        	params: {
        		xmlreq: xmlreq,
        		mimetype: 'json'
        	}
        });
    }
}

/**
 * Renders the model source options to the page.  This function renders the
 * options to the data series dropdown, and to the treatments tab used when
 * creating a group.
 *
 *
 * TODO: // THIS SHOULD ALL BE DONE USING EXT!!!!!
 *
 *
 */
function renderModel(response, options) {
    // Pull out the sources response text and cache it
	if(response) {
		modelSourcesCache.push(Ext.util.JSON.decode(response.responseText)["models"]["model"][0]);
	}

    // Get the model name, list of sources, and model bounds
    var modelName = '';
    var sourceList = [];
    var themeName = "";
    var boundNorth = 0;
    var boundEast = 0;
    var boundSouth = 0;
    var boundWest = 0;
    var constituent = '';
    var units = '';
    var docUrl = '';

    for (var i = 0; i < modelSourcesCache.length; i++) {
        // Check the model id and pull the sources and bounds if we find the right model
        if (modelSourcesCache[i]["@id"] == model_id) {
            modelName = modelSourcesCache[i]["name"];
            themeName = modelSourcesCache[i]["themeName"];
            sourceList = modelSourcesCache[i]["sources"]["source"];
            boundNorth = parseFloat(modelSourcesCache[i]["bounds"]["@north"]);
            boundEast = parseFloat(modelSourcesCache[i]["bounds"]["@east"]);
            boundSouth = parseFloat(modelSourcesCache[i]["bounds"]["@south"]);
            boundWest = parseFloat(modelSourcesCache[i]["bounds"]["@west"]);
            constituent = modelSourcesCache[i]["constituent"];
            units = modelSourcesCache[i]["units"];
            try {
            	docUrl = modelSourcesCache[i]["url"];
            } catch(e) {}
            break;
        }
    }

    Sparrow.SESSION.setModelName(modelName);
    Sparrow.SESSION.setThemeName(themeName);
    Sparrow.SESSION.setModelConstituent(constituent);
    Sparrow.SESSION.setModelUnits(units);
    Sparrow.SESSION.setOriginalBoundSouth(boundSouth);
    Sparrow.SESSION.setOriginalBoundNorth(boundNorth);
    Sparrow.SESSION.setOriginalBoundWest(boundWest);
    Sparrow.SESSION.setOriginalBoundEast(boundEast);
    var docMenu = Ext.menu.MenuMgr.get('sparrow-documentation-menu');

	/**
	 * @param {string} name - the user-facing text for the menu item
	 * @param {string} videoId - the youtube video id
	 */
	var addVideoItemToDocMenu = function(name, videoId){
		//access docMenu through closure
		docMenu.add({
	   	text: 'Video: ' + name,
	   	handler: function() {
	   		openScreencast(videoId);
	   	}
		});
	};

    docMenu.removeAll();
    if(docUrl != null) {
    	Sparrow.SESSION.setDocUrl(docUrl);
        docMenu.add({
        	text: 'About: ' + Sparrow.SESSION.getModelName() + '...',
        	handler: function() {
        		var docUrl = Sparrow.SESSION.getDocUrl();
        		var newWindow = window.open(docUrl, '_blank');
        		newWindow.focus();
        	}
        });
    }
    docMenu.add({
    	text: 'What is SPARROW?',
    	handler: function() {
    		var newWindow = window.open('http://pubs.usgs.gov/fs/2009/3019/pdf/fs_2009_3019.pdf', '_blank');
    		newWindow.focus();
    	}
    });
   docMenu.add({
    	text: 'SPARROW Applications & Documentation',
    	handler: function() {
    		var newWindow = window.open('http://water.usgs.gov/nawqa/sparrow/', '_blank');
    		newWindow.focus();
    	}
    });
   docMenu.add({
   	text: 'SPARROW FAQs',
   	handler: function() {
   		var newWindow = window.open('faq.jsp', '_blank');
   		newWindow.focus();
   	}
   });
   docMenu.add('-');
   docMenu.add({
	   text: 'Tutorial Videos',
	   style: {'font-weight': 'bold', 'font-size': '110%'}
   });
   docMenu.add({
	   text: 'Video windows can be resized to show full detail',
	   style: {'font-style': 'italic'}
   });
   docMenu.add('-');
   //add videos
   Ext.iterate(screenCastNameToVideoIdMap, function(name, videoId){
		addVideoItemToDocMenu(name, videoId);
   });

   Ext.getCmp('map-options-tab').autoBinsChk.setValue(Sparrow.SESSION.isBinAuto());

    // Zoom and center the map over the model's bounds
    if (response && boundEast != undefined && boundEast != 0) {
        map1.fitToBBox(boundEast, boundSouth, boundWest, boundNorth);
    }

    // Render the appropriate model 'theme'
    var siteTitleBar = document.getElementById('title-model-name');
    siteTitleBar.innerHTML = " - " + Sparrow.SESSION.getModelName();

    // Get the treaments tab from the group defintion window
    var treatmentTab = document.getElementById('treatment-tab');
    treatmentTab.innerHTML = '';

    // Iterate over the sources
    var mapOptionsTab = Ext.getCmp('map-options-tab');
    mapOptionsTab.clearSources();

    for (var i = 0; i < sourceList.length; i++) {

        // Add to the data series source select
        var displayName = sourceList[i]["displayName"];
        var description = sourceList[i]["description"];
        mapOptionsTab.addSource(displayName, i + 1, description);

        // Add a row to the treatment tab
        var data_row = document.createElement('div');
        data_row.className = 'data_row clearfix';
        (i%2) ? data_row.style.backgroundColor = '#FFFFFF' : data_row.style.backgroundColor = '#EEEEEE';

        var src_name = document.createElement('div');
        src_name.className = 'col_25';
        src_name.innerHTML = sourceList[i]["displayName"];

        var src_constituent = document.createElement('div');
        src_constituent.className = 'col_20';
        src_constituent.innerHTML = sourceList[i]["constituent"] + ' (' + sourceList[i]["units"] + ')';

        var src_adj_div = document.createElement('div');
        src_adj_div.className = 'col_20';
        var src_adj = Sparrow.USGS.createElement('select','treatment-tab_src_adj');
        src_adj.id = 'treatment-tab_src_adj_' + i;
        src_adj.size = 1;
        for (var j = 0; j <= 8; j++) {
            var opt = new Option(j * 0.25, j * 0.25);
            src_adj.options[j] = opt;
        }
        src_adj.value = 1;

        var src_cust = document.createElement('div');
        src_cust.className = 'col_30';
        //src_cust.innerHTML = '<a href="#" onclick="return false">customize...</a>';
        var src_cust_a = document.createElement('a');
        src_cust_a.href = "";
        src_cust_a.index = i;
        src_cust_a.innerHTML = 'enter custom multiplier...';
        src_cust_a.onclick = function() {
            var idx = this.index;
            var src_adj_sel = document.getElementById('treatment-tab_src_adj_' + idx);
            var reply = parseFloat(prompt("Enter new value for multiplier:",""));
            if (!isNaN(reply)) {
                src_adj_sel.value = reply;
                if (src_adj_sel.value != reply) { //number doesn't exist in list already, add it
                    src_adj_sel.options[9] = new Option(reply,reply);
                    src_adj_sel.value = reply;
                }
            }
            return false;
        };

        src_cust.appendChild(src_cust_a);
        treatmentTab.appendChild(data_row);
        data_row.appendChild(src_name);
        data_row.appendChild(src_constituent);
        data_row.appendChild(src_adj_div);
        src_adj_div.appendChild(src_adj);
        data_row.appendChild(src_cust);
    }

    mapOptionsTab.filterSourceCombo();
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

		SvgOverlay.removeAllOverlays();
		var geom = reachResponse["sparrow-id-response"]["results"]["result"][0]["identification"]["ReachGeometry"]["basin"];
		SvgOverlay.renderGeometry(geom, animateOptions, 'black', 'white');

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
				var msg = '<br/>Would you like to automatically adjust the bins to include the entire set of results on the map in \'Equal Count\' bins?';

				if(allValuesInABin=='false') msg = '- There are results not included in your custom bins that will not be displayed on the map.<br/>' + msg;
				if(allValuesInASingleBin=='false') msg = '- All results fall into a single bin.<br/>' + msg;

				Ext.Msg.confirm(
					'Potential Binning Issues',
					msg,
					function(yes){
						if(yes!='yes') {
							
							options.callback = callback;
							generateBins(options);
						}
					}
				);
			}
			if (callback) callback.call(this, options);
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
	
	var bucketCount = Sparrow.SESSION.getBinCount();
	var bucketType = Sparrow.SESSION.getBinType();
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
				options.dataLayer.datalayerWmsUrl = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'EndpointUrl');
				options.dataLayer.flowlineLayerName = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'FlowLayerName');
				options.dataLayer.catchLayerName = Sparrow.utils.getFirstXmlElementValue(r.responseXML, 'CatchLayerName');
				
				if (callback) callback.call(this, options);
			} else {
				onLocalErr.call(this, r, options);
			}
		},
		failure: onLocalErr
	});
};

function saveNewState(options) {
	
	var callback;
	if (options.callback) {
		callback = options.callback;
		delete options.callback;	//remove so later chained calls happen as normal
	} else if (options.callbackChain) {
		callback = options.callbackChain.shift();
	}		
					
	Sparrow.SESSION.setBinData(options.binData);
	
	//TODO:  This could be handled as a binChange event
	var comparisonBucketLbl = Ext.getCmp('map-options-tab').bucketLabel;
	comparisonBucketLbl.setText(Sparrow.SESSION.getBinCount() + ' ' + Sparrow.SESSION.getBinTypeName() + ' Bins');
	
	Sparrow.SESSION.setDataLayerInfo(
			options.dataLayer.datalayerWmsUrl, 
			options.dataLayer.flowlineLayerName, 
			options.dataLayer.catchLayerName);
	
	if (callback) callback.call(this, options);
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
		
	if (Sparrow.SESSION.isBinAuto()) {
		options.callbackChain = [generateBins, registerDataLayer, saveNewState, addDataLayer];
	} else {
		options.callbackChain = [confirmOrAdjustCurrentContextBins, registerDataLayer, saveNewState, addDataLayer];
	}
	
	getContextIdAsync(options);	//Adds the dataLayer if successful
}

/**
 * When adding the data layer, we always assume that the lastValidContext is
 * up to date and will be what we map from.  This method then promotes the last
 * valid state to the last mapped state.
 */
function addDataLayer() {

	//Internal ID used for the map layer
	var mappedValueLayerID = Sparrow.config.LayerIds.mainDataLayerId;

    //get parameters to create base url for sparrow data layer
    var what_to_map = Sparrow.SESSION.PermanentMapState["what_to_map"];

    var bins = Sparrow.SESSION.getBinData()["functionalBins"];
    var colors = Sparrow.SESSION.getBinData()["binColors"];

	var binParams = 'binLowList=' + Ext.pluck(bins, 'low').join();
	binParams += '&binHighList=' + Ext.pluck(bins, 'high').join();
	binParams += '&binColorList=' + colors.join();
	
	var dataLayerWmsUrl = Sparrow.SESSION.getDataLayerWmsUrl();	//ends with /wms
	var mapServerUrl = dataLayerWmsUrl.substring(0, dataLayerWmsUrl.length - 4);
	var layerName = "";
	
	if (what_to_map == "reach") {
		layerName = Sparrow.SESSION.getFlowlineDataLayerName();
	} else {
		layerName = Sparrow.SESSION.getCatchDataLayerName();
	}

    map1.layerManager.unloadMapLayer(mappedValueLayerID);
    map1.appendLayer(
    	new JMap.web.mapLayer.WMSLayer({
    		zDepth: 60000,
    		id: mappedValueLayerID,
    		scaleMin: 0,
    		scaleMax: 100,
    		baseUrl: dataLayerWmsUrl + "?",
    		legendUrl: 'getLegend?' + binParams,
    		title: layerName,
    		name: layerName,
			layersUrlParam: layerName,
    		isHiddenFromUser: true,
    		description: 'Sparrow Coverage',
    		opacity: Sparrow.SESSION.getDataLayerOpacity(),
			sld: encodeURIComponent(mapServerUrl + "/sld_endpoint?" + binParams)
    	})
    );

	Sparrow.SESSION.markMappedState(Sparrow.SESSION.getLastValidContextId(), Sparrow.SESSION.getLastValidSeriesData());
	
	// Update the legend (uses infor from the marked map state above)
    var legendEl = Ext.get('legend');
    Ext.DomHelper.overwrite(legendEl, '');
    Sparrow.ui.renderLegend();
	
	
    Sparrow.SESSION.fireContextEvent('map-updated-and-synced');

}






