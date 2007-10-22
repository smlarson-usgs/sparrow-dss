package gov.usgswim.service;

import gov.usgswim.ThreadSafe;

import java.io.IOException;

import java.io.StringReader;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import javax.servlet.http.HttpServletResponse;

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
public abstract class AbstractHttpRequestParser<T> implements HttpRequestParser<T> {

	/**
	 * Used to lock the xmlParamName for thread saftey.
	 * Synchronize all access to _paramName on this lock.
	 */
	protected Object PARAM_NAME_LOCK = new Object();
	
	private String _paramName;
	
	protected XMLInputFactory inFact;
	
	public AbstractHttpRequestParser() {
		inFact = XMLInputFactory.newInstance();
		inFact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
											 Boolean.FALSE);
		inFact.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
											 Boolean.FALSE);
		inFact.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);

	}

	//@Override
	//public abstract T parse(HttpServletRequest request) throws Exception;
	
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
	
	protected XMLStreamReader getXMLStream(HttpServletRequest request) throws XMLStreamException,
																																						IOException {
		String extraPath = request.getPathInfo();
		String xmlParam = getXmlParam();
		
		
		if ("GET".equals(request.getMethod())) {
		
			String xml = request.getParameter(xmlParam);
			return getXMLStream(xml);
			
		} else if ("POST".equals(request.getMethod())) {
		
			if (extraPath != null && extraPath.length() > 1) {
				//The client may have passed the XML request as a parameter...
				
				extraPath = extraPath.substring(1);
				if (extraPath.equals(xmlParam)) {
				
					String xml = request.getParameter(xmlParam);
					return getXMLStream(xml);

				} else {
					//ignore the extra url info and process as normal.
					//No idea what the extra stuff could be.
				}
	
			}
			
			//Assume the entire POST contents is the XML document
			return inFact.createXMLStreamReader(request.getInputStream());
			
		} else {
			throw new IOException("Unsupported request method '" + request.getMethod() + "'");
		}

	}
	
	protected XMLStreamReader getXMLStream(String xml) throws IOException,
																															 XMLStreamException {
																													
		if (xml != null) {
		
			XMLStreamReader xsr;
			StringReader sr = new StringReader(xml);
			xsr = inFact.createXMLStreamReader(sr);
			return xsr;
			
		} else {
			throw new IOException("No request data found");
		}																										
	}
	
	/**
	 * Returns the integer value found in the specified attribute of the current
	 * element.  If require is true and the attribute does not exist, an error
	 * is thrown.
	 * @param reader
	 * @param attrib
	 * @return
	 * @throws Exception
	 */
	public static int parseAttribAsInt(
			XMLStreamReader reader, String attrib) throws Exception {
			
		return parseAttribAsInt(reader, attrib, true);
	}
	
	/**
	 * Returns the Integer value found in the specified attribute of the current
	 * element.  If require is true and the attribute does not exist, an error
	 * is thrown.  If the attribute does not exist or is empty and require is
	 * not true, null is returned.
	 * @param reader
	 * @param attrib
	 * @param require
	 * @return
	 * @throws Exception
	 */
	public static Integer parseAttribAsInt(
			XMLStreamReader reader, String attrib, boolean require) throws Exception {
		
		String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );
		
		if (v != null) {
			int iv = 0;
			
			try {
				return Integer.parseInt(v);
			} catch (Exception e) {
				throw new Exception("The '" + attrib + "' attribute for element '" + reader.getLocalName() + "' must be an integer");
			}
			
		} else if (require) {
			throw new Exception("The '" + attrib + "' attribute must exist for element '" + reader.getLocalName() + "'");
		} else {
			return null;
		}
		
	}
	
	
	/**
	 * Returns the double value found in the specified attribute of the current
	 * element.  If the attribute does not exist or cannot be parsed as a number,
	 * an error is thrown.
	 * 
	 * @param reader
	 * @param attrib
	 * @return
	 * @throws Exception
	 */
	public static double parseAttribAsDouble(
			XMLStreamReader reader, String attrib) throws Exception {
			
		return parseAttribAsDouble(reader, attrib, true);
	}
	
	/**
	 * Returns the Double value found in the specified attribute of the current
	 * element.  If require is true and the attribute does not exist, an error
	 * is thrown.  If the attribute does not exist or is empty and
	 * require is not true, null is returned.
	 * @param reader
	 * @param attrib
	 * @param require
	 * @return
	 * @throws Exception
	 */
	public static Double parseAttribAsDouble(
			XMLStreamReader reader, String attrib, boolean require) throws Exception {
		
		String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );
		
		if (v != null) {
			int iv = 0;
			
			try {
				return Double.parseDouble(v);
			} catch (Exception e) {
				throw new Exception("The '" + attrib + "' attribute for element '" + reader.getLocalName() + "' must be a number");
			}
			
		} else if (require) {
			throw new Exception("The '" + attrib + "' attribute must exist for element '" + reader.getLocalName() + "'");
		} else {
			return null;
		}
		
	}
	
	/**
	 * Returns the long value found in the specified attribute of the current
	 * element.  If the attribute does not exist or cannot be parsed as a number,
	 * an error is thrown.
	 * 
	 * @param reader
	 * @param attrib
	 * @return
	 * @throws Exception
	 */
	public static long parseAttribAsLong(
			XMLStreamReader reader, String attrib) throws Exception {
			
		return parseAttribAsLong(reader, attrib, true);
	}
	
	/**
	 * Returns the Long value found in the specified attribute of the current
	 * element.  If require is true and the attribute does not exist, an error
	 * is thrown.  If the attribute does not exist or is empty and
	 * require is not true, null is returned.
	 * @param reader
	 * @param attrib
	 * @param require
	 * @return
	 * @throws Exception
	 */
	public static Long parseAttribAsLong(
			XMLStreamReader reader, String attrib, boolean require) throws Exception {
		
		String v = StringUtils.trimToNull( reader.getAttributeValue(null, attrib) );
		
		if (v != null) {
			int iv = 0;
			
			try {
				return Long.parseLong(v);
			} catch (Exception e) {
				throw new Exception("The '" + attrib + "' attribute for element '" + reader.getLocalName() + "' must be a long integer");
			}
			
		} else if (require) {
			throw new Exception("The '" + attrib + "' attribute must exist for element '" + reader.getLocalName() + "'");
		} else {
			return null;
		}
		
	}
}
