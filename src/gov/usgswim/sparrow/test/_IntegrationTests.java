package gov.usgswim.sparrow.test;


import gov.usgswim.sparrow.test.parsers.AllParseTests;
import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * Integrated tests that will hit the db.
 * 
 * These are started fresh as of May 28, 2008.
 * 
 * @author eeverman
 *
 */
public class _IntegrationTests {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Integration Tests");

		suite.addTestSuite(LogicalAdjustmentTest.class);
		suite.addTestSuite(ContextToPredictionTest.class);
		suite.addTestSuite(IDServiceTest.class);

		return suite;
	}

	public static void main(String args[]) {
		String args2[] = {"-noloading", "gov.usgswim.sparrow.test._Test"};

		junit.swingui.TestRunner.main(args2);
	}
}
