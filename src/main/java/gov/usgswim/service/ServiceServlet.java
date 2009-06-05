package gov.usgswim.service;

import gov.usgswim.service.pipeline.Pipeline;
import gov.usgswim.service.pipeline.PipelineRequest;
import gov.usgswim.sparrow.service.echo.EchoPipeline;
import gov.usgswim.sparrow.service.json.JSONifyPipeline;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;

import org.apache.log4j.Logger;

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
 * <code>/myservice/formpost</code>
 * Where xmlreq could be changed based on the 'xml-param-name' init parameter.
 */
public class ServiceServlet extends HttpServlet {
	private static final String JSON = "json";
	private static final String XML = "xml";
	private static final long serialVersionUID = 7831587587942691556L;
	protected static Logger log =
		Logger.getLogger(ServiceServlet.class); //logging for this class


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
	
	//Default value of XML_PARAM_NAME
	public static final String DEFAULT_XML_PARAM_NAME = "xmlreq";
	protected String xmlParamName = DEFAULT_XML_PARAM_NAME;
	
	/**
	 * The name of an init parameter which must contain the fully qualified
	 * class name of the Pipeline class that will handle requests to
	 * this servlet.
	 * 
	 * The name class must implement the Pipeline interface.
	 */
	public static final String PIPELINE_CLASS = "pipeline-class";


	/**
	 * The fully qualified class name of the Pipeline class that will
	 * handle requests to this servlet.
	 * 
	 */
	protected String pipelineClassName = "";
	protected Class<?> pipelineClass;

	protected XMLInputFactory inFact;

	// ================
	// INSTANCE METHODS
	// ================
	@Override
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		if (config.getInitParameter(XML_PARAM_NAME) != null) {
			xmlParamName = config.getInitParameter(XML_PARAM_NAME);
		}
		
		pipelineClassName = config.getInitParameter(PIPELINE_CLASS);
		
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class<?> _theClass;

		try {
			if (loader != null) {
				_theClass = Class.forName(pipelineClassName, false, loader);
			}	else {
				_theClass = Class.forName(pipelineClassName);
			}
			pipelineClass = _theClass;
		} catch (ClassNotFoundException e) {
			throw new ServletException(
					"Could not find initilization class '" + pipelineClassName +
					"' specified by the init param '" + PIPELINE_CLASS + "'"
			);
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
	@Override
	public void doGet(HttpServletRequest request,
										HttpServletResponse response) throws ServletException, IOException {
		
		doPost(request, response);
		// TODO suggest use this to handle REST by calling parseREST()
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
	@Override
	public void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		PipelineRequest o = null;
		// TODO suggest use this to handle POST & SOAP by calling parsePOST(), 
		try {
			String echoType = getEchoRequestType(request);
			Pipeline pipe = null;
			if (echoType != null) {
				pipe = makeEchoPipeline(echoType);
			} else {
				pipe = (Pipeline) pipelineClass.newInstance();
			}
			pipe.setXMLParamName(xmlParamName);
			o = pipe.parse(request);
			pipe.dispatch(o, response);
		} catch (Exception e) {
			throw new ServletException(e);
		}
	}

	public static Pipeline makeEchoPipeline(String echoType) {
		if (JSON.equals(echoType)) {
			return new JSONifyPipeline();
		} else if (XML.equals(echoType)) {
			return new EchoPipeline();
		}
		return null;
	}

	public static String getEchoRequestType(HttpServletRequest request) {
		String extraPath = request.getPathInfo();
		extraPath = (extraPath == null)? "": extraPath;
		if (extraPath.contains("xmlecho")) {
			return XML;
		} else if (extraPath.contains("jsonecho")) {
			return JSON;
		}
		return null;
	}

}
