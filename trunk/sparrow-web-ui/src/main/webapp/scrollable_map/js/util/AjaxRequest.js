/** AjaxRequest encapsulates AJAX functionality into a javascript class.
 * The aim of this is to make it easier for a developer to handle
 * data in the form of TEXT or XML from an asynchronous AJAX, or a synchronous
 * call from the XMLHttpRequest object.
 *
 * @constructor
 */
JMap.util.AjaxRequest = function(params) {
	if (window.XMLHttpRequest) {
		this._request = new XMLHttpRequest();  //for cool people
		if (params) this.asyncRequest(params);
	} else if (window.ActiveXObject) {
		this._request = new ActiveXObject("Microsoft.XMLHTTP");	//for IE users
		if (params) this.asyncRequest(params);
	} else
		return null;
}


/**
 * Cancels the Ajax request.
 */
JMap.util.AjaxRequest.prototype.abort = function() {
	this._response = null;
	this._onDataRetrieved = null;
	this._request.abort();
}



/**
 * asyncRequest is called when you want to make an ASYNCHRONOUS request
 * to the XMLHttpRequest object.  This is also known as AJAX.  The javascript
 * continues to run and DOES NOT wait for data to be returned.
 *
 * @param {JSON} params	JSON object specifying method, url, param string, and onData callback.
 * @param {String} method	HTML method to GET or POST parameters to a page.
 * @param {String} url		URL to request data from.
 * @param {String} paramString	parameters to GET/POST from/to that URL.  Should take the form: "param1=x1&param2=x2&..."  NOTICE: NO LEADING "?"!!
 * @param {function} onData	this is the callback function the coder wishes to execute once data is returned from the AJAX request.
 */
JMap.util.AjaxRequest.prototype.asyncRequest = function(params) {

	var method = params.method;
	var url = params.url;
	var paramString = params.paramString;
	var onData = params.onData;
	
	this.url = url;

	//this is the function you want to be called once the data is returned
	if (onData) this.onData = onData;

	//determines whether you want to post or get the parameters
	method ? method = method.toUpperCase() : method = "GET";

	//the url to request from
	var fullURL = url;

	if (method =="GET") {
		
		//stick the parameters onto the end of the URL
		if (params) fullURL += "?" + paramString;

		//this is the textbook AJAX call
		this._request.open(method, fullURL, true);

		//ANOTHER IE bug... have to set onreadystatechange after open so we can reuse _request
		//this allows the call back to know see the instance of AjaxRequest that called it.
		var _this = this;
		this._request.onreadystatechange = function() {if (_this._onDataRetrieved) _this._onDataRetrieved()};

		//we aren't POSTing any parameters so there's nothing to send.
		this._request.send(null);   

	} else if (method == "POST") {

		//here is the AJAX call
		this._request.open(method, fullURL , true);

		//ANOTHER IE bug... have to set onreadystatechange after open so we can reuse _request
		//this allows the call back to know see the instance of AjaxRequest that called it.
		var _this = this;

		//if there are any parameters, we post them here via the send method.
		if (params) {

			//set some request headers for our POSTed parameters
			this._request.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
			this._request.setRequestHeader('Content-length', paramString.length);
			this._request.setRequestHeader('Connection', 'close');		
			this._request.onreadystatechange = function() {if (_this._onDataRetrieved) _this._onDataRetrieved()};

			this._request.send(params);
		} else {

			//no parameters specified
			this._request.send(null);
		}
	}
}




/**
 * syncRequest is called when you want to make a SYNCHRONOUS request
 * to the XMLHttpRequest object. The JAVASCRIPT STOPS EXECUTION UNTIL DATA IS
 * IS RETURNED FROM THE REQUEST, that's why there is not call back function.  NOTICE: This will cause the browser to freeze 
 * until data is retrieved.
 *
 * @param {JSON} params	JSON object specifying method, url, and param string
 * @param {String} method	HTML method to GET or POST parameters to a page.
 * @param {String} url		URL to request data from.
 * @param {String} paramString	parameters to GET/POST from/to that URL.  Should take the form: "param1=x1&param2=x2&..."  NOTICE: NO LEADING "?"!!
 */
