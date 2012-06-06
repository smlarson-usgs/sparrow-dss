package gov.usgswim.sparrow.monitor;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

/**
 * Monitors a single request as it goes to the cache and invokes actions to 
 * complete the request.  The data structure allows nested cache and action
 * invocations to be recorded.
 * 
 * The RequestMonitor should be added to the request thread immediately as it is
 * received by a Servlet Filter.  Since the filter does not know the details of
 * the request, this Invocation implementation allows details that are normally
 * specified in the constructor to be set later by a handler servlet.
 * 
 * @author eeverman
 */
public class RequestMonitor extends Invocation {
	
	public String requestUrl;
	
	public RequestMonitor(String requestUrl) {
		super(null, null, null);
		this.requestUrl = requestUrl;
	}
	
	
	
}
