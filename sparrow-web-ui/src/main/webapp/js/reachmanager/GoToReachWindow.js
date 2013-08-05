
// TODO put buttonRenderer and CheckColumn in some kind of ExtJSUtils
/**
 * Renders a button with the specified text and handler function to a div
 * element in the page.
 */
var buttonRenderer = function(text, handler, options, iconCls) {
	options = options || {}
    var contentId = Ext.id();
    (function(id) {
        new Ext.Button({
            renderTo: id,
            text: text,
            iconCls: iconCls,
            //disabled: options.disabled,
            //cls: options.cls,
            handler: handler
        });
    }).defer(1, null, [contentId]);
    return '<div id="' + contentId + '"></div>';
};

Ext.grid.CheckColumn = function(config){
    Ext.apply(this, config);
    if(!this.id){
        this.id = Ext.id();
    }
    this.renderer = this.renderer.createDelegate(this);
};

Ext.grid.CheckColumn.prototype ={
    init : function(grid){
        this.grid = grid;
        this.grid.on('render', function(){
            var view = this.grid.getView();
            view.mainBody.on('mousedown', this.onMouseDown, this);
        }, this);
    },

    onMouseDown : function(e, t){
        if(t.className && t.className.indexOf('x-grid3-cc-'+this.id) != -1){
            e.stopEvent();
            var index = this.grid.getView().findRowIndex(t);
            var r = this.grid.store.getAt(index);

            if (!r.data[this.dataIndex]) {
                 // Add the reach to the group if it is not already a member
                Sparrow.SESSION.addReachToGroup(r.data.name, this.grid.reachId, this.grid.reachName);
            } else {
            	Sparrow.SESSION.removeReachFromGroup(r.data.name, this.grid.reachId);
            }

            r.set(this.dataIndex, !r.data[this.dataIndex]);
        }
    },

    renderer : function(v, p, record){
        p.css += ' x-grid3-check-col-td';
        return '<div class="x-grid3-check-col'+(v?'-on':'')+' x-grid3-cc-'+this.id+'">&#160;</div>';
    }
};




// DEFINE VTYPES for GoToReachWindow form fields
{
	{ // Define huc VType;
		Ext.form.VTypes['hucVal'] = /^\d{1,8}$/;
		Ext.form.VTypes['hucMask'] = /\d/;
		Ext.form.VTypes['hucText'] = 'Invalid huc. You may enter partial HUCs up to 8 digits';
		Ext.form.VTypes['huc'] = function(val){
			return Ext.form.VTypes['hucVal'].test(val);
		};
	}
	{ // Define reachid VType;
		Ext.form.VTypes['reachIDVal'] = /^\d{1,9}([\s,]+\d{1,9})*$/;
		Ext.form.VTypes['reachIDMask'] = /[\d,\s]/;
		Ext.form.VTypes['reachIDText'] = 'Invalid ReachID. Reach IDs are 1 to 5 digits';
		Ext.form.VTypes['reachID'] = function(val){
			return Ext.form.VTypes['reachIDVal'].test(val);
		};
	}
}

/**
 * Window to display find reach results
 */

