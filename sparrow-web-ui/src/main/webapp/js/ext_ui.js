// Reference local blank image
Ext.BLANK_IMAGE_URL = 'webjars/extjs/3.4.1.1/resources/images/default/s.gif';

var loadStateBBox = Sparrow.USGS.getURLParam("bbox");

var map1; //TODO global var, get rid of
var mapToolButtons; //TODO global var, get rid of
/*
 * This handler is called when the DOM tree has been loaded.  The components
 * defined here are then rendered to the page.
 */
Ext.onReady(function() {
	Ext.QuickTips.init();

	var syncTipText = 'You have made changes to the mapping or model options. ' +
	'To update the map and model output to reflect the latest changes, click the \"Update Map\" button at the bottom left of the window.';

	Ext.QuickTips.register({
		target: document.getElementById('map-sync-warning').firstChild,
		title: 'Map state is out of sync',
		text: syncTipText,
		dismissDelay: 1000000
	});
	Ext.QuickTips.register({
		target: document.getElementById('map-sync-warning').lastChild,
		title: 'Map state is out of sync',
		text: syncTipText,
		dismissDelay: 1000000
	});

	document.getElementById('index-link').innerHTML = '<a OnClick="confirmHome(); return false;" href="">&lt;&lt; Home&nbsp;</a>';
	document.getElementById('file_upload_form').onsubmit = function() {
		document.getElementById('file_upload_form').target = 'upload_target'; // iframe
	};

	// Create map (was 7X5)
	map1 = new JMap.web.Map({
		containerEl: 'map-area',
		numTilesX: 6,
		numTilesY: 4,
		minZoom: 1,
		centerLat: 37,
		centerLon: -96,
		zoomLevel: 3,
		mapWidthPx: 600,
		mapHeightPx: 400,
		cacheTiles: true,
		border: false,
		svg: true,
		projection: new JMap.projection.PlateCarree(),
		HUD: {
			zoomSlider: true,
			scaleRake: true
		},
		layersFile: {
			url: 'wms/wms_default.xml',
			isOnByDefault: true,
			onPostLoad : function() {

				//Turn on all the default layers (again), using the Sparrow Session.
				//This records  the layers as on and it doesn't hurt to turn the layers on twice.
				for (i=0; i<map1.layerManager.mapLayers.length; i++) {
					var layer = map1.layerManager.mapLayers[i];
					Sparrow.SESSION.setMapLayerEnabled(layer.id);
				}

				//Now add the non-default layers (must be in this order to prevent
				//the above from turning on layers that are not default).
				map1.layerManager.loadMapLayerServicesFile({
					url: 'wms/wms_nmc.xml',
					isHiddenFromUser: false,
					isOnByDefault: false
				});
			}
		}
	});

	map1.tilesLoadingImage.src = 'images/large_rotating_arrow.gif'; //custom loading image

	if (loadStateBBox) {
		var b = loadStateBBox.split(',');
		map1.fitToBBox(parseFloat(b[0]),parseFloat(b[1]),parseFloat(b[2]),parseFloat(b[3]));
	}




	// Put the legend div within the map
	var legend = document.createElement('div');
	legend.id = 'legend';
	legend.style.display = 'block';
	var map_pane = JMap.util.getElementsByClassName(document, 'map-viewport')[0];
	map_pane.appendChild(legend);

	var collapseButton = new Ext.Button({
		text: "Hide Header/Footer"
	});

	var header = new Ext.Panel({ //header
		region: 'north',
		border: false,
		contentEl: 'usgs-header-panel',
		height: 103,
		collapsible: true,
		collapseMode: 'mini',
		split: true,
		hideCollapseTool: true,
		useSplitTips: true,
		collapsibleSplitTip: 'Double click here to hide the top panel.'
	});

	var footer = new Ext.Panel({ //footer
		region: 'south',
		layout: 'border',
		border: false,
		collapsible: true,
		collapseMode: 'mini',
		split: true,
		hideCollapseTool: true,
		useSplitTips: true,
		collapsibleSplitTip: 'Double click here to hide the bottom panel.',
		height: 125,
		minSize: 110,
		items: [{
			region: 'center',
			contentEl: 'usgs-footer-panel'
		}]
	});

	var myHandler = function(btn) {
		var _h = header;
		var _f = footer;
		var _b = collapseButton;

		if(_b == btn){
			if(_b.text.indexOf("Show")>=0){
				_b.setText("Hide Header/Footer");
				_h.expand();
				_f.expand();
			} else {
				_b.setText("Show Header/Footer");
				_h.collapse();
				_f.collapse();
			}
		} else {
			if(_h.collapsed && _f.collapsed){
				_b.setText("Show Header/Footer");
			} else {
				_b.setText("Hide Header/Footer");
			}
		}
	};

	collapseButton.setHandler(myHandler);
	header.on('collapse', myHandler);
	header.on('expand', myHandler);
	footer.on('collapse', myHandler);
	footer.on('expand', myHandler);

	// Setup the map viewing area
	map_area = new Ext.Panel({ //TODO, global!?!?
		region:  'center',
		contentEl: 'map-area',
		style: 'background-color: #FF0000',
		margins: '0 3 0 0',
		tbar: [{
			text: 'Find a reach...',
			handler: function() {
				GOTO_REACH_WIN.open();
			}
		},{
			id: 'export-data-button',
			text: 'Export Data...',
			disabled: true,
			handler: function() {
				EXPORT_DATA_WIN.open();
			}
		},{
			text: 'Session',
			menu: {
				items: [{
					text: 'Load Session...',
					handler: function() {
						LOAD_STATE_WIN.open();
					}
				},{
					text: 'Load Predefined Session...',
					handler: function() {
						(new PredefinedSessionsWindow()).show();
					}
				},'-',{
					text: 'Save Session...',
					handler: function() {
						SAVE_AS_WIN.requestOpen();
					}
				},{
					text: 'Submit Session as a Predefined Scenario...',
					hidden: (document.getElementById('modeler-user-role')==null || document.getElementById('modeler-user-role').innerHTML != 'modeler'), //this looks into the html for the role
					handler: function() {
						(new Sparrow.ui.SavePredefinedScenarioWindow({})).show();
					}
				}]
			}
		},
		{
			id: 'layers-data-button',
			text: 'Layers',
			handler: function() {
				(new MapLayersWindow({
					map: map1
				})).show();
			}
		},
		'->',
		collapseButton,
		{
			text: '<b>SPARROW Model / Videos</b>',
			id: 'sparrow-documentation-button',
			menu: {
				id: 'sparrow-documentation-menu'
			},
			cls: 'x-btn-text-icon',
			icon: 'images/small_info_icon.png',
			listeners: {
				'afterrender' : function(cmp0) {
					setTimeout('Ext.getCmp("sparrow-documentation-button").getEl().fadeOut().fadeIn().fadeOut().fadeIn().fadeOut().fadeIn().fadeOut().fadeIn().fadeOut().fadeIn();', 5000);
				}
			}
		}],
		listeners: {
			'resize': function(mapPanel) {
				var s = mapPanel.getSize();
				if (map1) { //TODO get rid of global var access
					map1.resize(s.width-2, s.height - 2 - 25);
				}
				var gps_frame = document.getElementById('gps-area-frame');
				gps_frame.style.height = s.height - 100 + 'px';
				gps_frame.style.width = '100%';
			}
		}
	});

	//Help popup window
	var helpWindow = new Ext.Window({
		id: 'help-frame',
		layout: 'border',
		width: 700,
		height: 400,
		border: false,
		closeAction: 'hide',
		title: 'Sparrow DSS Help',
		items: [
		new Ext.Panel({
			height: 0,
			region: 'north',
			border: false,
			buttonAlign: 'center',
			buttons: [
			{
				text: 'Open In New Window...',
				handler: function() {
					var popup = window.open('', '_help', 'height=700,width=600,scrollbars=yes');
					var doc = popup.document;
					doc.write('<html><head><title>SPARROW DSS Help</title>');
					doc.write('<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />');
					doc.write('<link type="text/css" rel="stylesheet" href="css/usgs_style_main.css" />');
					doc.write('<link type="text/css" rel="stylesheet" href="css/custom.css" />');
					doc.write('</head><body>');
					doc.write(document.getElementById('wiki-help-panel').innerHTML);
					doc.write('</body></html>');
					doc.close();
					helpWindow.hide();
				},
				tooltip: {
					title: 'Open In New Window',
					text: 'Click to open the current help content in a new window for easy reference.',
					width: 200,
					dismissDelay: 1000000,
					showDelay: 0
				}
			}
			]
		}),
		new Ext.Panel({
			region: 'center',
			autoScroll: true,
			contentEl: 'wiki-help-panel'
		})
		]
	});

	//Delivery report window
	var deliveryReportWindow = new Ext.Window({
		id: 'delivery-report-frame',
		layout: 'border',
		width: 700,
		height: 400,
		border: false,
		closeAction: 'hide',
		title: 'Delivery Report',
		items: [
		new Ext.Panel({
			height: 0,
			region: 'north',
			border: false,
			buttonAlign: 'center',
			buttons: [
			{
				text: 'Open In New Window...',
				handler: function() {
					var popup = window.open('', '_help', 'height=700,width=600,scrollbars=yes');
					var doc = popup.document;
					doc.write('<html><head><title>SPARROW DSS - Delivery Report</title>');
					doc.write('<link type="text/css" rel="stylesheet" href="http://www.usgs.gov/styles/common.css" />');
					doc.write('<link type="text/css" rel="stylesheet" href="css/usgs_style_main.css" />');
					doc.write('<link type="text/css" rel="stylesheet" href="css/custom.css" />');
					doc.write('<link type="text/css" rel="stylesheet" href="css/report.css" />');
					doc.write('</head><body>');
					doc.write(document.getElementById('delivery-report-panel').innerHTML);
					doc.write('</body></html>');
					doc.close();
					deliveryReportWindow.hide();
				},
				tooltip: {
					title: 'Open In New Window',
					text: 'Click to open the current content in a new window for easy reference.',
					width: 200,
					dismissDelay: 1000000,
					showDelay: 0
				}
			}
			]
		}),
		new Ext.Panel({
			region: 'center',
			autoScroll: true,
			contentEl: 'delivery-report-panel'
		})
		]
	});

	//TODO, this code is NOT hooked up to anything right now and is here for prototype demonstration,
	//use it or lose it.
	//
	//To demo this, use firebug or any other javascript console in a browser and
	//enter the command: Ext.getCmp('sparrow-dock-demo').doExpand()
	var idDock = new Sparrow.ux.DockPanel({
		id: 'sparrow-dock-demo',
		parentPanel: map_area,
		title: 'DEMO OF DOCKING PANEL',
		height: 250,
		width: 400,
		contentPanel: { //Replace this config object with your content panel
			padding: 10,
			border: true,
	        baseCls: 'x-window-mc',
			html: 'Any content can go here'
		}
	});

	UpdateMapButtonPanel = Ext.extend(Ext.Panel, {
		constructor : function(config) {

			this.updateMapInstructions = new Ext.Panel({
	        	border: false,
	        	bodyStyle: {
	        		'background-color': 'transparent',
	        		'text-align': 'center'
	        	},
	        	autoHeight: true,
	        	layout: 'form'
	        });

			this.updateMapButton = new Ext.Button({ //referenced by multiple handlers, so declare here and attach to this
				text: '<b>Update Map</b>',
				autoHeight: true,
				handler: function() {
					if (Sparrow.SESSION.isValidMapState()) {
						make_map();
					} else {
						Ext.Msg.alert('Warning', Sparrow.SESSION.getInvalidMapStateMessage());
					}
				},
				tooltip: {
					title: 'Update Map',
					text: 'Create and/or update the map and model output information. Updates will reflect selections made on the <b>Display Results</b>, <b>Downstream Tracking</b>, and <b>Change Inputs</b> tabs.',
					width: 200,
					dismissDelay: 1000000,
					showDelay: 0
				}
			});
			this.deliveryReportsButton = new Ext.Button({
				id: 'leftHandOpenDeliveryReportsButton',
				text: 'Open Delivery Reports',
				tooltip: 'View detailed delivery data in a new window',
				handler: displayDeliverySummaryReport,
				hidden: true,
			});
			var defaults = {
					border: false,
					layout: 'fit',
					autoScroll: false,
					autoHeight: true,
		        	bodyStyle: 'background-color: transparent',
					region: 'south',
					padding: 4,
					buttonAlign: 'center',
					items: [this.updateMapInstructions],
					buttons: [this.updateMapButton, this.deliveryReportsButton]
			};

			config = Ext.applyIf(config, defaults);
			UpdateMapButtonPanel.superclass.constructor.call(this, config);
		},

		setStatusFreshLoad : function() {
			var text = "Click <b><i>Update map</i></b> to generate a map.";
			this.updateMapInstructions.body.update(text);
			this.updateMapButton.setDisabled(false);
			this.updateMapInstructions.doLayout();
			this.findParentByType('panel').doLayout();
		},
		
		setStatusInSync : function(dataseries) {

			if (! this.updateMapButton.disabled) {
				var text = (Sparrow.SESSION.getComparisons() == "none") ? "Currently mapping " : "Currently mapping <b><i>Change in </i></b>";
 				text += "<b><i>" + dataseries + "</i></b>.<br/>" +
					"The map is up to date.";
				this.updateMapInstructions.body.update(text);
				this.updateMapButton.setDisabled(true);
				this.updateMapInstructions.doLayout();
				this.findParentByType('panel').doLayout();
			}
		},

		setStatusOutOfSync : function(dataseries) {
			if (this.updateMapButton.disabled) {
				var text = "Map settings have changed. " +
					"<b><i>Update map</i></b> to refresh all data.";
				this.updateMapInstructions.body.update(text);
				this.updateMapButton.setDisabled(false);
				this.updateMapInstructions.doLayout();
				this.findParentByType('panel').doLayout();
			}
		}


	});

	// Viewport fills the browser window with its subcomponents
	viewport = new Ext.Viewport({
		id: 'sparrow-viewport',
		layout: 'border',
		items: [header,
			new Ext.Panel({
				region: 'west',
				layout: 'border',
				border: false,
				frame: false,
				collapsible: true,
				collapseMode: 'mini',
				split: true,
				hideCollapseTool: true,
				useSplitTips: true,
				collapsibleSplitTip: 'Double click here to hide the left control panel.',
				minSize: 325,
				width: 325,
				items: [new Ext.TabPanel({
					region: 'center',
					contentEl: 'navigation-bar',
					border: false,
					frame: false,
					split: false,
					activeTab: 0,
					plain: true,
					deferredRender: false,
					items: [
					new Ext.Panel({
						layout: 'border',
						border: false,
						title: 'Display Results',
						tabTip: 'Set mapping preferences and generate new maps',
						items: [
							{
								contentEl: 'display-results-text',
								frame: false,
								border: false,
								region: 'north',
								collapsible: true,
								hideCollapseTool: true,
								collapseMode: 'mini',
								split: true
							},
						    new MapOptionsPanel({id: 'map-options-tab'})
						]
					}),
					new Sparrow.TargetsPanel({id:'main-targets-tab'}),
					new Sparrow.AdjustmentsPanel({id:'main-adjustments-tab'})
					]
				}),
				new UpdateMapButtonPanel({id: 'update-map-button-panel'})
				]
			}),
			map_area,
			footer]
	});

	// Create hidden content (shown in popup windows)
	add_group_tabs = new Ext.TabPanel({
		renderTo: 'add-group-area',
		activeTab: 0,
		deferredRender: false,
		border: false,
		items:[
		{
			contentEl:'treatment-tab',
			title:'Input Changes',
			autoScroll:true
		},{
			contentEl:'notes-tab',
			title:'Notes'
		}
		]
	});

	if (Ext.isIE6 || Ext.isIE7 || Ext.isIE8) {
		IE6_WARN_WINDOW.open();
	}


	mapToolButtons = new Ext.ButtonGroup({
		renderTo: 'map-controls',
		id: 'map-tool-buttons',

		defaults: {
			iconAlign: 'top',
			//tooltipType: 'title',
			style: 'padding: 0px',
			scale: 'medium'
		},
		items: [{
			id: 'mapToolButtonsDrag',
			tooltip: 'Drag Map',
			iconCls: 'hand-drag-icon',
			pressed: true,
			enableToggle: true,
			toggleGroup: 'map-tools',
			handler: function(b) {
				if (!b.pressed) b.toggle();
				map1.setMouseAction(null);
			}
		},{
			id: 'mapToolButtonsIdentify',
			tooltip: 'Identify Reach',
			iconCls: 'id-reach-icon',
			enableToggle: true,
			toggleGroup: 'map-tools',
			handler: function(b) {
				if (!b.pressed) b.toggle();
				map1.setMouseAction(IDENTIFY.identifyPoint);
			}
		},{
			id: 'mapToolButtonsZoomIn',
			tooltip: 'Zoom In',
			iconCls: 'zoom-in-icon',
			enableToggle: true,
			toggleGroup: 'map-tools',
			handler: function(b) {
				if (!b.pressed) b.toggle();
				map1.setMouseAction(JMap.util.Tools.zoomIn);
			}
		},{
			id: 'mapToolButtonsZoomOut',
			tooltip: 'Zoom Out',
			iconCls: 'zoom-out-icon',
			enableToggle: true,
			toggleGroup: 'map-tools',
			handler: function(b) {
				if (!b.pressed) b.toggle();
				map1.setMouseAction(JMap.util.Tools.zoomOut);
			}
		},{
			id: 'mapToolButtonsResetBounds',
			tooltip: 'Reset View To Original Bounds',
			iconCls: 'zoom-to-original-extent-icon',
			handler: function(b) {
				map1.fitToBBox(Sparrow.SESSION.getOriginalBoundEast(), Sparrow.SESSION.getOriginalBoundSouth(), Sparrow.SESSION.getOriginalBoundWest(), Sparrow.SESSION.getOriginalBoundNorth());
			}
		},{
			id: 'mapToolButtonsCalibrationSiteIdentify',
			tooltip: 'Identify Calibration Site',
			iconCls: 'id-calib-icon',
			hidden: true,
			enableToggle: true,
			toggleGroup: 'map-tools',
			handler: function(b) {
				if (!b.pressed) b.toggle();
				map1.setMouseAction(IDENTIFY.identifyCalibrationSite);
			}
		},{
			id: 'mapToolButtonsHideOverlay',
			text: 'Remove overlay',
			hidden: true,
			handler: function() {
				SvgOverlay.removeAllOverlays();
			}
		},{
			id: 'mapToolButtonsOpenDeliveryReports',
			text: 'Open the Delivery Reports',
			tooltip: 'View detailed delivery data in a new window',
			hidden: true,
			handler: function() {
				displayDeliverySummaryReport();
			}
		}]
	});

	//This is the plumbing for the app
	Sparrow.events.EventManager.registerSessionEvents();

	Sparrow.SESSION.fireContextEvent('finished-loading-ui');
});

