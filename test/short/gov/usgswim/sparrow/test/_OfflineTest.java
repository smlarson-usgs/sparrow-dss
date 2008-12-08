package gov.usgswim.sparrow.test;


import gov.usgswim.sparrow.test.cachefactory.BinningFactoryTest;
//import gov.usgswim.sparrow.test.misc.BigDecimalTest;
import gov.usgswim.sparrow.test.parsers.AllParseTests;
import junit.framework.Test;
import junit.framework.TestSuite;

public class _OfflineTest {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Offline Prediction Tests");
		suite.addTestSuite(AssertionsTest.class);
		suite.addTest(AllParseTests.suite()); // Note: this is the way to add a suite.
		
		suite.addTestSuite(SharedApplicationCaching.class);
		suite.addTestSuite(PredictSimple_Test.class);
		suite.addTestSuite(SourceAdjustments_Test.class);


		{
			// TODO figure out whether these are still needed and make them work
			TestSuite nonWorkingTestsSuite = new TestSuite("nonworking Tests");
			nonWorkingTestsSuite.addTestSuite(DataLoaderOfflineTest.class);
			nonWorkingTestsSuite.addTestSuite(JDBCUtil_Test.class);
			nonWorkingTestsSuite.addTestSuite(PredictSerializerTest.class);
		}

		
		suite.addTestSuite(BinningFactoryTest.class);
		//suite.addTestSuite(BigDecimalTest.class);
		//parse tests
		return suite;

		
	}

	public static void main(String args[]) {
		String args2[] = {"-noloading", "gov.usgswim.sparrow.test._Test"};

		junit.swingui.TestRunner.main(args2);
	}
}
