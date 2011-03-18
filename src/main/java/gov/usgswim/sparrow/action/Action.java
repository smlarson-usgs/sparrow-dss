package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.domain.DataSeriesType;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

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
	
	//synch access on this class
	private static Properties dataSeriesProperties;
	
	//A string that Action implementations can set to record outcomes for logging
	private String postMessage;
	
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
		
		try {
			r = doAction();
		} catch (Exception ee) {
			e = ee;
		}
		postAction(r != null, e);
		return r;
	}
	
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
	
	protected void preAction() {
		
		if (log.isDebugEnabled()) {
			startTime = System.currentTimeMillis();
			log.debug("Beginning action for " + this.getClass().getName() +
					".  Run Number: " + runNumber);
		}
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
		
		if (success) {
			if (log.isDebugEnabled()) {
				long totalTime = System.currentTimeMillis() - startTime;
				
				String msg = getPostMessage();
				if (msg != null) {
					log.debug(msg + "  (Run Number: " + runNumber + ")");
				}
				
				if (error == null) {
					log.debug("Action completed for " + this.getClass().getName() +
							".  Total Time: " + totalTime + "ms, Run Number: " +
							runNumber);
				} else {
					log.warn("Action completed, but generated an exception for " +
							this.getClass().getName() + ".  Total Time: " +
							totalTime + "ms, Run Number: " +
							runNumber, error);
				}
				
			}
		} else {
			
			String msg = getPostMessage();
			if (msg != null) {
				msg = "  Message from the action: " + msg;
			} else {
				msg = "  (no message from the action)";
			}
			
			if (error != null) {
				log.error("Action FAILED w/ an exception for " +
						this.getClass().getName() + ".  Run Number: " +
						runNumber + msg, error);
			} else {
				log.debug("Action FAILED but did not generate an error for " +
						this.getClass().getName() + ".  Run Number: " +
						runNumber + msg);
			}

		}

		
		//Close any open prepared statements
		if (autoCloseStatements != null) {
			for (Statement ps : autoCloseStatements) {
				try {
					if (ps != null)	ps.close();
				} catch (Exception e) {
					log.warn("Good grief, I just tried to close a SQL Statement, should this really throw an exception?", e);
				}
			}
			
			autoCloseStatements.clear();
		}
		
		//Close the connections, if not null
		if (! externallyOwnedROConn) close(roConn);
		if (! externallyOwnedRWConn) close(rwConn);
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
		
		//Let the other method do the magic
		return getROPSFromString(sql, params);
	}
	
	/**
	 * Creates a read-write PreparedStatement with named parameter substitutions.
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
		
		//Let the other method do the magic
		return getRWPSFromString(sql, params);
	}
	
	protected PreparedStatement getROPSFromString(
			String sql, Map<String, Object> params)
			throws Exception {

		PreparedStatement statement = null;

		//Go through in order and get the variables, replace with question marks.
		SQLString temp = processSql(sql, params);
		sql = temp.sql.toString();
		
		//getNewRWPreparedStatement with the processed string
		statement = getNewROPreparedStatement(sql);
		
		assignParameters(statement, temp, params);
		
		return statement;
	}
	
	/**
	 * Creates a read/writeable prepared statement with named parameter substitutions.
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
	 * @param statement
	 * @param sqlString
	 * @param params
	 * @throws SQLException
	 */
	protected void assignParameters(PreparedStatement statement,
			SQLString sqlString, Map<String, Object> params) throws Exception {
		
		ArrayList<String> variables = new ArrayList<String>();
		
		//Go through in order and get the variables, replace with question marks.
		variables = sqlString.variables;
		
		Iterator<String> it = variables.iterator();
		for (int i = 1; it.hasNext(); i++) {
			String variable = it.next();
			if (params.containsKey(variable)) {
				Object val = params.get(variable);
				
				if (val instanceof String) {
					statement.setString(i, val.toString());
				} else if (val instanceof Long) {
					statement.setLong(i, (Long) val);
				} else if (val instanceof Integer) {
					statement.setInt(i, (Integer) val);
				} else if (val instanceof Float) {
					statement.setFloat(i, (Float) val);
				} else if (val instanceof Double) {
					statement.setDouble(i, (Double) val);
				} else if (val instanceof Timestamp) {
					statement.setTimestamp(i, (Timestamp) val);
				} else if (val instanceof Date) {
					statement.setDate(i, (Date) val);
				} else if (val instanceof Time) {
					statement.setTime(i, (Time) val);
				} else if (val instanceof SerializableBlobWrapper) {
					statement.setBytes(i, ((SerializableBlobWrapper)val).getBytes());
				} else {
					statement.setObject(i, val);
				}
				
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
					result.variables.add(variableBuffer.toString());
					//Clear the buffer
					variableBuffer = new StringBuilder();
					//add ? placeholder
					result.sql.append('?');
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
	
}