/**
 * Renders the model source options to the page and other model info dependent items..
 *
 */
function renderModelInfo() {
	
	var docUrl = Sparrow.SESSION.getDocUrl();
	
    var docMenu = Ext.menu.MenuMgr.get('sparrow-documentation-menu');

	/**
	 * @param {string} name - the user-facing text for the menu item
	 * @param {string} videoId - the youtube video id
	 */
	var addVideoItemToDocMenu = function(name, videoId){
		//access docMenu through closure
		docMenu.add({
	   	text: 'Video: ' + name,
	   	handler: function() {
	   		openScreencast(videoId);
	   	}
		});
	};

    docMenu.removeAll();
    if(docUrl != null) {
        docMenu.add({
        	text: 'About: ' + Sparrow.SESSION.getModelName() + '...',
        	handler: function() {
        		var newWindow = window.open(docUrl, '_blank');
        		newWindow.focus();
        	}
        });
    }
    docMenu.add({
    	text: 'What is SPARROW?',
    	handler: function() {
    		var newWindow = window.open('http://pubs.usgs.gov/fs/2009/3019/pdf/fs_2009_3019.pdf', '_blank');
    		newWindow.focus();
    	}
    });
   docMenu.add({
    	text: 'SPARROW Applications & Documentation',
    	handler: function() {
    		var newWindow = window.open('http://water.usgs.gov/nawqa/sparrow/', '_blank');
    		newWindow.focus();
    	}
    });
   docMenu.add({
   	text: 'SPARROW FAQs',
   	handler: function() {
   		var newWindow = window.open('faq.jsp', '_blank');
   		newWindow.focus();
   	}
   });
   docMenu.add('-');
   docMenu.add({
	   text: 'Tutorial Videos',
	   style: {'font-weight': 'bold', 'font-size': '110%'}
   });
   docMenu.add({
	   text: 'Video windows can be resized to show full detail',
	   style: {'font-style': 'italic'}
   });
   docMenu.add('-');
   //add videos
   Ext.iterate(screenCastNameToVideoIdMap, function(name, videoId){
		addVideoItemToDocMenu(name, videoId);
   });

   Ext.getCmp('map-options-tab').autoBinsChk.setValue(Sparrow.SESSION.isBinAuto());

    // Render the appropriate model 'theme'
    var siteTitleBar = document.getElementById('title-model-name');
    siteTitleBar.innerHTML = " - " + Sparrow.SESSION.getModelName();

    // Get the treaments tab from the group defintion window
    var treatmentTab = document.getElementById('treatment-tab');
    treatmentTab.innerHTML = '';

    // Iterate over the sources
    var mapOptionsTab = Ext.getCmp('map-options-tab');
    mapOptionsTab.clearSources();

	var sourceList = Sparrow.SESSION.getSourceList();
    for (var i = 0; i < sourceList.length; i++) {

        // Add to the data series source select
        var displayName = sourceList[i]["displayName"];
        var description = sourceList[i]["description"];
        mapOptionsTab.addSource(displayName, i + 1, description);

        // Add a row to the treatment tab
        var data_row = document.createElement('div');
        data_row.className = 'data_row clearfix';
        (i%2) ? data_row.style.backgroundColor = '#FFFFFF' : data_row.style.backgroundColor = '#EEEEEE';

        var src_name = document.createElement('div');
        src_name.className = 'col_25';
        src_name.innerHTML = sourceList[i]["displayName"];

        var src_constituent = document.createElement('div');
        src_constituent.className = 'col_20';
        src_constituent.innerHTML = sourceList[i]["constituent"] + ' (' + sourceList[i]["units"] + ')';

        var src_adj_div = document.createElement('div');
        src_adj_div.className = 'col_20';
        var src_adj = Sparrow.USGS.createElement('select','treatment-tab_src_adj');
        src_adj.id = 'treatment-tab_src_adj_' + i;
        src_adj.size = 1;
        for (var j = 0; j <= 8; j++) {
            var opt = new Option(j * 0.25, j * 0.25);
            src_adj.options[j] = opt;
        }
        src_adj.value = 1;

        var src_cust = document.createElement('div');
        src_cust.className = 'col_30';
        //src_cust.innerHTML = '<a href="#" onclick="return false">customize...</a>';
        var src_cust_a = document.createElement('a');
        src_cust_a.href = "";
        src_cust_a.index = i;
        src_cust_a.innerHTML = 'enter custom multiplier...';
        src_cust_a.onclick = function() {
            var idx = this.index;
            var src_adj_sel = document.getElementById('treatment-tab_src_adj_' + idx);
            var reply = parseFloat(prompt("Enter new value for multiplier:",""));
            if (!isNaN(reply)) {
                src_adj_sel.value = reply;
                if (src_adj_sel.value != reply) { //number doesn't exist in list already, add it
                    src_adj_sel.options[9] = new Option(reply,reply);
                    src_adj_sel.value = reply;
                }
            }
            return false;
        };

        src_cust.appendChild(src_cust_a);
        treatmentTab.appendChild(data_row);
        data_row.appendChild(src_name);
        data_row.appendChild(src_constituent);
        data_row.appendChild(src_adj_div);
        src_adj_div.appendChild(src_adj);
        data_row.appendChild(src_cust);
    }

    mapOptionsTab.filterSourceCombo();
}

