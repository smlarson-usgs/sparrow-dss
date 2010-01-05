/**
 * 
 */
package gov.usgswim.sparrow;

import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * A base test class that sets properties needed for a db connection.
 * @author eeverman
 *
 */
public class SparrowDBTest {

	private static Connection sparrowDBTestConn;
	
	protected static Logger log =
		Logger.getLogger(SparrowDBTest.class); //logging for this class
	
	//Application lifecycle listener handles startup / shutdown
	static LifecycleListener lifecycle = new LifecycleListener();
	
	@BeforeClass
	public static void sparrowDBTestSetUp() throws Exception {
		
		//Turns on detailed logging
		log.setLevel(Level.DEBUG);
		
		//Generically turn on logging for Actions
		log.getLogger(Action.class).setLevel(Level.DEBUG);
		
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
		System.setProperty("dburl", "jdbc:oracle:thin:@igsarmewdbdev.er.usgs.gov:1521:widev");
		System.setProperty("dbuser", "sparrow_dss");
		System.setProperty("dbpass", "***REMOVED***");
	}
	
}
