/*
 * Editable grid component representing the Treatments tab in the Reach Identify
 * popup window.  This component extends the EditorGridPanel and provides
 * functionality and convenience methods for retrieving adjustments made by the
 * user for a particular reach.
 */
AdjustmentsGrid = Ext.extend(Ext.grid.EditorGridPanel, {
    store: null,
    viewConfig: {autoFill: true, forceFit: true},
    autoScroll: false,
    enableColumnHide: false,
    enableColumnMove: false,
    enableHdMenu: false,
    clicksToEdit: 1,
    listeners: {
        afteredit: function(e) {
            e.grid.modified = true;
        },
        scope: this
    },

    // Custom properties for this extension
    reachResponse: null,
    modified: false,

    // Initialization of the grid - set up the data store and columns
    initComponent: function() {

        // The data store for our data model - sources and their adjustments
        this.store = new Ext.data.SimpleStore({
            fields: ['source', 'units', 'originalValue', 'overrideValue', 'currentValue'],
            data: []
        });
        this.loadStore();

        // Define the column model and apply it to the grid
        Ext.apply(this, {
            columns: [
                {header: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.For Source\')">Source</a>', dataIndex: 'source', tooltip:  "Name of the model source input."},
                {header: 'Units', dataIndex: 'units', width: 150, renderer: this.prettyUnits, tooltip: "The units that the source is reported in." },
                {header: 'Original Amount', dataIndex: 'originalValue', type: 'float', align: 'right', renderer: this.commaRenderer, tooltip: "The original source input value of the calibrated model."},
                // Note, type of Override should really be an AdjustmentDisplay type, but seems
                // like too much machinery must be written for that, so doing this as a simple string
                {id: 'overrideValue', header: 'Override Amount', dataIndex: 'overrideValue', type: 'String', align: 'right', editor: new Ext.form.TextField({selectOnFocus: true}), renderer: this.coloredRenderer, tooltip: "An editable new value that will replace the Original Amount." },
                {header: 'Adjusted Amount', dataIndex: 'currentValue', type: 'float', align: 'right', renderer: this.adjustedAmountRenderer, tooltip: "The resulting source value used for model predictions. This value will reflect group percentage adjustments or the Override Amount."}
            ]
        });

        // Call the superclass' init function
        AdjustmentsGrid.superclass.initComponent.apply(this, arguments);
    },
    
    prettyUnits : function(val) {
    	return Sparrow.USGS.prettyPrintUnitsForHtml(val);
    },
    
    commaRenderer: function(value, metaData, record, rowIndex, colIndex, store) {
    	return Ext.util.Format.number(value, '0,000.00');
    },
    
    adjustedAmountRenderer: function(value, metaData, record, rowIndex, colIndex, store) {
    	if(record.data.originalValue==record.data.currentValue) return '';
    	return Ext.util.Format.number(value, '0,000.00');
    },
    
    coloredRenderer: function(value, metaData, record, rowIndex, colIndex, store) {
    	metaData.css = "light-blue";
    	return value;
    },

    onRender: function() {
        // Call the superclass' onRender function
        AdjustmentsGrid.superclass.onRender.apply(this, arguments);
    },

    /*
     * Customized loader for the values on the Treatments tab
     */
    loadStore: function() {
        this.store.removeAll();

        // Set up a record definition for pushing into the store
        var SourceRecord = Ext.data.Record.create([
            {name: 'source'},{name: 'units'},{name: 'originalValue'},{name: 'overrideValue'},{name: 'currentValue'}
        ]);

        // Get the row data from the reach response and iterate over them
        var sourceData = [];
        if (this.reachResponse["sparrow-id-response"].results.result[0].adjustments) {
        	sourceData = this.reachResponse["sparrow-id-response"].results.result[0].adjustments.data.r;
        }

        // Get adjustment data from the Adjustments tab and append if necessary.
        var myId = this.reachResponse["sparrow-id-response"].results.result[0].identification.id;
        var myReach = Sparrow.SESSION.getAdjustedReach(myId);
        var myAdjustments = (myReach)? myReach.adjustment : null;

        for (var i = 0; i < sourceData.length; i++) {

            // Check for a null override value and replace with empty string
            // This is done to ensure that the grid doesn't think an empty
            // string is a new value when we're checking for changes
            var overrideValue = sourceData[i]["c"][5];
            overrideValue = (overrideValue == null) ? "" : overrideValue;

            var userOverrideValue = null;
            if (myAdjustments){ // get corresponding user adjust value
            	for (var j=0; j<myAdjustments.length; j++){
            		var adjustment = myAdjustments[j];
            		if ( adjustment["@src"] == (i + 1)) userOverrideValue = adjustment["@abs"];
            	}
            }

            // Build a record (row) for the grid and add to the store
            var record = new SourceRecord({
                source: sourceData[i]["c"][0],
                units: sourceData[i]["c"][2],
                originalValue: sourceData[i]["c"][4],
                overrideValue: ADJUSTMENT_DISPLAY.display(overrideValue, userOverrideValue),
                currentValue: sourceData[i]["c"][6]
            });
            this.store.add(record);
        }
    },

    /*
     * Returns whether or not the grid has been modified
     */
    isModified: function() {
        return this.modified;
    },

    /*
     * Sets the modified flag
     */
    setModified: function(modified) {
        this.modified = modified;
    },

    /*
     * Refreshes the grid using the specified reach identify response
     */
    refresh: function(reachResponse) {
        this.reachResponse = reachResponse;
        this.loadStore();
        this.modified = false;
    },

    /**
     * Returns an array consisting of the values within the override column. The
     * index of each of the values should correspond to (SourceIdentifier - 1)
     * where SourceIdentifier is the unique id given to a particular source.
     * TODO: The reliance on this technique to identify sources seems fragile.
     */
    getOverrides: function() {
        // Iterate over the overrideValue column and construct an array to return
        var overrideValues = new Array();
        for (var i = 0; i < this.store.getCount(); i++) {
            var override = ADJUSTMENT_DISPLAY.parseOverrideValue(this.store.getAt(i).get('overrideValue'));
            overrideValues.push(override);
        }

        return overrideValues;
    }
});

