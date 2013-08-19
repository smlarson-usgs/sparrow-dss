/**
 * OverviewMap creates a smaller, more zoomed out map in the bottom right corner of the 
 * parent map.
 * 
 * @constructor
 */
JMap.hud.OverviewMap = function(pHUD, params) {
	var _this = this;
	
	this.HUD = pHUD;
	if (!params.containerEl) {
		this._createHTML((params.mapWidthPx||150),(params.mapHeightPx||150));
	} else {
		this.containerEl = document.getElementById(params.containerEl);
	}
	this.autoPanInterval;
	
	this.overviewMap = new JMap.web.Map({
		parentMap: this.HUD.map,
		containerEl: this.containerEl.id,
		centerLat: this.HUD.map.centerLatLon.lat,
		centerLon: this.HUD.map.centerLatLon.lon,
		validBBox: this.HUD.map.tempValidBBox,
		numTilesX: (params.numTilesX||3),
		numTilesY: (params.numTilesY||3),
		tileSize: (params.tileSize||256),
		projection: this.HUD.map.projection,
		border: (!params.containerEl),
		layersFile: (params.layersFile),
		mapWidthPx: (params.mapWidthPx||150),
		mapHeightPx: (params.mapHeightPx||150),
		listeners: {
			onDrop: function() { _this.syncParentMap() }
		}
	});
/*
	var tl = new JMap.web.mapLayer.TMSLayer({
		id: 180,
		zDepth: 250050,
		baseUrl: 'http://labs.metacarta.com/wms-c/Basic.py/1.0.0/basic/'
	});
	
	var gl = new JMap.web.mapLayer.GoogleMapsLayer({
		id: -133,
		zDepth: 50050,
		baseUrl: 'http://mt$[0-3].google.com/mt/v=w2.95&s=Gal'
	})
	*/
	//http://mt0.google.com/mt/v=w2.95&hl=en&x=2&y=5&z=4&s=Gal
	
	//this.overviewMap.appendLayer(tl);
	//this.overviewMap.appendLayer(gl);
	
	if (!params.containerEl) {
		this.minMax = document.createElement('div');
		this.minMax.className = 'hud-overview-minmax-open';
		this.minMax.onclick = function() { _this.toggleOverviewMap(); };
		this.containerEl.appendChild(this.minMax);
	}
		
	this.pan = document.createElement('div');
	this.pan.className = 'hud-pan-controller';
	this.panBackground = document.createElement('div');
	this.panBackground.className = 'hud-pan-background';
	this.pan.appendChild(this.panBackground);
	
	this.pan.onmousedown = function(event) { return _this._startDrag(event); };
	
	this.overviewMap.mapViewport.appendChild(this.pan);
}

JMap.hud.OverviewMap.prototype._createHTML = function(mapWidth, mapHeight) {
	var numOverviews = JMap.util.getElementsByClassName(document, 'overview-map');
	if (numOverviews) {
		numOverviews = numOverviews.length;
	} else {
		numOverviews = 0;
	}
	
	this.containerEl = document.createElement('div');
	this.containerEl.className = 'hud-overview-map';
	this.containerEl.id = 'hud-overview-map-' + numOverviews;
	this.containerEl.style.width = mapWidth + 'px';
	this.containerEl.style.height = mapHeight + 'px';
	
	this.HUD.map.mapViewport.appendChild(this.containerEl);
}