var GOTO_REACH_WIN = new (function(){
	var goToReachWin;
	this.memberStore =  new Ext.data.JsonStore({
	    data: {groups: []},
	    root: 'groups',
	    fields: ['name', {name: 'member', type: 'bool'}]
	});
	var self = this;
	var checkBoxesArray;

	var reloadGroupsCombo = function(){
		var data = {
				groups: []
		};
		var groupNames = Sparrow.SESSION.getAllGroupNames();
		for (var i = 0; i < groupNames.length; i++) {
			data.groups.push({name: groupNames[i], member: false});
		}
		self.memberStore.loadData(data);

		var currentGrpName = Ext.getCmp('findReachWinGroupCombo').getValue();
		if (currentGrpName && !Sparrow.SESSION.groupExists(currentGrpName)) {
			Ext.getCmp('findReachWinGroupCombo').clearValue();
		}
	};

	Sparrow.CONTEXT.on("adjustment-group-changed", reloadGroupsCombo);

	var checkBoxRenderer = function(rowid, name) {
		var contentId = Ext.id();
		(function(id){
			var newCheckBox = new Ext.form.Checkbox({
				renderTo: id
			});
			checkBoxesArray.push({
				myCheckBox: newCheckBox,
				row_id: rowid,
				row_name: name
			});
		}).defer(1, null, [contentId]);
		return '<div id="'+contentId+'"></div>';
	};

	var _pageSize = 50;
	var _startRecord = 0;
	
	//Extend the timeout to 2 minutes
	var findReachProxy = new Ext.data.HttpProxy({ url: "findReaches" });
	findReachProxy.conn = { timeout: 120000 };

	var findReachStore = new Ext.data.Store({
		// load using HTTP
		url: 'findReaches',
		proxy: findReachProxy,
		//url: 'js_tests/FindReachTest.xml',
		remoteSort: true,
		// the return will be XML, so lets set up a reader
		reader: new Ext.data.XmlReader({
			// records will have an "Item" tag
			totalProperty: "results",
			record: 'reach',
			id: 'id'
		}, [
			// set up the fields mapping into the xml doc
			// The first needs mapping, the others are very basic
			{name: 'id', type: 'int'},
			'name',
			{name: 'huc8', mapping: 'hucs > huc8 > @id'},
			{name: 'meanq', type: 'float'},
			{name: 'catch-area', type: 'float'},
			{name: 'tot-contrib-area', type: 'float'},
			{name: 'groups', type: 'string'},
			{name: 'targets', type: 'string'}
		]),
		listeners: {
			beforeload: function (str) {
				str.lastOptions.params.xmlreq = getReachRequestXML(); //always have latest form request
			}
		}
	});
	findReachStore.setDefaultSort("meanq", "DESC");


	var goToReachFormContent = new Ext.FormPanel({
		buttonAlign: 'right',
		region: 'north',
		bodyStyle: 'padding: 5px',
		autoScroll: true,
		height: 250,
		items: [{
			border: false,
			layout: 'column',
			width: 675,
			defaults: {
				defaultType: 'textfield',
				border: false
			},
			items: [{
				layout: 'form',
				columnWidth: 0.60,
				labelWidth: 190,
				defaults: {
					style: {
						marginBottom: '1em'
					}
				},
				items: [{
					fieldLabel: 'Reach ID:<br/><span class="label-comment">an exact reach id</span>',
					labelSeparator: '',
					name: 'reachId',
					vtype: 'reachID'
				},{
					fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Reach Name Search Tips\')">Reach Name</a>:<br/><span class="label-comment">Complete or partial name</span>',
					labelSeparator: '',
					name: 'reachname'
				},{
					xtype: 'numberfield',
					fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Total Contributing Area\')">Total Contributing Area (km<sup style="text-decoration: none">2</sup>)</a>:<br /><span class="label-comment">Upper Limit</span>',
					name: 'watershedAreaHi'
				},{
					xtype: 'numberfield',
					fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.Total Contributing Area\')">Total Contributing Area (km<sup style="text-decoration: none">2</sup>)</a>:<br /><span class="label-comment">Lower Limit</span>',
					name: 'watershedAreaLo'
				},{
					fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.HUC\')">HUC</a>:<br/><span class="label-comment">Complete or beginning digits of a huc8</span>',
					labelSeparator: '',
					name: 'huc8',
					vtype: 'huc'
				}]
			},{
				layout: 'form',
				columnWidth: 0.40,
				labelWidth: 100,
				items: [{
					fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.EDA Name\')">EDA Name</a>',
					xtype: 'trigger',
					name: 'edaname',
					id: 'ext-edaname',
					style: {
						padding: '.1em',
						marginBottom: '1em'
					},
					onTriggerClick: function() {
						(new ShuttleBoxWindow({
							title: 'Select EDA Name(s)',
							optionItemsStore: edaNameStore,
							width: 300,
							displayField: 'name',
							valueField: 'name',
							defaultSelected: this.getRawValue().split(','),
							listeners: {
								close: function(w) {
									this.setValue(w.getValues());
								},
								scope: this
							}
						})).show();
					}
				},{
					fieldLabel: '<a class="helpLink" href="javascript:getHelpFromService(' + model_id + ',\'CommonTerms.EDA Code\')">EDA Code</a>',
					xtype: 'trigger',
					name: 'edacode',
					id: 'ext-edacode',
					style: {
						padding: '.1em',
						marginBottom: '1em'
					},
					onTriggerClick: function() {
						(new ShuttleBoxWindow({
							title: 'Select EDA Code(s)',
							width: 300,
							optionItemsStore: edaCodeStore,
							displayField: 'code',
							valueField: 'code',
							defaultSelected: this.getRawValue().split(','),
							listeners: {
								close: function(w) {
									this.setValue(w.getValues());
								},
								scope: this
							}
						})).show();
					}
				}]
			}]
		}],
		buttons: [{
			text: 'Find Reaches',
			handler: function() {
				checkBoxesArray = new Array();
				findReachStore.load({
					params: {
						xmlreq: getReachRequestXML(),
						start: _startRecord,
						limit: _pageSize
					}
				});
				findReachStore.on('load', function(g){
					var rawXml = findReachStore.reader.xmlData;
					var isOK = (rawXml.getElementsByTagName('status')[0].firstChild.data == "OK");
					var isOkEmpty = (rawXml.getElementsByTagName('status')[0].firstChild.data == "OK_EMPTY");
					Ext.getBody().unmask();
					var message = "";	//set below
					
					if (isOK) {
						var count = findReachStore.getTotalCount();
						var message = " (" + count + " records found)";
						if(checkBoxesArray.length > 0) {
			    			var bool = checkBoxesArray[0].myCheckBox.checked;
			    			for(i = 0; i < checkBoxesArray.length; i++) {
			    				cbObject = checkBoxesArray[i];
			    				cbObject.myCheckBox.setValue(false);
			    			}
			    		}
						
					} else if (isOkEmpty) {
						Ext.Msg.show({
							title: 'No Reaches Found',
							msg: 'No reaches were found that met your search criteria.',
							buttons: Ext.Msg.OK,
							icon: Ext.Msg.INFO
						});
						findReachStore.removeAll(true);
					} else  {
						//Must be an error
						Ext.Msg.show({
							title: 'An Error Occurred',
							msg: 'Please refer to the error below:<br />' + rawXml.getElementsByTagName('message')[0].firstChild.data,
							buttons: Ext.Msg.OK,
							icon: Ext.Msg.ERROR
						});
						findReachStore.removeAll(true);
					}
					GOTO_REACH_WIN.appendToTitle(message);
				});
			}
		}]
	});


	/**
	 * Renders an Ext-styled button to the grid column allowing the user to add a
	 * reach to the group specified by the grid's dropdown box.

	var addToGroupBtnRenderer = function(v, m, r, ri, ci, s) {
	    return buttonRenderer('add', function() { GOTO_REACH_WIN.addToAdjustmentGroup(r.id, r.data.name); });
	};
	*/

	/**
	 * Renders an Ext-styled button to the grid column allowing the user to add a
	 * reach to the list of targted reaches.

	var addToTargetBtnRenderer = function(v, m, r, ri, ci, s) {
	    return buttonRenderer('add', function() { SESSION.addToTargetReaches(r.id, r.data.name); });
	};
	*/


	/**
	 * Renders an Ext-styled button to the grid column allowing the user to ID a reach.
	 */
	var addIdentifyBtnRenderer = function(v, m, r, ri, ci, s) {
	    return buttonRenderer(
	    		'',
	    		function() { IDENTIFY.identifyReach(null, null, r.id, 4, true); }, {disabled:(context_id==null), cls: 'id_go'},
	    		'id-reach-icon');
	};

	var selectReachRenderer = function(v, m, r, ri, ci, s) {
		return checkBoxRenderer(r.id, r.data.name);
	};


	var findReachGrid = new Ext.grid.GridPanel({
		region: 'center',
	    store: findReachStore,
	    viewConfig: {forceFit: true},
	    columns: [
	  	    {header: "Identify", menuDisabled: true, width: 50, dataIndex: "identify", renderer: addIdentifyBtnRenderer},
	        {header: "ID", width: 55, dataIndex: 'id', sortable: true},
	        {header: "Name", width: 130, dataIndex: 'name', sortable: true},
	        {header: "HUC8", width: 100, dataIndex: 'huc8', sortable: true},
	        {header: "Total Contributing<br/>Area (km<sup>2</sup>)", width: 80, dataIndex: 'tot-contrib-area', sortable: true},
	        {header: "MeanQ<br/>(ft<sup>3</sup>/sec)", menuDisabled: true, width: 90, dataIndex: 'meanq', sortable: true},
	        {header: "Select?", menuDisabled: true, width: 50, dataIndex: "groups", renderer: selectReachRenderer}
	    ],
	    autoScroll: true,
	    stripeRows: true,
	    autoExpandColumn: 2,
		tbar: new Ext.PagingToolbar({
			pageSize: _pageSize,
			store: findReachStore,
			displayInfo: true,
			displayMsg: 'Displaying {0} - {1} of {2}',
			emptyMsg: "No Samples Found",
			items:[
			  '->',
			  ' - Add to:',
			  {
			    id: 'findReachWinGroupCombo',
			    xtype: 'combo',
			    emptyText: '- Type group name -',
			    store: self.memberStore,
			    displayField: 'name',
			    valueField: 'name',
			    mode: 'local',
			    triggerAction: 'all'
			}]
		})
	});

	this.open = function() {
		reloadGroupsCombo();

		if (!goToReachWin) {
			goToReachWin = new Ext.Window({
				title: 'Find Reach or Reaches',
				baseTitle: 'Find Reach or Reaches',
				closeAction: 'hide',
				border: false,
				layout: 'border',
				plain: true,
//				modal: true,
				width: 700,
				height: 545,
				draggable: true,
				resizable: true,
				buttonAlign: 'right',
				items: [
					goToReachFormContent,
					findReachGrid
					],
				buttons: [
				    {
				    	text: 'Select/Deselect All',
				    	handler: function() {
				    		if(checkBoxesArray.length > 0) {
				    			var bool = checkBoxesArray[0].myCheckBox.checked;
				    			for(i = 0; i < checkBoxesArray.length; i++) {
				    				cbObject = checkBoxesArray[i];
				    				cbObject.myCheckBox.setValue(!bool);
				    			}
				    		}
				    	}
				    },
				    {
				    	text: 'Add Selected to Group',
				    	handler: function() {
				    		var groupName = Ext.getCmp("findReachWinGroupCombo").getValue();
				    		var groupIsSelected = (groupName != "");
				    		if(!(groupIsSelected)) {
				    			var boxesAreSelected = false;
				    			for(i = 0; checkBoxesArray && i < checkBoxesArray.length; i++) {
				    				cbObject = checkBoxesArray[i];
				    				if(cbObject.myCheckBox.checked) {
				    					boxesAreSelected = true;
				    					break;
				    				}
				    			}
				    			if(boxesAreSelected) {
				    				Ext.Msg.alert("", "Please specify a group name.");
				    			}
				    		}
				    		else {
				    			Ext.getBody().mask('Adding to group ' + groupName + '...', 'x-mask-loading');
				    			Sparrow.SESSION.consolidateEvents();
					    		for(i = 0; checkBoxesArray && i < checkBoxesArray.length; i++) {
					    			cbObject = checkBoxesArray[i];
					    			if(cbObject.myCheckBox.checked) {
					    				GOTO_REACH_WIN.addToAdjustmentGroup(cbObject.row_id, cbObject.row_name);
					    			}
					    		}
					    		Sparrow.SESSION.releaseEvents();
					    	    Sparrow.SESSION.fireContextEvent("adjustment-group-changed"); //TODO, really should not be firing someone elses events
					    		setTimeout('Ext.getBody().unmask()', 500);
				    		}
				    	}
				    },
				    {
				    	text: 'Add Selected to Downstream Outlet Reach(es)',
				    	handler: function() {
		    				Ext.getBody().mask('Adding to targets...', 'x-mask-loading');
				    		Sparrow.SESSION.consolidateEvents();
				    		for(i = 0; checkBoxesArray && i < checkBoxesArray.length; i++) {
				    			cbObject = checkBoxesArray[i];
				    			if(cbObject.myCheckBox.checked) {
				    				Sparrow.SESSION.addToTargetReaches(cbObject.row_id, cbObject.row_name);
				    			}
				    		}
				    		Sparrow.SESSION.releaseEvents();
				    	    Sparrow.SESSION.fireContextEvent("targets-changed"); //TODO, really should not be firing someone elses events
				    		setTimeout('Ext.getBody().unmask()', 500);
				    	}
				    },
				    {
				    	text: 'Done',
				    	handler: function() {
							goToReachWin.hide();
						}
				    }
				]
			});
		}
		goToReachWin.show();

		var pos = goToReachWin.getPosition();
		if (pos[1] < 0) goToReachWin.setPosition(pos[0], 0);
	};

	this.appendToTitle = function(message){
		goToReachWin.setTitle(goToReachWin.baseTitle + message);
	};

	this.getFormValues = function(){
		return goToReachFormContent.getForm().getValues();
	};

	this.addToAdjustmentGroup = function(reachId, reachName){
	    var groupName = Ext.getCmp('findReachWinGroupCombo').getValue();
	    if (Sparrow.SESSION.groupExists(groupName)) {
	        if (Sparrow.SESSION.isReachMemberOf(reachId, groupName)) {
	            return; // already a member
	        }
	        Sparrow.SESSION.addReachToGroup(groupName, reachId, reachName);
	    } else if ('' !== groupName) {
	    	// Create a new group and add to that group
	        Sparrow.SESSION.addGroup(groupName, '', '', null);
	        Sparrow.SESSION.addReachToGroup(groupName, reachId, reachName);
	    } else {
	    	// No group specified. Prompt for group
	    	ADD_OR_REMOVE_REACH_FROM_GROUP_WIN.open(reachId, reachName);
	    }
	};
})();


