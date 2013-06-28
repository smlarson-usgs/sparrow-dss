/**
 * @author johnhollister
 */


/**
 * Map creates the map you see on the web page.
 * 
 * @param {Object} params	JSON object defining properties of this map:  
 * 		<ul>
 * 			<li>containerEl: id of the element the map is rendered to</li>
 * 			<li>centerLat: initial center latitude of map</li>
 * 			<li>centerLon: initial center longitude of map</li>
 * 			<li>zoomLevel: initial zoom level of map</li>
 * 			<li>maxZoom: highest zoom level a map can have (defaults to 18)</li>
 * 			<li>minZoom: lowest zoom level a map can have (defaults to 0)</li>
 * 			<li>cacheTiles: javascript clientside cache of map tiles (defaults to true for all browsers but IE)</li>
 * 			<li>svg: true to enable svg rendering on map (defaults to false)</li>
 * 			<li>listeners:
 * 				<ul>
 * 					<li>onDrop: callback when user releases a dragging map</li> 
 * 					<li>onZoomIn: callback when map zooms in</li> 
 * 					<li>onZoomOut: callback when map zooms out</li> 
 * 					<li>onDblClick: callback when map is double clicked</li> 
 * 					<li>onLayerAppend: callback when layer is added to map</li> 
 *					<li>onLayerRemove: callback when layer is removed from map</li> 
 *					<li>onResize: callback when map is resized</li> 
 *				</ul>
 * 		</ul>
 * @constructor
 */
JMap.web.Map = function(params) {

	this.layerManager = new JMap.web.LayerManager(this);
	this.containerEl = document.getElementById(params.containerEl);
	this.zoom = 0;
	this.maxZoom = (params.maxZoom||18);
	this.minZoom = (params.minZoom||0);
	this.centerLat = (params.centerLat||0);
	this.centerLon = (params.centerLon||0);
	this.tileSize = (params.tileSize||256);
	this.numTilesX = (params.numTilesX||6);
	this.numTilesY = (params.numTilesY||5);
	this.mapWidthPx = params.mapWidthPx;
	this.mapHeightPx = params.mapHeightPx;
	this.overviewMap = params.overviewMap;
	this.cacheTiles = (params.cacheTiles||false);
	this.epsg = params.epsg;
	this.border = params.border;
	if (params.validBBox) {
		this.noLoop = true;
	}
	this.tempValidBBox = params.validBBox;

	this.svg = params.svg;
	
	//auto center if no center was specified with a validbbox
	if (params.validBBox && !params.centerLat) {
		this.centerLat = ((params.validBBox.ymax + params.validBBox.ymin) / 2);
	}
	if (params.validBBox && !params.centerLon) {
		this.centerLon = ((params.validBBox.xmax + params.validBBox.xmin) / 2);
	}
	
	this.centerLatLon = {lat:this.centerLat, lon:this.centerLon};
	
	//for overview/linked maps
	this.parentMap = params.parentMap;
	
	var tZoom = (params.zoomLevel||0);
	if (tZoom < this.minZoom) tZoom = this.minZoom;

	//action handlers
	if (params.listeners) {
		//this.onDrag = params.listeners.onDrag;
		this.onDrop = params.listeners.onDrop;
		this.onZoomIn = params.listeners.onZoomIn;
		this.onZoomOut = params.listeners.onZoomOut;
		this.onDblClick = params.listeners.onDblClick;
		this.onLayerAppend = params.listeners.onLayerAppend;
		this.onLayerRemove = params.listeners.onLayerRemove;
		this.onResize = params.listeners.onResize;
	}

	//map's starting coordinate offset
	this.x = 0;
	this.xNoLoop = 0;
	this.y = 0;

	//flags to prevent the map from drawing
	this.isZooming = false;
	this.dontDraw = false;
	this.isJumping = false;
	this.isMoving = false;

	//place holder for a blank image so i don't have to create one all the time.
	this.blankImage = document.createElement('img');
	this.blankImage.src = 'scrollable_map/images/blank.gif';
	
	//initial map scaling
	if (!params.projection) {
		params.projection = new JMap.projection.Mercator(this);
	}
	this.projection = new params.projection.constructor(this);

	//map movement animation
	this.animator = false;
	this.pathIndex = 0;
	this.path = [];
	
	this.snapBackTimer = null;

	this._createMapHTML();
	
	
	//tile cacher/loader
	if (this.parentMap) {
		//share a tile cache
		this.tileManager = this.parentMap.tileManager;
	} else {
		var _this = this;
		this.tileManager = new JMap.web.TileManager({
			map: _this,
			cachingOn: _this.cacheTiles,
			onStartLoadingImages: function() { 
				if (_this.tilesLoadingImage) {
					_this.tilesLoadingImage.style.display = 'block';
				}
			},
			onEndLoadingImages: function() { 
				if (_this.tilesLoadingImage) {
					_this.tilesLoadingImage.style.display = 'none';
				}
			}
		});
	}

	//heads up display
	this._HUDManager = new JMap.hud.HUDManager(this, params.HUD);
	
	//features of interest
	this.FOIManager = new JMap.foi.FOIManager(this);
	
	
	if (!params.validBBox) {
		this.moveTo(this.centerLat, this.centerLon);
		this.zoomIn(tZoom);
	} else {

		//get minimum zoom
		this.fitToBBox(params.validBBox.xmin, params.validBBox.ymin, params.validBBox.xmax, params.validBBox.ymax);
		this.minZoom = this.zoom;	
		this.maxZoom += this.zoom;
		if (this.maxZoom > 18) {
			this.maxZoom = 18;
		}
		
		this._HUDManager.redrawZoomSlider();
		
		//convert valid bbox to pixels for compatibility across projections
		this.validBBox = params.validBBox;
		this.validXMinPx = Math.floor(this.projection.getXFromLon(this.validBBox.xmin));
		this.validXMaxPx = Math.floor(this.projection.getXFromLon(this.validBBox.xmax));
		this.validYMinPx = Math.floor(this.projection.getYFromLat(this.validBBox.ymin));
		this.validYMaxPx = Math.floor(this.projection.getYFromLat(this.validBBox.ymax));	
		
	}	
	if (this.svg) {
		this._SVGManager = new JMap.svg.SVGManager(this);
		this._SVGManager.update();
	}
	
	
	this.centerLatLon = {lat:this.centerLat, lon:this.centerLon};

	
	//load initially defined layers-
	if (params.layers) {
		for (var i = 0; i < params.layers.length; i++) {
			this.appendLayer(params.layers[i]);
		}
	}
	if (params.layersFile) {
		this.loadMapLayerServicesFile(params.layersFile);
	}	
}