/**
 * Prompt the user before following link home
 */

var confirmHome = function() {
	Ext.Msg.confirm('Warning',
		'Any unsaved changes to this session<br/>will be lost. Continue to home page?',
		function(v) {
			if(v=='yes') {

				//Get the url up to the 'map.jsp' part.
				var url = window.location.href;
				var pageIndex = url.indexOf("map.jsp", 0);
				url = url.substring(0, pageIndex);

				window.location.assign(url);

			} else {
				return false;
			}
		}, this);
};

/**
 * Returning to the map options tab is called by a javascript link, so it needs
 * to be global.
 * @returns
 */
var GOTO_MAP_OPTIONS_TAB = function() {
	var tp = Ext.getCmp('map-options-tab').findParentByType('tabpanel');
	tp.setActiveTab(0);
};

/**
 * Returning to the map options tab is called by a javascript link, so it needs
 * to be global.
 * @returns
 */
var GOTO_TARGETS_TAB = function() {
	var tp = Ext.getCmp('map-options-tab').findParentByType('tabpanel');
	tp.setActiveTab(1);
};

/**
 * Spinner to inform user that IDENTIFY request is in progress, meant to replace PLEASE_WAIT_WIN
 */

var IDENTIFY_REACH_SPINNER = function() {
	var _identifyReachMask;
	return {
		show: function() {
			if(!_identifyReachMask) {
				_identifyReachMask = new Ext.LoadMask(Ext.getBody(), {
					msg: 'Please wait, identifying reach... <input type="button" onclick="IDENTIFY_REACH_SPINNER.hide();Ext.Ajax.abort();" value="cancel" />'
				});
			}
			_identifyReachMask.show();
		},
		hide: function() {
			_identifyReachMask.hide();
		}
	};
}();

