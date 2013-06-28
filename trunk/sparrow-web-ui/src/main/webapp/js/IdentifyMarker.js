function IdentifyMarker(params) {
	this.map = params.map;

	this.minLat = params.minLat;
	this.minLon = params.minLon;
	this.maxLat = params.maxLat;
	this.maxLon = params.maxLon;
	this.offsetYPx = 0;
	this.offsetXPx = 0;
}



IdentifyMarker.prototype = {
	_createHTML : function() {
		this.foi = document.createElement('div');
		this.foi.className = 'id-box-highlight';

		this.moveToLatLon();
		this.map.pane.appendChild(this.foi);
	},

	resetPositionOnZoom : function() {
		this.moveToLatLon();
	},

	moveBy : function(dx, dy) {
		var foiLeft = parseInt(this.foi.style.left, 10);
		var foiTop = parseInt(this.foi.style.top, 10);

		var newX = (foiLeft + dx);
		var newY = (foiTop + dy);

		this.foi.style.left = newX + 'px';
		this.foi.style.top = newY + 'px';
	},

	moveToPx : function(x, y) {
		this.foi.style.top = (y - this.offsetYPx) + 'px';
		this.foi.style.left = (x - this.offsetXPx) + 'px';
	},

	moveToLatLon : function() {

		var bboxTopLeftPx = this.map.transformLatLonToScreenSpace(this.maxLat, this.minLon);
		var bboxBotRightPx = this.map.transformLatLonToScreenSpace(this.minLat, this.maxLon);

		this.foi.style.width = bboxBotRightPx.x - bboxTopLeftPx.x + 'px';
		this.foi.style.height = bboxBotRightPx.y - bboxTopLeftPx.y + 'px';

		if (bboxTopLeftPx.x < 0) bboxTopLeftPx.x += this.map.getTilesPerMapX() * this.map.tileSize;

		this.moveToPx(bboxTopLeftPx.x, bboxTopLeftPx.y);
	},

	kill : function() {
		this.map.pane.removeChild(this.foi);
		this.map = null;
	}

}
