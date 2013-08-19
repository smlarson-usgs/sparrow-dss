TargetGroupSummary = Ext.extend(Ext.Window, {
	title: 'Target Group Summary',
	id: 'ext-target-grp-sum-window',
	layout: 'fit',
	width: 600,
	height: 300,
	
	initComponent: function() {
		
		//make call to ID Service
	    var xmlreq = ''
	        + '<sparrow-id-request xmlns="http://www.usgs.gov/sparrow/id-point-request/v0_2" '
	        + 'xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">';
	        if (context_id) xmlreq += '<context-id>' + context_id + '</context-id>';
	        else xmlreq += '<model-id>' + model_id + '</model-id>';
	

	    var targetedReaches = Sparrow.SESSION.getAllTargetedReaches();
	    var reachIds = '';
	    for (var i = 0; i < targetedReaches.length; i++) {
	    	reachIds += '<reach id="' + targetedReaches[i]["@id"] + '"/>';
	    }
	    
	    // Request adjustments and attributes from the service
	    xmlreq += reachIds 
	        + '<content>'
	        + '<adjustments /><attributes />'
	        + '</content>'
	        + '<response-format><mime-type>json</mime-type></response-format>'
	        + '</sparrow-id-request>'
	        ;
	    
	    var idRequest = Ext.Ajax.request({
	    	method: 'POST',
	    	url: 'getIdentify',
	    	success: this.ajaxRenderTabs,
	    	failure: function(r,o) {

	    	},
	    	params: {
	    		xmlreq: xmlreq,
	    		mimetype: 'json'
	    	},
	    	scope: this
	    });
	
	    
	    
	    //summary stores
	    this.reachSummaryStore = new Ext.data.ArrayStore({
	        // store configs
	        autoDestroy: true,
	        storeId: 'reachStore',
	        // reader configs
	        idIndex: 1,  
	        fields: [
  	           'Name',
	           'Id',
	           {name: 'Original', type: 'float'},
	           {name: 'Treated', type: 'float'},
	           {name: 'pctChange', type: 'float'},
	        ]
	    });
	    
	    
	    this.sourceSummaryStore = new Ext.data.ArrayStore({
	        // store configs
	        autoDestroy: true,
	        storeId: 'sourceStore',
	        // reader configs
	        idIndex: 0,  
	        fields: [
	           'Source',
	           'Metric',
	           'Units',
	           {name: 'Original', type: 'float'},
	           {name: 'Treated', type: 'float'},
	           {name: 'pctChange', type: 'float'},
	        ]
	    });
	    
	    
	    
	    
		Ext.apply(this, {
			buttons: [{
				text: 'Close',
				handler: function() {
					Ext.getCmp('ext-target-grp-sum-window').close();
				}
			}],
			items: [{
				xtype: 'tabpanel',
				border: false,
				activeTab: 0,
				defaults: {autoscroll: true},
				items: [{
					title: 'Target Reach Summary',
					xtype: 'grid',
					store: this.reachSummaryStore,
					border: false,
					autoScroll: true,
					viewConfig: {forceFit: true},
				    sm: null,
				    colModel: new Ext.grid.ColumnModel([
		                { header: "Name", width: 150, sortable: true, dataIndex: 'Name'},
		                { header: "Id", width: 150, sortable: true, dataIndex: 'Id'},
		                { header: "Original", width: 100, sortable: true, dataIndex: 'Original'},
		                { header: "Treated", width: 100, sortable: true, dataIndex: 'Treated'},
		                { header: "% Changed", width: 100, sortable: true, dataIndex: 'pctChange'}
		            ])
				},{
					title: 'Target Reach Summary By Source',
					xtype: 'grid',
					store: this.sourceSummaryStore,
					border: false,
					autoScroll: true,
					viewConfig: {forceFit: true},
				    sm: null,
				    colModel: new Ext.grid.ColumnModel([
		                { header: "Source", width: 200, sortable: true, dataIndex: 'Source'},
		                { header: "Metric", width: 140, sortable: true, dataIndex: 'Metric'},
		                { header: "Units", width: 100, sortable: true, dataIndex: 'Units'},
		                { header: "Original", width: 100, sortable: true, dataIndex: 'Original'},
		                { header: "Treated", width: 100, sortable: true, dataIndex: 'Treated'},
		                { header: "% Chg", width: 60, sortable: true, dataIndex: 'pctChange'}
		            ])
				},{
					title: 'Graph',
					html: ''
				}]
			}]
		});
	
		TargetGroupSummary.superclass.initComponent.apply(this, arguments);
	},
	
	ajaxRenderTabs: function(response, options) {
		var idJson = Ext.util.JSON.decode(response.responseText);
		
		//console.log(idJson);
		
		//build up stores
		var results = idJson['sparrow-id-response'].results.result;
		var reachSummary = [];
		var srcSummary = [];
		for (var i = 0; i < results.length; i++) {
			var srcTotalOriginal = 0;
			var srcTotalTreated = 0;
			var srcs = results[i].adjustments.data.r;
			for (var j = 0; j < srcs.length; j++) {
				var srcOriginal = parseFloat(srcs[j].c[4]);
				var srcTreated = parseFloat(srcs[j].c[6]);
				srcTotalOriginal += srcOriginal;
				srcTotalTreated += srcTreated;
				if (!srcSummary[j]) {
					srcSummary.push([srcs[j].c[0], srcs[j].c[1], srcs[j].c[2], srcOriginal, srcTreated, 1-(srcOriginal/srcTreated)]);
				} else {
					srcSummary[j][3] += srcOriginal;
					srcSummary[j][4] += srcTreated;
					srcSummary[j][5] = (srcSummary[j][3]/srcSummary[j][4])||0;
				}
			}
			//target reach summary
			reachSummary.push([results[i].identification.name, results[i].identification.id, srcTotalOriginal, srcTotalTreated, 1-(srcTotalOriginal/srcTotalTreated)]);
		}
		
		//console.log(reachSummary);
		//console.log(srcSummary);
		
		this.reachSummaryStore.loadData(reachSummary);
		this.sourceSummaryStore.loadData(srcSummary);
	}
	
});