JMap.web.Map.prototype._createMapHTML = function() {
	var _this = this;

	//map layer container
	this.mapLayerContainer = document.createElement('div');
	this.mapLayerContainer.className = 'map-layer-container';
	this.mapLayerContainer.style.width = (this.numTilesX * this.tileSize) + 'px';
	this.mapLayerContainer.style.height = (this.numTilesY * this.tileSize) + 'px';
	this.mapLayerContainer.style.top = -this.tileSize + 'px';
	this.mapLayerContainer.style.left = '0px';

	//mapviewport
	this.mapViewport = document.createElement('div');
	this.mapViewport.className = 'map-viewport';
	this.mapViewport.style.width = (this.mapWidthPx || ((this.numTilesX - 1) * this.tileSize)) + 'px';
	this.mapViewport.style.height = (this.mapHeightPx || ((this.numTilesY - 1) * this.tileSize)) + 'px';
	if(!this.border) this.mapViewport.style.borderWidth = '0px';

	//set parent containing div to size of visible map
	this.containerEl.style.width = this.mapViewport.style.width;
	this.containerEl.style.height = this.mapViewport.style.height;

	//map pane
	this.pane = document.createElement('div');
	this.pane.className = 'map-pane cursor-hand-open';
	this.pane.style.left = '0px';
	this.pane.style.top = '0px';
	this.pane.style.width = this.mapViewport.style.width;
	this.pane.style.height = this.mapViewport.style.height;

	this.pane.onmousedown = function(event) { return _this._performAction(event); };
	this.pane.ondblclick = function(event) { return _this.animateMove(event); };
	this.pane.oncontextmenu = function() { return false; };
	this.pane.onmousemove = function(event) { _this._HUDManager.updateOnMouseMove(event); return false; };
	this.pane.onmouseout = function(event) { _this._HUDManager.updateOnMouseOut(event); return false; };
	
	//don't want to be able to zoom the overview map
	/*
	if (!this.parentMap) {
		this.pane.onmousewheel = function(event) { _this._scroll(event); return false; };
		this.pane.addEventListener('DOMMouseScroll', function(event) { _this._scroll(event); }, false);
	}
	*/
	

	
	//attach to html
	this.containerEl.appendChild(this.mapViewport);
	this.mapViewport.appendChild(this.mapLayerContainer);

	this.viewportWidth = parseFloat(this.mapViewport.style.width,10);
	this.viewportHeight = parseFloat(this.mapViewport.style.height,10);  

	//tiles loading img
	if (!this.parentMap) { //dont want on overview map
		this.tilesLoadingImage = document.createElement('img');
		this.tilesLoadingImage.className = 'tiles-loading-image';
		this.tilesLoadingImage.src = 'scrollable_map/images/rotating_arrow.gif';
		this.tilesLoadingImage.style.display = 'none';
		this.mapViewport.appendChild(this.tilesLoadingImage);
	}

	this.mapViewport.appendChild(this.pane);

}


/**
 * Resize the map viewport.
 * 
 * @param {int} viewportWidthPx	width in pixels of the new size of the map viewport.
 * @param {int} viewportHeightPx	height in pixels of the new size of the map viewport.
 */
JMap.web.Map.prototype.resize = function(viewportWidthPx, viewportHeightPx) {

	this.pane.style.width = viewportWidthPx + 'px';
	this.pane.style.height = viewportHeightPx + 'px';

	this.mapViewport.style.width = viewportWidthPx + 'px';
	this.mapViewport.style.height = viewportHeightPx + 'px';  

	this.containerEl.style.width = this.mapViewport.style.width;
	this.containerEl.style.height = this.mapViewport.style.height;

	this.viewportWidth = viewportWidthPx;
	this.viewportHeight = viewportHeightPx;  

	//recenter the map after viewport is resized
	this.moveTo(this.centerLatLon.lat,this.centerLatLon.lon)
	
	this._testSnapBack();
	
	if (this._HUDManager) this._HUDManager.update();
	if (this._SVGManager) this._SVGManager.update();
	
	if (this.onResize) this.onResize();
}



//----------------------------------------
// LAYER MANAGER
//----------------------------------------
/**
 * Clears the map by setting all the map tiles' image source attributes to a blank gif. The HTML
 * remains on the page though.  This does not deconstruct the map.
 */
JMap.web.Map.prototype.clear = function() {
	this.layerManager.clearLayers();
}



/**
 * Refreshes the map by resetting all the map tiles' source attributes to a map service URL.
 */
JMap.web.Map.prototype.draw = function() {
	this.tileManager.cancelAllLoads();
	this.layerManager.drawLayers();
}




