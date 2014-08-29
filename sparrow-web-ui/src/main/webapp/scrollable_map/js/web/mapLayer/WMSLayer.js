/**
 * @author johnhollister
 * 
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.WMSLayer = function(params) {
	this.format = 'image/png';
	this.request = 'GetMap';
		
	//call base class
	JMap.web.mapLayer.Layer.call(this,params);
}


//inherit prototype from Layer
JMap.web.mapLayer.WMSLayer.prototype = new JMap.web.mapLayer.Layer();


//redefine getSource method
JMap.web.mapLayer.WMSLayer.prototype.getSourceURL = function(x, y) {
	var src = this.baseUrl;
	if (src.indexOf('?') == -1) {
		src += '?';
	} else if (src.indexOf('?') != src.length - 1) {
		src += '&';
	}
	
	var xmin = this.map.projection.getLonFromX(((x) * this.map.tileSize) - this.overlapX);
	var xmax = this.map.projection.getLonFromX(((x + 1) * this.map.tileSize) + this.overlapX);
	var ymin = this.map.projection.getLatFromY(((y) * this.map.tileSize) - this.overlapY);
	var ymax = this.map.projection.getLatFromY(((y + 1) * this.map.tileSize) + this.overlapY);
	
	
	src += 'request=' + this.request;
	src += '&srs=EPSG:' + this.srs;
	src += '&version=' + this.version;
	src += '&layers=' + encodeURIComponent(this.layersUrlParam);
	src += "&BBOX=" + xmin + "," + ymin + "," + xmax + "," + ymax;
	src += "&width=" + (this.map.tileSize - (this.overlapX * -2));
	src += "&height=" + (this.map.tileSize - (this.overlapY * -2));            
	src += '&transparent=true';
	src += '&format=' + encodeURIComponent(this.format);    
	src += '&styles='; //according to the spec, styles is *required* in WMS, only support default here
	
	//any additional params not in the WMS standard
	if (this.customParams) {
		for (var x in this.customParams) {
			src += '&' + encodeURIComponent(x) + '=' + encodeURIComponent(this.customParams[x]);
		}
	}

	return src;
}