var ADJUSTMENT_DISPLAY = {

	/**
	 * Display the map override value and user override value as a single string.
	 *   CASE 1: If both agree, displays a single value
	 *   CASE 2: If map override exists, then user override must be shown as well
	 *   Shown user override values appear within parenthesis. If a null user
	 *   override is shown, then it appears as "none"
	 */
	display: function(mapValue, userOverrideValue){
		var mvDisp = (mapValue == null)? "": Ext.util.Format.number(mapValue, '0,000.00');
		var uvDisp = (userOverrideValue == null)? "": "(" + Ext.util.Format.number(userOverrideValue, '0,000.00') + ")";
		if (mvDisp && !uvDisp){
			// map contains an override which will be/has been cleared by user
			uvDisp = "(none)";
		}
		// Don't display userOverrideValue if the same
		return (parseFloat(mapValue) == parseFloat(userOverrideValue))? mvDisp: mvDisp + uvDisp;
	},

	/**
	 * Parse the user override value from the display value (which might also
	 * be a user-typed value), returning null if none exists.
	 */
	parseOverrideValue: function(displayValue){
		if (!displayValue) return null;
		displayValue = displayValue.replace(/,/g, '');
		var hasUserOverride = displayValue.indexOf('(') > -1;
		if ( hasUserOverride ){
			if (displayValue == "(none)") return null;
			// userOverride supersedes map override
			return displayValue.substring(displayValue.indexOf('(') + 1, displayValue.length - 1);
		}
		return displayValue;
	}
}



Ext.reg('adjustmentsGrid', AdjustmentsGrid);
