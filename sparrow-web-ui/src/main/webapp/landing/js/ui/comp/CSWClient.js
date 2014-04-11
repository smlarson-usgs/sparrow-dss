/*
 *
 * Large portions of the logic have been unknowingly contributed by:
 * 
 *
 * File : CSWClient.js
 * Author : Rob van Swol
 * Organisation: National Aerospace Laboratory NLR
 * Country : The Netherlands
 * email : vanswol@nlr.nl
 * Description: Simple AJAX based CSW client 
 * Tested on : FireFox 3, Safari, IE 7
 * Last Change : 2008-10-22
 * 
 * The original tool can be found at:
 * http://gdsc.nlr.nl/gdsc/en/tools/excat/excat_download_and_installation
 * 
 */

Sparrow.index.CSWClient = function() {

    var HTML_MIME_TYPE = "text/html";
    var XML_MIME_TYPE = "text/xml";

    var GEONETWORK_HOST = "sparrow_geonetwork";

    function loadDocument(uri) {

        var xml = Sarissa.getDomDocument();
        var xmlhttp = new XMLHttpRequest();
        xml.async = false;
        xmlhttp.open("GET", uri, false);
        xmlhttp.send('');
        xml = xmlhttp.responseXML;
        return xml;
    }

    function setXpathValue(_a, _b, _c) {

        var _e = _a.selectSingleNode ? _a.selectSingleNode(_b) : _a.evaluate(_b, _a).iterateNext();

        if (typeof _c === 'string') {
            if (_e) {
                if (_e.firstChild) {
                    _e.firstChild.nodeValue = _c;
                } else {
                    var dom = Sarissa.getDomDocument();
                    var v = dom.createTextNode(_c);
                    _e.appendChild(v);
                }
                return true;
            } else {
                return false;
            }
        } else if (_c.firstChild) {
            Sarissa.copyChildNodes(_c.firstChild, _e);
        }

        return false;
    }

    function parseXml(xmlString) {
        var result;
        if (window.ActiveXObject) {
            // IE
            result = new ActiveXObject('Msxml2.DOMDocument.6.0');
            result.loadXML(xmlString);
        } else {
            result = (new DOMParser()).parseFromString(xmlString, XML_MIME_TYPE);
        }
        return result;
    }


    function getCSWbyQueryAsHTML(config) {
        config.host = config.host + "/query";
        config.accept = HTML_MIME_TYPE;
        return sendCSWRequest(config);
    }

    function getCSWbyIDasHTML(config) {
        config.host = config.host + "/byid";
        config.accept = HTML_MIME_TYPE;
        return sendCSWRequest(config);
    }

    /**
     * Sends a request and returns the results, which may be text or xml, 
     * depending on the config.accept mime type.
     * 
     * config parameter values:
     * config.host: The url to send the request to, may be relative, eg just 'service'
     * config.request: I think this is a string passed in the body of the request
     * config.accept:  The mime type you expect back.  HTML_MIME_TYPE or XML_MIME_TYPE are expected.
     * 
     */
    function sendCSWRequest(config) {
        config = config || {};

        if (config.host && config.request) {
            var host = config.host;
            var params = config.request;

            var xml = Sarissa.getDomDocument();
            xml.async = false;
            var xmlhttp = new XMLHttpRequest();

            xmlhttp.open("POST", host, false);
            xmlhttp.setRequestHeader("Content-type", "application/xml");
            xmlhttp.setRequestHeader("Content-length", params.length);
            xmlhttp.setRequestHeader("Connection", "close");

            if (config.accept) {
                xmlhttp.setRequestHeader("Accept", config.accept);
            }
            xmlhttp.send(params); // POST


            if (config.accept == HTML_MIME_TYPE) {
                xml = xmlhttp.responseText;	//text string
            } else if (config.accept == XML_MIME_TYPE) {
                xml = xmlhttp.responseXML; //xml document
            } else {
                xml = xmlhttp.responseText;	//Don't know - assume text
            }

            return xml;
        } else {
            return undefined;
        }
    }

    return function(config) {
        config = config || {};

        var getRecordsReq = config.getRecordsRequestXsl || "landing/js/excat/xsl/getrecords.xsl";

        var getRecordByIdReq = config.getRecordByIdRequestXsl || "landing/js/excat/xsl/getrecordbyid.xsl";
        var getModelIdFromResp = config.getModelIdFromResponseXsl || "landing/js/excat/xsl/find_model_id.xsl";

        var prettyXmlResp = config.XmlResponseXsl || "landing/js/excat/xsl/prettyxml.xsl";

        var defaultsPath = config.defaultsXml || "landing/js/excat/xml/defaults.xml";
        var cswhost = config.cswhost || GEONETWORK_HOST;

        this.getrecords_xsl = loadDocument(getRecordsReq);
        this.getrecordbyid_xsl = loadDocument(getRecordByIdReq);
        this.defaults_xml = loadDocument(defaultsPath);
        this.getModelIdFromRespStylesheet = loadDocument(getModelIdFromResp);
        this.xmlStylesheet = loadDocument(prettyXmlResp);
        this.defaultschema = this.defaults_xml.selectSingleNode ?
                this.defaults_xml.selectSingleNode("/defaults/outputschema/text()").nodeValue :
                this.defaults_xml.evaluate("/defaults/outputschema/text()", this.defaults_xml).iterateNext().data;

        this.host = cswhost;

        /* =getQueryResultsAsHTML */
        this.getQueryResultsAsHTML = function(config) {
            config = config || {};

            var start = config.start || 1;
            var sortby = config.sortBy || "title";

            var property = "keyword";
            /*because geonetwork doen not follow the specs*/
            if (cswhost.indexOf(GEONETWORK_HOST) != -1 & property == "anytext") {
                property = "any";
            }

            var query = '';
            var literals = config.query || ["National"];
            for (var i = 0; i < literals.length; i++) {
                var literal = literals[i];
                if (literal && literal !== '') {
                    query += '<constraint><propertyname>';
                    query += property;
                    query += '</propertyname><literal>';
                    query += literal;
                    query += '</literal></constraint>';
                }
            }


            query = '<query>' + query + '</query>';
            query = parseXml(query);
            setXpathValue(this.defaults_xml, "/defaults/query", query);


            var schema = config.schema || this.defaultschema;
            setXpathValue(this.defaults_xml, "/defaults/outputschema", schema + '');
            setXpathValue(this.defaults_xml, "/defaults/startposition", start + '');
            setXpathValue(this.defaults_xml, "/defaults/sortby", sortby + '');

            var processor = new XSLTProcessor();
            processor.importStylesheet(this.getrecords_xsl);

            var request_xml = processor.transformToDocument(this.defaults_xml);
            var request = new XMLSerializer().serializeToString(request_xml);

            var csw_response = getCSWbyQueryAsHTML({host: this.host, request: request});

            return csw_response;
        };

        /* =getQueryResultsAsXML method to get XML records */
        this.getQueryResultsAsXML = function(config) {
            config = config || {};

            var start = config.start || 1;
            var sortby = config.sortBy || "title";

            var property = "keyword";
            /*because geonetwork doen not follow the specs*/
            if (cswhost.indexOf(GEONETWORK_HOST) != -1 & property == "anytext") {
                property = "any";
            }

            var query = '';
            var literals = config.query || ["National"];
            for (var i = 0; i < literals.length; i++) {
                var literal = literals[i];
                if (literal && literal !== '') {
                    query += '<constraint><propertyname>';
                    query += property;
                    query += '</propertyname><literal>';
                    query += literal;
                    query += '</literal></constraint>';
                }
            }


            query = '<query>' + query + '</query>';
            query = parseXml(query);
            setXpathValue(this.defaults_xml, "/defaults/query", query);


            var schema = config.schema || this.defaultschema;
            setXpathValue(this.defaults_xml, "/defaults/outputschema", schema + '');
            setXpathValue(this.defaults_xml, "/defaults/startposition", start + '');
            setXpathValue(this.defaults_xml, "/defaults/sortby", sortby + '');

            var processor = new XSLTProcessor();
            processor.importStylesheet(this.getrecords_xsl);

            var request_xml = processor.transformToDocument(this.defaults_xml);
            var request = new XMLSerializer().serializeToString(request_xml);

            var csw_response = sendCSWRequest({host: this.host, request: request, accept: XML_MIME_TYPE});
            var results = "<results><request start=\"" + start + "\"";
            results += " maxrecords=\"";
            results += this.defaults_xml.selectSingleNode ?
                    this.defaults_xml.selectSingleNode("/defaults/maxrecords/text()").nodeValue :
                    this.defaults_xml.evaluate("/defaults/maxrecords/text()", this.defaults_xml).iterateNext().data;
            results += "\"/></results>";
            var results_xml = parseXml(results);

            var importNode = results_xml.importNode(csw_response.documentElement, true);
            results_xml.documentElement.appendChild(importNode);

            return results_xml;

        };

        /* =getRecordById */
        this.getRecordById = function(config) {
            config = config || {};

            if (config.id) {
                var id = config.id;

                var schema = config.schema || this.defaultschema;

                setXpathValue(this.defaults_xml, "/defaults/outputschema", schema + '');
                setXpathValue(this.defaults_xml, "/defaults/id", id + '');

                var processor = new XSLTProcessor();
                processor.importStylesheet(this.getrecordbyid_xsl);

                var request_xml = processor.transformToDocument(this.defaults_xml);
                var request = new XMLSerializer().serializeToString(request_xml);

                var csw_response = getCSWbyIDasHTML({host: this.host, request: request});
                return csw_response;
            }

        };

        this.findModelUUID = function(singleModelXmlResponse) {

            var processor = new XSLTProcessor();
            processor.importStylesheet(this.getModelIdFromRespStylesheet);

            var xmlDom = processor.transformToDocument(singleModelXmlResponse);
            var serializer = new XMLSerializer();
            var output = serializer.serializeToString(xmlDom.documentElement);

            var uuid = xmlDom.documentElement.firstChild.firstChild.nodeValue;



            return uuid;

        };
    };
}();