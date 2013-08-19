/**
 * @constructor
 * 
 * @param {JMap.web.Map} map	the map that is using this projection
 */
JMap.projection.Mercator = function(map) {
	this.map = map;
}


/**
 * calculate a latitude and longitude coordinate pair from a location in pixel space
 * 
 * @param {int} x	x coordinate of the pixel
 * @param {int} y	y coordinate of the pixel
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return lat/lon coordinate pair
 */
JMap.projection.Mercator.prototype.getLatLonFromPixel = function(x, y) {
	return {
			lon: this.getLonFromX(x), 
			lat: this.getLatFromY(y)
		   };
}


/**
 * calculate a latitude value from a y coordinate in pixel space
 * 
 * @param {int} y	y coordinate for the pixel
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return	latitude
 */
JMap.projection.Mercator.prototype.getLatFromY = function(y, atZoom) {
	var yRatio = (y - ((this.map.getTilesPerMapY() * this.map.tileSize) / 2)) / (this.map.getTilesPerMapY(atZoom) * this.map.tileSize);
	var yRad = (2 * Math.PI) * yRatio;
	var lat = ((2 * Math.atan(Math.exp(yRad))) - (Math.PI/2)) * (180/Math.PI);
	
	return lat;
}


/**
 * calculate a longitude value from a x coordinate in pixel space
 * 
 * @param {int} x	x coordinate for the pixel
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return	longitude
 */
JMap.projection.Mercator.prototype.getLonFromX = function(x, atZoom) {
	return (x * ((360 / this.map.tileSize) / this.getTilesPerMapX(atZoom))) - 180;
}


/**
 * calculates a coordinate pair in pixel space from at lat/lon pair
 * 
 * @param {double} lat	latitude of the point to translate
 * @param {double} lon	longitude of the point to translate
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return x,y coordinate pair
 */
JMap.projection.Mercator.prototype.getPixelFromLatLon = function(lat, lon, atZoom) {
	return {
			x: this.getXFromLon(lon, atZoom), 
			y: this.getYFromLat(lat, atZoom)
		   };
}


/**
 * calculates the x coordinate from a given longitude value
 * 
 * @param {double} lon	longitude of the point to translate
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 *
 * @return x pixel location 
 */
JMap.projection.Mercator.prototype.getXFromLon = function(lon, atZoom) {
	return ((lon + 180) / 360) * (this.map.getTilesPerMapX(atZoom) * this.map.tileSize);
}


/**
 * calculates the y coordinate from a given latitude value
 * 
 * @param {double} lat	latitude of the point to translate
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 *
 * @return y pixel location 
 */
JMap.projection.Mercator.prototype.getYFromLat = function(lat, atZoom) {
	var latRad = (lat + 90) * (Math.PI/180);
	var radY = Math.log(Math.tan((latRad/2))) + Math.PI;
	var radRat = radY / (2 * Math.PI);
	var y = (this.map.getTilesPerMapY(atZoom) * this.map.tileSize) * radRat;
	
	return y;
}



/**
 * calculate the number of tiles that make up a map in the x-dimension
 * 
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return number of tiles that map up a map in the x-dimension
 */
JMap.projection.Mercator.prototype.getTilesPerMapX = function(atZoom) {
	atZoom = atZoom || this.map.zoom;
	return Math.pow(2, atZoom);
}


/**
 * calculate the number of tiles that make up a map in the y-dimension
 * 
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return number of tiles that map up a map in the y-dimension
 */
JMap.projection.Mercator.prototype.getTilesPerMapY = function(atZoom) {
	atZoom = atZoom || this.map.zoom;
	return Math.pow(2, atZoom);
}
