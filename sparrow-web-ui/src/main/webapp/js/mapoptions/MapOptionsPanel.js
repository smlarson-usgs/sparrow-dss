/*
 * Form panel containing all of the UI controls for specifying mapping options
 * and setting up the PredictionContext.
 *
 * TODO: fire events instead of performing edits on the PredictionContext JSON
 * object directly -- this needs to be a view only
 * 
 * Right now all fields update the SESSION, it is up to the SESSION to fire
 * events, and event.js to properly connect the event handling
 */

//Add QuickTips to menu items, with no delay
Ext.apply(Ext.QuickTips.getQuickTip(), {
	showDelay: 0
});

MapOptionsPanel = Ext.extend(Ext.form.FormPanel, {
	qTips : {
		dataSeries: 'Select a data series to map.',
		dataSeriesLabel: 'Select a data series to map.',
		source: 'Select a model source to use for SPARROW analysis. For help choosing a model source, click the link above.',
		sourceLabel: 'Select a model source to use for SPARROW analysis. For help choosing a model source, click here.',
		binning: 'Select binning (intervals) and color scheme for map legend. For help on binning, click the link above.',
		binningLabel: 'Select binning (intervals) and color scheme for map legend. For help on binning, click here.',
		comparison: 'Select whether to compare your adjusted model to the unadjusted (original) model. This is used to determine the effects of adjusting various model inputs. Comparing to the original model is only useful after you create an <i>adjustment group</i> and apply treatments to it. Please refer to the <b>Change Inputs</b> tab for help on groups. For information about comparing to the original model, click the link above.',
		comparisonLabel: 'Select whether to compare your adjusted model to the unadjusted (original) model. This is used to determine the effects of adjusting various model inputs. Comparing to the original model is only useful after you create an <i>adjustment group</i> and apply treatments to it. Please refer to the <b>Change Inputs</b> tab for help on groups. For information about comparing to the original model, click here.',
		reachesOrCatchments: 'Select whether to display individual river reaches or individual river catchments. Reaches are the actual bodies of water, while catchments refer to the area of land surrounding each reach. For help on this subject, click the link above.',
		reachesOrCatchmentsLabel: 'Select whether to display individual river reaches or individual river catchments. Reaches are the actual bodies of water, while catchments refer to the area of land surrounding each reach. For help on this subject, click here.',
		chooseBackground: 'Select layers (streets, urban areas, etc.) to display on the generated map.',
		displayCalibSites: 'Show an overlay of the locations of the monitoring sites used to calibrate the model.',
		displayReachOverlay: 'When mapping catchments, this option show an overlay of the stream reaches in grey.'
	},

	constructor : function(config) {
		var fieldsAnchor = '97%';
		var fieldsetAnchor = '92%';
		
		//Field creation section, fields are attached to this if needed on the outside
		this.mapUnitsRadioGrp = new Ext.form.RadioGroup({ //referenced by multiple handlers, so declare here and attach to this
			name: 'mapUnits',
			disabled: true,
    		itemCls: 'zero-padding',
    		ctCls: 'zero-padding',
    		columns: 2, 
			items: [
			        new Ext.form.Radio({
			        	checked: true,
			        	boxLabel: 'Mass',
			        	name: 'mapUnits',
			    		itemCls: 'zero-padding',
			    		ctCls: 'zero-padding',
			        	value: ''
			        }),
			        new Ext.form.Radio({
			        	checked: false,
			        	boxLabel: 'Percent',
			        	name: 'mapUnits',
			    		itemCls: 'zero-padding',
			    		ctCls: 'zero-padding',
			        	value: 'percent'
			        })
			        ],
			        listeners: {
			        	'change': function(grp, radio) {
			        		Sparrow.SESSION.setComparisons(radio? radio.value : '');
			        	}
			        }
		});
		
		//this event is fired to remove the mutually exclusive source comparison node from the session json
		this.mapUnitsRadioGrp.on('render', function(){ Sparrow.SESSION.setComparisons('');}, {single:true});

		this.dataSeriesCombo = new Ext.ux.form.GroupComboBox({
			store: new Ext.data.GroupingStore({
				reader: new Ext.data.ArrayReader({}, Sparrow.config.ComboValues.dataSeriesRecordDef),
				data: Sparrow.config.ComboValues.dataSeries,
		       sortInfo:{field: 'group', direction: "ASC"},
		       groupField: 'group'
			}),
			mode: 'local',
			anchor: fieldsAnchor,
			triggerAction: 'all',
			editable: false,
			labelSeparator: '',
			fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Data Series\')">Data Series</a>',
			displayField: 'display',
			valueField: 'value',
			value: 'total',
			groupTextTpl: '<h2>{text}</h2>',
			showGroupName: false,
			startCollapsed: false,
			listeners: {
				'select': this.handleDataSeriesSelect, 
				'afterrender': function(combo) {
					Ext.QuickTips.register({
						target: combo,
						title: 'Data Series',
						text: this.qTips.dataSeries,
						dismissDelay: 1000000
					});        	
					Ext.QuickTips.register({
						target: combo.label,
						title: 'Data Series',
						text: this.qTips.dataSeriesLabel,
						dismissDelay: 1000000
					});
				},
				scope: this
			}
		});

		this.modelSourceCombo = new Ext.form.ComboBox({
			tpl: '<tpl for="."><div ext:qtip="{desc}" class="x-combo-list-item">{name}</div></tpl>',
			store: new Ext.data.SimpleStore({
				fields: ['name', 'value', 'desc'],
				data: [
				       ['All', '0', 'All sources combined.']
				       ]
			}),
			mode: 'local',
			anchor: fieldsAnchor,
			triggerAction: 'all',
			hideLabel: false,
			editable: false,
			fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.For Source\')">Model Source</a>',
			labelSeparator: '',
			displayField: 'name',
			valueField: 'value',
			value: '0',
			listeners: {
				'afterrender': function(combo) {
					Ext.QuickTips.register({
						target: combo,
						title: 'Model Source',
						text: this.qTips.source,
						dismissDelay: 1000000
					});        		
					Ext.QuickTips.register({
						target: combo.label,
						title: 'Model Source',
						text: this.qTips.sourceLabel,
						dismissDelay: 1000000
					});
				},
				'select': function(combo, record, index) {
					var value = null;
					if(record) value = record.get('value');
					Sparrow.SESSION.setDataSeriesSource(value);
				},
				scope: this
			}
		});
		this.savedAllOptionsStore = this.modelSourceCombo.store;
		
		this.comparisonBucketBtn = new Ext.Button({ //referenced by multiple handlers, so declare here and attach to this
			text: 'Edit Custom Bins...',
			handler: openCustomBucketsWindow, //TODO global function reference, do not like
			tooltip: {
				title: 'Binning for Map Color and Legend',
				text: this.qTips.binning,
				dismissDelay: 1000000
			},
			disabled: true
		});

		this.mapDisplayTypeRadioGrp = new Ext.form.RadioGroup({
			name: 'mapDisplayType',
			fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Reaches Or Catchments\')">Display</a>',
			anchor: fieldsAnchor,
			items: [
			        new Ext.form.Radio({
			        	checked: false,
			        	boxLabel: 'Reaches',
			        	name: 'mapDisplayType',
			        	value: 'reach'
			        }),
			        new Ext.form.Radio({
			        	checked: true,
			        	boxLabel: 'Catchments',
			        	name: 'mapDisplayType',
			        	value: 'catch'
			        })
			        ],
			        listeners: {
			        	'change': function(grp, radio) {
			        		this.setWhatToMapCtl(radio.value);
			        		Sparrow.SESSION.setWhatToMap(radio.value);
			        	},
			        	'afterrender': function(grp) {
			        		this.setWhatToMapCtl("catch");
			        		Ext.QuickTips.register({
			        			target: grp.label,
			        			title: 'Display Reaches or Catchments',
			        			text: this.qTips.reachesOrCatchmentsLabel,
			        			dismissDelay: 1000000
			        		});
			        	},
			        	scope: this
			        }
		});
		
		this.comparisonCombo = new Ext.form.ComboBox({
        	disabled: true, 
        	store: new Ext.data.SimpleStore({
        		fields: ['name', 'value'],
        		data: [
        		       ['Do Not Compare', 'none'],
        		       ['Absolute change from original', 'absolute'],
        		       ['% change from original', 'percent_change']
        		       ]
        	}),
        	mode: 'local',
        	triggerAction: 'all',
        	anchor: fieldsAnchor,
        	hideLabel: false,
        	labelSeparator: '',
        	editable: false,
        	displayField: 'name',
        	fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Comparison\')">Comparison To Original Model</a>',
        	valueField: 'value',
        	value: 'none',
        	listeners: {
        		'afterrender': function(comparisonCombo) {
        			Ext.QuickTips.register({
        				target: comparisonCombo,
        				title: 'Comparison to Original Model',
        				text: this.qTips.comparison,
        				dismissDelay: 1000000
        			});
        			Ext.QuickTips.register({
        				target: comparisonCombo.label,
        				title: 'Comparison to Original Model',
        				text: this.qTips.comparisonLabel,
        				dismissDelay: 1000000
        			});
        		},
        		'select': function(combo, record, index) {
        			Sparrow.SESSION.setComparisons(record.get('value'));
        		}, scope: this
        	}
        });
		
		this.calibSitesChk = new Ext.form.Checkbox({
        	boxLabel: 'Calibration Sites',
        	checked: Sparrow.SESSION.isCalibSitesOverlayRequested(),
        	handler: function(checkbox, checked) {
        		Sparrow.SESSION.setCalibSitesOverlayRequested(checked);
        		this.calibSitesSlider.setDisabled(!checked);
        	}, 
        	scope: this
        });
		
		this.calibSitesSlider = new Ext.Slider({
				fieldLabel: ' ',
				labelSeparator: '',
				layerId: Sparrow.config.layers.calibrationSiteLayer.id,
			    width: 70,
			    value: Sparrow.SESSION.getCalibSitesOverlayOpacity(),
			    increment: 1,
			    minValue: 1,
			    maxValue: 100,
			    disabled: ! Sparrow.SESSION.isCalibSitesOverlayRequested(),
			    listeners: {
					change: function(s, v) {
						Sparrow.SESSION.setCalibSitesOverlayRequested(true, v);
					}
				}
			});
		
		this.reachOverlayChk = new Ext.form.Checkbox({
        	boxLabel: 'Reach Overlay',
        	checked: Sparrow.SESSION.isReachOverlayRequested(),
        	handler: function(checkbox, checked) {
        		Sparrow.SESSION.setReachOverlayRequested(checked);
        		this.reachOverlaySlider.setDisabled(!checked);
        	},
        	scope: this
        });
		
		this.reachOverlaySlider = new Ext.Slider( {
			fieldLabel: ' ',
			labelSeparator: '',
		    width: 70,
		    value: Sparrow.SESSION.getReachOverlayOpacity(),
		    increment: 1,
		    minValue: 1,
		    maxValue: 100,
		    disabled: ! Sparrow.SESSION.isReachOverlayRequested(),
		    listeners: {
				change: function(s, v) {
					Sparrow.SESSION.setReachOverlayRequested(true, v);
				}
			}
		});
		
		this.huc8OverlayChk = new Ext.form.Checkbox({
        	boxLabel: 'HUC8 Overlay',
        	checked: Sparrow.SESSION.isHuc8OverlayRequested(),
        	handler: function(checkbox, checked) {
        		Sparrow.SESSION.setHuc8OverlayRequested(checked);
        		this.huc8OverlaySlider.setDisabled(!checked);
        	},
        	scope: this
        });
		
		this.huc8OverlaySlider = new Ext.Slider( {
			fieldLabel: ' ',
			labelSeparator: '',
		    width: 70,
		    value: Sparrow.SESSION.getHuc8OverlayOpacity(),
		    increment: 1,
		    minValue: 1,
		    maxValue: 100,
		    disabled: ! Sparrow.SESSION.isHuc8OverlayRequested(),
		    listeners: {
				change: function(s, v) {
					Sparrow.SESSION.setHuc8OverlayRequested(true, v);
				}
			}
		});
		
		this.bucketLabel = new Ext.form.Label({
        	text: 'None',
        	fieldLabel: ' ',
        	labelSeparator: '',
        	listeners: {
        		"afterrender": function(label) {
        			Ext.QuickTips.register({
        				target: label,
        				title: 'Binning for Map Color and Legend',
        				text: this.qTips.binning,
        				dismissDelay: 1000000
        			});
        		},
        		scope: this
        	}
        });
		
		this.autoBinsChk = new Ext.form.Checkbox({
    		boxLabel: 'Auto binning&nbsp;&nbsp;',
    		checked: true,
    		listeners: {
    			'check': {
    				fn: function() {
    					if(this.autoBinsChk.checked) {
    						this.comparisonBucketBtn.disable();
    						Sparrow.SESSION.setBinAuto(true);
    					}
    					else {
    						this.comparisonBucketBtn.enable();
    						Sparrow.SESSION.setBinAuto(false);
    					}
    				},
    				scope: this
    			}
    		}
    	});

		//actual LAYOUT portion starts here
		var dataSeriesSection = new Ext.form.FieldSet({
			anchor: fieldsetAnchor,
			labelAlign: 'top',
    		itemCls: 'zero-padding',
    		ctCls: 'zero-padding',
			items: [
			        {xtype: 'label', html: '<b>1. Select a Data Series</b>'},
			        this.dataSeriesCombo,{
			        	xtype: 'panel',
			        	labelSeparator: '',
			        	layout: 'form',
			        	labelAlign: 'top',
			    		itemCls: 'zero-padding',
			    		ctCls: 'zero-padding',
			        	border: false,
			        	items: [
			        	        this.comparisonCombo]
			        }
			        ]
		});

		var modelSourceSection = new Ext.form.FieldSet({
			anchor: fieldsetAnchor,
			labelAlign: 'top',
    		itemCls: 'zero-padding',
    		ctCls: 'zero-padding',
			items: [
			        {xtype: 'label', html: '<b>2. Select a Model Source</b>'},
			        this.modelSourceCombo,{
			    		itemCls: 'zero-padding',
			    		ctCls: 'zero-padding',
			    		layout: 'column',
			    		border: false,
			    		items: [{
			    			html: "Map Units:", 
			    			border: false,
			    			bodyStyle: 'padding-top: 3px;',
			    			columnWidth: .3
			    		},{
			    			columnWidth: .6,
			    			border: false,
				    		itemCls: 'zero-padding',
				    		ctCls: 'zero-padding',
			    			items: [this.mapUnitsRadioGrp]
			    		}]
			        }
			        ]
		});

		var mapDisplayOptionsSection = new Ext.form.FieldSet({
			anchor: fieldsetAnchor,
			labelWidth: 50,
			labelAlign: 'left',
    		itemCls: 'zero-padding',
    		ctCls: 'zero-padding',
			items: [
			        {xtype: 'label', html: '<b>3. Select the map display options</b>'},
			        this.mapDisplayTypeRadioGrp,{
						border: false,
						layout: 'column',
						fieldLabel: ' ',
			        	labelSeparator: '',
			        	anchor: fieldsAnchor,
						items: [{
							layout: 'anchor',
							border: false,
							columnWidth: 0.58,
							items:[this.calibSitesChk, this.reachOverlayChk, this.huc8OverlayChk]
						},{
							layout: 'anchor',
							border: false,
							columnWidth: 0.40,
							items:[this.calibSitesSlider, this.reachOverlaySlider, this.huc8OverlaySlider]
						}
						]
					},
			        {
			        	xtype: 'panel',//just a label
			        	layout: 'form',
			        	labelWidth: 200,
			        	labelAlign: 'left',
			    		itemCls: 'zero-padding',
			    		ctCls: 'zero-padding',
			        	border: false,
			        	items: [new Ext.form.Label({
			        		fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Bins\')">Binning for Map Color and Legend</a>',
			        		text: '',
			        		labelSeparator: '',
			        		listeners: {
			        			"afterrender": function(label) {
			        				Ext.QuickTips.register({
			        					target: label.label,
			        					title: 'Binning for Map Color and Legend',
			        					text: this.qTips.binningLabel,
			        					dismissDelay: 1000000
			        				});
			        			},
			        			scope: this
			        		}
			        	})]
			        },
			        this.bucketLabel, {
			        	xtype: 'panel',
			        	fieldLabel: ' ',
			        	labelSeparator: '',
			        	anchor: fieldsAnchor,
			        	layout: 'hbox',
			        	border: false,
			        	items: [this.autoBinsChk,
			        	this.comparisonBucketBtn
			        	]
			        }
			        ]
		});

		var defaults = {
				border: true,
				autoScroll: true,
				region: 'center',
				padding: 5,
				items: [
				        dataSeriesSection,
				        modelSourceSection,
				        mapDisplayOptionsSection
				        ]
		};

		config = Ext.applyIf(config, defaults);
		MapOptionsPanel.superclass.constructor.call(this, config);
	},

	/*
	 * Adds a new source to the 'For Source' dropdown.
	 */
	addSource: function(sourceName, sourceValue, sourceDescription) {
		var SourceRecord = Ext.data.Record.create([{name: 'name'}, {name: 'value'}, {name: 'desc'}]);
		var newRecord = new SourceRecord({name: sourceName, value: sourceValue, desc: sourceDescription});
		this.modelSourceCombo.store.add(newRecord);
		this.savedAllOptionsStore = this.modelSourceCombo.store;
	},


	clearSources: function() {
		this.modelSourceCombo.store.removeAll();
	},

	//TODO:  Move this all to an event
	handleDataSeriesSelect : function(combo, record, index) {
		var dataSeriesValue = combo.getValue();

		var sourceCombo = this.modelSourceCombo;
		var initSourceValue = sourceCombo.getValue();

		//central 'series changed' handler w/ side effects
		this.setDataSeriesCtl(dataSeriesValue);

		var finalSourceValue = sourceCombo.getValue();
		var effectiveSourceValue = sourceCombo.disabled ? -1 : finalSourceValue;

		if (initSourceValue != finalSourceValue) {
			//The source selection has been forced to change as a result
			//of the dataSeries selection.
			sourceCombo.focus();
			sourceCombo.syncSize();
		}

		//Ensure up to date, will cause the Context to mark its state as dirty
		Sparrow.SESSION.setDataSeries(dataSeriesValue);
		Sparrow.SESSION.setDataSeriesSource(effectiveSourceValue);
	},
	
	/*
	 * Sets the 'Data Series' dropdown value and then filters the 'For Source'
	 * dropdown based on that value.  This method does not fire the 'select'
	 * event for either dropdown.
	 * 
	 * This method provides a unified means to set this value, since in some
	 * cases the user will pick the value, other times it will be loaded from
	 * a predefined prediction context.
	 */
	setDataSeriesCtl: function(value, combo) {
		var ctrl = combo || this.dataSeriesCombo;
		var initValue = ctrl.getValue();

		if (ctrl.findRecord(ctrl.valueField, value) && value != initValue) {
			//This is a non-user caused value change
			ctrl.setValue(value);
		}

		this.filterSourceCombo(combo);	//possibly updates the sourceCombo
	},

	/*
	 * Sets the 'For Source' dropdown value.  This method does not fire the
	 * 'select' event for the dropdown.
	 */
	setSourceCtl: function(value) {
		var sourceCombo = this.modelSourceCombo;
		var oldVal = sourceCombo.getValue();
		sourceCombo.setValue(value ? value : 0);
		if(oldVal !== value) sourceCombo.fireEvent("select", sourceCombo, sourceCombo.findRecord(sourceCombo.valueField, value), null); //warning, null should be the selected index
	},

	/*
	 * Sets the 'Comparison' dropdown value and then filters the 'Buckets'
	 * dropdown based on that value.  This method does not fire the 'select'
	 * event for either dropdown.
	 */
	setComparisonCtl: function(value) {
		var rad = this.mapUnitsRadioGrp;
		if(value) {
			for(var i = 0; i < rad.items.items.length; i++) {
				if(value == rad.items.items[i].value) {
					rad.items.items[i].setValue(true);
					this.mapUnitsRadioGrp.fireEvent('change', this.mapUnitsRadioGrp, rad.items.items[i]);
					break;
				}
			}
		} else {
			rad.items.items[0].setValue(true); //always set the default value 
		}
	
		this.comparisonCombo.setValue(value);
	},

	/*
	 * Sets the 'Buckets' dropdown value.  This method does not fire the
	 * 'select' event for the dropdown.
	 */
	setBinDescription: function(value) {
		this.bucketLabel.setText(value);
	},

	/*
	 * Sets the 'Map' dropdown value and then filters the 'Aggregate Function'
	 * dropdown based on that value.  This method does not fire the 'select'
	 * event for either dropdown.
	 */
	setWhatToMapCtl: function(value) {
		var rad = this.mapDisplayTypeRadioGrp;
		for(var i = 0; i < rad.items.items.length; i++) {
			rad.items.items[i].setValue(value == rad.items.items[i].value);
		}
	},
	
	syncDataOverlayControlsToSession: function() {
		//Calibration site overlay
		this.calibSitesChk.setValue(Sparrow.SESSION.isCalibSitesOverlayRequested());
		this.calibSitesSlider.setValue(Sparrow.SESSION.getCalibSitesOverlayOpacity());
		
		//Reach Overlay
		this.reachOverlayChk.setValue(Sparrow.SESSION.isReachOverlayRequested());
		this.reachOverlaySlider.setValue(Sparrow.SESSION.getReachOverlayOpacity());
		if (Sparrow.SESSION.isReachOverlayEnabled()) {
			this.reachOverlayChk.enable();
			this.reachOverlaySlider.enable();
		} else {
			this.reachOverlayChk.disable();
			this.reachOverlaySlider.disable();
		}
		
		//Huc Overlay
		this.huc8OverlayChk.setValue(Sparrow.SESSION.isHuc8OverlayRequested());
		this.huc8OverlaySlider.setValue(Sparrow.SESSION.getHuc8OverlayOpacity());
	},

	/*
	 * Filters the 'For Source' dropdown based on the value of the 'Data Series'
	 * dropdown.
	 */
	filterSourceCombo: function(dataSeriesCombo) {
		if(!dataSeriesCombo) dataSeriesCombo = this.dataSeriesCombo;
		// Get the selected data series record and the sources combo
		var dataSeriesValue = dataSeriesCombo.getValue();
		var dataSeriesStore = dataSeriesCombo.store;
		var dataSeriesRecordIndex = dataSeriesStore.find('value', dataSeriesValue);
		var dataSeriesRecord = dataSeriesStore.getAt(dataSeriesRecordIndex);
		var sourceCombo = this.modelSourceCombo;
		var initSourceValue = sourceCombo.getValue();

		if ('source-all' == dataSeriesRecord.get('type')) {
			sourceCombo.store = this.savedAllOptionsStore;
			//add the 'all' option back in if it is missing
			if (sourceCombo.store.find('name', 'All') == -1) {
				var SourceRecord = Ext.data.Record.create([{name: 'name'}, {name: 'value'}, {name: 'desc'}]);
				var newRecord = new SourceRecord({name: 'All', value: '0', desc: 'All sources combined.'});
				sourceCombo.store.insert(0, newRecord);
			}

			sourceCombo.resetDropDownList();
			sourceCombo.enable();
		} else if ('source-specific' == dataSeriesRecord.get('type')) {
			var newStore = this.cloneSimpleStore(this.savedAllOptionsStore);
			// Remove 'All' if we're dealing with a non-calculated data series
			var i = newStore.find('name', 'All');

			if (i > -1) {
				newStore.remove(newStore.getAt(i));
			}

			sourceCombo.store = newStore;
			sourceCombo.resetDropDownList();
			if (initSourceValue == '0') {
				//The value '0' is the 'All' selection.  If picked, switch
				//to the first source in the list.
				sourceCombo.setValue(sourceCombo.store.getAt(0).get('value'));
			}
			sourceCombo.enable();

		} else if ('source-none' == dataSeriesRecord.get('type')) {
			if (sourceCombo.disabled == false) {
				sourceCombo.disable();
			} 
		} else if ('source-all-only' == dataSeriesRecord.get('type')) {
			var newStore = this.cloneSimpleStore(this.savedAllOptionsStore);
			newStore.each(function(r){
				if(r.data.name!='All') newStore.remove(r);
			});
			
			sourceCombo.store = newStore;
			sourceCombo.resetDropDownList();
			if (initSourceValue != '0') {
				sourceCombo.setValue(sourceCombo.store.getAt(0).get('value'));
			}
			sourceCombo.enable();
		} else {
			Ext.Msg.alert('Warning', "unexpected datasource type: " + dataSeriesRecord.get('type'));
		}
	},
	
	//private util
	cloneSimpleStore : function(store){
		var data = [];
		store.each(function(r){
			data.push([r.data.name, r.data.value, r.data.desc]);
		});
		var n = new Ext.data.SimpleStore({
			fields: ['name', 'value', 'desc'],
			data: data
		});
		return n;
	}
});

Ext.reg('mapOptionsPanel', MapOptionsPanel);