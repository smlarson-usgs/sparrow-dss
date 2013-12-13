Sparrow.ux.GroupNode = Ext.extend(Ext.tree.TreeNode, {
    contextMenu: new Ext.menu.Menu({
        items: [{
            text: 'Enabled',
            checked: true,
            listeners: {
                checkchange: function(item, checked) {
                    var node = item.parentMenu.contextNode;
                    Sparrow.ui.toggle_group_on_off(node.attributes.groupName, checked);
                }
            }
        },'-',{
            text: 'Edit / Apply Changes',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var groupName = node.attributes.groupName;
                Sparrow.ui.edit_group(groupName);
            }
        },{
            text: 'Duplicate',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var groupName = node.attributes.groupName;
                Sparrow.ui.duplicateGroup(groupName);
            }
        },{
            text: 'Move Up',
            disabled: true,
            handler: function(item, e) {
            }
        },{
            text: 'Move Down',
            disabled: true,
            handler: function(item, e) {
            }
        },{
            text: 'Delete',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var groupName = node.attributes.groupName;
                Sparrow.SESSION.removeGroup(groupName);
            }
        }]
    }),

    updateMenuState: function() {
        var disabled = this.disabled;
        this.contextMenu.get(0).setChecked(!disabled, true);
    }
});

Sparrow.ux.ReachNode = Ext.extend(Ext.tree.TreeNode, {
    contextMenu: new Ext.menu.Menu({
        items: [{
            text: 'Show on Map',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var reachId = node.attributes.reachId;
                IDENTIFY.identifyReach(null, null, reachId, 4, true);
            }
        },{
            text: 'Remove from group',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var reachId = node.attributes.reachId;
                var groupName = node.parentNode.attributes.groupName;
                Sparrow.SESSION.removeReachFromGroup(groupName, reachId);
            }
        }]
    }),

    onBeforeMove: function(tree, node, oldParent, newParent, index) {
        var reachId = node.attributes.reachId;
        var reachName = node.attributes.reachName;
        var oldGroup = oldParent.attributes.groupName;
        var newGroup = newParent.attributes.groupName;

        if (oldGroup === newGroup || Sparrow.SESSION.isReachMemberOf(reachId, newGroup)) {
            return false;
        }

        Sparrow.CONTEXT.suspendEvents(false);
        Sparrow.SESSION.removeReachFromGroup(oldGroup, reachId);
        Sparrow.SESSION.addReachToGroup(newGroup, reachId, reachName);
        Sparrow.CONTEXT.resumeEvents();
    },

    updateMenuState: Ext.emptyFn
});

Sparrow.ux.HucNode = Ext.extend(Ext.tree.TreeNode, {
    contextMenu: new Ext.menu.Menu({
        items: [{
			text: 'Show on Map',
			handler: function(item, e) {
				var node = item.parentMenu.contextNode;
				var hucId = node.attributes.hucId;
				SvgOverlay.identifyHuc(hucId, 3);
						}
					},{
            text: 'Remove from group',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var hucId = node.attributes.hucId;
                var hucType = node.attributes.type;
                var groupName = node.parentNode.attributes.groupName;
                Sparrow.SESSION.removeLogicalSetFromGroup(groupName, hucType, hucId);
            }
        }]
    }),

    onBeforeMove: function(tree, node, oldParent, newParent, index) {
        var hucId = node.attributes.hucId;
        var hucLevel = node.attributes.type;
        var hucName = node.attributes.hucName;
        var oldGroup = oldParent.attributes.groupName;
        var newGroup = newParent.attributes.groupName;

        if (oldGroup === newGroup || Sparrow.SESSION.isLogicalSetMemberOf(newGroup, hucLevel, hucId)) {
            return false;
        }

        Sparrow.CONTEXT.suspendEvents(false);
        Sparrow.SESSION.removeLogicalSetFromGroup(oldGroup, hucLevel, hucId);
        Sparrow.SESSION.addLogicalSetToGroup(newGroup, hucLevel, hucId, hucName);
        Sparrow.CONTEXT.resumeEvents();
    },

    updateMenuState: Ext.emptyFn
});

