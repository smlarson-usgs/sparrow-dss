package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

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
	
	/**
	 * A shared run count that increments when a new instance is created.
	 */
	private static int staticRunCount = 0;
	
	/**
	 * The run number of the current instance, based on staticRunCount.
	 */
	private int runNumber = 0;
	
	/** A connection that will be closed when the action completes */
	private Connection conn = null;
	
	private long startTime;		//Time the action starts
	
	private List<PreparedStatement> preparedStatements;
	
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
		return null;
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
		if (preparedStatements != null) {
			for (PreparedStatement ps : preparedStatements) {
				try {
					ps.close();
				} catch (Exception e) {
					log.warn("Good grief, I just tried to close a preparedstatement, should this really throw an exception?", e);
				}
			}
			
			preparedStatements.clear();
		}
		
		//Close the connection, if not null
		close(conn);
	}
	
	/**
	 * Returns a connection which is guaranteed to be closed regardless of how
	 * the action completes.
	 * 
	 * In generally this method will reuse the same connection for the same
	 * Action execution if called multiple times during a single run() invocation.
	 * However, this method will check to ensure that the Connection is not
	 * closed before handing it out.
	 * 
	 * @return A JDBC Connection which will be auto-closed at Action completion.
	 * @throws SQLException
	 */
	protected Connection getConnection() throws SQLException {
		if (conn == null) {
			conn = SharedApplication.getInstance().getConnection();
		} else {
			if (conn.isClosed()) {
				conn = SharedApplication.getInstance().getConnection();
			}
		}
		
		return conn;
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
		Connection conn = getConnection();
		
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
		
		if (preparedStatements == null) {
			preparedStatements = new ArrayList<PreparedStatement>();
		}
		
		preparedStatements.add(st);
		
		return st;
	}
	
	/**
	 * Meant to be a PreparedStatement version of getTextWithParamSubstitution(...).
	 * @param name
	 * @param clazz
	 * @param params String variableName, Object value
	 * @return
	 * @throws SQLException
	 * @throws IOException
	 */
	public PreparedStatement getPSFromPropertiesFile(String name, Class<?> clazz, Map<String, Object> params) throws SQLException, IOException {
		//Get the text from the properties file
		String sql = getText(name, (clazz != null)? clazz : this.getClass());
		
		//Let the other method do the magic
		return getPSFromString(sql, params);
	}
	
	protected PreparedStatement getPSFromString(String sql, Map<String, Object> params) throws SQLException, IOException {
		PreparedStatement result = null;
		ArrayList<String> variables = new ArrayList<String>();
		
		//Go through in order and get the variables, replace with question marks.
		SQLString temp = processSql(sql, params);
		sql = temp.sql.toString();
		variables = temp.variables;
		
		//getNewROPreparedStatement with the readied string
		result = getNewROPreparedStatement(sql);
		
		//Set the params with setObject
		Iterator<String> it = variables.iterator();
		for (int i = 1; it.hasNext(); i++) {
			String variable = it.next();
			if (params.containsKey(variable)) {
				Object val = params.get(variable);
				
				if (val instanceof String) {
					result.setString(i, val.toString());
				} else if (val instanceof Long) {
					result.setLong(i, (Long) val);
				} else if (val instanceof Integer) {
					result.setInt(i, (Integer) val);
				} else if (val instanceof Float) {
					result.setFloat(i, (Float) val);
				} else if (val instanceof Double) {
					result.setDouble(i, (Double) val);
				} else {
					result.setObject(i, val);
				}
				
			}
		}
		
		return result;
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
	 * other pieces of SQL that cannot be parameterized in a prepared statemetn.
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
			sql = sql.replaceAll(key, entry.getValue().toString());
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
	public static String getDataSeriesProperty (DataSeriesType dataSeriesType, boolean isDescription) throws IOException {
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
	
	private static synchronized Properties getDataSeriesProperty() throws IOException{
		if (dataSeriesProperties == null) {
			Properties props = new Properties();
			
			String path = "/gov/usgswim/sparrow/DataSeriesType.properties";
			InputStream ins = getResourceAsStream(path);

			props.load(ins);
			
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
