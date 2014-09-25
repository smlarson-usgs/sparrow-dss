var model_id = (Sparrow.USGS.getURLParam("model") || 22); // 22 is the default model

/**
 * Class definition for context object, instances serve as event dispatchers.
 * Note that the Context holds all the data but has no methods so that it
 * can be serialized w/o including the flotsome of the methods.
 */
Sparrow.ux.Context = Ext.extend(Ext.util.Observable, {

    /** The PredictionContext data. */
    sessionTemplate: {
        PredictionContext : {
            "@xmlns": "http://www.usgs.gov/sparrow/prediction-schema/v0_2",
            "@xmlns:xsi": "http://www.w3.org/2001/XMLSchema-instance",
            "@model-id": model_id,
            adjustmentGroups : {
                "@conflicts" : "accumulate",
                reachGroup : [],
                individualGroup : {
                    "@enabled" : "true",
                    reach : []
                }
            },
            analysis : {
                dataSeries : {
                    "#text" : "total",
                    "@source": null
                },
                groupBy : {
                    "#text" : "",
                    "@aggFunction" : "avg"
                }
            },
            terminalReaches : {
                reach: []
            },
            areaOfInterest : "",
            nominalComparison : {
            	"@type":"none"
            },
            sourceShareComparison : {
            	"@type": null
            }
        },
        PermanentMapState : {
            what_to_map: "catch",
            
            /**
             * displayBins and functional bins each contain {low: #, high: #}
             * binColors is just an array of html colors, one per bin.
             * BoundUnlimited contains {low: true/false, high: true/false} for each bin.
             * 
             * displayBins are formatted for use in the legend and may contain
             * '<' and '>' values for top and bottom bins.  It uses sci-notation
             * when appropriate.  When custom bins are used, no attempt is made
             * to further format the users values (ie don't attempt to sci-format
             * the user's value of 1000000000.1).
             * 
             * functionalBins should be used to actually create the bins used
             * by mapviewer and as the default bin values when the user opens the
             * custom bin window.  These values are always non-sci format.
             * 
             * BoundUnlimited of true for low or high indicates that that bound
             * is unlimited and should be shown with a '<' or '>' as appropriate.
             * 
             * nonDetect is an array of booleans that is true if a bin is a
             * non detect bin, ie, it contains the values that were below the
             * detection limit.
             * 
             */
            binData: {
        		"displayBins":[],
        		"functionalBins":[],
        		"binColors":[],
        		"boundUnlimited":[],
        		"nonDetect":[]
        	},
        	binAuto: true,
        	binCount: 5,
        	binType: 'EQUAL_COUNT',
        	
        	//Similar to mapLayers, but for specific named layers that are
        	//related to the model.  Negative means not shown but spec's the
        	//remembered opacity (-75 == 75 opacity, not shown).
        	calibSites: -75,
			reachOverlay: 80,
			huc8Overlay: -75,
			dataLayerOpacity: 100,
			
			/*
			 * lat, lon & zoom are only valid at selet times:
			 * -initially after loading a predefined or user uploaded session
			 * -immediately before the session is exported (via update_SESSION_mpastate)
			 * At all other times, expect this to be out of sync w/ the current map.
			 * As such, there are no accessor methods to limit usage.
			 */
        	lat: null,
        	lon: null,
        	zoom: null,
        	/*
        	 * NOTE:  See the setMapLayerEnabled and related methods.
        	 * The mapLayers object is an associative array in which a layer's
        	 * ID retrieves the opacity property.
        	 * For instance, if there was a map layer with ID 99,:
        	 * mapLayers[99] = 90
        	 * would indicate that the layer is active with an opacity of  90%.
        	 * A negative value means the layer is disabled but the opacity
        	 * setting is being preserved for when it is later re-enabled.
        	 */
        	mapLayers: new Object()
        },
        TransientMapState : {
        	constituent: "",
        	units: "",
        	docUrl: "",
        	modelName: "",
        	themeName: "",
			sourceList: null,	/* Structured array - see renderModelData */
        	originalBoundSouth: null, /* float */
        	originalBoundNorth: null, /* float */
        	originalBoundEast: null, /* float */
        	originalBoundWest: null, /* float */
        	lastNominalComparison: null,	/* comparison last time comp was disabled (for re-enabling) */
			spatialServiceEndpoint: null,	/* nominally Geoserver's urls */
			predefinedSessionName: null,	/* possibly passed in the url, or choosen by user after load */
			
			//Similar to calibSites in PermanentMapState, but for the Reach
			//Identification overlay.  This is kept in transient state b/c
			//the currently IDed reach is not considered 'saveable' to a session.
        	//Negative means not shown but spec's the
        	//remembered opacity (-75 == 75 opacity, not shown).
        	identifiedReachOverlay: -75,
			
			identifiedReachId: null,
			
			
			/* state tracking */
			lastMappedState: null, /* Snapshot of the state (context and permState) at time of last map draw */
			lastMappedContextId: null, /* Id (a number) of this state as registered w/ the server */
			lastMappedContext: null, /* Context ONLY of the last map (perm state is not included) */
			lastValidState: null, /* Snapshot of the state (context and permState) that was last known to be valid and registered w/ server (fallback if no map) */
			lastValidContextId: null, /* Id (a number) of this state as registered w/ the server */
        	
        	previousState: null, /* previous context and perm state, serialized as json */
			
			lastValidSeriesData: {
				seriesConstituent: null,
				seriesUnits: null,
				seriesName: null,
				seriesDescription: null,
				rowCount: null
			},
			lastMappedSeriesData: {
				seriesConstituent: null,
				seriesUnits: null,
				seriesName: null,
				seriesDescription: null,
				rowCount: null
			},
			
			/* Arry of context ids and their associated dataLayerInfo
			 * registered as data layers w/ the map server.
			 * This list should be kept short (5?) b/c the server will eventually
			 * sweep old map layers.  This list allows the UI to know that layers
			 * have been previously registerd so that they don't have to be
			 * if the user is just changing bin color or what to map.
			 * Note that a context may be registered for predictions registering
			 * the layer is a separate process.
			 */
			registerDataLayers: {
				contextIds : [],
				dataLayerInfos : []
			},
        	
        	/*
        	 * Bin data become transient if auto-binning is used b/c its redundant
        	 * data.
        	 */
            binData: {
        		"displayBins":[],
        		"functionalBins":[],
        		"binColors":[],
        		"boundUnlimited":[],
        		"nonDetect":[]
        	},
			
			/* The current data layer wms url and layer names */
			dataLayerInfo : {
				dataLayerWmsUrl: "",	//ends with /wms
				flowlineDataLayerName: "",
				catchDataLayerName: ""
			}

        }
    }

});

/**
 * Sparrow.ux.Session take a Sparrow.ux.Context and fire events on the that for given events in the session 
 * Note that all data is stored in the Context and all the method are in the Session.
 * The separation allows the Context to be serialized w/o including the methods.
 */
