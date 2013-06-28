
/**
 * this class handles all fois
 */


/**
 * @constructor
 */
JMap.foi.FOIManager = function(map) {	
	this.map = map;
	this.fois = [];
}


JMap.foi.FOIManager.prototype.updateOnMove = function() {
	//hide and show various fois based on viewport boundingbox
}


JMap.foi.FOIManager.prototype.updateOnZoom = function() {
	for (var i = 0; i < this.fois.length; i++) {
		this.fois[i].resetPositionOnZoom();
	}
}



JMap.foi.FOIManager.prototype.moveBy = function(dx, dy) {
	for (var i = 0; i < this.fois.length; i++) {
		this.fois[i].moveBy(dx, dy);
	}
}



/**
 * if two or more foi's are in same (or nearly the same) spot, explode them out so all fois 
 * are visible
 */
JMap.foi.FOIManager.prototype.explode = function() {
	
	
}



/**
 * add a foi object to the map
 * 
 * @param {FOI}	feature of interest object
 */
JMap.foi.FOIManager.prototype.addFOI = function(foi) {
	foi.map = this.map;
	foi.FOIManager = this;
	foi._createHTML();
	this.fois.push(foi);
}


/**
 * remove a foi object from the map
 * 
 * @param {FOI}	feature of interest object
 */
JMap.foi.FOIManager.prototype.removeFOI = function(foi) {
	for (var i = 0; i < this.fois.length; i++) {
		if (this.fois[i] == foi) {
			foi.kill();
			this.fois.splice(i,1);
			break;
		}
	}
}



/**
 * remove all foi objects from the map
 * 
 * @param {FOI}	feature of interest object
 */
JMap.foi.FOIManager.prototype.removeAllFOIs = function() {
	for (var i = 0; i < this.fois.length; i++) {
		this.removeFOI(this.fois[i]);
		i--;
	}
}







/**
 * hide a foi object from the map
 * 
 * @param {FOI}	feature of interest object
 */
JMap.foi.FOIManager.prototype.hideFOI = function(foi) {
	foi.hide();
}


/**
 * show a foi object from the map
 * 
 * @param {FOI}	feature of interest object
 */
JMap.foi.FOIManager.prototype.showFOI = function(foi) {
	foi.show();
}


JMap.foi.FOIManager.prototype.kill = function() {
	for (var i = 0; i < this.fois.length; i++) {
		this.fois[i].kill();
	}
	this.map = null;
}