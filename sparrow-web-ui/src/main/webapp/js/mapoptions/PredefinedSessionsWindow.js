PredefinedSessionsWindow = Ext.extend(Ext.Window, {
    title: 'Load a Predefined Session...',
    layout: 'border',
    closeAction: 'close',
    plain: true,
    modal: true,
    width: 400,
    height: 300,
    draggable: true,
    autoScroll: false,
    bodyBorder: false,
    resizable: false,
    
    initComponent: function() {
	
		this.sessionsStore = new Ext.data.Store({
			autoLoad: true,
			proxy: new Ext.data.HttpProxy({
				url: 'listPredefSessions',
				method: 'GET',
				baseParams: {
					 modelId: Sparrow.SESSION.PredictionContext["@model-id"]
				}
			}),
			reader: new Ext.data.XmlReader({
				root: 'entityList',
				record: 'entity'
			}, ['name', 'uniqueCode', 'contextString']),
			baseParams: {
				modelId: Sparrow.SESSION.PredictionContext["@model-id"]
			}
		});
	
		var _this = this;
	
		Ext.apply(this, {
			items: [{
				id: 'predef-sessions-grid',
				region: 'center',
				xtype: 'grid',
				layout: 'fit',
				border: false,
				store: this.sessionsStore,
				stripeRows: true,
				loadMask: true,
				cm: new Ext.grid.ColumnModel([{header: 'Session Name', dataIndex: 'name', width: 200}]),
			    sm: new Ext.grid.RowSelectionModel({singleSelect:true}),
			    viewConfig: {forceFit: true}
			}],
			buttons:[{
				text: 'OK',
				handler: function() {
					var sessionsGrid = Ext.getCmp('predef-sessions-grid');
					var sessionRecord = sessionsGrid.getSelectionModel().getSelected();
					if (!sessionRecord) {
						Ext.Msg.alert('Warning', 'Please select a model to load.');
					}
					_this.close();
					Sparrow.ui.loadPredefinedSessionFromJSON(sessionRecord.data.contextString);
				}
			},{
				text: 'Cancel',
				handler: function() {
					_this.close();
				}
			}]
		});
		
		PredefinedSessionsWindow.superclass.initComponent.apply(this, arguments);
	}
});