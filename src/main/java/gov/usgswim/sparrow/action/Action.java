package gov.usgswim.sparrow.action;

import org.apache.log4j.Logger;

/**
 * An Action is intended to be a single use, non-threadsafe instance that is
 * used to run a calculation, build, and or load data.
 * 
 * The minimum to implement is to override the abstract doAction() method.
 * State is handled by the implementing subclass, so you will likely need some
 * setState() methods.
 * 
 * If logging for this class is enables, auto timing will take place, along with
 * a unique run number used in the logs so its possible to match up the
 * start and end messages in the logging by the number.
 * 
 * @author eeverman
 *
 * @param <R>
 */
public abstract class Action<R extends Object> {
	protected static Logger log =
		Logger.getLogger(Action.class); //logging for this class
	
	/**
	 * A shared run count that increments when a new instance is created.
	 */
	private static int staticRunCount = 0;
	
	/**
	 * The run number of the current instance, based on staticRunCount.
	 */
	private int runNumber = 0;
	
	private long startTime;		//Time the action starts
	
	public Action() {
		staticRunCount++;
		runNumber = staticRunCount;
	}
	
	/**
	 * Generic public run method, invokes pre, do, and post action.
	 * 
	 * Actions may decide to return null to indicate a fail, or may choose
	 * to throw an error.
	 * 
	 * @return R as specified by the subclass.
	 * @throws Exception If the action throws an exception, post is still called.
	 */
	public R run() throws Exception {
		preAction();
		
		R r = null;
		Exception e = null;
		
		try {
			r = doAction();
		} catch (Exception ee) {
			e = ee;
		}
		postAction(r != null, e);
		return r;
	}
	
	protected void preAction() {
		
		if (log.isDebugEnabled()) {
			startTime = System.currentTimeMillis();
			log.debug("Beginning action for " + this.getClass().getName() +
					".  Run Number: " + runNumber);
		}
	}
	
	protected abstract R doAction() throws Exception;
	
	/**
	 * An optional message to add to the log on completion.  Only called
	 * if the log level is debug or greater, or the doAction method returned null
	 * (a fail) or threw an exception.  Note that the runNumber is
	 * auto added to the end of the message.
	 * 
	 * @return
	 */
	protected String getPostMessage() {
		return null;
	}
	
	protected void postAction(boolean success, Exception error) {
		if (log.isDebugEnabled()) {
			long totalTime = System.currentTimeMillis() - startTime;
			
			if (success && error == null) {
				log.debug("Action completed for " + this.getClass().getName() +
						".  Total Time: " + totalTime + "ms, Run Number: " +
						runNumber);
			} else {
				if (error != null) {
					log.error("Action FAILED w/ an exception for " +
							this.getClass().getName() + ".  Total Time: " +
							totalTime + "ms, Run Number: " +
							runNumber, error);
				} else {
					log.debug("Action FAILED, returning null, for " +
							this.getClass().getName() + ".  Total Time: " +
							totalTime + "ms, Run Number: " +
							runNumber);
				}
			}
			
			String msg = getPostMessage();
			if (msg != null) {
				log.debug(msg + "  (Run Number: " + runNumber + ")");
			}
		}
	}
}
