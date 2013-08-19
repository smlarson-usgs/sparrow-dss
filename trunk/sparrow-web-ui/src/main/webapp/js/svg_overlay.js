var appbase = location.host;
var contextroot = location.pathname.split('/')[1];
appbase += '/' + contextroot;



var SvgOverlay = new (function(){
	var hucRequest;
	var upstreamRequest;
	var self = this;


	/**
	 * 0: Show a single goem, recentering the map and clearing other layers
	 * 1: Show a part of a set:  don't recenter or clear other layers
	 * 2: Show a single geom, but don't recenter.  Do clear other layers
	 */
	this.standardAnimateOptions = [
		{showPleaseWait: true, zoom: true, reCenterMap: true, removeOtherLayers: true},
		{showPleaseWait: true, zoom: false, reCenterMap: false, removeOtherLayers: false},
		{showPleaseWait: true, zoom: false, reCenterMap: false, removeOtherLayers: true},
		{showPleaseWait: true, zoom: true, reCenterMap: true, removeOtherLayers: true},
		{showPleaseWait: true, zoom: true, reCenterMap: true, removeOtherLayers: false}
		];
	
	/**
	 *
	 *
	 * detailLevel: 0: Minimal detail (convex hull) 1: reduced detail 2: full detail
	 */
	this.identifyHuc = function(hucCode, animOpts) {

		if (Ext.isNumber(animOpts)){
			// user gave a standard option set
			animOpts = SvgOverlay.standardAnimateOptions[animOpts];
		}

		if ( animOpts.showPleaseWait ) IDENTIFY_REACH_SPINNER.show();
		
			Ext.Ajax.abort(hucRequest);
			
		    hucRequest = Ext.Ajax.request({
		    	method: 'GET',
		    	url: 'huc',
		    	success: function(response,o) {
		    	  var srcJSON = response.responseText;
		    	  var wrap = Ext.util.JSON.decode(srcJSON);
		    	  wrap = wrap['ServiceResponseWrapper'];
		    		this.loadSpatialObject(wrap, animOpts, "simpleGeometry");
		    		
		    		hucRequest = null;
		    		if (animOpts.showPleaseWait)
		    			IDENTIFY_REACH_SPINNER.hide();
		    	},
		    	failure: function(r,o) {
		    		Ext.Msg.alert('Warning', 'Could not load the HUC geometry');
		    		if (animOpts.showPleaseWait)
		    			IDENTIFY_REACH_SPINNER.hide();
		    	},
		    	params: {
		    		huc: hucCode,
		    		mime_type: 'json'
		    	},
		    	scope: this
		    });

	};
	
	
	/*
	 * As a temp fix for the fact that our upstream tracing doesn't work
	 * for large basins, give the user a warning.
	 */
	this.identifyUpstream = function(reachId, modelId, animOpts) {
		
		Ext.Msg.confirm('Warning', 
				'Reaches upstream of the selected reach will be highlighted, ' +
				'however, this only works for small (< 100 reaches) upstream basins. ' +
				'Larger basins may timeout. <br/>' +
				'Would you like to continue with the upstream highlighting?',
				function(v) {
					if(v=='yes') {
						this.doIdentifyUpstream(reachId, modelId, animOpts);
					}
					else {
						return false;
					}
				}, this);
		
	};
	
	this.doIdentifyUpstream = function(reachId, modelId, animOpts) {

		if (Ext.isNumber(animOpts)){
			// user gave a standard option set
			animOpts = SvgOverlay.standardAnimateOptions[animOpts];
		}

		if ( animOpts.showPleaseWait ) IDENTIFY_REACH_SPINNER.show();
		
			Ext.Ajax.abort(upstreamRequest);
			
		    request = Ext.Ajax.request({
		    	method: 'GET',
		    	url: 'reachwatershed',
		    	success: function(response,o) {
		    	  var srcJSON = response.responseText;
		    	  var wrap = Ext.util.JSON.decode(srcJSON);
		    	  wrap = wrap['ServiceResponseWrapper'];
		    		this.loadSpatialObject(wrap, animOpts, "basin");
		    		
		    		upstreamRequest = null;
		    		
		    		if (animOpts.showPleaseWait)
		    			IDENTIFY_REACH_SPINNER.hide();
		    	},
		    	failure: function(r,o) {
		    		Ext.Msg.alert('Warning', 'Could not load the upstream geometry');
		    		if (animOpts.showPleaseWait)
		    			IDENTIFY_REACH_SPINNER.hide();
		    	},
		    	params: {
		    		reach_id: reachId,
		    		model_id : modelId,
		    		mime_type: 'json'
		    	},
		    	scope: this
		    });


	};

	/**
	 * Callback function for the Reach Identify service.  This function receives the
	 * callback and decodes the response.  It then delegates to the render function.
	 */
	this.loadSpatialObject =  function(wraper, animateOptions, geomElementName) {

	    if (wraper.status == 'OK') {
	    	var obj = wraper.entityList.entity;	//Only expecting a single object here
	    	var geom = obj.geometry;

	    		self.renderGeometry(obj[geomElementName], animateOptions, 'blue', 'white');
//				if (detailLevel == 2) {
//					self.renderGeometry(obj.geometry, animateOptions, 'blue', 'white');
//				} else if (detailLevel == 0) {
//					self.renderGeometry(obj.convexGeometry, animateOptions, 'blue', 'white');
//				} else {
//					self.renderGeometry(obj.simpleGeometry, animateOptions, 'blue', 'white');
//				}
				
	    } else {
			var message = wraper.errorMessage;
			IDENTIFY_REACH_SPINNER.hide();
			Ext.Msg.alert('Unable to load the requested geometry', message);
	    }

	};
	

	/**
	 * Renders the reach information contained within reachResponse to the user
	 * interface.  Calculates and draws a bounding box around the reach, places a
	 * marker at the point clicked, and opens the Reach Identify window.
	 */
	this.renderGeometry = function(geometry, animateOptions, strokeColor, fillColor) {

		// Get the bounding box of the identified reach
		var xmin = parseFloat(geometry.minLong);
		var ymin = parseFloat(geometry.minLat);
		var xmax = parseFloat(geometry.maxLong);
		var ymax = parseFloat(geometry.maxLat);


		var segments = geometry.Segments; 
		
		if(segments.constructor != Array) {
			//the code wants an array, but when only a single segment exists,
			//the service serializes the json without an array wrapper
			segments = [segments["Segment"]];
		}

		if (animateOptions.removeOtherLayers && self.hasOverlay()) {
			self.removeAllOverlays();
		}

		if (animateOptions.reCenterMap) { // recenter window to reach, zooming if necessary

			var xExtent = xmax - xmin;
			var yExtent = ymax - ymin;

			// This temporarily centers the catchment squarely in upper right of the map
			// and sets the zoom level
			if (animateOptions.zoom) map1.fitToBBox(xmin - xExtent, ymin - yExtent, xmax, ymax);


				// Calculate how far south and west we should center the map from the reach
				var vBBox = map1.getViewportBoundingBox();
				var yDelta = (vBBox.ymax - vBBox.ymin) / 4;
				var xDelta = (vBBox.xmax - vBBox.xmin) / 4;

				// Calculate the center point of the reach's bounding box
				var y = (ymax + ymin) / 2;
				var x = (xmax + xmin) / 2;

				// Animate the map to the point south and west of the reach's box
				map1.moveTo(y - yDelta, x - xDelta);

		}

		IDENTIFY_REACH_SPINNER.hide();

		self.renderSegments(segments, strokeColor, fillColor);
		self.internalNotifyOverlayAdded(false, true);
	};

	this.renderSegments = function(segments, strokeColor, fillColor) {
		for (var i = 0; i < segments.length; i++) {
			self.renderPointArray(segments[i].coords, strokeColor, fillColor);
		}
	};


	this.renderPointArray = function(coordinateString, strokeColor, fillColor) {

		var reachPoints = [];

		//build the reachPoints array
		{
			
			//Depending on how the geom was serialized, the coordString
			//may actually be an array which contains the string.
			//(I-lin's framework vs XStream).
			if(coordinateString.constructor == Array) {
				coordinateString = coordinateString[0];
			}
			
			
			var reachPointPairs = coordinateString.trim().split(',');
			for (var i = 0; i < reachPointPairs.length; i+=2) {

				//longitude is the first of each set (i.e. its 'x')
				var lat = parseFloat(reachPointPairs[i + 1]);
				var lon = parseFloat(reachPointPairs[i]);

				reachPoints.push(lat);
				reachPoints.push(lon);
			}
		}

		if (reachPoints.length > 0){

				map1.drawShape(new JMap.svg.Polygon({
					points: reachPoints,
					properties: {
						stroke: strokeColor,
						'stroke-width': '4px',
						fill: fillColor,
						'fill-opacity': 0.3,
						'stroke-linejoin': 'round'
					},
					events: {
						//onclick: function(e) {alert('identify handler'); return false;}
					}
				}));

		}

	};

	/**
	 * Returns true if there is currently a SVG Overlay
	 */
	this.hasOverlay = function() {
		return (map1._SVGManager.shapes.length > 0);
	}

	/**
	 * Public method that can be called to 'remind' the user of a newly added
	 * svn overlay and highlights how the user can turn it off.
	 *
	 * typical use case:
	 * * A popup allows the user to turn on an SVN Overlay
	 * * The user closes the popup, but the overlay remains on
	 * * On close, the popup can call this method to highlight the 'turn off' button.
	 */
	this.remindUserOverlayAdded = function() {
		self.internalNotifyOverlayAdded(true, false);
	}

	/**
	 * Public method to remove all SVG shapes.
	 */
	this.removeAllOverlays = function() {
		map1._SVGManager.removeAllShapes();
		self.internalNotifyOverlayRemoved();
	}

	/**
	 * Called internally after a SVG layer is added
	 *
	 * highlightControl If true, the control to turn off layers will be highlighted.
	 * highlightControlIfNew If true, the control to turn off layers will be
	 *	highlighted only if the overlay is the first one, meaning there were no
	 *	overlays before.  This is determined by checking the number of overlays:
	 *	if it is one, the lsyer is considered 'new'.
	 *
	 * If both options are true, the control is highlighted.
	 */
	this.internalNotifyOverlayAdded = function(highlightControl, highlightControlIfNew) {

		var btn = mapToolButtons.getComponent('mapToolButtonsHideOverlay');

		if (btn.hidden) {
			btn.show();
			mapToolButtons.doLayout();
		}

		if (highlightControl || (highlightControlIfNew && map1._SVGManager.shapes.length < 2)) {
			btn.getEl().fadeOut().fadeIn().fadeOut().fadeIn();
		}
	}


	/**
	 * Called internally after a SVG layer is added.
	 */
	this.internalNotifyOverlayRemoved = function() {

		var btn = mapToolButtons.getComponent('mapToolButtonsHideOverlay');

		if (! btn.hidden) {
			btn.hide();
			mapToolButtons.doLayout();
		}
		
	}

})();

