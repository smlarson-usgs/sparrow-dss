/**
 * @param {Object} config:
 *	id - the programmatic id used throughout this app
 *	videoId - the id of the video given to it by it's hosting service
 *	name - the user-facing name
 */
var Screencast = function(config){
	var self = this;
	config = config || {};

	self.id = config.id;
	self.videoId = config.videoId;
	self.name = config.name;

	//delegate to static method
	self.open = function(){
		Screencast.open(self.videoId);
	};
};
//static method:
Screencast.open = function(videoId){
	var newWindow = window.open('screencast.jsp?videoId=' + videoId, '_blank',
	   				'resizable=0,location=0,status=0,scrollbars=0,width=1280,height=780');
		newWindow.focus();
		return newWindow;
};

/**
 * Screencasts is a psuedoimmutable array-hash hybrid. It's an array with an extra
 * method to give it hash-like functionality. The hash functionality is not
 * updated after instantiation -- subsequent modifications to the hybrid array
 * would not be reflected in the hash-like functionality. Accordingly we remove
 * the array instance mutator methods that developers would be most tempted to
 * use, hence "psuedoimmutable."
 *
 * getById method.
 *
 * Example usage:
 *
 * Ext.each(Screencasts, function(screencast){alert(screencast.name);});
 * Screencast.open(Screencasts.getById('incYield'));
 *
 */
var Screencasts = (function(){
	var screencastHybridArray = [];//will be returned
	var screencastIdMap = {};
	var screencastData = [
		//id, name, video id triples:
		['incYield', 'Incremental Yield', '5K1Smu7Q4Fc'],
		['downstreamOutlets', 'Selecting Downstream Outlets' , 'zrycRF7MeG8'],
		['srcInputs', 'Changing Source Inputs' , 'UkC_76uq748'],
		['incYieldToOutlet', 'Incremental Yield to an Outlet' , 'tHnxt2ORNQU'],
		['deliveryReports','Summarizing Delivered Load to Downstream Outlets' , 'HG9S4D0Jjfc']
	];
	var counter;
	for(counter = 0; counter < screencastData.length; counter++){
		var screencastDatum = screencastData[counter];
		var id = screencastDatum[0],
			name = screencastDatum[1],
			videoId = screencastDatum[2];

		var numericalIndex = screencastHybridArray.push(
			new Screencast({
				id: id,
				name: name,
				videoId: videoId
			})
		);
		screencastIdMap[id] = screencastHybridArray[numericalIndex-1];
	};

	/**
	 * Get the Screencast by id
	 *
	 * @param {string} id - the programmatic identifier for the screencast
	 * @returns {Screencast} - the corresponding Screencast object if present, else undefined
	 */
	screencastHybridArray.getById = function(id){
		return screencastIdMap[id];
	};

	//now remove the mutator methods

	//enumerate the mutator methods
	var mutatorMethodNames = [
		'pop',
		'push',
		'reverse',
		'shift',
		'sort',
		'splice',
		'unshift'
	];
	var warning = function(){
		throw	'The array mutator methods have been intentionally removed. '+
				'See the Screencasts.js class docs for explanation.';
	};
	for(counter = 0; counter < mutatorMethodNames.length; counter++){
		var mutatorMethodName = mutatorMethodNames[counter];
		screencastHybridArray[mutatorMethodName] = warning;
	};
	return screencastHybridArray;
}());

