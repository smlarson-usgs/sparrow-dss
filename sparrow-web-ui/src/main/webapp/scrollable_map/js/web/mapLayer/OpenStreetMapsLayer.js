/**
 * @author johnhollister
 * 
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.OpenStreetMapsLayer = function(params) {
	this.format = 'png';
		
	//call base class
	JMap.web.mapLayer.Layer.call(this,params);
}

//inherit prototype from Layer
JMap.web.mapLayer.OpenStreetMapsLayer.prototype = new JMap.web.mapLayer.Layer();

//redefine getSource method
JMap.web.mapLayer.OpenStreetMapsLayer.prototype.getSourceURL = function(x, y) {
	var src = this.baseUrl + '/' + this.map.zoom  +  '/' + x + '/' + (this.map.getTilesPerMapY() - y - 1) + '.' + this.format;
	return src;
}