Sparrow.ux.Session = function(config){
	this.context = config.context;
	return this.load(this.context.sessionTemplate, true);
};
Sparrow.ux.Session.prototype = {
	
	/**
	 * Load a new state from a JSON String.
	 * 
	 * This is used at application startup to load a blank state from the 
	 * Sparrow.ux.Context.sessionTemplate, its used to load a predefined session
	 * (which are stored as JSON strings) and simlarly for adhoc stored sessions
	 * that the user might upload.
	 * 
	 * Loading a predefined session or user uploaded session should *not* set
	 * the loadNewTransientData flag, since:
	 * In the case of a PreSession, the model data is still valid.
	 * In the case of a user uploaded session, it could be a different model,
	 * so the model data needs to be reloaded.
	 * 
	 * @param {String} jsonOrString A JSON serialized string representing a context state.  See Sparrow.ux.Context.sessionTemplate for structure.
	 * @param {boolean} loadNewTransientData If true, the transient data (or lack of) in jsonOrString will overwrite the current transient data.
	 * @returns {Sparrow.ux.Session.prototype}
	 */
	load : function(jsonOrString, loadNewTransientData) {
		// JSONify the input if it is a string.
		var data = (Ext.isString(jsonOrString))? Ext.decode(jsonOrString): jsonOrString;

		// This is done so that the Session object may be manipulated just as the original JSON object
		this.PredictionContext = data.PredictionContext;
		this.PermanentMapState = data.PermanentMapState;
		
		if (loadNewTransientData) {
			this.TransientMapState = data.TransientMapState;
		}
		
		//Its possible the loaded transient is null, so update to the template.
		if (! this.TransientMapState) {
			this.TransientMapState = this.context.sessionTemplate.TransientMapState;
		}
		
		//Only one should be non-null at a time
		if (this.PermanentMapState.binAuto) {
			this.PermanentMapState.binData = null;
		} else {
			this.TransientMapState.binData = null;
		}
		
		return this;
	},
	
	getModelId : function() {
		return parseInt(Sparrow.SESSION.PredictionContext["@model-id"]);
	},

	getData : function(){return {
				PredictionContext: this.PredictionContext,
				PermanentMapState: this.PermanentMapState
			};
	},

	getPredictionContext: function(){return this.PredictionContext;},

	/**
	 * Returns a PredictionContext as an XML document for communication w/ the server.
	 * 
	 * if no context is specified, the current PredictionContext is used.
	 * 
	 * @param PredictionContext context Optional context - the current one is used if not specified.
	 * @returns An XML String represention of the context.
	 */
	getPredictionContextAsXML : function(context) {
		if (context == null) context = this.PredictionContext;
		return Sparrow.USGS.JSONtoXML({PredictionContext : context});
	},
	
	/** Returns adjustments as XML String */
	getAdjustmentGroupsAsXML : function(){
		return Sparrow.USGS.JSONtoXML({adjustmentGroups : this.PredictionContext.adjustmentGroups});
	},


	/**
	 * Returns the current non-transient state as a JSON string.
	 * 
	 * @param {boolean} contextOnly If true, only the Prediction context is included - the perm state is skipped.
	 * @returns {String} A JSON string representing the application state.
	 */
	asJSON : function(contextOnly){
		var state = this.getData();
		if (contextOnly == true) {
			return Ext.util.JSON.encode({PredictionContext: state.PredictionContext});
		} else {
			return Ext.util.JSON.encode(state);
		}
	},
	

	/** Returns SESSION/SESSION.data as a XML String*/
	asSessionXML : function(){ 
		//trying to serialize a Ext.Observable breaks things, detach it for a bit
		var contextTemp = this.context;
		this.context = {};
		var xml = Sparrow.USGS.JSONtoXML({"SESSION" : this}); 
		this.context = contextTemp;
	},

	/** Holds and consolidates any change events */
	consolidateEvents: function(){
		this.suspend = true;
		this.changeQueue = false;// empty the queue
	},

	/** Release change events if in queue */
	releaseEvents: function(){
		this.suspend = false;
		if (this.changeQueue){
			this.changeQueue = false;
		}
	},

	/** Registers change event */
	changed : function(){
		//No-op:  this should be handled by specific events
		//this.fireContextEvent('changed');
	},
	
	fireContextEvent : function(event, argument){
		if (this.suspend){
			// the change queue is now full
			this.changeQueue = true;
		} else {
			// just fire the event
			this.context.fireEvent(event, argument);
		}
	},
			
	/**
	 *  Marks a server-registered mappable state so it can be returned to and compared to.
	 *  Marking a mapped state automatically updates the last valid state (calls
	 *  markValidState).
	 *  
	 *  @param contextId {Number} Required context ID assigned by the server.
	 *  @param seriesData {object} Required object matching the structure of lastMappedSeriesData
	 */
	markMappedState: function(contextId, seriesData) {
		this.TransientMapState.lastMappedState = this.asJSON();
		this.TransientMapState.lastMappedContextId = contextId;
		this.TransientMapState.lastMappedContext = this.asJSON(true);
		this._copySeriesData(seriesData, this.TransientMapState.lastMappedSeriesData);
		this.markValidState(contextId, seriesData);
	},
	
	/**
	 * Marks a (possibly) server-registered valid state so it can be returned to and compared to.
	 * 
	 * At startup, this will be called to save the init state so that even if the
	 * user modifies the UI to an invalid state, UI operations can use the init
	 * state for communications w/ the server that require a valid context.
	 * 
	 * The optional parameters can be set if the context is registered w/ the server.
	 * 
	 * @param contextId {Number} optional validContextId The context ID (if available)
	 * @param seriesData {object} optional object matching the structure of lastMappedSeriesData
	 */
	markValidState: function(contextId, seriesData) {
		if (contextId) {
			this.TransientMapState.lastValidContextId = contextId;
		} else {
			this.TransientMapState.lastValidContextId = null;	//don't let old value stay
		}
		
		this.TransientMapState.lastValidState = this.asJSON();
		this._copySeriesData(seriesData, this.TransientMapState.lastValidSeriesData);
	},
	
	/**
	 * Safely copies series data from one instance to another, handling null
	 * conditions in copyFrom.
	 * 
	 * If copyFrom is null or one of its fields unspecified, the corresponding
	 * field in copyTo is set to "" (possibly the entire copyTo if copyFrom is null).
	 */
	_copySeriesData: function(copyFrom, copyTo) {
		if (copyFrom) {
			copyTo.seriesConstituent = copyFrom.seriesConstituent?copyFrom.seriesConstituent:null;
			copyTo.seriesUnits = copyFrom.seriesUnits?copyFrom.seriesUnits:null
			copyTo.seriesName = copyFrom.seriesName?copyFrom.seriesName:null;
			copyTo.seriesDescription = copyFrom.seriesDescription?copyFrom.seriesDescription:null;
			copyTo.rowCount = copyFrom.rowCount?copyFrom.rowCount:null;
		} else {
			copyTo.seriesConstituent = null;
			copyTo.seriesUnits = null;
			copyTo.seriesName = null;
			copyTo.seriesDescription = null;
			copyTo.rowCount = null;
		}
	},
	
	/**
	 * Returns a usable context ID, good for most operations.
	 * This is the ID that is currently being mapped, or if there is no map,
	 * the ID of the default context, marked at the time the UI loads.
	 * 
	 * It is theoretically possible that the UI could update the valid state w/o
	 * updating the map, in which case the caller would need to choose between this
	 * and getMappedContextId.
	 * 
	 * @returns {unresolved}
	 */
	getMappedOrValidContextId: function() {
		if (this.TransientMapState.lastMappedContextId) {
			return this.TransientMapState.lastMappedContextId;
		} else {
			return this.TransientMapState.lastValidContextId;
		}
	},
	
	getMappedSeriesData: function() {
		return this.TransientMapState.lastMappedSeriesData;
	},
	
	/**
	 * Returns the most recent usable context ID, which may not be mapped.
	 * 
	 * It is possible that the UI could update the valid state w/o
	 * updating the map, in which case the caller would need to choose between this
	 * and getMappedContextId.
	 * 
	 * @returns {unresolved}
	 */
	getLastValidContextId: function() {
		return this.TransientMapState.lastValidContextId;
	},
	
	getLastValidSeriesData: function() {
		return this.TransientMapState.lastValidSeriesData;
	},
	
	/**
	 * Returns true only if the lastValidId, context and seriesData are all
	 * set.  If false, the last valid context was not registered with the server.
	 * @returns {boolean}
	 */
	isLastValidLastValidContextFullySpecified: function() {
		return (
				this.TransientMapState.lastValidContextId &&
				this.TransientMapState.lastValidState &&
				this.TransientMapState.lastValidSeriesData.seriesConstituent); 
	},
			
	/**
	 * Returns the most recent usable context, which may not be mapped.
	 * 
	 * It is possible that the UI could update the valid state w/o
	 * updating the map, in which case the caller would need to choose between this
	 * and getMappedContext.
	 * 
	 * @returns {Object} {PredictionContext, PermanentMapState}
	 */
	getLastValidState: function() {
		var vs = this.TransientMapState.lastValidState;

		if (vs != null) {
			return Ext.util.JSON.decode(vs);
		} else {
			return null;
		}
	},
		
	/**
	 * Returns the context ID of the last generated map, which may be null.
	 * 
	 * @returns {unresolved}
	 */
	getLastMappedContextId: function() {
		return this.TransientMapState.lastMappedContextId;
	},
			
	/**
	 * Returns the context of the last generated map, which may be null.
	 * 
	 * @returns {Object} {PredictionContext, PermanentMapState} or null.
	 */
	getLastMappedState: function() {
		if (this.TransientMapState.lastMappedState != null) {
			return Ext.util.JSON.decode(this.TransientMapState.lastMappedState);
		} else {
			return null;
		}
	},

	/**
	 *  Marks an assumed to be mappable state for future comparison.
	 *  This state is also used to register a context if no map has been created
	 *  yet, but a context is needed for server communication (eg the ID operation).
	 */
	mark: function(){
		this.TransientMapState.previousState = this.asJSON();  	
	},
	
	/**
	 * Returns true if a map was ever created for this model.
	 */		
	isMapping : function() {
		return this.TransientMapState.lastMappedContextId != null;
	},
	
	/**
	 * Returns true if context (not perm or trans state) has changed since the
	 * last map was drawn.
	 * If no map was ever drawn, returns true.
	 */
	isContextChangedSinceLastMap : function() {
		if (this.TransientMapState.lastMappedContextId == null) {
			return true;
		} else {
			return (this.asJSON(true) != this.TransientMapState.lastMappedContext);
		}
	},
			
	/**
	 * Returns true if the state has changed since the context was last registered.
	 * If no context was ever registered, this returns true.
	 * Perhaps rename to changedSinceLastRegisteredState?
	 */
	isChangedSinceLastMarkedState : function() {
		if (this.TransientMapState.lastValidContextId == null) {
			return true;	//No state was ever registered or we have no ID for it
		} else {
			return (this.asJSON() != this.TransientMapState.lastValidState);
		}
	},


	/**
	 * Returns true if the current app state can be mapped.
	 * This will check isValidContextState in addition to other non-context
	 * related settings that might make the state unmappable.
	 * @returns {undefined}
	 */
	isValidMapState : function() {
		var msg = this.getInvalidMapStateMessage();
		return msg == null;
	},
			
	/**
	 * Returns true if the current context state would be expected to be mappable,
	 * i.e., would not through an error when registering a context id.
	 * e.g.:  A delivery series w/o downstream reaches is not mappable.
	 * This is a best guess:  The server has the final say.
	 */
	isValidContextState : function() {
		var msg = this.getInvalidContextStateMessage();
		return msg == null;
	},
	
	/**
	 * Returns a message explaining why the current state is invalid/unmappable, if it is.
	 * If the current context would be expected to be mappable, null is returned.
	 * This basically adds additional requirements to getInvalidContextStateMessage.
	 * @returns A String message if the context is unmappable, null if it is mappable.
	 */		
	getInvalidMapStateMessage : function() {
		
		var msg = this.getInvalidContextStateMessage();
		
		if (msg == null) {
			if (! this.isBinAuto() && this.getBinData()['functionalBins'].length == 0) {
				msg = "The <i>Auto Binning</i> option on the <i>Display Results</i> tab is disabled, but no bins are specified.  " +
						"Please enable <i>Auto Binning</i> or define custom bins to create a map.<br/>"  +
						"<i>Binning</i> determines which values are drawn in which color on the map.  " +
						"If you are unsure how to proceed, it is generally OK to use the auto binning.";
			}
		}
		
		return msg;
	},
			
	/**
	 * Returns a message explaining why the current context is unregisterable w/ the server, if it is.
	 * If the current context would be expected to be registerable, null is returned.
	 * @returns A String message if the context is unregisterable, null if it is.
	 */		
	getInvalidContextStateMessage : function() {			
		if (this.isDeliveryDataSeries() && ! this.hasEnabledTargetReaches())	{
			return "A <i>Downstream Tracking</i> data series is selected on the <i>Display Results</i> tab, but no <i>Downstream Reaches</i> are selected.  " +
					"Please select at least one <i>Downstream Reach</i> on the <i>Downstream Tracking</i> tab and try again.";
		} else if (this.getDataSeries() == 'source_value' && this.getDataSeriesSource() == '') {
			return "The data series <i>Model Input Sources</i> is selected on the <i>Display Results</i> tab, but no <i>Model Source</i> is selected.  " +
					"Please select a <i>Model Source</i> on the <i>Display Results</i> tab and try again.";
		} else {
			return null;
		}
	},		
			
	/**
	 * Returns true if the context id was previously registered as a data layer
	 * with the map server.
	 * 
	 * @param {type} contextId
	 * @returns {undefined}
	 */
	isRegisteredDataLayer : function(contextId) {
		return (this.TransientMapState.registerDataLayers.contextIds.indexOf(contextId) > -1);
	},
	
	/**
	 * Finds the data layer info for the context ID, or returns null if not found.
	 * @param {type} contextId
	 * @returns {registerDataLayers.dataLayerInfos}
	 */
	getRegisteredDataLayer : function(contextId) {
		idx = this.TransientMapState.registerDataLayers.contextIds.indexOf(contextId);
		if (idx > -1) {
			return this.TransientMapState.registerDataLayers.dataLayerInfos[idx];
		} else {
			return null;
		}
	},
	
	/**
	 * Records a context ID and its datalyer info as being registered w/ the map
	 * server as a data layer.
	 * 
	 * Only the five most recent context IDs are kept so that very old IDs will
	 * be forced to re-register.
	 * 
	 * If the layer is already recorded, it brings it to the top of the list
	 * (more recently used), allowing older unused ones to drop off.
	 * 
	 * @param {type} contextId
	 * @returns {undefined}
	 */
	bumpRegisteredDataLayer : function(contextId) {
		
		idx = this.TransientMapState.registerDataLayers.contextIds.indexOf(contextId);
		
		
		//If zero, its already at the top of the list
		if (idx > 0) {
			//already in list - remove it
			this.TransientMapState.registerDataLayers.contextIds.splice(idx, 1);
			dl = this.TransientMapState.registerDataLayers.dataLayerInfos.splice(idx, 1)[0];
			
			//re-insert at zero
			this.TransientMapState.registerDataLayers.contextIds.splice(0,0, contextId);
			this.TransientMapState.registerDataLayers.dataLayerInfos.splice(0,0, dl);
		}
		
	},
	
	/**
	 * Records a context ID and its datalyer info as being registered w/ the map
	 * server as a data layer.
	 * 
	 * Only the five most recent context IDs are kept so that very old IDs will
	 * be forced to re-register.
	 * 
	 * If the layer is already recorded, it brings it to the top of the list
	 * (more recently used), allowing older unused ones to drop off.
	 * 
	 * @param {type} contextId The context ID of the data layer
	 * @param dataLayerInfo All the layer info - see TransientState above for definition
	 * @returns {undefined}
	 */
	addRegisteredDataLayer : function(contextId, dataLayerInfo) {
		
		idx = this.TransientMapState.registerDataLayers.contextIds.indexOf(contextId);
		
		if (idx < 0) {
			//Not already in list
			this.TransientMapState.registerDataLayers.contextIds.splice(0,0, contextId);
			this.TransientMapState.registerDataLayers.dataLayerInfos.splice(0,0, dataLayerInfo);
			
			//trim to most recent 5 entries
			while (this.TransientMapState.registerDataLayers.contextIds.length > 5) {
				this.TransientMapState.registerDataLayers.contextIds.pop();
				this.TransientMapState.registerDataLayers.dataLayerInfos.pop();
			}
		} else {
			//already in list - just bump it
			this.bumpRegisteredDataLayer(contextId);
		}
	},

	// Doesn't seem to be used?? [IK]
	setWhatToMap: function(val) {
		this.PermanentMapState["what_to_map"] = val;
		this.fireContextEvent("what-to-map");
		this.changed();
	},
	
	/**
	 * Sets the dataLayer information, used to map the data layer as a wms layer
	 * from the map server.
	 * 
	 * If only the context ID is provide, it is assumed that this layer was already
	 * registerd and we just find it in the registerDataLayers list.
	 * 
	 * @param {type} contextId
	 * @param {type} dataLayerInfo
	 * @param {type} flowlineDataLayerName
	 * @param {type} catchDataLayerName
	 * @returns {undefined}
	 */
	setCurrentDataLayerInfo: function(contextId, dataLayerInfo) {
		
		if (dataLayerInfo != null && ! this.isRegisteredDataLayer(contextId)) {
			//Adding a new layer, or one not previously registered

			this.TransientMapState["dataLayerInfo"] = dataLayerInfo;

			this.addRegisteredDataLayer(contextId, dataLayerInfo);
		} else {
			//Making an already registered layer current again
			var dl = this.getRegisteredDataLayer(contextId);
			this.TransientMapState["dataLayerInfo"] = dl;
			this.bumpRegisteredDataLayer(contextId);
		}

		this.changed();
	},
	
	getDataLayerInfo: function() {
		return this.TransientMapState["dataLayerInfo"];
	},
	
	getAutoBinType: function() {
		return this.PermanentMapState.binType;
	},
	
	setAutoBinType: function(type) {
		this.PermanentMapState.binType = type;
	},
	
	/**
	 * Returns a user displayable name for the type of bins that *would* be
	 * created with the current type of autoBin.
	 * 
	 * @See getCurrentBinTypeName
	 * @returns {String}
	 */
	getAutoBinTypeName: function() {
		var type = this.PermanentMapState.binType;
		
		if (type == 'EQUAL_COUNT') {
			return 'Equal Count';
		} else if (type == 'EQUAL_RANGE') {
			return 'Equal Range';
		} else {
			return 'UNKNOWN';
		}
	},
	
	/**
	 * Returns a user displayable name for the current type of bins.
	 * If autoBin is disabled, this will return 'Custom', which is different than
	 * what getAutoBinTypeName(), which will always return what type of auto be
	 * generated if they were enabled.
	 * @returns {String}
	 */
	getCurrentBinTypeName: function() {
		var type = this.PermanentMapState.binType;
		
		if (this.PermanentMapState.binAuto) {
			return this.getAutoBinTypeName();
		} else {
			return 'Custom';
		}
	},
	
	isBinAuto: function() {
		return this.PermanentMapState.binAuto;
	},
	
	/**
	 * This method has the side-effect of moving the binData to the transient
	 * state, since it doesn't really need to be perm if its auto-binning.
	 * @param auto
	 */
	setBinAuto: function(auto) {
		
		if (auto != this.PermanentMapState.binAuto) {
			this.PermanentMapState.binAuto = auto;
			
			//This is a change
			if (auto) {
				//switch to auto-binning mode.  Bin data moves to  transient state
				this.TransientMapState.binData = this.PermanentMapState.binData;
				this.PermanentMapState.binData = null;
			} else {
				if (this.TransientMapState.binData) {
					this.PermanentMapState.binData = this.TransientMapState.binData;
				}
				this.TransientMapState.binData = null;
			}
			
			this.fireContextEvent("autobin-changed");
		}
	},
	
	getBinData: function() {
		if (this.PermanentMapState.binAuto) {
			return this.TransientMapState.binData;
		} else {
			return this.PermanentMapState.binData;
		}
	},

	/**
	 * See the structure of this data defined above.
	 * 
	 * If isIsolatedUserChange is true, the map will auto redraw.  This should
	 * be set true when the user is ONLY changing the bins directly.  In other
	 * cases (autoBins or auto-adjustming the bins) where the caller will
	 * update the map, leave this flag false so we don't redraw the map twice.
	 * 
	 * @param structuredBinningData
	 * @param isIsolatedUserChange - Set true if this is a single user action 
	 *	(false when autoBin sets this data or caller plans to update map)
	 */
	setBinData: function(structuredBinData, isIsolatedUserChange) {
		
		//If the user's bins are being 'fixed' b/c they don't contain all the
		//values, keep the users previous colors and just take the numbers.
		if (structuredBinData['binColors'] == null) {
			structuredBinData['binColors'] = this.getBinData()['binColors'];
		}
		
		if (this.PermanentMapState.binAuto) {
			this.TransientMapState.binData = structuredBinData;
		} else {
			this.PermanentMapState.binData = structuredBinData;
		}
		
		//Update bin count, not counting a non-detect bin (if one exists)
		var newBins = this.getBinData();
		var cnt = newBins['functionalBins'].length;
		for (i=0; i< newBins['functionalBins'].length; i++) {
			if (newBins['nonDetect'][i] == true) {
				cnt = cnt - 1;	//decrement b/c one bin is 'fake'
				break;
			}
		}
		this.PermanentMapState.binCount = cnt;
		
		if (isIsolatedUserChange) {
			this.fireContextEvent("user-bindata-changed");
		} else {
			this.fireContextEvent("system-bindata-changed");
		}
	    
	},
	
	/**
	 * Returns the current number of bins, or if there are no
	 * bins and using auto-binning, the number of bins to be requested.
	 * 
	 * Note that there is no setBinCount b/c the number is never set
	 * directly.  When the user generates auto-bins, the number of
	 * resulting bins is recorded in permanent state.
	 */
	getBinCount: function() {
		return this.PermanentMapState.binCount;
	},
	
	/**
	 * If autoBin is enabled, returns true if the current auto generated bin
	 * data is current.  If true, it is not necessary to regenerate bins with
	 * the server before drawing the map.  This means this method can only return
	 * true if the map was previously drawn, the context has not changed, and
	 * the previous map was done w/ autoBin.
	 * 
	 * If autoBin is false, this method returns false.
	 * @returns {undefined}
	 */
	isAutoBinDataCurrent: function() {
		
		if (this.isBinAuto() && ! this.isContextChangedSinceLastMap()) {
			//We are in autoBin mode && the context has not changed since the last map
			
			var lastMapState = this.getLastMappedState();
			
			if (lastMapState && lastMapState.PermanentMapState) {
				//Edge case err checking:  we had a map, so there should be a mapped state
				
				var lastMapPermState = lastMapState.PermanentMapState;

				if (lastMapPermState.binAuto) {
					//The previous map was generated w/ autoBin, so should be OK to reuse bins
					return true;
				}
			}
		}
		
		return false;
	},
	
	/**
	 * If autoBin is disabled, returns true if the prediction context has been
	 * mapped with the current user created custom bins, and the context has not
	 * changed since that map was drawn.
	 * If true, it is not necessary to validate the custom bins with
	 * the server before drawing the map.  This means this method can only return
	 * true if the map was previously drawn w/ autoBin disabled, the context and
	 * the bin break-point values have not changed, and autoBin is still disabled.
	 * 
	 * If autoBin is true, this method returns false.
	 * @returns {undefined}
	 */
	isCustomBinDataCurrent: function() {
		if (! this.isBinAuto() && ! this.isContextChangedSinceLastMap()) {
			//We are not in autoBin mode && the context has not changed since the last map
			
			var lastMapState = this.getLastMappedState();
			
			if (lastMapState && lastMapState.PermanentMapState && lastMapState.PermanentMapState.binData) {
				//Edge case err checking:  we had a map, so there should be a mapped state
				
				
				var lastMapPermState = lastMapState.PermanentMapState;

				if (! lastMapPermState.binAuto) {
					//The previous map was generated w/o autoBin, so check bin values
					
					var binsAreadyVerified = true;
					
					var currentFBins = this.getBinData().functionalBins;
					var oldFBins = lastMapPermState.binData.functionalBins;
					
					//check all functional bins
					for (i = 0; i < currentFBins.length; i++) {
						if (currentFBins[i].low != oldFBins[i].low || currentFBins[i].high != oldFBins[i].high) {
							binsAreadyVerified = false;
							break;
						}
					}
					
					return binsAreadyVerified;
				}
			}
		}
		
		return false;
	},
	
	/**
	 * Tests for and converts older style overlay layers to the newer style.
	 * This handles reach, HUC8 and calibration site overlays, not the WMS layers.
	 */
	convertOldOverlaysToNew: function() {
		
		var isOldStyle = false;
		
		if (this.PermanentMapState.showReachOverlay != null) {
			isOldStyle = true;
			this.PermanentMapState.showReachOverlay?this.PermanentMapState.reachOverlay = 75:this.PermanentMapState.reachOverlay = -75;
			this.PermanentMapState.showReachOverlay = undefined;
		}
		
		if (this.PermanentMapState.showCalibSites != null) {
			isOldStyle = true;
			this.PermanentMapState.showCalibSites?this.PermanentMapState.calibSites = 75:this.PermanentMapState.calibSites = -75;
			this.PermanentMapState.showCalibSites = undefined;
		}
		
		if (isOldStyle) {
			//This layer was not present in the older style layers
			this.PermanentMapState.huc8Overlay = -75
		}
	},
	
	
	/**
	 * Tests for and converts older style predefined session bin data to new
	 * bin data.
	 */
	convertOldBinningDataToNew: function() {
		if (this.PermanentMapState.binning != null && 
				this.PermanentMapState.binning.bins != null &&
				this.PermanentMapState.binning.bins.length > 0) {
			
	        var displayBins = [];
	        var functionalBins = [];
	        var boundUnlimited = [];
	        var colors = [];
	        var nonDetect = [];
	        var type = this.PermanentMapState.binning['type'];
	        
            for (var i = 0; i < this.PermanentMapState.binning.bins.length; i++) {
            	var lowVal = this.PermanentMapState.binning.bins[i]['low'];
            	var highVal = this.PermanentMapState.binning.bins[i]['high'];
            	var color = this.PermanentMapState.binning.bins[i]['color'];
            	
            	displayBins.push({low: lowVal, high: highVal});
            	functionalBins.push({low: lowVal, high: highVal});
            	boundUnlimited.push({low: false, high: false});
            	colors.push(color);
            	nonDetect[i] = false;
            }
            
            var binData = {
        		"displayBins": displayBins,
        		"functionalBins": functionalBins,
        		"binColors": colors,
        		"boundUnlimited": boundUnlimited,
        		"nonDetect": nonDetect
            };
            
            this.setBinData(binData);
            
            
            if (type.indexOf("Range") > 0) {
            	this.PermanentMapState.binType = "EQUAL_RANGE";
            } else {
            	this.PermanentMapState.binType = "EQUAL_COUNT";
            }
		}
		
		if (this.PermanentMapState.autoBin != null) {
			
			//bypass any events
			this.PermanentMapState.binAuto = this.PermanentMapState.autoBin;
		}
		
		//Null out old properties
		this.PermanentMapState.autoBin = undefined;
		this.PermanentMapState.binning = undefined;
	},

	// Doesn't seem to be used?? [IK]
	setAggFunction: function(val) {
		this.PredictionContext.analysis.groupBy["@aggFunction"] = val;
		this.changed();
	},
	// Doesn't seem to be used?? [IK]
	setGroupBy: function(val) {
		this.PredictionContext.analysis.groupBy["#text"] = val;
		this.changed();
	},
	getGroupBy:	function() {
		return this.PredictionContext.analysis.groupBy["#text"];
	},

	// Doesn't seem to be used?? [IK]
	setAnalyticFunction: function(val) {
		this.PredictionContext.analysis.analyticFunction["#text"] = val;
		this.changed();
	},

	/**
	 * Resets the comparison to the last user selected value, if any, and
	 * clears the previous user selection.
	 * If there is no previous user selection, no change is made.
	 * 
	 * Returns true if it was reset (indicating it was disabled before)
	 */
	resetComparisonToLastUserSelectionIfDisabled: function() {
		if (this.TransientMapState.lastNominalComparison) {
			this.setComparisons(this.TransientMapState.lastNominalComparison, true);
			this.TransientMapState.lastNominalComparison = null;
			
			return true;	//was reset
		} else {
			return false;	//not reset
		}
	},
	
	/**
	 * Resets the comparison to the last user selected value, if any, and
	 * clears the previous user selection.
	 * If there is no previous user selection, no change is made.
	 */
	clearComparisonUserSelection: function() {
		var oldVal = this.getComparisons();
		this.setComparisons(null, true);
		this.TransientMapState.lastNominalComparison = oldVal;
	},
	
	/**
	 * Assigns the current state of the nominalComparison
	 * 
	 * @param val The new value to assign to
	 * @param isSideEffect If true, this change is a side affect of a different
	 * 	change, thus it should not raise a generic change event.
	 * 
	 * TODO remove "isSideEffect". Move responsibility of suspending events to the caller of this function. 
	 * That can be accomplished by calling "suspendEvents" on the CONTEXT object before the code, then "resumeEvents" after the code.
	 * See Ext documentation for further detail. Another alternative is to simply rename "isSideEffect"
	 * to "suspendEvents" as that is closer to common patterns.
	 */
	setComparisons: function(val, isSideEffect) {
		
		var oldVal = this.getComparisons(val);
		//source comparison must be mutually exclusive with nominalComparison, so make sure to delete it when setting to null
		if(val) {
			if(val=="percent") {
				if(!this.PredictionContext.sourceShareComparison) this.PredictionContext.sourceShareComparison = {};
				this.PredictionContext.sourceShareComparison["@type"] = val;
				
				if(this.PredictionContext.nominalComparison){
					this.PredictionContext.nominalComparison["@type"] = null;
					delete this.PredictionContext.nominalComparison;
				}
			} else {
				if(this.PredictionContext.sourceShareComparison){
					this.PredictionContext.sourceShareComparison["@type"] = null;
					delete this.PredictionContext.sourceShareComparison;
				}
				
				if(!this.PredictionContext.nominalComparison) this.PredictionContext.nominalComparison = {};
				this.PredictionContext.nominalComparison["@type"] = val;
			}
		} else {
			if(this.PredictionContext.sourceShareComparison){
				this.PredictionContext.sourceShareComparison["@type"] = null;
				delete this.PredictionContext.sourceShareComparison;
			}
			
			if(!this.PredictionContext.nominalComparison) {
				this.PredictionContext.nominalComparison = {};
			}
			this.PredictionContext.nominalComparison["@type"] = "none";
		}
		
		if(oldVal != this.getComparisons()) {
			this.fireContextEvent("comparisonchanged");
			
			if (! isSideEffect) {
				//Don't call a general update if this is due to another change
				this.changed();
			}
		}
	},
	
	getComparisons: function() {
		var comparison = this.PredictionContext.nominalComparison || this.PredictionContext.sourceShareComparison;
		return comparison["@type"];
	},
	
	setSpatialServiceEndpoint: function(endpointUrl) {
		this.TransientMapState.spatialServiceEndpoint = endpointUrl;
	},
	
	getSpatialServiceEndpoint: function() {
		return this.TransientMapState.spatialServiceEndpoint;
	},
	
	setPredefinedSessionName: function(sessionName) {
		if (sessionName == "") sessionName = null;
		this.TransientMapState.predefinedSessionName = sessionName;
	},
	
	getPredefinedSessionName: function() {
		return this.TransientMapState.predefinedSessionName;
	},

	/**
	 * If setting the dataSeries, update the name and description as well so that
	 * events have access to the current info.
	 * The intent is that this is called by the UI when the user changes the
	 * series - it does not update the series info returned by a registered
	 * context (lastValidSeriesData) and these two are expected to often be out
	 * of sync.
	 * 
	 * @param dataSeries
	 */
	setDataSeries: function(dataSeries) {
		var oldVal = this.PredictionContext.analysis.dataSeries["#text"];
		this.PredictionContext.analysis.dataSeries["#text"] = dataSeries;
		
		if(oldVal != dataSeries) {
			this.fireContextEvent("dataseries-changed");
			this.changed();
		}
	},
	
	getDataSeries: function() {
		return this.PredictionContext.analysis.dataSeries["#text"];
	},
	
	isDeliveryDataSeries: function() {
		return this.PredictionContext.analysis.dataSeries["#text"].toLowerCase().indexOf("deliver") >= 0;
	},

	setDataSeriesSource: function(val) {
		var oldVal = this.PredictionContext.analysis.dataSeries["@source"]
		if (val > 0) {
			this.PredictionContext.analysis.dataSeries["@source"] = val;
		} else {
			delete this.PredictionContext.analysis.dataSeries["@source"];
		}
		if(this.PredictionContext.analysis.dataSeries["@source"] != oldVal) {
			this.fireContextEvent("model-source-changed");
			this.changed();
		}
	},
	
	getDataSeriesSource: function() {
		return this.PredictionContext.analysis.dataSeries["@source"];
	},
	
	/**
	 * Enables/disables and assigns the opacity of this data-related layer.
	 * @param enable required
	 * @param newOpacity optional if enabling the layer.  Otherwise it uses
	 * the previous opacity.
	 */
	setCalibSitesOverlayRequested: function(enable, newOpacity) {
		this._setAnyDataLayerEnabled(enable, newOpacity, "calibSites", "calibsites-changed");
	},
	
	isCalibSitesOverlayRequested: function() {
		return (this.PermanentMapState["calibSites"] > 0);
	},
	
	getCalibSitesOverlayOpacity: function() {
		return this._getAnyDataLayerOpacity("calibSites");
	},

	/**
	 * Enables/disables and assigns the opacity of this data-related layer.
	 * @param enable required
	 * @param newOpacity optional if enabling the layer.  Otherwise it uses
	 * the previous opacity.
	 */
	setReachOverlayRequested: function(enable, newOpacity) {
		this._setAnyDataLayerEnabled(enable, newOpacity, "reachOverlay", "reachoverlay-changed");
	},

	/**
	 * Returns true if the reach overlay is requested by the user.
	 * Note that the layer may be disabled due to reaches being the data layer.
	 * Check isReachOverlayEnabled to confirm that the layer should be drawn if it
	 * is requested.
	 */
	isReachOverlayRequested: function() {
		return (this.PermanentMapState["reachOverlay"] > 0);
	},
	
	/**
	 * Returns true if it OK to draw the reach overlay.  Check to see if the user
	 * requested it:  isReachOverlayRequested().
	 * The reach overlay should not be drawn if the data layer is 'reach' instead of cathcment.
	 */
	isReachOverlayEnabled: function() {
		return (this.PermanentMapState["what_to_map"] != "reach");
	},
	
	getReachOverlayOpacity: function() {
		return this._getAnyDataLayerOpacity("reachOverlay");
	},
	
	/**
	 * Enables/disables and assigns the opacity of the reach identification layer.
	 * This cannot use the std _setAnyDataLayerEnabled call b/c this is based on
	 * trans state, not the perm state.
	 * 
	 * @param enable required
	 * @param newOpacity optional if enabling the layer.  Otherwise it uses
	 * the previous opacity.
	 */
	setReachIdOverlayRequested: function(enable, newOpacity, reachId) {
		
		var orgOpacity = this.TransientMapState.identifiedReachOverlay;
		
		if (enable) {
			
			if (reachId) {
				this.setIdentifiedReachId(reachId);
			}
			
			if (newOpacity) {
				this.TransientMapState.identifiedReachOverlay = newOpacity;
			} else if (orgOpacity < 0) {
				this.TransientMapState.identifiedReachOverlay = Math.abs(orgOpacity);
			}
			
			this.fireContextEvent("reach-id-layer-enabled");
			
		} else {
			if (orgOpacity > 0) {
				this.TransientMapState.identifiedReachOverlay = orgOpacity * -1;
			}
			this.fireContextEvent("reach-id-layer-disabled");
		}
	},

	/**
	 * Returns true if the reach overlay is requested by the user.
	 * Note that the layer may be disabled due to reaches being the data layer.
	 * Check isReachOverlayEnabled to confirm that the layer should be drawn if it
	 * is requested.
	 */
	isReachIdOverlayRequested: function() {
		return (this.TransientMapState.identifiedReachOverlay > 0);
	},
	
	getReachIdOverlayOpacity: function() {
		var op = this.TransientMapState.identifiedReachOverlay;
		if (op != null) {
			op = Math.abs(op);
			return op;
		} else {
			return 75;	//default
		}
	},
	
	setIdentifiedReachId: function(id) {
		this.TransientMapState.identifiedReachId = id;
	},
	
	getIdentifiedReachId: function(id) {
		return this.TransientMapState.identifiedReachId;
	},
	
	/**
	 * Enables/disables and assigns the opacity of this data-related layer.
	 * @param enable required
	 * @param newOpacity optional if enabling the layer.  Otherwise it uses
	 * the previous opacity.
	 */
	setHuc8OverlayRequested: function(enable, newOpacity) {
		this._setAnyDataLayerEnabled(enable, newOpacity, "huc8Overlay", "huc8overlay-changed");
	},

	isHuc8OverlayRequested: function() {
		return (this.PermanentMapState["huc8Overlay"] > 0);
	},
	
	getHuc8OverlayOpacity: function() {
		return this._getAnyDataLayerOpacity("huc8Overlay");
	},
	
	/**
	 * 
	 * @param opacity opacity to set the main data layer to
	 */
	setDataLayerOpacity: function(opacity) {
		this._setAnyDataLayerEnabled(true, opacity, "dataLayerOpacity", "dataLayerOpacity-changed");
	},
	
	getDataLayerOpacity: function() {
		return this._getAnyDataLayerOpacity("dataLayerOpacity");
	},
	
	/**
	 * Used by the three added data layers above.
	 * @param enable
	 * @param newOpacity
	 * @param permMapStateProperty
	 * @param eventName
	 */
	_setAnyDataLayerEnabled: function(enable, newOpacity, permMapStateProperty, eventName) {
		var orgOpacity = this.PermanentMapState[permMapStateProperty];
		
		if (enable) {
			if (newOpacity) {
				this.PermanentMapState[permMapStateProperty] = newOpacity;
				this.fireContextEvent(eventName);
			} else if (orgOpacity < 0) {
				this.PermanentMapState[permMapStateProperty] = Math.abs(orgOpacity);
				this.fireContextEvent(eventName);
			}
		} else {
			if (orgOpacity > 0) {
				this.PermanentMapState[permMapStateProperty] = orgOpacity * -1;
				this.fireContextEvent(eventName);
			}
		}
	},
	
	_getAnyDataLayerOpacity: function(permMapStateProperty) {
		var op = this.PermanentMapState[permMapStateProperty];
		if (op != null) {
			op = Math.abs(op);
			return op;
		} else {
			return 75;	//default
		}
	},
	
	/*
	 * Enables the layer and sets the opacity. (Use to assign opacity as well)
	 * If opacity is not specified, the previously used opacity will be used.
	 * If not previously enabled, the default opacity from the map framework
	 * will be used.
	 */
	setMapLayerEnabled: function(layerId, opacity) {
		
		if (opacity == null) {
			opacity = this.PermanentMapState["mapLayers"][layerId];
			if (opacity == null) {
				opacity = map1.layerManager.getMapLayer(layerId).opacity;
			} else {
				opacity = Math.abs(opacity);
			}
		}
		
		this.PermanentMapState["mapLayers"][layerId] = opacity;
		this.fireContextEvent("mapLayers-changed", layerId);
	},
	
	/*
	 * Disables the layer.
	 * If the layer was previously enabled, its opacity setting will be
	 * preserved.
	 * 
	 * Note:  No distinction is made b/t zero opacity and disabled.
	 */
	setMapLayerDisabled: function(layerId) {
		var opacity = this.PermanentMapState["mapLayers"][layerId];
		if (opacity != null && opacity > 0) {
			this.PermanentMapState["mapLayers"][layerId] = opacity * -1;
			this.fireContextEvent("mapLayers-changed", layerId);
		}
	},
	
	/**
	 * Returns true if the map layer is enabled.
	 * @param layerId The id of the layer
	 * @returns {Boolean}
	 */
	isMapLayerEnabled: function(layerId) {
		var opacity = this.PermanentMapState["mapLayers"][layerId];
		var enabled = opacity != null && opacity > 0;
		return enabled;
	},
	
	/*
	 * Returns the opacity of the layer if it is enabled.
	 * If the layer was previously enabled but is now disabled, it returns the
	 * previous opacity (see isMapLayerEnabled to distinguish).
	 * If the layer was never enabled, it returns null.
	 */
	getMapLayerOpacity: function(layerId) {
		var opacity = this.PermanentMapState["mapLayers"][layerId];
		if (opacity == null) {
			return null;
		} else {
			return Math.abs(opacity);
		}
		
	},
	
	/**
	 * Returns the available map layers as Layer objects from John's
	 * framework.  Layers must be available in the current zoom level and not
	 * hidden from the user to be returned.
	 * @returns
	 */
	getAvailableMapLayersAtCurrentZoomLevel: function() {
		var candidates = this.getAvailableMapLayers();
		var included = new Array();

		for (var i = 0; i < candidates.length; i++) {
			var layer = candidates[i];
			if (map1.layerManager.isLayerAvailable(layer) && !layer.isHiddenFromUser) {
				included.push(layer);
			}
		}
		
		return included;
	},
	
	/**
	 * Returns the available map layers as Layer objects from John's
	 * framework.
	 * Note that this method is doing away with the concept of hidden layers,
	 * since we don't really support them in this app b/c we want the user
	 * to be able to turn off everything if needed.
	 * @returns
	 */
	getAvailableMapLayers: function() {
		var layers = map1.layerManager.getAllLayers();
		return layers;
	},
	
	// This is called once, I hope. [KL]
	setModelConstituent: function(val) {
		this.TransientMapState.constituent = val;
	},
	
	getModelConstituent: function() {
		return this.TransientMapState.constituent;
	},
	
	// This is called once, I hope. [KL]
	setModelUnits: function(val) {
		this.TransientMapState.units = val;
	},
	
	getModelUnits: function() {
		return this.TransientMapState.units;
	},
	
	getLegendTitle: function() {
		var title = this.getMappedSeriesData().seriesName;
		if(this.hasAdjustments() && 
				this.getComparisons() != 'none' && 
				this.getComparisons() != 'percent') {
			title = "Change in <br/>"+title;
		}
		return title;
	},
	
	getLegendUnitsAndConstituent: function() {
		var seriesData = this.getMappedSeriesData();
		
		var u = Sparrow.USGS.prettyPrintUnitsForHtml(seriesData.seriesUnits);
		var c = seriesData.seriesConstituent;
		
		if (u == "") u = null;
		if (c == "") c = null;
		
		if (u != null && c != null) {
			return '<span class="unit">' + u + '</span> of ' + c;
		} else if (u != null) {
			return '<span class="unit">' + u + '</span>';
		} else if (c != null) {
			return c;
		} else {
			return null;
		}
	},
	
	setOriginalBoundSouth: function(val) {
		this.TransientMapState.originalBoundSouth = parseFloat(val);
	},
	
	setOriginalBoundNorth: function(val) {
		this.TransientMapState.originalBoundNorth = parseFloat(val);
	},

	setOriginalBoundEast: function(val) {
		this.TransientMapState.originalBoundEast = parseFloat(val);
	},

	setOriginalBoundWest: function(val) {
		this.TransientMapState.originalBoundWest = parseFloat(val);
	},
	
	getOriginalBoundSouth: function() {
		return this.TransientMapState.originalBoundSouth;
	},

	getOriginalBoundNorth: function() {
		return this.TransientMapState.originalBoundNorth;
	},

	getOriginalBoundEast: function() {
		return this.TransientMapState.originalBoundEast;
	},

	getOriginalBoundWest: function() {
		return this.TransientMapState.originalBoundWest;
	},
	
	setDocUrl: function(val) {
		this.TransientMapState.docUrl = val;
	},
	
	getDocUrl: function() {
		return this.TransientMapState.docUrl;
	},
	
	setModelName: function(val) {
		this.TransientMapState.modelName = val;
	},
	
	getModelName: function() {
		return this.TransientMapState.modelName;
	},
	
	setThemeName: function(val) {
		this.TransientMapState.themeName = val;
	},
	
	getThemeName: function() {
		return this.TransientMapState.themeName;
	},
	
	setSourceList: function(val) {
		this.TransientMapState.sourceList = val;
	},
	
	getSourceList: function() {
		return this.TransientMapState.sourceList;
	},
	
	hasAdjustments: function() {
	    return (this.getAllAdjustedReaches().length > 0 || 
	    		this.getAllTreatedGroups().length > 0);
	},
	
	hasEnabledAdjustments: function() {
		if(this.PredictionContext.adjustmentGroups.individualGroup['@enabled']) {
			var adjustedReachList = this.getAllAdjustedReaches();
		    for (var i = 0; i < adjustedReachList.length; i++) {
		        if(adjustedReachList[i].adjustment && adjustedReachList[i].adjustment.length>0) return true;
		    }
		}
	    
	    var treatedGroupList = this.getAllTreatedGroups();
	    for (var i = 0; i < treatedGroupList.length; i++) {
	        if(treatedGroupList[i]["@enabled"]) return true;
	    }
	    
	    return false;
	},
	
	/**
	 * Adds a group to the PredictionContext and returns that group object.
	 */
	addGroup : function(name, desc, notes, adjustments) {
		var group = {
			"@enabled": true,
			"@name": name,
			desc: desc,
			notes: notes,
			adjustment: adjustments || [], // TODO change adjustment to adjustments
			logicalSet: [],
			reach: []
			// TODO change reach to reaches
		};
		if(adjustments && adjustments.length > 0) this.fireContextEvent("adjustment-changed");
		
	    this.PredictionContext.adjustmentGroups.reachGroup.push(group);
	    this.fireContextEvent("adjustment-group-changed");
		this.changed();
		return group;
	},
	
	applyAdjustmentsToGroup: function(name, adjustments) {
	    var gp_JSON = this.getGroup(name);
	    gp_JSON.adjustment = adjustments;
	    this.fireContextEvent("adjustment-changed");
	    this.changed();
	},
	
	applyNotesToGroup: function(name, groupNotes) {
	    var gp_JSON = this.getGroup(name);
	    gp_JSON.notes = groupNotes;
	    this.changed();
	},
	
	setGroupEnabled: function(name, on) {
		var group = this.getGroup(name);
	    if(on === group["@enabled"]) return;
	    group["@enabled"] = on;
	    this.fireContextEvent("adjustment-changed");
		this.changed();
	},
	
	setIndividualGroupEnabled: function(on) {
		 if(on === this.PredictionContext.adjustmentGroups.individualGroup['@enabled']) return;
		this.PredictionContext.adjustmentGroups.individualGroup['@enabled'] = on;
	    this.fireContextEvent("adjustment-changed");
		this.changed();
	},

	/**
	  * Removes a group from the PredictionContext.  Deleting a group also removes
	  * all reach and adjustment information for that group.  However, reaches with
	  * individual adjustments remain a part of the PredictionContext (within an
	  * 'individual' group).
	 */
	removeGroup : function(name){
		var result = Sparrow.USGS.removeFirst(this.getAllGroups(), "@name", name);
		if (result != null) {
		    this.fireContextEvent("adjustment-group-changed");
			this.changed();
		}
		if(result[0].adjustment.length > 0) this.fireContextEvent("adjustment-changed");
		return result;
	},

	/**
	 * Returns the specified group object if it exists, null otherwise.
	 */
	getGroup : function(name) {
		return Sparrow.USGS.findFirst(this.getAllGroups(), "@name", name);
	},

	/**
	 * Determines whether or not a group by the specified name already exists.
	 * Returns true if so, and false otherwise.
	 */
	groupExists : function(name) {
		return (this.getGroup(name) != null);
	},

	/**
	 * Returns an array of all of the user's reach groups.
	 */
	getAllGroups : function(){
		return this.PredictionContext.adjustmentGroups.reachGroup;
	},

	/**
	 * Returns an array of all of the user's reach group names.
	 */
	getAllGroupNames : function() {
		return Ext.pluck(this.getAllGroups(), "@name");
	},
	
	/**
	 * Returns an array of all groups of which the specified reach is a member.
	 */
	getGroupNamesFor : function(reachId) {
		var groupList = this.getAllGroups();
		var groupNameList = [];

		for (var i = 0; i < groupList.length; i++) {
			var group = groupList[i];
			var reachList = group.reach;

			// Iterate over the reaches for each group
			for (var j = 0; j < reachList.length; j++) {
				if (reachList[j]["@id"] == reachId) {
					// If the reach is a member, push the group name into the array
					groupNameList.push(group["@name"]);
					break;
				}
			}
		}

		return groupNameList;
	},

	/**
	 * Determines if a HUC is part of a group
	 */
	getHUCGroupForReach : function(hucs) {
		var groupList = this.getAllGroups();
		var groupNameList = [];

		for (var i = 0; i < groupList.length; i++) {
			var logicalset = groupList[i].logicalSet;

			// Iterate over the logical sets for each group
			for (var j = 0; j < logicalset.length; j++) {
				var criteria = logicalset[j].criteria;
				for (var x in hucs) {
					if (criteria["#text"] == hucs[x]["@id"]) {
						// If the reach is a member, push the group name into the array
						groupNameList.push({name:groupList[i]["@name"], whatToAdd:x});
						j = logicalset.length;
						break;
					}
				}
			}
		}
		return groupNameList;
	},

	 /**
	  * Returns true if the specified reach is a member of the specified group,
	  * false otherwise.
	  */
	isReachMemberOf : function(reachId, groupName) {
		var group = this.getGroup(groupName);

		if (group != null) {
			var reachList = group.reach;

			for (var i = 0; i < reachList.length; i++) {
				if (reachList[i]["@id"] == reachId) {
					return true;
				}
			}
		}

		return false;
	},

	/**
	 * Adds a reach to the group and returns that reach object.
	 */
	addReachToGroup : function(groupName, reachId, reachName) {
		// Get the group and push the reach onto it
		var group = this.getGroup(groupName);
		var reach = {
				"@id": reachId,
				"@name": reachName,
				adjustment: []};
		group.reach.push(reach);
	    this.fireContextEvent("adjustment-group-changed");

		this.changed();
		return reach;
	},

	/**
	 * Removes a reach from the group.
	 */
	removeReachFromGroup : function(groupName, reachId) {
		var group = this.getGroup(groupName);
		if (group != null) {
			// Iterate over the reaches
			var reachList = group.reach;
			for (var i = 0; i < reachList.length; i++) {
				// If the reach matches, remove it
				var reach = reachList[i];
				if (reach["@id"] == reachId) {
					reachList.splice(i, 1);
				    this.fireContextEvent("adjustment-group-changed");
					this.changed();
				}
			}
		}
	},

	/**
	 * Returns true if the specified logical set is a member of the specified group,
	 * false otherwise.
	 */
	isLogicalSetMemberOf : function(groupName, setType, setId) {
		var group = this.getGroup(groupName);

		if (group != null) {
			var logicalSetList = group.logicalSet;
			for (var i = 0; i < logicalSetList.length; i++) {

				// If the logical set matches, return true
				var setCriteria = logicalSetList[i].criteria;
				if (setCriteria["@attrib"] == setType && setCriteria["#text"] == setId) {
					return true;
				}
			}
		}

		return false;
	},

	/**
	 * Adds a logical set to the group and returns that logical set object.
	 */
	addLogicalSetToGroup : function(groupName, setType, setId, setName) {
	    // Get the group and push the logical set onto it
		//make sure set doesn't already exist in group
		if (!this.isLogicalSetMemberOf(groupName, setType, setId)) {

			var logicalSet;
			var group = this.getGroup(groupName);

				if (setType == 'upstream') {

					logicalSet = {
							criteria: {
									"@name": setName,
									"@attrib": 'reach',
									"@relation": 'upstream',
									"#text": setId
							}
					};
					group.logicalSet.push(logicalSet);
				} else {

					logicalSet = {
							criteria: {
									"@name": setName,
									"@attrib": setType,
									"@relation": 'in',
									"#text": setId
							}
					};
					group.logicalSet.push(logicalSet);
			}
			this.fireContextEvent("adjustment-changed");
		    this.changed();
		}
	    return logicalSet;
	},

	/**
	 * Removes a logical set from a group.
	 */
	removeLogicalSetFromGroup : function(groupName, setType, setId) {
	    var group = this.getGroup(groupName);
	    if (group != null) {
	        // Iterate over the logical sets
	        var logicalSetList = group.logicalSet;
	        for (var i = 0; i < logicalSetList.length; i++) {

	            // If the logical set matches, remove it
	            var setCriteria = logicalSetList[i].criteria;
	            if (setCriteria["@attrib"] == setType && setCriteria["#text"] == setId) {
	                logicalSetList.splice(i, 1);
	                this.changed();
	            }
	        }
	    }
	},
	
	/**
	 * Adds an adjustment to the specified reach.
	 */
	addAdjustment : function(reachId, reachName, srcIndex, value) {
		// Get the reach (from the individual group)
		var reach = this.getAdjustedReach(reachId);
		if (reach == null) {
			reach = {
				"@id" : reachId,
				"@name" : reachName,
				adjustment : []
			};
			// Add the reach if it is not already part of the individual group
			this.PredictionContext.adjustmentGroups.individualGroup.reach.push(reach);
		}

		// Get the adjustment
		var adjustment = null;
		var adjustmentList = reach.adjustment;
		for (var i = 0; i < adjustmentList.length; i++) {
			if (adjustmentList[i]["@src"] == srcIndex) {
				adjustment = adjustmentList[i];
				break;
			}
		}

		// Create and push the adjustment if it doesn't exist, otherwise edit the value
		if (adjustment == null) {
			adjustment = {
				"@src" : srcIndex,
				"@abs" : value
			};
			reach.adjustment.push(adjustment);
			this.fireContextEvent("adjustment-changed");
		} else {
			adjustment["@abs"] = value;
		}
	    this.changed();
	},

	/**
	 * Removes an adjustment from the specified reach.  This function also removes
	 * the reach from the individual group if the specified adjustment is the last
	 * one attached to the reach.
	 */
	removeAdjustment : function(reachId, srcIndex) {
	    // Get the reach
	    var reach = this.getAdjustedReach(reachId);
	    if (reach != null) {

	        // Get the adjustment if it exists and remove it
	        var adjustmentList = reach.adjustment;
	        for (var i = 0; i < adjustmentList.length; i++) {
	            if (adjustmentList[i]["@src"] == srcIndex) {
	                adjustmentList.splice(i, 1);
	                this.fireContextEvent("adjustment-changed");
	                this.changed();
	            }
	        }

	        // Remove the reach if there are no more adjustments
	        if (adjustmentList.length == 0) {
	            this.removeAdjustedReach(reachId);
	        }
	    }
	},

	/**
	 * Removes a reach from the individual group.
	 */
	removeAdjustedReach : function(reachId) {
	    var adjustedReachList = this.getAllAdjustedReaches();
	    for (var i = 0; i < adjustedReachList.length; i++) {
	        if (adjustedReachList[i]["@id"] == reachId) {
	            adjustedReachList.splice(i, 1);
	            this.fireContextEvent("adjustment-changed");
	            this.changed();
	        }
	    }
	},

	/**
	 * Returns the specified adjusted reach if it exists in the individual group,
	 * null otherwise.
	 */
	getAdjustedReach : function(reachId) {
		var reach = null;

		var adjustedReachList = this.getAllAdjustedReaches();
		for (var i = 0; i < adjustedReachList.length; i++) {
			if (adjustedReachList[i]["@id"] == reachId) {
				reach = adjustedReachList[i];
				break;
			}
		}

	    return reach;
	},

	/**
	 * Returns an array of all of the reaches that have had their treatment values
	 * adjusted.
	 */
	getAllAdjustedReaches : function() {
		return this.PredictionContext.adjustmentGroups.individualGroup.reach;
	},
	
	removeAllAdjustedReaches : function() {
		if(this.PredictionContext.adjustmentGroups.individualGroup.reach.length > 0) {
			this.PredictionContext.adjustmentGroups.individualGroup.reach = [];
			this.fireContextEvent("adjustment-changed");
	        this.changed();
		}
	},
	
	/**
	 * Adds the specified reach to the list of targeted reaches.
	 * Returns true to indicate the reach was added.
	 */
	addToTargetReaches : function(reachId, reachName){
	    if (this.isReachTarget(reachId)) {
	        return false;
	    }

	    var reaches = this.PredictionContext.terminalReaches.reach;
	    if (!reaches) reaches = [];
	    reaches.push({
	        '@id': reachId,
	        '@name': reachName
	    });
	    this.PredictionContext.terminalReaches.reach = reaches;

      this.fireContextEvent("targets-changed");
	    this.changed();
			
			return true;
	},
	
	/**
	 * Adds an array of reaches as targets.  For adding multiple reaches,
	 * use this method instead of addToTargetReaches, since that method fires
	 * changes events for each addition.
	 * 
	 * Each item in the array is expected to be an Ojbect with these properties:
	 * reachId Integer id for the reach
	 * reachName Name of the reach
	 * 
	 * Returns the number of reaches added, which may be less then the number
	 * (it may be zero) if the reaches already were target reaches.
	 */
	addAllToTargetReaches : function(reachArray){
		
		var chgCount = 0;
		
		var reaches = this.PredictionContext.terminalReaches.reach;
		if (!reaches) reaches = [];
		
		for (var i = 0, len = reachArray.length; i < len; i += 1) {
			
			if (! this.isReachTarget(reachArray[i].reachId)) {
				
				reaches.push({
						'@id': reachArray[i].reachId,
						'@name': reachArray[i].reachName
				});
				
				chgCount++;
				
			}
		}
		
		if (chgCount > 0) {
			this.PredictionContext.terminalReaches.reach = reaches;
			this.fireContextEvent("targets-changed");
			this.changed();
		}
		
		return chgCount;
	},

	/**
	 * Removes the specified reach from the list of targeted reaches.
	 */
	removeReachFromTargets : function(reachId) {
	    var targets = this.PredictionContext.terminalReaches.reach;
	    for (var i = 0, len = targets.length; i < len; i += 1) {
	        if (targets[i]["@id"] === reachId) {
	            targets.splice(i, 1);
	            this.fireContextEvent("targets-changed");
	            this.changed();
	            break;
	        }
	    }
	},
	
	removeAllReachesFromTargets: function() {
		var targets = this.getAllTargetedReaches();
		if(targets.length == 0) {
			return;
		}
		Sparrow.SESSION.consolidateEvents();
		while(targets.length > 0) {
			Sparrow.SESSION.removeReachFromTargets(targets[0]['@id']);
		}
		Sparrow.SESSION.releaseEvents();
        this.fireContextEvent("targets-changed");
	},

	/**
	 * Returns true if the specified reach is flagged as a targeted
	 * reach, false otherwise.
	 */
	isReachTarget : function (reachId) {
	    var targets = this.PredictionContext.terminalReaches.reach;
	    for (var i = 0, len = targets.length; i < len; i += 1) {
	        if (targets[i]["@id"] === reachId) {
	            return true;
	        }
	    }
	    return false;
	},
			
	hasEnabledTargetReaches : function() {
		return this.PredictionContext.terminalReaches.reach != null &&
				this.PredictionContext.terminalReaches.reach.length > 0;
	},

	/**
	 * Returns an array containing all of the targeted reaches.
	 */
	getAllTargetedReaches : function() {
	    return this.PredictionContext.terminalReaches.reach;
	},
	
	/**
	 * GROUP treatment functions
	 */
	getAllTreatedGroups : function() {
		var reachGroups = this.PredictionContext.adjustmentGroups.reachGroup;
		var treatedGroups = [];
		for (var i = 0; i < reachGroups.length; i++) {
			if (reachGroups[i].adjustment.length > 0) {
				treatedGroups.push(reachGroups[i]);
			}
		}
		return treatedGroups;
	}
};



/*
 * The PredictionContext object.  This object holds all data related to the
 * user's current PredictionContext and is based entirely upon options chosen
 * within the user interface.
 *
 */
Sparrow.CONTEXT = new Sparrow.ux.Context({});
Sparrow.SESSION = new Sparrow.ux.Session({context: Sparrow.CONTEXT});










