/**
 * @author johnhollister
 * 
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.MVMapCacheLayer = function(params) {
	//default settings - these can all be specified in the params object
	this.format = 'PNG';	//default image format 
	this.request = 'gettile';


	this.serviceType = 'mapcache';
	
	//call base class
	JMap.web.mapLayer.Layer.call(this,params);
	
}


//inherit prototype from Layer
JMap.web.mapLayer.MVMapCacheLayer.prototype = new JMap.web.mapLayer.Layer();


//redefine getSource method
JMap.web.mapLayer.MVMapCacheLayer.prototype.getSourceURL = function(x, y) {	
	
	var tempBase = '';
	if (this.serverIndex != undefined) {
		this.serverIndex = (x * y) % this.baseUrls.length;
		tempBase = this.baseUrls[this.serverIndex];
	} else {
		tempBase = this.baseUrl;
	}

	
	var src = '';
	if (src.indexOf('?') == -1) {
		src += '?';
	} else if (src.indexOf('?') != src.length - 1) {
		src += '&';
	}
	
	src += 'request=' + this.request;
	src += '&zoomlevel=' + this.map.zoom;
	src += '&mx=' + (x % this.map.getTilesPerMapX());
	src += '&my=' + y;
	src += '&mapcache=' + this.dataSource + "." + this.name;
	src += '&format=' + this.format;  
	
	return tempBase + src;
}