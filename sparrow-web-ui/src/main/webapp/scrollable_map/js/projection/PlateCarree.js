/**
 * @author johnhollister
 * 
 */


/**
 * @constructor
 * 
 * @param {JMap.web.Map} map	the map that is using this projection
 */
JMap.projection.PlateCarree = function(map) {
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
JMap.projection.PlateCarree.prototype.getLatLonFromPixel = function(x, y, atZoom) {	
	return {
			lon: this.getLonFromX(x, atZoom),
			lat: this.getLatFromY(y, atZoom)
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
JMap.projection.PlateCarree.prototype.getLatFromY = function(y, atZoom) {
	return (y * ((180 / this.map.tileSize) / this.getTilesPerMapY(atZoom))) - 90;
}


/**
 * calculate a longitude value from a x coordinate in pixel space
 * 
 * @param {int} x	x coordinate for the pixel
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return	longitude
 */
JMap.projection.PlateCarree.prototype.getLonFromX = function(x, atZoom) {
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
JMap.projection.PlateCarree.prototype.getPixelFromLatLon = function(lat, lon, atZoom) {
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
JMap.projection.PlateCarree.prototype.getXFromLon = function(lon, atZoom) {
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
JMap.projection.PlateCarree.prototype.getYFromLat = function(lat, atZoom) {
	return ((lat + 90) / 180) * (this.map.getTilesPerMapY(atZoom) * this.map.tileSize);
}


/**
 * calculate the number of tiles that make up a map in the x-dimension
 * 
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return number of tiles that map up a map in the x-dimension
 */
JMap.projection.PlateCarree.prototype.getTilesPerMapX = function(atZoom) {
	atZoom = atZoom || this.map.zoom;
	return Math.pow(2, (atZoom + 1));
}


/**
 * calculate the number of tiles that make up a map in the y-dimension
 * 
 * @param {int} atZoom	(optional)  the zoom level to perform the calculation with, will default to map's current zoom level
 * 
 * @return number of tiles that map up a map in the y-dimension
 */
JMap.projection.PlateCarree.prototype.getTilesPerMapY = function(atZoom) {
	atZoom = atZoom || this.map.zoom;
	return Math.pow(2, atZoom);
}