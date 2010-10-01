package gov.usgswim.sparrow;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.service.SharedApplication;

import java.sql.Connection;
import java.sql.SQLException;

import org.junit.Test;

/**
 * Tests the SparrowDBTest base test class.
 * @author eeverman
 *
 */
public class SparrowDBTestTest extends SparrowDBTest {
	
	@Test
	public void testAvailableConnections() throws Exception {
		
		Connection conn1 = null;
		Connection conn2 = null;
		
		try {
			conn1 = SharedApplication.getInstance().getConnection();
			conn2 = SharedApplication.getInstance().getConnection();
			
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
			conn1 = SharedApplication.getInstance().getConnection();
			conn2 = SharedApplication.getInstance().getConnection();
			conn3 = SharedApplication.getInstance().getConnection();
			
			//Only three connections are available, so this should throw an exception
			conn4 = SharedApplication.getInstance().getConnection();
		} finally {
			SharedApplication.closeConnection(conn1, null);
			SharedApplication.closeConnection(conn2, null);
			SharedApplication.closeConnection(conn3, null);
			SharedApplication.closeConnection(conn4, null);
		}
	}
}
