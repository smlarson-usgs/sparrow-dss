Sparrow.events.EventManager = function(){ return{
	//register events TODO may want to create methods to register certain events, to split events into logical types
	registerSessionEvents : function(){
		
		//The user just clicked the Update Map Button successfully
		Sparrow.CONTEXT.on('map-updated-and-synced', Sparrow.handlers.UiComponents.updatePerInSyncMap);
		
		
		//Changes that would invalidate the current map
		Sparrow.CONTEXT.on('changed', function() {
			Sparrow.handlers.UiComponents.updatePerOutOfSyncMap();
		});	//required?
		Sparrow.CONTEXT.on('dataseries-changed', function() {
			Sparrow.handlers.UiComponents.updateComparisons();
			Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(true);
			Sparrow.handlers.UiComponents.updatePerOutOfSyncMap();
		});
		
		//User initiated turn autobin on/off
		Sparrow.CONTEXT.on('autobin-changed', function() {
			
			if (Sparrow.SESSION.isBinAuto()) {
				make_map();	//Just switched to autobin
			} else {
				openCustomBucketsWindow();	//Just turned autobin OFF
			}
		});
		
		//User init:  Change custom bin data
		Sparrow.CONTEXT.on('user-bindata-changed', function() {
			
			//Chain these two events
			Sparrow.SESSION.fireContextEvent('system-bindata-changed');

			make_map();
		});
		
		//System init:  System updated auto bin data
		Sparrow.CONTEXT.on('system-bindata-changed', function() {
			var mapOptionsTab = Ext.getCmp('map-options-tab');
			mapOptionsTab.setBinDescription(Sparrow.SESSION.getBinCount() + ' ' + Sparrow.SESSION.getCurrentBinTypeName()+ ' Bins');
		});
		Sparrow.CONTEXT.on('comparisonchanged', function() {
			Sparrow.handlers.UiComponents.updateComparisons();
			Sparrow.handlers.UiComponents.updatePerOutOfSyncMap();
		});
		Sparrow.CONTEXT.on('model-source-changed', function() {
			Sparrow.handlers.UiComponents.updateComparisons();
			Sparrow.handlers.UiComponents.updatePerOutOfSyncMap();
		});
		Sparrow.CONTEXT.on("targets-changed", function() {
			Sparrow.handlers.UiComponents.updatePerTargetsChanged();
			Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(true);
			Sparrow.handlers.DownstreamTrackingInstructions.disableReport();
			Sparrow.handlers.UiComponents.updatePerOutOfSyncMap();
		});
		Sparrow.CONTEXT.on('adjustment-changed', function() {
			Sparrow.handlers.UiComponents.adjustmentChange();
			Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(true);
			Sparrow.handlers.DownstreamTrackingInstructions.disableReport();
			Sparrow.handlers.UiComponents.updatePerOutOfSyncMap();
		});
		
		//Shouldn't this be the same as the adjustment-changed?
		Sparrow.CONTEXT.on('adjustment-group-changed', function() {
			Sparrow.handlers.UiComponents.updateAdjustmentsTree();
			Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(true);
			Sparrow.handlers.UiComponents.updatePerOutOfSyncMap();
		});
		
		//need sourceshareComparison change event
		
		//TODO:  ee 4/15/14
		//Proposed loading event (not implemented, but would be much cleaner) :
		//1) Build (load) UI
		//2) Load application state - either default context, pre-session (or upload)
		//3) Load model info (model ID now known from presession, if specified)
		//4) Load external service endpoints
		//5) Sync UI to application state
		//
		//Currently, setting app state and sync'ing the UI to that state is
		//mixed in multiple places.  The above order would cleanly order things,
		//syncing the UI last after we fully know the app state to load.
		

		/**
		 * The basic ui is built and ready to load state to.
		 */
		Sparrow.CONTEXT.on('finished-loading-ui', function() {
			loadBasicModelInfo();
			
			//Load the name of a predefined session, if we have one
			Sparrow.SESSION.setPredefinedSessionName(Sparrow.USGS.getURLParam("session"));
		});
		
		/**
		 * The model data has been load, such as model name, sources, theme name - all loaded to transient state.
		 */
		Sparrow.CONTEXT.on('finished-loading-basic-model-info', function() {
			renderModelInfo();
			loadExternalResourceInfo();
		});
		
		/**
		 * External resource info has been loaded (currently, the location of GeoServer)
		 */
		Sparrow.CONTEXT.on('finished-loading-external-resource-info', function() {
			if (Sparrow.SESSION.getPredefinedSessionName()) {
				Sparrow.ui.loadPredefinedSessionByName(Sparrow.SESSION.getPredefinedSessionName());
			} else {
				Sparrow.SESSION.fireContextEvent('finished-loading-everything');
			}
		});

		/**
		 * The user has uploaded a saved session and it is from a model other than
		 * the model initially loaded.
		 */
		Sparrow.CONTEXT.on('finished-loading-pre-session-new-model', function() {
			//Remove all data-related layers (the finished-loading-ui will put them back)
			Sparrow.handlers.MapComponents.turnOffDataRelatedLayers();
			
			//deal w/ older session definitions
			Sparrow.handlers.MapComponents.ensureMaplayerState();
			
			//This name is used to determine if we have loaded a named session
			//and it might still be present from a previous load.
			//A new model is from a user uploaded session, so not a named pre-session.
			Sparrow.SESSION.setPredefinedSessionName(null);
			
			//This will reload model info, but should not go down the 'predefined session' path again.
			loadBasicModelInfo();
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

			//Remove all data-related layers (the finished-loading-ui will put them back)
			Sparrow.handlers.MapComponents.turnOffDataRelatedLayers();
			
			//deal w/ older session definitions
			Sparrow.handlers.MapComponents.ensureMaplayerState();

			//Google Analytics event tracking
			var series = Sparrow.SESSION.getLastValidSeriesData().seriesName;
			_gaq.push(['_trackEvent', 'Context', 'Update', series, parseInt(model_id)]);
			_gaq.push(['_trackEvent', 'PreSession', 'Loaded', series, parseInt(model_id)]);
			
			Sparrow.SESSION.fireContextEvent('finished-loading-everything');
		});
		
		Sparrow.CONTEXT.on('finished-loading-everything', function() {
			//Update the overlay controls to the current state
			var mapOptionsTab = Ext.getCmp('map-options-tab');
			mapOptionsTab.syncDataOverlayControlsToSession();


			
			//Todo - is this a mappable state?
			if (Sparrow.SESSION.isValidContextState()) {
				Sparrow.SESSION.markValidState();
			} else {
				//This should never get this far, but just in case, tell the user.
				Ext.Msg.alert('Warning', Sparrow.SESSION.getInvalidContextStateMessage());
			}
			
			Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(false);
			Ext.getCmp('update-map-button-panel').setStatusFreshLoad();
			
			//
			//Update the map to the current state
			//
			
			map1.dontDraw = true;	//turn off map updates
			var allLayers = Sparrow.SESSION.getAvailableMapLayers();

			//Remove all (including reach overlay, calibration & hucs if enabled)
			for (var i=0; i<allLayers.length; i++) {
				map1.layerManager.removeLayerFromMap(allLayers[i].id);
			}
			
			//Update named layers
			Sparrow.handlers.MapComponents.updateCalibrationLayerOnMap();
			Sparrow.handlers.MapComponents.updateReachOverlayOnMap();
			Sparrow.handlers.MapComponents.updateHuc8OverlayOnMap();

			//re-enable requested (not including reach overlay, calibration & hucs)
			for (var layerId in Sparrow.SESSION.PermanentMapState["mapLayers"]) {
				if (Sparrow.SESSION.isMapLayerEnabled(layerId)) {
					Sparrow.SESSION.fireContextEvent("mapLayers-changed", layerId);
				}
			}
			
			//Only the zoom/fit operation is left, so turn updates back on
			map1.dontDraw = false;
			
			//Pre-session bounds (non-null if loading from pre-session)
			var lat = Sparrow.SESSION.PermanentMapState.lat;
			var lon = Sparrow.SESSION.PermanentMapState.lon;
			var zoom = Sparrow.SESSION.PermanentMapState.zoom;
			
			if (lat != null && lon != null && zoom != null) {
				//Only happens if we are loading a saved/predefined session
				
				if (zoom < map1.minZoom) zoom = map1.minZoom;
				map1.jumpTo(lat,lon,zoom);
				
				//set to null so we don't pick up again.  Repopulated if we save the session.
				Sparrow.SESSION.PermanentMapState.lat = null;
				Sparrow.SESSION.PermanentMapState.lon = null;
				Sparrow.SESSION.PermanentMapState.zoom = null;
				
				//add the sparrow data layer to the map
				make_map();
			} else {
				map1.fitToBBox(Sparrow.SESSION.getOriginalBoundEast(),
					Sparrow.SESSION.getOriginalBoundSouth(), 
					Sparrow.SESSION.getOriginalBoundWest(), 
					Sparrow.SESSION.getOriginalBoundNorth());
			}

		});

		/**
		 * Invoked when the user changes the map selection b/t Reaches or Catchments.
		 */
		Sparrow.CONTEXT.on("what-to-map", function(){
			//Update the overlay controls to the current state
			var mapOptionsTab = Ext.getCmp('map-options-tab');
			mapOptionsTab.syncDataOverlayControlsToSession();

			//Update the reach overlay, which may now be newly enable or disabled.
			Sparrow.handlers.MapComponents.updateReachOverlayOnMap();
			
			make_map();
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

		Sparrow.CONTEXT.on("mapLayers-changed", Sparrow.handlers.MapComponents.updateBackgroundLayer);
	}
};}();

//TODO move this into its own file if needed
Sparrow.handlers.DownstreamTrackingInstructions = function(){
	var _downstreamTabLinkMsg = "<b>On the Map:</b><br />" +
		"Select one of the <i><b>Downstream Tracking</b></i> " +
		"data series at the top of the " +
		"<a href='javascript:GOTO_MAP_OPTIONS_TAB()'>Display Results tab</a>.<br/><br/>" +
		"<b>In a Report:</b><br />" +
		"<a href='javascript:displayDeliverySummaryReport()'>Open the Summary Reports</a><br />" +
		"The summary reports show load delivered to the downstream reaches and the originating region (state or HUC).";

	return {
		
	getReportButtons : function() {
		var reportBtns = [
			mapToolButtons.getComponent('mapToolButtonsOpenDeliveryReports'),
			Ext.getCmp('leftHandOpenDeliveryReportsButton')
		];
		return reportBtns;
	},
			
	enableReport : function() {
		
		var highlightClass = 'background-highlight';
		var msToHighlightFor = 10000;
				
		Ext.each(this.getReportButtons(), function(reportBtn){
			if (reportBtn && reportBtn.hidden) {
				reportBtn.show();
				reportBtn.ownerCt.doLayout();
				reportBtn.getEl().addClass(highlightClass).fadeOut().fadeIn().fadeOut().fadeIn().fadeOut().fadeIn().fadeOut().fadeIn();
				var removeClass = function(){reportBtn.removeClass(highlightClass); window.clearInterval(intervalId);};
				var intervalId = window.setInterval(removeClass, msToHighlightFor);
			}
		});
	},
	
	/**
	 * Enables the delivery report if we have a delivery series and terminal reaches.
	 * Does not check if map is in sync - do not call if not.
	 */
	enableReportIfPermitted : function() {
		if (Sparrow.SESSION.isDeliveryDataSeries() && Sparrow.SESSION.getAllTargetedReaches().length > 0) {
			this.enableReport();
		}
	},
			
	disableReport : function() {
		//Hide the button to open the reports on the map
		Ext.each(this.getReportButtons(), function(reportBtn){
			if (reportBtn && ! reportBtn.hidden) {
				reportBtn.hide();
				reportBtn.ownerCt.doLayout();
			}
		});
	},
	syncDeliveryTabInstructions : function(mapOutOfSync) {
	    //Downstream tracking
		var targetPanel = Ext.getCmp('main-targets-tab');
		var hasTargetReaches = Sparrow.SESSION.getAllTargetedReaches().length > 0;
		var series = Sparrow.SESSION.getMappedSeriesData().seriesName;

		var howToChooseReaches = "To choose downstream reaches, see step 1. <b><i>Select Downstream Reaches(es)</i></b> above to select downstream reaches.";
		var howToChooseDatasource = "To choose a <i><b>Downstream Tracking</b></i> data series, " +
			"select a data series under that heading at the top of the " +
			"<a href='javascript:GOTO_MAP_OPTIONS_TAB()'>Display Results</a> tab."
		var toMapDownstream = "To map a downstream tracking data, you must ";

		if (!mapOutOfSync) {
			//In Sync options
			if (Sparrow.SESSION.isDeliveryDataSeries()) {
				//Has delivery dataseries
				targetPanel.updateStepTwoInstructions(
					"The map is displaying the downstream tracking data series <i><b>" + series + "</b></i>.<br/><br/>"+
					"<b style=\"font-size: 1.3em;\"><a title\"Click to open the reports in a new window\" href=\"javascript:displayDeliverySummaryReport()\">Open the Delivery Summary Report</a></b>.<br />" +
					"The summary reports total the load delivered to the downstream reaches and show breakdowns of the originating regions (state or HUC).");
				
			} else {
				//Does not have delivery dataseries

				if (hasTargetReaches) {
					targetPanel.updateStepTwoInstructions("The map is displaying the non-downstream tracking data series <i><b>" + series + "</b></i>.<br/><br/>" + howToChooseDatasource);
				} else {
					targetPanel.updateStepTwoInstructions("No downstream reaches have been chosen.  " + howToChooseReaches);
				}
			}


		} else {
			//Out of Sync options
			var outOfSyncMsg = "map is not showing your current selections - Please click <b><i>Update Map</i></b> when you are done making your selections and adjustments.";

			if (Sparrow.SESSION.isDeliveryDataSeries()) {
				//Has delivery dataseries

				if (hasTargetReaches) {
					targetPanel.updateStepTwoInstructions("A Downstream Tracking data series and downstream reaches are selected, however, the " + outOfSyncMsg);
				} else {
					targetPanel.updateStepTwoInstructions(toMapDownstream + " choose downstream reaches. " + howToChooseReaches + "<br /><br />The " + outOfSyncMsg);
				}

			} else {
				//Does not have delivery dataseries

				if (hasTargetReaches) {
					targetPanel.updateStepTwoInstructions(toMapDownstream + " choose a downstream tracking data series. " + howToChooseDatasource);
				} else {
					targetPanel.updateStepTwoInstructions(toMapDownstream + " choose downstream reaches and a downstream tracking data series. " + howToChooseReaches);
				}

			}

		}
	}
}}();

Sparrow.handlers.UiComponents = function(){ return{
	
	updatePerInSyncMap : function(){
		//Current series
		var series = Sparrow.SESSION.getMappedSeriesData().seriesName;

		//hide any out of sync messages
		if(document.getElementById('map-sync-warning').className.indexOf('x-hidden') < 0) {
			document.getElementById('map-sync-warning').className += " x-hidden";
		}

		Ext.getCmp('update-map-button-panel').setStatusInSync(series);
		EXPORT_DATA_WIN.enable();	//One way trip to enable

		Sparrow.handlers.DownstreamTrackingInstructions.syncDeliveryTabInstructions(false);
		Sparrow.handlers.DownstreamTrackingInstructions.enableReportIfPermitted();

		//Google Analytics event tracking
		_gaq.push(['_trackEvent', 'Context', 'Update', series, parseInt(model_id)]);
	},
			
	updatePerOutOfSyncMap : function(){
		if (Sparrow.SESSION.isContextChangedSinceLastMap() && Sparrow.SESSION.isMapping()) {
			//re-enable the gen map button
			Ext.getCmp('update-map-button-panel').setStatusOutOfSync(Sparrow.SESSION.getMappedSeriesData().seriesName);

			//Notify user with a visual cue
			document.getElementById('map-sync-warning').className = document.getElementById('map-sync-warning').className.replace(/x-hidden/g, '');

			//Update the ID window if open
			var reachIdentifyWindow = Ext.getCmp('reach-identify-window');
			if (reachIdentifyWindow && reachIdentifyWindow.isApplying) {
				// refresh the window
				var reachId = reachIdentifyWindow.getReachId();
				IDENTIFY.identifyReach(null, null, reachId, 1, true);
			}

		}
	},
			
	updatePerTargetsChanged : function(){
		var targetPanel = Ext.getCmp('main-targets-tab');
		targetPanel.treePanel.loadTree();

		var targetsCount = Sparrow.SESSION.getAllTargetedReaches();
		targetsCount = targetsCount ? targetsCount.length : 0;

		targetPanel.updateInstructions(targetsCount);
	},
			
			
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

	return {
	updateBackgroundLayer : function(layerId){
		//User has enabled/disabled a mapLayer, or changed opacity.
		//This event should also fire after mapLayer selections are
		//loaded from a predefined theme or at startup.
		var isEnabled = Sparrow.SESSION.isMapLayerEnabled(layerId);
		var opacity = Sparrow.SESSION.getMapLayerOpacity(layerId);

		if (isEnabled) {
			
			//TODO: This next line look wrong - we can't append a layer id like this
			if (! map1.layerManager.getSelectedLayer(layerId)) {
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
		var isShowing = (map1.layerManager.getSelectedLayer(layerId) != null);

		if (showRequested) {
			if (isShowing) {
				map1.layerManager.getMapLayer(layerId).setOpacity(opacity);
			} else {
				var viewParams = "viewparams=modelid:" + Sparrow.SESSION.getModelId() + ";";
				var baseUrl = Sparrow.SESSION.getSpatialServiceEndpoint();
				if (baseUrl.lastIndexOf("/") != (baseUrl.length - 1)) {
					baseUrl = baseUrl + "/";
				}

				//There is not a separate WMS param for this, so tack onto base url
				baseUrl = baseUrl + "wms?" + viewParams;

				var wsAndLayerName = "calibration-overlay:calibration";

				map1.layerManager.unloadMapLayer(layerId);
				map1.appendLayer(
					new JMap.web.mapLayer.WMSLayer({
						id: layerId,  zDepth: 59990, opacity: opacity,
						scaleMin: 0, scaleMax: 100,
						baseUrl: baseUrl,
						title: "Calibration Site Overlay",
						name: "calibration",
						isHiddenFromUser: true,
						description: 'Calibration sites overlay',
						layersUrlParam: wsAndLayerName
					})
				);
			}
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
		var isShowing = (map1.layerManager.getSelectedLayer(layerId) != null);

		if (showRequested) {
			if (isShowing) {
				map1.layerManager.getMapLayer(layerId).setOpacity(opacity);
			} else {
				var baseUrl = Sparrow.SESSION.getSpatialServiceEndpoint();
				if (baseUrl.lastIndexOf("/") != (baseUrl.length - 1)) {
					baseUrl = baseUrl + "/";
				}
				baseUrl = baseUrl + "wms?";

				var wsAndLayerName = "reach-overlay:" + Sparrow.SESSION.getThemeName();

				map1.layerManager.unloadMapLayer(layerId);
				map1.appendLayer(
					new JMap.web.mapLayer.WMSLayer({
						id: layerId,  zDepth: 59995, opacity: opacity,
						scaleMin: 0, scaleMax: 100,
						baseUrl: baseUrl,
						title: "Reach Overlay",
						name: "reach_overlay",
						isHiddenFromUser: true,
						description: 'Reaches overlayed in grey',
						layersUrlParam: wsAndLayerName
					})
				);
			}
		} else {
	    	map1.layerManager.unloadMapLayer(layerId);
	    }
	},

	updateHuc8OverlayOnMap : function() {
		var layerId = Sparrow.config.LayerIds.huc8LayerId;
		var opacity = Sparrow.SESSION.getHuc8OverlayOpacity();
		var showRequested = Sparrow.SESSION.isHuc8OverlayRequested();
		var isShowing = (map1.layerManager.getSelectedLayer(layerId) != null);

		if (showRequested) {
//			if (! isShowing) {
//				
//				var urlParams = 'model_id=' + model_id;
//
//				map1.appendLayer(
//					new JMap.web.mapLayer.WMSLayer({
//						id: layerId, zDepth: 59992, opacity: opacity,
//						scaleMin: 0, scaleMax: 100,
//						baseUrl: 'huc8Overlay?' + urlParams,
//						title: "HUC8 Overlay",
//						name: "huc8_overlay",
//						isHiddenFromUser: true,
//						description: 'HUC8 boundries overlayed in black'
//					})
//				);
//		
//			} else {
//				map1.layerManager.getMapLayer(layerId).setOpacity(opacity);
//			}
		} else {
	    	map1.layerManager.unloadMapLayer(layerId);
	    }
	},
	
	/**
	 * Older saved/predefined sessions may not have mapLayers stored in them.
	 * Rather than have no layers turned on, pick up whatever layers are currently
	 * on.
	 * @returns {undefined}
	 */
	ensureMaplayerState : function() {
		
		if (! Sparrow.SESSION.PermanentMapState["mapLayers"]) {
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
		}
	}
}}();
