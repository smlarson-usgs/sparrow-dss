package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.monitor.ActionInvocation;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import javax.naming.NameNotFoundException;

import org.apache.commons.lang.StringUtils;
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
public abstract class Action<R extends Object> implements IAction<R> {
	protected static Logger log =
		Logger.getLogger(Action.class); //logging for this class

	public final static String NL = System.getProperty("line.separator");

	//synch access on this class
	private static Properties dataSeriesProperties;

	//A string that Action implementations can set to record outcomes for logging
	private String postMessage;

	//A list of validation messages.  Never null.
	private ArrayList<String> validationErrors = new ArrayList<String>();

	/**
	 * A shared run count that increments when a new instance is created.
	 */
	private static int staticRunCount = 0;

	/**
	 * The run number of the current instance, based on staticRunCount.
	 */
	private int runNumber = 0;

	/** A read-only connection that will be closed when the action completes */
	private Connection roConn = null;

	/** A read-write connection that will be closed when the action completes */
	private Connection rwConn = null;

	/** true if the read-only db connection was passed in, thus should not be closed */
	private boolean externallyOwnedROConn = false;

	/** true if the read-write db connection was passed in, thus should not be closed */
	private boolean externallyOwnedRWConn = false;

	private long startTime;		//Time the action starts

	/** A list of statements to be closed when the action completes */
	private List<Statement> autoCloseStatements;

	/** A list of resultsets to be closed when the action completes */
	private List<ResultSet> autoCloseResults;

	/**
	 * A monitor object that will be non-null when doAction() is called.
	 * To help monitor, subclasses should add the request object and
	 * string to the invocation.
	 */
	protected ActionInvocation invocation;

	public Action() {
		staticRunCount++;
		runNumber = staticRunCount;
	}

	/* (non-Javadoc)
	 * @see gov.usgswim.sparrow.action.IAction#run()
	 */
	@Override
	public R run() throws Exception {
		preAction();

		R r = null;
		Exception e = null;

		if (! hasValidationErrors()) {
			try {
				initFields();
				r = doAction();
			} catch (Exception ee) {
				e = ee;
			}
		} else {
			//There are validation errors - do not run
			r = null;

		}

		postAction(r != null, e);
		return r;
	}

	@Override
	public R run(Connection readOnlyConnection, Connection readWriteConnection)
			throws Exception {
		if (readOnlyConnection != null) {
			externallyOwnedROConn = true;
			roConn = readOnlyConnection;
		}

		if (readWriteConnection != null) {
			externallyOwnedRWConn = true;
			rwConn = readWriteConnection;
		}

		return run();
	}
	
	@Override
	public Long getModelId() {
		return null;
	}

	protected void preAction() {

		startTime = System.currentTimeMillis();

		invocation = new ActionInvocation(this.getClass());

		if (log.isTraceEnabled()) {
			log.trace("Beginning action for " + this.getClass().getName() +
					".  Run Number: " + runNumber);
		}

		invocation.start();

		//Do validation
		validate();
	}


	/**
	 * An optional message to add to the log on completion.  Only called
	 * if the log level is debug or greater, or the doAction method returned null
	 * (a fail) or threw an exception.  Note that the runNumber is
	 * auto added to the end of the message.
	 *
	 * @return
	 */
	protected String getPostMessage() {
		return postMessage;
	}

	/**
	 * Assigns the post message
	 * @param msg
	 */
	protected void setPostMessage(String msg) {
		postMessage = msg;
	}

	/**
	 * Override to validate the caller supplied parameters.
	 * 
	 * This method is called <em>before</em> initFileds() is called and is
	 * intended to validate the caller parameters only.  It may later not be
	 * possible to initFields(), but that is <i>Exception</i>al.
	 *
	 * Implementations can call addValidationError(), which will be record the
	 * error.  Any recorded errors will prevent initiFields() and doAction()
	 * from being called.  The run() argument will return null and the call
	 * can call get/hasValidationErrors to determine the cause.
	 * 
	 * Note that calling addValidateError() with a null argument will not add
	 * an error message, to simplify implementations.
	 * method will not be called.  The run() method will
	 * return null.
	 *
	 */
	protected void validate() {
		//Default implementation does nothing.
		//By not calling addValidationError, no errors are created.
	}

