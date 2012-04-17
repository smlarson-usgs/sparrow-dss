/**
 *
 * load map layers from wms xml config files into map interface.  control
 * default toggling of map layers and when to make layers available/unavailable
 * to the interface.
 *
 * @constructor
 */
JMap.web.LayerManager = function(map) {
	this.map = map; 
	this.servicesFiles = [];

	//the full catalog of all map layers
	this.mapLayers = [];

	//map layers that are currently turned on
	this.activeMapLayers = [];

	//map layers that are selected, but can not be activateable due to being out of scale or bbox
	this.selectedNotAvailable = [];

}



/**
 * loads a map services file by creating an object which performs an ajax call to load the
 * file's contents as xml.
 * 
 * OVERRIDE to add a callback when a file is loaded to define layers.
 * This is required so that there is a way to know when it is safe to ask the
 * LayerManager for a current set of layers.
 * 
 * @param {JSON} params	JSON object specifying url, name, isHiddenFromUser, and isOnByDefault
 */
JMap.web.LayerManager.prototype.loadMapLayerServicesFile = function(params) {
	var _this = this;
	this.servicesFiles.push(
			new JMap.web.MapServicesFile({
				url: params.url,
				isHiddenFromUser: params.isHiddenFromUser,
				isOnByDefault: params.isOnByDefault,
				name: params.name,
				onLoad: function(arg1, arg2) { _this._loadLayersToMap(arg1, arg2); },
				onPostLoad: params.onPostLoad
			})
	);
}



JMap.web.LayerManager.prototype._loadLayersToMap = function(layersXML, servicesFile) {	
	if (layersXML) {

		var services = layersXML.getElementsByTagName('Service');

		for (var i = 0; i < services.length; i++) {
			var version = JMap.util.getNodeValue(services[i],'Version');
			var baseUrl = JMap.util.getNodeValue(services[i],'GetMapURL');   
			var serviceType = JMap.util.getNodeValue(services[i],'serviceType','wms').toLowerCase();
			var formatType = JMap.util.getNodeValue(services[i],'format','image/png');
			var request = JMap.util.getNodeValue(services[i],'request','GetMap');

			var layers = services[i].getElementsByTagName('Layer');

			for (var j = 0; j < layers.length; j++) {
				var dataSource = JMap.util.getNodeValue(layers[j],'datasource');
				var overlap = JMap.util.getNodeValue(layers[j],'overlapTiles', 0);
				var name = JMap.util.getNodeValue(layers[j],'Name');
				var title = JMap.util.getNodeValue(layers[j],'Title');
				var id = layers[j].getAttribute('id'); 
				var zDepth = layers[j].getElementsByTagName('Classification')[0].getAttribute('zdepth');
				var classTheme = JMap.util.getNodeValue(layers[j].getElementsByTagName('Classification')[0],'Theme');
				var className = JMap.util.getNodeValue(layers[j].getElementsByTagName('Classification')[0],'Name');
				var srs = JMap.util.getNodeValue(layers[j],'SRS');
				var scaleMin = JMap.util.getNodeValue(layers[j],'MinScale', 0);
				var scaleMax = JMap.util.getNodeValue(layers[j],'MaxScale', 10);
				var layerName = JMap.util.getNodeValue(layers[j],'Name');
				var re = RegExp("\\s","g");
				var layersUrlParam = layerName.replace(re,'+');

				var legendUrl = JMap.util.getNodeValue(layers[j],'LegendURL');    
				var metaUrl = JMap.util.getNodeValue(layers[j],'MetadataURL');         
				var opacity = JMap.util.getNodeValue(layers[j],'opacity',100);
				var boundingBox = JMap.util.getNodeValue(layers[j],'LatLon','-180,-90,180,90');
				var description = JMap.util.getNodeValue(layers[j], 'Description','No description');
				var sld = JMap.util.getNodeValue(layers[j], 'SLDURL');

				var isHidden = JMap.util.getNodeValue(layers[j],'hiddenFromUser',false);
				if (isHidden == 'false') {
					isHidden = false
				}
				var isDefault = JMap.util.getNodeValue(layers[j],'onByDefault', false);
				if (isDefault == 'false') {
					isDefault = false
				}
				var cacheLayer = JMap.util.getNodeValue(layers[j],'cacheLayer', true);
				if (cacheLayer == 'false') {
					cacheLayer = false
				}
				
				//test to make sure map doesn't already have/know about this layer
				if (!this.getMapLayer(id)) {

					var layerObj;
					var layerObjParams = {
						map: this.map,
						id: id,
						title: title,
						name: name,
						scaleMin: scaleMin,
						scaleMax: scaleMax,
						baseUrl: baseUrl,
						zDepth: zDepth,
						opacity: opacity,
						version: version,
						srs: srs,
						request: request,
						format: formatType,
						legendUrl: legendUrl,
						metaUrl: metaUrl,
						description: description,
						classTheme: classTheme,
						className: className,
						sld: sld,
						overlap: overlap,
						dataSource: dataSource,
						isHiddenFromUser: (servicesFile.isHiddenFromUser||!!isHidden),
						isOnByDefault: (servicesFile.isOnByDefault||!!isDefault),
						validBoundingBox: boundingBox,
						serviceType: serviceType,
						layersUrlParam: layersUrlParam,
						cacheLayer: cacheLayer
					};

					if (serviceType == 'tms') {
						layerObj = new JMap.web.mapLayer.TMSLayer(layerObjParams);
					} else if (serviceType == 'arctms') {
						layerObj = new JMap.web.mapLayer.ArcTMSLayer(layerObjParams);
					} else if (serviceType == 'mapcache') {
						layerObj = new JMap.web.mapLayer.MVMapCacheLayer(layerObjParams);
					} else if (serviceType == 'mvwms') {
						layerObj = new JMap.web.mapLayer.MVWMSLayer(layerObjParams);
					} else if (serviceType == 'basemap') {
						layerObj = new JMap.web.mapLayer.MVBaseMapLayer(layerObjParams);
					} else if (serviceType == 'google') {
						layerObj = new JMap.web.mapLayer.GoogleMapLayer(layerObjParams);
					} else {
						layerObj = new JMap.web.mapLayer.WMSLayer(layerObjParams);
					}
					
					this.loadMapLayer(layerObj);
					if (layerObj.isOnByDefault && this.isLayerAvailable(layerObj)) {

						//set default flag to false so the layer won't keep turning itself on if the user turns it off
						layerObj.isOnByDefault = false;
						this.appendLayerToMap(layerObj.id);
						if (this.map.onLayerAppend) {
							this.map.onLayerAppend(layerObj.id);
						}
					} else if (layerObj.isOnByDefault) {
						
						//layer is not available at current scale
						this.selectedNotAvailable.push(layerObj);
					}
				}
			} //j	
		} //i
	}
	layersXML = null;
}



