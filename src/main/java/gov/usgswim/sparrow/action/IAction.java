package gov.usgswim.sparrow.action;

/**
 * An Action is intended to be a single use, non-threadsafe instance that is
 * used to run a calculation, build, and or load data.
 * 
 * The minimum to implement is to override the abstract doAction() method.
 * State is handled by the implementing subclass, so you will likely need some
 * setState() methods.
 * 
 * @author eeverman
 *
 * @param <R>
 */
public interface IAction<R extends Object> {

	/**
	 * Implementation class should implement this method and leave the run()
	 * method for a parent class to handle cleanup.
	 * 
	 * Actions may decide to return null to indicate a fail, or may choose
	 * to throw an error.
	 * 
	 * @return R as specified by the subclass.
	 * @throws Exception If the action throws an exception, post is still called.
	 */
	public R doAction() throws Exception;
	
	/**
	 * This method is the method callers should call.
	 * 
	 * A base class should handle logging and error recovery.  The base class
	 * should call doAction on subclasses.
	 * @return
	 * @throws Exception
	 */
	public R run() throws Exception;

}