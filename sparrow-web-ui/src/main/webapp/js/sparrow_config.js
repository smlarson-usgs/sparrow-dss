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

Sparrow.config.LayerIds = function(){return {
	huc8LayerId: -203525,
	reachLayerId: -203526,
	calibrationSiteLayerId: -203527,
	mainDataLayerId: -203528,
	reachIdLayerId: -203600
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