var IDENTIFY_CALIB_SITE_SPINNER = function() {
	var _identifyReachMask;
	return {
		show: function() {
			if(!_identifyReachMask) {
				_identifyReachMask = new Ext.LoadMask(Ext.getBody(), {
					msg: 'Please wait, identifying calibration site... <input type="button" onclick="IDENTIFY_CALIB_SITE_SPINNER.hide();Ext.Ajax.abort();" value="cancel" />'
				});
			}
			_identifyReachMask.show();
		},
		hide: function() {
			_identifyReachMask.hide();
		}
	};
}();

/**
 * Options window for saved sessions functionality
 */
var SAVE_AS_WIN = new (function(){
	var save_as_win;
	
	this.requestOpen = function() {
		if (Sparrow.SESSION.isMapping()) {
			if (Sparrow.SESSION.isContextChangedSinceLastMap()) {
				Ext.Msg.alert('Warning', "The map is not showing your current selections.  Click <i>Update Map</i> and try again.");
			} else {
				this.open();
			}
		} else {
			Ext.Msg.alert('Warning', "There must be a generated map before the session is saved.  Click <i>Update Map</i> and try again.");
		}
	};

	this.open = function(){
		if(!save_as_win){
			save_as_win = new Ext.Window({
				title: 'Save Mapping Session...',
				contentEl:'ui-save-window',
				closeAction:'hide',
				layout: 'fit',
				plain: true,
				modal: true,
				width: 250,
				resizable: false,
				buttons: [
				{
					text:'Save Session',
					handler: function(){
						if (Sparrow.ui.save_map_state()) document.getElementById('save_state_form').submit();
					}
				}/*,
					{
			            text:'Save as XML',
			            handler: function(){
			              if (Sparrow.ui.save_map_state(true)) document.getElementById('save_state_form').submit();
			            }
			          }*/
				]
			});
		}
		save_as_win.show();
	};

	this.close = function(){
		var val = document.getElementById('savefileas').value;
		if (val != null && val != '') {
			save_as_win.hide();
			return true;
		} else {
			Ext.Msg.alert('Required', "name the file you want to save");
			return false;
		}
	};
})();


