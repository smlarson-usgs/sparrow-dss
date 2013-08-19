/*
 * Panel representing the Graph tab in the Reach Identify popup window.  The
 * graphs are updated when the panel is activated allowing them to display
 * information reflecting the most recent changes. google
 */
GraphPanel = Ext.extend(Ext.Panel, {
    title: 'Graphs',
    autoScroll: true,
    padding: '10 5 5 5',
    
    reachId: null,

    initComponent: function() {
        Ext.apply(this, {
            html: '<div id="gcapi-warn" align="center" style="display:none; font-family: tahoma">This model dataset contains too many sources to graph.</div><div id="gcapi-graphs" align="center"><div id="src_graph_legend"></div><br /><img id="src_bvg" /><br /><br /><img id="src_total_orig" /><img id="src_total_adj" /></div>'
        });
        
        // Call the superclass' init function
        GraphPanel.superclass.initComponent.apply(this, arguments);
    },

    onRender: function() {
        //Call the superclass' onRender function
        GraphPanel.superclass.onRender.apply(this, arguments);
    }
});

Ext.reg('graphPanel', GraphPanel);
