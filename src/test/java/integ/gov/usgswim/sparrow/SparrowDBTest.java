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

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.AfterClass;
import org.junit.Before;

/**
 * A base test class that sets properties needed for a db connection, cache
 * configuration and other application lifecycle aspects.
 * 
 * Crazy Aspects:
 * JUnit has some nasty features.  There are BeforeClass & AfterClass annotations,
 * however, its not possible to allow these methods to be overridden so there
 * are places where this doesn't work for us (in particular, one aspect of the
 * lifecycle cannot be used for Services so it needs to be overridden by the
 * SparrowServiceTest class).  I tried using a 'firstRun' flag to simualte this
 * feature, however, firstrun is always reset to true because another JUnit
 * 'feature' is to create a new instance for every test.  Thus, all instance
 * variables are always reset.
 * 
 * So...  This class uses static variables as if they were instance variables.
 * This *should* work as long as JUnit is single threaded in running the tests
 * (a requirement that I think is pretty safe).
 * 
 * @author eeverman
 *
 */
public class SparrowDBTest extends SparrowUnitTest {

	/**
	 * Name of a system property that if "true" will switch to the production
	 * db and prompt for a password.
	 */
	public static final String SYS_PROP_USE_PRODUCTION_DB = "USE_PRODUCTION_DB";
	
	/** A connection, shared for this class and autoclosed */
	private static Connection sparrowDBTestConn;

	
	/** A single instance which is destroyed in teardown */
	private static SparrowDBTest singleInstanceToTearDown;

	
	public void doOneTimeCustomSetup() throws Exception {
		doDbSetup();
		singleInstanceToTearDown = this;
	}
	
	public void doOneTimeCustomTearDown() throws Exception {
		singleInstanceToTearDown.doDbTearDown();
		singleInstanceToTearDown = null;
	}
	
	protected void doDbSetup() throws Exception {
		if (System.getProperty("dburl") == null) {
			setDbProperties();
		}
	}
	
	protected void doDbTearDown() throws Exception {
		if (sparrowDBTestConn != null) {
			sparrowDBTestConn.close();
			sparrowDBTestConn = null;
		}
	}
	
	public static Connection getConnection() throws SQLException {
		if (sparrowDBTestConn == null || sparrowDBTestConn.isClosed()) {
			sparrowDBTestConn = SharedApplication.getInstance().getConnection();
		}
		
		return sparrowDBTestConn;
	}
	
	protected static void setDbProperties() throws IOException {
		
		
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
	
	protected static String prompt(String prompt) throws IOException {

		// prompt the user to enter their name
		System.out.print(prompt);

		// open up standard input
		BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

		String val = null;
		val = br.readLine();
		return val;
	}
	
	/**
	 * Constructs a XmlUnit Diff object for the two passed xml strings.  The
	 * comparison ignores the 'context-id' element so that responses are less
	 * sensitive to PredictionContext implememntation changes.
	 * 
	 * @param controlDocument
	 * @param testDocument
	 * @return
	 * @throws Exception
	 */
	public static Diff compareXMLIgnoreContextId(String controlDocument,
			String testDocument) throws Exception {
		
		Diff diff = new Diff(controlDocument, testDocument);
		diff.overrideDifferenceListener(new IgnoreContextIdDifferenceListener());
		return diff;
	}
	
	public static boolean similarXMLIgnoreContextId(String controlDocument,
			String testDocument) throws Exception {
		Diff diff = compareXMLIgnoreContextId(controlDocument, testDocument);
		return diff.similar();
	}
	
}