/**
 * Adds a map layer to the map layers list.  You can now append this layer to the map.
 * 
 * @param {Layer} Layer object to make available to map.
 */
JMap.web.LayerManager.prototype.loadMapLayer = function(layer) {
	if (!this.getMapLayer(layer.id)) {
		this.mapLayers.push(layer);
	}
}



/**
 * Removes a map layer from the map layers list.  You can no longer append this layer to the map.
 * 
 * @param {String} id of the layer to destroy
 */
JMap.web.LayerManager.prototype.unloadMapLayer = function(layerId) {
	for (var i = 0; i < this.mapLayers.length; i++) {
		if (this.mapLayers[i].id == layerId) {
			var layer = this.mapLayers[i];
			this.removeLayerFromMap(layerId);
			this.mapLayers.splice(i, 1);
			layer.kill();
			break;
		}
	}
}



/**
 * Adds a layer (from the map layers list) to the map making the layer visible.
 * 
 * @param {String} layerId	id of the layer to add to the map.
 */
JMap.web.LayerManager.prototype.appendLayerToMap = function(layerId) {
	var layer = this.getMapLayer(layerId);
	if (layer && !this.getActiveLayer(layerId) && this.isLayerAvailable(layer)) {
		layer.activate();
		
		var inserted = false;
		for (var i = 0; i < this.activeMapLayers.length; i++) {		
			if (parseInt(layer.zDepth,10) > parseInt(this.activeMapLayers[i].zDepth,10)) {
				this.activeMapLayers.splice(i, 0, layer);
				inserted = true;
				break;
			}
		}  

		//If it's not already in the HTML, put it at the top
		if (!inserted) {
			this.activeMapLayers.push(layer);
		}
		
	} else if (layer && !this.getActiveLayer(layerId)) {
		this.selectedNotAvailable.push(layer);
	}
}


/**
 * Removes a layer from the map.
 * 
 * @param {String} layerId	id of the layer to remove from the map.
 */
JMap.web.LayerManager.prototype.removeLayerFromMap = function(layerId) {
	var layer = this.getMapLayer(layerId);
	if (layer && this.getActiveLayer(layer.id)) {
		layer.deactivate();

		//remove from active layers list
		for (var i = 0; i < this.activeMapLayers.length; i++) {
			if (this.activeMapLayers[i] == layer) {
				this.activeMapLayers.splice(i,1);
				break;
			}
		}
	} else {
		//remove from select not available layers list
		for (var i = 0; i < this.selectedNotAvailable.length; i++) {
			if (this.selectedNotAvailable[i] == layer) {
				this.selectedNotAvailable.splice(i,1);
				break;
			}
		}		
	}
}




