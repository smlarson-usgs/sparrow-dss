/**
 * Tile Manager manages the loading of map tiles.  It can cache tiles or "pre" load the images.
 *
 * @constructor
 */
JMap.web.TileManager = function(params) {

	this.numImagesLoading = 0;


	this.map = params.map;
	this.cachingOn = params.cachingOn;
	this.onStartLoadingImages = params.onStartLoadingImages;
	this.onEndLoadingImages = params.onEndLoadingImages;

	if (this.cachingOn && navigator.appName.indexOf("Microsoft Internet Explorer") == -1) { //change to detect IE only
		this.tileCache = new JMap.web.tile.TileCache(this);
	}
}


JMap.web.TileManager.prototype.startLoadingImage = function() {

	if (this.imageLoadingTimeout) {
		clearTimeout(this.imageLoadingTimeout);
	}

	this.numImagesLoading++;
	if (this.numImagesLoading == 1) {
		this.onStartLoadingImages();
	}

	var _this = this;
	this.imageLoadingTimeout = setTimeout(function() {
		_this.numImagesLoading = 0;
		_this.onEndLoadingImages();
	}, 5000);

}

JMap.web.TileManager.prototype.endLoadingImage = function() {


	if (this.imageLoadingTimeout) {
		clearTimeout(this.imageLoadingTimeout);
	}

	this.numImagesLoading--;

	if (this.numImagesLoading <= 0) {
		this.numImagesLoading = 0;
		this.onEndLoadingImages();
	}

	var _this = this;
	this.imageLoadingTimeout = setTimeout(function() {
		_this.numImagesLoading = 0;
		_this.onEndLoadingImages();
	}, 5000);
}


/**
 * Load an image into a map tile.  This function determines whether or not caching
 * is being used
 *
 * @param {MapTile} tile	map tile to load image in
 * @param {String} src	url of the image to load in the map tile
 */
JMap.web.TileManager.prototype.loadTile = function(tile, src) {

	if (this.tileCache && tile.layer.map.cacheTiles && tile.layer.cacheLayer) {
		//do js image caching
		if (tile.cachedImage) {
			tile.cachedImage.onload = null;
		}
		//see if the image is in the cache
		var cachedTile = this.tileCache.getTile(src);
		if (!cachedTile) {
			//if the image isn't in the cache, cache it
			tile.cachedImage = this.tileCache.addTile(src, tile.img);
		} else {
			//the image was in the cache, set the src of this maptile
			tile.img.src = cachedTile.src;
		}
	} else {
		//run image through the image loader
		if (tile.tileLoader) {
			tile.tileLoader.kill();
		}
		tile.tileLoader = new  JMap.web.tile.TileLoader(this);
		tile.tileLoader.loadTile(src, tile.img);
	}
}


 /**
  *  cancels any currently loading tiles
  */
JMap.web.TileManager.prototype.cancelAllLoads = function() {
	if (this.tileCache) {
		this.tileCache.cancelAllLoads();
	}
	this.numImagesLoading = 0;
	this.endLoadingImage();
}



/**
 * Loads blank.gif into this tile
 *
 * @param {MapTile} tile	tile to load the blank image in.
 */
 JMap.web.TileManager.prototype.loadBlankTile = function(tile) {
	if (tile.cachedImage) {
		tile.cachedImage.onload = null;
	} else if (tile.tileLoader) {
		tile.tileLoader.kill();
	}
	tile.img.src = tile.layer.map.blankImage.src;
}


 /**
  * deconstructor
  */
 JMap.web.TileManager.prototype.kill = function() {
	if (this.tileCache) {
		this.tileCache.kill();
	}
 }