package gov.usgswim.sparrow;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

/**
 * Tests the SparrowDBTestBaseClass base test class.
 * @author eeverman
 *
 */
public class SparrowTestBaseWithDBLongRunTest extends SparrowTestBaseWithDB {
	
	@Test
	public void testAvailableConnections() throws Exception {
		
		Connection conn1 = null;
		Connection conn2 = null;
		
		try {
			conn1 = SharedApplication.getInstance().getROConnection();
			conn2 = SharedApplication.getInstance().getROConnection();
			
			assertNotNull(conn1);
			assertNotNull(conn2);
			assertTrue(! conn1.equals(conn2));
		} finally {
			SharedApplication.closeConnection(conn1, null);
			SharedApplication.closeConnection(conn2, null);
		}
		
	}
	
	
	/**
	 * Test that we have exactly three connections available
	 */
	@Test(expected=SQLException.class)
	public void testAvailableConnectionCount() throws Exception {
		
		Connection conn1 = null;
		Connection conn2 = null;
		Connection conn3 = null;
		Connection conn4 = null;
		
		try {
			conn1 = SharedApplication.getInstance().getROConnection();
			conn2 = SharedApplication.getInstance().getROConnection();
			conn3 = SharedApplication.getInstance().getROConnection();
			
			//Only three connections are available, so this should throw an exception
			conn4 = SharedApplication.getInstance().getROConnection();
		} finally {
			SharedApplication.closeConnection(conn1, null);
			SharedApplication.closeConnection(conn2, null);
			SharedApplication.closeConnection(conn3, null);
			SharedApplication.closeConnection(conn4, null);
		}
	}
}
