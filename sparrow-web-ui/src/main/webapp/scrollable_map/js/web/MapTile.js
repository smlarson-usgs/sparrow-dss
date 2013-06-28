/**
 * MapTile class represents one tile of one layer in a map.
 *
 *@param layer the layer this map tile belongs to
 *@param initX initial x pixel location for this tile
 *@param initY initial y pixel location for this tile
 *
 *@constructor
 */
JMap.web.MapTile = function(params) {

	this.layer = params.layer;
	this.initX = params.initX;
	this.initY = params.initY;

	//get inital x, y coordinates of tile in reference to overall map
	this.x = (this.initX + this.layer.map.x) % this.layer.map.getTilesPerMapX();
	this.y = this.initY + this.layer.map.y;


	//calculate width and height of tile.
	this.height = this.layer.map.tileSize - (this.layer.overlapY * -2);
	this.width = this.layer.map.tileSize - (this.layer.overlapX * -2);

	this._createHTML();
	
}

/**
 * create and insert html for this map tile
 */
JMap.web.MapTile.prototype._createHTML = function() {
	this.img = document.createElement('img');
	this.img.src = this.layer.map.blankImage.src;
	this.img.className = 'map-tile';
	this.img.style.width = this.width + 'px';
	this.img.style.height = this.height + 'px';
	this.img.style.left = (this.layer.map.tileSize * this.initX) - this.layer.overlapX + 'px';
	this.img.style.top = (this.layer.map.tileSize * (this.layer.map.numTilesY - 1 - this.initY)) - this.layer.overlapY + 'px';
	this.layer.mapLayer.appendChild(this.img);
}

/**
 * shift the map tile by dx * tile height and dy * tile height pixels.  should only be called on 
 * map snapback, that's why drawTile() is called in this method.
 *
 *@param dx  number of map tile widths to move this tile in the x direction
 *@param dy  number of map tile heights to move this tile in the y direction
 */
JMap.web.MapTile.prototype.moveBy = function(dx, dy) {

	//calculate new map tile pixel position
	var curTileX = parseInt(this.img.style.left.split("px")[0],10) + this.layer.overlapX;
	var curTileY = parseInt(this.img.style.top.split("px")[0],10) + this.layer.overlapY;
	var newTileX = (curTileX + (dx * this.layer.map.tileSize));
	var newTileY = (curTileY + (dy * this.layer.map.tileSize));

	//test pixel location against max and min allowable x/y pixel location thresholds
	//cycles the map tile around to the other side of the map if over a threshold
	if (newTileX >= (this.layer.map.tileSize * this.layer.map.numTilesX)) {
		newTileX = 0;
		var tx = (this.layer.map.x - dx) % this.layer.map.getTilesPerMapX();
		if (tx < 0) { 
			tx = this.layer.map.getTilesPerMapX() - dx; 
		}  
		
		this.x -= this.layer.map.numTilesX;
		if (!this.layer.map.noLoop) {
			if (this.x < 0) {
				this.x = tx;
			}
		}
		this.draw();
	} else if (newTileX <= -(this.layer.map.tileSize)) {  
		newTileX = this.layer.map.tileSize * (this.layer.map.numTilesX - 1);
		this.x += this.layer.map.numTilesX;
		if (!this.layer.map.noLoop) {
			if (this.x >= this.layer.map.getTilesPerMapX()) {
				this.x = ((this.layer.map.x - (this.layer.map.getTilesPerMapX())) + (this.layer.map.numTilesX));
				this.x = this.x % this.layer.map.getTilesPerMapX();
			}
		}
		this.draw();
	} else if (newTileY >= (this.layer.map.tileSize * this.layer.map.numTilesY)) {
		newTileY = 0;
		this.y += +this.layer.map.numTilesY;
		this.draw();
	} else if (newTileY <= -(this.layer.map.tileSize)) {
		newTileY = this.layer.map.tileSize * (this.layer.map.numTilesY - 1);
		this.y -= +this.layer.map.numTilesY;    
		this.draw();
	}
	//set the new pixel location
	this.img.style.left = newTileX - this.layer.overlapX + "px";
	this.img.style.top = newTileY - this.layer.overlapY + "px";
}

/**
 * performs math to sync the coordinates of the map tile with current map view
 */
JMap.web.MapTile.prototype.syncWithMapCoordinates = function() {
	//get current pixel x, y coords
	this.img.style.left = parseInt((this.layer.map.tileSize * this.initX) - this.layer.overlapX,10) + 'px';
	this.img.style.top = parseInt((this.layer.map.tileSize * (this.layer.map.numTilesY - 1 - this.initY)) - this.layer.overlapY,10) + 'px';

	//get the new coordinates for the tile in map-coordinate space
	this.x = this.initX + this.layer.map.x;
	this.y = this.initY + this.layer.map.y;

	//make sure to cycle the tiles around 180/-180 degrees longitude
	if (this.x >= this.layer.map.getTilesPerMapX()) {
		this.x -= this.layer.map.getTilesPerMapX();
	}
	this.x = this.x % this.layer.map.getTilesPerMapX();
}



/**
 * clear a tile by setting its src to a blank gif image.
 */
JMap.web.MapTile.prototype.clear = function() {
	this.img.src = 'scrollable_map/images/blank.gif';//this.layer.map.blankImage.src;
}


/**
 * draw a tile using its base url
 */
JMap.web.MapTile.prototype.draw = function() {
	
	this.tempX = this.x;
	if (this.layer.map.noLoop) {
		if (this.x >= this.layer.map.getTilesPerMapX()) {
			this.tempX = ((this.layer.map.x - (this.layer.map.getTilesPerMapX())) + (this.layer.map.numTilesX));    
		} else if (this.x < 0) {
			this.tempX = this.layer.map.getTilesPerMapX() + this.x;
		}
	}
	
	if (!(this.layer.map.isZooming || this.layer.map.isMoving || this.layer.map.isJumping || this.layer.map.dontDraw)) {
		this.clear();
		
		if (this.y >= 0 && this.y < this.layer.map.getTilesPerMapY()) {
			
			var src = this.layer.getSourceURL(this.tempX, this.y);
									
			//load the tile
			this.layer.map.tileManager.loadTile(this, src);

		} else {
			//outside of valid lat/lon range. draw blank image			
			this.layer.map.tileManager.loadBlankTile(this);

		}
	}
}



/**
 * kill this map tile object
 */
JMap.web.MapTile.prototype.kill = function() {
	this.layer.mapLayer.removeChild(this.img);
	this.img = null;
	this.layer = null;
	this.initX = null;
	this.initY = null;
	this.x = null;
	this.y = null;
}


