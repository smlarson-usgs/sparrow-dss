package gov.usgswim.service;

import java.io.IOException;
import java.io.PrintWriter;

import java.io.StringReader;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

/**
 * A thin servlet that takes requests for a service and allows them to be
 * handled by a configurable handler class.
 * 
 * This servlet attempts to turn the incoming request into an XML stream, or,
 * in the case of a GET request, it looks for a configurable parameter to
 * contain the xml request (which it also turns into a stream).
 * 
 * For GET requests:  The XML request must be passed as a properly escaped XML
 * document using the parameter name defined in the servlet configuration.
 * 
 * For POST requests:  The XML request document may be passed as the content of the
 * POST request itself with no parameter name.  Not all frameworks will support
 * creating requests in this way so the XML document may also be included as
 * a parameter, using the parameter name defined by the 'xml-param-name' 
 * servlet init param, which defaults to xmlreq.  To indicate that the XML
 * request document is passed as a parameter, the name of the parameter
 * must be appened to the url.  For instance, if the servlet url is:
 * <code>/myservice</code>
 * To indicate that the XML has been passed as a parameter, modify the url to:
 * <code>/myservice/xmlreq</code>
 * Where xmlreq could be changed based on the 'xml-param-name' init parameter.
 */
public class ServiceServlet extends HttpServlet {
	private static final String CONTENT_TYPE = "text/html; charset=ISO-8859-1";
	
	/**
	 * The name of the optional init parameter that defines the name of the http request
	 * parameter that contains the XML request.  If not specified as an init
	 * parameter to this servlet, it defaults to 'xmlreq'.
	 * 
	 * For GET requests, the parameter defined here is required to pass the
	 * request content.  See the class documentation for details regarding
	 * GET and POST requests.
	 */
	public static final String XML_PARAM_NAME = "xml-param-name";
	
	/**
	 * The name of an init parameter which must contain the fully qualified
	 * class name of the HttpServiceHandler class that will handle requests to
	 * this servlet.
	 * 
	 * The name class must implement the HttpRequestHandler interface.
	 */
	public static final String HANDLER_CLASS = "handler-class";
	
	/**
	 * The name of an init parameter which may contain the fully qualified
	 * class name of the HttpRequestParser class that will
	 * create request objects to pass to the handler.  The SimpleHttpRequestParser
	 * is used if none is specified.
	 * 
	 * The name class must implement the HttpRequestParser interface.
	 */
	public static final String PARSER_CLASS = "parser-class";
	
	
	//Default value of XML_PARAM_NAME
	private static final String DEFAULT_XML_PARAM_NAME = "xmlreq";
	
	private static final String DEFAULT_PARSER_CLASS_NAME = "gov.usgswim.service.SimpleHttpRequestParser";
	
	/**
	 * The fully qualified class name of the HttpRequestHandler class that will
	 * handle requests to this servlet.
	 */
	protected String handlerClassName = "";
	
	/**
	 * The fully qualified class name of the HttpRequestParser class that will
	 * create a request object to pass to the handler.  The SimpleHttpRequestParser
	 * is used if none is specified.
	 */
	protected String parserClassName = DEFAULT_PARSER_CLASS_NAME;
	
	/**
	 * An instance of the handler class named by handlerClassName.
	 */
	protected HttpRequestHandler handler;
	
	/**
	 * An instance of the parser.
	 */
	protected HttpRequestParser parser;
	
	protected String xmlParamName = DEFAULT_XML_PARAM_NAME;
	protected XMLInputFactory inFact;
	
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		handlerClassName = config.getInitParameter(HANDLER_CLASS);
		
		if (config.getInitParameter(PARSER_CLASS) != null) {
			parserClassName = config.getInitParameter(PARSER_CLASS);
		}
		

		if (config.getInitParameter(XML_PARAM_NAME) != null) {
			xmlParamName = config.getInitParameter(XML_PARAM_NAME);
		}

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class _theClass;
		
		try {
		
			try {
				if (loader != null) {
					_theClass = Class.forName(handlerClassName, false, loader);
				}	else {
					_theClass = Class.forName(handlerClassName);
				}
				
				handler = (HttpRequestHandler) _theClass.newInstance();
			
			} catch (ClassNotFoundException e) {
				throw new ServletException(
					"Could not find initilization class '" + handlerClassName +
					"' specified by the init param 'handler-class'"
				);
			}
			
			try {
				if (loader != null) {
					_theClass = Class.forName(parserClassName, false, loader);
				}	else {
					_theClass = Class.forName(parserClassName);
				}
				
				parser = (HttpRequestParser) _theClass.newInstance();
				parser.setXmlParam(xmlParamName);
			
			} catch (ClassNotFoundException e) {
				throw new ServletException(
					"Could not find initilization class '" + parserClassName +
					"' specified by the init param 'parser-class'"
				);
			}
		} catch (IllegalAccessException e) {
			throw new ServletException(e);
		} catch (InstantiationException e) {
			throw new ServletException(e);
		}
		


		inFact = XMLInputFactory.newInstance();
		inFact.setProperty(XMLInputFactory.IS_REPLACING_ENTITY_REFERENCES,
											 Boolean.FALSE);
		inFact.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES,
											 Boolean.FALSE);
		inFact.setProperty(XMLInputFactory.IS_COALESCING, Boolean.TRUE);


	}


	public void doGet(HttpServletRequest request,
										HttpServletResponse response) throws ServletException,
																												 IOException {
		
		Object o;
		try {
			o = parser.parse(request);
			handler.dispatch(o, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}
	


	/**
	 * Post expect either xml request to be contain either as the body of the
	 * request, or as the parameter DEFAULT_XML_PARAM_NAME *if* that name is added
	 * to the url, eg:
	 * 
	 * url: servlet-context/xmlreq
	 * -and-
	 * request contains a parameter 'xmlreq' w/ the XML document
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request,
										 HttpServletResponse response) throws ServletException,
																													IOException {

		String extraPath = request.getPathInfo();
		
		if (extraPath != null && extraPath.length() > 1) {
			extraPath = extraPath.substring(1);
			if (xmlParamName.equals(extraPath)) {
			
				String xml = request.getParameter(xmlParamName);
				doStringRequest(xml, response);
				
				return;	//request has been handled
				
			} else {
				//ignore the extra url info and process as normal
			}

		}
		
		
		XMLStreamReader xsr;
		try {
			xsr = inFact.createXMLStreamReader(request.getInputStream());
			handler.dispatch(xsr, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
		
		response.getOutputStream().flush();
		response.getOutputStream().close();

		
	}
	
	protected void doStringRequest(String xml, HttpServletResponse response)
				throws ServletException, IOException {
																													

		if (xml != null) {
			XMLStreamReader xsr;
			try {
				StringReader sr = new StringReader(xml);
				xsr = inFact.createXMLStreamReader(sr);
				handler.dispatch(xsr, response);
			} catch (Exception e) {
				throw new ServletException(e);
			}
			
			
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST , "No data found");
		}																										
	}

}