	/**
	 * Implementations can override to initiate fields they need to do the calculation.
	 * For instance, if they are passed only a model ID, they can load the model
	 * data in this method.
	 * 
	 * This method is called after validate().  
	 * 
	 * @throws Exception If required values cannot be initialized.
	 */
	protected void initFields() throws Exception {
		//Default implementation does nothing.
		//By not calling addValidationError, no errors are created.
	}


	/**
	 * Adds a validation error message.
	 *
	 * Any validation errors will result in the actual doAction() method
	 * not being called and null will be returned from the run() method.
	 *
	 * @param message (ignored if null)
	 */
	protected void addValidationError(String message) {
		if (message != null) {
			validationErrors.add(message);
		}
	}

	/**
	 * Returns a copy of the validation errors.
	 *
	 * @return Never returns null - if no errors, an empty array is returned.
	 */
	public String[] getValidationErrors() {
		return validationErrors.toArray(new String[]{});
	}

	/**
	 * Returns true if there are validation errors.
	 * @return
	 */
	public boolean hasValidationErrors() {
		return validationErrors.size() > 0;
	}
	
	/**
	 * Returns an Exception, if one occurred during the execution of the Action.
	 * 
	 * @return A Throwable or null if one did not occur.
	 */
	public Throwable getException() {
		if (invocation != null) {
			return invocation.getError();
		} else {
			return null;
		}
	}

	/**
	 * This method is called at the completion of the action, regardless of
	 * how it completes.
	 *
	 * This method cleans up resources and does logging for the completion of
	 * the action.  Subclasses may override, but must call the super
	 * implementation to ensure that JDBC resources are cleaned up.
	 * @param success Pass true to indicate that the action completed normally.
	 * @param error Pass an error that might have occurred for logging.
	 */
	protected void postAction(boolean success, Exception error) {

		invocation.setNonNullResponse(success);
		invocation.setError(error);
		if (hasValidationErrors()) invocation.setValidationErrors(getValidationErrors());

		try {
			try {

				//TODO:  Do we really need an inner try block?
				String msg = getPostMessage();
				if (msg == null) {
					msg = "(no message from the action)";
				}

				if (success) {
					if (log.isInfoEnabled()) {
						long totalTime = (System.currentTimeMillis() - startTime) / 1000;
						String tTimeStr = Long.toString(totalTime);

						if (error == null) {
							log.info("Action completed for " + this.getClass().getName() +
									".  Total Time: " + tTimeStr + "secs, Run Number: " +
									runNumber + NL + "Message from Action: " + msg);
						} else {
							log.error("Action completed, but generated an exception for " +
									this.getClass().getName() + " for model " + getModelId() + ".  Total Time: " +
									tTimeStr + "secs, Run Number: " +
									runNumber + NL + "Message from Action: " + msg, error);
						}

					}
				} else {

					if (error != null) {
						log.error("Action FAILED w/ an exception for " +
								this.getClass().getName() + " for model " + getModelId() + ".  Run Number: " +
								runNumber + NL + "Message from Action: " + msg, error);
					} else if (hasValidationErrors()) {
						log.error("Action FAILED due to validation errors for " +
								this.getClass().getName() + " for model " + getModelId() + ".  Run Number: " +
								runNumber + ".  Validation errors follow:");

						for (String valErr : getValidationErrors()) {
							log.error("  Validation Error: " + valErr);
						}

					} else {
						log.error("Action FAILED but did not generate an error for " +
								this.getClass().getName() + " for model " + getModelId() + ".  Run Number: " +
								runNumber + NL + "Message from Action: " + msg);

					}
				}
			} catch (Exception e) {
				//Ignore - some type of loggin err not worth handling
			}


			//Close any open resultSets
			if (autoCloseResults != null) {
				for (ResultSet rs : autoCloseResults) {
					try {
						if (rs != null)	{
							rs.close();
							log.trace("Successfully autoclosed ResultSet ID: " + rs.hashCode());
						}
					} catch (Exception e) {
						log.warn(
								"Good grief, I just tried to close a SQL ResultSet," +
								" should this really throw an exception?  " +
								"ResultSet ID: " + rs.hashCode(), e);
					}
				}

				autoCloseResults.clear();
			}

			//Close any open prepared statements
			if (autoCloseStatements != null) {
				for (Statement ps : autoCloseStatements) {
					try {
						if (ps != null)	{
							ps.close();
							log.trace("Successfully autoclosed statement ID: " + ps.hashCode());
						}
					} catch (Exception e) {
						log.warn(
								"Good grief, I just tried to close a SQL Statement," +
								" should this really throw an exception?  " +
								"Statement ID: " + ps.hashCode(), e);
					}
				}

				autoCloseStatements.clear();
			}

			//Close the connections, if not null
			if (! externallyOwnedROConn) close(roConn);
			if (! externallyOwnedRWConn) close(rwConn);
		} finally {
			invocation.finish();
		}
	}