Sparrow.ui.SavePredefinedScenarioWindow = Ext.extend(Ext.Window, {
	constructor: function(config) {
		this.nameField = new Ext.form.TextField({
			fieldLabel: 'Contact Name',
			maxLength: 40,
			anchor: '97%',
			allowBlank: false
		});
		this.emailField = new Ext.form.TextField({
			fieldLabel: 'Contact Email',
			maxLength: 40,
			anchor: '97%',
			vtype: 'email',
			allowBlank: false
		});
		this.phoneField = new Ext.form.TextField({
			fieldLabel: 'Contact Phone',
			maxLength: 40,
			anchor: '97%',
			vtype: 'phone',
			allowBlank: false
		});
		this.scenarioNameField = new Ext.form.TextField({
			fieldLabel: 'Scenario Name',
			maxLength: 40,
			anchor: '97%',
			allowBlank: false
		});
		this.descField = new Ext.form.TextArea({
			height: 120,
			fieldLabel: 'Scenario Description',
			maxLength: 1000,
			anchor: '97%',
			allowBlank: false
		});
		this.typeField = new Ext.form.ComboBox({
			store: new Ext.data.SimpleStore({
				fields: ['value'],
				data: [
			       ['', ''],
			       ['FEATURED'],
			       ['LISTED'],
			       ['UNLISTED']
			    ]
			}),
			mode: 'local',
			autoLoad: true,
			fieldLabel: 'Scenario Type',
			displayField: 'value',
			valueField: 'value',
			value: '',
			anchor: '97%',
			allowBlank: false
		});
		this.scenarioCodeField = new Ext.form.TextField({
			fieldLabel: 'Unique Code (Optional)',
			maxLength: 40,
			anchor: '97%'
		});

		var lazyLoadedConfigOption = { //TODO add text and maybe change var names
				border: false,
				html: "TEXT HERE, put this object next to a field for this text to display (see below). this defaults to a container in most cases"
		};

		this.form = new Ext.FormPanel({
				region: 'center',
				padding: 10,
				labelWidth: 150,
				items:[
				       this.nameField,
				       this.emailField,
				       this.phoneField,
				       this.scenarioNameField,
				       {border: false, html: '<div class="enable-css" style="padding: 3px 0 1.5em 6em;">A good descriptive name for this scenario.</div>'},
				       this.descField,
				       {border: false, html: '<div class="enable-css" style="padding: 3px 0 1.5em 6em;">Description of the scenario.</div>'},
				       this.typeField,
				       {border: false, html: '<div class="enable-css" style="padding: 3px 0 1.5em 6em;"><ul>' +
				    	   '<li><b>FEATURED</b> scenarios are prominently displayed and should be good "look at these" first examples.</li>' +
				    	   '<li><b>LISTED</b> scenarios will be listed on less prominently for the model.</li>' +
				    	   '<li><b>UNLISTED</b> scenarios are not shown for the model, but can be linked to directly from a paper or other web sites.  Be sure to choose a good "Unique Code" if you choose this option.</li>' +
				    	   '</ul></div>'},
				       this.scenarioCodeField,
				       {border: false, html: '<div class="enable-css" style="padding: 3px 0 2em 6em;">The unique code is the last portion of the url that you can use to link to this predefined scenario. ' +
				    		  'Ignoring the first part of the url, a typical example of a Predefined Scenario url will look like this: ' +
				    		  '<br/><code>.../map.jsp?model=50&session=soil_1</code><br/> ' +
				    		  'In this example, the "soil_1" is the unique code and makes the url a bit more meaningful.</div>'}
				       ]
			});

		var defaults = {
			title: "Submit Session as a Predefined Scenario",
			width: 750,
			height: 700,
			resizable: true,
			autoscroll: true,
			autoHeight: false,
			layout: 'border',
			items: [{
				xtype: 'panel',
				region: 'north',
		        baseCls: 'x-window-mc',
				html: '<div class="enable-css">To permanently save your current scenario ' +
					'so that it can be shared with other users:' +
					'<ol>' +
					'<li>Complete the form, including all fields and click "Submit".</li>' +
					'<li>Send an email to the <a title="Contact Email" href="mailto:sparrowdss@usgs.gov?subject=Sparrow Map Comments">SPARROW DSS Administrator</a> to notify them of the added scenario.</li>' +
					'<li>Your session will saved and reviewed by an administrator for inclusion in the application.  They may need to contact you, so ensure that your contact information is complete.</li>' +
					'</ol></div>',
				padding: 5
			},
			this.form],

			buttons: [{
				text: "Submit",
				handler: function(){
					if(!this.form.getForm().isValid()){
						Ext.Msg.alert("Invalid entry", "Please fix all errors before continuing.");
						return;
					}
					Sparrow.ui.update_SESSION_mapstate();
					var params = {
						modelId: model_id, //TODO, global var used, bad bad bad
						predefinedSessionType: this.typeField.getValue(),
						name: this.scenarioNameField.getValue(),
						description: this.descField.getValue(),
						contextString: Sparrow.SESSION.asJSON(),
						addBy: this.nameField.getValue(),
						addNote: "APPROVAL REQUIRED",
						addContactInfo: "Name: "+this.nameField.getValue()+
								", Phone: "+this.phoneField.getValue()+
								", Email: "+this.emailField.getValue(),
						groupName: "Front End Submission"
					};

					if(this.scenarioCodeField.getValue()) {
						params["uniqueCode"] = this.scenarioCodeField.getValue();
					}

					Ext.Ajax.request({
						method: 'POST',
						url: 'listPredefSessions',
						jsonData: {"gov.usgswim.sparrow.domain.PredefinedSessionBuilder": params},
						success: function(r,o){
							if(r.statusText=="OK") {
								var json = Ext.util.JSON.decode(r.responseText);
								if(json.ServiceResponseWrapper.entityList) {
									Ext.Msg.alert("Scenario Submitted", "Your scenario has been successfully submitted for approval. The scenario was given the unique code \"<b>"+
										json.ServiceResponseWrapper.entityList.entity.uniqueCode+"</b>\", please make note of this code for future reference.<br/><br/>"+
										"<b>Scenario Summary</b><br/>"+
										json.ServiceResponseWrapper.entityList.entity.name+"<br/>"+
										json.ServiceResponseWrapper.entityList.entity.description+"<br/><br/>"+
										"When the SPARROW DSS Administrator reviews and approves your scenario, a link will be displayed in the list of Predefined Scenarios. For help, please contact <a href='mailto:sparrowdss@usgs.gov?subject=Predfined Scenario Help'>SPARROW DSS Administrator</a>.");
									this.close();
								} else {
									Ext.Msg.alert("Scenario Submission Failed", "Your scenario was not saved. If you entered a unique code, it may already be in use. Choose another code or leave it blank to have one generated for you and try again.");
								}
							}
						},
						failure: function() {
							Ext.Msg.alert("Scenario Submission Failed", "There was a server error in saving submitting your session. Please try again.");
							this.close();
						},
						scope: this
					});

				},
				scope: this
			}, {
				text: "Cancel",
				handler: function() {
					this.close();
				},
				scope: this
			}]
		};

		config = Ext.applyIf(config, defaults);

		Sparrow.ui.SavePredefinedScenarioWindow.superclass.constructor.call(this, config);
	}
});