/**
 * turn a map layer on
 * 
 * @param {var} layer layer or layer id to add to map
 */
JMap.web.Map.prototype.appendLayer = function(layer) {
	
	var layerId = layer;	//default to treat param like a layer id
	if (typeof layer == 'object') {
		if (!this.getMapLayer(layer.id)) {
			layer.map = this;
			layer._createLayerHTML();
			this.layerManager.loadMapLayer(layer);
		}
		layerId = layer.id;
	}
	this.layerManager.appendLayerToMap(layerId);
	if (this.onLayerAppend) {
		this.onLayerAppend(layerId);
	}
}


/**
 * Remove a layer from the map.
 * 
 * @param {var} layer or layerId	id of the layer to remove from the map.
 */
JMap.web.Map.prototype.removeLayer = function(layer) {
	
	var layerId = layer;	//default to treat param like a layer id

	if (typeof layer == 'object') {
		layerId = layer.id;
	}
	
	this.layerManager.removeLayerFromMap(layerId);
	
	if (this.onLayerRemove) {
		this.onLayerRemove(layerId);
	}
}



/**
 * Return a layer loaded in to the map.
 * 
 * @param {var} layerId	id of the layer.
 */
JMap.web.Map.prototype.getMapLayer = function(layerId) {
	return this.layerManager.getMapLayer(layerId);
}



/**
 * Load a file containing multiple layer definitions.
 * 
 * @param {JSON} params	JSON containing url, name, isOnByDefault, isHiddenFromUser
 */
JMap.web.Map.prototype.loadMapLayerServicesFile = function(params) {
	this.layerManager.loadMapLayerServicesFile(params);
}







/**
 * draw an SVG/VML object to the map
 * 
 * @param {SVG/VML Object} the shape/line to draw
 */
JMap.web.Map.prototype.drawShape = function(shape) {
	map1._SVGManager.addShape(shape);
}


/**
 * removes an SVG/VML object from the map
 * 
 * @param {SVG/VML Object} the shape/line to remove from the map
 */
JMap.web.Map.prototype.removeShape = function(shape) {
	map1._SVGManager.removeShape(shape);
}



//-----------------------------------------------------------------------------
//MAP EVENTS
//-----------------------------------------------------------------------------
JMap.web.Map.prototype.setMouseAction = function(func) {
	this.toolAction = func;
	if (func) {
		this.pane.className = 'map-pane ' + func.cursor;
	} else {
		this.pane.className = 'map-pane cursor-hand-open';
	}
}


JMap.web.Map.prototype._performAction = function(event) {
	event = event||window.event;

	(!this.toolAction)?this._startDrag(event):this.toolAction(event, this);
	if (this.onaction) {
		this.onaction(event);
	}
	return false;
}

JMap.web.Map.prototype._startDrag = function(event) {	
	this.pane.className = 'map-pane cursor-hand-closed';
	
	var _this = this;
	event = event || window.event;  
	
	this.startDragAt = event.clientX + ':' + event.clientY;

	this.lastMouseX = event.clientX;
	this.lastMouseY = event.clientY;  

	document.onmousemove = function(event) { return _this._drag(event); };
	document.onmouseup = function(event) { return _this._endDrag(event); };
	return false;
}


JMap.web.Map.prototype._drag = function(event) {
	var _this = this;

	//don't allow the map to redraw while the user is dragging it
	if (this.snapBackTimer) clearTimeout(this.snapBackTimer);
	this.snapBackTimer = null;

	event = event || window.event;
	//if (navigator.appVersion.indexOf("MSIE")!=-1) unselect();

	var currentMouseX = event.clientX;
	var currentMouseY = event.clientY;

	this.moveBy(currentMouseX - this.lastMouseX, currentMouseY - this.lastMouseY);

	this.lastMouseX = currentMouseX;
	this.lastMouseY = currentMouseY;

	//if the user pauses during a drag, take that time to redraw the map to fill
	//the viewport
	//this._testSnapBack();

	if (!this.animator)
		this.snapBackTimer = setTimeout(function() {_this._testSnapBack() }, 50);  

	return false;
}


JMap.web.Map.prototype._endDrag = function(event) {
	event = event||window.event;
	
	//release the event handlers
	document.onmousemove = null;
	document.onmouseup = null;
		
	if (this.startDragAt != event.clientX + ':' + event.clientY) {
				
		//end the snapback check and redraw map to fill the viewport
		if (this.snapBackTimer) {
			this._testSnapBack();
			clearTimeout(this.snapBackTimer);
		}
		this.snapBackTimer = false; 
		
		this._HUDManager.updateOnMapMoved(event);
		if (this._SVGManager) this._SVGManager.update();
		
		if (this.onDrop) {
			this.onDrop();
		}
	}
	
	this.pane.className = 'map-pane cursor-hand-open';

	return false;
}


/**
 * zoom in/out on mouse scroll
 */
JMap.web.Map.prototype._scroll = function(event) {
	event = event||window.event;

	var delta = 0;

	if (event.wheelDelta) { /* IE/Opera. */
		delta = event.wheelDelta/120;
		/** In Opera 9, delta differs in sign as compared to IE.
		 */
		if (window.opera) delta = -delta;
	} else if (event.detail) { /** Mozilla case. */
		/** In Mozilla, sign of delta is different than in IE.
		 * Also, delta is multiple of 3.
		 */
		delta = -event.detail/3;
	}
	/** If delta is nonzero, handle it.
	 * Basically, delta is now positive if wheel was scrolled up,
	 * and negative, if wheel was scrolled down.
	 */
	if (delta) {
		if (delta > 0) {
			this.zoomIn(1);
		} else if (delta < 0) {
			this.zoomOut(1);
		}

	}
	/** Prevent default actions caused by mouse wheel.
	 * That might be ugly, but we handle scrolls somehow
	 * anyway, so don't bother here..
	 */
	if (event.preventDefault)
		event.preventDefault();
	
	event.returnValue = false;

	return false;
}









