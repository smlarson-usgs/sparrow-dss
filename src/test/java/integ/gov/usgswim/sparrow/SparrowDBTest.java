/**
 * 
 */
package gov.usgswim.sparrow;

import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.service.SharedApplication;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.apache.log4j.PatternLayout;

/**
 * A base test class that sets properties needed for a db connection.
 * @author eeverman
 *
 */
public class SparrowDBTest extends TestHelper {

	/**
	 * Name of a system property that if "true" will switch to the production
	 * db and prompt for a password.
	 */
	private static final String SYS_PROP_USE_PRODUCTION_DB = "USE_PRODUCTION_DB";
	
	/** The model ID of MRB2 in the test db */
	public static final Long TEST_MODEL_ID = 50L;
	
	/** A connection, shared for this class and autoclosed */
	private static Connection sparrowDBTestConn;
	
	/** lifecycle listener handles startup / shutdown */
	static LifecycleListener lifecycle = new LifecycleListener();
	
	//True until the firstRun is complete (used for onetime init)
	//Cannot use the @BeforeClass since we need the ability to override methods.
	private static boolean firstRun = true;

	
	@Before
	public void sparrowDBTestSetUp() throws Exception {
		if (firstRun) {
			doLogSetup();
			doLifecycleSetup();
			doDbSetup();
			doGeneralSetup();
		}
	}
	
	@After
	public void sparrowDBTestTearDown() throws Exception {
		if (firstRun) {
			firstRun = false;
			doLogTearDown();
			doLifecycleTearDown();
			doDbTearDown();
			doGeneralTearDown();
		}
	}
	
	protected void doLogSetup() {
		setLogLevel(Level.ERROR);
	}
	
	protected void doLifecycleSetup() {
		lifecycle.contextInitialized(null, true);
	}
	
	protected void doDbSetup() throws Exception {
		if (System.getProperty("dburl") == null) {
			setProperties();
		}
	}
	
	protected void doGeneralSetup() {
		XMLUnit.setIgnoreWhitespace(true);
		XMLUnit.setIgnoreComments(true);
	}
	
	protected void doLogTearDown() {
		//nothing to do
	}
	
	protected void doLifecycleTearDown() {
		lifecycle.contextDestroyed(null, true);
	}
	
	protected void doDbTearDown() throws Exception {
		if (sparrowDBTestConn != null) {
			sparrowDBTestConn.close();
			sparrowDBTestConn = null;
		}
	}
	
	protected void doGeneralTearDown() {
		//nothing to do
	}
	
	public static Connection getConnection() throws SQLException {
		
		if (sparrowDBTestConn == null || sparrowDBTestConn.isClosed()) {
			
			sparrowDBTestConn = SharedApplication.getInstance().getConnection();
		}
		
		return sparrowDBTestConn;
	}
	
	protected static void setProperties() throws IOException {
		
		
		String strUseProd = System.getProperty(SYS_PROP_USE_PRODUCTION_DB);
		boolean useProd = false;
		
		if (strUseProd != null) {
			strUseProd = strUseProd.toLowerCase();
			if ("yes".equals(strUseProd) || "true".equals(strUseProd)) {
				useProd = true;
			}
		}
		
		if (! useProd) {
			//130.11.165.154
			//igsarmewdbdev.er.usgs.gov
			System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.154:1521:widev");
			System.setProperty("dbuser", "sparrow_dss");
			System.setProperty("dbpass", "***REMOVED***");
		} else {
			
			String pwd = prompt(SYS_PROP_USE_PRODUCTION_DB +
					" is set to 'true', requesting the production db be used." +
					" Enter the production db password: ");
			
			//Production Properties
			System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.152:1521:widw");
			System.setProperty("dbuser", "sparrow_dss");
			System.setProperty("dbpass", pwd);
		}
	}
	
	protected static void setLogLevel(Level level) {
		//Turns on detailed logging
		log.setLevel(level);
		
		//Generically set level for all Actions
		Logger.getLogger(Action.class).setLevel(level);
	}
	
	protected static String prompt(String prompt) throws IOException {

		// prompt the user to enter their name
		System.out.print(prompt);

		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String val = null;
		val = br.readLine();
		return val;
	}
	
}
