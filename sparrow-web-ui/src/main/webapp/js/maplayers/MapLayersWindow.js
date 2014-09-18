MapLayersWindow = Ext.extend(Ext.Window, {
	id: 'map-layers-window',
	layout: 'border',
	title: 'Change Map Layers...',
	modal: true,
	resizable: false,
	width: 600,
	height: 400,
	map: this.map,
	
	initComponent: function() {
		this.controller = new MapLayersController({map: this.map});
	
		Ext.apply(this, {
			items: {
				id: 'map-layers-panel',
				region: 'center',
				layout: 'anchor',
				autoScroll: true,
				border: false,
				bodyStyle: 'padding: 5px'
			},
			buttons: [{
				text: 'Done',
				handler: function() {
					Ext.getCmp('map-layers-window').close();
				}
			}]
		});
		MapLayersWindow.superclass.initComponent.apply(this, arguments);
	},
	
	
	show: function() {
		MapLayersWindow.superclass.show.call(this);
		this.controller.listLayers();
		this.doLayout();
	}
});


function MapLayersController(params) {
	this.map = params.map;
	
	this.listLayers = function() {
		//get layers
		this.mapLayers = Sparrow.SESSION.getAvailableMapLayers();
		var panel = Ext.getCmp('map-layers-panel');
		var _this = this;

		for (var i = 0; i < this.mapLayers.length; i++) {
			
			var l = this.mapLayers[i];
			
			if (! l.isHiddenFromUser) {
				

				var layerHTML = '';
				layerHTML += '<div class="clearfix">';
				layerHTML += '<img style="float:left; border: dotted gray 1px; width: 150px;" src="' + l.legendUrl + '" alt="legend" />';
				layerHTML += '<div style="float:left; padding-left: 5px; width: 345px;">';
				layerHTML += '</div>';

				var descHTML = '<h2>' + l.title + '</h2>';

				if (! map1.layerManager.isLayerAvailable(l)) {
					descHTML += '<h2 style="color:red">This layer is not available at the current zoom scale</h2>';
				}

				descHTML += '<p>Description: ' + l.description + '</p>';
				descHTML += '</div>';

				var slider = new Ext.Slider({
					fieldLabel: 'Opacity',
					layerId: l.id,
					width: 100,
					value: l.opacity,
					increment: 1,
					minValue: 1,
					maxValue: 100,
					disabled: ! (Sparrow.SESSION.isMapLayerEnabled(l.id)),
					listeners: {
						change: function(s, v) {			
							Sparrow.SESSION.setMapLayerEnabled(s.layerId, v);
						}
					}
				});

				//create a panel for this layer
				var p = new Ext.Panel({
					style: 'padding-bottom: 5px',
					bodyStyle: 'padding: 3px;',
					layout: 'column',
					layoutConfig: {columns: 2},
					items: [{
						width: 160,
						xtype: 'form',
						labelWidth: 50,
						border: false,
						items: [{
							xtype: 'panel',
							bodyStyle: 'padding: 3px',
							html: layerHTML,
							border: false
						},{
							xtype: 'checkbox',
							fieldLabel: 'On/Off',
							checked: Sparrow.SESSION.isMapLayerEnabled(l.id),
							layerId: l.id,
							opacitySlider: slider,
							listeners: {
								check: function(c,b) {
									this.opacitySlider.setDisabled(!b);

									if (b) {
										Sparrow.SESSION.setMapLayerEnabled(c.layerId);
									} else {
										Sparrow.SESSION.setMapLayerDisabled(c.layerId);
									}
								}
							}
						},
						slider
						]
					},{
						html: descHTML,
						width: 350,
						border: false
					}]
				});
				panel.add(p);
			}
		}		
	};
}
