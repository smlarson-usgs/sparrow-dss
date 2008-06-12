package gov.usgswim.service;

import gov.usgswim.ThreadSafe;
import gov.usgswim.service.pipeline.PipelineRequest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URLDecoder;

import javax.servlet.http.HttpServletRequest;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.lang.StringUtils;


/**
 * An abstract implementation of HttpRequestParser provides utility methods to
 * find the XML data included in a request and provide it as an XMLStreamReader.
 *
 * Subclasses will need to parse the XMLStreamReader into a request object
 * specific to the application.
 */
@ThreadSafe
public abstract class AbstractHttpRequestParser<T extends PipelineRequest> implements HttpRequestParser<T> {

	/**
	 * Used to lock the xmlParamName for thread saftey.
	 * Synchronize all access to _paramName on this lock.
	 */
	protected Object PARAM_NAME_LOCK = new Object();
	
	private String _paramName;	//TODO Shouldn't this have a default?
	
	protected XMLInputFactory inFact;
	
	public AbstractHttpRequestParser() {
		inFact = XMLInputFactory.newInstance();
		inFact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
											 Boolean.FALSE);
		inFact.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
											 Boolean.FALSE);
		inFact.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);

	}
	
	public T parse(HttpServletRequest request) throws Exception {
		String xmlRequest = readInputXMLRequest(request);
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(xmlRequest));
		T result = parse(reader);
		result.setXMLRequest(xmlRequest);
		return result;
	}

	public T parse(String in) throws Exception {
		XMLStreamReader reader = getXMLStream(in);
		T result = parse(reader);
		result.setXMLRequest(in);
		return result;
	}
	
	public void setXmlParam(String paramName) {
		synchronized (PARAM_NAME_LOCK) {
			_paramName = paramName;
		}
	}
	
	public String getXmlParam() {
		synchronized (PARAM_NAME_LOCK) {
			return _paramName;
		}
	}
	
	public String readXMLRequest(HttpServletRequest request, String xmlParam) throws IOException {
		//just use the default implementation
		return defaultReadXMLRequest(request, xmlParam);
	}
	
	public static String defaultReadXMLRequest(HttpServletRequest request, String xmlParam) throws IOException {
		String extraPath = request.getPathInfo();
//		request.get
		
		if ("GET".equals(request.getMethod())) {
		
			String xml = request.getParameter(xmlParam);
			return (xml == null)? "": xml;
			
		} else if ("POST".equals(request.getMethod())) {
			String xml = request.getParameter(xmlParam);
			if (extraPath != null && extraPath.length() > 1) {
				//The client may have passed the XML request as a parameter...
				
				extraPath = extraPath.substring(1);
				if (extraPath.contains("formpost")) {
				
					xml = request.getParameter(xmlParam);
					return (xml == null)? "": xml;

				} else {
					//ignore the extra url info (it may be asking for echo service)
					//fall thru to code below
				}
	
			}
			if (xml != null && xml.length() > 0) {
				// TODO Eliminate later. This handles the old way
				boolean isFormEncoded = xml.indexOf("%3C") > -1;
				if (isFormEncoded) xml = URLDecoder.decode(xml);
				return xml;
			}
			
			// TODO [IK] Read the body input stream into a String to be used as submission. Not right with form posts
			StringBuilder result = new StringBuilder();
			BufferedReader requestReader = request.getReader();
			String line = null;
			while ((line = requestReader.readLine()) != null) {
				result.append(line).append("\n");
			}
			// TODO [IK] HACK Must do this properly!!! 
			
			String singleParamResult = result.toString();
			if (singleParamResult.startsWith(xmlParam)) singleParamResult = singleParamResult.substring(xmlParam.length() + 1); // +1 for = sign
			boolean isFormEncoded = singleParamResult.indexOf("%3C") > -1;
			if (isFormEncoded) singleParamResult = URLDecoder.decode(singleParamResult);
			return result.toString();
			
		} else {
			throw new IOException("Unsupported request method '" + request.getMethod() + "'");
		}
	}
	
	private String readInputXMLRequest(HttpServletRequest request) throws IOException {
		return defaultReadXMLRequest(request, getXmlParam());
	}



	protected XMLStreamReader getXMLStream(String xml) throws IOException, XMLStreamException {							
		if (xml != null) {
			return inFact.createXMLStreamReader(new StringReader(xml));		
		} else {
			throw new IOException("No request data found");
		}																										
	}

	/**
	 * Returns the double value found in the specified parameter of the HTTPRequest.
	 * If the parameter does not exist or cannot be parsed as a number, an error is thrown.
	 * 
	 * @param req
	 * @param name
	 * @return
	 * @throws Exception If the value cannot be converted to a Double
	 */
	public static double parseParamAsDouble(HttpServletRequest req, String name) throws Exception {
		return parseParamAsDouble(req, name, true);
	}
	
	/**
	 * Returns the double value found in the specified parameter of the HTTPRequest.
	 * If require is true and the parameter does not exist, an error
	 * is thrown.  If the parameter does not exist or is empty and
	 * require is not true, null is returned.
	 * 
	 * @param req
	 * @param name
	 * @param require
	 * @return
	 * @throws Exception If the value cannot be converted to a Double
	 */
	public static Double parseParamAsDouble(HttpServletRequest req, String name, boolean require) throws Exception {
	
		String v = req.getParameter(name);
		
		if (v != null) {
			try {
				return Double.parseDouble(v);
			} catch (Exception e) {
				throw new Exception("The '" + name + "' parameter could not be converted to an integer");
			}
			
		} else if (require) {
			throw new Exception("A double (decimal) '" + name + "' parameter is required as part of the request");
		} else {
			return null;
		}
			
	}
	
	/**
	 * Returns the double value found in the specified parameter of the HTTPRequest.
	 * If the attribute does not exist, the default value is returned.
	 * 
	 * @param req
	 * @param name
	 * @param defaultVal
	 * @return
	 * @throws Exception If the value cannot be converted to a Double
	 */
	public static Double parseParamAsDouble(HttpServletRequest req, String name, Double defaultVal) throws Exception {
		String v = req.getParameter(name);
		
		if (v != null) {
			try {
				return Double.parseDouble(v);
			} catch (Exception e) {
				throw new Exception("The '" + name + "' parameter could not be converted to an double (decimal)");
			}
		} else {
			return defaultVal;
		}
	}
	
	public static long parseParamAsLong(HttpServletRequest req, String name) throws Exception {
		return parseParamAsLong(req, name, true);
	}
	
	public static Long parseParamAsLong(HttpServletRequest req, String name, boolean require) throws Exception {
	
		String v = req.getParameter(name);
		
		if (v != null) {
			try {
				return Long.parseLong(v);
			} catch (Exception e) {
				throw new Exception("The '" + name + "' parameter could not be converted to an integer");
			}
			
		} else if (require) {
			throw new Exception("An integer '" + name + "' parameter is required as part of the request");
		} else {
			return null;
		}
			
	}
	
	public static Long parseParamAsLong(HttpServletRequest req, String name, Long defaultVal) throws Exception {
		String v = req.getParameter(name);
		
		if (v != null) {
			try {
				return Long.parseLong(v);
			} catch (Exception e) {
				throw new Exception("The '" + name + "' parameter could not be converted to an integer");
			}
		} else {
			return defaultVal;
		}
	}
	
	/**
	 * Splits the extra path into pieces separated by '/'
	 * 
	 * @param request
	 * @return
	 * @throws Exception
	 */
	public static String[] parseExtraPath(HttpServletRequest request) throws Exception {
		return StringUtils.split(request.getPathInfo(), '/');
	}

}