JMap.hud.OverviewMap.prototype.update = function() {
	var center = this.HUD.map.getCenterLatLon();
	this.overviewMap.moveTo(center.lat, center.lon);
	this.overviewMap.centerLatLon = center;
	
	var bbox = this.HUD.map.getViewportBoundingBox();
	var topleft = this.overviewMap.transformLatLonToScreenSpace(bbox.ymax, bbox.xmin);
	var bottomright = this.overviewMap.transformLatLonToScreenSpace(bbox.ymin, bbox.xmax);
	var overviewCoords = this.overviewMap.getMapCoordsInPixelSpace();

	this.pan.style.width = (bottomright.x - topleft.x) + 'px';
	this.pan.style.left = topleft.x + 'px';

	this.pan.style.height = bottomright.y - topleft.y + 'px';
	this.pan.style.top = topleft.y + 'px';
	
	//autozoom based on pan controller size
	if (((bottomright.x - topleft.x) < (this.overviewMap.viewportWidth * (3/10)) && this.HUD.map.viewportWidth >= this.HUD.map.viewportHeight) ||
		((bottomright.y - topleft.y) < (this.overviewMap.viewportHeight * (3/10)) && this.HUD.map.viewportWidth < this.HUD.map.viewportHeight)) {
		this.overviewMap.zoomIn();
	} else {
		if (((bottomright.x - topleft.x) >= (this.overviewMap.viewportWidth * (6/10)) && this.HUD.map.viewportWidth >= this.HUD.map.viewportHeight) ||
			((bottomright.y - topleft.y) >= (this.overviewMap.viewportHeight * (6/10)) && this.HUD.map.viewportWidth < this.HUD.map.viewportHeight)) {
			if (this.overviewMap.zoom > this.overviewMap.minZoom) {
				this.overviewMap.zoomOut();
			} else {
				this.pan.style.display = 'none';
			}
		} else {
			this.pan.style.display = 'block';	
			if (bottomright.x > (this.overviewMap.getTilesPerMapX() * this.overviewMap.tileSize)) {
				topleft.x -= (this.overviewMap.getTilesPerMapX() * this.overviewMap.tileSize);
			} else if (topleft.x < 0) {
				topleft.x += (this.overviewMap.getTilesPerMapX() * this.overviewMap.tileSize);
			}
			
			this.pan.style.left = topleft.x + 'px';			
			this.pan.style.top = topleft.y + 'px';
		}
	}
}


JMap.hud.OverviewMap.prototype.syncParentMap = function() {
	var center = this.overviewMap.getCenterLatLon();
	this.overviewMap.dontDraw = true;
	this.HUD.map.moveTo(center.lat, center.lon);
	this.overviewMap.dontDraw = false;
	this.HUD.map.centerLatLon = center;
	
}

JMap.hud.OverviewMap.prototype.toggleOverviewMap = function() {
	if (this.minMax.className == 'hud-overview-minmax-open') {
		this.minimize();
	} else {
		this.maximize();
	}
}

JMap.hud.OverviewMap.prototype.minimize = function() {
	this.minMax.className = 'hud-overview-minmax-close';
	this.containerEl.style.width = '15px';
	this.containerEl.style.height = '15px';
	this.containerEl.style.paddingTop = '0em';
	this.containerEl.style.paddingLeft = '0em';
}

JMap.hud.OverviewMap.prototype.maximize = function() {
	this.minMax.className = 'hud-overview-minmax-open';	
	this.containerEl.style.width = this.overviewMap.viewportWidth + 'px';
	this.containerEl.style.height = this.overviewMap.viewportHeight + 'px';
	this.containerEl.style.paddingTop = '0.3em';
	this.containerEl.style.paddingLeft = '0.3em';
}




JMap.hud.OverviewMap.prototype._startDrag = function(event) {
	var _this = this;

	event = event || window.event;  

	this.lastMouseX = event.clientX;
	this.lastMouseY = event.clientY;  
	
	this.validX = this.lastMouseX;
	this.validY = this.lastMouseY;
	
	this.startDragAt = event.clientX + ':' + event.clientY;

	document.onmousemove = function(event) { return _this._drag(event); };
	document.onmouseup = function(event) { return _this._endDrag(event); };
	return false;
}


JMap.hud.OverviewMap.prototype._drag = function(event) {
	event = event || window.event;
	if (navigator.appVersion.indexOf("MSIE")!=-1) JMap.util.unselect();

	var currentMouseX = event.clientX;
	var currentMouseY = event.clientY;

	var dx = currentMouseX - this.lastMouseX;
	var dy = currentMouseY - this.lastMouseY;
	
	var panLeft = parseFloat(this.pan.style.left.split('px')[0],10);
	var panTop = parseFloat(this.pan.style.top.split('px')[0],10);
	var panWidth = parseFloat(this.pan.style.width.split('px')[0],10);
	var panHeight = parseFloat(this.pan.style.height.split('px')[0],10);
	
	var xMinLoc = panLeft + dx;
	var yMinLoc = panTop + dy;
	var xMaxLoc = xMinLoc + panWidth;
	var yMaxLoc = yMinLoc + panHeight;
		
	this.pan.style.left = xMinLoc + 'px';
	this.pan.style.top = yMinLoc + 'px';

	this.lastMouseX = currentMouseX;
	this.lastMouseY = currentMouseY;
	
	
	//detect autopan
	if (xMinLoc < 0 || yMinLoc < 0 || xMaxLoc > this.overviewMap.viewportWidth || yMaxLoc > this.overviewMap.viewportHeight) {
		var _this = this;
			clearInterval(this.autoPanInterval);

			this.autoPanInterval = setInterval(function() { _this._autoPan(); }, 10);
		
	} else {
		clearInterval(this.autoPanInterval);
	}
	
	
	return false;
}


