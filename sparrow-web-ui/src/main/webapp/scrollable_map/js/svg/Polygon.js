/**
 * @author johnhollister
 * 
 * @augments	JMap.svg.Path
 * @constructor
 */
JMap.svg.Polygon = function(params) {
    JMap.svg.Path.call(this, params);
    this.points = params.points;
}

JMap.svg.Polygon.prototype = new JMap.svg.Path();

JMap.svg.Polygon.prototype.createSVG = function() {
    if (!this.SVGManager.isIE) {
        this.shape = document.createElementNS('http://www.w3.org/2000/svg','path');
    } else {
        this.shape = document.createElement('v:shape');   
        this.shape.setAttribute('stroked',true);
        
        //unless otherwise specified... or not
        this.shape.setAttribute('filled',true); 
        
        
        this.shape.style.height = (3 * this.SVGManager.map.viewportHeight) + 'px';
        this.shape.style.width = (3 * this.SVGManager.map.viewportWidth) + 'px';
        this.shape.style.left = (-this.SVGManager.map.viewportWidth + 'px');
        this.shape.style.top = (-this.SVGManager.map.viewportHeight + 'px');
        this.shape.setAttribute('coordorigin',(-this.SVGManager.map.viewportWidth) + ',' + -this.SVGManager.map.viewportHeight);
        this.shape.setAttribute('coordsize',(3 * this.SVGManager.map.viewportWidth) + ',' + (3 * this.SVGManager.map.viewportHeight));
    }
    
    this.draw();
    JMap.svg.Path.prototype.createSVG.call(this);
    
    this.SVGManager.canvas.appendChild(this.shape);
}

JMap.svg.Polygon.prototype.draw = function() {
	var translatedPoints = this.translatePointsToMapSpace(this.points);
	if (!this.SVGManager.isIE) {
		this.shape.setAttribute('d', 'M' + translatedPoints[0] + ',' + translatedPoints[1] + ' L' + translatedPoints.join(' ') + ' Z');
	} else {
        this.shape.setAttribute('path', 'm ' + translatedPoints[0] + ',' + translatedPoints[1] + ' l ' + translatedPoints.join() + ' x e');
	}
}