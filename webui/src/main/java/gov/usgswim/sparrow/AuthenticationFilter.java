package gov.usgswim.sparrow;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

/**
 * Filters requests to determine if the user should be prompted to login.
 * 
 * If requests are received via 'water.usgs.gov', the user is not prompted
 * to login.
 * 
 * If requests are received via any other source, the user is prompted to login.
 * 
 * Detection is done via the 'x-forwarded-host header', which includes 
 * 'water.usgs.gov' if the request was forwarded from that server.  I don't
 * actually know why or how this header gets included.
 * 
 * @author eeverman
 */
public class AuthenticationFilter implements Filter {
	protected static Logger log =
		Logger.getLogger(AuthenticationFilter.class); //logging for this class
	
	private static final String FORWARD_HEADER = "x-forwarded-host";
	
	private static final String WATER_FORWARD_HEADER = "water.usgs.gov";
	
	/** The path reported when the map page is requested */
	private static final String MAP_PAGE_PATH = "/map.jsp";
	
	
	private String contextPath;
	
	@Override
	public void init(FilterConfig config) throws ServletException {
		contextPath = config.getServletContext().getContextPath() + "/";
	}
	
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		
		HttpServletRequest httpReq = (HttpServletRequest)servletRequest;
		
		String forwardHeader = httpReq.getHeader(FORWARD_HEADER);
		
		if (forwardHeader != null && forwardHeader.contains(WATER_FORWARD_HEADER)) {
			//This is a request via the water.usgs.gov url
			//let it go through w/o prompting for login.
			chain.doFilter(servletRequest, response);
		} else {
			if (httpReq.getUserPrincipal() == null ) {
				String uri = httpReq.getRequestURI();
				//String servletPath = httpReq.getServletPath();
				
				if (contextPath.equalsIgnoreCase(uri) ||
						MAP_PAGE_PATH.equalsIgnoreCase(httpReq.getServletPath())) {
					
					HttpServletResponse httpResp = (HttpServletResponse) response;
					httpResp.sendRedirect("secure_home.jsp");  
			        return;  
				}
				
				
			}
			chain.doFilter(servletRequest, response);
		}
			
	}


	
	@Override
	public void destroy() {
		//Nothing to do
	}

}
