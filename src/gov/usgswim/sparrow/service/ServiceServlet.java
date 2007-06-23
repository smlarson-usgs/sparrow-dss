package gov.usgswim.sparrow.service;

import java.io.IOException;
import java.io.PrintWriter;

import java.io.StringReader;

import javax.servlet.*;
import javax.servlet.http.*;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class ServiceServlet extends HttpServlet {
	private static final String CONTENT_TYPE = "text/html; charset=ISO-8859-1";
	private static final String DEFAULT_XML_PARAM_NAME = "xmlreq";
	
	String handlerClassName = "";
	HttpServiceHandler handler;
	
	String xmlParamName = DEFAULT_XML_PARAM_NAME;
	protected XMLInputFactory inFact;
	
	
	public void init(ServletConfig config) throws ServletException {
		super.init(config);
		
		handlerClassName = config.getInitParameter("handler-class");

		if (config.getInitParameter("xml-param-name") != null) {
			xmlParamName = config.getInitParameter("xml-param-name");
		}

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		Class _theClass;
		
		try {
		
			if (loader != null) {
				_theClass = Class.forName(handlerClassName, false, loader);
			}	else {
				_theClass = Class.forName(handlerClassName);
			}
			
			handler = (HttpServiceHandler) _theClass.newInstance();
			
		} catch (ClassNotFoundException e) {
			throw new ServletException(
				"Could not find initilization class '" + handlerClassName +
				"' specified by the init param 'handler-class'"
			);
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
		
		String xml = request.getParameter(xmlParamName);
		if (xml != null) {
			XMLStreamReader xsr;
			try {
				StringReader sr = new StringReader(xml);
				xsr = inFact.createXMLStreamReader(sr);
				handler.dispatch(xsr, response);
			} catch (XMLStreamException e) {
				throw new ServletException(e);
			}
			
			
			response.getOutputStream().flush();
			response.getOutputStream().close();
		} else {
			response.sendError(HttpServletResponse.SC_BAD_REQUEST , "No data found");
		}
		
		
	}

	public void doPost(HttpServletRequest request,
										 HttpServletResponse response) throws ServletException,
																													IOException {

		//response.setContentType(CONTENT_TYPE);
		
		XMLStreamReader xsr;
		try {
			xsr = inFact.createXMLStreamReader(request.getInputStream());
			handler.dispatch(xsr, response);
		} catch (XMLStreamException e) {
			throw new ServletException(e);
		}
		
		response.getOutputStream().flush();
		response.getOutputStream().close();
	}
}
