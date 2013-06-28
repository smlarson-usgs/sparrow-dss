/**
 * Updated to add a callback when a file is loaded to define layers.
 * This is required so that there is a way to know when it is safe to ask the
 * LayerManager for a current set of layers.
 * 
 */
JMap.web.MapServicesFile = function(params) {
	this.url = params.url;
	this.name = params.name;
	this.isOnByDefault = params.isOnByDefault;
	if (this.isOnByDefault == undefined) {
		this.isOnByDefault = true;
	}
	this.isHiddenFromUser = params.isHiddenFromUser;
	this.onLoad = params.onLoad;
	
	//Somehow we need a way to know that the layers have been loaded if we
	//want to be able to work with layers that are known to be current.
	this.onPostLoad = params.onPostLoad;
	var _this = this;
	
	new JMap.util.AjaxRequest({
		method: 'GET',
		url: this.url,
		paramString: 'nocache=' + (new Date()).getTime(),
		onData: function(arg) { _this._fileLoaded(arg); }
	});
}


JMap.web.MapServicesFile.prototype._fileLoaded = function(req) {
	var _this = this;
	var xml = req.getResponseXML();
	req = null;
	this.onLoad(xml, _this);
	
	if (this.onPostLoad) {
		this.onPostLoad(xml, _this);
	}
}