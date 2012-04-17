/**
 * @author johnhollister
 * 
 * @augments	JMap.svg.PolyLine
 * @constructor
 */
JMap.svg.Line = function(params) {
	var polyLineParams = {
		points: [params.startLon, params.startLat, params.endLon, params.endLat],
		properties: params.properties,
		events: params.events
	};
	
	JMap.svg.PolyLine.call(this, polyLineParams);
	this.properties.fill = 'none';
}

JMap.svg.Line.prototype = new JMap.svg.PolyLine();