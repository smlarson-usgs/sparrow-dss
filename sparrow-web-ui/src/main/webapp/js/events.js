Sparrow.events.EventManager = function(){ return{
	//register events TODO may want to create methods to register certain events, to split events into logical types
	registerSessionEvents : function(){
		Sparrow.CONTEXT.on('map-updated-and-synced', function() {
			//Current series
			var series = Sparrow.SESSION.getSeriesName();
			
		    //hide any out of sync messages
		    if(document.getElementById('map-sync-warning').className.indexOf('x-hidden') < 0) {
				document.getElementById('map-sync-warning').className += " x-hidden";
		    }    

		    Ext.getCmp('update-map-button-panel').setStatusInSync(series);
		    EXPORT_DATA_WIN.enable();	//One way trip to enable
		    
		    Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(false);
		});
		
		Sparrow.CONTEXT.on('dataseries-changed', function() {
			Sparrow.handlers.UiComponents.updateComparisons();
			
			//Called, but no distinction currently from just 'changed'
			//Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(true);
		});
		
		Sparrow.CONTEXT.on('model-source-changed', function() {
			Sparrow.handlers.UiComponents.updateComparisons();
		});

		Sparrow.CONTEXT.on('changed', function(){
			var isChanged = Sparrow.SESSION.isChanged();
			if (isChanged) {
				//re-enable the gen map button
				Ext.getCmp('update-map-button-panel').setStatusOutOfSync(Sparrow.SESSION.getSeriesName());
				
				//Notify user with a visual cue
				document.getElementById('map-sync-warning').className = document.getElementById('map-sync-warning').className.replace(/x-hidden/g, '');
				
				Sparrow.SESSION.fireContextEvent("targets-changed");

				//Update the ID window if open
				var reachIdentifyWindow = Ext.getCmp('reach-identify-window');
				if (reachIdentifyWindow && reachIdentifyWindow.isApplying) {
					// refresh the window
					var reachId = reachIdentifyWindow.getReachId();
					IDENTIFY.identifyReach(null, null, reachId, 1, true);	
				}
				
			}
		});
		
		Sparrow.CONTEXT.on("targets-changed", function() {
			if (Sparrow.SESSION.isTermReachesChanged()) {
				var targetPanel = Ext.getCmp('main-targets-tab');
				targetPanel.treePanel.loadTree();
				
				//update the delivery instructions to reflect
				Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(true);
				
				var targetsCount = Sparrow.SESSION.getAllTargetedReaches();
				targetsCount = targetsCount ? targetsCount.length : 0;
				
				if(targetsCount > 0) 
					targetPanel.showInstructions();
				else
					targetPanel.hideInstructions();
			}
		});
		
		Sparrow.CONTEXT.on('comparisonchanged', function() {
			Sparrow.handlers.UiComponents.updateComparisons();
		});
		
		/**
		 * Called when the state loading is complete.  Must happen after
		 * 'finished-loading-pre-session' if it is a pre-session.
		 */
		Sparrow.CONTEXT.on('finished-loading-state', function() {
			//Update the overlay controls to the current state
			var mapOptionsTab = Ext.getCmp('map-options-tab');
			mapOptionsTab.syncDataOverlayControlsToSession();
			
			//Update the map to the current state
			Sparrow.handlers.MapComponents.updateCalibrationLayerOnMap();
			Sparrow.handlers.MapComponents.updateReachOverlayOnMap();
			Sparrow.handlers.MapComponents.updateHuc8OverlayOnMap();
			
		});
		
		/**
		 * When a predefined session is loaded, this method can be called to force
		 * the mapping framework to display all the layers specified by that session.
		 * 
		 * Its possible that different layers will be added in the future and turned
		 * on by default, so this method should ensure that unfound layers are turned
		 * off.
		 */
		Sparrow.CONTEXT.on('finished-loading-pre-session', function() {
			
			//Remove all data-related layers (the finished-loading-state will put them back)
			Sparrow.handlers.MapComponents.turnOffDataRelatedLayers();
			
			if (! Sparrow.SESSION.PermanentMapState["mapLayers"]) {
				//This is mostly to handle old sessions that do not have map layers
				//defined.
				Sparrow.SESSION.PermanentMapState["mapLayers"] = new Object();
				

				//Pick up whatever map layers are currently turned on
				var mapLayers = map1.layerManager.activeMapLayers;
				for (var i=0; i<mapLayers.length; i++) {
					Sparrow.SESSION.setMapLayerEnabled(mapLayers[i].id, mapLayers[i].opacity);
				}

				//map layers that are selected, but not active due to being out of scale or bbox
				mapLayers = map1.layerManager.selectedNotAvailable;
				for (var i=0; i<mapLayers.length; i++) {
					Sparrow.SESSION.setMapLayerEnabled(mapLayers[i].id, mapLayers[i].opacity);
				}
				
			} else {
				var allLayers = Sparrow.SESSION.getAvailableMapLayers();
				for (var i=0; i<allLayers.length; i++) {
					map1.layerManager.removeLayerFromMap(allLayers[i].id);
				}
				
				
				for (var layerId in Sparrow.SESSION.PermanentMapState["mapLayers"]) {
					if (Sparrow.SESSION.isMapLayerEnabled(layerId)) {
						Sparrow.SESSION.fireContextEvent("mapLayers-changed", layerId);
					}
				}
			}
		});
		
		Sparrow.CONTEXT.on('adjustment-changed', Sparrow.handlers.UiComponents.adjustmentChange);
		Sparrow.CONTEXT.on('adjustment-group-changed', Sparrow.handlers.UiComponents.updateAdjustmentsTree);

		/**
		 * Invoked when the user changes the map selection b/t Reaches or Catchments.
		 */
		Sparrow.CONTEXT.on("what-to-map", function(){
			//Update the overlay controls to the current state
			var mapOptionsTab = Ext.getCmp('map-options-tab');
			mapOptionsTab.syncDataOverlayControlsToSession();
			
			//Update the reach overlay, which may now be newly enable or disabled.
			Sparrow.handlers.MapComponents.updateReachOverlayOnMap();
		});
		
		Sparrow.CONTEXT.on("calibsites-changed", function(){
			Sparrow.handlers.MapComponents.updateCalibrationLayerOnMap();
			Sparrow.handlers.MapComponents.updateCalibrationSiteIdControls();
		});
		
		Sparrow.CONTEXT.on("reachoverlay-changed", function() { Sparrow.handlers.MapComponents.updateReachOverlayOnMap(); });
		Sparrow.CONTEXT.on("huc8overlay-changed", function() { Sparrow.handlers.MapComponents.updateHuc8OverlayOnMap(); });
		
		Sparrow.CONTEXT.on("dataLayerOpacity-changed", function() {
			if(map1.layerManager.getMapLayer(Sparrow.config.LayerIds.mainDataLayerId))
				map1.layerManager.getMapLayer(Sparrow.config.LayerIds.mainDataLayerId).setOpacity(Sparrow.SESSION.getDataLayerOpacity()); //TODO global map
		});
		
		Sparrow.CONTEXT.on("mapLayers-changed", Sparrow.handlers.MapComponents.toggleMapLayer);
	}
}}();

