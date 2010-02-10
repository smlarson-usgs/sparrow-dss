/**
 * 
 */
package gov.usgswim.sparrow;

import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.apache.log4j.PatternLayout;

/**
 * A base test class that sets properties needed for a db connection.
 * @author eeverman
 *
 */
public class SparrowDBTest {

	/** The model ID of MRB2 in the test db */
	public static final Long TEST_MODEL_ID = 50L;
	
	/** A connection, shared for this class and autoclosed */
	private static Connection sparrowDBTestConn;
	
	/** Logging.  Log messages will use the name of the subclass */
	protected static Logger log =
		Logger.getLogger(SparrowDBTest.class); //logging for this class
	
	/** lifecycle listener handles startup / shutdown */
	static LifecycleListener lifecycle = new LifecycleListener();

	
	@BeforeClass
	public static void sparrowDBTestSetUp() throws Exception {
		
		//log.getAppender("dest1").setLayout(arg0)
		
		//Turns on detailed logging
		setLogLevel(Level.ERROR);
		
		lifecycle.contextInitialized(null, true);
		
		//For comparing xml docs
		XMLUnit.setIgnoreWhitespace(true);
		
		if (System.getProperty("dburl") == null) {
			setProperties();
		}
	}

	@AfterClass
	public static void sparrowDBTestTearDown() throws Exception {
		lifecycle.contextDestroyed(null, true);
		
		if (sparrowDBTestConn != null) {
			if (! sparrowDBTestConn.isClosed()) {
				sparrowDBTestConn.close();
				sparrowDBTestConn = null;
			}
		}
	}
	
	public static Connection getConnection() throws SQLException {
		
		if (sparrowDBTestConn == null || sparrowDBTestConn.isClosed()) {
			
			sparrowDBTestConn = SharedApplication.getInstance().getConnection();
		}
		
		return sparrowDBTestConn;
	}
	
	protected static void setProperties() {
		//130.11.165.154
		//igsarmewdbdev.er.usgs.gov
		System.setProperty("dburl", "jdbc:oracle:thin:@130.11.165.154:1521:widev");
		System.setProperty("dbuser", "sparrow_dss");
		System.setProperty("dbpass", "***REMOVED***");
	}
	
	protected static void setLogLevel(Level level) {
		//Turns on detailed logging
		log.setLevel(level);
		
		//Generically set level for all Actions
		Logger.getLogger(Action.class).setLevel(level);
	}
	
}
