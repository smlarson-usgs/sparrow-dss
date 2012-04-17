/**
 * @author johnhollister
 *
 * @augments	JMap.web.mapLayer.Layer
 * @constructor
 */
JMap.web.mapLayer.TMSLayer = function(params) {
	this.format = 'PNG';

	//call base class
	JMap.web.mapLayer.Layer.call(this,params);
}

//inherit prototype from Layer
JMap.web.mapLayer.TMSLayer.prototype = new JMap.web.mapLayer.Layer();

//redefine getSource method
JMap.web.mapLayer.TMSLayer.prototype.getSourceURL = function(x, y) {


	var tempBase = '';
	if (this.serverIndex != undefined) {
		this.serverIndex = (x * y) % this.baseUrls.length;
		tempBase = this.baseUrls[this.serverIndex];
	} else {
		tempBase = this.baseUrl;
	}


	var tx = JMap.util.pad(''+(x % this.map.getTilesPerMapX()), 9, '0', 1);
	var ty = JMap.util.pad(''+y, 9, '0', 1);

	var x10e8 = tx.substring(0,3);
	var x10e5  = tx.substring(3,6);
	var x10e2 = tx.substring(6,9);

	var y10e8 = ty.substring(0,3);
	var y10e5  = ty.substring(3,6);
	var y10e2 = ty.substring(6,9);


	var z = JMap.util.pad(''+this.map.zoom, 2, '0', 1);

	//http://c0.labs.metacarta.com/wms-c/cache/basic/03/000/000/001/000/000/000.png

	//var src = this.baseUrl + z  +  '/' + (x % this.map.getTilesPerMapX()) + '/' + y + '.' + this.format;
	var src = tempBase + z + '/' + x10e8 + '/' + x10e5 + '/' + x10e2 + '/' + y10e8 + '/' + y10e5 + '/' + y10e2 + '.' + this.format;

	return src;
}