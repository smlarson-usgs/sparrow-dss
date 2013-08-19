/**
 * @constructor
 */

JMap.hud.ScaleRake = function(pHUD) {
	this.HUD = pHUD;
	this._createHTML();
	this.update();
}

JMap.hud.ScaleRake.prototype._createHTML = function() {

	var sb = document.createElement("div");
	sb.className = 'hud-scale-bars';
	this.HUDScaleRakeDiv = sb;


	var sbm = document.createElement('div');
	sbm.className = 'hud-scale-bar-mi'
	sb.appendChild(sbm);
	this.HUDScaleRakeMI = sbm;


	var sbk = document.createElement('div');
	sbk.className = 'hud-scale-bar-km';
	sb.appendChild(sbk);
	this.HUDScaleRakeKM = sbk;

	this.HUD.map.mapViewport.appendChild(this.HUDScaleRakeDiv);
}

JMap.hud.ScaleRake.prototype.update = function() {
	this.scale('mi');
	this.scale('km');
}



JMap.hud.ScaleRake.prototype.scale = function(units) {

	//var vpbbox = this.HUD.map.getViewportBoundingBox();
	//var baseScaleLat = vpbbox.ymin;
	var mapCoordY = this.HUD.map.getMapCoordsInPixelSpace().y;
	var baseScaleLat = this.HUD.map.projection.getLatFromY(mapCoordY + (this.HUD.map.viewportHeight / 2));
	
	var degPerPxLon = ((360 / this.HUD.map.tileSize) / this.HUD.map.projection.getTilesPerMapX());

	var unitFactor = 3963.0;
	var distanceUnit = " mi.";
	if (units == 'km') {
		unitFactor = 6377.0;
		distanceUnit = " km.";
	}

	//do scale stuff
	var rad = 180/Math.PI;
	var barInDeg = 60 * degPerPxLon;	//relative to a 60 px wide bar.
	var scale = unitFactor * Math.acos(Math.sin(baseScaleLat/rad) * Math.sin(baseScaleLat/rad) + 
			Math.cos(baseScaleLat/rad) * Math.cos(baseScaleLat/rad) * 
			Math.cos((barInDeg/rad)-(0/rad)));

	var barScale = 500;                      

	//format scale
	if (scale >= 5000) {
		barScale = 10000;
	} else if (scale >= 2500) {
		barScale = 5000;
	} else if (scale >= 1000) {
		barScale = 2500;
	} else if (scale >= 500) {
		barScale = 1000;
	} else if (scale >= 250) {
		barScale = 500;
	} else if (scale >= 100) {
		barScale = 250;
	} else if (scale >= 50) {
		barScale = 100;
	} else if (scale >= 25) {
		barScale = 50;
	} else if (scale >= 10) {
		barScale = 25;
	} else if (scale >= 5) {
		barScale = 10;
	} else if (scale >= 2) {
		barScale = 5;
	} else if (scale >= 1) {
		barScale = 2;
	} else if (scale >= 0.5) {
		barScale = 1;
	} else {
		if (units == 'mi') {
			distanceUnit = " ft.";
		} else {
			distanceUnit = " m.";
		}
		if (scale >= 0.2) {
			barScale = 0.5;
		} else if (scale >= 0.1) {
			barScale = 0.25;
		} else if (scale >= 0.05) {
			barScale = 0.1;
		} else if (scale >= 0.025) {
			barScale = 0.05;
		} else if (scale >= 0.01) {
			barScale = 0.025;
		} else if (scale >= 0.005) {
			barScale = 0.01;		
		} else if (scale >= 0.0025) {
			barScale = 0.005;		
		} else {
			barScale = 0.005;
		}
	}

	var barLength = rad * (Math.acos((Math.cos(barScale/unitFactor) - (Math.sin(baseScaleLat/rad)*Math.sin(baseScaleLat/rad)))/
			(Math.cos(baseScaleLat/rad)*Math.cos(baseScaleLat/rad))))/degPerPxLon;

	if (distanceUnit == " ft.") {
		barScale *= 5280;
	}
	if (distanceUnit == " m.") {
		barScale *= 1000;
	}

	
	if (units == 'mi') {
		this.HUDScaleRakeMI.innerHTML = parseInt(barScale,10) + "" + distanceUnit;
		this.HUDScaleRakeMI.style.width = (Math.floor(barLength) || '0') + 'px';
	} else {
		this.HUDScaleRakeKM.innerHTML = parseInt(barScale,10) + "" + distanceUnit;
		this.HUDScaleRakeKM.style.width = (Math.floor(barLength) || '0') + 'px';
	}
}


/**
 * @deconstructor
 */
JMap.hud.ScaleRake.prototype.kill = function() {

	this.HUD.map.mapViewport.removeChild(this.HUDScaleRakeDiv);
	this.HUDScaleRakeDiv = null;

}