//custom column plugin example
var toggleFindReachGroup = new Ext.grid.CheckColumn({
	header: "Member",
	dataIndex: 'member',
	menuDisabled: true,
	width: 55
});

var groupMemberGrid = new Ext.grid.EditorGridPanel({
  store: GOTO_REACH_WIN.memberStore,
	viewConfig: {forceFit: true},
  columns: [
      toggleFindReachGroup,
      {header: "Group",  dataIndex: 'name', sortable: true}
  ],
  enableHdMenu: true,
  border: false,
  autoScroll: true,
  plugins: toggleFindReachGroup,
  stripeRows: true,
  listeners: {
		rowclick: function(g) {
			g.getSelectionModel().clearSelections();
		}
	}
});

/**
* Popup window which appears when user clicks add button on a find reach result
* without having first selected a group to add to
*/
var ADD_OR_REMOVE_REACH_FROM_GROUP_WIN = new (function(){
	var addRemoveGoToReachWin;

	this.open = function(reachId, reachName){
		var data = {
				groups: []
		};
		var groupNames = Sparrow.SESSION.getAllGroupNames();
		for (var i = 0; i < groupNames.length; i++) {
			data.groups.push({name: groupNames[i], member: Sparrow.SESSION.isReachMemberOf(reachId, groupNames[i])});
		}

		groupMemberGrid.reachId = reachId;
		groupMemberGrid.reachName = reachName;

		GOTO_REACH_WIN.memberStore.loadData(data);

		if (!addRemoveGoToReachWin) {
			addRemoveGoToReachWin = new Ext.Window({
				closeAction: 'hide',
				plain: true,
				modal: true,
				draggable: true,
				resizable: false,
				layout: 'fit',
				buttonAlign: 'right',
				height: 200,
				width: 300,
				items: groupMemberGrid,
				buttons: [{
					text: 'Done',
					handler: function() {
						addRemoveGoToReachWin.hide();
					}
				}]
			});
		}

		addRemoveGoToReachWin.setTitle('Add/Remove ' + reachName + ' From Groups...');
		addRemoveGoToReachWin.show();
	}
})();