//TODO move this into its own file if needed
Sparrow.handlers.DownstreamTrackingInstructions = function(){ 
	var _downstreamTabLinkMsg = "<b>On the Map:</b><br />" +
		"Select one of the <i><b>Downstream Tracking</b></i> " +
		"data series at the top of the " +
		"<a href='javascript:GOTO_MAP_OPTIONS_TAB()'>Display Results tab</a>.<br/><br/>" +
		"<b>In a Report:</b><br />" +
		"<a href='javascript:displayDeliverySummaryReport()'>Open the Summary Reports</a><br />" +
		"The summary reports show load delivered to the downstream reaches and the originating region (state or HUC).";
	
	return{
	syncDeliveryTabInstructions : function(mapOutOfSync) {
	    //Downstream tracking
		var targetPanel = Ext.getCmp('main-targets-tab');
		var targetReaches = Sparrow.SESSION.getAllTargetedReaches();
		
		if (targetReaches.length == 0) {
			Sparrow.handlers.DownstreamTrackingInstructions.setNoDeliveryReachesSelected(Sparrow.SESSION.isDeliveryDataSeries());
		} else if (! Sparrow.SESSION.isDeliveryDataSeries()) {
			Sparrow.handlers.DownstreamTrackingInstructions.setNoDeliverySeriesSelected();
		} else {
			if (mapOutOfSync) {
				Sparrow.handlers.DownstreamTrackingInstructions.setDeliveryOKButOutOfSync();
			} else {
				Sparrow.handlers.DownstreamTrackingInstructions.setDeliveryOK();
			}
		}
	},
	
	setNoDeliverySeriesSelected : function() {
		var comp = Ext.getCmp('main-targets-tab').displayResultsInstructions;
		if (comp.body) {
			comp.body.update(_downstreamTabLinkMsg);
		}
	},
	
	setNoDeliveryReachesSelected : function(deliverySeriesIsChosen) {
		var comp = Ext.getCmp('main-targets-tab').displayResultsInstructions;
		if (comp.body) {
			var text;
			if (deliverySeriesIsChosen) {
				text = "A delivery data series is chosen, but no downstream reaches are selected.";
			} else {
				text = "[No downstream reaches have been chosen]";
			}
			Ext.getCmp('main-targets-tab').displayResultsInstructions.body.update(text);
		}
	},
	
	setDeliveryOK : function() {
		var comp = Ext.getCmp('main-targets-tab').displayResultsInstructions;
		if (comp.body) {
    		var series = Sparrow.SESSION.getSeriesName();
    		Ext.getCmp('main-targets-tab').displayResultsInstructions.body.update("Currently displaying the delivery data series <i><b>" + series + "</b></i>.<br/><br/>"+_downstreamTabLinkMsg);
		}
	},
	
	setDeliveryOKButOutOfSync : function() {
		var comp = Ext.getCmp('main-targets-tab').displayResultsInstructions;
		if (comp.body) {
    		var series = Sparrow.SESSION.getSeriesName();
    		Ext.getCmp('main-targets-tab').displayResultsInstructions.body.update("The delivery data series <i><b>" + series + "</b></i>&nbsp;&nbsp;is chosen, but the map needs to be redrawn.  Click Update Map, below.");
		}
	}
}}();

