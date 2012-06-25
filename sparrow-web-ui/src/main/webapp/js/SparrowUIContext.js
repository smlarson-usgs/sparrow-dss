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
            what_to_map: "reach",
            
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
					reachOverlay: -100,
					huc8Overlay: -75,
					dataLayerOpacity: 100,
			
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
        	seriesConstituent: "",
        	seriesUnits: "",
        	seriesName: "",
        	seriesDescription: "",
        	docUrl: "",
        	modelName: "",
        	rowCount: "",
        	themeName: "",
        	originalBoundSouth: null,
        	originalBoundNorth: null,
        	originalBoundEast: null,
        	originalBoundWest: null,
        	lastNominalComparison: null,	/* comparison last time comp was disabled (for re-enabling) */
        	
        	previousState: null, /* previous context and perm state, serialized as json */
        	previousTermReachesState: null, /* previous terminalReaches, serialized as json */
        	previousAdjGroupsState: null, /* previous adjustmentGroups, serialized as json */
        	
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
	return this.load(this.context.sessionTemplate);
};
Sparrow.ux.Session.prototype = {
	load : function(jsonOrString) {
		// JSONify the input if it is a string.
		var data = (Ext.isString(jsonOrString))? Ext.decode(jsonOrString): jsonOrString;

		// This is done so that the Session object may be manipulated just as the original JSON object
		this.PredictionContext = data.PredictionContext;
		this.PermanentMapState = data.PermanentMapState;
		this.TransientMapState = data.TransientMapState;
		
		if (this.TransientMapState == null) {
			//This happens during pre-session load
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

	getData : function(){return {
				PredictionContext: this.PredictionContext,
				PermanentMapState: this.PermanentMapState
			};
	},

	getPredictionContext: function(){return this.PredictionContext;},

	/** Returns PredictionContext as XML String */
	getPredictionContextAsXML : function(){
		return Sparrow.USGS.JSONtoXML({ PredictionContext : this.PredictionContext});
	},
	
	/** Returns adjustments as XML String */
	getAdjustmentGroupsAsXML : function(){
		return Sparrow.USGS.JSONtoXML({ adjustmentGroups : this.PredictionContext.adjustmentGroups});
	},

	/** Returns SESSION/SESSION.data as a JSON String for serialization. */
	asJSON : function(){ return Ext.util.JSON.encode(this.getData()); }, // might need to be this.getData()

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
			this.fireContextEvent('changed');
		}
	},

	/** Registers change event */
	changed : function(){
		this.fireContextEvent('changed');
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

	/** marks the current state for future comparison */
	mark: function(){
		this.TransientMapState.previousState = this.asJSON();
		this.TransientMapState.previousTermReachesState = Ext.util.JSON.encode(this.PredictionContext.terminalReaches);
		this.TransientMapState.previousAdjGroupsState = Ext.util.JSON.encode(this.PredictionContext.adjustmentGroups);    	
	},

	/** Compares current state to argument */
	isChanged : function(){
		return (this.TransientMapState.previousState != this.asJSON());
	},
	
	isTermReachesChanged : function() {
		return (this.TransientMapState.previousTermReachesState != Ext.util.JSON.encode(this.PredictionContext.terminalReaches));
	},
	
	isAdjGroupsChanged : function() {
		return (this.TransientMapState.previousAdjGroupsState != Ext.util.JSON.encode(this.PredictionContext.adjustmentGroups));
	},

	// Doesn't seem to be used?? [IK]
	setWhatToMap: function(val) {
		this.PermanentMapState["what_to_map"] = val;
	    this.changed();
	},
	
	getBinType: function() {
		return this.PermanentMapState.binType;
	},
	
	setBinType: function(type) {
		this.PermanentMapState.binType = type;
	},
	
	getBinTypeName: function() {
		var type = this.PermanentMapState.binType;
		if (type == 'EQUAL_COUNT') {
			return 'Equal Count';
		} else if (type == 'EQUAL_RANGE') {
			return 'Equal Range';
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
	 * @param structuredBinningData
	 */
	setBinData: function(structuredBinData) {
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
		
	    this.changed();
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
            	
            	displayBins.push({ low: lowVal, high: highVal });
            	functionalBins.push({ low: lowVal, high: highVal });
            	boundUnlimited.push({ low: false, high: false });
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
			this.setBinAuto(this.PermanentMapState.autoBin);
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
	

	/**
	 * If setting the dataSeries, update the name and description as well so that
	 * events have access to the current info.
	 * @param dataSeries
	 * @param dataSeriesName
	 * @param dataSeriesDescription
	 */
	setDataSeries: function(dataSeries, dataSeriesName, dataSeriesDescription) {
		var oldVal = this.PredictionContext.analysis.dataSeries["#text"];
		this.PredictionContext.analysis.dataSeries["#text"] = dataSeries;
		this.TransientMapState.seriesName = dataSeriesName;
		
		if (dataSeriesDescription) {
			this.TransientMapState.seriesDescription = dataSeriesDescription;
		} else {
			this.TransientMapState.seriesDescription = dataSeriesName;
		}
		
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
	setCalibSitesOverlayEnabled: function(enable, newOpacity) {
		this._setAnyDataLayerEnabled(enable, newOpacity, "calibSites", "calibsites-changed");
	},
	
	isCalibSitesOverlayEnabled: function() {
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
	setReachOverlayEnabled: function(enable, newOpacity) {
		this._setAnyDataLayerEnabled(enable, newOpacity, "reachOverlay", "reachoverlay-changed");
	},

	isReachOverlayEnabled: function() {
		return (this.PermanentMapState["reachOverlay"] > 0);
	},
	
	getReachOverlayOpacity: function() {
		return this._getAnyDataLayerOpacity("reachOverlay");
	},
	
	/**
	 * Enables/disables and assigns the opacity of this data-related layer.
	 * @param enable required
	 * @param newOpacity optional if enabling the layer.  Otherwise it uses
	 * the previous opacity.
	 */
	setHuc8OverlayEnabled: function(enable, newOpacity) {
		this._setAnyDataLayerEnabled(enable, newOpacity, "huc8Overlay", "huc8overlay-changed");
	},

	isHuc8OverlayEnabled: function() {
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
		var layers = map1.layerManager.getAvailableLayers();
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

	setSeriesConstituent: function(val) {
		this.TransientMapState.seriesConstituent = val;
	},
	
	setSeriesUnits: function(val) {
		this.TransientMapState.seriesUnits = val;
	},
	
	setSeriesName: function(val) {
		this.TransientMapState.seriesName = val;
	},
	
	getSeriesName: function() {
		return this.TransientMapState.seriesName;
	},
	
	setSeriesDescription: function(val) {
		this.TransientMapState.seriesDescription = val;
	},
	
	getSeriesDescription: function() {
		return this.TransientMapState.seriesDescription;
	},
	
	getLegendTitle: function() {
		var title = this.TransientMapState.seriesName;
		if(this.hasAdjustments() && 
				this.getComparisons() != 'none' && 
				this.getComparisons() != 'percent') {
			title = "Change in <br/>"+title;
		}
		return title;
	},
	
	getLegendUnitsAndConstituent: function() {
		
		var u = Sparrow.USGS.prettyPrintUnitsForHtml(this.TransientMapState.seriesUnits);
		var c = this.TransientMapState.seriesConstituent;
		
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
		this.TransientMapState.originalBoundSouth = val;
	},
	
	setOriginalBoundNorth: function(val) {
		this.TransientMapState.originalBoundNorth = val;
	},

	setOriginalBoundEast: function(val) {
		this.TransientMapState.originalBoundEast = val;
	},

	setOriginalBoundWest: function(val) {
		this.TransientMapState.originalBoundWest = val;
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
	
	setRowCount: function(val) {
		this.TransientMapState.rowCount = val;
	},
	
	getRowCount: function() {
		return this.TransientMapState.rowCount;
	},
	
	setThemeName: function(val) {
		this.TransientMapState.themeName = val;
	},
	
	getThemeName: function() {
		return this.TransientMapState.themeName;
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
	 */
	addToTargetReaches : function(reachId, reachName){
	    if (this.isReachTarget(reachId)) {
	        return;
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
 * Note that this object does not necessarily correspond with the current
 * context_id variable being used throughout the scripts.  The context_id is only
 * updated when a PredictionContext has been submitted to the web service, so
 * the variable will only be up to date as of the last submittal of this object.
 */
Sparrow.CONTEXT = new Sparrow.ux.Context({});
Sparrow.SESSION = new Sparrow.ux.Session({context: Sparrow.CONTEXT});










