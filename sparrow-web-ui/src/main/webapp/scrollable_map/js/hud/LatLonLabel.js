/**
 * A label on the map that displays the current latitude and longitude of the mouse on the map.
 * 
 * @param {HUD} pHUD	heads up display this latlonlabel belongs to
 * @constructor
 */
JMap.hud.LatLonLabel= function(pHUD) {
	this.HUD = pHUD
	this._createHTML();
}



JMap.hud.LatLonLabel.prototype._createHTML = function() {
	this.latLonLabel = document.createElement('div');
	this.latLonLabel.className = 'hud-lat-lon-label';
	
	if (this.HUD._HUDOverviewMap) {
		this.latLonLabel.style.right = this.HUD._HUDOverviewMap.overviewMap.viewportWidth + 10 + 'px';
	} else {
		this.latLonLabel.style.right = '0px';
	}
	
	this.HUD.map.mapViewport.appendChild(this.latLonLabel);
}



/**
 * update the label with the current lat/lon of the mouse
 * 
 * @param {int} x	x coordinate of the mouse
 * @param {int} y	y coordinate of the mouse
 */
JMap.hud.LatLonLabel.prototype.update = function(event) {
	if (!document.onmousemove) {
		var coords = JMap.util.getRelativeCoords(event, this.HUD.map.mapViewport);
		var mousePosition = this.HUD.map.getLatLonFromPixel(coords.x, coords.y);
		
		var tLat = mousePosition.lat;
		var tLon = mousePosition.lon;
		
		var latDegrees = Math.floor(tLat);
		var latMinutes = Math.floor((tLat - latDegrees) * 60);
		
		var latSeconds = Math.floor((((tLat - latDegrees) * 60) - latMinutes) * 60);
		
		
		var lonDegrees = Math.floor(tLon);
		var lonMinutes = Math.floor((tLon - lonDegrees) * 60);

		var lonSeconds = Math.floor((((tLon - lonDegrees) * 60) - lonMinutes) * 60);		
		
		
		
		
		if (tLat > 0) {
			tLat = latDegrees + '\u00B0 ' + latMinutes + "' " + latSeconds + '" N';
		} else if (tLat < 0) {
			tLat = Math.abs(latDegrees) + '\u00B0 ' + latMinutes + "' " + latSeconds + '" S';

		} else {
			tLat = "0\u00B0 0' " + ' 0"';
		}
		
		if (tLon > 0) {
			tLon = lonDegrees + '\u00B0 ' + lonMinutes + "' " + lonSeconds + '" E';
		} else if (tLon < 0) {
			tLon = Math.abs(lonDegrees) + '\u00B0 ' + lonMinutes + "' " + lonSeconds + '" W';
		} else {
			tLon = "0\u00B0 0' " + ' 0"';	
		}
		
		this.latLonLabel.innerHTML = tLat + '<br/>' + tLon;
	}
}


/**
 * Clears the text out of the lat/lon label.  Should be called when mouse leaves the map area.
 */
JMap.hud.LatLonLabel.prototype.clear = function() {
	this.latLonLabel.innerHTML = '';
}


/**
 * deconstructor
 */
JMap.hud.LatLonLabel.prototype.kill = function() {
	this.HUD.map.mapViewport.removeChild(this.latLonLabel);
	this.latLonLabel = null;
}