//-----------------------------------------------------------------------------
//MAP MOVEMENTS
//-----------------------------------------------------------------------------

/**
 * zooms the map in n times as specified in the method signature.
 * 
 * @param {int} n	(optional) number of times to zoom in, defaults to 1.
 */
JMap.web.Map.prototype.zoomIn = function(n) {		
	(n==undefined)?n=1:n=n;
	
	var center = this.centerLatLon;

//	clear the map
	this.clear();

//	set this flag so no unnecessary tiles are rendered during the zooming process
	this.isZooming = true;

//	zooms the map in n times- once per iteration
	for (var i = 0; i < n; i++) {
		if (this.zoom < this.maxZoom) {
			this.zoom++;
			this.x = (this.x * 2) + Math.floor(this.numTilesX / 2);
			this.xNoLoop = this.x;
			this.y = (this.y * 2) + Math.floor(this.numTilesY / 2);  
			if (!this.noLoop) {
				this.x = this.x % this.getTilesPerMapX();
				if (this.x < 0) { this.x = (this.getTilesPerMapX() - 1); }   
			}
			
			if (this.validBBox) {
				this.validXMinPx = Math.floor(this.projection.getXFromLon(this.validBBox.xmin));
				this.validXMaxPx = Math.floor(this.projection.getXFromLon(this.validBBox.xmax));
				this.validYMinPx = Math.floor(this.projection.getYFromLat(this.validBBox.ymin));
				this.validYMaxPx = Math.floor(this.projection.getYFromLat(this.validBBox.ymax));	
			}
					
			this.layerManager.syncWithMapCoordinates();
			this.moveTo(center.lat, center.lon);
		} else {
			break;
		}
	} //i

//	it's okay to draw tiles again, zooming is done.
	this.isZooming = false;

//	redraw the map
	this.draw();

//	onZoomIn callback
	if (this.onZoomIn) this.onZoomIn(this.zoom);
	
	this._HUDManager.updateOnZoom();
	if (this._SVGManager) this._SVGManager.updateOnZoom();
	this.FOIManager.updateOnZoom();
	this.layerManager.updateActiveMapLayers();
}

/**
 * zooms the map out n times as specified in the method signature.
 * 
 * @param {int} n	(optional) number of times to zoom out, defaults to 1.
 */
JMap.web.Map.prototype.zoomOut = function(n) {
	(n==undefined)?n=1:n=n;
	
	var center = this.centerLatLon;

//	clear the map
	this.clear();

//	set this flag so no unnecessary tiles are rendered during zooming process.
	this.isZooming = true;

//	zoom out n times- once per iteration
	for (var i = 0; i < n; i++) {
		if (this.zoom >= this.minZoom + 1) {
			this.zoom--;
			this.x = Math.floor((this.x - Math.floor(this.numTilesX / 2)) / 2);
			this.xNoLoop = this.x;      
			this.y = Math.floor((this.y - Math.floor(this.numTilesY / 2)) / 2);
			if (!this.noLoop) {
				this.x = (this.x) % this.getTilesPerMapX();
				if (this.x < 0) { this.x = this.getTilesPerMapX() - 1; }
			}
			
			if (this.validBBox) {
				this.validXMinPx = Math.floor(this.projection.getXFromLon(this.validBBox.xmin));
				this.validXMaxPx = Math.floor(this.projection.getXFromLon(this.validBBox.xmax));
				this.validYMinPx = Math.floor(this.projection.getYFromLat(this.validBBox.ymin));
				this.validYMaxPx = Math.floor(this.projection.getYFromLat(this.validBBox.ymax));	
			}
			
			this.layerManager.syncWithMapCoordinates();
			this.moveTo(center.lat, center.lon);  
		} else {
			break;
		}
	}  //i

//	it's okay to draw tiles again, zooming is done
	this.isZooming = false;

//	redraw the map
	this.draw();

//	onZoomOut callback
	if (this.onZoomOut) this.onZoomOut(this.zoom);
	
	this._HUDManager.updateOnZoom();
	if (this._SVGManager) this._SVGManager.updateOnZoom();
	this.FOIManager.updateOnZoom();
	this.layerManager.updateActiveMapLayers();
}

/**
 * jumps view to a lat/long point and a zoom level.
 *
 * @param lon  the longitudinal coordinate to jump to
 * @param lat  the latitudinal coordinate to jump to
 * @param zoom (optional) the zoom level to zoom to
 */
JMap.web.Map.prototype.jumpTo = function(lat, lon, zoom) {

//	set these flags to keep map from rendering tiles when performing map action
	this.isJumping = true;

//	allows you to not pass the third argument in this method (zoom), default to 
//	current zoom level
	var toZoom = this.zoom;
	if (zoom != undefined) toZoom = zoom;
	this.zoomOut(this.zoom);
	this.moveTo(lat, lon);
	this.centerLatLon = {lat: lat, lon: lon};
	this.zoomIn(toZoom);

//	it's okay to start drawing tiles again.
	this.isJumping = false;

//	draw tiles
	this.draw();
}

/**
 * moves view to a latitude/longitude point on the map
 *
 * @param {double} lon  longitude
 * @param {double} lat  latitude
 */
