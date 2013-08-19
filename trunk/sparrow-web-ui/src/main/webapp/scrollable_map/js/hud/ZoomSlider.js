JMap.hud.ZoomSlider = function(pHUD, hideSlider) {
	this.HUD = pHUD;

	this.maxZoom = this.HUD.map.maxZoom + 1;
	this.minZoom = this.HUD.map.minZoom;
	this.currentZoom = this.HUD.map.zoom;

	this.hideSlider = hideSlider;
	
	this._createHTML();
	
	this.update();

}

JMap.hud.ZoomSlider.prototype._createHTML = function() {

	var _this = this;

	this.zoomIn = document.createElement('div');
	this.zoomIn.className = 'hud-zoom-in-button';
	this.zoomIn.onclick = function() { _this.HUD.map.zoomIn(1); };

	this.zoomOut = document.createElement('div');
	this.zoomOut.className = 'hud-zoom-out-button';  
	this.zoomOut.onclick = function() { _this.HUD.map.zoomOut(1); };
	if (this.hideSlider) {
		this.zoomOut.style.top = '25px';
	} else {
		this.zoomOut.style.top = ((this.maxZoom - this.minZoom) * 10) + 30 + 'px';
	}

	this.rail = document.createElement('div');
	this.rail.className = 'hud-slider-rail';


	this.railTop = document.createElement('div');
	this.railTop.className = 'hud-rail-top';

	this.railMid = document.createElement('div');
	this.railMid.className = 'hud-rail-mid';
	this.railMid.style.height = ((this.maxZoom - this.minZoom) * 10) + 'px';
	this.railMid.onmousedown = function(event) { _this.jumpTo(event); };

	this.railBottom = document.createElement('div');
	this.railBottom.className = 'hud-rail-bottom';

	this.sliderHandle = document.createElement('div');
	this.sliderHandle.className = 'hud-slider-handle';
	this.sliderHandle.style.top = (((this.maxZoom - this.currentZoom) - 1) * 10) + 2 + 'px';
	this.sliderHandle.onmousedown = function(event) { _this._startDrag(event); };


	this.rail.appendChild(this.railTop);
	this.rail.appendChild(this.railMid);
	this.rail.appendChild(this.railBottom);
	this.rail.appendChild(this.sliderHandle);

	this.HUD.map.mapViewport.appendChild(this.zoomIn);
	if (!this.hideSlider) {
		this.HUD.map.mapViewport.appendChild(this.rail);
	}
	this.HUD.map.mapViewport.appendChild(this.zoomOut);
}




//this method handles the event when the slider handle is clicked down on
JMap.hud.ZoomSlider.prototype._startDrag = function(event) {
	event = event||window.event;
	var _this = this;
	this.lastMouseY = event.clientY;
	this.curMouseY = this.lastMouseY;

//	set event handlers to update the zoom slider handle's position
	document.onmousemove = function(event) { _this._drag(event); };
	document.onmouseup = function() { _this._endDrag(); };
	return false;
}


//when the user drags the mouse while selecting the zoom slider handle
JMap.hud.ZoomSlider.prototype._drag = function(event) {
	event = event||window.event;
	this.curMouseY = event.clientY;
	var dy = this.curMouseY - this.lastMouseY;
	this.moveBy(dy);
	this.lastMouseY = this.curMouseY;
	JMap.util.unselect(event);

	return false;
}


//user releases the zoom slider handle
JMap.hud.ZoomSlider.prototype._endDrag = function() {

	var _this = this;
	var oldLevel = this.level;
	this.y = this.ypos;
	var ypos = this.ypos;
	var newLevel = Math.floor((((this.maxZoom - 1) * 10) - ypos) / 10);
	if (oldLevel != newLevel) {
		var n = newLevel - oldLevel;
		this.level = newLevel;  
		//do zoom
		if (n > 0) {
			this.HUD.map.zoomIn(n);
		} else if (n < 0) {
			this.HUD.map.zoomOut(-n);
		}    
	}
	document.onmousemove = null;
	document.onmouseup = null;
	return false;
}



//when the user clicks on the slider RAIL, the handle will "jump" to that zoom level
JMap.hud.ZoomSlider.prototype.jumpTo = function(event) {
	event = event||window.event;
	var coords = JMap.util.getRelativeCoords(event, this.railMid) 
	this.moveTo(coords.y - 5);
	return false;
}


//move the slider handle by dy pixels
JMap.hud.ZoomSlider.prototype.moveBy = function(dy) {
	this.y += dy;
	if (this.y >= 0 && this.y <= (((this.maxZoom - this.minZoom) - 1) * 10)) { 
		this.ypos = Math.floor((this.y + 3) / 10) * 10;
		if (this.ypos < 0) this.ypos = 0;
		if (this.ypos > (((this.maxZoom - this.minZoom) - 1) * 10)) this.ypos = ((this.maxZoom - this.minZoom - 1) * 10);
		this.sliderHandle.style.top = (this.ypos + 2) + "px";
	} else {
		if (this.y < 0) {
			this.sliderHandle.style.top = '2px';
		} else {
			this.sliderHandle.style.top = ((this.maxZoom - this.minZoom - 1) * 10) + 2 + 'px';
		}
	}
}


//move the slider to the yth pixel relative to its parent element
JMap.hud.ZoomSlider.prototype.moveTo = function(y) {
	this.y = y;
	this.ypos = Math.floor((this.y + 3) / 10) * 10;
	this.sliderHandle.style.top = (this.ypos + 2) + "px";
	this._endDrag(); 
}

JMap.hud.ZoomSlider.prototype.update = function() {
	this.level = this.HUD.map.zoom;
	this.y = ((((this.maxZoom) - 1) - this.level) * 10);
	this.ypos = Math.floor((this.y + 3) / 10) * 10;
	this.sliderHandle.style.top = (this.ypos + 2) + "px";
}

JMap.hud.ZoomSlider.prototype.kill = function() {
	this.level = null;
	this.y = null;
	this.ypos = null;

	this.railMid.onmousedown = null;
	this.railMid = null;

	this.sliderHandle.onmousedown = null;
	this.sliderHandle = null;

	this.HUD.map.mapViewport.removeChild(this.zoomIn);
	if (!this.hideSlider) {
		this.HUD.map.mapViewport.removeChild(this.rail);
	}
	this.HUD.map.mapViewport.removeChild(this.zoomOut);
}