/**
 * Window for loading a saved session
 */
var LOAD_STATE_WIN = new (function(){
	var load_state_win;

	this.open = function(){
		if(!load_state_win){
			load_state_win = new Ext.Window({
				title: 'Load Mapping Session...',
				contentEl:'ui-load-window',
				closeAction:'hide',
				layout: 'fit',
				plain: true,
				modal: true,
				width: 500,
				resizable: false
			});
		}
		load_state_win.show();
	};

	this.close = function(){
		var val = document.getElementById('ui_file').value;
		if (val != null && val != '') {
			load_state_win.hide();
			return true;
		} else {
			Ext.Msg.alert('Required', 'select a file to load');
			return false;
		}
	};
})();

/**
 * Window for exporing/downloading Sparrow data to user workstation
 */
var EXPORT_DATA_WIN = new (function(){
	var export_data_win;
	var self = this;

	/**
	 * Enables the 'Export Data' button allowing the user to save their data.
	 */
	self.enable = function(){
		var button = Ext.getCmp('export-data-button');
		button.setDisabled(false);
	};

	self.open = function(){
		if(!export_data_win){
			export_data_win = new Ext.Window({
				title: 'Export Data',
				contentEl:'export-data-window',
				closeAction:'hide',
				layout: 'fit',
				plain: true,
				modal: true,
				width: 675,
				resizable: false,
				buttons: [
				{
					text: 'Export',
					handler: function(){
						Sparrow.ui.setUpExport();
						document.getElementById('export_form').submit();
					}
				},{
					text:'Close',
					handler: function(){
						self.close();
					}
				}
				],
				listeners: {
					'show' : function(cmp0) {
						//Update the displayed row count
						document.getElementById('export-row-count-1').innerHTML = Sparrow.SESSION.getMappedSeriesData().rowCount;
						document.getElementById('export-row-count-2').innerHTML = Sparrow.SESSION.getMappedSeriesData().rowCount;
						self.enableFormSection(document.getElementById('export-data-window-adjusted-series'), Sparrow.SESSION.hasEnabledAdjustments());
					}
				}
				});
		}
		export_data_win.show();
	};

	self.enableFormSection = function(formSection, enable) {
		var cl = formSection.getAttribute("class");
		var classes = null;
		var disabledIndex = -1;

		if (cl && cl.length > 0) {
			classes = cl.split(" ");
			for (var i=0; i<classes.length; i++) {
				if (classes[i] == 'disabled') {
					disabledIndex = i;
					break;
				}
			}
		}

		if (enable && disabledIndex > -1) {
			classes.splice(disabledIndex, 1);
			cl = classes.join(' ');
			formSection.setAttribute("class", cl);
		} else if (! enable && disabledIndex < 0) {
			classes.push('disabled');
			cl = classes.join(' ');
			formSection.setAttribute("class", cl);
		}


		self.recurseEnableFormSection(formSection, enable);
	};

	self.recurseEnableFormSection = function(formSection, enable) {

		if (formSection.nodeType == formSection.ELEMENT_NODE) {
			var en = formSection.nodeName.toUpperCase();
			if (
						'BUTTON' == en ||
						'INPUT' == en ||
						'OPTGROUP' == en ||
						'OPTION' == en ||
						'SELECT' == en ||
						'TEXTAREA' == en) {
				if (enable) {
					formSection.removeAttribute("disabled");
				} else {
					formSection.setAttribute("disabled", "disabled");
				}

			}
		}

		if (formSection.hasChildNodes()) {
			for(var i=0; i<formSection.childNodes.length; i++) {
				self.recurseEnableFormSection(formSection.childNodes[i], enable);
			}
		}
	};

	self.close = function(){
		export_data_win.hide();
	};
})();


// Doesn't seem to be used any more. Will comment out and delete later
//function toggleAdjustmentTab(b) {
//  id_group_tabs.getItem("1").setDisabled(!b);
//}







function getReachRequestXML() {
	Ext.getBody().mask('searching for reaches... <input type="button" onclick="Ext.getBody().unmask();Ext.Ajax.abort();" value="cancel" />', 'x-mask-loading');
	var params = GOTO_REACH_WIN.getFormValues();
	var xmlreq = ''
	+ '<sparrow-reach-request xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" '
	+ 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">'
	+ '<model-id>' + model_id + '</model-id>'
	+ '<match-query>';
	if (params) {
		if (params.reachId) xmlreq += '<reach-ids>' + params.reachId + '</reach-ids>';
		if (params.reachname) xmlreq += '<reach-name>' + params.reachname + '</reach-name>';
		if (params.meanQHi) xmlreq += '<meanQHi>' + params.meanQHi + '</meanQHi>';
		if (params.meanQLo) xmlreq += '<meanQLo>' + params.meanQLo + '</meanQLo>';
		if (params.watershedAreaHi) xmlreq += '<tot-contrib-area-hi>' + params.watershedAreaHi + '</tot-contrib-area-hi>';
		if (params.watershedAreaLo) xmlreq += '<tot-contrib-area-lo>' + params.watershedAreaLo + '</tot-contrib-area-lo>';
		if (params.huc8) xmlreq += '<huc>' + params.huc8 + '</huc>';
		//	    	if (params.limit) xmlreq += '<bbox>' + map1.getViewportBoundingBoxString() + '</bbox>'; //xmin,ymin,xmax,ymax
		if (params.edaname) xmlreq += '<edaname>' + params.edaname + '</edaname>';
		if (params.edacode) xmlreq += '<edacode>' + params.edacode + '</edacode>';
	}
	xmlreq += '</match-query>'
	+ '<content><adjustments/></content><response-format><mime-type>XML</mime-type>'
	+ '</response-format></sparrow-reach-request>';

	return xmlreq;
}

/**
 * Window for Add Group functionality (invoked from menu)
 */
var NAME_DESC_GROUP_WIN = new (function(){
	var name_desc_group_win;

	this.open = function Window() {
		if (!name_desc_group_win){
			name_desc_group_win = new Ext.Window({
				contentEl:'name-group-area',
				title: 'Add Group',
				closeAction:'hide',
				layout: 'fit',
				plain: true,
				modal: true,
				width: 250,
				height: 150,
				draggable: true,
				resizable: false,
				buttons: [{
					text: 'OK',
					handler: function(){
						// TODO: check group name for empty or non-unique
						name_desc_group_win.hide();
						if (reopen_add_to_group) {
							CREATE_GROUP_WIN.open(document.getElementById('group_name').value,null,true);
							reopen_add_to_group = false;
							Sparrow.ui.add_group();
							resetGroupWindow();
							openIdGroupWindow();
						} else {
							CREATE_GROUP_WIN.open(document.getElementById('group_name').value);
						}
					}
				},{
					text: 'Cancel',
					handler: function(){
						name_desc_group_win.hide();
						if (reopen_add_to_group) {
							//openIdGroupWindow();
							reopen_add_to_group = false;
						}
					}
				}]
			});
		}
		name_desc_group_win.show();
	};
})();

