package gov.usgswim.sparrow;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

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
public class ActivityLogFilter implements Filter {
	protected static Logger log =
		Logger.getLogger(ActivityLogFilter.class); //logging for this class
	
	public static final int MAX_STORED_REQUESTS_TOTAL = 5000;
	
	public static final int MAX_AGE_MILLISECONDS = 1000 * 60 * 60 * 4;	//4 hours
	
	public static final int CHECK_INTERVAL = 50;	//How often we check the limits
	
	//All requests
	private static ConcurrentLinkedQueue<Request> requests = new ConcurrentLinkedQueue<Request>();
	
	//Total number of requests
	private static volatile int requestCount = 0;
	
	/** list of extensions w/ the dot to not log.  All lower case. */
	private ArrayList<String> excludeExtensions;
	
	/** list of paths to not include.  Paths are just checked based indexOf > 0.  All lower case. */
	private ArrayList<String> excludePaths;
	
	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		//Nothing to do
		excludeExtensions = new ArrayList<String>();
		excludeExtensions.add(".jpg");
		excludeExtensions.add(".css");
		excludeExtensions.add(".ico");
		excludeExtensions.add(".js");
		excludeExtensions.add(".jsp");
		
		excludePaths = new ArrayList<String>();
		excludePaths.add("/images/");
	}
	
	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		
		boolean include = false;
		
		int currentCount = ++requestCount;	//Store locally so we don't see outside changes
		
		Request req = null;
		long time = System.currentTimeMillis();
		
		if (servletRequest instanceof HttpServletRequest) {
			HttpServletRequest httpReq = (HttpServletRequest)servletRequest;
			
			String path = httpReq.getServletPath();
			
			
			//Check to see if we should include the request
			include = includeRequest(path);
			
			if (include) {
				req = new Request(path, time, 0, requestCount);
				requests.add(req);
			}
			
		} else {
			req = new Request("Unknown non-http request", time, 0, requestCount);
			requests.add(req);
		}
		
		
		chain.doFilter(servletRequest, response);
		

		if (include) {
			
			long endTime = System.currentTimeMillis();
			req.setDuration((int) (endTime - req.getStartTime()));
			
			//Check to see if we should trim requests off the end of the que
			if ((currentCount / CHECK_INTERVAL) == ((double)currentCount / (double)CHECK_INTERVAL)) {
				log.debug("Sweeping the Request log que after " + currentCount + " requests");
				int size = requests.size();
				
				if (size > MAX_STORED_REQUESTS_TOTAL) {
					log.trace("Current size of " + size + " is over the size limit of " + MAX_STORED_REQUESTS_TOTAL + ".  Removing extras.");
					
					for (int i = 0; i < size - MAX_STORED_REQUESTS_TOTAL; i++) {
						requests.remove();
					}
					
				} else {
					log.trace("Current size of " + size + " is under the size limit of " + MAX_STORED_REQUESTS_TOTAL + ".  No action required.");
				}
			}
			
		}
	}
	
	/**
	 * Returns true if this path should be include in the logged requests
	 * @param path
	 * @return
	 */
	protected boolean includeRequest(String path) {
		
		path = path.toLowerCase();
		
		for (String ext : excludeExtensions) {
			if (path.endsWith(ext)) {
				return false;
			}
		}
		
		for (String pathSegment : excludePaths) {
			if (path.indexOf(pathSegment) > -1) {
				return false;
			}
		}
		
		return true;
	}


	
	@Override
	public void destroy() {
		requests.clear();
		requests = null;
	}
	
	public static Request[] getRequests() {
		Request[] reqs = requests.toArray(new Request[]{});
		
		//reverse the order of the array
		int i = 0;
		int j = reqs.length - 1;
		Request tmp;
		while (j > i) {
			tmp = reqs[j];
			reqs[j] = reqs[i];
			reqs[i] = tmp;
			j--;
			i++;
		}
		
		return reqs;
	}
	
	public static RequestSetIterator getRequestSetIterator() {
		Request[] reqs = getRequests();
		return new RequestSetIterator(reqs);
	}
	
	/**
	 * Iterates through the results in blocks of time from the most recent to
	 * the oldest requests.  It does not implement the standard interface, but
	 * works similar in that it is one time only usage.
	 * @author eeverman
	 *
	 */
	public static class RequestSetIterator {
		final int ONE_HOUR = 60 * 60;
		
		Request[] requests;
		String[] paths;
	    LinkedHashMap<String, RequestSet> reqsInBlock = new LinkedHashMap<String, RequestSet>();
	    long originalStartTime;
	    long lastEndTime;
	    long endTime;
	    int startRecordIndex = 0;	//the first record to report (first one in the time block)
	    
		public RequestSetIterator(Request[] requests) {
			this.requests = requests;
			
		    HashSet<String> pathSet = new HashSet<String>();
		    
		    for (int i=0; i<requests.length; i++) {
		    	Request r = requests[i];
		    	pathSet.add(r.getPath());
		    }
		    
		    paths = pathSet.toArray(new String[pathSet.size()]);
		    Arrays.sort(paths);
		    
		    for (int i=0; i< paths.length; i++) {
		    	reqsInBlock.put(paths[i], new RequestSet(paths[i]));
		    }
		    
		}
		
		public String[] getPaths() {
			return paths;
		}
		
		public int getTimeAgoMillis() {
			if (originalStartTime == 0) {
				originalStartTime = System.currentTimeMillis();
				lastEndTime = originalStartTime;
			}
			
			return (int) (originalStartTime - endTime);
		}
		
		public String getTimeAgo() {
			if (originalStartTime == 0) {
				originalStartTime = System.currentTimeMillis();
				lastEndTime = originalStartTime;
			}
			
			int totalSeconds = (int) ((originalStartTime - endTime) / 1000);
			int hours = totalSeconds / ONE_HOUR;
			int minutes = (totalSeconds - (hours * ONE_HOUR)) / 60;
			int seconds = totalSeconds - ((hours * ONE_HOUR) + (minutes * 60));
			
			return "" + hours + ":" + minutes + ":" + seconds;
		}
		
		public RequestSet[] getNextResultSetArray(int timeIncrement) {
			if (originalStartTime == 0) {
				originalStartTime = System.currentTimeMillis();
				lastEndTime = originalStartTime;
			}
			
			if (startRecordIndex >= requests.length) return null;
			
			for (RequestSet rs : reqsInBlock.values()) {
				rs.clear();
			}
			
			endTime = lastEndTime - timeIncrement;

			while (startRecordIndex < requests.length) {
				Request req = requests[startRecordIndex];
				if (req.getStartTime() > endTime) {
					reqsInBlock.get(req.getPath()).add(req);
					startRecordIndex++;
				} else {
					break;
				}
			}

			
    		//Update time marker
    		lastEndTime = endTime;
    		
    		return reqsInBlock.values().toArray(new RequestSet[reqsInBlock.size()]);
		}
		
		public int getTotalRequestCount() {
			return requests.length;
		}
	}

	public static class RequestSet {
		String path;
		ArrayList<Request> requests = new ArrayList<Request>();
		int max = 0;
		int min = 0;
		int count = 0;
		int statCount = 0;	//count for purpose of stats (skips neg numbers)
		
		public RequestSet(String path) {
			this.path = path;
		}
		
		public void add(Request request) {
			requests.add(request);
			count++;
			int duration = request.getDuration();
			if (duration > -1) {
				statCount++;
				if (statCount == 1) {
					min = duration;
					max = duration;
				} else {
					if (duration < min) min = duration;
					if (duration > max) max = duration;
				}
			}
		}
		
		public void clear() {
			requests.clear();
			max = 0;
			min = 0;
			count = 0;
			statCount = 0;
		}
		
		public double getMaxDuration() { return (double)max / 1000d; }
		
		public double getMinDuration() { return (double)min / 1000d; }
		
		public double getAvgDuration() {
			if (statCount == 0) {
				return 0;
			} else {
			
				int totalTime = 0;
				for (Request req : requests) {
					if (req.getDuration() > -1) totalTime+= req.getDuration();
				}
				
				return (double)totalTime / (double)(statCount * 1000);
			}
		}
		
		public int getCount() { return count; }
	}
	
	
	public static class Request {
		private final String path;
		private final long startTime;
		private final int contextId;	//zero == unknown
		private final int index;	//sequential index number of ths request
		int duration = -1;	//-1 indicates it is not set
		
		public Request(String path, long startTime, int contextId, int index) {
			this.path = path;
			this.startTime = startTime;
			this.contextId = contextId;
			this.index = index;
		}

		public int getDuration() { return duration; }

		public void setDuration(int duration) { this.duration = duration; }

		public String getPath() { return path; }

		public long getStartTime() { return startTime; }

		public int getContextId() { return contextId; }
		
		public int getIndex() { return index; }

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + contextId;
			result = prime * result + index;
			result = prime * result + ((path == null) ? 0 : path.hashCode());
			result = prime * result + (int) (startTime ^ (startTime >>> 32));
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			//This simple implementation is OK - I don't expect there to be
			//duplicate instances and if there are a few its OK to consider
			//them as distinct.
			return (this == obj);
		}
	}



}
