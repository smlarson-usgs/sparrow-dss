package gov.usgswim.service;

import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRegistry;
import gov.usgswim.service.pipeline.PipelineRequest;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;

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


	/**
	 * @see doPost()
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doGet(HttpServletRequest request,
										HttpServletResponse response) throws ServletException, IOException {
		
		doPost(request, response);
	}
	


	/**
	 * Delegates to the HttpRequestParser to get a request, then passes the request
	 * to the HttpRequestHandler.
	 * 
	 * @param request
	 * @param response
	 * @throws ServletException
	 * @throws IOException
	 */
	public void doPost(HttpServletRequest request,
										 HttpServletResponse response) throws ServletException, IOException {

		PipelineRequest o;
		try {
			o = parser.parseForPipeline(request); 
			Pipeline pipe = PipelineRegistry.lookup(o);
			pipe.setHandler(handler);
			pipe.dispatch(o, response);
			
		} catch (Exception e) {
			throw new ServletException(e);
		}

	}

}
