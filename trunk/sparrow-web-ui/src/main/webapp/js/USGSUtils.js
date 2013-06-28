/**
 * USGS Utility Functions
*/
function Utils(){};
Utils.prototype = {
	/**
	 * Reverses the order of an array
	 */
	reverse : function(anArray){
		var result = [];
		var size = anArray.length;
		for (var i=anArray.length - 1; i>= 0; i--){
			result.push(anArray[i]);
		}
		return result;
	},

	/**
	* Returns first item with matching "attribute" value, null if not found.
	*/
	findFirst : function(anArray, attribName, value){
		for (var i=0; i<anArray.length; i++){
			if (anArray[i][attribName] == value) return anArray[i];
		}
		return null;
	},

	/**
	* Returns array of items with matching "attribute" value, empty array if not found.
	*/
	findAll : function(anArray, attribName, value){
		var result=[];
		for (var i=0; i<anArray.length; i++){
			if (anArray[i][attribName] == value) result.push(anArray[i]);
		}
		return result;
	},

	/**
	 * Returns an array containing the first item with matching "attribute" value,
	 * and removes the item from the original array, null if not found.
	 */
	removeFirst : function(anArray, attribName, value){
		for (var i=0; i<anArray.length; i++){
			if (anArray[i][attribName] == value) return anArray.splice(i, 1);
		}
		return null;
	},

	/**
	 * Parse out URL parameter using javascript, returning empty string if not found
	 */
	getURLParam : function(paramKey, strHref){
		strHref = strHref ||window.location.href;
		var pos = strHref.indexOf("?");
		if ( pos > -1 ){
			var queryString = strHref.substr(pos + 1);
			var queryParts = queryString.split("&");
			for ( var i=0; i<queryParts.length; i++ ){
				if (queryParts[i].indexOf(paramKey + "=") == 0 ){
					var value = queryParts[i].split("=")[1];
					return unescape(value.replace(/\+/g,' '));
				}
			}
		}
		return "";
	},


	/**
 	 * Takes a JSON object and turns it into OUR dialect of XML.
 	 */
	JSONtoXML : function(json) {
		// Hide the auxiliary functions
		function JSONtoXMLRec(key, value) {
			var xml = [];

			if (isArray(value)) { //handle arrays in JSON
				for (var i = 0; i < value.length; i++) {
					xml.push(JSONtoXMLRec(key, value[i]));
				}
			} else {

				xml.push("<", key);
				//look for attributes and close tag
				if (isJSONObject(value)) {
					for (var x in value) {
						if (isAttribute(x)) {
							xml.push(" ", x.substring(1,x.length), '="', value[x], '"');
						}
					}
				}
				xml.push(">");

				if (isLiteral(value)) {  //handle a literal
					xml.push(value);
				} else if (isJSONObject(value)) {  //handle another json object
					for (var x in value) {
						if (!isAttribute(x) && !isText(x)) {
							xml.push(JSONtoXMLRec(x, value[x]));
						} else if (isText(x)) {
							xml.push(value[x]);
						}
					}
				}
				xml.push("</", key, ">");
			}
			return xml.join("");
		};

		/**
		 * Is this an element text content in our XML-flavored JSON?
		 */
		function isText(key) {
			return (key.toLowerCase() == '#text');
		}

		/**
		 * Is this an attribute in our XML-flavored JSON
		 */
		function isAttribute(key) {
			return (key.indexOf("@") == 0);
		}

		/**
		 * Convenience method to see whether this is a javascript/JSON array
		 */
		function isArray(obj) {
			return Ext.isArray(obj);
		}

		function isJSONObject(obj) {
			return (!isLiteral(obj) && !isArray(obj) && typeof(obj) == 'object');
		}

		/**
		 * Is this a javascript literal?
		 * literals can't have properties assigned to them
		 */
		function isLiteral(val) {
			if (!val) return true;
			val.newProperty = 'test';
			var test = !!!val.newProperty;
			delete val.newProperty;
			return test;
		}
		// END of auxiliary functions

		// code
		var xml = [];
		for (var x in json) {
			xml.push(JSONtoXMLRec(x,json[x]));
		}
		return xml.join("");
	},

	// some util function for IE
	// Is this actually used?
	createElement: function(type, name) {
	   var element = null;

	   try {
	      // First try the IE way; if this fails then use the standard way
	      element = document.createElement('<'+type+' name="'+name+'">');
	   } catch (e) {
	      // Probably failed because we�re not running on IE
	   }
	   if (!element) {
	      element = document.createElement(type);
	      element.name = name;
	   }
	   return element;
	},

	/**
	 * Inserts one array into another beginning at specified index. Returns a copy.
	 */
	insertArrayAt: function(/* array */ insertInto, /* array */ toInsert, index){
		var result = [];
		if (index > 0) result = result.concat(insertInto.slice(0, index));
		result = result.concat(toInsert);
		return result.concat(insertInto.slice(index));
	},
	
	prettyPrintUnitsForGoogleQueryParam: function(units){
		switch(units) {
			case "ft³⋅sec⁻¹":
			case "feet³ ⋅ second⁻¹":
				return 'ft³/sec';

			case "ft⋅sec⁻¹":
			case "feet ⋅ second⁻¹":
				return 'ft/sec';

			case "kg⋅year⁻¹":
			case "kg ⋅ year⁻¹":
				return 'kg/year';

			case "mg⋅L⁻¹":
			case "mg ⋅ L⁻¹":
				return 'mg/L';

			case "kg⋅km⁻²⋅yr⁻¹":
			case "kg ⋅ km⁻² ⋅ year⁻¹":
				return 'kg/km²year';

			case "ppm⋅km²":
			case "parts per million ⋅ km²":
				return 'ppm/km²';

			default: return units;
		}
	},
	
	prettyPrintUnitsForHtml: function(units){
		var htmlUnits = units;
		
		if(Ext.isIE7 || Ext.isIE6) {
			//replace interpunct
			htmlUnits = htmlUnits.replace(/⋅/g, "&middot;");
			
			//replace superscripted text with superscript wrapped regular text
			htmlUnits = htmlUnits.replace(/⁻/g, "<sup>-</sup>");
			htmlUnits = htmlUnits.replace(/¹/g, "<sup>1</sup>");
			htmlUnits = htmlUnits.replace(/²/g, "<sup>2</sup>");
			htmlUnits = htmlUnits.replace(/³/g, "<sup>3</sup>");
		}
		
		return htmlUnits;
	}
}

