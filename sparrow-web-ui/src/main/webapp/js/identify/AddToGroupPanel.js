Sparrow.ui.AddToGroupPanel = Ext.extend(Ext.Panel, {
//	title: 'Add to Group',
	layout: 'form',
	plain: true,
	// Custom properties for this extension
	reachResponse: null,
	allGroupStore: null,
	whatToAdd: 'reach',

	bodyStyle: "background-color: transparent",
	upstream: {},
	huc8: {},
	huc6: {},
	huc4: {},
	huc2: {},
	reach: {},

	constructor: function(config) {
		this.reachResponse = config.reachResponse;

		// Set up the data store for the groups dropdown
		this.allGroupStore = new Ext.data.SimpleStore({
			fields: ['group'],
			data: []
		});
		this.loadAllGroupStore();
		var _this = this;
		var myGroupHandler = function(){ _this.loadAllGroupStore(); }
		Sparrow.CONTEXT.on("adjustment-group-changed", myGroupHandler);
		
		// Populate HUC names and ids
		var reachIdent = this.reachResponse["sparrow-id-response"].results.result[0].identification;
		this.reach.name = reachIdent.name;
		this.reach.id = reachIdent.id;
		this.upstream.name = 'Upstream of rch ' + this.reach.id;
		this.upstream.id = this.reach.id;
		this.huc8.name = reachIdent.hucs.huc8["@name"];
		this.huc8.id = reachIdent.hucs.huc8["@id"];
		this.huc6.name = reachIdent.hucs.huc6["@name"];
		this.huc6.id = reachIdent.hucs.huc6["@id"];
		this.huc4.name = reachIdent.hucs.huc4["@name"];
		this.huc4.id = reachIdent.hucs.huc4["@id"];
		this.huc2.name =reachIdent.hucs.huc2["@name"];
		this.huc2.id = reachIdent.hucs.huc2["@id"];
		this.comboBox = // Add the groups dropdown
	        new Ext.form.ComboBox({
	        	store: this.allGroupStore,
	        	anchor: '90%',
        		itemCls: 'zero-padding',
        		ctCls: 'zero-padding',
	        	hideLabel: true,
	        	mode: 'local',
	        	emptyText: 'Type group name',
	        	triggerAction: 'all',
	        	displayField: 'group',
	        	valueField: 'group'
	        });

		var defaults = {
				bodyStyle: "background-color: transparent",
				items: [{
					border: false,
					bodyStyle: "background-color: transparent",
					frame: false,
					padding: 4,
					items: [
					        {xtype: 'label', html: '<b>Reach Name: '+this.reach.name+'</b><br/>Choose a group from the dropdown and select what set of reaches to add.  A new group can be created by entering the name directly in the dropdown.'},
					        {
					        	layout: 'column',
								border: false,
				        		itemCls: 'zero-padding',
				        		ctCls: 'zero-padding',
								items: [{
									layout: 'form',
									border: false,
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
									columnWidth: 0.70,
									items:[this.comboBox]
								},{
									layout: 'form',
									border: false,
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
									columnWidth: 0.30,
									items:[{
						        		   xtype: 'button',
						           		itemCls: 'zero-padding',
						        		ctCls: 'zero-padding',
						        		anchor:"90%",
						        		   text: 'Add to Group',
						        		   handler: this.addToGroupClick,
						        		   scope: this
						        	   }]
								}
								]
					        },{
					        	xtype: 'radiogroup',
					        	columns: 1,
					        	hideLabel: true,
					        	items: [{
					        		name: 'add-to-group-radio',
					        		boxLabel: '<span style="font-size: small">This reach only.</span>',
					        		inputValue: 'reach',
					        		checked: true,
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
					        		listeners: {
					        			'check': {
					        				fn: this.onCheck,
					        				scope: this
					        			},
					        			'destroy': function(){
					        				Sparrow.CONTEXT.un("adjustment-group-changed", myGroupHandler);}
					        			}
					        	},{
					        		boxLabel: '<span style="font-size: small">' +
					        		'<a class="actionLink" title="Click to highlight the basin upstream of this reach" href="javascript:SvgOverlay.identifyUpstream(' +
					        		this.reach.id + ',' + Sparrow.SESSION.PredictionContext["@model-id"] + ',4)">' +
					        		'Reaches upstream of this reach' +
					        		'</a></span>',
					        		name: 'add-to-group-radio', inputValue: 'upstream',
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
					        		listeners: {
					        			'check': {
					        				fn: this.onCheck,
					        				scope: this
					        			}
					        		}
					        	},{
					        		boxLabel: '<span style="font-size: small">Reaches in HUC 8 : ' +
					        		'<a class="actionLink" title="Click to highlight this HUC" href="javascript:SvgOverlay.identifyHuc(\'' + this.huc8.id + '\',4)">' +
					        		this.huc8.name + ' : ' + this.huc8.id +
					        		'</a></span>',
					        		name: 'add-to-group-radio', inputValue: 'huc8',
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
					        		autoHeight: true,
					        		listeners: {
					        			'check': {
					        				fn: this.onCheck,
					        				scope: this
					        			}
					        		}
					        	},{
					        		boxLabel: '<span style="font-size: small">Reaches in HUC 6 : ' +
					        		'<a class="actionLink" title="Click to highlight this HUC" href="javascript:SvgOverlay.identifyHuc(\'' + this.huc6.id + '\',4)">' +
					        		this.huc6.name + ' : ' + this.huc6.id +
					        		'</a></span>',
					        		name: 'add-to-group-radio', inputValue: 'huc6',
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
					        		autoHeight: true,
					        		listeners: {
					        			'check': {
					        				fn: this.onCheck,
					        				scope: this
					        			}
					        		}
					        	},{
					        		boxLabel: '<span style="font-size: small">Reaches in HUC 4 : ' +
					        		'<a class="actionLink" title="Click to highlight this HUC" href="javascript:SvgOverlay.identifyHuc(\'' + this.huc4.id + '\',4)">' +
					        		this.huc4.name + ' : ' + this.huc4.id +
					        		'</a></span>',
					        		name: 'add-to-group-radio', inputValue: 'huc4',
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
					        		autoHeight: true,
					        		listeners: {
					        			'check': {
					        				fn: this.onCheck,
					        				scope: this
					        			}
					        		}
					        	},{
					        		boxLabel: '<span style="font-size: small">Reaches in HUC 2 : ' +
					        		'<a class="actionLink" title="Click to highlight this HUC" href="javascript:SvgOverlay.identifyHuc(\'' + this.huc2.id + '\',4)">' +
					        		this.huc2.name + ' : ' + this.huc2.id +
					        		'</a></span>',
					        		name: 'add-to-group-radio', inputValue: 'huc2',
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
					        		autoHeight: true,
					        		listeners: {
					        			'check': {
					        				fn: this.onCheck,
					        				scope: this
					        			}
					        		}
					        	}]
					        }
					        ]
				}]
		};
		// Apply all of the following settings to this object (basic instantiation)
		config = Ext.apply(config, defaults);
		Sparrow.ui.AddToGroupPanel.superclass.constructor.call(this, config);

	},
	/*
	 * Called when a radio button is checked or unchecked.  This function
	 * modifies the 'whatToAdd' attribute of the window to reflect what reaches
	 * the user intends to add to the selected group.
	 */
	onCheck: function(radio, checked) {
		if (checked) this.whatToAdd = radio.getGroupValue();
	},

	addToGroupClick: function() {
		// Get the selected group
		var groupName = this.comboBox.getRawValue();
		
		if (groupName == null || groupName == '') {

			if (this.whatToAdd == 'upstream') {
				groupName = this[this.whatToAdd].name + ' Group';
			} else {
				groupName = this[this.whatToAdd].name + ' ' + this.whatToAdd + ' Group';
			}
			
			Ext.MessageBox.confirm(
				"Default Name?", 
				'Would you like to use the default group name, "' + groupName + '"?', 
				function(answer){
					if(answer=='yes') this.addToGroup(groupName);
				},
				this);
		} else {
			this.addToGroup(groupName);
		}
	},
	
	addToGroup : function(groupName) {
		// Determine if this reach/set is already a member of that group
		var alreadyMember = false;
		var memberGroups = Sparrow.SESSION.getAllGroups();
		
		//if we are adding an individual reach, we just need to check if it is already a part of this group
		if (this.whatToAdd.indexOf('huc') == 0) { 
			var hucId = this.reachResponse["sparrow-id-response"].results.result[0].identification.hucs[this.whatToAdd]["@id"];
			alreadyMember = Sparrow.SESSION.isLogicalSetMemberOf(groupName, this.whatToAdd, hucId);
		} else if (this.whatToAdd == 'upstream') {
			for(var i = 0; i < memberGroups.length; i++) {
				var grp = memberGroups[i];
				for(var j = 0; j < grp.logicalSet.length; j++){
					var rId = grp.logicalSet[j].criteria['#text'];
					var at = grp.logicalSet[j].criteria['@attrib'];
					if(at == "reach" && rId == this.reach.id) alreadyMember = true;
				}
			}
		} else { //standard case, check if the reach exists in the group
			if(Sparrow.SESSION.isReachMemberOf(this.reach.id, groupName)) alreadyMember = true;
		}

		// Ensure the user selected a new group
		if (alreadyMember) {
			var type = this.whatToAdd;
			if(type.indexOf('huc') == 0) type = 'HUC';
			if(type=="upstream") type ='set of upstream reaches';
			Ext.Msg.show({
				title: 'Add to Group',
				msg: 'This ' + type + ' is already a member of that group.',
				buttons: Ext.Msg.OK,
				icon: Ext.Msg.INFO
			});
			return;
		} else {
			handleGroupMembership(this.reachResponse, [[groupName, this.whatToAdd]]);
		}
		
		this.refreshComboBox(groupName);
	},
	
	refreshComboBox : function(groupName) {
		this.loadAllGroupStore();
		this.comboBox.setValue(groupName); //make sure the value reregisters in combo
	},

	/*
	 * Loads the data store with all current user groups.  The store is used to
	 * populate the group dropdown.
	 */
	loadAllGroupStore: function() {
		this.allGroupStore.removeAll();

		var GroupRecord = Ext.data.Record.create([
		                                          {name: 'group'}
		                                          ]);

		// Get the list of all groups and add each to the data store
		var groups = Sparrow.SESSION.getAllGroupNames();
		for (var i = 0; i < groups.length; i++) {
			var record = new GroupRecord({
				group: groups[i]
			});
			this.allGroupStore.add(record);
		}
	}
});
Ext.reg('addtogrouppanel', Sparrow.ui.AddToGroupPanel);
