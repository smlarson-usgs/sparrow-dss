/**
 * @class Ext.ux.CustomBinsWindow
 * @extends Ext.Window
 *
 * Popup window used to create, manage, and remove custom color-coded buckets.
 * The buckets are listed in an editable grid where each row may be selected,
 * removed, and/or moved up or down.  Each cell may also be edited to provide
 * new boundaries for the buckets or to select a new color.
 *
 * This implementation also contains a link to an 'auto-generate' popup which
 * allows the user to select a bucket type and bucket count.  These parameters
 * are passed on to a bucketing service which automatically generates a list of
 * buckets based on the bucketing scheme and data that are appropriate to this
 * window's context.
 *
 * @constructor
 * @param {Object} config The config object.
 */
Ext.ux.CustomBinsWindow = Ext.extend(Ext.Window, {
    // Default configuration parameters (may be changed by instantiator)
    title: 'Define Custom Bins',

    bucketStore: null,

    initComponent: function() {

        this.bucketTypeDisp = 'Custom';

        this.bucketStore = new Ext.data.SimpleStore({
            fields: ['low', 'high', 'color'],
            data: [],
            listeners: {
                scope: this,
                update: function(store, record) {
                    var low = record.get('low');
                    var high = record.get('high');

                    if (isNaN(parseFloat(low))) {
                        record.set('low', '-Infinity');
                    }
                    if (isNaN(parseFloat(high))) {
                        record.set('high', 'Infinity');
                    }
                    // Commit immediately to avoid the dirty marker in the cell.
                    // This means buckets must ultimately be stored outside of this
                    // window -- the store is only valid as long as the window
                    // remains open.
                    store.commitChanges();
                    this.bucketTypeDisp = 'Custom';
                }
            }
        });

        var colorPallet = [
			'00FF00', '00FF33', '00FF66', '00FF99', '00FFCC', '00FFFF', '00CC00', '00CC66', '00CC66', '00CC99', '00CCCC', '00CCFF', '009900', '009933', '009966', '009999', '0099CC', '0099FF',
			'33FF00', '33FF33', '33FF66', '33FF99', '33FFCC', '33FFFF', '33CC00', '33CC33', '33CC66', '33CC99', '33CCCC', '33CCFF', '339900', '339933', '339966', '339999', '3399CC', '3399FF',
			'66FF00', '66FF33', '66FF66', '66FF99', '66FFCC', '66FFFF', '66CC00', '66CC33', '66CC66', '66CC99', '66CCCC', '66CCFF', '669900', '669933', '669966', '669999', '6699CC', '6699FF',
			'99FF00', '99FF33', '99FF66', '99FF99', '99FFCC', '99FFFF', '99CC00', '99CC33', '99CC66', '99CC99', '99CCCC', '99CCFF', '999900', '999933', '999966', '999999', '9999CC', '9999FF',
			'CCFF00', 'CCFF33', 'CCFF66', 'CCFF99', 'CCFFCC', 'CCFFFF', 'CCCC00', 'CCCC33', 'CCCC66', 'CCCC99', 'CCCCCC', 'CCCCFF', 'CC9900', 'CC9933', 'CC9966', 'CC9999', 'CC99CC', 'CC99FF',
			'FFFF00', 'FFFF33', 'FFFF66', 'FFFF99', 'FFFFCC', 'FFFFEE', 'FFCC00', 'FFCC33', 'FFCC66', 'FFCC99', 'FFCCCC', 'FFCCFF', 'FF9900', 'FF9933', 'FF9966', 'FF9999', 'FF99CC', 'FF99FF',
			'006600', '006633', '006666', '006699', '0066CC', '0066FF', '003300', '003333', '003366', '003399', '0033CC', '0033FF', '000011', '000033', '000066', '000099', '0000CC', '0000FF',
			'336600', '336633', '336666', '336699', '3366CC', '3366FF', '333300', '333333', '333366', '333399', '3333CC', '3333FF', '330000', '330033', '330066', '330099', '3300CC', '3300FF',
			'666600', '666633', '666666', '666699', '6666CC', '6666FF', '663300', '663333', '663366', '663399', '6633CC', '6633FF', '660000', '660033', '660066', '660099', '6600CC', '6600FF',
			'996600', '996633', '996666', '996699', '9966CC', '9966FF', '993300', '993333', '993366', '993399', '9933CC', '9933FF', '990000', '990033', '990066', '990099', '9900CC', '9900FF',
			'CC6600', 'CC6633', 'CC6666', 'CC6699', 'CC66CC', 'CC66FF', 'CC3300', 'CC3333', 'CC3366', 'CC3399', 'CC33CC', 'CC33FF', 'CC0000', 'CC0033', 'CC0066', 'CC0099', 'CC00CC', 'CC00FF',
			'FF6600', 'FF6633', 'FF6666', 'FF6699', 'FF66CC', 'FF66FF', 'FF3300', 'FF3333', 'FF3366', 'FF3399', 'FF33CC', 'FF33FF', 'FF0000', 'FF0033', 'FF0066', 'FF0099', 'FF00CC', 'FF00FF',
			'FFFFFF', 'DDDDDD', 'C0C0C0', '969696', '808080', '646464', '4B4B4B', '242424', '000000', 'FFFFBB', 'FFFFDD', 'FFBBFF', 'FFDDFF', 'BBFFFF', 'DDFFFF', 'EEEEDD', 'EEDDEE', 'DDEEEE'               
        ];
        var config = {
            // Configuration parameters (cannot be changed by instantiator)
            closeAction: 'close',
            border: false,
            bodyBorder: false,
            plain: true,
            modal: true,
            width: 350,
            height: 250,
            draggable: true,
            resizable: true,
            autoScroll: false,
            layout: 'fit',

            // Visual components to add to the window
            items: [{
                id: 'custom-buckets-grid',
                xtype: 'editorgrid',
                autoHeight: false,
                bodyBorder: false,
                viewConfig: {forceFit: true},
                enableColumnHide: false,
                enableColumnMove: false,
                enableHdMenu: false,
                enableDragDrop: false,
                sm: new Ext.grid.RowSelectionModel({
                    singleSelect: true,
                    moveEditorOnEnter: false
                }),
                clicksToEdit: 1,
                columns: [ new Ext.grid.RowNumberer({}), {
                    header: 'Low',
                    align: 'right',
                    dataIndex: 'low',
                    renderer: function(value) {
                        if (isNaN(parseFloat(value))) {
                            return '-Infinity';
                        } else {
                            return value;
                        }
                    },
                    editor: new Ext.form.NumberField({selectOnFocus: true})
                },{
                    header: 'High',
                    align: 'right',
                    dataIndex: 'high',
                    renderer: function(value) {
                        if (isNaN(parseFloat(value))) {
                            return 'Infinity';
                        } else {
                            return value;
                        }
                    },
                    editor: new Ext.form.NumberField({selectOnFocus: true})
                },{
                    header: 'Color',
                    align: 'right',
                    dataIndex: 'color',
                    renderer: function(value, metadata) {
                        metadata.attr = 'style="background-color: #' + value + ';"';
                    },
                    editor: new Ext.ux.ColorField({ colors: colorPallet })
                }],
                store: this.bucketStore,
                tbar: [{
                    icon: 'images/add.png',
                    cls: 'x-btn-icon',
                    tooltip: 'Add bucket',
                    handler: function() {
                        var lastIndex = this.bucketStore.getCount() - 1;
                        if (lastIndex >= 0) {
                            var lastRecord = this.bucketStore.getAt(lastIndex);
                            var lowValue = lastRecord.get('high');
                            var highValue = lowValue * 2;
                        } else {
                            var lowValue = 0;
                            var highValue = 'Infinity';
                        }

                        this.addBucket({
                            low: lowValue,
                            high: highValue,
                            color: 'C0C0C0'
                        });
                        Ext.getCmp('custom-buckets-grid').getSelectionModel().selectRow(lastIndex + 1);
                    },
                    scope: this
                },{
                    icon: 'images/remove.png',
                    cls: 'x-btn-icon',
                    tooltip: 'Remove bucket',
                    handler: function() {
                        var grid = Ext.getCmp('custom-buckets-grid');
                        var record = grid.getSelectionModel().getSelected();
                        if (record) {
                            this.bucketStore.remove(record);
                        }
                        grid.getView().refresh();
                    },
                    scope: this
                }, '-' , {
                    icon: 'images/up.png',
                    cls: 'x-btn-icon',
                    tooltip: 'Move bucket up',
                    handler: function() {
                        var grid = Ext.getCmp('custom-buckets-grid');
                        var record = grid.getSelectionModel().getSelected();
                        var index = this.bucketStore.indexOf(record);
                        if (record && index > 0) {
                            this.bucketStore.remove(record);
                            this.bucketStore.insert(index - 1, record);
                            grid.getSelectionModel().selectRow(index - 1);
                        }
                        grid.getView().refresh();
                    },
                    scope: this
                },{
                    icon: 'images/down.png',
                    cls: 'x-btn-icon',
                    tooltip: 'Move bucket down',
                    handler: function() {
                        var grid = Ext.getCmp('custom-buckets-grid');
                        var record = grid.getSelectionModel().getSelected();
                        var index = this.bucketStore.indexOf(record);
                        if (record && index < this.bucketStore.getCount() - 1) {
                            this.bucketStore.remove(record);
                            this.bucketStore.insert(index + 1, record);
                            grid.getSelectionModel().selectRow(index + 1);
                        }
                        grid.getView().refresh();
                    },
                    scope: this
                }, '-', {
                    icon: 'images/auto.png',
                    cls: 'x-btn-text-icon',
                    text: 'Auto-generate...',
                    tooltip: 'Auto-generate bins...',
                    handler: function() {
                        var autoGenerateWin = new Ext.ux.AutoGenerateBucketsWindow({
                        	listeners: {
                        		ok: function(w) {
		                        	var b = getContextIdAsync({
		                        		scope: this,
										profile: 'bin',
		                        		callback: function() {
				                            var bucketCount = w.getBucketCount();
				                            var bucketType = w.getBucketType();
				                            this.bucketTypeDisp = w.getBucketTypeDisp();

				                            // Make ajax call to auto-generate service
				                            Ext.Ajax.request({
				                                url: 'getBins',
				                                method: 'GET',
				                                params: 'context-id=' + Sparrow.SESSION.getUsableContextId() + '&bin-count=' + bucketCount + '&bin-type=' + bucketType,
				                                scope: this,
				                                success: this.handleAutoGenerateSuccess,
				                                failure: this.handleAutoGenerateFailure,
				                                timeout: 40000
				                            });
				                            // timeout upped to 40000 from 10000 to deal with annoying timeout issue
				                            // TODO[IK] add method to inspect object and autoCancel pending request/timeout
				                            Ext.getBody().mask('Generating bins...  <input type="button" onclick="Ext.getBody().unmask();Ext.Ajax.abort();" value="cancel" />', 'x-mask-loading');

				                        }
		                        	});
		                        	if (!b) {
		                        		this.onCancel();
		                        	}
		                        },
                        		scope: this
                        	}
                        });
                        autoGenerateWin.show();
                    },
                    scope: this
                }]
            }],

            // Buttons attached at the window level
            buttons: [{
                text: 'Save',
                handler: this.onSave,
                scope: this
            },{
                text: 'Cancel',
                handler: this.onCancel,
                scope: this
            }]
        };

        // Apply the configuration object
        Ext.apply(this, config);
        Ext.apply(this.initialConfig, config);

        // Call superclass' init
        Ext.ux.CustomBinsWindow.superclass.initComponent.call(this);

        // Install event handlers after everything is attached
        this.addEvents({save: true});
        this.addEvents({cancel: true});
    },

    onRender: function(ct, position) {
        // Call superclass' onRender
        Ext.ux.CustomBinsWindow.superclass.onRender.call(this, ct, position);
    },

    onSave: function() {
        this.fireEvent('save', this);
        this.close();
    },

    onCancel: function() {
        this.fireEvent('cancel', this);
        this.close();
    },

    addBucket: function(low, high, color) {
        var BucketRecord = Ext.data.Record.create([
            {name: 'low'}, {name: 'high'}, {name: 'color'}
        ]);

        this.bucketStore.add(new BucketRecord({
        	'low': low,
        	'high': high,
        	'color': color
        	}));
    },

    getBucketCount: function() {
        return this.bucketStore.getCount();
    },

    getBucketType: function() {
        //return this.bucketTypeDisp;
    	return 'Custom';
    },
    
    /**
     * Returns the user's binning data in the structure used by the Sparrow.SESSION.
     */
    getBucketList: function(customBins) {
    	
      var customBins = [];
      this.bucketStore.each(function(record) {
    	  customBins.push(record.data);
      });
    	
        var displayBins = [];
        var functionalBins = [];
        var boundUnlimited = [];
        var colors = [];
        var nonDetect = [];
        
        for (var i = 0; i < customBins.length; i++) {

        	displayBins.push({
                low: customBins[i]['low'],
                high: customBins[i]['high']
            });
        	
        	functionalBins.push({
                low: customBins[i]['low'],
                high: customBins[i]['high']
            });
        	
        	boundUnlimited.push({
        		low: false,
        		high: false
        	});
        	
        	colors[i] = customBins[i]['color'];
        	
        	nonDetect[i] = false;
        }
        
        var binning = {
        		"displayBins": displayBins,
        		"functionalBins": functionalBins,
        		"binColors": colors,
        		"boundUnlimited": boundUnlimited,
        		"nonDetect": nonDetect
            };
        
        return binning;
	},

    loadBuckets: function(functionalBins, colors) {
        this.bucketStore.removeAll();
        
	    for (var i = 0; i < functionalBins.length; i++) {	
	    	this.addBucket(functionalBins[i]['low'], functionalBins[i]['high'], colors[i]);
	    }
    },

    handleAutoGenerateSuccess: function(response, options) {
        this.bucketStore.removeAll();
        var binValues = Sparrow.ui.parseBinDataResponse(response.responseXML);
        var functionalBins = binValues['functionalBins'];
        var binColors = binValues['binColors'];

	    for (var i = 0; i < functionalBins.length; i++) {	
	    	this.addBucket(functionalBins[i]['low'], functionalBins[i]['high'], binColors[i]);
	    }

        Ext.getBody().unmask();
    },

    handleAutoGenerateFailure: function(response, options) {
    	if (response.isTimeout) {
    		Ext.Msg.alert('Warning', 'autogen bins timed out');
    	} else {

    	}
    }

});




// Register the xtype
Ext.reg('custombinswindow', Ext.ux.CustomBinsWindow);
