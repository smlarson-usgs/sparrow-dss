/*
 * Grid component representing the Attributes tab in the Reach Identify popup
 * window.  This grid displays attribute values specific to the reach being
 * identified.
 */
var AttributesGrid = (function(){
	var attributeRecords = [
            {name: 'section'},
            {name: 'name'},
            {name: 'value'},
			{name: 'docId'}
    ];

	return Ext.extend(Ext.grid.GridPanel, {
    // Default grid properties
    title: 'Reach/Catchment Info',
    store: null,
    viewConfig: {autoFill: true, forceFit: true},
    autoScroll: false,
    enableColumnHide: false,
    enableColumnMove: false,
    enableHdMenu: false,
    hideHeaders: true,
    trackMouseOver: false,
    disableSelection: true,
    region: 'center',

    // Custom properties for this extension
    reachResponse: null,

    // Initialization of the grid - set up the data store and columns
    initComponent: function() {

        // The data store for our data model - attribute name and value pairs
        var attributeReader = new Ext.data.ArrayReader({}, attributeRecords);
        // Use a grouping store to group the rows by section
        this.store = new Ext.data.GroupingStore({
            reader: attributeReader,
            data: [],
            sortInfo: {field: 'name', direction: 'ASC'},
            groupField: 'section'
        });
        this.loadStore();

        // Define the column model and apply it to the grid
        Ext.apply(this, {
            columns: [
                {header: 'Section', dataIndex: 'section', hidden: true},
				{header: 'Help', dataIndex: 'docId', renderer: this.renderHelpIfAvailable, width: 25, fixed: true},
                {header: 'Name', dataIndex: 'name'},
                {header: 'Value', dataIndex: 'value', renderer: this.renderNumbersWithColumns}


            ],
            view: new Ext.grid.GroupingView({
                forceFit: true,
                startCollapsed: true,
                groupTextTpl: '{group}'
            })
        });

        this.on('viewready', function(grid) {
        	grid.view.toggleGroup(grid.view.getGroupId('Basic Attributes'), true);
        });

        // Call the superclass' init function
        AttributesGrid.superclass.initComponent.apply(this, arguments);
    },

    renderHelpIfAvailable: function(value, metaData, record, rowIndex, colIndex, store) {
		return value ?
		'<a onclick="getGeneralHelp(\''+value+'\')"href="#"><img src="images/small_info_icon.png" alt="(?)"/></a>'
		:
		'';
	},
    renderNumbersWithColumns : function(value, metaData, record, rowIndex, colIndex, store) {
    	var fieldsToFormat = {
    		"reach length": true,
    		"mean flow":true,
    		"mean velocity":true,
    		"incremental area":true,
    		"cumulative drainage area":true
    	};
    	var formattedVal = value;
    	if(fieldsToFormat[record.data.name.toLowerCase()]) {
    		var parts = value.split(' ');
    		formattedVal = Ext.util.Format.number(parts[0], '0,000.00');
    		if(parts.length>1) formattedVal += ' ' + parts[1];
    	}
    	return Sparrow.USGS.prettyPrintUnitsForHtml(formattedVal);
    },

    onRender: function() {
    	// Call the superclass' onRender function
        AttributesGrid.superclass.onRender.apply(this, arguments);
    },

    /*
     * Customized loader for the values on the Attributes tab
     */
    loadStore: function() {
        this.store.removeAll();

        // Set up a record definition for pushing into the store
        var AttributeRecord = Ext.data.Record.create(attributeRecords);
		var mappedval = this.reachResponse["sparrow-id-response"].results.result[0]['mapped-value'];
		var mappedValHtml = "";
		
		if (! Sparrow.SESSION.isMapping()) {
			mappedValHtml = 'Current Mapped Value: <i>[There is no current map]</i><br/>' +
			'<img src="images/small_info_icon.png" alt="Careful!"/><span class="note">All values displayed in this window are based on the unadjusted Total Load data series.</span>';
		} else if (Sparrow.SESSION.isContextChangedSinceLastMap()) {
			mappedValHtml = '<img class="warn" src="images/small_alert_icon.png" alt="Careful!"/>Current Mapped Value: <i>' + Math.round(mappedval.value*1000)/1000 + ' ' + Sparrow.USGS.prettyPrintUnitsForHtml(mappedval.units) +
    		' of ' + mappedval.constituent + ' (' + mappedval.name + ')</i><br/>' +
			'<span class="note">This mapped value and the values displayed in this window are not up to date with your mapping selections.  Click the Update Map button to update.</span>';
		} else {
			mappedValHtml = 'Current Mapped Value: ' + Math.round(mappedval.value*1000)/1000 + ' ' + Sparrow.USGS.prettyPrintUnitsForHtml(mappedval.units) +
    		' of ' + mappedval.constituent + ' (' + mappedval.name + ')';
		}
		
        document.getElementById('sparrow-identify-mapped-value-a').innerHTML = mappedValHtml;
        document.getElementById('sparrow-identify-mapped-value-b').innerHTML = mappedValHtml;
        var sectionList = this.reachResponse["sparrow-id-response"].results.result[0].attributes.data.section;
        for (var i = 0; i < sectionList.length; i++) {
            var sectionTitle = sectionList[i]["@display"];

            var rowList = sectionList[i]["r"];
            for (var j = 0; j < rowList.length; j++) {
				var docId = rowList[j]["c"][3];
                var units = rowList[j]["c"][2] ? ' ' + rowList[j]["c"][2] : '';
                var value = rowList[j]["c"][1] + units;
				var name = rowList[j]["c"][0];

                //exclude some values from the list
                if("incremental delivery fraction" === name.toLowerCase()) continue;

                // Build a record (row) for the grid and add to the store
                var record = new AttributeRecord({
                    section: sectionTitle,
                    name: name,
                    value: value,
					docId: docId
                });
                this.store.add(record);
            }
        }
    },

    /*
     * Refreshes the attribute list using the specified reach identify response
     */
    refresh: function(reachResponse) {
        this.reachResponse = reachResponse;
        this.loadStore();
    }
});}());

Ext.reg('attributesGrid', AttributesGrid);