Sparrow.ux.UpstreamNode = Ext.extend(Ext.tree.TreeNode, {
    contextMenu: new Ext.menu.Menu({
        items: [{
            text: 'Remove from group',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var reachId = node.attributes.reachId;
                var type = node.attributes.type;
                var groupName = node.parentNode.attributes.groupName;
                Sparrow.SESSION.removeLogicalSetFromGroup(groupName, type, reachId);
            }
        }]
    }),

    onBeforeMove: function(tree, node, oldParent, newParent, index) {
        var reachId = node.attributes.reachId;
        var type = node.attributes.type;
        var reachName = node.attributes.reachName;
        var oldGroup = oldParent.attributes.groupName;
        var newGroup = newParent.attributes.groupName;

        if (oldGroup === newGroup || Sparrow.SESSION.isLogicalSetMemberOf(newGroup, type, reachId)) {
            return false;
        }

        Sparrow.CONTEXT.suspendEvents(false);
        Sparrow.SESSION.removeLogicalSetFromGroup(oldGroup, type, reachId);
        Sparrow.SESSION.addLogicalSetToGroup(newGroup, type, reachId, reachName);
        Sparrow.CONTEXT.resumeEvents();
    },

    updateMenuState: Ext.emptyFn
});

Sparrow.ux.AdjustmentGroupNode = Ext.extend(Ext.tree.TreeNode, {
    contextMenu: new Ext.menu.Menu({
        items: [{
            text: 'Enabled',
            checked: true,
            listeners: {
                checkchange: function(item, checked) {
                    var node = item.parentMenu.contextNode;
                    Sparrow.ui.toggleIndividualGroup(checked);
                }
            }
        }]
    }),

    updateMenuState: Ext.emptyFn
});

Sparrow.ux.AdjustmentNode = Ext.extend(Ext.tree.TreeNode, {
    contextMenu: new Ext.menu.Menu({
        items: [{
            text: 'Show on Map',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var reachId = node.attributes.reachId;
                IDENTIFY.identifyReach(null, null, reachId, 4, true);
            }
        },{
            text: 'Clear adjustments',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var reachId = node.attributes.reachId;
                Sparrow.SESSION.removeAdjustedReach(reachId);
            }
        }]
    }),

    updateMenuState: Ext.emptyFn
});

Sparrow.ux.TargetNode = Ext.extend(Ext.tree.TreeNode, {
    contextMenu: new Ext.menu.Menu({
        items: [{
            text: 'Show on Map',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var reachId = node.attributes.reachId;
                IDENTIFY.identifyReach(null, null, reachId, 4, true);
            }
        },{
            text: 'Remove from Downstream Reaches',
            handler: function(item, e) {
                var node = item.parentMenu.contextNode;
                var reachId = node.attributes.reachId;
                Sparrow.SESSION.removeReachFromTargets(reachId);
            }
        }]
    })
});

