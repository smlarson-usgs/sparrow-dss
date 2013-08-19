/*
 * Custom window component representing the Reach Identify window.  This window
 * is responsible for displaying and providing controls for managing data that
 * is specific to a particular reach.
 */

Sparrow.ui.ReachIdentifyWindow = Ext.extend(Ext.Window, {
    // Default window properties
    id: 'reach-identify-window',
    closeAction: 'close',
    layout: 'fit',
    plain: true,
    width: 725,
    minWidth: 675,
    height: 400,
    minHeight: 300,
    draggable: true,
    bodyBorder: false,
    shadow: false,
    listeners: {
        minimize: function(w) {
            w.toggleCollapse(true);
        },
        close: function() {

					if (SvgOverlay.hasOverlay()) {
						SvgOverlay.remindUserOverlayAdded();
					}

        	var newdiv = document.createElement('div');
        	newdiv.setAttribute('id', 'sparrow-identify-mapped-value-a');
        	newdiv.setAttribute('class', 'sparrow-identify-mapped-value');
        	document.body.appendChild(newdiv);
        	newdiv = document.createElement('div');
        	newdiv.setAttribute('id', 'sparrow-identify-mapped-value-b');
        	newdiv.setAttribute('class', 'sparrow-identify-mapped-value');
        	document.body.appendChild(newdiv);
        }
    },

    // Custom properties for this extension
    reachResponse: null,

    initComponent: function() {

        var reachId = this.reachResponse["sparrow-id-response"].results.result[0].identification.id;
        var reachName = this.reachResponse["sparrow-id-response"].results.result[0].identification.name;

        this.modelSourceMsg = '<p>Source input values can be changed in two ways. ' +
        'To change source values for just this reach,  enter new source values in the Override Amount column, below, then click OK. ' +
        'Reaches with this type of change are listed under \'Reaches with Absolute Changes\' on the Change Inputs tab at the left of the screen.</p>' +
        '<br/><p>To change source values for an entire set of reaches (such as all reaches in the same HUC as this reach), use the options on the Change Inputs tab to the left of the screen.</p>' +
        '<br/><p>The map (and the Adjusted Amount column) will not reflect input changes until the Update Map button is clicked.</p>';

    	this.individualAdjDisabledMsg = "Note: 'Reaches with Absolute Changes' is currently disabled on the Change Inputs tab, so changes in the Override Amount column will have no effect.";

        this.modelSourceInstructions = new Ext.Panel({
        	region: 'north',
        	autoHeight: true,
           	border: false,
           	bodyStyle: 'background-color: transparent',
           	padding: 5,
	    	html: this.modelSourceMsg
	   });

        // Set up the tab panel
        var tabs = new Ext.TabPanel({
            id: 'reach-identify-tab-panel',
            activeTab: 0,
            border: false,
            items: [
                new Ext.Panel({
                	id: 'attributesTab',
                    title: 'Reach/Catchment Info',
                	border: false,
                	layout: 'border',
                	items: [
							new Ext.Panel({
								border: false,
								region: 'north',
							   	contentEl: 'sparrow-identify-mapped-value-a'
							}),
                	        new AttributesGrid({id: 'attributesTabGrid', reachResponse: this.reachResponse})
                	]
                }),
                {
                    title: 'Model Source Inputs',
                    tabTip: 'Model source units can differ from kg/yr. See model documentation for details.',
                   	layout: 'border',
                   	border: false,
                	items: [
                	    this.modelSourceInstructions,
                		new AdjustmentsGrid({
                			id: 'treatmentsTab',
                			region: 'center',
                			reachResponse: this.reachResponse
            		})]
                },
                new Ext.Panel({
                	id: 'predictedValuesTab',
                    title: 'Predicted Values',
                	border: false,
                	layout: 'border',
                	items: [
							new Ext.Panel({
								border: false,
								region: 'north',
							   	contentEl: 'sparrow-identify-mapped-value-b'
							}),
                            new PredictedValuesGrid({id: 'predictedValuesTabGrid', reachId: reachId})
                	]
                }),
                new GraphPanel({id: 'graphTab', reachId: reachId})
//                ,new GroupsPanel({reachResponse: this.reachResponse}) //TODO candidate to remove permanently
            ]
        });

        // Apply the title, tab panel, and buttons to the window
        Ext.apply(this, {
            title: reachName + ' (ID: ' + reachId + ')',
            items: [tabs],
            buttons: [
//             {
//                id: 'reach-identify-apply-button',
//                text: 'Apply',
//                disabled: true,
//                listeners: {
//                    'click': {
//                        fn: this.onApply,
//                        scope: this
//                    }
//                }
//            },
            {
                id: 'reach-identify-ok-button',
                text: 'OK',
                listeners: {
                    'click': {
                        fn: this.onOk,
                        scope: this
                    }
                }
            },{
                id: 'reach-identify-cancel-button',
                text: 'Cancel',
                listeners: {
                    'click': {
                        fn: this.onCancel,
                        scope: this
                    }
                }
            }]
        });

        // Add custom events to the window
        this.addEvents({
        	apply: true,
        	ok: true,
        	cancel: true
        });


        ////TODO candidate to remove permanently after we decide on Group/Target tab
//        //Listen for add/remove of groups and enable the Apply button
//        Ext.getCmp('render-identify-groups-tab').on('modified', function() {
//            Ext.getCmp('reach-identify-apply-button').setDisabled(false);
//        });

        // Enable the Apply button when an adjustment is made
//        Ext.getCmp('treatmentsTab').on('afteredit', function() {
//            Ext.getCmp('reach-identify-apply-button').setDisabled(false);
//        });

        // Listen for activation of the predictions or graph tab and update on demand
        Ext.getCmp('predictedValuesTab').on('activate', function() {
            updateReachPredictions(reachId);
        });
        Ext.getCmp('graphTab').on('activate', function() {
            updateReachPredictions(reachId);
        });

        this.on("show", function(){
        	var grid = Ext.getCmp('treatmentsTab');
        	var override = grid.getColumnModel().getColumnById('overrideValue');

        	override.header = "Override Amount";
        	var msg = this.modelSourceMsg;
        	if(!Sparrow.SESSION.PredictionContext.adjustmentGroups.individualGroup['@enabled']) {
        		override.header = "<span style='color: gray'>"+override.header+"</span>";
        		msg += this.individualAdjDisabledMsg;
        	}
        	this.modelSourceInstructions.html = msg;
        });

        // Call the superclass' init function
        Sparrow.ui.ReachIdentifyWindow.superclass.initComponent.apply(this, arguments);
    },


    onRender: function() {
        // Call the superclass' onRender function
    	Sparrow.ui.ReachIdentifyWindow.superclass.onRender.apply(this, arguments);
    },

    /*
     * Gathers the changes that have been made to the tabs and fires the 'apply'
     * event, passing the list of changes as parameters.  Called when the user
     * clicks the Apply button.
     */
    //TODO:  The apply function is very misleading because it updates the client's
    //context, but does not post it.  Removing the apply function and letting
    //the user click 'OK' is a bit less confusing.
//    onApply: function() {
//    	this.isApplying = true;
//        Ext.getCmp('reach-identify-apply-button').setDisabled(true);
//        //this.applyChanges();//TODO remove this if we decide to remove the Group/Target tab for good
//        this.setModified(false);
//
//        var _this = this;
//        var reachId = this.reachResponse["sparrow-id-response"].results.result[0].identification.id;
//        var reachName = this.reachResponse["sparrow-id-response"].results.result[0].identification.name;
//        var overrides = Ext.getCmp('treatmentsTab').getOverrides();
//
//        var handler = function() {
//	        handleAdjustments(
//	        		reachId,
//	        		reachName,
//	        		overrides
//	        );
//	        _this.isApplying = false;
//        };
//
//      //check to see if reachId is already in adjustments
//    	var alreadyAdded = false;
//    	var allReaches = Sparrow.SESSION.getAllAdjustedReaches();
//    	for(var i = 0; i < allReaches.length; i++){
//    		if(allReaches[i]['@id']==reachId) {
//    			alreadyAdded = true;
//    			break;
//    		}
//    	}
//
//    	var hasOverrides = false;
//    	for (var i = 0; i < overrides.length; i++) {
//    		if (isNaN(parseFloat(overrides[i]))) {
//    			//do nothing
//    		} else {
//    			hasOverrides = true;
//    		}
//    	}
//
//    	if(alreadyAdded || !hasOverrides)
//    		handler();
//    	else
//	        Sparrow.ui.confirmAndAddGroup({
//	        	reachId: reachId,
//	        	modelId: model_id, //TODO global removal
//	        	existingGroups_xml: Sparrow.SESSION.getAdjustmentGroupsAsXML()
//	        }, handler);
//    },

    /*
     * Gathers the changes that have been made to the tabs and fires the 'ok'
     * event, passing the list of changes as parameters.  The window is also
     * closed after this event is fired.  Called when the user clicks the OK
     * button.
     */
    onOk: function() {
    	//this.applyChanges();//TODO remove this if we decide to remove the Group/Target tab for good
    	var reachId = this.reachResponse["sparrow-id-response"].results.result[0].identification.id;
    	var reachName = this.reachResponse["sparrow-id-response"].results.result[0].identification.name;

    	var overrides = Ext.getCmp('treatmentsTab').getOverrides();
    	var _this = this;
    	var handler = function() {
	    	handleAdjustments(
	    			reachId,
	    			reachName,
	    			overrides
	        );
	    	_this.close();
    	}

    	//check to see if reachId is already in adjustments
    	var alreadyAdded = false;
    	var allReaches = Sparrow.SESSION.getAllAdjustedReaches();
    	for(var i = 0; i < allReaches.length; i++){
    		if(allReaches[i]['@id']==reachId) {
    			alreadyAdded = true;
    			break;
    		}
    	}

    	var hasOverrides = false;
    	for (var i = 0; i < overrides.length; i++) {
    		if (isNaN(parseFloat(overrides[i]))) {
    			//do nothing
    		} else {
    			hasOverrides = true;
    		}
    	}

    	if(alreadyAdded || !hasOverrides)
    		handler();
    	else
	        Sparrow.ui.confirmAndAddGroup({
	        	reachId: reachId,
	        	modelId: model_id, //TODO global removal
	        	existingGroups_xml: Sparrow.SESSION.getAdjustmentGroupsAsXML()
	        }, handler);
    },

  //TODO remove this if we decide to remove the Group/Target tab for good
//    applyChanges: function() {
//      handleGroupMembership(this.reachResponse, Sparrow.SESSION.getAllGroups());
//	    var targetFlag = Ext.getCmp('render-identify-groups-tab').isTarget();
//	    if (targetFlag) {
//	    	Sparrow.SESSION.addToTargetReaches(this.getReachId(), this.getReachName());
//	    }
//	    else if(!targetFlag && Sparrow.SESSION.isReachTarget(this.getReachId())) {
//	    	Sparrow.SESSION.removeReachFromTargets(this.getReachId());
//	    }
//    },

    /*
     * Checks to see if any modifications have been made to the tabs in the
     * window and then closes the window.  If unsaved changes are found, the
     * window prompts the user to confirm the cancellation.
     */
    onCancel: function() {
        var cancel = true;
        if (this.isModified()) {
            // TODO: use Ext.Msg
            cancel = confirm('Your changes will not be saved.  Are you sure you would like to cancel?');
        }

        if (cancel) {
            this.fireEvent('cancel');
            this.close();
        }
    },

    /*
     * Returns the reach id associated with this reach identify.
     */
    getReachId: function() {
        return this.reachResponse["sparrow-id-response"].results.result[0].identification.id;
    },


    /*
     * Returns the reach name associated with this reach identify.
     */
    getReachName: function() {
        return this.reachResponse["sparrow-id-response"].results.result[0].identification.name;
    },

    /*
     * Returns whether or not the tabs in this window have been modified.  This
     * function simply delegates the call to each of its editable tabs and ORs
     * the responses.
     */
    isModified: function() {
        var modified = false;//TODO candidate for removal: Ext.getCmp('render-identify-groups-tab').isModified();
        modified = modified || Ext.getCmp('treatmentsTab').isModified();

        return modified;
    },

    /*
     * Sets the modified flag.  This function distributes the modified flag to
     * each of its editable tabs, setting their modified flag.
     */
    setModified: function(modified) {
        // Set the child tabs to the value of modified
        //TODO candidate for removal: Ext.getCmp('render-identify-groups-tab').setModified(modified);
        Ext.getCmp('treatmentsTab').setModified(modified);

        // Disable the Apply button based on the opposite of modified
        Ext.getCmp('reach-identify-apply-button').setDisabled(!modified);
    },

    /*
     * Refreshes the Reach Identify window.  This method checks for unsaved
     * changes to any of the tabs within the window and prompts the user to
     * save those changes.  If the user elects to save the changes, the 'apply'
     * event is fired which submits the changes and will ultimately result in
     * another call to refresh.
     */
    refresh: function(reachResponse) {
        if (this.isModified()) {
            // Prompt user to save and fire 'apply' if yes
            Ext.Msg.show({
                title: 'Save Changes?',
                msg: 'You have unsaved changes to the reach.  Would you like to save your changes?',
                buttons: Ext.Msg.YESNO,
                icon: Ext.Msg.QUESTION,
                fn: function(buttonId) {
                    if (buttonId == 'yes') {
                        this.onApply();
                    } else {
                        this.setModified(false);
                        this.refresh(reachResponse);
                    }
                },
                scope: this
            });
        } else {
            this.reachResponse = reachResponse;
            //TODO candidate for removal: Ext.getCmp('render-identify-groups-tab').refresh(this.reachResponse);
            Ext.getCmp('treatmentsTab').refresh(this.reachResponse);
            Ext.getCmp('attributesTabGrid').refresh(this.reachResponse);

            // If the predicted values or graph tab is active, update it
            var activePanel = Ext.getCmp('reach-identify-tab-panel').getActiveTab();
            if (activePanel.getId() == 'predictedValuesTab' || activePanel.getId() == 'graphTab') {
                var reachId = this.reachResponse["sparrow-id-response"].results.result[0].identification.id;
                updateReachPredictions(reachId);
            }
        }
    }
});

Ext.reg('reachIdentifyWindow', Sparrow.ui.ReachIdentifyWindow);