JMap.web.Map.prototype.moveTo = function(lat, lon) {

//	set flag to prevent rendering of unnecessary tiles during map action
	this.isMoving = true
	
//	get center lat lon of map view
	var center = this.getCenterLatLon(); 
	var centerPx = this.projection.getPixelFromLatLon(center.lat, center.lon);
	
	var moveToPx = this.projection.getPixelFromLatLon(lat, lon);
	
//	move by lat lon offset in pixels
	this.moveBy(centerPx.x - moveToPx.x, moveToPx.y - centerPx.y);

	this._testSnapBack();

//	it's okay to start drawing tiles again
	this.isMoving = false;  
	this.draw();
	
	this._HUDManager.updateOnMapMoved();
	if (this._SVGManager) this._SVGManager.update();
	//this.FOIManager.update();
}


/**
 * moves map view BY x and y pixels
 *
 * @param {int} dx amount of pixels to move the map view along the x (lon) axis
 * @param {int} dy amount of pixels to move the map view along the y (lat) axis
 */
JMap.web.Map.prototype.moveBy = function(dx, dy) {
	var mapLayerContainerTop = parseInt(this.mapLayerContainer.style.top,10);
	var mapLayerContainerLeft = parseInt(this.mapLayerContainer.style.left,10);
	
	if (this.validBBox) {
		var mapCoords = this.getMapCoordsInPixelSpace();

		if (this.viewportWidth < (this.validXMaxPx - this.validXMinPx)) {
			if (mapCoords.x - dx <= this.validXMinPx) {
				dx = (mapCoords.x - this.validXMinPx);
			} else if (mapCoords.x + this.viewportWidth - dx >= this.validXMaxPx) {
				dx = ((mapCoords.x + this.viewportWidth) - this.validXMaxPx);
			}
		} else {
			//move to center of bbox lons
			dx = (mapCoords.x + (this.viewportWidth / 2)) - ((this.validXMinPx + this.validXMaxPx) / 2);
		}
		

		if (this.viewportHeight < (this.validYMaxPx - this.validYMinPx)) {
			if (mapCoords.y + dy <= this.validYMinPx) {
				dy = (this.validYMinPx - mapCoords.y);
			} else if (mapCoords.y + this.viewportHeight + dy >= this.validYMaxPx) {
				dy = (this.validYMaxPx - (mapCoords.y + this.viewportHeight));
			}
		} else {
			//move to center of bbox lats
			dy =  ((this.validYMinPx + this.validYMaxPx) / 2) - (mapCoords.y + (this.viewportHeight / 2));
		}
	}
	
	if (this._SVGManager) this._SVGManager.moveBy(dx, dy);
	this.FOIManager.moveBy(dx, dy);
	
//	move the map in the html
	this.mapLayerContainer.style.left = (mapLayerContainerLeft + dx) + 'px';  
	this.mapLayerContainer.style.top = (mapLayerContainerTop + dy) + 'px';

	//center lat/lon will get off due to pixel translations to lat/lon when zooming
	if (!this.isZooming && !this.isJumping) {
		this.centerLatLon = this.getCenterLatLon();
	}
}



/**
 * move and zoom the map viewport to fit an arbitrary bounding box.  this method will not
 * MATCH the bounding box, but will calculate an allowable bounding box that 
 * most closely envelopes the bbox passed into the method
 *
 *@param {double} xmin  minimum x value in decimal degrees
 *@param {double} ymin  minimum y value in decimal degrees
 *@param {double} xmax  maximum x value in decimal degrees
 *@param {double} ymax  maximum y value in decimal degrees
 */
JMap.web.Map.prototype.fitToBBox = function(xmin, ymin, xmax, ymax) {
	
	this.isJumping = true;
	
	//1. zoom out to minimum zoom level
	this.zoomOut((this.zoom - this.minZoom));
	
	//2. move to center lat/lon of bbox
	var yminPx = this.projection.getYFromLat(ymin);
	var ymaxPx = this.projection.getYFromLat(ymax);
	var xminPx = this.projection.getXFromLon(xmin);
	var xmaxPx = this.projection.getXFromLon(xmax);
	
	var moveToLon = this.projection.getLonFromX(((xminPx + xmaxPx) / 2));
	//var moveToLat = this.projection.getLatFromY(((yminPx + ymaxPx) / 2));
	var moveToLat = this.projection.getLatFromY(((yminPx + ymaxPx) / 2));
	this.moveTo(moveToLat, moveToLon);
	this.centerLatLon = {lat: moveToLat, lon: moveToLon};

	//3. zoom in until one extent from the current map's bbox fits in the 
	//	constraining bbox, then zoom out
	var xExtent = xmax - xmin;
	var yExtent = ymax - ymin;
	while (this.zoom < this.maxZoom) {
		var mapBBox = this.getViewportBoundingBox();
		if (xExtent >= (mapBBox.xmax - mapBBox.xmin) || yExtent >= (mapBBox.ymax - mapBBox.ymin)) {
			this.zoomOut(1);
			break;
		}
		this.zoomIn(1);
	}
	
	this.isJumping = false;
	this.draw();
}


/**
 * on a map double click, move the map to the point clicked on, and animate it
 * 
 * @param {Object} event	Event object
 */
JMap.web.Map.prototype.animateMove = function(event) {
	event = event || window.event;

//	get pixel coords where user clicked on map pane
	var coords = JMap.util.getRelativeCoords(event, this.pane);

//	move map view to center on those coordinates
	this.animateMoveToPx(coords.x, coords.y);
}


/**
 * animate map motion and move by the distance defined in dx and dy pixels
 * 
 * @param {int} dx	number of pixels to move by in the horizontal direction
 * @param {int} dy	number of pixels to move by in the vertical direction
 */
JMap.web.Map.prototype.animateMoveByPx = function(dx, dy) {
	var startX = this.viewportWidth / 2;
	var startY = this.viewportHeight / 2;

	this.animateMoveToPx(startX + dx, startY + dy);
}