/**
 * Popup window when add to group on menu
 */
var CREATE_GROUP_WIN = new (function(){
	var create_group_win, ok_but, ok_but_text;

	this.initOKButton = function(editGroup){
		ok_but_text = 'Save Group Changes';
		ok_but = new Ext.Button({
			text: ok_but_text,
			minWidth: 75,
			handler: function(){
				create_group_win.hide();
				Sparrow.ui.add_group(editGroup);
				resetGroupWindow();

				// Refresh the identify window if it's visible in case our change
				// affected that reach
				//TODO this block of code is duplicate. REFACTOR
				var reachIdentifYWindow = Ext.getCmp('reach-identify-window');
				if (reachIdentifYWindow) {
					var reachId = reachIdentifYWindow.getReachId();
					IDENTIFY.identifyReach(null, null, reachId, 1, true);
				}
			}
		});
	};

	this.initCreateGroupWin = function(groupName){
		create_group_win = new Ext.Window({
			el:'add-group-area',
			title: groupName,
			closeAction:'hide',
			layout: 'fit',
			plain: true,
			modal: true,
			width: 500,
			height: 300,
			draggable: true,
			autoScroll: true,
			resizable: false,
			items: add_group_tabs,
			buttons: [
			ok_but,
			{
				text: 'Cancel',
				handler: function(){
					create_group_win.hide();
					resetGroupWindow();
				}
			}]
		});
	};

	this.open = function(groupName, editGroup, isHidden){
		if(!create_group_win) {
			this.initOKButton(editGroup);
			this.initCreateGroupWin(groupName);
		} else {

			ok_but.setText(ok_but_text);

			//select tab 0!
			add_group_tabs.setActiveTab("0");

			create_group_win.setTitle(groupName);
			ok_but.setHandler(function() {
				create_group_win.hide();
				Sparrow.ui.add_group(editGroup);
				resetGroupWindow();

				// Refresh the identify window if it's visible in case our change
				// affected that reach
				// TODO refactor this duplicate block of code
				var reachIdentifYWindow = Ext.getCmp('reach-identify-window');
				if (reachIdentifYWindow) {
					var reachId = reachIdentifYWindow.getReachId();
					IDENTIFY.identifyReach(null, null, reachId, 1, true);
				}
			});
		}

		try {
			if (!isHidden) create_group_win.show();
		} catch (e) {
		//nothing
		}
	}
})();


var reopen_add_to_group = false;
function resetGroupWindow() {
	var src_adjs = document.getElementsByName("treatment-tab_src_adj");
	for (var i = 0; i < src_adjs.length; i++) {
		src_adjs[i].value = "1";
	}
	document.getElementById("group_notes").value = '';
	document.getElementById("group_name").value = '';
	document.getElementById("group_desc").value = '';
}



/*
 * Opens the reach identify window.  This method will also refresh the window
 * if it is already visible and displaying the same reach.
 *
 * @see ReachIdentifyWindow.js
 */
function renderReachIdentifyWindow(reachResponse) {
	// If the window is visible and displaying the same reach, just refresh and
	// return, otherwise create
	var reachIdentifyWin = Ext.getCmp('reach-identify-window');
	if (reachIdentifyWin) {
		var reachId = reachResponse["sparrow-id-response"].results.result[0].identification.id;
		if (reachIdentifyWin.getReachId() == reachId) {
			reachIdentifyWin.refresh(reachResponse);
			return;
		} else {
			reachIdentifyWin.close();
		}
	}
	reachIdentifyWin = new Sparrow.ui.ReachIdentifyWindow({
		reachResponse: reachResponse
	});

	var targetsTab = Ext.getCmp('main-targets-tab');
	var adjTab = Ext.getCmp('main-adjustments-tab');

	// TODO: do we need to reset these handlers each time?
	// Get the basic reach info for handling events
	var reachId = reachResponse["sparrow-id-response"].results.result[0].identification.id;
	var reachName = reachResponse["sparrow-id-response"].results.result[0].identification.name;

	reachIdentifyWin.on('show', function(w) {
		targetsTab.setIdentifiedReach(reachId, reachName);
		adjTab.setIdentifiedReach(reachResponse);
	});
	reachIdentifyWin.on('close', function(w) {
		targetsTab.clearIdentifiedReach();
		adjTab.clearIdentifiedReach();
	});

	// Position the window at the top left of the map and show
	reachIdentifyWin.setPagePosition(map_area.getPosition()[0] + 35, map_area.getPosition()[1] + 75);
	reachIdentifyWin.show();
}

/*
 * Handles any changes that have been made to the group membership of a
 * particular reach, and/or the HUCs with which it is associated.
 */
function handleGroupMembership(reachResponse, memberGroups) {
	// Get reach info
	var reachId = reachResponse["sparrow-id-response"].results.result[0].identification.id;
	var reachName = reachResponse["sparrow-id-response"].results.result[0].identification.name;

	// Iterate over the reach's groups from the PredictionContext
	var oldGroupsList = Sparrow.SESSION.getGroupNamesFor(reachId);
	Sparrow.SESSION.consolidateEvents();

	// Iterate over the list of groups returned from ReachIdentify
	for (var i = 0; i < memberGroups.length; i++) {
		var groupName = memberGroups[i][0];
		var whatToAdd = memberGroups[i][1];

		// Create the group if it doesn't exist
		if (!Sparrow.SESSION.groupExists(groupName)) {
			Sparrow.SESSION.addGroup(groupName, '', '', null);
		}

		if (whatToAdd == 'reach') {
			if (!Sparrow.SESSION.isReachMemberOf(reachId, groupName)) {
				Sparrow.ui.confirmAndAddGroup({
		        	reachId: reachId,
		        	modelId: model_id, //TODO global removal
		        	existingGroups_xml: Sparrow.SESSION.getAdjustmentGroupsAsXML()
		        }, function() { Sparrow.SESSION.addReachToGroup(groupName, reachId, reachName); });
			}
		} else if (whatToAdd == 'upstream') {
			Sparrow.ui.confirmAndAddGroup({
	        	logicalSet_xml: Sparrow.ui.getUpstreamLogicalSetXml(reachId),
	        	modelId: model_id, //TODO global removal
	        	existingGroups_xml: Sparrow.SESSION.getAdjustmentGroupsAsXML()
	        }, function() { Sparrow.SESSION.addLogicalSetToGroup(groupName, whatToAdd, reachId, 'upstream of ' + reachId); });
		} else {
        	// Add the logical set to the group if it is not already a member
			var hucId = reachResponse["sparrow-id-response"].results.result[0].identification.hucs[whatToAdd]["@id"];
			var hucName = reachResponse["sparrow-id-response"].results.result[0].identification.hucs[whatToAdd]["@name"];
			Sparrow.ui.confirmAndAddGroup({
	        	logicalSet_xml: Sparrow.ui.getHucLogicalSetXml(whatToAdd, hucId),
	        	modelId: model_id, //TODO global removal
	        	existingGroups_xml: Sparrow.SESSION.getAdjustmentGroupsAsXML()
	        }, function() {
				Sparrow.SESSION.addLogicalSetToGroup(groupName, whatToAdd, hucId, hucName);
	        });
		}
	}
	Sparrow.SESSION.releaseEvents();	//will cause a changed event to fire
	Sparrow.SESSION.fireContextEvent('adjustment-changed');
}

