package gov.usgswim.sparrow.monitor;

import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * The invocation of some type of event / request.
 * @author eeverman
 */
public class Invocation implements Serializable {
	
	private final static Invocation[] EMPTY_INVOCATION_ARRAY = new Invocation[0];
	
	
	private volatile Long startTime;
	private volatile Long endTime;
	private volatile Invocation parent;
	private volatile Object target;
	private volatile Boolean nonNullResponse;
	private volatile Throwable throwable;
	
	private volatile Object request;
	private volatile WeakReference<Object> weakRequest;
	private volatile String requestStr;
	
	private static final ThreadLocal<Invocation> currentInvocationRef = new ThreadLocal<Invocation>();
	
	private Object CHILD_LOCK = new Object();
	private volatile List<Invocation> children;
	
	public Invocation(Class target, Object request, String requestAsString) {
		this((Object)target, request, requestAsString);
	}
		
	public Invocation(Enum target, Object request, String requestAsString) {
		this((Object)target, request, requestAsString);
	}
	
	public Invocation(Class target) {
		this((Object)target, null, null);
	}
	
	public Invocation(Enum target) {
		this((Object)target, null, null);
	}
	
	public Invocation() {
	}
	
	private Invocation(Object target, Object request, String requestAsString) {
		
		this.target = target;
		
		if (request != null) {
			this.request = request;
			weakRequest = new WeakReference<Object>(request);
		}
		
		if (requestAsString != null) {
			requestStr = requestAsString;
		}
	}
	

	
	
	////////////////////////////
	// LIFECYCLE METHODS
	////////////////////////////
	
	/**
	 * Starts the timer running, assigns the parent as the first unfinished
	 * ancestor from the current invocation, and adds this instance as the
	 * currently running Invocation in the thread local storage.
	 */
	public void start() {
		
		//Assign parent as the first unfinished invocation
		parent = getFirstUnfinishedAncestorFromCurrent();
		if (parent != null)	parent.addChild(this);
		
		//This become current
		currentInvocationRef.set(this);
		
		//Start the clock
		startTime = System.currentTimeMillis();
	}
	
	/**
	 * Stops the timer running and replaces this instance with the nearest
	 * unfinished ancestor in the thread local storage of the currently active
	 * Invocation.
	 */
	public void finish() {
		
		//stop the clock
		endTime = System.currentTimeMillis();
		
		//This is no longer the current invocation
		currentInvocationRef.remove();
		
		//Hunt up towards the trunk of the tree of invocations to find the first
		//unfinished one - that becomes the new current invocation.
		Invocation unfinishedAncestor = this.getFirstUnfinished();
		if (unfinishedAncestor != null) {
			currentInvocationRef.set(unfinishedAncestor);
		} else {
			//the request is completely done
		}
	}
	
	/**
	 * When the request is complete, this method can be called to 'demote' the
	 * reference held for the request to a weak reference so that we do not
	 * indefinately hold onto request objects, which might be very large.
	 */
	public void releaseRequest(boolean releaseForAllDescendants) {
		this.request = null;
		
		if (releaseForAllDescendants && hasChildren()) {
			for (Invocation i : children) {
				i.releaseRequest(true);
			}
		}
	}
	
	////////////////////////////
	// GETTER / SETTERS
	////////////////////////////
	
	/**
	 * 
	 * @param request The request object 
	 */
	public void setRequest(Object request) {
		if (request != null) {
			this.request = request;
			weakRequest = new WeakReference<Object>(request);
		}
	}
	
	public Object getTarget() {
		return target;
	}
	
	/**
	 * @param requestString A string version of the request
	 */
	public void setRequestString(String requestString) {
		requestStr = requestString;
	}
	
	public boolean isFinished() {
		return endTime != null;
	}
	
	/**
	 * The start time in milliseconds that this invocation was started, as defined
	 * by System.currentTimeMillis at start time.
	 * May be null if not yet started.
	 * @return 
	 */
	public Long getStartTime() {
		return startTime;
	}
		
