/**
 * @author johnhollister
 * 
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.GoogleMapsLayer = function(params) {
	//call base class
	JMap.web.mapLayer.Layer.call(this, params);
}

//inherit prototype from Layer
JMap.web.mapLayer.GoogleMapsLayer.prototype = new JMap.web.mapLayer.Layer();

//redefine getSource method
JMap.web.mapLayer.GoogleMapsLayer.prototype.getSourceURL = function(x, y) {
	var tempBase = this.baseUrl;
	if (this.serverIndex != undefined) {
		this.serverIndex = (x * y) % this.baseUrls.length;
		tempBase = this.baseUrls[this.serverIndex];
	}
	
	//v=w2.92  roads
	//v=w2t.92 satellite
	//v=w2p.87	terrain
	var src = tempBase + '&hl=en&s=&x=' + x + '&y=' + (this.map.getTilesPerMapY() - y - 1) + '&z=' + (this.map.zoom);
	return src;
}