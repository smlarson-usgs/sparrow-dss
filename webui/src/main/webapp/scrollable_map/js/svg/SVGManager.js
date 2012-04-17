
document.write('<!--[if IE]> '+
		'<xml:namespace ns="urn:schemas-microsoft-com:vml" prefix="v"/>' +
		'<style type="text/css">' + 
		'v\\:* { behavior: url(#default#VML);}' + 
		'</style>     ' + 
'<![endif]-->  ');

JMap.svg.SVGManager = function(map) {
	this.isIE = (navigator.appName == 'Microsoft Internet Explorer');

	this.propertyEnum = [];
	if (this.isIE) {
		this.propertyEnum['stroke'] = 'strokecolor';
		this.propertyEnum['fill'] = 'fillcolor';
		this.propertyEnum['stroke-width'] = 'strokeweight';
	} else {
		this.propertyEnum['stroke'] = 'stroke';
		this.propertyEnum['fill'] = 'fill';
		this.propertyEnum['stroke-width'] = 'stroke-width';          
	}

	this.map = map;
	this.x = -this.map.viewportWidth;
	this.y = -this.map.viewportHeight;
	this.shapes = [];
	this._createHTML();
}

JMap.svg.SVGManager.prototype._createHTML = function() {

	this.width = this.map.viewportWidth * 3;
	this.height = this.map.viewportHeight * 3;

	if (!this.isIE) {
		this.canvas = document.createElementNS('http://www.w3.org/2000/svg', 'svg');
		this.canvas.setAttribute('width', (this.width+1));
		this.canvas.setAttribute('height', (this.height+1));
		this.canvas.setAttribute('viewBox', (this.x - 0.5) + ' ' + (this.y - 0.5) + ' ' + (this.width + 1) + ' ' + (this.height + 1));
		this.canvas.setAttribute('preserveAspectRatio',"xMidYMid slice");
		this.canvas.style.position = 'absolute';
		this.canvas.style.top = this.y + 'px';
		this.canvas.style.left = this.x + 'px';
	} else {

		this.canvas = document.createElement('v:group');
		this.canvas.style.width = this.width + 'px';
		this.canvas.style.height = this.height + 'px';
		this.canvas.style.top = this.y + 'px';
		this.canvas.style.left = this.x + 'px';
		this.canvas.style.position = 'absolute';
		this.canvas.setAttribute('coordsize',this.width + ' ' + this.height);
		this.canvas.setAttribute('coordorigin', this.x + ', ' + this.y);

	}
	this.map.pane.appendChild(this.canvas); 

	this.draw();
}



JMap.svg.SVGManager.prototype.update = function() {
	
	this.width = this.map.viewportWidth * 3;
	this.height = this.map.viewportHeight * 3;
	
	var canvasTop = parseInt(this.canvas.style.top.split('px')[0],10);
	var canvasLeft = parseInt(this.canvas.style.left.split('px')[0],10);

	this.x -= (canvasLeft + this.map.viewportWidth);
	this.y -= (canvasTop + this.map.viewportHeight);
	
	this.canvas.style.visibility = 'hidden';

	if (!this.isIE) {
		this.canvas.setAttribute('viewBox',(this.x-0.5) + ' ' + (this.y-0.5) + ' ' + (this.width+1) + ' ' + (this.height+1));
		this.canvas.setAttribute('width', (this.width+1));
		this.canvas.setAttribute('height', (this.height+1));
	} else {
		
		this.canvas.setAttribute('coordorigin', this.x + ', ' + this.y);
		this.canvas.setAttribute('coordsize',this.width + ' ' + this.height);
		this.canvas.style.width = this.width + 'px';
		this.canvas.style.height = this.height + 'px';
	}

	this.canvas.style.top = -this.map.viewportHeight + 'px';
	this.canvas.style.left = -this.map.viewportWidth + 'px';
	this.canvas.style.visibility = 'visible';

	
}

JMap.svg.SVGManager.prototype.updateOnZoom = function() {

	this.x = -this.map.viewportWidth;
	this.y = -this.map.viewportHeight;
	
	this.canvas.style.visibility = 'hidden';

	if (!this.isIE) {
		this.canvas.setAttribute('viewBox',(this.x-0.5) + ' ' + (this.y-0.5) + ' ' + (this.width+1) + ' ' + (this.height+1));
	} else {
		this.canvas.setAttribute('coordorigin', this.x + ', ' + this.y);
		this.canvas.setAttribute('coordsize',this.width + ' ' + this.height);
	}

	this.canvas.style.top = -this.map.viewportHeight + 'px';
	this.canvas.style.left = -this.map.viewportWidth + 'px';
	this.draw();
	this.canvas.style.visibility = 'visible';

}


JMap.svg.SVGManager.prototype.draw = function() {
	//redraw all points
	for (var i = 0; i < this.shapes.length; i++) {
		this.shapes[i].draw();
	}
}


JMap.svg.SVGManager.prototype.moveBy = function(dx, dy) {
	var canvasTop = parseInt(this.canvas.style.top.split('px')[0],10);
	var canvasLeft = parseInt(this.canvas.style.left.split('px')[0],10);

	this.canvas.style.top = (canvasTop + dy) + 'px';
	this.canvas.style.left = (canvasLeft + dx) + 'px';

}

JMap.svg.SVGManager.prototype.addShape = function(shape) {
	shape.SVGManager = this;
	this.shapes.push(shape);
	shape.createSVG();

}

JMap.svg.SVGManager.prototype.removeShape = function(shape) {
	for (var i = 0; i < this.shapes.length; i++) {
		if (this.shapes[i] == shape) {
			this.shapes.splice(i,1);
			break;
		}
	}
	shape.kill();
}


JMap.svg.SVGManager.prototype.removeAllShapes = function() {
	for (var i = 0; i < this.shapes.length; i++) {
		this.shapes[i].kill();
	}
	this.shapes = [];
}


JMap.svg.SVGManager.prototype.kill = function() {


}