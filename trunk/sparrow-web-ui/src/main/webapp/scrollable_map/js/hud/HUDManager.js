/**
 * Manages the componenets of the Heads Up Display (HUD). Components include: zoom slider,
 * scale rake, overview map, lat/lon label
 * 
 * @param {JSON} params
 * @constructor
 */
JMap.hud.HUDManager = function(map, params) {
	this.map = map;
	
	if (params) {
	
		if (params.scaleRake) {
			this._HUDScaleRake = new JMap.hud.ScaleRake(this);
		}
		
		if (params.overviewMap) {
			this._HUDOverviewMap = new JMap.hud.OverviewMap(this, params.overviewMap);
		}
		
		if (params.latLonLabel) {
			this._HUDLatLonLabel = new JMap.hud.LatLonLabel(this);
		}
		
		if (params.zoomSlider) {
			this._HUDZoomSlider = new JMap.hud.ZoomSlider(this);
		}
		
		if (params.zoomButtons) {
			this._HUDZoomSlider = new JMap.hud.ZoomSlider(this, true);
		}
	}
}


/**
 * Update all the components in the HUD to match the current state of the map.
 */
JMap.hud.HUDManager.prototype.update = function() {
	
	if (this._HUDScaleRake) {
		this._HUDScaleRake.update();
	}
	
	if (this._HUDOverviewMap) {
		this._HUDOverviewMap.update();
	}
	
	if (this._HUDZoomSlider) {
		this._HUDZoomSlider.update();
	}
}



JMap.hud.HUDManager.prototype.updateOnMouseMove = function(event) {
	event = event||window.event;
	if (this._HUDLatLonLabel) {
		this._HUDLatLonLabel.update(event);
	}
}

JMap.hud.HUDManager.prototype.updateOnMouseOut = function(event) {
	event = event||window.event;
	if (this._HUDLatLonLabel) {
		this._HUDLatLonLabel.clear();
	}
}


JMap.hud.HUDManager.prototype.updateOnZoom = function() {
	if (this._HUDScaleRake) {
		this._HUDScaleRake.update();
	}
	
	if (this._HUDOverviewMap) {
		this._HUDOverviewMap.update();
	}
	
	if (this._HUDZoomSlider) {
		this._HUDZoomSlider.update();
	}
}


JMap.hud.HUDManager.prototype.updateOnMapMoved = function() {
	
	if (this._HUDScaleRake) {
		this._HUDScaleRake.update();
	}
	
	if (this._HUDOverviewMap) {
		this._HUDOverviewMap.update();
	}

}


/**
 * 
 */
JMap.hud.HUDManager.prototype.redrawZoomSlider = function() {
	if (this._HUDZoomSlider && !this._HUDZoomSlider.hideSlider) {
		this._HUDZoomSlider.kill();
		this._HUDZoomSlider = new JMap.hud.ZoomSlider(this);
	}
}


/**
 * deconstructor
 */
JMap.hud.HUDManager.prototype.kill = function() {
	if (this._HUDScaleRake) {
		this._HUDScaleRake.kill();
	}
	
	if (this._HUDOverviewMap) {
		this._HUDOverviewMap.kill();
	}
	
	if (this._HUDLatLonLabel) {
		this._HUDLatLonLabel.kill();
	}
	
	if (this._HUDZoomSlider) {
		this._HUDZoomSlider.kill();
	}
}