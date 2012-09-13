package gov.usgswim.sparrow;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Zapps any sessions of requests.
 * @author eeverman
 *
 */
public class NoMapViewerSessions implements Filter {

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		chain.doFilter(request, response);
		
		if (request instanceof HttpServletRequest) {
			HttpServletRequest httpReq = (HttpServletRequest)request;
			
			if (httpReq.getSession(false) != null) {
				HttpSession session = httpReq.getSession();
				session.invalidate();
			}
			
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