Sparrow.ux.GroupTreePanel = Ext.extend(Ext.tree.TreePanel,{
	constructor: function (config) {
		var defaults = {
			border: true,
		    animate: false,
		    useArrows: true,
		    rootVisible: false,
		    enableDD: true,
		    dropConfig: {
		        appendOnly: true
		    },
		    root: new Ext.tree.TreeNode(),
		    listeners: {
		        contextmenu: function(node, e) {
		            node.select();
		            node.updateMenuState();

		            var menu = node.contextMenu;
		            menu.contextNode = node;
		            menu.showAt(e.getXY());
		        }
		    }
		};
		config = Ext.applyIf(config, defaults);
		Sparrow.ux.GroupTreePanel.superclass.constructor.call(this, config);
	},


    loadTree: function() {
        this.clearTree();
        var groupList = Sparrow.SESSION.getAllGroups();
        for (var i = 0, ilen = groupList.length; i < ilen; i += 1) {
            var group = groupList[i];
            var isDisabled = !group['@enabled'];

            var groupNode = this.getRootNode().appendChild(new Sparrow.ux.GroupNode({
                groupName: group['@name'],
                text: group['@name'],
                draggable: false,
                disabled: isDisabled,
                leaf: false
            }));

            var reaches = group.reach;
            for (var j = 0, jlen = reaches.length; j < jlen; j += 1) {
                var reachNode = groupNode.appendChild(new Sparrow.ux.ReachNode({
                    reachId: reaches[j]['@id'],
                    reachName: reaches[j]['@name'],
                    text: reaches[j]['@name'] + ' (' + reaches[j]['@id'] + ')',
                    disabled: isDisabled,
                    leaf: true
                }));
                reachNode.on('beforemove', reachNode.onBeforeMove);
            }

            var sets = group.logicalSet;
            for (var j = 0, jlen = sets.length; j < jlen; j += 1) {
                var attrib = sets[j].criteria['@attrib'].toUpperCase();
                if(attrib == "REACH") {
                	var upstreamNode = groupNode.appendChild(new Sparrow.ux.UpstreamNode({
	                    reachId: sets[j].criteria['#text'],
	                    upstreamName: sets[j].criteria['@name'],
	                    type: sets[j].criteria['@attrib'],
	                    text: sets[j].criteria['@name'] + ' (' + attrib + ':' + sets[j].criteria['#text'] + ')',
	                    disabled: isDisabled,
	                    leaf: true
	                }));
                	upstreamNode.on('beforemove', upstreamNode.onBeforeMove);
                } else {
	                var hucNode = groupNode.appendChild(new Sparrow.ux.HucNode({
	                    hucId: sets[j].criteria['#text'],
	                    hucName: sets[j].criteria['@name'],
	                    type: sets[j].criteria['@attrib'],
	                    text: sets[j].criteria['@name'] + ' (' + attrib + ':' + sets[j].criteria['#text'] + ')',
	                    disabled: isDisabled,
	                    leaf: true
	                }));
	                hucNode.on('beforemove', hucNode.onBeforeMove);
                }
            }
        }


        var adjReaches = Sparrow.SESSION.getAllAdjustedReaches();
        var adjNode = this.getRootNode().findChild('id', 'adjReaches');
        if(adjReaches.length > 0) {
            isDisabled = !Sparrow.SESSION.PredictionContext.adjustmentGroups.individualGroup['@enabled'];
            if (!adjNode) {
                adjNode = this.getRootNode().appendChild(new Sparrow.ux.AdjustmentGroupNode({
                    id: 'adjReaches',
                    text: 'Reaches with Absolute Changes',
                    draggable: false,
                    allowDrop: false,
                    disabled: isDisabled,
                    leaf: false
                }));
            }
	        for (var i = 0, len = adjReaches.length; i < len; i += 1) {
	            adjNode.appendChild(new Sparrow.ux.AdjustmentNode({
	                reachId: adjReaches[i]['@id'],
	                text: adjReaches[i]['@name'] + ' (' + adjReaches[i]['@id'] + ')',
	                draggable: false,
	                disabled: isDisabled,
	                leaf: true
	            }));
	        }
        } else { //remove group
        	if(adjNode) this.getRootNode().removeChild(adjNode);
        }
        this.expandAll();
    },

    clearTree: function() {
        var root = this.getRootNode();
        while (root.firstChild) {
            root.removeChild(root.firstChild).destroy();
        }
    }
});

Sparrow.ux.TargetTreePanel = Ext.extend(Ext.tree.TreePanel,{
	constructor: function(config) {
		var defaults = {
			border: true,
			autoScroll: true,
			region: 'center',
		    animate: false,
		    useArrows: true,
		    rootVisible: false,
		    root: new Ext.tree.TreeNode()
		};

		config = Ext.applyIf(config, defaults);
		Sparrow.ux.TargetTreePanel.superclass.constructor.call(this, config);
	},

    loadTree: function() {
        this.clearTree();

        var targetReaches = Sparrow.SESSION.getAllTargetedReaches();
        for (var i = 0, len = targetReaches.length; i < len; i += 1) {
            this.getRootNode().appendChild(new Sparrow.ux.TargetNode({
                reachId: targetReaches[i]['@id'],
                text: targetReaches[i]['@name'] + ' (' + targetReaches[i]['@id'] + ')',
                leaf: true
            }));
        }
        this.expandAll();
    },

    clearTree: function() {
        var root = this.getRootNode();
        while (root.firstChild) {
            root.removeChild(root.firstChild).destroy();
        }
    },

    listeners: {
        contextmenu: function(node, e) {
            node.select();
            var menu = node.contextMenu;
            menu.contextNode = node;
            menu.showAt(e.getXY());
        }
    }
});