	/**
	 * The end time in milliseconds that this invocation was finished, as defined
	 * by System.currentTimeMillis at finish time.
	 * May be null if not yet finished.
	 * @return 
	 */
	public Long getEndTime() {
		return endTime;
	}
	
	/**
	 * Returns the time in seconds that the Invocation took, if finished.
	 * If not finished, the current run time up to now is returned.
	 * If this method is called before start() is called, null is returned.
	 * @return Time in Seconds, or null if the Invocation is not yet started.
	 */
	public Integer getRunTimeSeconds() {
		Integer ms = getRunTimeMs();
		if (ms != null) return ms / 1000;
		return null;
	}
	
	/**
	 * Returns the time in milliseconds that the Invocation took, if finished.
	 * If not finished, the current run time up to now is returned.
	 * If this method is called before start() is called, null is returned.
	 * @return Time in milliseconds, or null if the Invocation is not yet started.
	 */
	public Integer getRunTimeMs() {
		if (startTime != null && endTime != null) {
			return new Long(endTime - startTime).intValue();
		} else if (startTime != null) {
			return new Long(System.currentTimeMillis() - startTime).intValue();
		} else {
			return null;
		}
	}
	
	public Boolean isNonNullResponse() {
		return nonNullResponse;
	}

	public void setNonNullResponse(Boolean nonNullResponse) {
		this.nonNullResponse = nonNullResponse;
	}
	
	public Object getRequest() {
		if (request != null) {
			return request;
		} else if (weakRequest != null) {
			return weakRequest.get();
		} else {
			return null;
		}
	}

	public String getRequestStr() {
		if (requestStr != null) {
			return requestStr;
		} else {
			Object r = getRequest();
			if (r != null) {
				return r.toString();
			} else {
				return null;
			}
		}
	}
	
	/*
	 * If overriden and not invoked, you must call ensureChildList to make sure
	 * it is not null.
	 */
	public void addChild(Invocation child) {
		if (child != null) {
			synchronized (CHILD_LOCK) {
				ensureChildList();
				children.add(child);
			}
		}
	}
	
	public Invocation getParent() {
		return parent;
	}
	
	
	/**
	 * Returns a detached list of children or an empty array if there are none.
	 * The returned array is ordered such that the most recently started Invocation
	 * is first in the array.
	 * 
	 * @return Never null.
	 */
	public Invocation[] getChildren() {
		synchronized (CHILD_LOCK) {
			if (children != null) {
				Invocation[] invos = children.toArray(new Invocation[children.size()]);
				return invos;
			} else {
				return EMPTY_INVOCATION_ARRAY;
			}
		}
	}
	
	public boolean hasChildren() {
		return children != null;
	}
	
	
	public Throwable getError() {
		return throwable;
	}
	
	public boolean hasError() {
		return throwable != null;
	}

	public void setError(Throwable error) {
		this.throwable = error;
	}
	
	////////////////////////////
	// INTERNAL / PROTECTED METHODS
	////////////////////////////
	
	/**
	 * Returns the first unfinished Invocation, starting with the current request
	 * in the queue.
	 * 
	 * @return May return null if the entire request is complete.
	 */
	protected Invocation getFirstUnfinishedAncestorFromCurrent() {
		Invocation current = (Invocation) currentInvocationRef.get();
		if (current == null) return null;
		return current.getFirstUnfinished();
	}
	
	/**
	 * Returns the first unfinished Invocation, starting with this instance and
	 * searching up the hierarchy to find the first unfinished one.  If no
	 * unfinished ancestor is found, the entire request is complete.
	 * 
	 * @return May return null if the entire request is complete.
	 */
	protected Invocation getFirstUnfinished() {
		if (! this.isFinished()) {
			return this;
		} else if (parent != null) {
			return parent.getFirstUnfinished();
		} else {
			return null;
		}
	}
	
	/**
	 * Inits the childList if needed.
	 * If addChild is overriden and not invoked, you must call this method.
	 */
	protected void ensureChildList() {
		synchronized (CHILD_LOCK) {
			if (children == null) {
				children = new ArrayList<Invocation>(1);
			}
		}
	}
	
}
