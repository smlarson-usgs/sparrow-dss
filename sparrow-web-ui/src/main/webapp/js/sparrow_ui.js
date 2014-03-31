Sparrow.ui = function() { return{
	loadSessionName : Sparrow.USGS.getURLParam("session")
	
	, loadUserSession : false
	
	/**
	 * Called when the iFrame used to load a predefined session is created or
	 * actually loaded to (ie, it will be called at startup w/ no data).
	 */
	, load_ui : function(iframe) {
	    document.getElementById('gps-area').innerHTML = '';
	    var ibody = iframe.contentWindow.document.body;
	    var ipre = "";
	    if (ibody) ipre = ibody.getElementsByTagName('pre');
	
	    if (ipre.length > 0) {
	    	var sessionJSON = ipre[0].innerHTML;
	    	Sparrow.ui.loadUserSession = true;
	    	Sparrow.ui.render_ui(sessionJSON);
	    }
	}
	
	, render_ui : function(SESSION_txt) {
	    Sparrow.SESSION.load(SESSION_txt);
	    Sparrow.SESSION.convertOldBinningDataToNew();
	    Sparrow.SESSION.convertOldOverlaysToNew();
	
	    model_id = Sparrow.SESSION.PredictionContext["@model-id"]; //TODO
		edaCodeStore.load({params: {model: model_id}});
		edaNameStore.load({params: {model: model_id}});
	
	    getModel();
	
	    // Get the values from the PredictionContext first, since selecting
	    // options from the dropdowns may fire events that change the context
	    var pc = Sparrow.SESSION.PredictionContext;
	    var ms = Sparrow.SESSION.PermanentMapState;
	
	    var dataSeries = pc.analysis.dataSeries["#text"];
	    var source = pc.analysis.dataSeries["@source"];
	    var comparison = pc.nominalComparison["@type"];
	    var bucketType = Sparrow.SESSION.getBinTypeName();
	    var bucketCount = Sparrow.SESSION.getBinCount();
	    var whatToMap = ms["what_to_map"];
	    var aggFunction = Sparrow.SESSION.PredictionContext.analysis.groupBy["@aggFunction"];
	
	    // Set the appropriate map options controls
	    var mapOptionsTab = Ext.getCmp('map-options-tab');
	    mapOptionsTab.setDataSeriesCtl(dataSeries);
	    mapOptionsTab.setSourceCtl(source);
	    mapOptionsTab.setComparisonCtl(comparison);
	    mapOptionsTab.setComparisonBucket(bucketCount + ' ' + bucketType + ' Bins');
	    mapOptionsTab.setWhatToMapCtl(whatToMap);

	    //load map state
	    if (!loadStateBBox || Sparrow.ui.loadUserSession) {
	    	var zoom = Sparrow.SESSION.PermanentMapState.zoom;
		    var lat = Sparrow.SESSION.PermanentMapState.lat;
		    var lon = Sparrow.SESSION.PermanentMapState.lon;
		    map1.jumpTo(lat,lon,zoom - map1.minZoom);
	    }
	
	    //load adjustments
	    Ext.getCmp('main-adjustments-tab').treePanel.loadTree(); 
	    Ext.getCmp('main-targets-tab').treePanel.loadTree(); 
	    
	    //if adjustments are not made, disable the comparisons ui
	    if (Sparrow.SESSION.hasEnabledAdjustments())
		{	
	    	mapOptionsTab.comparisonCombo.enable();
		} else {
			var combo = mapOptionsTab.comparisonCombo; 
	    	combo.setValue('none'); 
	    	combo.disable();
		}
	    
	    Sparrow.ui.loadUserSession = false;
	    
	    Sparrow.SESSION.fireContextEvent('finished-loading-pre-session');
	
	    //add the sparrow data layer to the map
	    make_map();

	}
	
	/*
	 * Adds a new group.
	 *
	 * @editGroup JSON of a group if editing
	 */
	, add_group : function(editGroup) {
	  //get important values from id="name-group-area" children
	  var groupName = document.getElementById("group_name").value;
	  document.getElementById("group_name").value = '';
	  var groupDesc = document.getElementById("group_desc").value;
	  document.getElementById("group_desc").value = '';
	
	  //get important values from id="add-group-tab"  children
	  var groupNotes = document.getElementById("group_notes").value;
	
	  var src_adjs = document.getElementsByName("treatment-tab_src_adj");
	  var treatments_JSON = [];
	  for (var i = 0; i < src_adjs.length; i++) {
	    if (src_adjs[i].value != "1") {
	
	      treatments_JSON.push({
	        "@src" : (i+1),
	        "@coef" : src_adjs[i].value
	      });
	    }
	    src_adjs[i].options[9] = null;
	  }
	
	  if (!editGroup) {
		  Sparrow.SESSION.addGroup(groupName, groupDesc, groupNotes, treatments_JSON);
	  } else {
		//apply adjustments to group
		  Sparrow.SESSION.applyAdjustmentsToGroup(editGroup, treatments_JSON);
		  Sparrow.SESSION.applyNotesToGroup(editGroup, groupNotes);
	  }
	  Sparrow.SESSION.fireContextEvent('changed');
	}
	
	//find the index in the group [] of the passed gp, by matching on JSON
	, get_current_group_index : function(gp) {
	  var groups = Sparrow.SESSION.getAllGroups();
	
	  for (var i = 0; i < groups.length; i++) {
	    if (gp == groups[i]) {
	      return i;
	    }
	  }
	  return -1;
	}
	
	//move group up in the left panel
	, move_group_up : function(gp_id) {
	  var gp = document.getElementById(gp_id);
	  if (gp.previousSibling) {
	    gp.parentNode.insertBefore(gp, gp.previousSibling);
	
	    //move group JSON up too
	    var groups = Sparrow.SESSION.getAllGroups();
	    var i = Sparrow.ui.get_current_group_index(gp.json);
	
	    var t_g = groups[i-1];
	    groups[i-1] = groups[i];
	    groups[i] = t_g;
	  }
	}
	
	//move a group down in the left panel
	, move_group_down : function(gp_id) {
	  var gp = document.getElementById(gp_id);
	  if (gp.nextSibling && gp.nextSibling.nextSibling) {
	    gp.parentNode.insertBefore(gp, gp.nextSibling.nextSibling);
	
	    //move group JSON down too
	    var groups = Sparrow.SESSION.getAllGroups();
	    var i = Sparrow.ui.get_current_group_index(gp.json);
	
	    var t_g = groups[i+1];
	    groups[i+1] = groups[i];
	    groups[i] = t_g;
	
	  } else if (gp.nextSibling) {
	    gp.parentNode.appendChild(gp);
	
	    //move group JSON down too
	    var groups = Sparrow.SESSION.getAllGroups();
	    var i = Sparrow.ui.get_current_group_index(gp.json);
	
	    var t_g = groups[i+1];
	    groups[i+1] = groups[i];
	    groups[i] = t_g;
	
	  }
	}
	
	/*
	 * Duplicates the named group.
	 */
	, duplicateGroup : function(groupName) {
	    var group = Sparrow.SESSION.getGroup(groupName);
	
	    // Duplicate the group
	    var groupStr = Ext.util.JSON.encode(group);
	    var newGroup = Ext.util.JSON.decode(groupStr);
	
	    // Create a new name for the group (for uniqueness)
	    var newGroupName = 'Copy of ' + newGroup["@name"];
	    if (Sparrow.SESSION.groupExists(newGroupName)) {
	        var nameExists = true;
	        for (var i = 2; nameExists; i++) {
	            newGroupName = 'Copy (' + i + ') of ' + newGroup["@name"];
	            nameExists = Sparrow.SESSION.groupExists(newGroupName);
	        }
	    }
	
	    // Add the new group to the model and render
	    newGroup["@name"] = newGroupName;
	    Sparrow.SESSION.PredictionContext.adjustmentGroups.reachGroup.push(newGroup);
	    Sparrow.SESSION.fireContextEvent('adjustment-group-changed');
	}
	
	
	//select a reach group for editing and open the popup to allow it filling out inputs
	//with all appropriate values from the group
	, edit_group : function(gp_id) {
	    gp_json = Sparrow.SESSION.getGroup(gp_id);
	
	  //populate name and desc fields
	  document.getElementById('group_name').value = gp_json["@name"];
	  document.getElementById('group_desc').value = gp_json["desc"];
	
	  //populate adjustment drop downs
	  var sels = document.getElementsByName('treatment-tab_src_adj');
	  var adjs = gp_json["adjustment"];
	  for (var i = 0; i < adjs.length; i++) {
	    sels[(adjs[i]["@src"] - 1)].value = adjs[i]["@coef"];
	    //a custom multiplier was added previously
	    if (sels[(adjs[i]["@src"] - 1)].value != adjs[i]["@coef"]) {
	      sels[(adjs[i]["@src"] - 1)].options[9] = new Option(adjs[i]["@coef"],adjs[i]["@coef"]);
	      sels[(adjs[i]["@src"] - 1)].value = adjs[i]["@coef"];
	    }
	  }
	
	  //fill in notes area
	  document.getElementById('group_notes').value = gp_json["notes"];
	
	  //open window for edit
	  CREATE_GROUP_WIN.open(gp_json["@name"], gp_id);
	
	}
	
	//enable/disable a group
	, toggle_group_on_off : function(groupName, onoff) {
		Sparrow.SESSION.setGroupEnabled(groupName, onoff);
	}
	
	, toggleIndividualGroup : function(onoff) {
		Sparrow.SESSION.setIndividualGroupEnabled(onoff);
	}
	
	, view_group_JSON : function(gp_id) {
	  var gp = document.getElementById(gp_id);
	  Ext.Msg.alert('Warning', Ext.util.JSON.encode(gp.json));
	}
	
	, get_UI_JSON : function() {
		Sparrow.ui.update_SESSION_mapstate();
		return Sparrow.SESSION.asJSON();
	}
	
	, update_SESSION_mapstate : function(){
		Sparrow.SESSION.PermanentMapState["zoom"] = map1.zoom;
		var map_ll = map1.getCenterLatLon();
		Sparrow.SESSION.PermanentMapState["lat"] = map_ll.lat;
		Sparrow.SESSION.PermanentMapState["lon"] = map_ll.lon;
	}
	
	//save the SESSION to a file on local machine
	, save_map_state : function(asXML) {
		
	  if (SAVE_AS_WIN.close()) {
		Sparrow.ui.update_SESSION_mapstate();
	
	    if (asXML) {
	      document.getElementById('savefileas_extension').value = 'xml';
	      document.getElementById('ui_XML').value = Sparrow.SESSION.asSessionXML();
	    } else {
	      document.getElementById('savefileas_extension').value = 'js';
	      document.getElementById('ui_XML').value = Sparrow.SESSION.asJSON();
	      Sparrow.SESSION.asSessionXML();
	    }
	    return true;
	  } else {
	    return false;
	  }
	}
	
	// Prepare export window form
	, setUpExport : function() {
			    var mimetype = 'csv';
			    var exps = document.getElementsByName('export_radio');
			    for (var i = 0; i < exps.length; i++) {
			        if (exps[i].checked) {
			            mimetype = exps[i].value;
			            break;
			        }
			    }
			    var bound = document.getElementById('export_checkbox_bounded').checked;
	
				//User should be warned if their state does not match the map
			    var xmlreq = ''
			        + '<?xml version="1.0" encoding="ISO-8859-1" ?>'
			        + '<sparrow-report-request '
			        + '  xmlns="http://www.usgs.gov/sparrow/prediction-schema/v0_2" '
			        + '  xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"> '
			        + '  <PredictionContext context-id="' + Sparrow.SESSION.getMappedOrValidContextId() + '"/> '
			        ;
			    if (bound) {
			        xmlreq += '  <bbox>' + map1.getViewportBoundingBoxString() + '</bbox> ';
			    }
	
			    xmlreq += '  <response-content>';
			    
			    
			    //predicted and source values options
			    if (document.getElementById('export-adjusted-predicted-values').checked && Sparrow.SESSION.hasEnabledAdjustments()) {
			        xmlreq += '<adjusted-predicted-values/>';
			    }
			    
			    if (document.getElementById('export-adjusted-source-values').checked && Sparrow.SESSION.hasEnabledAdjustments()) {
			        xmlreq += '<adjusted-source-values/>';
			    }
			    
			    if (document.getElementById('export-original-predicted-values').checked) {
			        xmlreq += '<original-predicted-values/>';
			    }
			    
			    if (document.getElementById('export-original-source-values').checked) {
			        xmlreq += '<original-source-values/>';
			    }
			    
			    //id and stats attribs
			    if (document.getElementById('export-include-id-attributes').checked) {
			        xmlreq += '    <id-attributes/>';
			    }
			    
			    if (document.getElementById('export-include-stat-attributes').checked) {
			        xmlreq += '    <stat-attributes/>';
			    }
	
			    xmlreq += ''
			        + '  </response-content>'
			        + '  <response-format>'
			        + '    <mime-type>' + mimetype + '</mime-type> '
			        + '  </response-format> '
			        + '</sparrow-report-request>'
			        ;
	
			    document.getElementById('report_xmlreq').value = xmlreq;
	}
	
	, highlightTool : function(node) {
	  var buttons = node.parentNode.getElementsByTagName('a');
	  for (var i = 0; i < buttons.length; i++) {
	    buttons[i].className = '';
	  }
	  node.className = 'selected';
	}
	
	
	, convertFromScientificNotation : function(value){
		// TODO doesn't work properly yet
		var parts = value.split("E");
		if (parts.length == 1) return value;
		return parts[0] * (10^parts[1]);
	}
	
	, toggleLegend : function() {
		var legend = document.getElementById('legend');
		if (legend.style.display == 'block') {
			legend.style.display = 'none';
		} else {
			legend.style.display = 'block';
		}
	}
	
	, parseBinDataResponse : function(xmlResponse) {
        var buckets = xmlResponse.getElementsByTagName('bin');
        
        var type = xmlResponse.getElementsByTagName('entity')[0].getAttribute('binType');
        var displayBins = [];
        var functionalBins = [];
        var boundUnlimited = [];
        var nonDetect = [];
        
        for (var i = 0; i < buckets.length; i++) {
        	var binElm = buckets[i];
        	var topElm = binElm.getElementsByTagName('top')[0];
        	var bottomElm = binElm.getElementsByTagName('bottom')[0];
        	
        	displayBins.push({
                low: bottomElm.getAttribute('formatted'),
                high: topElm.getAttribute('formatted')
            });
        	
        	functionalBins.push({
                low: bottomElm.getAttribute('formattedFunctional'),
                high: topElm.getAttribute('formattedFunctional')
            });
        	
        	boundUnlimited.push({
        		low: bottomElm.getAttribute("unbounded") == 'true',
        		high: topElm.getAttribute("unbounded") == 'true'
        	});
        	
        	nonDetect.push(binElm.getAttribute("nonDetect") == 'true');
        	
        }
        
        var colors = this.generateBucketColors(functionalBins);
        
        var binning = {
    		"displayBins": displayBins,
    		"functionalBins": functionalBins,
    		"binColors": colors,
    		"boundUnlimited": boundUnlimited,
    		"nonDetect": nonDetect
        }
        
        return binning;
	}
	
    /**
     * This is expecting getBinData()['functionalBins'] from the UI Context.
     * @param buckets
     * @returns
     */
    , generateBucketColors: function(functionalBins) {
    	var count = functionalBins.length;
    	var lo = functionalBins[0]['low'];
    	var hi = functionalBins[functionalBins.length - 1]['high'];
    	
        // Three cases:
        //	1. lo<0<hi 
        //  2. 0<=lo
        //  3. hi<=0

    	if (lo < 0 && 0 < hi) {
            // case 1: lo < 0 < hi
            // find bin which brackets zero. For bins below, use negative hues, for bins above, positive hues.
            for (var i=0; i<functionalBins.length; i++){
            	if (functionalBins[i]['high'] >= 0) break;
            }
            var colors = this.useNegativeHues(i);
            colors = colors.concat(this.usePositiveHues(count - i));
            return colors;
    	} else if (0<=lo) {
    		return this.usePositiveHues(count) 
    	} else {
    		return this.useNegativeHues(count);
    	}
        

    }

    /**
     * Returns a palette/array of positive hues
     * @param count
     * @param useGray true if the first returned color should be gray
     */
    , usePositiveHues: function(count){
    	var colors=[];
    	if (count == 0) return colors;
    	if (count < Sparrow.COLORS.POSITIVE_HUES.length) {
    		return colors.concat(Sparrow.COLORS.POSITIVE_HUES[count]);
    	}
    	// otherwise, return interpolated colors
    	return colors.concat(
    			Sparrow.COLORS.interpolateColors(Sparrow.COLORS.POSITIVE_HUES[Sparrow.COLORS.POSITIVE_HUES.length - 1], count));

    }

    /**
     * Returns a palette/array of negative hues
     * @param count
     * @param useGray true if the last returned color should be gray
     */
    , useNegativeHues: function(count){
    	var colors=[];
    	if (count == 0) return colors;
    	colors = []; // reset
    	if (count < Sparrow.COLORS.NEGATIVE_HUES.length) {
    		colors = colors.concat(Sparrow.COLORS.NEGATIVE_HUES[count]);
    	} else {
    		colors = colors.concat(
    				Sparrow.COLORS.interpolateColors(Sparrow.COLORS.NEGATIVE_HUES[Sparrow.COLORS.NEGATIVE_HUES.length - 1], count));
    	}
    	return colors;
    }

}}();