/**
 * animate the motion to the specified latitude and longitude
 *
 * @param {double} lat	latitude to animate move to.
 * @param {double} lon	longitude to animate move to.
 */
JMap.web.Map.prototype.animateMoveToLatLon = function(lat, lon) {
	var coords = this.getPixelFromLatLon(lat,lon);
	var px = this.transformPixelToScreenSpace(coords.x, coords.y);
	this.animateMoveToPx(px.x, px.y);
}


/**
 * animate the movement of the map view to center on a point (endX, endY) in pixel space
 *
 *@param {int} endX  x (lon) pixel location to center map on by animation.
 *@param {int} endY  y (lat) pixel location to center map on by animation.
 */
JMap.web.Map.prototype.animateMoveToPx = function(endX, endY) {

//	startX, startY will be center of image
	this.path = [];
	this.pathIndex = 0;
	clearInterval(this.animator);

	var startX = this.viewportWidth / 2;
	var startY = this.viewportHeight / 2;

	var dx = (startX - endX);
	var dy = (startY - endY);

	var dirVect = [];

//	collect all vectors along the path to animate into dirVect array
	if (dx != 0 || dy != 0) {
		if (Math.abs(dx) >= Math.abs(dy)) {
			//divide both by dx to normalize vector so one value (x or y) will equal 1
			dirVect.push(dx/Math.abs(dx));  //normal x
			dirVect.push(dy/Math.abs(dx));	//normal y
			while (Math.abs(dx) > 5) {
				dx /= 2;
				dx = Math.floor(dx);
				dy = Math.floor(Math.abs(dx) * (dirVect[1]));
				this.path.push([dx,dy]);
			}
			var ubound = Math.abs(dx);
			for (var i = 0; i < ubound; i++) {
				dx -= (dx / Math.abs(dx));
				dy = Math.floor(Math.abs(dx) * (dirVect[1]));
				this.path.push([dx,dy]);
			}

		} else if (Math.abs(dx) < Math.abs(dy)) {
			//divide by dy to normalize vector so one value (x or y) will equal 1
			dirVect.push(dx/Math.abs(dy));  //normal x
			dirVect.push(dy/Math.abs(dy));	//normal y
			while (Math.abs(dy) > 5) {
				dy /= 2;
				dy = Math.floor(dy);
				dx = Math.floor(Math.abs(dy) * (dirVect[0]));
				this.path.push([dx,dy]);
			}
			var ubound = Math.abs(dx);
			for (var i = 0; i < ubound; i++) {
				dy -= (dy / Math.abs(dy));
				dx = Math.floor(Math.abs(dy) * (dirVect[0]));
				this.path.push([dx,dy]);
			}
		}

//		set an interval to iterate through dirVect and move by that many pixels
		var _this = this;
		this.pathIndex = 0;
		this.animator = setInterval(function() { _this._animateMapStep(); }, 20);
	}
}


/**
 * read a coordinate array representing a path and move the map along it.
 *
 * @return true if path array is empty (at final location)
 */
JMap.web.Map.prototype._animateMapStep = function() {
	var vector = this.path[this.pathIndex];
	if (vector) {

//		get values from path vector and move the map
		this.moveBy(vector[0], vector[1]);
		this.pathIndex++;
		if (this.pathIndex == this.path.length) {
			clearInterval(this.animator);
			this.animator = false;
			this._testSnapBack(); 
			this.path = null;
			this._HUDManager.updateOnMapMoved();
			if (this._SVGManager) this._SVGManager.update();
			if (this.onDrop) {
				this.onDrop();
			}
			return true;  //done
		} else {
			return false; //not done
		}
	}
}



/**
 * Test to see if the tiles need to be cycled around the borders of the map. when
 * the border of the map container reaches a threshold, the tiles on the end of 
 * the map where the threshold is exceeded need to be cycled around to the other
 * side of the map.  i call this "snap back".  this function tests to see whether
 * or not snap back needs to occur and, if so, how many times.
 */
JMap.web.Map.prototype._testSnapBack = function() {
	clearTimeout(this.snapBackTimer);
	this.snapBackTimer = null;

	var curMapX = parseInt(this.mapLayerContainer.style.left,10);
	var curMapY = parseInt(this.mapLayerContainer.style.top,10);

	//see if the map window has been resized
	var resizeOffsetX = (((this.numTilesX - 1) * this.tileSize) - this.viewportWidth) / 2;
	var resizeOffsetY = (((this.numTilesY - 1) * this.tileSize) - this.viewportHeight) / 2;
	
	//test x thresholds for snapback and perform snapback cycles as necessary    
	if (curMapX > (0.0 * this.tileSize) - resizeOffsetX) { 
		var numCycles = Math.floor((curMapX + resizeOffsetX) / this.tileSize) + 1;   
		curMapX -= (numCycles * this.tileSize);
		this.mapLayerContainer.style.left = curMapX + 'px';
		for (var i = 0; i < Math.abs(numCycles); i++) {
			this._mapTileCycle(1,0);
		}
	} else if (curMapX < (-1.0 * this.tileSize) - resizeOffsetX) {
		var numCycles = Math.ceil(curMapX / this.tileSize);
		curMapX -= (numCycles * this.tileSize);
		this.mapLayerContainer.style.left = curMapX + 'px';
		for (var i = 0; i < Math.abs(numCycles); i++) {
			this._mapTileCycle(-1,0);
		}
	}

	//test y thresholds for snapback and perform snapback cycles as necessary
	if (curMapY > (-0.0 * this.tileSize) - resizeOffsetY) {
		var numCycles = Math.floor((curMapY + resizeOffsetY) / this.tileSize) + 1;
		curMapY -= (numCycles * this.tileSize);
		this.mapLayerContainer.style.top = curMapY + 'px';
		for (var i = 0; i < Math.abs(numCycles); i++) {
			this._mapTileCycle(0,1);
		}		
	} else if (curMapY < (-1.0 * this.tileSize) - resizeOffsetY) {
		var numCycles = Math.ceil(curMapY / this.tileSize);
		curMapY -= (numCycles * this.tileSize);
		this.mapLayerContainer.style.top = curMapY + 'px';
		for (var i = 0; i < Math.abs(numCycles); i++) {
			this._mapTileCycle(0,-1);
		}			
	}	
}



