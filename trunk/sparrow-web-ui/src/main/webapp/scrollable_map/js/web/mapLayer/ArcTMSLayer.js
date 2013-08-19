/**
 * @author johnhollister
 * 
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.ArcTMSLayer = function(params) {
	this.format = 'jpeg';
		
	//call base class
	JMap.web.mapLayer.Layer.call(this,params);
}

//inherit prototype from Layer
JMap.web.mapLayer.ArcTMSLayer.prototype = new JMap.web.mapLayer.Layer();

//redefine getSource method
JMap.web.mapLayer.ArcTMSLayer.prototype.getSourceURL = function(x, y) {
	
	var tempBase = '';
	if (this.serverIndex != undefined) {
		this.serverIndex = (x * y) % this.baseUrls.length;
		tempBase = this.baseUrls[this.serverIndex];
	} else {
		tempBase = this.baseUrl;
	}
	

	  var tpmx = this.map.getTilesPerMapX()
	  var tpmy = this.map.getTilesPerMapY();
	  var src = tempBase + this.map.zoom  + '/' + (tpmy - y - 1) +  '/' + (x % tpmx);
	    
	  return src;
}