//Establish USGS Global
Sparrow.USGS = new Utils();

Sparrow.COLORS = {
	POSITIVE_HUES : [ null,
	                  ['FEC44F'], 
	                  ['FEE391', 'EC7014'], 
	                  ['FFFFD4','FEC44F','CC4C02'], 
	                  ['FFFFD4','FEE391','FE9929','CC4C02'], 
	                  ['FFFFD4','FEE391','FEC44F','FE9929','EC7014'] 
	                ],

	NEGATIVE_HUES : [null,
	                 ['41B6C4'], 
	                 ['225EA8', '41B6C4'], 
	                 ['0C2C84', '1D91C0', '7FCDBB'], 
	                 ['225EA8', '1D91C0', '41B6C4', '7FCDBB'] , 
	                 ['0C2C84', '225EA8', '1D91C0', '41B6C4', '7FCDBB'] 
	                ],

	/**
	 * Returns an array of equally spaced post values between
	 * the from and to RGB values. Note the number of bins
	 * between is ()#post + 1)
	 */
	interpolateRGB: function(fromRGB, toRGB, post){
		if (post <= 0) return null; // no new posts needed
		fromRGB = Ext.isString(fromRGB)? this.decomposeRGB(fromRGB): fromRGB;
		toRGB = Ext.isString(toRGB)? this.decomposeRGB(toRGB): toRGB;
			var step={}, total={};
		for (var color in fromRGB){
			total[color] = parseInt(fromRGB[color],16);
			step[color] = Math.ceil((parseInt(toRGB[color],16) - total[color])/(post+1));
		}
			var result = [];
		for (var i=0; i<post; i++){
			var val="";
			for (var color in fromRGB){
				total[color] = total[color] +  step[color];
				var part = Math.ceil(total[color]).toString(16);
				if (part.length == 1) part = "0" + part;
				val += part;
			}
			result.push(val);
		}
		return result;
	},

	/**
	 * Decomposes an RGB string value into its component
	 * R, G, B string parts and number values
	 */
	decomposeRGB: function(value){
		return {
			R: value.substr(0,2),
			G: value.substr(2,2),
			B: value.substr(4,2)
		};
	},

	/**
	 * Given an array of colors, performs evenly spaced interpolations to get
	 * a total of <code>count</code> colors.
	 */
    interpolateColors: function(/** array of RGB values */ colors, count){
    	// assert(colors.length > 1);
    	if (colors.length >= count) return colors;

    	var numOfBaseColors = colors.length;
    	var remainder = count - numOfBaseColors;
    	for (var i=numOfBaseColors - 1; i>0; i--){
    		if (remainder == 0) break;
    		var numNeeded = Math.ceil(remainder/i);
    		var interpolations = Sparrow.COLORS.interpolateRGB(colors[i-1], colors[i], numNeeded);
    		colors = Sparrow.USGS.insertArrayAt(colors, interpolations, i);
    		remainder -= numNeeded;
    	}
    	return colors;
    }
}


//The following is a temporary logging hack. I don't want to add any dependencies right now
//as I haven't had time to investigate more interesting logging frameworks.
//See http://log4javascript.org/
//http://www.alistapart.com/articles/jslogging
//http://ajaxpatterns.org/Javascript_Logging_Frameworks
//http://www.gscottolson.com/blackbirdjs/
//http://monket.net/wiki-v2/Javascript_Logging
//The leading candidates seem to be log4javascript and blackbird

Sparrow.LOG = new (function(){
	this.doLogging = false;
	this.doAlerts = false;
	this.log = function(message){
		if (this.doLogging){
			if (console){
				// We're working in FireFox with Firebug's console logging enabled. Great! Use it.
				console.log(message);
			} else if (this.doAlerts){
				// For IE and non-firebug-enabled firefoxes
				Ext.msg('Warning', message);
			}
		}
	}
})();