Sparrow.AdjustmentsPanel = Ext.extend(Ext.Panel, {
	constructor: function(config){
		var fieldsetAnchor = '92%';

		this.instructionsPanel = new Ext.Panel({
			region: 'north',
			border: false,
			autoHeight: true,
			html: '<b>2. Change the values of the source inputs</b>'
		});
		this.treePanel = new Sparrow.ux.GroupTreePanel({
			border: false
		});
		this.currentReachAsAdjusted = new Ext.Panel({
        	border: false,
        	fieldLabel: '',
        	labelSeparator: '',
        	labelWidth: 10,
        	labelAlign: 'top',
        	layout: 'form',
        	hidden: true,
    		itemCls: 'zero-padding',
    		ctCls: 'zero-padding',
        	items:[
        	       {xtype:'panel',
	        		itemCls: 'zero-padding',
	        		ctCls: 'zero-padding'
	        		} //holder, will have the radio group inserted here later
        	], listeners: {
        		'beforeshow' : function(p) {
				p.getEl().fadeIn();
        		}
        	}
        });
		var defaults = {
			title: 'Change Inputs',
			border: false,
			layout: 'border',
			tabTip: 'Change inputs in selected reaches/basins',
			items: [{
					contentEl: 'adjustments-text',
					frame: false,
					border: false,
					region: 'north',
					collapsible: false,
					layout: 'fit'
			    },new Ext.form.FormPanel({
					border: true,
					autoScroll: true,
					region: 'center',
	        		itemCls: 'zero-padding',
	        		ctCls: 'zero-padding',
					items: [{
						xtype: 'fieldset',
						anchor: fieldsetAnchor,
						layout: 'form',
						labelWidth: 15, //this is actually used as indentation for this section
		        		itemCls: 'zero-padding',
		        		ctCls: 'zero-padding',
						items: [
					        {xtype: 'panel', border: false, html: '<b>1. Select stream reach(es) where changes will be applied</b>'},
					        {
					        	xtype: 'panel',
					        	border: false,
					        	labelSeparator: '',
					        	html: '<a style="cursor: pointer; padding: .2em 0; display: block;" title="Identify Reach">--Locate on map<img src="images/identifygif.gif" alt="Identify icon" style="vertical-align: bottom;"/></a>',
				        		itemCls: 'zero-padding',
				        		ctCls: 'zero-padding',
					        	listeners: {
					        		render: function(c) {
				        		      Ext.fly(c.el).on('click', function(e, t) {
				        		    	  var b = Ext.getCmp('mapToolButtonsIdentify');//TODO using global map1 here
				        		    	  if (!b.pressed) b.toggle();
				        		    	  map1.setMouseAction(IDENTIFY.identifyPoint); //TODO using global map1 here
				        		      });
				        		    }
					        	}
					        },{
					        	xtype: 'panel',
					        	border: false,
					        	labelSeparator: '',
				        		itemCls: 'zero-padding',
				        		ctCls: 'zero-padding',
				        		html: '--<a style="cursor: pointer;">Find by name or hydrologic unit code</a>',
					        	listeners: {
					        		render: function(c) {
				        		      Ext.fly(c.el).on('click', function(e, t) {
				        		    	  GOTO_REACH_WIN.open();
				        		      });
				        		    }
					        	}
					        },
					        this.currentReachAsAdjusted
				        ]
					},
					{
						xtype: 'fieldset',
						anchor: fieldsetAnchor,
		        		itemCls: 'zero-padding',
		        		ctCls: 'zero-padding',
						layout: 'border',
						height: 70,
						autoScroll: false,
						items: [
						       this.instructionsPanel,
						       {
						    	   region: 'center',
						    	   layout: 'anchor',
						    	   autoScroll: true,
						    	   border: false,
					        		itemCls: 'zero-padding',
					        		ctCls: 'zero-padding',
						    	   items: [this.treePanel]

						       }]
					},
					{
						xtype: 'fieldset',
						anchor: fieldsetAnchor,
		        		itemCls: 'zero-padding',
		        		ctCls: 'zero-padding',
						layout: 'form',
						labelWidth: 15, //this is actually used as indentation for this section
						items: [
					        {xtype: 'panel', border: false, html: '<b>3. Display Results</b><br/>'},
					        {
					        	xtype: 'panel',
					        	padding: 5,
					        	border: false,
					        	html: 'From the '+
					        	'<a href="javascript:GOTO_MAP_OPTIONS_TAB()">Display Results tab</a>, select a data series.'+
					        	'<br/><br/>(Map relative or absolute changes using the <i><b>Comparison to Original Model</b></i> feature)'
					        }
				        ]
					}
					]
				})
			]
		};
		config = Ext.applyIf(config, defaults);
		Sparrow.AdjustmentsPanel.superclass.constructor.call(this, config);
	},

	showInstructions : function() {
		this.instructionsPanel.update('<b>2. Change the values of the source inputs</b><br/>(Right click to change input values or show on map)');
		this.syncSize();
		this.instructionsPanel.ownerCt.setHeight(150);
		this.doLayout(false, true);
	},

	hideInstructions : function() {
		this.instructionsPanel.update('<b>2. Change the values of the source inputs</b>');
		this.syncSize();
		this.instructionsPanel.ownerCt.setHeight(70);
		this.doLayout(false, true);
	},
	
	/**
	 * Sets which reach is currently identified so that the UI can
	 * provide a buttons to add that reach (or related set) to the adjustment list.
	 */
	setIdentifiedReach : function(reachResponse) {

		var adjReachGroup = this.currentReachAsAdjusted.items.items[0];
		this.currentReachAsAdjusted.remove(adjReachGroup, true);
		this.currentReachAsAdjusted.show();
		this.currentReachAsAdjusted.add(new Sparrow.ui.AddToGroupPanel({reachResponse: reachResponse}));
		this.doLayout(false, true);
	},
	
	/**
	 * Cancels the available actions for an identified reach.
	 */
	clearIdentifiedReach : function() {
		this.currentReachAsAdjusted.hide();
		this.doLayout(false, true);
	}
});

