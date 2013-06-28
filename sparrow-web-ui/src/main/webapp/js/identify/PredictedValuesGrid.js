/*
 * Grid component representing the Predicted Values tab in the Reach Identify
 * popup window.  This grid displays predicted values for both the original data
 * and the data after treatments have been applied.  The calculation takes place
 * when the tab is activated, allowing it to display calculations based on the
 * most recent adjustments.
 */
PredictedValuesGrid = Ext.extend(Ext.grid.GridPanel, {
    // Default editor grid properties
    title: 'Predicted Values (<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Data Series\')">Data Series</a>)',
    store: null,
    viewConfig: {autoFill: true, forceFit: true},
    autoScroll: true,
    enableColumnHide: false,
    enableColumnMove: false,
    enableHdMenu: false,
    hideHeaders: false,
    trackMouseOver: true,
    disableSelection: true,
    region: 'center',
    
    // Custom properties for this extension
    reachId: null,
    
    // Initialization of the grid - set up the data store and columns
    initComponent: function() {
        
        // The data store for our data model - sources and their predicted values
        var predictedReader = new Ext.data.ArrayReader({}, [
            {name: 'section'},
            {name: 'source'},
            {name: 'originalValue'},
            {name: 'treatedValue'},
            {name: 'percentChange'}
        ]);
        // Use a grouping store to group the rows by section
        this.store = new Ext.data.GroupingStore({
            reader: predictedReader,
            data: [],
            sortInfo: {field: 'source', direction: 'ASC'},
            groupDir: 'DESC',
            groupField: 'section'
        });
    
        // Define the column model and apply it to the grid
        var modelConstituent = Sparrow.SESSION.getModelConstituent();
        var modelUnits = Sparrow.USGS.prettyPrintUnitsForHtml(Sparrow.SESSION.getModelUnits());
        var displayUnits = modelConstituent + " " + modelUnits;
        if(Sparrow.SESSION.hasEnabledAdjustments()) {
	        Ext.apply(this, {
	            columns: [
	                {header: 'Section', dataIndex: 'section', hidden: true},
	                {
	                    header: 'Source',
	                    dataIndex: 'source',
	                    renderer: function(value, metadata) {
	                        metadata.attr = 'title="' + value + '"';
	                        //return Ext.util.Format.ellipsis(value, 24);
	                        return value;
	                    }
	                },
	                {header: 'Original (' + displayUnits + ')', dataIndex: 'originalValue', type: 'float', align: 'right', renderer: this.commaRenderer},
	                {header: '% of Load (Orig.)', dataIndex: 'percOrig', width: 65, type: 'float', align: 'right', renderer: function(value, metaData, record, rowIndex, colIndex, store) {return this.percentTotalRenderer('originalValue', record, store)}, scope: this},
	 	            {header: 'Adjusted (' + displayUnits + ')', dataIndex: 'treatedValue', type: 'float', align: 'right', renderer: this.commaRenderer},
	 	            {header: '% of Load(Adj.)', dataIndex: 'percAdj', width: 65, type: 'float', align: 'right', renderer: function(value, metaData, record, rowIndex, colIndex, store) {return this.percentTotalRenderer('treatedValue', record, store)}, scope: this},
	 	            {header: '% Change', width: 50, dataIndex: 'percentChange', type: 'float', align: 'right', renderer: this.commaRenderer}
		        ],
	            view: new Ext.grid.GroupingView({
	                forceFit: true,
	                groupTextTpl: '{group}'
	            })
	        });
        }
        else {
	        Ext.apply(this, {
	            columns: [
	                {header: 'Section', dataIndex: 'section', hidden: true},
	                {
	                    header: 'Source',
	                    dataIndex: 'source',
	                    renderer: function(value, metadata) {
	                        metadata.attr = 'title="' + value + '"';
	                        //return Ext.util.Format.ellipsis(value, 24);
	                        return value;
	                    }
	                },
	                {header: 'Predicted (' + displayUnits + ')', dataIndex: 'originalValue', type: 'float', align: 'right', renderer: this.commaRenderer},
	                {header: '% of Load', dataIndex: 'percOrig', type: 'float', align: 'right', renderer: function(value, metaData, record, rowIndex, colIndex, store) {return this.percentTotalRenderer('originalValue', record, store)}, scope: this}
	 	        ],
	            view: new Ext.grid.GroupingView({
	                forceFit: true,
	                groupTextTpl: '{group}'
	            })
	        });
        }

        // Call the superclass' init function
        PredictedValuesGrid.superclass.initComponent.apply(this, arguments);
    },
    
    commaRenderer: function(value, metaData, record, rowIndex, colIndex, store) {
    	return Ext.util.Format.number(value, '0,000');
    },
    
    percentTotalRenderer: function(valName, record, store) {
    	var group = record.data.section;
    	var value = record.data[valName];
    	var total = 1;
    	store.each(function(r){
    		if(r.data.section == group && r.data.source == group)
    			total = r.data[valName];
    	});
    	var perc = value / total * 100;
    	return Ext.util.Format.number(perc, '0,000.0');
    },
    
    onRender: function() {
        // Call the superclass' onRender function
        PredictedValuesGrid.superclass.onRender.apply(this, arguments);
    },
    
    // Customized loader for the values on the Predicted Values tab
    loadStore: function(reachResponse) {
        this.store.removeAll();
        
        // Set up a record defintion for pushing into the store
        var SourceRecord = Ext.data.Record.create([
            {name: 'section'}, // incremental or total
            {name: 'source'},
            {name: 'originalValue'},
            {name: 'treatedValue'},
            {name: 'percentChange'}
        ]);

        // Parse the reach response and generate records from the rows
        var sections = reachResponse["sparrow-id-response"].results.result[0].predicted.data.section;
        for (var i = sections.length-1; i >= 0; i--) {
            var sectionTitle = sections[i]["@display"];
            
            var sourceData = sections[i]["r"];
            for (var j = 0; j < sourceData.length; j++) {
                
                // Build a record (row) for the grid and add to the store
                var record = new SourceRecord({
                    section: sectionTitle,
                    source: sourceData[j]["c"][0],
                    originalValue: sourceData[j]["c"][4],
                    treatedValue: sourceData[j]["c"][5],
                    percentChange: sourceData[j]["c"][6]
                });
                this.store.add(record);
            }
        }
    },
    
    //
    refresh: function(reachResponse) {
        this.loadStore(reachResponse);
    }
});

Ext.reg('predictedValuesGrid', PredictedValuesGrid);