	/**
	 * Returns a connection intended for read-only access.
	 *
	 * RO access may not be enfored, however, likely there will be two dbs:  a
	 * warehouse db and a transactional db, which may have different tables.
	 * This connection is guaranteed to be closed regardless of how
	 * the action completes.
	 *
	 * This method will reuse the same connection for the same
	 * Action execution if called multiple times during a single run() invocation.
	 * However, this method will check to ensure that the Connection is not
	 * closed before handing it out.
	 *
	 * If a RO connection is passed into the run method, it will not be closed.
	 *
	 * @return A JDBC Connection which will be auto-closed at Action completion.
	 * @throws SQLException
	 */
	protected Connection getROConnection() throws SQLException {
		if (roConn == null) {
			roConn = SharedApplication.getInstance().getROConnection();
		} else {
			if (roConn.isClosed()) {
				roConn = SharedApplication.getInstance().getROConnection();
			}
		}

		return roConn;
	}

	/**
	 * Returns a connection intended for read-only access.
	 *
	 * RO access may not be enfored, however, likely there will be two dbs:  a
	 * warehouse db and a transactional db, which may have different tables.
	 * This connection is guaranteed to be closed regardless of how
	 * the action completes.
	 *
	 * This method will return the same rw connection for the life of the Action,
	 * unless the connection is closed.
	 * If transaction support is needed, simply start a transaction at the beginning
	 * of the action, then call commit or rollback when the action is complete.
	 * If commit is never called, the auto-close of the connection will force
	 * a rollback.
	 *
	 * If a RW connection is passed into the run method, it will not be closed.
	 *
	 * @return A JDBC Connection which will be auto-closed at Action completion.
	 * @throws SQLException
	 */
	protected Connection getRWConnection() throws SQLException {
		if (rwConn == null) {
			rwConn = SharedApplication.getInstance().getRWConnection();
		} else {
			if (rwConn.isClosed()) {
				rwConn = SharedApplication.getInstance().getRWConnection();
			}
		}

		return rwConn;
	}

	/**
	 * Returns a new read-only prepared statement using the passed sql.
	 * This statement and its connection is guaranteed to be closed regardless
	 * of how the action completes.  Any ResultSet created from the Statement
	 * is also close automatically when the Statement is closed (see jdbc docs
	 * for Statement).
	 *
	 * @param sql The SQL statement, which may contain '?' parameters.
	 * @return A JDBC PreparedStatement which will be auto-closed at Action completion.
	 * @throws SQLException
	 */
	protected PreparedStatement getNewROPreparedStatement(String sql) throws SQLException {
		Connection conn = getROConnection();

		PreparedStatement st =
			conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);

		//In general we have a small number of columns in our data.
		//Test results over a DSL+VPN+ssh+Proxifier:
		//Fetch Size | Seconds to return a 2 col data set of a few K rows
		// 10  | 193
		// 100 | 19
		// 200 | 11
		// 400 | 7
		//Note that 10 rows is the default for the oracle jdbc driver (!)
		st.setFetchSize(200);

		addStatementForAutoClose(st);

