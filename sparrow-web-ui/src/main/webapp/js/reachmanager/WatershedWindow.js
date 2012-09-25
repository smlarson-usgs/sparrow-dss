/**
 * Window to display find reach results
 */

var WATERSHED_WINDOW = new (function() {
	var goToWatershedWin = null;
	
	function getWatershedStore() {
		return new Ext.data.Store({
			// load using HTTP
			url : 'sp_watershed',
			remoteSort : true,
			method: 'GET',
			baseParams:{'mime-type':'xml'},
			// the return will be XML, so lets set up a reader
			reader : new Ext.data.XmlReader({
				totalProperty : "metadata[rowCount]",
				record : 'r',
				id : '@id'
			}, [
			// set up the fields mapping into the xml doc
			{
				name : 'name',
				mapping : 'c:nth(1)'
			},{
				name : 'description',
				mapping : 'c:nth(2)'
			},{
				name : 'count',
				type : 'integer',
				mapping : 'c:nth(3)' 
			} ])
		});
	};

	var findWatershedStore = getWatershedStore();

	var goToWatershedFormContent = new Ext.Panel({
		region: 'north',
		bodyStyle: 'padding: 5px',
		autoScroll: true,
		title: "Choose one or more watersheds to add to your Active Downstream Reaches and click the 'Add' button at the bottom. " +
		' Not all watersheds are represented in this list.'
	});
	var gridSelectionModel =  new Ext.grid.CheckboxSelectionModel({
		header: '',
		checkOnly: true
	});
	
	var findWatershedGrid = new Ext.grid.GridPanel({
		region : 'center',
		id: 'findWatershedGrid',
		store : findWatershedStore,
		selModel: gridSelectionModel,
		viewConfig : {
			forceFit : true,
		},
		columns : [
		   gridSelectionModel,
		{
			header : "Watershed Name",
			sortable : false,
			width : 40,
			hideable : false,
			dataIndex : "name"
		}, {
			header : "Watershed Description",
			width : 55,
			dataIndex : 'description',
			hidden : true,
			hideable: true,
			sortable : false,
		}, {
			header : "# of Reaches",
			width : 80,
			fixed : true,
			dataIndex : 'count',
			sortable : false,
			hideable : true
		} ],
		stripeRows : true,
		autoExpandColumn : 1
	});

	this.open = function() {

		if (!goToWatershedWin) {
			goToWatershedWin = new Ext.Window({
				title : 'Select Downstream Reaches',
				closeAction : 'hide',
				border : false,
				layout : 'border',
				plain : true,
				// modal: true,
				width : 400,
				height : 545,
				draggable : true,
				resizable : true,
				buttonAlign : 'right',
				items : [ goToWatershedFormContent, findWatershedGrid ],

				listeners : { 
					beforehide : function(str) {
						var grid = Ext.getCmp('findWatershedGrid');
						var sm = grid.getSelectionModel();
						if (sm && sm.getCount()> 0) {
							sm.clearSelections(false);
						}
					}
				},
				buttons:[{
							text : 'Close',
							handler : function() {
								goToWatershedWin.hide();
							}
						},{
							text : 'Add Selections to Active Downstream Reaches',
							handler : function() {
								var grid = Ext.getCmp('findWatershedGrid');
								var sm = grid.getSelectionModel();
								if (sm.getCount() < 1) {
									Ext.Msg.alert("","Please pick a watershed.");
								} else {
									Ext.getBody().mask('Adding watershed... ','x-mask-loading');
									var reachInfo = {
											totalReaches : 0,
											reachAddedCnt : 0
									};
									sm.each(function(watershedRecord){
										var reachStore = getWatershedStore();
										reachStore.on('load', function(){
											
											var reachesToAdd = [];
											
											reachStore.each(function(reachRecord){
												//Sparrow.SESSION.addToTargetReaches(reachRecord.id, reachRecord.get('name'));
												reachesToAdd.push({ 'reachId': reachRecord.id, 'reachName': reachRecord.get('name') });
												
											});
									
											var reachedAddedCnt = Sparrow.SESSION.addAllToTargetReaches(reachesToAdd);
											reachInfo.totalReaches = reachInfo.totalReaches + reachesToAdd.length;
											reachInfo.reachAddedCnt = reachInfo.reachAddedCnt + reachedAddedCnt;
											if (reachInfo.reachAddedCnt == 0) {
												//Couldn't add them - duplicates
												Ext.Msg.alert("","All the reaches in the selected watershed are already in the 'Selected Downstream Reaches' list.  No reaches were added.");
											} else if (reachInfo.reachAddedCnt < reachInfo.totalReaches) {
												Ext.Msg.alert("","The selected watersheds contain " + 
													reachInfo.totalReaches + " reaches, however, only " + 
													reachInfo.reachAddedCnt + " were added because some were already in the 'Selected Downstream Reaches' list.");
											} else {
												Ext.Msg.alert("","Added " + 
													reachInfo.reachAddedCnt + " reache(s) to the 'Selected Downstream Reaches' list.");
											}																			
										});	
										reachStore.load({
											params:{
												'watershed-id': watershedRecord.id
											}
										});
									}
									);	
									setTimeout('Ext.getBody().unmask()', 500);
								};
							}
						}]
					});
		}
		goToWatershedWin.show();
		var modelID= Sparrow.SESSION.getPredictionContext()["@model-id"];
		findWatershedStore.load({
			params:{
				'model-id': modelID
			}
		});

		var pos = goToWatershedWin.getPosition();
		if (pos[1] < 0)
			goToWatershedWin.setPosition(pos[0], 0);
	};

	this.appendToTitle = function(message) {
		goToWatershedWin.setTitle(goToWatershedWin.baseTitle + message);
	};

})();