Sparrow.ui.getHucLogicalSetXml = function(whatToAdd, id) {
	return "<logicalSet>"
	  + "<criteria attrib=\""+whatToAdd+"\">"+id+"</criteria>"
	  + "</logicalSet>";
}

Sparrow.ui.getUpstreamLogicalSetXml = function(id) {
	return "<logicalSet>"
	  + "<criteria attrib=\"reach\" relation=\"upstream\">"+id+"</criteria>"
	  + "</logicalSet>";
}

Sparrow.ui.confirmAndAddGroup = function(params, handler) {
	Ext.getBody().mask('Checking reach groups... <input type="button" onclick="Ext.getBody().unmask();Ext.Ajax.abort();" value="cancel" />', 'x-mask-loading');
	Ext.Ajax.request({
        url: 'getConflictingReachGroups',
        method: 'GET',
        params: params,
        scope: this,
        success: function(response, options) {
        	Ext.getBody().unmask();
        	var result = (new Ext.data.XmlReader({
    			record: 'entity',
    			root: 'ServiceResponseWrapper.entityList'
    		}, ["type", "groupName", "value"])).readRecords(response.responseXML);
        	if(result.records.length > 0){
        		var groupString = "";
        		for(var i = 0; i < result.records.length; i++) {
        			var groupName = result.records[i].data.groupName.toLowerCase() == "individual" ? "Reaches with Absolute Changes" : result.records[i].data.groupName;
        			var setName = '';

        			if(groupName == "individual") {
        				setName = "Reach ID: " + result.records[i].data.value;
        			} else if(result.records[i].data.type.toLowerCase() == "reach"){
        				setName = "upstream of reach: "+result.records[i].data.value;
        			} else if(result.records[i].data.type.toLowerCase() == "individual") {
        				setName = "Reach ID: "+result.records[i].data.value;
        			} else {
        				setName = result.records[i].data.type.toUpperCase()+": "+result.records[i].data.value;
        			}

        			groupString += "<span>- " + groupName + " ("+setName+")</span><br/>";
        		}
	        	Ext.MessageBox.confirm(
					"Reaches Exist in Multiple Groups",
					'Reach(es) being added could already be included in the following groups:<br/><div style="height: 100px; width: 400px; background-color: transparent; overflow: auto; border: 1px solid #6593cf; padding: 5px;">' +
					groupString +
					'</div>Continue adding to group?',
					function(answer){
						if(answer=='yes') handler();
					},
					this);
        	} else handler();
        },
        failure: function(response, options) {
        	Ext.getBody().unmask();
        	//TODO:  Should really detect failure based on the status flag
        	if (response.isTimeout) {
        		Ext.Msg.alert('Warning', 'Reach check timed out')
        	} else {

        	}
        },
        timeout: 40000
    });
}

/*
 * Handles any adjustments that may have been made to the identified reach.
 * This function will add and remove adjustment information to the individual
 * group based on the values in overrideValues.
 */
function handleAdjustments(reachId, reachName, overrideValues) {
	for (var i = 0; i < overrideValues.length; i++) {
		if (isNaN(parseFloat(overrideValues[i]))) {
			Sparrow.SESSION.removeAdjustment(reachId, i + 1);
		} else {
			Sparrow.SESSION.addAdjustment(reachId, reachName, i + 1, overrideValues[i]);
		}
	}
}

/*
 * Opens a popup dialog to handle creation, maintenance, and removal of custom
 * buckets for thematic maps.  This function passes the list of saved buckets
 * (if any exist) to the popup, where the user may manipulate them as desired.
 * If the user elects to save their changes this function handles the save event
 * generated by the popup.
 */
function openCustomBucketsWindow() {

	var customBucketsWin = new Ext.ux.CustomBinsWindow({});

	// Determine if we already have custom buckets, and load if so
	var binning = Sparrow.SESSION.getBinData();
	if (binning.functionalBins.length > 0) {
		customBucketsWin.loadBuckets(binning.functionalBins, binning.binColors);
	}

	customBucketsWin.show();

}
/**
 * Warning window which pops up if Ext detects user is using IE6
 */
var IE6_WARN_WINDOW = new (function(){
	var ie_warn_win;

	this.open = function(){
		if(!ie_warn_win){
			ie_warn_win = new Ext.Window({
				title: 'Warning...',
				contentEl:'ie6-warn-win',
				closeAction:'close',
				layout: 'fit',
				plain: true,
				modal: true,
				width: 600,
				height: 400,
				draggable: false,
				resizable: false,
				autoScroll: true,
				buttons: [{
					text:'Continue at your own risk...',
					handler: function() {
						ie_warn_win.close();
					}
				}]
			});
		}
		ie_warn_win.show();
	}
})();


function cancelEventPropagation(e) {
	var event = e || window.event;

	if (event.stopPropagation) {
		event.stopPropagation();
	} else {
		event.cancelBubble = true;
	}
}

function toggleAccordion(node_id, tog_id) {

	var reach_list = document.getElementById(node_id);
	var toggle_arrow = document.getElementById(tog_id);
	if (reach_list.style.display == 'block') {
		reach_list.style.display = 'none';
		toggle_arrow.src = 'images/group_min.gif';
	} else {
		var groups = JMap.util.getElementsByClassName(document, 'group_reaches');
		for (var i = 0; i < groups.length; i++) {
			groups[i].style.display = 'none';
		}

		var group_arrows = JMap.util.getElementsByClassName(document, 'group_toggle');
		for (var i = 0; i < group_arrows.length; i++) {
			group_arrows[i].src = 'images/group_min.gif';
		}

		reach_list.style.display = 'block';
		toggle_arrow.src = 'images/group_max.gif';
	}
}

function hideGroupLinks(node_id) {
	document.getElementById(node_id).style.display = 'none';
	document.onmousemove = null;
	var gps = JMap.util.getElementsByClassName(document, 'group_element');
	for (var i = 0; i < gps.length; i++) {
		gps[i].style.zIndex = 0;
	}
}

function showGroupLinks(node_id) {
	document.getElementById(node_id).style.display = 'block';
	document.onmousemove = function(event) {
		checkCloseGroupLinks(event, node_id);
		cancelEventPropagation(event);
	};
}

function checkCloseGroupLinks(e, node_id) {
	var event = e || window.event;
	var group_elem = document.getElementById(node_id);

	var target_elem = event.srcElement || event.target;

	if ('group_edit' != target_elem.className &&
		'group_edit_list' != target_elem.parentNode.className &&
		'group_edit_list' != target_elem.className) {
		hideGroupLinks(node_id);
	}

	cancelEventPropagation(e);

}