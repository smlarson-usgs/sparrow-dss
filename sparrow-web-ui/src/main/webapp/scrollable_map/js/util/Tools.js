JMap.util.Tools.zoomOut = function(event, map) {
	var c = JMap.util.getRelativeCoords(event, map.pane);
	var clickLL = map.getLatLonFromScreenPixel(c.x, c.y);
	map.zoomOut();
	map.moveTo(clickLL.lat, clickLL.lon);
}
JMap.util.Tools.zoomOut.cursor = 'cursor-zoom-out';


JMap.util.Tools.zoomIn = function(event, map) {
	var zoomInBBox = new JMap.util.Tools.zoomIn._ZoomInBBox(event, map);
}
JMap.util.Tools.zoomIn.cursor = 'cursor-zoom-in';


/**
 * @namespace
 * @constructor
 */
JMap.util.Tools.zoomIn._ZoomInBBox = function(event, map) {
	this.map = map;
	var mapPixelCoords = JMap.util.getRelativeCoords(event, this.map.pane);

	var _this = this;
	this.startDragX = mapPixelCoords.x;
	this.startDragY = mapPixelCoords.y;

	document.onmousemove = function(event) { return _this._ZoomInDrag(event); }
	document.onmouseup = function(event) { return _this._ZoomInEndDrag(event); }

	this.bbox = document.createElement('div');
	this.bbox.className = 'tools-zoom-bbox';
	this.bbox.style.display = 'none';
	this.map.pane.appendChild(this.bbox);

}

JMap.util.Tools.zoomIn._ZoomInBBox.prototype._ZoomInDrag = function(event) {
	JMap.util.unselect();
	this.bbox.style.display = 'block';

	event = event || window.event;
	var mapScreenPixelCoords = JMap.util.getRelativeCoords(event, this.map.pane);

	var dx = mapScreenPixelCoords.x - this.startDragX;
	if (dx < 0) {
		dx = -dx;
		this.bbox.style.left = mapScreenPixelCoords.x + 'px';
	} else {
		this.bbox.style.left = this.startDragX + 'px';
	}
	this.bbox.style.width = dx + 'px';

	var dy = mapScreenPixelCoords.y - this.startDragY;
	if (dy < 0) {
		dy = -dy;
		this.bbox.style.top = mapScreenPixelCoords.y + 'px';
	} else {
		this.bbox.style.top = this.startDragY + 'px';
	}
	this.bbox.style.height = dy + 'px';

	return false;
}



JMap.util.Tools.zoomIn._ZoomInBBox.prototype._ZoomInEndDrag = function(event) {
	var mapCoords = this.map.getMapCoordsInPixelSpace();


	event = event || window.event;
	var mapPixelCoords = JMap.util.getRelativeCoords(event, this.map.pane);
	var dragBBoxWidth = parseInt(this.bbox.style.width.split('px')[0]);
	if (isNaN(dragBBoxWidth)) dragBBoxWidth = 0;
	var dragBBoxHeight = parseInt(this.bbox.style.height.split('px')[0]);
	if (isNaN(dragBBoxHeight)) dragBBoxHeight = 0;
	var dragBBoxTop = parseInt(this.bbox.style.top.split('px')[0]);
	if (dragBBoxTop < 0) dragBBoxTop = 0;
	var dragBBoxLeft = parseInt(this.bbox.style.left.split('px')[0]);
	if (dragBBoxLeft < 0) dragBBoxLeft = 0;
	var dragBBoxBottom = (dragBBoxTop + dragBBoxHeight);
	if (dragBBoxBottom > this.map.viewportHeight) dragBBoxBottom = this.map.viewportHeight;
	var dragBBoxRight = dragBBoxLeft + dragBBoxWidth;
	if (dragBBoxRight > this.map.viewportWidth) dragBBoxRight = this.map.viewportWidth;

	//if zoom box is too small, just zoom in
	if (dragBBoxHeight <= 10 || dragBBoxWidth <= 10) {
		this.map.pane.removeChild(this.bbox);
		var clickLL = this.map.getLatLonFromPixel(mapCoords.x + mapPixelCoords.x, mapCoords.y + (this.map.viewportHeight - mapPixelCoords.y));
		clickLL.lon = (((clickLL.lon + 180) % 360) - 180);

		this.map.moveTo(clickLL.lat, clickLL.lon);
		this.map.zoomIn();
	} else {
		var minLL = this.map.getLatLonFromPixel(mapCoords.x + dragBBoxLeft, mapCoords.y + (this.map.viewportHeight - dragBBoxBottom));
		var maxLL = this.map.getLatLonFromPixel(mapCoords.x + dragBBoxRight, mapCoords.y + (this.map.viewportHeight - dragBBoxTop));

		this.map.pane.removeChild(this.bbox);
		minLL.lon = (((minLL.lon + 180) % 360) - 180);
		maxLL.lon = (((maxLL.lon + 180) % 360) - 180);

		this.map.fitToBBox(minLL.lon, minLL.lat, maxLL.lon, maxLL.lat);
	}
	this.map = null;
	this.bbox = null;
	document.onmousemove = null;
	document.onmouseup = null;

	return false;
}


JMap.util.Tools.debug = function(event, map) {
	//console.log(map.getScale());
}
