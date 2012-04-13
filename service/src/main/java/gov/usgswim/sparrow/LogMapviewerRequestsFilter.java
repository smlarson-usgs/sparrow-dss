package gov.usgswim.sparrow;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.log4j.Logger;

/**
 * Logs MapViewer XML requests when set to trace level debuging.
 * 
 * The filter assumes it is mapped correctly to: /omserver/*
 * 
 * @author eeverman
 */
public class LogMapviewerRequestsFilter implements Filter {
	protected static Logger log =
		Logger.getLogger(LogMapviewerRequestsFilter.class); //logging for this class
	
	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
		
		if (log.isTraceEnabled() && request.getParameter("xml_request") != null) {
			log.trace("SPARROW MapViewer received this XML request: " + request.getParameter("xml_request"));
		}
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		//Nothing to do
	}
	
	@Override
	public void destroy() {
		//Nothing to do
	}

}
