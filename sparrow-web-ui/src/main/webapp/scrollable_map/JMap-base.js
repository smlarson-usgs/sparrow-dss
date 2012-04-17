JMap.web.mapLayer.Layer = function(params) {
	this.mapTiles = [];
	this.mapLayer;
	this.params = params;
	if (params != undefined) {
		this.map = params.map;
		this.id = params.id;
		this.title = params.title;
		this.name = params.name;
		this.scaleMin = (params.scaleMin || 0);
		this.scaleMax = (params.scaleMax || 100);
		this.zDepth = (params.zDepth || 0);
		this.opacity = (params.opacity || 100);
		this.version = (params.version || '1.1.1');
		this.srs = (params.srs || 4326);
		if (params.request)
			this.request = params.request;
		if (params.format)
			this.format = params.format;
		this.validBoundingBox = params.validBoundingBox;
		this.layersUrlParam = params.layersUrlParam;
		this.legendUrl = params.legendUrl;
		this.metaUrl = params.metaUrl;
		this.description = params.description;
		this.classTheme = params.classTheme;
		this.className = params.className;
		this.sld = params.sld;
		this.overlap = (params.overlap || 0);
		this.customParams = params.customParams;
		this.baseUrl = params.baseUrl;
		if (this.baseUrl.indexOf('$[') > 0) {
			var serverIndexes = this.baseUrl.split('$[')[1].split(']')[0];
			this.serverStart = parseInt(serverIndexes.split('-')[0]);
			this.serverEnd = parseInt(serverIndexes.split('-')[1]);
			this.baseUrls = [];
			var serverBeginPart = this.baseUrl.split('$[')[0];
			var serverEndPart = this.baseUrl.split(']')[1];
			for (var i = this.serverStart; i <= this.serverEnd; i++) {
				this.baseUrls[i - this.serverStart] = serverBeginPart + i
						+ serverEndPart;
			}
			this.serverIndex = this.serverStart;
		}
		if (this.overlap) {
			this.overlapX = Math.floor(this.overlap);
			this.overlapY = Math.floor(this.overlap);
		} else {
			this.overlapX = 0;
			this.overlapY = 0;
		}
		this.dataSource = params.dataSource;
		this.isHiddenFromUser = (params.isHiddenFromUser || false);
		this.isOnByDefault = params.isOnByDefault;
		if (params.isOnByDefault == undefined) {
			this.isOnByDefault = true;
		}
		this.cacheLayer = params.cacheLayer;
		if (params.cacheLayer == undefined) {
			this.cacheLayer = true;
		}
		if (this.isOnByDefault && this.map) {
			this._createLayerHTML();
		}
	}
}
JMap.web.mapLayer.Layer.prototype._createLayerHTML = function() {
	this.mapLayer = document.createElement('div');
	this.mapLayer.className = 'map-layer';
	this.mapLayer.style.width = this.map.mapLayerContainer.style.width;
	this.mapLayer.style.height = this.map.mapLayerContainer.style.height;
	if (this.opacity >= 0 && this.opacity < 100) {
		this.mapLayer.style.opacity = (this.opacity / 100);
		this.mapLayer.style.filter = 'alpha(opacity=' + this.opacity + ')';
	} else {
		this.mapLayer.style.filter = '';
		this.mapLayer.style.opacity = '';
	}
	for (var i = 0; i < this.map.numTilesX; i++) {
		for (var j = 0; j < this.map.numTilesY; j++) {
			this.mapTiles.push(new JMap.web.MapTile({
						layer : this,
						initX : i,
						initY : j
					}));
		}
	}
}
JMap.web.mapLayer.Layer.prototype.activate = function() {
	if (!this.mapLayer) {
		this._createLayerHTML();
	} else {
		for (var i = 0; i < this.mapTiles.length; i++) {
			this.mapTiles[i].syncWithMapCoordinates();
		}
	}
	var inserted = false;
	for (var i = 0; i < this.map.layerManager.activeMapLayers.length; i++) {
		if (parseInt(this.zDepth, 10) > parseInt(
				this.map.layerManager.activeMapLayers[i].zDepth, 10)) {
			this.map.mapLayerContainer.insertBefore(this.mapLayer,
					this.map.layerManager.activeMapLayers[i].mapLayer);
			inserted = true;
			break;
		}
	}
	if (!inserted) {
		this.map.mapLayerContainer.appendChild(this.mapLayer);
	}
	this.draw();
}
JMap.web.mapLayer.Layer.prototype.deactivate = function() {
	this.map.mapLayerContainer.removeChild(this.mapLayer);
}
JMap.web.mapLayer.Layer.prototype.draw = function() {
	for (var i = 0; i < this.mapTiles.length; i++) {
		this.mapTiles[i].draw();
	}
}
JMap.web.mapLayer.Layer.prototype.clear = function() {
	for (var i = 0; i < this.mapTiles.length; i++) {
		this.mapTiles[i].clear();
	}
}
JMap.web.mapLayer.Layer.prototype.getSourceURL = function(x, y) {
	return '';
}
JMap.web.mapLayer.Layer.prototype.setOpacity = function(opacity) {
	if (this.mapLayer) {
		this.opacity = opacity;
		if (this.opacity >= 0 && this.opacity < 100) {
			this.mapLayer.style.opacity = (this.opacity / 100);
			this.mapLayer.style.filter = 'alpha(opacity=' + this.opacity + ')';
		} else {
			this.mapLayer.style.filter = '';
			this.mapLayer.style.opacity = '';
		}
	}
}
JMap.web.mapLayer.Layer.prototype.mapTileCycle = function(dx, dy) {
	if (this.opacity >= 0 && this.opacity <= 100) {
		this.mapLayer.style.filter = '';
	}
	for (var i = 0; i < this.mapTiles.length; i++) {
		this.mapTiles[i].moveBy(dx, dy);
	}
	if (this.opacity >= 0 && this.opacity < 100) {
		this.mapLayer.style.filter = 'alpha(opacity=' + this.opacity + ')';
	}
}
JMap.web.mapLayer.Layer.prototype.syncWithMapCoordinates = function() {
	for (var i = 0; i < this.mapTiles.length; i++) {
		this.mapTiles[i].syncWithMapCoordinates();
	}
}
JMap.web.mapLayer.Layer.prototype.getLayerAsJSON = function() {
	return this.params;
}
JMap.web.mapLayer.Layer.prototype.kill = function() {
	try {
		this.map.mapLayerContainer.removeChild(this.mapLayer);
	} catch (e) {
	}
	for (var i = 0; i < this.mapTiles.length; i++) {
		this.mapTiles[i].kill();
	}
	this.mapTiles = null;
	this.mapLayer = null;
	this.map = null;
}
JMap.svg.Path = function(params) {
	if (params != undefined) {
		this.properties = params.properties;
		this.events = params.events;
	}
}
JMap.svg.Path.prototype.createSVG = function() {
	var _this = this;
	for (var x in this.properties) {
		if (this.SVGManager.propertyEnum[x]) {
			this.shape.setAttribute(this.SVGManager.propertyEnum[x],
					this.properties[x]);
		} else {
			this.shape.setAttribute(x, this.properties[x]);
		}
	}
	if (this.events) {
		if (this.events.onmouseover)
			this.shape.onmouseover = function(e) {
				_this.events.onmouseover(_this);
				return false
			};
		if (this.events.onmouseout)
			this.shape.onmouseout = function(e) {
				_this.events.onmouseout(_this);
				return false
			};
		if (this.events.onmousemove)
			this.shape.onmousemove = function(e) {
				_this.events.onmousemove(_this);
				return false
			};
		if (this.events.onmousedown)
			this.shape.onmousedown = function(e) {
				_this.events.onmousedown(_this);
				return false
			};
		if (this.events.onmouseup)
			this.shape.onmouseup = function(e) {
				_this.events.onmouseup(_this);
				return false;
			};
		if (this.events.onclick)
			this.shape.onclick = function(e) {
				_this.events.onclick(_this);
				return false;
			};
		if (this.events.ondblclick)
			this.shape.ondblclick = function(e) {
				_this.events.ondblclick(_this);
				return false;
			};
	}
	if (this.SVGManager.isIE && this.properties) {
		var stroke = document.createElement('v:stroke');
		if (this.properties['fill-opacity']) {
			var fill = document.createElement('v:fill');
			fill.setAttribute('opacity',
					(this.properties['fill-opacity'] * 100) + '%');
			this.shape.appendChild(fill);
		}
		if (this.properties['stroke-opacity']) {
			stroke.setAttribute('opacity',
					(this.properties['stroke-opacity'] * 100) + '%');
		}
		if (this.properties['stroke-linejoin']) {
			stroke
					.setAttribute('joinstyle',
							this.properties['stroke-linejoin']);
		}
		if (this.properties['stroke-linecap']) {
			stroke.setAttribute('endcap', this.properties['stroke-linecap']);
		}
		this.shape.appendChild(stroke);
	}
}
JMap.svg.Path.prototype.draw = function() {
}
JMap.svg.Path.prototype.translatePointsToMapSpace = function(points) {
	var translatedPoints = [];
	for (var i = 0; i < points.length; i += 2) {
		var coords = this.SVGManager.map.getPixelFromLatLon(points[i], points[i
						+ 1]);
		var txCoords = this.SVGManager.map.transformPixelToScreenSpace(
				coords.x, coords.y);
		translatedPoints.push(Math.floor(txCoords.x
				+ (this.SVGManager.x + this.SVGManager.map.viewportWidth)));
		translatedPoints.push(Math.floor(txCoords.y
				+ (this.SVGManager.y + this.SVGManager.map.viewportHeight)));
	}
	return translatedPoints;
}
JMap.svg.Path.prototype.kill = function() {
	this.shape.onmouseover = null;
	this.shape.onmouseout = null;
	this.shape.onmousemove = null;
	this.shape.onmouseup = null;
	this.shape.onmousedown = null;
	this.shape.onclick = null;
	this.shape.ondblclick = null;
	this.SVGManager.canvas.removeChild(this.shape);
	this.SVGManager = null;
}
JMap.svg.PolyLine = function(params) {
	if (params != undefined) {
		JMap.svg.Path.call(this, params);
		this.points = params.points;
	}
}
JMap.svg.PolyLine.prototype = new JMap.svg.Path();
JMap.svg.PolyLine.prototype.createSVG = function() {
	if (!this.SVGManager.isIE) {
		this.shape = document.createElementNS('http://www.w3.org/2000/svg',
				'path');
		if (this.properties)
			this.properties.fill = 'none';
	} else {
		this.shape = document.createElement('v:shape');
		this.shape.setAttribute('stroked', true);
		this.shape.setAttribute('filled', false);
		this.shape.style.height = (3 * this.SVGManager.map.viewportHeight)
				+ 'px';
		this.shape.style.width = (3 * this.SVGManager.map.viewportWidth) + 'px';
		this.shape.style.left = (-this.SVGManager.map.viewportWidth + 'px');
		this.shape.style.top = (-this.SVGManager.map.viewportHeight + 'px');
		this.shape.setAttribute('coordorigin',
				(-this.SVGManager.map.viewportWidth) + ','
						+ -this.SVGManager.map.viewportHeight);
		this.shape.setAttribute('coordsize',
				(3 * this.SVGManager.map.viewportWidth) + ','
						+ (3 * this.SVGManager.map.viewportHeight));
	}
	this.draw();
	JMap.svg.Path.prototype.createSVG.call(this);
	this.SVGManager.canvas.appendChild(this.shape);
}
JMap.svg.PolyLine.prototype.draw = function() {
	var translatedPoints = this.translatePointsToMapSpace(this.points);
	if (!this.SVGManager.isIE) {
		this.shape.setAttribute('d', 'M' + translatedPoints[0] + ','
						+ translatedPoints[1] + ' L'
						+ translatedPoints.join(' '));
	} else {
		this.shape.setAttribute('path', 'm ' + translatedPoints[0] + ','
						+ translatedPoints[1] + ' l ' + translatedPoints.join()
						+ ' e');
	}
}