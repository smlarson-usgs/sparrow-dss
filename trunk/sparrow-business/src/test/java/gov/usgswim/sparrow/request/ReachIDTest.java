package gov.usgswim.sparrow.request;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eeverman
 */
public class ReachIDTest {

	@Test
	public void equalReachIdsShouldBeEqual() throws Exception {
		ReachID rid1 = new ReachID(50L, 99L);
		ReachID rid2 = new ReachID(50L, 99L);
		assertTrue(rid1.equals(rid2));
		assertEquals(rid1.hashCode(), rid2.hashCode());
	}
	
	@Test
	public void nonEqualReachIdsShouldBeNonEqual() throws Exception {
		ReachID rid1 = new ReachID(50L, 98L);
		ReachID rid2 = new ReachID(50L, 99L);
		assertFalse(rid1.equals(rid2));
		assertFalse(rid1.hashCode() == rid2.hashCode());
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void nullReachIdIllegal() throws Exception {
		ReachID rid1 = new ReachID(50L, (Long)null);
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void nullModelIdIllegal() throws Exception {
		ReachID rid1 = new ReachID(null, 99L);
	}
}
