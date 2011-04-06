package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.ServiceResponseMimeType.*;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.json.JsonHierarchicalStreamDriver;
import com.thoughtworks.xstream.io.xml.StaxDriver;

/**
 * A based class for servlets which includes utility methods to serialize objects
 * into a wrapper using XStream, and to find those objects back again in a POST
 * or PUT.
 * 
 * @author eeverman
 *
 */
public abstract class AbstractSparrowServlet extends HttpServlet {

	public static final String REQUESTED_MIME_TYPE_PARAM_NAME = "mime_type";
	/** Posters may set a header of this name to the name of a http parameter
	 *  used to post XML data.  If not specified, the content of the post will
	 *  be used w/ content type detection.
	 */
	public static final String XML_SUBMIT_HEADER_NAME = "xml_req_param";
	/** Posters may set a header of this name to the name of a http parameter
	 *  used to post JSON data.  If not specified, the content of the post will
	 *  be used w/ content type detection.
	 */
	public static final String JSON_SUBMIT_HEADER_NAME = "json_req_param";
	/** Posters may set a header of this name to the name of a http parameter
	 *  used to post XML data.  If not specified, the content of the post will
	 *  be used w/ content type detection.
	 */
	public static final String XML_SUBMIT_DEFAULT_PARAM_NAME = "xml";
	/** Posters may set a header of this name to the name of a http parameter
	 *  used to post JSON data.  If not specified, the content of the post will
	 *  be used w/ content type detection.
	 */
	public static final String JSON_SUBMIT_DEFAULT_PARAM_NAME = "json";

	
	/**
	 * Default constructor.
	 */
	public AbstractSparrowServlet() {
		super();
	}
	
	
	/**
	 * Returns a trimmed to null value from the parameter map for the passed name.
	 * 
	 * @param params
	 * @param name
	 */
	protected static String getClean(Map params, String name) {
		Object v = params.get(name);
		if (v != null) {
			String[] vs = (String[]) v;
			if (vs.length > 0) {
				return StringUtils.trimToNull(vs[0]);
			}
		}
		return null;
	}

	/**
	 * Returns a Long value from the parameter map for the passed name.
	 * If unparsable, missing, or null, null is returned.
	 * 
	 * @param params
	 * @param name
	 */
	protected static Long getLong(Map params, String name) {
		String s = getClean(params, name);
		if (s != null) {
			try {
				return Long.parseLong(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}

	/**
	 * Returns an Integer value from the parameter map for the passed name.
	 * If unparsable, empty, or missing, null is returned.
	 * 
	 * @param params
	 * @param name
	 */
	private static Integer getInteger(Map params, String name) {
		String s = getClean(params, name);
		if (s != null) {
			try {
				return Integer.parseInt(s);
			} catch (NumberFormatException e) {
				//allow null to be returned
			}
		}
		return null;
	}

	/**
	 * Returns a Boolean value from the parameter map for the passed name.
	 * If empty or missing, null is returned.  Otherwise, values are considered
	 * true if they are 'T' or 'TRUE', case insensitive.
	 * 
	 * @param params
	 * @param name
	 */
	protected static Boolean getBoolean(Map params, String key) {
		String s = getClean(params, key);
		if (s != null) {
			return ("T".equalsIgnoreCase(s) || "true".equalsIgnoreCase(s));
		}
		return null;
	}
	
	/**
	 * Determines if a string represents true.
	 * Values are considered true if they are 'T' or 'TRUE', case insensitive.
	 * @param value
	 * @return
	 */
	protected boolean parseBoolean(String value) {
		value = StringUtils.trimToNull(value);
		return ("true".equalsIgnoreCase(value) || "t".equalsIgnoreCase(value));
	}

	/**
	 * Serializes the passed wrapper using the mimetype and encoding in
	 * the wrapper.
	 * 
	 * @param resp
	 * @param wrap
	 * @throws IOException
	 */
	protected void sendResponse(HttpServletResponse resp, ServiceResponseWrapper wrap) throws IOException {
		
		XStream xs = null;
		resp.setCharacterEncoding(wrap.getEncoding());
		resp.setContentType(wrap.getMimeType().toString());
	
		switch (wrap.getMimeType()) {
		case XML: 
			xs = ServletResponseParser.getXMLXStream();
			break;
		case JSON:
			xs = ServletResponseParser.getJSONXStreamWriter();
			break;
		default:
			throw new RuntimeException("Unknown MIMEType.");
		}
		
		xs.toXML(wrap, resp.getWriter());
	}

	/**
	 * Determines the desired mime type of the client from the request.
	 * This method is used in a GET request, since likely only http parameters
	 * were posted, but XML or JSON needs to be returned.
	 * 
	 * @param req
	 * @return
	 */
	protected ServiceResponseMimeType parseMime(HttpServletRequest req) {
		String mimeStr = StringUtils.trimToNull(
				req.getParameter(REQUESTED_MIME_TYPE_PARAM_NAME));
		
		ServiceResponseMimeType type = ServiceResponseMimeType.parse(mimeStr);
		
		if (type != UNKNOWN) {
			return type;
		} else {
			Enumeration heads = req.getHeaders("Accept");
			
			while (heads.hasMoreElements()) {
				String a = heads.nextElement().toString();
				type = ServiceResponseMimeType.parse(a);
				if (type != UNKNOWN) return type;
			}
			
		}
		
		//Couldn't find a type - use the default
		return XML;
	}

	/**
	 * Trims the extraPath to remove the leading slash and completely trims it
	 * to null.
	 * 
	 * @param req
	 * @return
	 */
	protected String cleanExtraPath(HttpServletRequest req) {
		String extraPath = StringUtils.trimToNull(req.getPathInfo());
		
		
		if (extraPath != null) {
			if (extraPath.startsWith("/")) {
				extraPath = StringUtils.trimToNull(extraPath.substring(1));
			}
		}
		
		return extraPath;
	}

}