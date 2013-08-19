/**
 * @author johnhollister
 * 
 * @param {object} params - an object with the following members
 * 
 * @constructor
 */
JMap.svg.Path = function(params) {
	if (params != undefined) {
		this.properties = params.properties; //properties object
		this.events = params.events;
	}
}


JMap.svg.Path.prototype.createSVG = function() {

    var _this = this;
        
    //apply other properties and events
    for (var x in this.properties) {
        if (this.SVGManager.propertyEnum[x]) {
            //VML and SVG property names are different
            this.shape.setAttribute(this.SVGManager.propertyEnum[x], this.properties[x]);

        } else {
            //VML and SVG are the same
            this.shape.setAttribute(x, this.properties[x]);
        }
    }

    if (this.events) {
        if (this.events.onmouseover) this.shape.onmouseover = function(e) {
        	_this.events.onmouseover(_this); 
        	return false
        };
        if (this.events.onmouseout) this.shape.onmouseout = function(e) { 
        	_this.events.onmouseout(_this); 
        	return false
        };
        if (this.events.onmousemove) this.shape.onmousemove = function(e) { 
        	_this.events.onmousemove(_this); 
        	return false
        };
        if (this.events.onmousedown) this.shape.onmousedown = function(e) { 
        	_this.events.onmousedown(_this); 
        	return false
        };
        if (this.events.onmouseup) this.shape.onmouseup = function(e) { 
        	_this.events.onmouseup(_this); 
        	return false;
        };
        if (this.events.onclick) this.shape.onclick = function(e) { 
        	_this.events.onclick(_this); 
        	return false;
        };
        if (this.events.ondblclick) this.shape.ondblclick = function(e) { _this.events.ondblclick(_this); return false;};
    }
    
    //handle IE VML diffs from SVG properties
    if (this.SVGManager.isIE && this.properties) {
        var stroke = document.createElement('v:stroke');
        if (this.properties['fill-opacity']) {
            var fill = document.createElement('v:fill');
            fill.setAttribute('opacity', (this.properties['fill-opacity']*100) + '%');
            this.shape.appendChild(fill);
        }
        if (this.properties['stroke-opacity']) {
            stroke.setAttribute('opacity', (this.properties['stroke-opacity']*100) + '%');
        }
        if (this.properties['stroke-linejoin']) {
            stroke.setAttribute('joinstyle', this.properties['stroke-linejoin']);
        }
        if (this.properties['stroke-linecap']) {
            stroke.setAttribute('endcap', this.properties['stroke-linecap']);
        }

        this.shape.appendChild(stroke);
    }
}

JMap.svg.Path.prototype.draw = function() {
	//dummy function
}

JMap.svg.Path.prototype.translatePointsToMapSpace = function(points) {
	var translatedPoints = [];
	for (var i = 0; i < points.length; i+=2) {
		var coords = this.SVGManager.map.getPixelFromLatLon(points[i], points[i+1]);
		var txCoords = this.SVGManager.map.transformPixelToScreenSpace(coords.x, coords.y);
		translatedPoints.push(Math.floor(txCoords.x + (this.SVGManager.x + this.SVGManager.map.viewportWidth)));
		translatedPoints.push(Math.floor(txCoords.y + (this.SVGManager.y + this.SVGManager.map.viewportHeight)));
	}	
	return translatedPoints;
}

JMap.svg.Path.prototype.kill = function() {
	this.shape.onmouseover = null;
	this.shape.onmouseout = null;
	this.shape.onmousemove = null;
	this.shape.onmouseup = null;
	this.shape.onmousedown = null;
	this.shape.onclick = null;
	this.shape.ondblclick = null;
	this.SVGManager.canvas.removeChild(this.shape);
	this.SVGManager = null;
}