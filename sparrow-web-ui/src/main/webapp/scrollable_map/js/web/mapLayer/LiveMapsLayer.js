/**
 * @author johnhollister
 * 
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.LiveMapsLayer = function(params) {
	//call base class
	JMap.web.mapLayer.Layer.call(this, params);
}

//inherit prototype from Layer
JMap.web.mapLayer.LiveMapsLayer.prototype = new JMap.web.mapLayer.Layer();

//redefine getSource method
JMap.web.mapLayer.LiveMapsLayer.prototype.getSourceURL = function(x, y) {
	
	
	y = (this.map.getTilesPerMapY() - y - 1);
	
	var tempBase = this.baseUrl;
	if (this.serverIndex != undefined) {
		this.serverIndex = (x * y) % this.baseUrls.length;
		tempBase = this.baseUrls[this.serverIndex];
	}
	

	
	var quadKey = '';
	
	
    for (var i = this.map.zoom; i > 0; i--) {
        var digit = 0;
        var mask = 1 << (i - 1);
        if ((x & mask) != 0) {
            digit++;
        }
        if ((y & mask) != 0) {
            digit+=2;
        }
        quadKey += '' + digit;
    }
    //http://ecn.t0.tiles.virtualearth.net/tiles/r0210.png?g=282&mkt=en-us&shading=hill
	
	var src = tempBase + '/r' + quadKey + '.png?g=282&mkt=en-us&shading=hill';
	return src;
}