/**
 * Return an available layer object.
 * 
 * @param {String} layerId	id of the layer.
 * @return Layer object otherwise null if does not exist.
 */
JMap.web.LayerManager.prototype.getMapLayer = function(layerId) {
	for (var i = 0; i < this.mapLayers.length; i++) {
		if (this.mapLayers[i].id == layerId) {
			return this.mapLayers[i];
		}
	}
	return null;
}


/**
 * Return an active (currently on the map) layer object.
 * 
 * @param {String} layerId	id of the layer.
 * @return Layer object otherwise null if does not exist.
 */
JMap.web.LayerManager.prototype.getActiveLayer = function(layerId) {
	for (var i = 0; i < this.activeMapLayers.length; i++) {
		if (this.activeMapLayers[i].id == layerId) {
			return this.activeMapLayers[i];
		}
	}
	return null;
}




/**
 * determines whether or not a layer can be "turned on" at the current map scale.
 * 
 * @param boolean indicating whether or not this layer is activateable.
 */
JMap.web.LayerManager.prototype.isLayerAvailable = function(layer) {
	
	var scale = this.map.getScale();
	if (scale >= layer.scaleMin && scale <= layer.scaleMax) {
		return true;
	}
	return false;
}



/**
 * get array list of all layers that are available within the current map scale.
 * 
 * @return a list of layers that can be viewed (activated) at this scale
 */
JMap.web.LayerManager.prototype.getAvailableLayers = function() {

	var available = [];
	for (var i = 0; i < this.mapLayers.length; i++) {
		if (this.isLayerAvailable(this.mapLayers[i])) {
			available.push(this.mapLayers[i]);    
		}
	}
	return available;
}



/**
 * 
 */
JMap.web.LayerManager.prototype.updateActiveMapLayers = function() {

	//look at current map layers and remove if they are out of their valid scale range (not available)
	for (var i = 0; i < this.activeMapLayers.length; i++) {
		var tLayer = this.activeMapLayers[i];
		if (!this.isLayerAvailable(tLayer)) {

			//remove from active layers
			this.removeLayerFromMap(tLayer.id);

			if (this.map.onLayerRemove) {
				this.map.onLayerRemove(tLayer.id);
			}
			
			//place into a "selected but not available" list
			this.selectedNotAvailable.push(tLayer);

			i--;
		}
	}


	//go through the selectedNotActivateable layers to see if some have become available again
	for (var i = 0; i < this.selectedNotAvailable.length; i++) {
		var tLayer = this.selectedNotAvailable[i];

		if (this.isLayerAvailable(tLayer)) {

			//remove from "selected but not available" list
			this.selectedNotAvailable.splice(i,1);
			
			//append to map
			this.appendLayerToMap(tLayer.id);
			
			if (this.map.onLayerAppend) {
				this.map.onLayerAppend(tLayer.id);
			}
			
			
			i--;
		}
	}	


	//check for default layers list to see if they need to be turned on
	for (var i = 0; i < this.mapLayers.length; i++) {
		var tLayer = this.mapLayers[i];
		if (tLayer.isOnByDefault && this.isLayerAvailable(tLayer)) {

			//set default flag to false so the layer won't keep turning itself on if the user turns it off
			tLayer.isOnByDefault = false;
			this.appendLayerToMap(tLayer);
			
			if (this.map.onLayerAppend) {
				this.map.onLayerAppend(tLayer.id);
			}
		}
	}
}



/**
 * Clears the map by setting all the map tiles' image source attributes to a blank gif. The HTML
 * remains on the page though.  This does not deconstruct the map.
 */
JMap.web.LayerManager.prototype.clearLayers = function() {
	for (var i = 0; i < this.activeMapLayers.length; i++) {
		this.activeMapLayers[i].clear();
	}
}


/**
 * Refreshes the map by resetting all the map tiles' source attributes to a map service URL.
 */
JMap.web.LayerManager.prototype.drawLayers = function() {
	for (var i = 0; i < this.activeMapLayers.length; i++) {
		this.activeMapLayers[i].draw();
	}
}



/**
 * syncs all active map layers with current map position
 */
JMap.web.LayerManager.prototype.syncWithMapCoordinates = function() {
	for (var i = 0; i < this.activeMapLayers.length; i++) {
		this.activeMapLayers[i].syncWithMapCoordinates();
	}
}



/**
 * deconstructor
 */
JMap.web.LayerManager.prototype.kill = function() {
	for (var i = 0; i < this.mapLayers.length; i++) {
		this.mapLayers[i].kill();
	}
}
