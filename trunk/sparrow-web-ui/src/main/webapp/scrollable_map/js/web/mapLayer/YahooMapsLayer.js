/**
 * @author johnhollister
 * 
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.YahooMapsLayer = function(params) {
	//call base class
	JMap.web.mapLayer.Layer.call(this, params);
}

//inherit prototype from Layer
JMap.web.mapLayer.YahooMapsLayer.prototype = new JMap.web.mapLayer.Layer();

//redefine getSource method
JMap.web.mapLayer.YahooMapsLayer.prototype.getSourceURL = function(x, y) {
	var tempBase = this.baseUrl;
	if (this.serverIndex != undefined) {
		this.serverIndex = (x * y) % this.baseUrls.length;
		tempBase = this.baseUrls[this.serverIndex];
	}

	var src = tempBase + '&.intl=en&r=1&x=' + x + '&y=' + (y - Math.floor(this.map.getTilesPerMapY() / 2)) + '&z=' + (this.map.zoom + 1);
	return src;
}