package gov.usgswim.sparrow.action;

import java.sql.Connection;

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
	
	/**
	 * This method is the method callers should call.
	 * 
	 * The passed connections are used instead of fetching new connections.
	 * These connections are not closed, however, any statements created via the
	 * getRXStatement() will be closed.
	 * 
	 * A base class should handle logging and error recovery.  The base class
	 * should call doAction on subclasses.
	 * @param readOnlyConnection
	 * @param readWriteConnection
	 * @return
	 * @throws Exception
	 */
	public R run(Connection readOnlyConnection, Connection readWriteConnection, Connection postgresConnection) throws Exception;
	
	/**
	 * Returns the model ID for this operation, if applicable.
	 * This is used in error reporting and logging.
	 * @return 
	 */
	public Long getModelId();

}