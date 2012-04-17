/**
 * Generally for use with non IE browsers, or browsers that don't cache
 * images very well.  I find that if I use this hash in Firefox, memory
 * usage doesn't get out of control as fast.
 *
 * John Hollister 2007
 */
JMap.web.tile.TileCache = function(manager) {
	this.cache = {};
	this.tempCache = {};
	this.manager = manager;
}


/** 
 * searches through the cache to see if the image is already there.
 * if it isn't, it adds it to the cache.
 *
 * @param src source of the tile to add to the cache
 * @return newly cached tile
 */

JMap.web.tile.TileCache.prototype.addTile = function(src, imageElement) {	

	var _this = this;
	this.manager.startLoadingImage();

	//problem if :80 port 80 is specified in URL.  browser automatically pulls it out

	var image = document.createElement('img');
	imageElement.loading = true;
	image.imageElement = imageElement;

	image.onload = function(event) { _this.onload(event, this); }
	//image.onerror = function(event) { _this.onerror(event, this); }
	//image.onabort = function(event) { _this.onabort(event, this); }

	image.src = src;
	this.tempCache[src] = image;
	return this.tempCache[src];
}


/**
 * getTile searches through the cache for a specified image by looking
 * at the src property.  It returns the image, if found, otherwise false.
 *
 * @param {String} src source of the image to find.
 * @return image if found, otherwise false
 */
JMap.web.tile.TileCache.prototype.getTile = function(src) {

	//problem if :80 port 80 is specified in URL.  browser automatically pulls it out
	if (this.cache[src]) {
		return this.cache[src];
	} else {
		return false;
	}
}


JMap.web.tile.TileCache.prototype.onerror = function(/*Event Object*/ event, /*Image Element*/ image) {

}


JMap.web.tile.TileCache.prototype.onabort = function(/*Event Object*/ event, /*Image Element*/ image) {

}


JMap.web.tile.TileCache.prototype.cancelAllLoads = function() {
	for (var i in this.cache) {
		if (this.cache[i] && this.cache[i].onload != null) {
			this.cache[i].onload = null;
			this.cache[i].imageElement = null;
			delete this.cache[i];
		}
	}
}


JMap.web.tile.TileCache.prototype.onload = function(event, image) {
	this.manager.endLoadingImage();
	this.cache[image.src] = this.tempCache[image.src];	
	image.imageElement.src = image.src;
	image.onload = null;
	image.imageElement.loading = null;
	delete this.tempCache[image.src];
}

/**
 * deconstructor
 */
JMap.web.tile.TileCache.prototype.kill = function() {
	for (var i in this.cache) {
		if (this.cache[i]) {
			this.cache[i].imageElement = null;
			this.cache[i].onload = null;
			this.cache[i].onerror = null;
			this.cache[i].onabort = null;
			delete this.cache[i];
		}
	}
	this.cache = null
}