		return st;
	}

	/**
	 * Returns a new read/write prepared statement using the passed sql.
	 * This statement and its connection is guaranteed to be closed regardless
	 * of how the action completes.  Any ResultSet created from the Statement
	 * is also close automatically when the Statement is closed (see jdbc docs
	 * for Statement).
	 *
	 * @param sql The SQL statement, which may contain '?' parameters.
	 * @return A JDBC PreparedStatement which will be auto-closed at Action completion.
	 * @throws SQLException
	 */
	protected PreparedStatement getNewRWPreparedStatement(String sql) throws SQLException {
		Connection conn = getRWConnection();

		PreparedStatement st =
			conn.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY,
			ResultSet.CONCUR_UPDATABLE, ResultSet.HOLD_CURSORS_OVER_COMMIT);

		st.setFetchSize(200);

		addStatementForAutoClose(st);

		return st;
	}

	/**
	 * Creates a read-only PreparedStatement with named parameter substitutions.
	 *
	 * The params Map may contain single name-value pairs or name-collection pairs.
	 * Names will replace $name$ variables in the SQL string with '?'s, assigning
	 * parameters as needed in the prepared statement.  Collections of values
	 * will be expanded into a list  of '?'s  in the expanded statement (currently
	 * this is intended to be used for an IN clause).
	 *
	 * @param name
	 * @param clazz
	 * @param params String variableName, Object value
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public PreparedStatement getROPSFromPropertiesFile(
			String name, Class<?> clazz, Map<String, Object> params)
			throws Exception {

		//Get the text from the properties file
		String sql = getText(name, (clazz != null)? clazz : this.getClass());
		if(null == sql){
			throw new NameNotFoundException("No query named '" + name + "' was not found in the specified properties file.");
		}
		//Let the other method do the magic
		return getROPSFromString(sql, params);
	}

	/**
	 * Creates a read-write PreparedStatement from a properties file.
	 *
	 * A properties file with a name matching this class is expected, with an
	 * entry matching the passed name.
	 *
	 * The params Map may contain single name-value pairs or name-collection pairs.
	 * Names will replace $name$ variables in the SQL string with '?'s, assigning
	 * parameters as needed in the prepared statement.  Collections of values
	 * will be expanded into a list  of '?'s  in the expanded statement (currently
	 * this is intended to be used for an IN clause).
	 *
	 * @param name
	 * @param clazz
	 * @param params String variableName, Object value
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public PreparedStatement getRWPSFromPropertiesFile(
			String name, Class<?> clazz, Map<String, Object> params)
			throws Exception {

		//Get the text from the properties file
		String sql = getText(name, (clazz != null)? clazz : this.getClass());
		if(null == sql){
			throw new NameNotFoundException("No query named '" + name + "' was not found in the specified properties file.");
		}
		//Let the other method do the magic
		return getRWPSFromString(sql, params);
	}

	/**
	 * Creates a prepared statement from a SQL string (possibly with named params).
	 *
	 * The params Map may contain single name-value pairs or name-collection pairs.
	 * Names will replace $name$ variables in the SQL string with '?'s, assigning
	 * parameters as needed in the prepared statement.  Collections of values
	 * will be expanded into a list  of '?'s  in the expanded statement (currently
	 * this is intended to be used for an IN clause).
	 *
	 * @param sql
	 * @param params
	 * @return
	 * @throws Exception
	 */
	protected PreparedStatement getROPSFromString(
			String sql, Map<String, Object> params)
			throws Exception {

		PreparedStatement statement = null;

		//Go through in order and get the variables, replace with question marks.
		SQLString temp = processSql(sql, params);
		sql = temp.sql.toString();

		//getNewRWPreparedStatement with the processed string
		statement = getNewROPreparedStatement(sql);

		//Add now before assigning params (may bomb during assignment)
		addStatementForAutoClose(statement);

		assignParameters(statement, temp, params);

		return statement;
	}

	/**
	 * Creates a read-write PreparedStatement from a properties file.
	 *
	 * A properties file with a name matching this class is expected, with an
	 * entry matching the passed name.
	 *
	 * The params Map may contain single name-value pairs or name-collection pairs.
	 * Names will replace $name$ variables in the SQL string with '?'s, assigning
	 * parameters as needed in the prepared statement.  Collections of values
	 * will be expanded into a list  of '?'s  in the expanded statement (currently
	 * this is intended to be used for an IN clause).
	 *
	 * @param sql
	 * @param params
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	protected PreparedStatement getRWPSFromString(
			String sql, Map<String, Object> params)
			throws Exception {

		PreparedStatement statement = null;

		//Go through in order and get the variables, replace with question marks.
		SQLString temp = processSql(sql, params);
		sql = temp.sql.toString();

		//getNewRWPreparedStatement with the processed string
		statement = getNewRWPreparedStatement(sql);

		//Add now before assigning params (may bomb during assignment)
		addStatementForAutoClose(statement);

		assignParameters(statement, temp, params);

		return statement;
	}

	/**
	 * Assigns the passed parameters to the PreparedStatement.
	 * The parameters are passed in two forms:  sqlString contains the parameters
	 * by name (only) in the order they need to be placed in the statement, that
	 * is, the order matches the question marks in the statement; params contains
	 * the name and value pairs.
	 *
	 * The params Map may contain collections of values which will be expanded into
	 * a list  of '?'s  in the expanded statement.  Currently this is intended to
	 * be used for an IN clause.
	 *
	 * @param statement
	 * @param sqlString
	 * @param params
	 * @throws SQLException
	 */
	public static void assignParameters(PreparedStatement statement,
			SQLString sqlString, Map<String, Object> params) throws Exception {

		ArrayList<String> variables = new ArrayList<String>();

		//Go through in order and get the variables, replace with question marks.
		variables = sqlString.variables;
		int paramIndex = 1;		//current param index, increment after setting

		for (String variable : variables) {
			if (params.containsKey(variable)) {
				Object valueEntry = params.get(variable);

				//The param value may be a colletion, in which case add an entry for each
				Collection valueColl = null;
				if (valueEntry instanceof Collection) {
					valueColl = (Collection) valueEntry;
				} else {
					valueColl = new ArrayList<Object>();
					valueColl.add(valueEntry);
				}

				for (Object oneValue : valueColl) {
					if (oneValue instanceof String) {
						statement.setString(paramIndex, oneValue.toString());
					} else if (oneValue instanceof Long) {
						statement.setLong(paramIndex, (Long) oneValue);
					} else if (oneValue instanceof Integer) {
						statement.setInt(paramIndex, (Integer) oneValue);
					} else if (oneValue instanceof Float) {
						statement.setFloat(paramIndex, (Float) oneValue);
					} else if (oneValue instanceof Double) {
						statement.setDouble(paramIndex, (Double) oneValue);
					} else if (oneValue instanceof Timestamp) {
						statement.setTimestamp(paramIndex, (Timestamp) oneValue);
					} else if (oneValue instanceof Date) {
						statement.setDate(paramIndex, (Date) oneValue);
					} else if (oneValue instanceof Time) {
						statement.setTime(paramIndex, (Time) oneValue);
					} else if (oneValue instanceof SerializableBlobWrapper) {
						statement.setBytes(paramIndex, ((SerializableBlobWrapper)oneValue).getBytes());
					} else {
						statement.setObject(paramIndex, oneValue);
					}

					paramIndex++;
				}

			} else {
				throw new Exception("The variable '" + variable + "' was not found in the list of SQL parameters.");
			}
		}

	}

	/**
	 * Adds a SQL Statement to a list of statements that will be closed automatically
	 * when the action completes, regardless of how the action completes.
	 *
	 * This is opened up as a protected method so that actions can add their own
	 * statements for which there may not be direct creation support for (i.e.
	 * CallableStatements).
	 *
	 * @param statement
	 */
	protected void addStatementForAutoClose(Statement statement) {
		if (autoCloseStatements == null) {
			autoCloseStatements = new ArrayList<Statement>();
		}

		autoCloseStatements.add(statement);
	}

	/**
	 * Adds a SQL ResultSet to a list of resultSets that will be closed automatically
	 * when the action completes, regardless of how the action completes.
	 *
	 * This is opened up as a protected method so that actions can add their own
	 * results for which there may not be direct creation support for (i.e.
	 * CallableStatements).
	 *
	 * @param statement
	 */
	protected void addResultSetForAutoClose(ResultSet resultSet) {
		if (autoCloseResults == null) {
			autoCloseResults = new ArrayList<ResultSet>();
		}

		autoCloseResults.add(resultSet);
	}

	/**
	 * Quick implementation of what's more or less a "tuple".
	 * no functionality, just storage.
	 *
	 * Created so the process can be testable, without using
	 * side-effects to implement functionality.
	 * @author dmsibley
	 *
	 */
	public static class SQLString {
		public StringBuilder sql;
		public ArrayList<String> variables;

		public SQLString() {
			this.sql = new StringBuilder();
			this.variables = new ArrayList<String>();
		}
	}

	/**
	 * Processes a SQL string template with two types of replacements.
	 *
	 * SQL parameters embedded in the SQL in the form '$sqlParamName$' are
	 * replaced with a '?' and the actual name is pushed into an array list,
	 * in the order it was found.
	 *
	 *
	 * Non-SQL parameters embedded in the SQL in the form '@nonSqlParamName@'
	 * are replaced with the value of the matching parameter from the params
	 * Map.  This can be used to replace table names in the SQL string, or
	 * other pieces of SQL that cannot be parameterized in a prepared statement.
	 * Since the query parameters and the non-query params share the same Map,
	 * the names must be unique.
	 *
	 * @param sqlWithVariables A SQL string template with variables to be replaced.
	 * @param params A map of parameter values.
	 * @return
	 */
	public static SQLString processSql(String sqlWithVariables, Map<String, Object> params) {
		SQLString result = new SQLString();
		char[] sqlChars = sqlWithVariables.toCharArray();
		StringBuilder variableBuffer = new StringBuilder();

		boolean isVariableName = false;
		for (int i = 0; i < sqlChars.length; i++) {
			if (sqlChars[i] != '$') {
				if (isVariableName) {
					//Store the variable
					variableBuffer.append(sqlChars[i]);
				} else {
					//store the sql
					result.sql.append(sqlChars[i]);
				}
			} else {
				//Start or End variable
				if (isVariableName) {
					//End of variable, store the name
					String paramName = variableBuffer.toString();
					result.variables.add(paramName);
					//Clear the buffer
					variableBuffer = new StringBuilder();


					if (params.get(paramName) != null && params.get(paramName) instanceof Collection) {
						//If there is an array for this param, add a list of ?s
						//The only place this should be used is in an IN clause.
						Collection c = (Collection)params.get(paramName);

						if (c.size() > 0) {
							for (Object o : c) {
								result.sql.append("?, ");
							}
							result.sql.delete(result.sql.length() - 2, result.sql.length());
						}

					} else {
						//add ? placeholder
						result.sql.append('?');
					}

				}
				isVariableName = !isVariableName;
			}
		}

		String sql = result.sql.toString();

		for (Entry<String, Object> entry : params.entrySet()) {
			String key = "@" + entry.getKey() + "@";
			if (entry.getValue() != null) {
				sql = sql.replaceAll(key, entry.getValue().toString());
			} else {
				sql = sql.replaceAll(key, "");
			}
		}

		result.sql = new StringBuilder(sql);

		return result;
	}


	/**
	 * Taken From:
	 * Properly closing Connections and ResultSets without throwing exceptions, see the section
	 * "Here is an example of properly written code to use a db connection obtained from a connection pool"
	 * http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html
	 * @param conn
	 */
	protected static void close(Connection conn) {
		if (conn != null) {
			try {
				conn.close();
			} catch (SQLException e) {
				log.error("Unable to close a jdbc connection", e);
			}
			conn = null;
		}
	}

	/**
	 * Taken From:
	 * Properly closing Connections and ResultSets without throwing exceptions, see the section
	 * "Here is an example of properly written code to use a db connection obtained from a connection pool"
	 * http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html
	 *
	 * Note that this method makes no attempt to close the parent connection
	 * or statement.
	 * @param statement
	 */
	protected static void close(Statement statement) {
		if (statement != null) {
			try {
				statement.close();
			} catch (SQLException e) {
				log.error("Unable to close a jdbc statement", e);
			}
			statement = null;
		}
	}

	/**
	 * Taken From:
	 * Properly closing Connections and ResultSets without throwing exceptions, see the section
	 * "Here is an example of properly written code to use a db connection obtained from a connection pool"
	 * http://tomcat.apache.org/tomcat-6.0-doc/jndi-datasource-examples-howto.html
	 *
	 * Note that this method makes no attempt to close the parent connection
	 * or statement.
	 * @param rset
	 */
	protected static void close(ResultSet rset) {
		if (rset != null) {
			try {
				rset.close();
			} catch (SQLException e) {
				log.error("Unable to close a jdbc resultset", e);
			}
			rset = null;
		}
	}


	/**
	 * Loads the named text chunk from the properties file and inserts the named values passed in params.
	 *
	 * This method assumes that the properties are contained in properties file
	 * in the same package and with the same name as the class.  E.g: pkg.MyClass
	 * should have a properties file as: pkg/MyClass.properties.
	 * <br><br>
	 * params are passed in serial pairs as {"name1", "value1", "name2", "value2"}.
	 * toString is called on each item, so it is OK to pass in autobox numerics.
	 * See the DataLoader.properties file for the names of the parameters available
	 * for the requested query.
	 *
	 * @param name	Name of the query in the properties file
	 * @param params	An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 */
	public String getTextWithParamSubstitution(String name, Object... params)
			throws IOException {
		return getTextWithParamSubstitution(name, this.getClass(), params);
	}

	public String getTextWithParamSubstitution(String name, String... params)
	throws IOException {
		return getTextWithParamSubstitution(name, this.getClass(), (Object[]) params);
	}

	/**
	 * Loads the named text chunk from the properties file in the same package
	 * and with the same name as the class. E.g: pkg.MyClass should have a
	 * properties file as: pkg/MyClass.properties.
	 * <br><br>
	 * params are passed in serial pairs as {"name1", "value1", "name2",
	 * "value2"}. toString is called on each item, so it is OK to pass in
	 * autobox numerics. See the DataLoader.properties file for the names of the
	 * parameters available for the requested query.
	 *
	 * @param name
	 *            Name of the query in the properties file
	 * @param clazz
	 * @param params
	 *            An array of name and value objects to replace in the query.
	 * @return
	 * @throws IOException
	 *
	 * TODO move this to a utils class of some sort
	 */
	public static String getTextWithParamSubstitution(String name,
			Class<?> clazz, Object... params) throws IOException {

		String query = getText(name, clazz);

		for (int i=0; i<params.length; i+=2) {
			String n = "$" + params[i].toString() + "$";
			String v = params[i+1].toString();

			query = StringUtils.replace(query, n, v);
		}

		return query;
	}

	/**
	 * Loads the named text chunk from the properties file.
	 *
	 * This method assumes that the properties are contained in properties file
	 * in the same package and with the same name as the class.  E.g: pkg.MyClass
	 * should have a properties file as: pkg/MyClass.properties.
	 *
	 * @param name
	 * @return
	 * @throws IOException
	 * @
	 * TODO move this to a utils class of some sort
	 */
	public String getText(String name) throws IOException {
		return getText(name, this.getClass());
	}

	/**
	 * Loads the named text chunk from a properties file located in the same
	 * package and with the same name as the {@code}clazz{@code} parameter
	 *
	 * @param name of text chunk
	 * @param clazz
	 * @return
	 * @throws IOException
	 */
	public static String getText(String name, Class<?> clazz) throws IOException {
		Properties props = new Properties();

		String path = clazz.getName().replace('.', '/') + ".properties";
		InputStream ins = getResourceAsStream(path);
		props.load(ins);

		return props.getProperty(name);
	}

	/**
	 * Fetches a dataseries related property from the dataseries properties
	 * file.  Each series has a user readable name, or if isDescription, a
	 * description that can be fetched.  If the value is not found, an empty
	 * String is returned.
	 *
	 * @param dataSeriesType
	 * @param isDescription	True to get the description
	 * @return
	 */
	public static String getDataSeriesProperty (DataSeriesType dataSeriesType, boolean isDescription) {
		String name = dataSeriesType.name();
		if (isDescription) name += "_description";
		return getDataSeriesProperty().getProperty(name, "");
	}

	/**
	 * Fetches a dataseries related property from the dataseries properties
	 * file.  Each series has a user readable name, or if isDescription, a
	 * description that can be fetched.  If the value is not found, an empty
	 * String is returned.
	 *
	 * @param name
	 * @param isDescription	True to get the description
	 * @return
	 */
	public static String getDataSeriesProperty (String name, boolean isDescription) throws IOException {
		if (isDescription) name += "_description";
		return getDataSeriesProperty().getProperty(name, "");
	}

	/**
	 * Fetches a dataseries related property from the dataseries properties
	 * file.  Each series has a user readable name, or if isDescription, a
	 * description that can be fetched.
	 *
	 * @param dataSeriesType
	 * @param isDescription	True to get the description
	 * @param defaultValue is returned if no property is found
	 * @return
	 */
	public static String getDataSeriesProperty (DataSeriesType dataSeriesType, boolean isDescription, String defaultValue) throws IOException {
		String name = dataSeriesType.name();
		if (isDescription) name += "_description";
		return getDataSeriesProperty().getProperty(name, defaultValue);
	}

	/**
	 * Fetches a dataseries related property from the dataseries properties
	 * file.  Each series has a user readable name, or if isDescription, a
	 * description that can be fetched.
	 *
	 * @param name
	 * @param isDescription	True to get the description
	 * @param defaultValue is returned if no property is found
	 * @return
	 */
	public static String getDataSeriesProperty (String name, boolean isDescription, String defaultValue) throws IOException {
		if (isDescription) name += "_description";
		return getDataSeriesProperty().getProperty(name, defaultValue);
	}

	private static synchronized Properties getDataSeriesProperty() {
		if (dataSeriesProperties == null) {
			Properties props = new Properties();

			String path = "/gov/usgswim/sparrow/DataSeriesType.properties";
			InputStream ins = getResourceAsStream(path);

			try {
				props.load(ins);
			} catch (IOException e) {
				log.error("Unable to load the Action data series properties file.", e);

			}

			dataSeriesProperties = props;
		}

		return dataSeriesProperties;
	}

	protected static InputStream getResourceAsStream(String path) {
		InputStream ins = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
		if (ins == null) {
			ins = Action.class.getResourceAsStream(path);
		}
		return ins;
	}
	
	/**
	 * Breaks the passed list into sublists of the specified size.
	 * 
	 * This can be used to split query parameters into batches to deal w/ parameter
	 * count limitations (ie, you can only have 1K params in a SQL IN clause).
	 * 
	 * This method uses List.subList(), so the destinationListOfLists is
	 * backed by the sourceList.  Do not modify the source list or the
	 * destination lists will be invalid.
	 * 
	 * @param destinationListOfLists List of Lists to split the lists into
	 * @param sourceList	Source list
	 * @param itemCountInSublists	Number of items allowed in each sublist.
	 */
	protected static <T> void splitListIntoSubLists(List<List<T>> destinationListOfLists,
			List<T> sourceList, int itemCountInSublists) {
		
		List<List<T>> listOfLists = destinationListOfLists;	//shorter name
		

		int startIndexInclusive = 0;
		int endIndexExclusive = 0;	//will be assigned

		while (startIndexInclusive < sourceList.size()) {
			endIndexExclusive = startIndexInclusive + itemCountInSublists;

			if (endIndexExclusive > sourceList.size()) {
				endIndexExclusive = sourceList.size();
			}

			listOfLists.add(sourceList.subList(startIndexInclusive, endIndexExclusive));

			startIndexInclusive = endIndexExclusive;
		}

	}

}