var edaCodeStore = new Ext.data.XmlStore({
  autoLoad: true,
	baseParams: {
		get: 'code',
		model: (model_id || Sparrow.USGS.getURLParam("model") || 22)
	},
  url: 'findReachSupport',
  record: 'code',
  fields: [{name: 'code', mapping: '/'}],
  sortInfo: {
      field: 'code'
  },
  listeners: {
		load: function() {
			if (edaCodeStore.getTotalCount() < 1) {
				Ext.getCmp('ext-edacode').hide();
			} else {
				Ext.getCmp('ext-edacode').show();
			}
		}
	}
});

var edaNameStore = new Ext.data.XmlStore({
  autoLoad: true,
	baseParams: {
		get: 'name',
		model: (model_id || Sparrow.USGS.getURLParam("model") || 22)
	},
  url: 'findReachSupport',
  record: 'name',
  fields: [{name: 'name', mapping: '/'}],
  sortInfo: {
      field: 'name'
  },
  listeners: {
		load: function() {
			if (edaNameStore.getTotalCount() < 1) {
				Ext.getCmp('ext-edaname').hide();
			} else {
				Ext.getCmp('ext-edaname').show();
			}
		}
	}
});


var ShuttleBoxWindow = Ext.extend(Ext.Window, {
	modal: true,
	bodyStyle: 'padding: 5px',
  initComponent: function() {
		Ext.apply(this, {
			buttons: [{
				text: 'Done',
				handler: this.close,
				scope: this
			}]
		})

		ShuttleBoxWindow.superclass.initComponent.apply(this, arguments);
	},

	afterRender: function() {
		this.options = document.createElement('select');
		this.options.multiple = true;
		this.options.size = 25;
		this.options.style.width = '100%';

		//populate options pick list
		this.optionItems = this.optionItemsStore.getRange();
		for (var i = 0; i < this.optionItems.length; i++) {
			this.options.options[i] = new Option(this.optionItems[i].data[this.displayField],this.optionItems[i].data[this.valueField]);

			//see if this should be default selected when window opens
			if (this.defaultSelected) {
				for (var j = 0; j < this.defaultSelected.length; j++) {
					if (this.optionItems[i].data[this.valueField] == this.defaultSelected[j]) {
						this.options.options[i].selected = true;
						this.options.options[i].defaultSelected = true;
						break; //j
					}
				}//j
			}
		}//i


//		This does not work for IE b/c the onmousedown
//		event (or any mouse event) does not indicate which
//		option was clicked.
//		this.options.onmousedown = function(e) {
//			if (!e) var e = window.event;
//			if (e) {
//
//				if (e.toElement) {
//					var selectedIndex = e.toElement.index;
//					var toggleMe = this[selectedIndex];
//					toggleMe.selected = ! toggleMe.selected;
//
//					//Cancel the event
//					if (e.preventDefault) e.preventDefault();
//					if (e.stopPropagation) e.stopPropagation();
//					return false;
//				}
//			}
//		};

		var optcap = document.createElement('div');
		if (Ext.isMac) {
			optcap.innerHTML = '<b>Selection List:</b><br /> Command-click to select multiple items.';
		} else {
			optcap.innerHTML = '<b>Selection List:</b><br /> Control-click to select multiple items.';
		}

		this.body.dom.appendChild(optcap);
		this.body.dom.appendChild(this.options);
		var bdiv = document.createElement('div');
		bdiv.id = 'shuttle-box-button-div';
		bdiv.align = 'center';
		this.body.dom.appendChild(bdiv);

		new Ext.ButtonGroup({
			renderTo: 'shuttle-box-button-div',
			columns: 3,
			border: false,
			style: 'margin-top: 1em',
			defaults: {
				iconAlign: 'top',
				tooltipType: 'title',
				style: 'margin-left: 5px; margin-right: 5px'
			},
			items: [{
				text: 'Clear all selections',
				//cls: 'x-btn-text-icon',
				tooltip: 'Clear All Selected Items',
				handler: this.clearSelections,
				scope: this
			}]
		});


		ShuttleBoxWindow.superclass.afterRender.apply(this, arguments);
	},

	clearSelections: function() {
		for (var i = 0; i < this.options.length; i++) {
			this.options[i].selected = false;
		}
	},

	getValues: function(delimiter) {
		var valString = '';
		delimiter = delimiter || ',';
		for (var i = 0; i < this.options.length; i++) {
			if (this.options[i].selected) {
				valString += this.options[i].value + delimiter;
			}
		}
		return valString.substr(0,valString.length-delimiter.length);
	}
});