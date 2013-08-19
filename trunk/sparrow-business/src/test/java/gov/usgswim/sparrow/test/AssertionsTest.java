package gov.usgswim.sparrow.test;

import junit.framework.TestCase;

/**
 * Simple test to ensure that you're running the tests with assertions turned on.
 * @author ilinkuo
 *
 */
public class AssertionsTest extends TestCase {

	public void testIsAssertionsOn() {
		try {
			assert false;
			assertTrue("This junit assertion should not execute if the"
					+ " previous java lang assert worked. "
					+ "You need to run tests with argument -ea", false);
		}catch (AssertionError e) {
			// Success!
		}
	}
}