JMap.hud.OverviewMap.prototype._endDrag = function(event) {
	event = event||window.event;
	document.onmousemove = null;
	document.onmouseup = null;
	
	clearInterval(this.autoPanInterval);
	
	if (this.startDragAt != event.clientX + ':' + event.clientY) {
	
		//find center of pan controller and move parent map to that lat lon-
		var panLeft = parseFloat(this.pan.style.left.split('px')[0],10);
		var panTop = parseFloat(this.pan.style.top.split('px')[0],10);
		var panWidth = parseFloat(this.pan.style.width.split('px')[0],10);
		var panHeight = parseFloat(this.pan.style.height.split('px')[0],10);
		var overviewCoords = this.overviewMap.getMapCoordsInPixelSpace();
		
		var panCenterX = panLeft + (panWidth / 2);
		var panCenterY = this.overviewMap.viewportHeight - (panTop + (panHeight / 2));
		
		var centerLatLon = this.overviewMap.getLatLonFromPixel(overviewCoords.x + panCenterX, overviewCoords.y + panCenterY);
		if (this.overviewMap.validBBox) {
			if (centerLatLon.lon < this.overviewMap.validBBox.xmin) {
				centerLatLon.lon = this.overviewMap.validBBox.xmin;
			} else if (centerLatLon.lon > this.overviewMap.validBBox.xmax) {
				centerLatLon.lon = this.overviewMap.validBBox.xmax;
			}
		}
		
		//		this.HUD.map.animateMoveToLatLon(centerLatLon.lat, centerLatLon.lon)
		this.HUD.map.moveTo(centerLatLon.lat, centerLatLon.lon);
		this.HUD.map.centerLatLon = centerLatLon;
		this.HUD.map.draw();
	}

	return false;
}


/**
 * when panner is dragged to side of viewport, the map automatically scrolls in the
 * direction that the panner contacts the side of the viewport.
 */
JMap.hud.OverviewMap.prototype._autoPan = function() {
	var panLeft = parseFloat(this.pan.style.left.split('px')[0],10);
	var panTop = parseFloat(this.pan.style.top.split('px')[0],10);
	var panWidth = parseFloat(this.pan.style.width.split('px')[0],10);
	var panHeight = parseFloat(this.pan.style.height.split('px')[0],10);
	
	var xMinLoc = panLeft;
	var yMinLoc = panTop;
	var xMaxLoc = xMinLoc + panWidth;
	var yMaxLoc = yMinLoc + panHeight;
	
	var panCenterX = panLeft + (panWidth / 2);
	var panCenterY = panTop + (panHeight / 2);
	
	var centerLatLon = this.overviewMap.getLatLonFromPixel(panCenterX, panCenterY, true);
	
	var dx = 0;
	if (xMinLoc < 0) {
		dx = 1;
	} else if (xMaxLoc > this.overviewMap.viewportWidth) {
		dx = -1;
	}
	
	var dy = 0;
	if (yMinLoc < 0) {
		dy = 1;
	} else if (yMaxLoc > this.overviewMap.viewportHeight) {
		dy = -1;
	}
	
	this.overviewMap.moveBy(dx, dy);
	
	this.overviewMap._testSnapBack();
}


JMap.hud.OverviewMap.prototype.kill = function() {

	this.pan.onmousedown = null;
	this.overviewMap.mapViewport.removeChild(this.pan);
	this.pan = null;
	
	if (this.minMax) {
		this.minMax.onclick = null;
		this.HUD.map.mapViewport.removeChild(this.containerEl);

	}
	this.overviewMap.kill();
}