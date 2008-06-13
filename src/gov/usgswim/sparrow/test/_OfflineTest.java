package gov.usgswim.sparrow.test;


import gov.usgswim.sparrow.test.parsers.AllParseTests;
import gov.usgswim.sparrow.test.service.ModelServiceTest;
import gov.usgswim.sparrow.test.service.PredictServiceTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class _OfflineTest {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Offline Prediction Tests");

		suite.addTestSuite(PredictSimple_Test.class);
		suite.addTestSuite(TabDelimFileUtil_Test.class);
		suite.addTestSuite(ReadStreamAsIntegersTest.class);
		suite.addTestSuite(ReadStreamAsDoubleTest.class);
		suite.addTestSuite(DataTableBuilderTest.class);
		suite.addTestSuite(DataLoaderOfflineTest.class);
		suite.addTestSuite(SourceAdjustments_Test.class);
		suite.addTestSuite(SharedApplicationCaching.class);

		{
			TestSuite nonWorkingTestsSuite = new TestSuite("Prediction Tests");
			nonWorkingTestsSuite.addTestSuite(JDBCUtil_Test.class);
			nonWorkingTestsSuite.addTestSuite(ModelServiceTest.class);
			nonWorkingTestsSuite.addTestSuite(PredictServiceTest.class);
			nonWorkingTestsSuite.addTestSuite(ModelServiceTest.class);
			nonWorkingTestsSuite.addTestSuite(PredictSerializerTest.class);
			nonWorkingTestsSuite.addTestSuite(PredictSerializerTest.class);
			nonWorkingTestsSuite.addTestSuite(PredictSerializerTest.class);
		}

		//parse tests
		suite.addTest(AllParseTests.suite()); // Note: this is the way to add a suite.
		return suite;
	}

	public static void main(String args[]) {
		String args2[] = {"-noloading", "gov.usgswim.sparrow.test._Test"};

		junit.swingui.TestRunner.main(args2);
	}
}
