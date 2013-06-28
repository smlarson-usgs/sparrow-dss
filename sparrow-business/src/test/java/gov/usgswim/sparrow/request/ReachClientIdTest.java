package gov.usgswim.sparrow.request;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author eeverman
 */
public class ReachClientIdTest {

	@Test
	public void equalReachClientIdsShouldBeEqual() throws Exception {
		ReachClientId rid1 = new ReachClientId(50L, "99L");
		ReachClientId rid2 = new ReachClientId(50L, "99L");
		assertTrue(rid1.equals(rid2));
		assertEquals(rid1.hashCode(), rid2.hashCode());
	}
	
	@Test
	public void nonEqualReachClientIdsShouldBeNonEqual() throws Exception {
		ReachClientId rid1 = new ReachClientId(50L, "98L");
		ReachClientId rid2 = new ReachClientId(50L, "99L");
		assertFalse(rid1.equals(rid2));
		assertFalse(rid1.hashCode() == rid2.hashCode());
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void nullReachFullIdIllegal() throws Exception {
		ReachClientId rid1 = new ReachClientId(50L, (String)null);
	}
	
	@Test(expected = IllegalArgumentException.class) 
	public void nullModelIdIllegal() throws Exception {
		ReachClientId rid1 = new ReachClientId(null, "test");
	}
	
}