Sparrow.handlers.UiComponents = function(){ return{
	updateComparisons : function(){
		var comp = Ext.getCmp('map-options-tab').comparisonCombo;
		var rad = Ext.getCmp('map-options-tab').mapUnitsRadioGrp;
		var ds = Ext.getCmp('map-options-tab').dataSeriesCombo;

		var modelSourceVal = Sparrow.SESSION.getDataSeriesSource();
		var comparisonVal = Sparrow.SESSION.getComparisons();
		var allowPercUnits = ds.findRecord(ds.valueField, Sparrow.SESSION.getDataSeries()).data.allowPercUnits;
		
		comp.suspendEvents();
		rad.suspendEvents();
		
		//set values of two fields
		if(!comparisonVal) {
			comp.setValue('none');
		} else if(comparisonVal=='') {
			rad.items.items[0].setValue(true);
		} else if(comparisonVal=='percent') {
			rad.items.items[1].setValue(true);
		} else {
			comp.setValue(comparisonVal);
			rad.items.items[0].setValue(true);
		}
		
		//set enable/disable states
		if (Sparrow.SESSION.hasEnabledAdjustments())
			comp.enable();
		else 
			comp.disable();
		
		
		if(allowPercUnits && modelSourceVal!='0' && modelSourceVal && (comparisonVal=='none' || comparisonVal=='percent' || comparisonVal=='')) {
			rad.enable();
		} else {
			rad.items.items[0].setValue(true);
			rad.disable();
		}
		
		comp.resumeEvents();
		rad.resumeEvents();
	},
	
	updateAdjustmentsTree : function() {
		//Reload the adjustments tree
		var adjustmentsTab = Ext.getCmp('main-adjustments-tab');
		adjustmentsTab.treePanel.loadTree();
		
		var groups = Sparrow.SESSION.getAllGroups();
		groups = groups ? groups.length : 0;
		
		var reaches = Sparrow.SESSION.getAllAdjustedReaches();
		reaches = reaches ? reaches.length : 0;
		
		if(groups > 0 || reaches > 0) 
			adjustmentsTab.showInstructions();
		else
			adjustmentsTab.hideInstructions();
	},
	
	adjustmentChange : function() {
		var ds = Ext.getCmp('map-options-tab').dataSeriesCombo;
		if (Sparrow.SESSION.hasEnabledAdjustments()) {	
			//remove uncertainty data series
			ds.store.each(function(r) {
				if(r.data.group && r.data.group.toLowerCase().indexOf('uncertainty') >= 0) ds.store.remove(r);
			});
			ds.resetDropDownList();
			
			Sparrow.SESSION.resetComparisonToLastUserSelectionIfDisabled();
		} else {
			//first check if we don't have any uncertainty terms
			ds.store.each(function(r) {
				if(r.data.group && r.data.group.toLowerCase().indexOf('uncertainty') >= 0) return;
			});
			
			//reinitialize the original store
			ds.store = new Ext.data.GroupingStore({
				reader: new Ext.data.ArrayReader({}, Sparrow.config.ComboValues.dataSeriesRecordDef),
				data: Sparrow.config.ComboValues.dataSeries,
		       sortInfo:{field: 'group', direction: "ASC"},
		       groupField: 'group'
			});
			
			Sparrow.SESSION.clearComparisonUserSelection();
			
			ds.resetDropDownList();
		}
		
		//The UI context doesn't know the state of the combo (enabled or val)
		//so we need to tell it to update
		Sparrow.handlers.UiComponents.updateComparisons();
		Sparrow.handlers.UiComponents.updateAdjustmentsTree();
	}
}}();

