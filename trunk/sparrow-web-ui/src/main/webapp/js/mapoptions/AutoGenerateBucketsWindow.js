/**
 * @class Ext.ux.AutoGenerateBucketsWindow
 * @extends Ext.Window
 * 
 * Popup window used to select parameters for auto-generation of custom buckets.
 * This dialog is responsible only for presenting the options for auto-generation.
 * It implements no functionality related to how the selections are processed.
 * 
 * @constructor
 * @param {Object} config The config object.
 */
Ext.ux.AutoGenerateBucketsWindow = Ext.extend(Ext.Window, {
    // Default configuration parameters (may be changed by instantiator)
    title: 'Auto-generate bins',
    bucketType: 'EQUAL_COUNT',
    bucketTypeDisp: 'Equal Count',
    bucketCount: 5,
    
    initComponent: function() {
        var config = {
            // Configuration parameters (cannot be changed by instantiator)
            closeAction: 'close',
            bodyBorder: false,
            plain: true,
            modal: true,
            layout: 'form',
            width: 200,
            height: 125,
            labelWidth: 50,
            bodyStyle: 'padding: 5px',
            buttonAlign: 'center',
            draggable: true,
            resizable: false,
            autoScroll: false,
            
            items: [{
                xtype: 'combo',
                fieldLabel:'Type',
                width: 100,
                displayField: 'display',
                valueField: 'value',
                forceSelection: true,
                mode: 'local',
                triggerAction: 'all',
                store: new Ext.data.SimpleStore({
                    fields: ['value', 'display'],
                    data:[['EQUAL_COUNT','Equal Count'],['EQUAL_RANGE','Equal Range']]
                }),
                value: 'Equal Count',
                listeners: {
                    'select': {
                        fn: function(combo, record, index) {
                            this.bucketType = record.get('value');
                            this.bucketTypeDisp = record.get('display');
                        },
                        scope: this
                    }
                }
            },{
                xtype: 'numberfield',
                fieldLabel:'Count',
                width: 30,
                allowBlank: false,
                allowDecimals: false,
                allowNegative: false,
                selectOnFocus: true,
                value: 5,
                minValue: 1,
                listeners: {
                    'change': {
                        fn: function(field, newValue, oldValue) {
                            this.bucketCount = newValue;
                        },
                        scope: this
                    }
                }
            }],
        
            buttons: [{
                text: 'OK',
                handler: this.onOk,
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
        Ext.ux.AutoGenerateBucketsWindow.superclass.initComponent.call(this);
        
        // Install event handlers after everything is attached
        this.addEvents({ok: true});
        this.addEvents({cancel: true});
    },
    
    onRender: function(ct, position) {
        // Call superclass' onRender
        Ext.ux.AutoGenerateBucketsWindow.superclass.onRender.call(this, ct, position);
    },
    
    onOk: function() {
        this.fireEvent('ok', this);
        this.close();
    },
    
    onCancel: function() {
        this.close();
    },
    
    getBucketType: function() {
        return this.bucketType;
    },
    
    getBucketTypeDisp: function() {
        return this.bucketTypeDisp;
    },
    
    getBucketCount: function() {
        return this.bucketCount;
    }
});

Ext.reg('autogeneratebucketswindow', Ext.ux.AutoGenerateBucketsWindow);
