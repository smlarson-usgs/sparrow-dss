/*
 * Window component used for editing group membership for a specific reach.
 * This component extends Ext.Window, providing settings and functions that are
 * specific to group membership (e.g., adding a group to the list).
 */
GroupMembershipWindow = Ext.extend(Ext.Window, {
    // Default window properties
    id: 'group-membership-window',
    title: 'Edit Group Membership',
    layout: 'border',
    closeAction: 'close',
    plain: true,
    modal: true,
    width: 500,
    height: 400,
    draggable: true,
    autoScroll: false,
    bodyBorder: false,
    resizable: false,
    minimizable: true,
    listeners: {
        minimize: function(w) {
            w.toggleCollapse(true);
        }
    },
    // Custom properties for this extension
    whatToAdd: 'reach',
    modified: false,
    memberGroups: [],
    memberGroupStore: null,
    removeGroupStore: null,
    allGroups: [],
    
    // Initialization of the window - add components and such
    initComponent: function() {
        
        // Set up the data store for the group membership list
        this.memberGroupStore = new Ext.data.SimpleStore({
            fields: ['groupName'],
            expandData: true,
            data: this.memberGroups
        });
        
        // Set up the data store for the groups from which to remove this reach
        this.removeGroupStore = new Ext.data.SimpleStore({
            fields: ['groupName'],
            expandData: true,
            data: []
        });
        
        // Set up the data store for the groups dropdown
        var allGroupStore = new Ext.data.SimpleStore({
            fields: ['groupName'],
            expandData: true,
            data: this.allGroups
        });

        // Apply all of the following settings to this object (basic instantiation)
        Ext.apply(this, {
            items: [new Ext.grid.GridPanel({
                // Top panel displaying the table of member groups
                id: 'current-groups',
                title: 'Groups',
                region: 'center',
                frame: false,
                hideHeaders: true,
                autoScroll: true,
                viewConfig: {forceFit: true},
                buttonAlign: 'center',
                store: this.memberGroupStore,
                columns: [
                    {dataIndex: 'groupName'}
                ],
                buttons: [{
                    text: 'Remove from selected groups',
                    handler: function() {
                        var selectedGroups = Ext.getCmp('current-groups').getSelectionModel().getSelections();
                        for (var i = 0; i < selectedGroups.length; i++) {
                            var record = selectedGroups[i];
                            // Remove it from the member list
                            this.memberGroupStore.remove(record);
                        }
                        // Add it to the remove list
                        this.removeGroupStore.add(selectedGroups);
                        this.modified = true;
                    },
                    scope: this
                }]
            }),{
                // Bottom panel displaying 'Add to Group' form
                id: 'add-to-group-panel',
                title: 'Add to Group',
                region: 'south',
                autoHeight: true,
                layout: 'form',
                frame: true,
                items: [
                    // Add the groups dropdown
                    new Ext.form.ComboBox({
                        id: 'all-groups-combobox',
                        store: allGroupStore,
                        hideLabel: true,
                        mode: 'local',
                        triggerAction: 'all',
                        displayField: 'groupName',
                        valueField: 'groupName'
                    }),
                    // Add radio buttons for the reach itself and its logical sets
                    new Ext.form.Radio({
                        name: 'add-to-group-radio',
                        boxLabel: 'This reach only.',
                        checked: true,
                        hideLabel: true,
                        inputValue: 'reach',
                        listeners: {
                            'check': {
                                fn: function(radio, checked) {
                                    if (checked) this.whatToAdd = 'reach';
                                },
                                scope: this
                            }
                        }
                    }),
                    new Ext.form.Radio({
                        name: 'add-to-group-radio',
                        boxLabel: 'All reaches in HUC8 : ' + this.huc8.name + ' : ' + this.huc8.id,
                        hideLabel: true,
                        inputValue: 'huc8',
                        listeners: {
                            'check': {
                                fn: function(radio, checked) {
                                    if (checked) this.whatToAdd = 'huc8';
                                },
                                scope: this
                            }
                        }
                    }),
                    new Ext.form.Radio({
                        name: 'add-to-group-radio',
                        boxLabel: 'All reaches in HUC6 : ' + this.huc6.name + ' : ' + this.huc6.id,
                        hideLabel: true,
                        inputValue: 'huc6',
                        listeners: {
                            'check': {
                                fn: function(radio, checked) {
                                    if (checked) this.whatToAdd = 'huc6';
                                },
                                scope: this
                            }
                        }
                    }),
                    new Ext.form.Radio({
                        name: 'add-to-group-radio',
                        boxLabel: 'All reaches in HUC4 : ' + this.huc4.name + ' : ' + this.huc4.id,
                        hideLabel: true,
                        inputValue: 'huc4',
                        listeners: {
                            'check': {
                                fn: function(radio, checked) {
                                    if (checked) this.whatToAdd = 'huc4';
                                },
                                scope: this
                            }
                        }
                    }),
                    new Ext.form.Radio({
                        name: 'add-to-group-radio',
                        boxLabel: 'All reaches in HUC2 : ' + this.huc2.name + ' : ' + this.huc2.id,
                        hideLabel: true,
                        inputValue: 'huc2',
                        listeners: {
                            'check': {
                                fn: function(radio, checked) {
                                    if (checked) this.whatToAdd = 'huc2';
                                },
                                scope: this
                            }
                        }
                    })
                ]
            }],
            buttons: [{
                text: 'OK',
                listeners: {
                    'click': {
                        fn: this.onOk,
                        scope: this
                    }
                }
            },{
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
        this.addEvents({ok: true});
        this.addEvents({cancel: true});
        
        // Call the superclass' init function
        GroupMembershipWindow.superclass.initComponent.apply(this, arguments);
    },
    
    onRender: function() {
        // Call the superclass' onRender function
        GroupMembershipWindow.superclass.onRender.apply(this, arguments);
    },
    
    onOk: function() {
        // Get the selected group
        var groupName = Ext.getCmp('all-groups-combobox').getRawValue();
        var alreadyMember = this.memberGroupStore.find('groupName', groupName) != -1;
        
        // Ensure the user selected a new group
        if (this.whatToAdd == 'reach' && alreadyMember) {
            Ext.Msg.alert('Already a member', 'This reach is already a member of that group.');
            return;
        } else {
            var removeFromGroups = this.removeGroupStore.collect('groupName');
            this.fireEvent('ok', groupName, this.whatToAdd, removeFromGroups);
            this.close();
        }
    },
    
    onCancel: function() {
        var cancel = true;
        if (this.modified) {
            cancel = confirm('Your changes will not be saved.  Are you sure you would like to cancel?');
        }
        
        if (cancel) {
            this.fireEvent('cancel');
            this.close();
        }
    }
});

Ext.reg('groupMembershipWindow', GroupMembershipWindow);
