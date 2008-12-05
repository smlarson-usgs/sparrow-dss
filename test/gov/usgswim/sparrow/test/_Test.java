package gov.usgswim.sparrow.test;


import gov.usgswim.sparrow.test.integration._IntegrationTests;
import gov.usgswim.sparrow.test.parsers.IDByPointParserTest;
import gov.usgswim.sparrow.test.service.ModelServiceTest;
import gov.usgswim.sparrow.test.service.PredictServiceTest;
import junit.framework.Test;
import junit.framework.TestSuite;

public class _Test {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Sparrow Tests");
		
		suite.addTest(_OfflineTest.suite()); // Note: this is the way to add a suite.
		
//		suite.addTestSuite(JDBCUtil_Test.class); superseded by DataLoader
		suite.addTestSuite(DataLoaderTest.class);
		suite.addTestSuite(IDByPointParserTest.class);
		
		suite.addTestSuite(ModelServiceTest.class);
		suite.addTestSuite(PredictServiceTest.class);
		
		suite.addTest(_IntegrationTests.suite()); // Note: this is the way to add a suite.
		
		return suite;
	}

	public static void main(String args[]) {
		String args2[] = {"-noloading", "gov.usgswim.sparrow.test._Test"};

		junit.swingui.TestRunner.main(args2);
	}
}
