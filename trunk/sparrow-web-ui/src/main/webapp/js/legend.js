/**
 * Renders the legend panel for the current set of
 * buckets.  The legend displays each bucket's color
 * and the range of values it will include.
 * Average
 */
Sparrow.ui.renderLegend = function() {
	 // Get data series
    var dataSeries = Sparrow.SESSION.getDataSeries();
    var comparison = Sparrow.SESSION.getDataSeriesSource();

    // Retrieve bins from the json object
    var displayBins = Sparrow.SESSION.getBinData()["displayBins"];
    var colors = Sparrow.SESSION.getBinData()["binColors"];
    var boundUnlimited = Sparrow.SESSION.getBinData()["boundUnlimited"];
    var nonDetect = Sparrow.SESSION.getBinData()["nonDetect"];
    
    var storeData = [];
    
    //Merge the bounds, colors, and unbounded flags into one set of data for the store
    for (var i=0; i< displayBins.length; i++) {
    	storeData[i] = {};
    	storeData[i]['low'] = displayBins[i]['low'];
    	storeData[i]['high'] = displayBins[i]['high'];
    	storeData[i]['color'] = colors[i];
    	storeData[i]['lowUnlimited'] = boundUnlimited[i]['low'];
    	storeData[i]['highUnlimited'] = boundUnlimited[i]['high'];
    	storeData[i]['nonDetect'] = nonDetect[i];
    }
    
    var store = new Ext.data.JsonStore({
        data: storeData,
        fields: ['low', 'high', 'color', 'lowUnlimited', 'highUnlimited', 'nonDetect']
    });

    // Template to create html table for bins
    var tpl = new Ext.XTemplate(
        '<div style="text-align: right;"><table cellspacing="5" id="legend-table">',
        '' +  ((comparison=='absolute')?'<tr><td colspan="4" style="font-size: smaller; font-style: italic">*negative numbers<br/>indicate reduction</td></tr>':'') + '',
        '<tr><th colspan="4" class="legend-title">' + Sparrow.SESSION.getLegendTitle() + '</th></tr>',
        '<tr><td colspan="4" style="text-align: center;">' + Sparrow.SESSION.getLegendUnitsAndConstituent() + '</td></tr>',
        '<tpl for=".">',
        '  <tr class="bin"><td style="background-color: #{color}; width: 20px; border: 1px solid #666666;"></td>',
        '    {[this.renderNumberRange(values, xindex, parent)]}',
        '</tpl>',
        '</table><img src="images/usgs_black.jpg"/></div>',
        {
        	renderNumberRange: function(values, xindex, displayBins){
        		var nonDetect = '<td colspan="3" style="text-align: right;">&lt;' + this.prettyPrintScientificNotation(values.high) +' <i>(non-detect)</i></td>';
        		var low = '    <td style="text-align: right;">' + this.prettyPrintScientificNotation(values.low) + '</td>';
                var to = '    <td style="text-align: center;"> to </td>';
                var high = '    <td style="text-align: right;">' + this.prettyPrintScientificNotation(values.high) + '</td></tr>';
        		var empty = '    <td style="text-align: right;"></td>';
        		var lt = '    <td style="text-align: right;">&lt;</td>';
        		var gt = '    <td style="text-align: right;">&gt;</td>';

        		if (values.nonDetect) {
        			return nonDetect;
        		} else if (values.lowUnlimited) {
        			return empty + lt + high;
        		} else if (values.highUnlimited) {
        			return empty + gt + low;
        		} else {
        			return low + to + high;
        		}
        		
        	},
            prettyPrintScientificNotation: function(val) {
            	var newVal = ''+val;
            	var eIndx = newVal.indexOf('E');
            	if(eIndx>=0) {
            		newVal = newVal.replace("E", "x10<sup>");
            		newVal += "</sup>";
            	}
            	return newVal;
            }
        }
    );

    // Panel container for the legend template
    var panel = new Ext.Panel({
        autoHeight: true,
        collapsible: true,
        animCollapse: false,
        bodyStyle: 'padding: 0px 5px 5px 5px',
        title: 'EXPLANATION',
        items: [new Ext.DataView({
            store: store,
            tpl: tpl,
            autoHeight: true,
            itemSelector: 'tr.bin'
        }),
        new Ext.Slider({
    		fieldLabel: ' ',
    		labelSeparator: '',
    		layerId: Sparrow.config.LayerIds.mainDataLayerId,
    	    increment: 1,
    	    value: Sparrow.SESSION.getDataLayerOpacity(),
    	    minValue: 0,
    	    maxValue: 100,
    	    listeners: {
    			change: function(s, v) {
    				Sparrow.SESSION.setDataLayerOpacity(v);
    			}
    		}
    	})]
    });
    panel.render(Ext.get('legend'));
    panel.setWidth(panel.body.getWidth());
    panel.items.items[1].setWidth(panel.body.getWidth()-10);
    panel.items.items[1].setValue(Sparrow.SESSION.getDataLayerOpacity());
};