/**
 * performs the snapback of the image tiles.
 *
 *@param dx  number of times to cycle image tiles in the x direction
 *@param dy  number of times to cycle image tiles in the y direction
 */
JMap.web.Map.prototype._mapTileCycle = function(dx, dy) {
	//snapback for all layers in the map
	for (var i = 0; i < this.layerManager.activeMapLayers.length; i++) {
		this.layerManager.activeMapLayers[i].mapTileCycle(dx, dy);
	}

	this.xNoLoop -= dx;

	//update current map x and y coordinates for reference
	if (!this.noLoop) {
		this.x = (this.x - dx) % this.getTilesPerMapX();
		if (this.x < 0) { 
			this.x = this.getTilesPerMapX() - dx; 
		}
	} else {
		this.x = this.xNoLoop;
	}
	
	this.y += dy;   
}





//---------------------------------------------------------------------------
//PIXEL CALCULATIONS
//---------------------------------------------------------------------------

JMap.web.Map.prototype.getLatLonFromScreenPixel = function(x, y) {
	var mapCoords = this.getMapCoordsInPixelSpace();
	var clickLL = this.getLatLonFromPixel(mapCoords.x + x, mapCoords.y + (this.viewportHeight - y));
	return { lat: clickLL.lat, lon: clickLL.lon };
}


/**
 * returns the map coordinates in pixel space of the upper left corner of the map viewport
 * 
 * @return  coordinate pair in x,y pixel space
 */
JMap.web.Map.prototype.getMapCoordsInPixelSpace = function() {
	
	var mapLayerContainerTop = parseInt(this.mapLayerContainer.style.top,10);
	var y = ((this.y + 1) * this.tileSize) + (mapLayerContainerTop);
	//viewport resize
	y += (((this.numTilesY - 1) * this.tileSize) - this.viewportHeight);
	
	var mapLayerContainerLeft = parseInt(this.mapLayerContainer.style.left,10);
	var x = ((this.x) * this.tileSize) - mapLayerContainerLeft;
	x = x % (this.getTilesPerMapX() * this.tileSize);
	
	return {x:x, y:y};
}

/**
 * takes a pixel coordinate and translates it to the corresponding computer screen's pixel 
 * coordinates
 * 
 * @param {int} x	x pixel location from pixel space
 * @param {int} y	y pixel location from pixel space
 * 
 * @return	coordinate pair representing a pixel location on computer screen
 */
JMap.web.Map.prototype.transformPixelToScreenSpace = function(x, y) {
	var mapCoords = this.getMapCoordsInPixelSpace();
	
	x = x - mapCoords.x;	
	y = this.viewportHeight - (y - mapCoords.y);
	
	//place in center of map area if zoomed out far
	var pixelsPerMapX = this.getTilesPerMapX() * this.tileSize;
	if (pixelsPerMapX < this.viewportWidth) {
		var shiftX = Math.round(Math.floor(this.viewportWidth / pixelsPerMapX) / 2);
		if (mapCoords.x < pixelsPerMapX / 2 && Math.round(this.viewportWidth / pixelsPerMapX) == 1) {
			shiftX -= 1;
		}		
		x += (pixelsPerMapX * shiftX);
	}
	
	return {x: x, y: y};
}


/**
 * takes a lat lon coordinate pair and translates it to the corresponding computer screen's pixel 
 * coordinates
 * 
 * @param {double} lat	lat to translate
 * @param {double} lon	lon to translate
 * 
 * @return	coordinate pair representing a pixel location on computer screen
 */
JMap.web.Map.prototype.transformLatLonToScreenSpace = function(lat, lon) {
	var mapCoords = this.getPixelFromLatLon(lat, lon);
	return this.transformPixelToScreenSpace(mapCoords.x, mapCoords.y);	
}


/**
 * Calculate the center latitude and longitude value pair of the current
 * map viewport by finding the midpoint of the current viewport bounding box
 * in the latitudinal and longitudinal dimensions.
 *
 * @return lat/lon pair representing center of map viewport
 */
JMap.web.Map.prototype.getCenterLatLon = function() {
	var mapCoords = this.getMapCoordsInPixelSpace();
	
	var centerLL = this.getLatLonFromPixel(
			mapCoords.x + (this.viewportWidth/2), 
			mapCoords.y + (this.viewportHeight/2)
		);
	
	if (!this.noLoop) {
		centerLL.lon = (((centerLL.lon + 180) % 360) - 180);
	}
	
	return {lat: centerLL.lat, lon: centerLL.lon};
}



/**
 * translate a point in pixel space to decimal degree space
 *
 * @param {int} x	x (left, right) coordinate on the screen
 * @param {int} y	y (top, bottom) coordinate on the screen.
 *
 * @return	lat/lon coordinate pair
 */
JMap.web.Map.prototype.getLatLonFromPixel = function(x, y) {
	return this.projection.getLatLonFromPixel(x, y);
}



