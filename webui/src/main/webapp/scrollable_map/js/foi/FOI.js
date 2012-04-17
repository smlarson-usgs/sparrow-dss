/**
 * this is a base class for any FOI subclass the developer wants to create
 */


/**
 * @constructor
 */
JMap.foi.FOI = function(params) {
	if (params != undefined) {
		this.map;	//set by FOIManager
		this.offsetXPx = 0;
		this.offsetYPx = 0;
		this.lat = params.lat;
		this.lon = params.lon;
	}
}

JMap.foi.FOI.prototype._createHTML = function() {
	var _this = this;
	
	this.offsetXPx = 5;
	this.offsetYPx = 5;
	
	this.foi = document.createElement('div');
	this.foi.style.position = 'absolute';
	this.foi.style.backgroundColor = 'red';
	this.foi.style.border = "dotted black 1px";
	this.foi.style.overflow = 'hidden';
	this.foi.style.width = '10px';
	this.foi.style.height = '10px';
	this.foi.style.top = '0px';
	this.foi.style.left = '0px';
	
	this.foi.onmouseover = function() { _this.FOIManager.explode(); };
	//this.foi.onclick = function() { alert(_this.lat + " : " + _this.lon); };
	
	this.moveToLatLon(this.lat, this.lon)
	
	this.map.pane.appendChild(this.foi);
}


JMap.foi.FOI.prototype.moveBy = function(dx, dy) {
	var foiLeft = parseInt(this.foi.style.left, 10);
	var foiTop = parseInt(this.foi.style.top, 10);
	
	var newX = (foiLeft + dx);
	var newY = (foiTop + dy);
	
	this.foi.style.left = newX + 'px';
	this.foi.style.top = newY + 'px';
	
	//this.moveToLatLon(this.lat, this.lon);
}



JMap.foi.FOI.prototype.moveToPx = function(x, y) {
	this.foi.style.top = (y - this.offsetYPx) + 'px';
	this.foi.style.left = (x - this.offsetXPx) + 'px';
}



JMap.foi.FOI.prototype.moveToLatLon = function(lat, lon) {
	this.lat = lat;
	this.lon = lon;
	var mapCoordsPx = this.map.transformLatLonToScreenSpace(lat, lon);
	if (mapCoordsPx.x < 0) mapCoordsPx.x += this.map.getTilesPerMapX() * this.map.tileSize;

	this.moveToPx(mapCoordsPx.x, mapCoordsPx.y);
}


JMap.foi.FOI.prototype.resetPositionOnZoom = function() {
	this.moveToLatLon(this.lat, this.lon);
}


JMap.foi.FOI.prototype.kill = function() {
	this.map.pane.removeChild(this.foi);
	this.map = null;
}