Sparrow.TargetsPanel = Ext.extend(Ext.Panel, {
	constructor: function(config){
		var dataSeriesTip = 'Please note that data series selected here will require that you first specify a target reach for the analysis.';

		this.treePanel = new Sparrow.ux.TargetTreePanel({
			border: false,
			region: 'center',
			margin: 20,
			title: 'Selected Downstream Reaches (0 total)<br/><i>Use the buttons above to add reaches.</i>'
		});

		this.displayResultsInstructions = new Ext.Panel({
			region: 'south',
        	border: false,
        	html: '<p><b>2. Display Results</b><br/>[No downstream reaches have been chosen]</p>'
		});

		this.selectByIdButton = new Ext.Button({
			text: 'Identify on the map', style: 'margin: 5px; clear: both;',
			icon: 'images/identifygif.gif', cls: 'x-btn-text-icon',
			listeners: {
				render: function(c) {
				  Ext.fly(c.el).on('click', function(e, t) {
					  var b = Ext.getCmp('mapToolButtonsIdentify');//TODO using global map1 here
					  if (!b.pressed) {
						  b.toggle();
						  Ext.Msg.alert(
							'Identifying a reach to add it as a downstream reach', 
							"The 'Identify Reach' tool is now selected on the map - its located in the upper left corner of the map window.<br/>" +
							"Click on the map to identify a reach, then use the menu to the left of the map to add that reach as a Downstream Reach.");
					  } else {
						  Ext.Msg.alert(
							'Identifying a reach to add it as a downstream reach', 
							"The 'Identify Reach' tool is already selected on the map - its located in the upper left corner of the map window.<br/>" +
							"Click on the map to identify a reach, then use the menu to the left of the map to add that reach as a Downstream Reach.");
					  }
					  map1.setMouseAction(IDENTIFY.identifyPoint); //TODO using global map1 here

				  });
				},
				beforeshow : function(p) { p.getEl().fadeIn();}
			}
		});
		
		this.currentReachAsDownstreamButton = new Ext.Button({
			hidden: true, style: 'clear: both; margin: 5px;',
			scale: 'large', text: 'Add as Downstream Reach<br/>name',
			icon: 'images/identifygif.gif', cls: 'x-btn-text-icon',
			listeners: {'beforeshow' : function(p) { p.getEl().fadeIn();}}
		});

		var defaults = {
			title: 'Downstream Tracking',
			border: false,
			layout: 'border',
			tabTip: 'view and manage downstream reaches',
			items: [{
					contentEl: 'targets-text',
			        frame: false,
			        border: false,
					collapsible: false,
					region: 'north'
			    },{
					xtype: 'panel',
					border: false,
					region: 'center',
					layout: 'border',
					items: [{
						xtype: 'panel',
						region: 'north',
						padding: 5,
						height: 110,
						items: [{
								xtype: 'panel', border: false, html: '<b>1. Select Downstream Reach(es) :</b>', style: 'margin: 5px 0;'
							}, {
								xtype: 'button', text: 'From list of watersheds', style: 'margin: 5px; float: left;',
								listeners: {
									render: function(c) {
									  Ext.fly(c.el).on('click', function(e, t) {
										  WATERSHED_WINDOW.open();
									  });
									}
								}
							}, {
								xtype: 'button', text: 'By name or hydrologic unit code', style: 'margin: 5px; float: left;',
								listeners: {
									render: function(c) {
									  Ext.fly(c.el).on('click', function(e, t) {
										  GOTO_REACH_WIN.open();
									  });
									}
								}
							},
							this.selectByIdButton, 
							this.currentReachAsDownstreamButton]

					},
					{
						xtype: 'panel',
						region: 'center',
						layout: 'border',
						padding: '10',
						border: false,
						items: [
							   this.treePanel,
							   {
									xtype: 'panel',
									region: 'south',
									border: false,
									items: [{
										xtype: 'button',
										style: 'margin: 5px auto',
										text: 'Remove all Downstream Reaches',
										handler: function() {
											Ext.Msg.confirm('', 'Are you sure you want to remove all of your selected downstream reaches?', function(choice) {
												if(choice == 'yes')
													Sparrow.SESSION.removeAllReachesFromTargets();
											}, this);
										}
									}]

							   }]
					},
					{
						
						xtype: 'panel', region: 'south', layout: 'fit',
						height: 34, padding: 5,
						items: [
							this.displayResultsInstructions
						]
					}
					]
				}]
		};
		config = Ext.applyIf(config, defaults);
		Sparrow.TargetsPanel.superclass.constructor.call(this, config);
	},

	updateInstructions : function(reachCount) { 
		var tree = this.treePanel;
		if (reachCount > 0) {
			tree.setTitle('<b>Selected Downstream Reaches ('+reachCount+' total)</b><br/><i>Right click on reach name for more information</i>');
		} else {
			tree.setTitle('<b>Selected Downstream Reaches (0 total)</b><br/><i>Use the buttons above to add reaches.</i>');
		}
		this.doLayout();
	},
			
	updateStepTwoInstructions : function(text) {
		this.displayResultsInstructions.body.update('<p><b>2. Display Results</b><br/>' + text + '</p>');
		var newHeight = this.displayResultsInstructions.body.dom.children[0].offsetHeight;
		this.displayResultsInstructions.ownerCt.setHeight(newHeight + 10);
		this.doLayout();
	},
	
	/**
	 * Sets which reach is currently identified so that the UI can
	 * provide a button to add that reach to the downstream list.
	 */
	setIdentifiedReach : function(reachId, reachName) {

		var addButton = this.currentReachAsDownstreamButton;
		
		//Stash these as properties of the control
		addButton.reachid = reachId;
		addButton.reachName = reachName;

		addButton.setText("Add the currently identified reach<br/><b>'" + reachName + "'</b> as a Downstream Reach");
		addButton.handler=function(btn){
			if(Sparrow.SESSION.isReachTarget(btn.reachid))
				Ext.MessageBox.alert("Already added", '"'+btn.reachName+'" is already a downstream reach');
			else
				Sparrow.SESSION.addToTargetReaches(btn.reachid, btn.reachName);
		};
		this.selectByIdButton.hide();
		this.currentReachAsDownstreamButton.show();
		this.doLayout(false, true);
	},
	
	/**
	 * Cancels the available actions for an identified reach.
	 */
	clearIdentifiedReach : function() {
		//Clear these props so there is no confusion
		this.currentReachAsDownstreamButton.reachid = '';
		this.currentReachAsDownstreamButton.reachName = '';
		
		this.currentReachAsDownstreamButton.hide();
		this.selectByIdButton.show();
		this.doLayout(false, true);
	}
});
