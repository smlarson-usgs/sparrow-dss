Sparrow.index.Controller = Ext.extend(Ext.util.Observable, {
	region : undefined,
	parameter : undefined,
	uuid: undefined,
	modelId: undefined,
	
	/** Constants */
	HISTORY_ITEM_SEPARATOR : ":",
	HISTORY_NAME_VALUE_SEPARATOR : "=",
	
	/** URL key to encode a region or state into the url */
	REGION_KEY : "region",
	
	/** URL key to encode a constituent (Nitrogen, Phosh., into the URL. */
	PARAMETER_KEY : "const",
	
	/** Event fired when the user selects a specific model. */
	METADATA_KEY : "uuid",
	
	/** Event fired when the page is loaded w/ no model selected. */
	NO_MODEL_ID_KEY : "nomodelid",
	
	/** Name of url key name and event fired when a specific model is loaded at startup from the url. */
	MODELID_KEY : "modelid",
	
	/**
	 * Name of event fired when the filter criteria changes.
	 * This will happen after the user makes a selection or when the page loads
	 * to let components know to init their state to match the state of the Controller.
	 */
	FILTER_CHANGED_KEY : "filter_change",	
	
	/**
	 * Key used to store selection history as a session cookie.
	 * This is required so that users returning from the mapping page to the index
	 * page will have the same filters maintained.
	 * 'HISTORY'  is the name used by EXT to store this same info in the url -
	 * it doesn't actually keep history, only the last selection.
	 */
	FILTER_HISTORY_COOKIE_KEY : "filter_selection",
	
	constructor : function(config) {
		config = config || {};
		
		this.region = config.region;
		this.parameter = config.parameter;
		
		config = Ext.apply({
			
		}, config);
		
		Sparrow.index.Controller.superclass.constructor.call(this, config);
		
		this.addEvents(this.METADATA_KEY, this.NO_MODEL_ID_KEY, this.MODELID_KEY, this.FILTER_CHANGED_KEY);
		
		/**
		 * Called any time the filter is changed.
		 */
		this.on(this.FILTER_CHANGED_KEY, function() {
			var region = this.region;
			var param = this.parameter;

			this.selectValue('region-combo-input', region);
			this.selectValue('constituent-combo-input', param);
			
			var params = this.buildConfig(region, param);
			var output = CSWClient.getQueryResultsAsHTML({query : params});
			
			this.updateModelList(output);
		}, this);
		
		///////
		this.on(this.METADATA_KEY, function(modelUUID) {

			if (modelUUID != null) {
				var htmlOutput = CSWClient.getRecordById({id : modelUUID});
				
				this.updateModelDisplay(htmlOutput, true);
			}

		}, this);
		
		this.on(this.MODELID_KEY, function(modelId) {

			var modelUUID = null;
			
			if (modelId != null) {
				//Find the UUID
				var params = [modelId];
				var fullXml = CSWClient.getQueryResultsAsXML({query : params});
				modelUUID = CSWClient.findModelUUID(fullXml);
			}
			
			if (modelUUID != null) {
				var htmlOutput = CSWClient.getRecordById({id : modelUUID});
				
				this.updateModelDisplay(htmlOutput, true);
			}

		}, this);
		
		this.on(this.NO_MODEL_ID_KEY, function() {

			var htmlOutput = "";
			
			htmlOutput+= '<div class="model-select-default-instructions">';
			htmlOutput+= "<h4>No model selected</h4>";
			htmlOutput+= "<p>Use the filter and selection list to the left to select a model.</p>";
			htmlOutput+= '</div>';
			
			this.updateModelDisplay(htmlOutput, false);


		}, this);
		
		
		
		////
	},
	

	/**
	 * Updates the stored state from the Controller to a cookie and the current url.
	 */
	updateHistory : function() {
		
		
		var tokens = [];
		
		if (this.region != null && this.region != "Any") {
			tokens.push(this.REGION_KEY + this.HISTORY_NAME_VALUE_SEPARATOR + this.region);
		}
		
		if (this.parameter != null && this.parameter != "Any") {
			tokens.push(this.PARAMETER_KEY + this.HISTORY_NAME_VALUE_SEPARATOR + this.parameter);
		}
		
		if (this.modelId != null) {
			tokens.push(this.MODELID_KEY + this.HISTORY_NAME_VALUE_SEPARATOR + this.modelId);
		}
		
		var historyString = tokens.join(this.HISTORY_ITEM_SEPARATOR);
		
		
		var currentHistoryString = Ext.History.getToken();
		if (historyString != currentHistoryString) {
			//Store state to the url as a hash (ie .../url#hash)
			//but only if different (don't cause new browser history entries)
			Ext.History.add(historyString);
		}
		
		//Store state to the criteria cookie
		writeLocalSessionCookie(this.FILTER_HISTORY_COOKIE_KEY, historyString);
	},
	
	/**
	 * Updates the controller state based on either the cookie or the URL.
	 * If filter criteria exists, the appropriate MODELID_KEY or NO_MODEL_ID_KEY
	 * event will be fired.
	 * 
	 * Then the FILTER_CHANGED_KEY event ALWAYS fires.
	 * 
	 * This method will call updateHistory if any state is found to ensure 
	 * that the two state mechanisms (cookie and url) are in sync.
	 */
	readStateFromHistory : function() {
		var historyString = Ext.History.getToken();
		
		if (historyString == null || historyString == "") {
			var cookieVal = getCookieValue(this.FILTER_HISTORY_COOKIE_KEY);
			
			if (cookieVal) {
				historyString = cookieVal;
			}
		}
		
		if (historyString != null && historyString != "") {
			var tokens = historyString.split(this.HISTORY_ITEM_SEPARATOR);
			
			if (tokens.length > 0) {
				for (var i=0; i<tokens.length; i++) {
					var token = tokens[i];
					var keyVal = token.split(this.HISTORY_NAME_VALUE_SEPARATOR);
					
					switch(keyVal[0]) {
						case this.REGION_KEY: this.region = keyVal[1]; break;
						case this.PARAMETER_KEY: this.parameter = keyVal[1]; break;
						case this.MODELID_KEY: this.modelId = keyVal[1]; break;
					}
				}
				
				if (this.modelId) {
					this.fireEvent(this.MODELID_KEY, this.modelId);
				} else {
					this.fireEvent(this.NO_MODEL_ID_KEY);
				}

			}
			
			this.updateHistory();
		} else {
			this.fireEvent(this.NO_MODEL_ID_KEY);
		}
		
		
		//This even should fire even if not filter state found, since that means
		//that no criteria is applied, so now all records should show
		this.fireEvent(this.FILTER_CHANGED_KEY);
	},
	
	selectRegion : function(regionId) {
		if (regionId) {
			
			if (regionId.options) {
				//passed a select DOM object
				regionId = regionId.options[regionId.selectedIndex].value;
			}
			
			this.region = regionId;
			this.updateHistory();
		}
		
		this.fireEvent(this.FILTER_CHANGED_KEY);
	},
	
	selectParameter : function(parameterId) {
		
		if (parameterId) {
			
			if (parameterId.options) {
				//passed a select DOM object
				parameterId = parameterId.options[parameterId.selectedIndex].value;
			}
		
			this.parameter = parameterId;
			this.updateHistory();
		}
		
		this.fireEvent(this.FILTER_CHANGED_KEY);
	},
	
	updateLayout : function() {
		this.updateModelLayout();
	},
	
	updateModelLayout : function() {
		var centerPanel = Ext.getCmp('main-layout-center-panel');
		
		//Not yet defined during init load
		if (centerPanel.rendered) {
			var centerPanelHeight = centerPanel.getInnerHeight();
			var upperPanelHeight = Ext.getCmp('documentation-panel').getHeight();
			var availableHeight = centerPanelHeight - upperPanelHeight;
			var outputContainer = Ext.getCmp('model-display-panel');
			outputContainer.setHeight(availableHeight);
			centerPanel.doLayout();
		}
	},
	
	/**
	 * UUID of the ID of the model.
	 * 
	 * @param id The fileIdentification id, which is a CSW UUID.
	 */
	selectUUID : function(id) {

		if (id) {
			
			this.fireEvent(this.METADATA_KEY, id);
			this.uuid = id;
			
			//Right now we are picking the model ID out of an id'ed html element
			//that is built from the returned xml.  Kind of inefficient, but
			//due to the event structure there is not a good way to get the
			//id from the xml w/o creating a reverse dependency.
			var modelIdEl = Ext.getDom("sparrow_model_id_value");
			this.modelId = modelIdEl.innerHTML;
			this.updateHistory();
		}
		
		
	},
	
	
	/**
	 * Called by the XSLT generated 'start the DSS on a watershed' button.
	 */
	launchModelWatershed : function() {
		var modelUrl = document.getElementById('select-watershed').value;
		
		location = modelUrl;
	},
	
	/* Util */ 
	updateModelList : function(htmlContent) {
		var outputComp = document.getElementById('csw-output');
		outputComp.innerHTML = htmlContent;
		
		//Highlight the change
		var animators = [];
		animators[0] = Animator.apply("search-results-container", ["highlite-w", "highlite-b"], {duration: 100});
		animators[1] = Animator.apply("search-results-container", ["highlite-b", "highlite-w"], {duration: 300});
		animators[2] = Animator.apply("search-results-container", ["highlite-w", "highlite-b"], {duration: 100});
		animators[3] = Animator.apply("search-results-container", ["highlite-b", "highlite-w"], {duration: 300});
		animators[4] = Animator.apply(
				"search-results-container",
				["highlite-w", "highlite-w"],
				{
					duration: 10, 
				    onComplete: function() {
				    	document.getElementById("search-results-container").removeAttribute('style');
				    }
				});

		var animation = new AnimatorChain(animators);
		animation.play();
	},
	
	selectValue : function(selectId, value) {
		var sel = document.getElementById(selectId);
		for(var index = 0; index < sel.options.length; index++) {
			if(sel[index].value == value) {
				sel.selectedIndex = index;
				break;
			}
		}
	},
	
	buildConfig : function(region, param) {
		var searchConfig = [];
		if (region && region != 'Any' && region != '') {
			searchConfig.push(region);
		}
		if (param && param != 'Any' && param != '') {
			searchConfig.push(param);
		}
		return searchConfig;
	}, 
	
	updateModelDisplay : function(htmlContent, highlight) {
		var outputComp = document.getElementById('metadata');
		outputComp.innerHTML = htmlContent;
		
		if (highlight) {
			//Implement later - seems to block the model list animation if
			//both play together.
		}
	}
});