/**
 * this class keeps broken images from appearing on the map by first loading a
 * blank gif file to the map tile and only changing the src if and when an
 * actual map tile image is succesfully is loaded in to a javascript image object
 * I use this class for IE browsers as IE does a good job of caching images
 * 
 * @constructor
 */
JMap.web.tile.TileLoader = function(manager) {
	this.image = document.createElement('img');
	this.manager = manager;
}


/**
 * Loads an image into this instances image property.  Sets event handlers to 
 * put the image into the HTML after it completely loads.
 *
 * @param src    source of the image
 * @param imageElement html image element to display the image to load
 */
 JMap.web.tile.TileLoader.prototype.loadTile = function(/*String*/ src, /*Image element*/ imageElement) {
	var _this = this;

	this.manager.startLoadingImage();
	
	this.image.onload = null;
	this.image.imageElement = imageElement;
	this.image.onload = function(event) { _this.onload(event, this); }
	//this.image.onerror = function(event) { _this.onerror(event, this); }
	//this.image.onabort = function(event) { _this.onabort(event, this); }  
	this.image.src = src;

	return this.image;
}


/**
 * on image load, place the image into the html by setting the image element's
 * src to the loaded image source
 *
 * @param event    event object fired onload (not used here)
 * @param image    place holder image element that first loads the image before displaying it
 */
 JMap.web.tile.TileLoader.prototype.onload = function(/*Event Object*/ event, /*image element*/ image) {	
	
	this.manager.endLoadingImage();
	image.imageElement.src = image.src;
	image.imageElement.loading = null;
	image.onload = null;
}


 JMap.web.tile.TileLoader.prototype.onerror = function(event, image) {
 
}

 JMap.web.tile.TileLoader.prototype.onabort = function(event, image) {
 
}
 

/**
 * deconstructor
 */
 JMap.web.tile.TileLoader.prototype.kill = function() {
	if (this.image && this.image.onload) {
		this.image.onload = null;
	}
	this.image = null;
}