Sparrow.handlers.MapComponents = function(){ 
	var _isLayerActiveInMap = function(layerId) {
		var isActive = false;
		
		//Check the active and displayed layers
		var mapLayers = map1.layerManager.activeMapLayers;
		for (var i=0; i<mapLayers.length; i++) {
			if (mapLayers[i].id == layerId) {
				isActive = true;
				break;
			}
		}

		if (!isActive) {}
			//check the map layers that are selected, but are out of scale or bbox
			mapLayers = map1.layerManager.selectedNotAvailable;
			for (var i=0; i<mapLayers.length; i++) {
				if (mapLayers[i].id == layerId) {
					isActive = true;
					break;
				}
			}
		return isActive;
	};
	
	return {
	toggleMapLayer : function(layerId){	
		//User has enabled/disabled a mapLayer, or changed opacity.
		//This event should also fire after mapLayer selections are
		//loaded from a predefined theme or at startup.
		var isEnabled = Sparrow.SESSION.isMapLayerEnabled(layerId);
		var opacity = Sparrow.SESSION.getMapLayerOpacity(layerId);
		
		if (isEnabled) {
			if (! _isLayerActiveInMap(layerId)) {
				map1.appendLayer(layerId);
			}
			if(map1.layerManager.getMapLayer(layerId)) map1.layerManager.getMapLayer(layerId).setOpacity(opacity);
		} else {
			map1.removeLayer(layerId);
		}
	},
	
	turnOffDataRelatedLayers: function() {
		map1.layerManager.unloadMapLayer(Sparrow.config.LayerIds.calibrationSiteLayerId);
		map1.layerManager.unloadMapLayer(Sparrow.config.LayerIds.reachLayerId);
		map1.layerManager.unloadMapLayer(Sparrow.config.LayerIds.huc8LayerId);
	},

	updateCalibrationLayerOnMap : function() {
		var layerId = Sparrow.config.LayerIds.calibrationSiteLayerId;
		var opacity = Sparrow.SESSION.getCalibSitesOverlayOpacity();
		var showRequested = Sparrow.SESSION.isCalibSitesOverlayRequested();
		var isShowing = (map1.getMapLayer(layerId) != null);
		
	    if (showRequested && ! isShowing) {
	        var urlParams = 'model_id=' + model_id;
	        
	        map1.layerManager.unloadMapLayer(layerId);
	        map1.appendLayer(
	        	new JMap.web.mapLayer.WMSLayer({
	        		id: layerId, zDepth: 59990, opacity: opacity,
	        		scaleMin: 0, scaleMax: 100,
	        		baseUrl: 'calibSiteOverlay?' + urlParams,
	        		title: "Calibration Sites",
	        		name: "calibration_sites",
	        		isHiddenFromUser: true,
	        		description: 'Sites used to calibration the Sparrow Model'
	        	})
	        );
	    } else if (showRequested) {
	    	map1.layerManager.getMapLayer(layerId).setOpacity(opacity);
	    } else {
	    	map1.layerManager.unloadMapLayer(layerId);
	    }
	},
	
	updateCalibrationSiteIdControls: function() {
		if(Sparrow.SESSION.isCalibSitesOverlayRequested()){
			Ext.getCmp('mapToolButtonsCalibrationSiteIdentify').show();
		} else {
			Ext.getCmp('mapToolButtonsCalibrationSiteIdentify').hide();
			//reenable drag tool if calibration id is on
			if(Ext.getCmp('mapToolButtonsCalibrationSiteIdentify').pressed) {
				var b = Ext.getCmp('mapToolButtonsDrag');
				if (!b.pressed) b.toggle();
				map1.setMouseAction(null);
			}
			
		}
		Ext.getCmp('mapToolButtonsCalibrationSiteIdentify').ownerCt.doLayout();
	},
	
	/**
	 * Updates the reach overlay layer on the map to match the current session
	 * state.
	 */
	updateReachOverlayOnMap : function() {
		var layerId = Sparrow.config.LayerIds.reachLayerId;
		var opacity = Sparrow.SESSION.getReachOverlayOpacity();
		var showRequested = Sparrow.SESSION.isReachOverlayRequested() && Sparrow.SESSION.isReachOverlayEnabled();
		var isShowing = (map1.getMapLayer(layerId) != null);

		if (showRequested && ! isShowing) {
	        var urlParams = 'model_id=' + model_id + '&context_id=' + context_id;

	        map1.layerManager.unloadMapLayer(layerId);
	        map1.appendLayer(
	        	new JMap.web.mapLayer.WMSLayer({
	        		id: layerId,  zDepth: 59995, opacity: opacity,
	        		scaleMin: 0, scaleMax: 100,
	        		baseUrl: 'reachOverlay?' + urlParams,
	        		title: "Reach Overlay",
	        		name: "reach_overlay",
	        		isHiddenFromUser: true,
	        		description: 'Reaches overlayed in grey'
	        	})
	        );
	    } else if (showRequested) {
	    	map1.layerManager.getMapLayer(layerId).setOpacity(opacity);
	    } else {
	    	map1.layerManager.unloadMapLayer(layerId);
	    }
	},

	updateHuc8OverlayOnMap : function() {
		var layerId = Sparrow.config.LayerIds.huc8LayerId;
		var opacity = Sparrow.SESSION.getHuc8OverlayOpacity();
		var showRequested = Sparrow.SESSION.isHuc8OverlayRequested();
		var isShowing = (map1.getMapLayer(layerId) != null);

		if (showRequested && ! isShowing) {
	        var urlParams = 'model_id=' + model_id;

	        map1.layerManager.unloadMapLayer(layerId);
	        map1.appendLayer(
	        	new JMap.web.mapLayer.WMSLayer({
	        		id: layerId, zDepth: 59992, opacity: opacity,
	        		scaleMin: 0, scaleMax: 100,
	        		baseUrl: 'huc8Overlay?' + urlParams,
	        		title: "HUC8 Overlay",
	        		name: "huc8_overlay",
	        		isHiddenFromUser: true,
	        		description: 'HUC8 boundries overlayed in black'
	        	})
	        );
	    } else if (showRequested) {
	    	map1.layerManager.getMapLayer(layerId).setOpacity(opacity);
	    } else {
	    	map1.layerManager.unloadMapLayer(layerId);
	    }
	}
}}();
