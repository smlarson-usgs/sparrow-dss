Sparrow.config.ComboValues = function(){return{
	/**
	 * this is a convenience map so that indexes are not hardcoded throughout the app
	 */
	dataSeriesIndexOf: {
		type: 0,
		display: 1,
		value:  2,
		group: 3,
		allowPercUnits: 4
	},
	dataSeriesRecordDef : ['type', 'display', 'value', 'group', 'allowPercUnits'], //***if you change this you must update objects which reference it
	/**
	 * both of these arrays should have records in the following format of the ['type', 'display', 'value', 'group', 'allowPercUnits']
	 */
	dataSeries: [
		['source-all', 'Total Load', 'total', '<span style="display:none">1</span>Model Estimates', true],
		['source-all', 'Incremental Load', 'decayed_incremental', '<span style="display:none">1</span>Model Estimates', true],
		['source-all-only', 'Flow-weighted Concentration', 'total_concentration', '<span style="display:none">1</span>Model Estimates', false],
		['source-all', 'Total Yield', 'total_yield', '<span style="display:none">1</span>Model Estimates', true],
		['source-all', 'Incremental Yield', 'incremental_yield', '<span style="display:none">1</span>Model Estimates', true],
		['source-specific', 'Model Input Sources', 'source_value', '<span style="display:none">2</span>Model Inputs', false],
		['source-none', 'Streamflow', 'flux', '<span style="display:none">3</span>Stream Network', false],
		['source-all', 'Standard Error of Total Load', 'total_std_error_estimate', '<span style="display:none">4</span>Model Uncertainty', false],
		['source-all', 'Standard Error of Incremental Load', 'incremental_std_error_estimate', '<span style="display:none">4</span>Model Uncertainty', false],
		['source-all', 'Total Delivered Load', 'total_delivered_flux', '<span style="display:none">5</span>Downstream Tracking', true],
		['source-all', 'Incremental Delivered Load', 'incremental_delivered_flux', '<span style="display:none">5</span>Downstream Tracking', true],
		['source-none', 'Delivery Fraction', 'delivered_fraction', '<span style="display:none">5</span>Downstream Tracking', false],
		['source-all', 'Incremental Delivered Yield', 'incremental_delivered_yield','<span style="display:none">5</span>Downstream Tracking', true]
   ]
	
}}();

/*
 * The various dynamic layers that the application loads from
 * its associated map server instance.
 * Key:
 * id: Unique ID used to track this layer internally.  Do not change the ID,
 *		since predefined scenarios will and stored sessions iwill refer to this number.
 * workspaceName: The name of the workspace on the server.  Often the workspace
 *		of a layer is fixed, but the actual layer name depends on the model or its network.
 * zDepth: Stacking order of the layers.  Smaller number (more negative) are on
 *		top of layers with larger numbers.
 * scaleMin / scaleMax: Scale at which the layer turns on and off.
 *		0/100 is basically always on.
 * title: Used internally as a title - This likely has no function.
 * 
 * Layers are listed below in order with those on top listed first.
 */
Sparrow.config.layers = function(){return {
	reachIdLayer: {
		id: -203600,
		workspaceName: "catchment-overlay", /* This layer is a filtered catchment layer */
		zDepth: -60000,
		scaleMin: 0,
		scaleMax: 100,
		title: "Reach Identification Layer"
	},
	calibrationSiteLayer: {
		id: -203527,
		workspaceName: 'sparrow-calibration',
		zDepth: 59990,
		scaleMin: 0,
		scaleMax: 100,
		title: "Calibration sites overlay"
	},
	huc8Layer: {
		id: -203525,	/* UI Internal unique ID */
		workspaceName: 'huc8-overlay', /* Name of the workspace on GeoServer */
		zDepth: 59994, /* UI Stacking order */
		scaleMin: 0,
		scaleMax: 100,
		title: "HUC8 Overlay"
	},
	reachOverlayLayer: {
		id: -203526,
		workspaceName: 'reach-overlay',
		zDepth: 59995,
		scaleMin: 0,
		scaleMax: 100,
		title: "Reach Overlay"
	},
	mainDataLayer: {
		id: -203528,
		workspaceName: null, /* Specified by Context */
		zDepth: 60000,
		scaleMin: 0,
		scaleMax: 100,
		title: "Predicted data display layer"
	}
}}();

Sparrow.config.GraphColorArray = [
  ["43A2CA", "65C4EC"],
  ["E34A33", "F56C55"],
  ["31A354", "53C576"],
  ["636363", "858585"],
  ["8856A7", "AA78C9"],
  ["1C9099", "3EB2BB"],
  ["B2DF8A", "D4FFAC"],
  ["DD1C77", "FF3E99"],
  ["FEC44F", "FFE66F"],
  ["FA9FB5", "FCBFD7"],
  ["A6D854", "C8FA76"],
  ["D95F0E", "FB7F2F"],
  ["FB8072", "FDA294"],
  ["C2A5CF", "E4C7EF"],
  ["FC9272", "FEB494"],
  ["8DD3C7", "AFF5E9"],
  ["F4A582", "F6C7A4"],
  ["B8E186", "DAF3A8"]
];


/**
 * Extra extjs vtypes
 */

Ext.apply(Ext.form.VTypes, {
	'phone' : function(v) {
		return /^1?-?(\d\d\d)?-?(\d\d\d)-?(\d\d\d\d)$/.test(v);
	},
	'phoneText' : 'Enter a valid phone number, ie: 123-456-7890 (dashes optional)'
});  