/**
 * translate a point in decimal degree space to pixel space
 *
 * @param {double} lat	latitude in decimal degrees
 * @param {double} lon	longitude in decimal degrees
 *
 * @return lat/lon pair translated to pixel space
 */
JMap.web.Map.prototype.getPixelFromLatLon = function(lat, lon) {
	return this.projection.getPixelFromLatLon(lat, lon);
}



/**
 * The number of tiles that fit across the ENTIRE map -180 to 180 horizontally.
 * This method applies at any zoom level in this application
 * 
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 *
 * @return number of tiles it takes to horizontally span 360 decimal degrees
 */
JMap.web.Map.prototype.getTilesPerMapX = function(atZoom) {
	return this.projection.getTilesPerMapX(atZoom);
}

/**
 * The number of tiles that fit across the ENTIRE map -90 to 90 VERTICALLY.
 * This method applies at any zoom level in this application
 * 
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 *
 * @return number of tiles it takes to vertically span 180 decimal degrees
 */
JMap.web.Map.prototype.getTilesPerMapY = function(atZoom) {
	return this.projection.getTilesPerMapY(atZoom);
}

/**
 * Get the scale of this map.
 *
 * @return map scale as a decimal value
 */
JMap.web.Map.prototype.getScale = function() {
	
	var bbox = this.getViewportBoundingBox();
	var mapCoords = this.getMapCoordsInPixelSpace()
	
	var yHeightInPx = mapCoords.y + this.viewportHeight;
	var vertScale = (bbox.ymax - bbox.ymin) / (yHeightInPx);
	
	var xWidthInPx = mapCoords.x + this.viewportWidth;
	var horzScale = (bbox.xmax - bbox.xmin) / (xWidthInPx);

	return ((vertScale + horzScale) / 2.0);
}



/**
 * Calculates the bounding box, in decimal degrees, of the viewport (visible
 * portion of the map.)
 *
 * @return an object representing a bounding box xmin, ymin, xmax, ymax
 */
JMap.web.Map.prototype.getViewportBoundingBox = function() {

	var mapCoords = this.getMapCoordsInPixelSpace();
	
	var bboxMin = this.getLatLonFromPixel(mapCoords.x, mapCoords.y);
	var bboxMax = this.getLatLonFromPixel(mapCoords.x + this.viewportWidth, mapCoords.y + this.viewportHeight);
	if (bboxMax.lon < bboxMin.lon) {
		if (bboxMin.lon < 0) {
			bboxMax.lon += 360;
		} else {
			bboxMin.lon -= 360;
		}
	}

	return {xmin:bboxMin.lon, ymin:bboxMin.lat, xmax:bboxMax.lon, ymax:bboxMax.lat};
}


/**
 * Calculates the bounding box, in decimal degrees, of the viewport (visible
 * portion of the map) and creates a string out of it.
 *
 * @return a string representing the bounding box of the viewport in this format: xmin, ymin, xmax, ymax
 */
JMap.web.Map.prototype.getViewportBoundingBoxString = function() {
	var bbox = this.getViewportBoundingBox();
	return bbox.xmin + ',' + bbox.ymin + ',' + bbox.xmax + ',' + bbox.ymax;
}



/**
 * Calculates the bounding box, in decimal degrees, of the entire map, including what is clipped
 * off by the viewport.
 * @return an object representing a bounding box xmin, ymin, xmax, ymax
 */
JMap.web.Map.prototype.getUnclippedBoundingBox = function() {

	
	
	var mapLayerContainerTop = parseInt(this.mapLayerContainer.style.top,10);
	var y = ((this.y + 1) * this.tileSize);
	//viewport resize
	y += (((this.numTilesY - 1) * this.tileSize) - (this.tileSize * this.numTilesY));
	
	var mapLayerContainerLeft = parseInt(this.mapLayerContainer.style.left,10);
	var x = ((this.x) * this.tileSize)  % (this.getTilesPerMapX() * this.tileSize);
	
	
	var bboxMin = this.getLatLonFromPixel(x, y);
	var bboxMax = this.getLatLonFromPixel(x + (this.tileSize * this.numTilesX), y + (this.tileSize * this.numTilesY));
	if (bboxMax.lon < bboxMin.lon) {
		if (bboxMin.lon < 0) {
			bboxMax.lon += 360;
		} else {
			bboxMin.lon -= 360;
		}
	}

	return {xmin:bboxMin.lon, ymin:bboxMin.lat, xmax:bboxMax.lon, ymax:bboxMax.lat};
}


/**
 * Calculates the bounding box, in decimal degrees, of the entire map, including what is clipped
 * off by the viewport and creates a string out of it.
 *
 * @return a string representing the bounding box of the viewport in this format: xmin, ymin, xmax, ymax
 */
JMap.web.Map.prototype.getUnclippedBoundingBoxString = function() {
	var bbox = this.getUnclippedBoundingBox();
	return bbox.xmin + "," + bbox.ymin + "," + bbox.xmax + "," + bbox.ymax;
}




/**
 * Deconstructor
 * 
 */
JMap.web.Map.prototype.kill = function() {

	this.containerEl.removeChild(this.mapViewport);

	this.mapViewport.removeChild(this.pane);
	this.mapViewport.removeChild(this.mapLayerContainer);

	//kill all layers
	this.layerManager.kill();
	
	if (this.tileManager) this.tileManager.kill();
	this.tileManager = null;
	
	//nullify event handlers
	this.pane.oncontextmenu = null;
	this.pane.onmousedown = null;
	this.pane.ondblclick = null;

	document.onmousemove = null;
	document.onmouseup = null;

	this.mapLayerContainer = null;
	this.pane = null;
	this.mapViewport = null;
	this.containerEl = null; 	
}