JMap.util.AjaxRequest.prototype.syncRequest = function(params) {

	var method = params.method;
	var url = params.url;
	var paramString = params.paramString;
	
	//determines whether you want to post or get the parameters
	method ? method = method.toUpperCase() : method = "GET";

	//the url to request from
	var fullURL = url;

	if (method =="GET") {
		//HTML method is GET

		//stick the parameters at the end of the url
		if (params) fullURL += "?" + paramString;

		//this is the synchronous call that freezes up your browser while it waits
		//for data to return
		this._request.open(method, fullURL, false);

		//we aren't POSTing any parameters so there's nothing to send.
		this._request.send(null);   

	} else if (method == "POST") {
		//HTML method is POST

		//this is the synchronous call that freezes up your browser while it waits
		//for data to return
		this._request.open(method, fullURL , false);

		//if there are any parameters, we post them here via the send method.
		if (paramString) {

			//set some request headers for our POSTed parameters
			this._request.setRequestHeader("Cache-Control","no-store");
			this._request.setRequestHeader('Content-type', 'application/x-www-form-urlencoded');
			this._request.setRequestHeader('Connection', 'close');		
			this._request.setRequestHeader('Content-length', paramString.length);
			this._request.send(paramString);
		} else {

			//no parameters specified
			this._request.send(null);
		}
	}

	//perform the call back since the request call has by now returned.
	if (this._onDataRetrieved) this._onDataRetrieved();
}




/**
 * _onDataRetrieved is called by both async and sync requests.  This call reads the
 * returned data from the XMLHttpRequest instance and puts it in to a string.
 * If there is a callback function specified (only from an AJAX (async) request),
 * the callback is executed here.
 */
JMap.util.AjaxRequest.prototype._onDataRetrieved = function() {
	if (this._request.readyState == 4) {
		if (this._request.status == 200) {

			//get the data in String form from request
			this._response = this._request.responseText;

			//the onData callback (FOR AJAX (async) CALLS ONLY)
			if (this.onData) {
				this.onData(this);
				this.onData = null;
			}

		} else {

			//there was an error retrieving the data, there are several-
			//see: http://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
			this._response = "An error occurred retrieving data: " + this._request.status;

		}
	}
}






/**
 * getReponseText returns the data retrieved from the request in the form of a 
 * String.
 *
 * @return	the response from the XMLHttpRequest request as a String.
 */
JMap.util.AjaxRequest.prototype.getResponseText = function() {
	return this._response;
}





/**
 * getResponseXML transforms the data returned from the request into an XML
 * document.  Make sure it is valid XML returned in the first place.
 *
 * @return	data retrieved from request in form of an XML document.
 */
JMap.util.AjaxRequest.prototype.getResponseXML = function() {

	var theDocument = false;	//this will be the XML document

	if (window.ActiveXObject) {		//for IE users......

		try {

			//parse the request String to form an XML document.
			theDocument = new ActiveXObject("Microsoft.XMLDOM");
			theDocument.async = false;
			theDocument.loadXML(this._response);

		} catch (e) {

			//oops. something went wrong in your parsing.		
			//alert("Error parsing XML of Ajax response!.\n" + this._response);
			alert("Error parsing XML of Ajax response.");
			return false;
		}
	} else if (window.XMLHttpRequest) {		//for everyone else

		try {

			//parse the request String to form an XML document.
			var parser = new DOMParser();        
			theDocument = parser.parseFromString(this._response, "text/xml");

		} catch (e) {
			//oops. something went wrong in your parsing.
			//alert("Error parsing XML of Ajax response!.\n" + this._response); some problem with ie on background map request parsing
			alert("Error parsing XML of Ajax response.");

			return false;
		}

	} 

	return theDocument;
}
