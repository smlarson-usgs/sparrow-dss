package gov.usgswim.sparrow.test;


import junit.framework.Test;
import junit.framework.TestSuite;

public class _OfflineTest {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Prediction Tests");

		suite.addTestSuite(PredictSimple_Test.class);
		suite.addTestSuite(TabDelimFileUtil_Test.class);
		suite.addTestSuite(ReadStreamAsIntegersTest.class);
		suite.addTestSuite(ReadStreamAsDoubleTest.class);
		suite.addTestSuite(DataTableBuilderTest.class);
		//suite.addTestSuite(JDBCUtil_Test.class);
		suite.addTestSuite(DataLoaderOfflineTest.class);
		suite.addTestSuite(SourceAdjustments_Test.class);
		//suite.addTestSuite(DomainSerializerTest.class);
		suite.addTestSuite(PredictSerializerTest.class);
		suite.addTestSuite(IDByPointParserTest.class);

		//suite.addTestSuite(ModelServiceTest.class);
		//suite.addTestSuite(PredictServiceTest.class);

		
		suite.addTestSuite(SharedApplicationCaching.class);

		return suite;
	}

	public static void main(String args[]) {
		String args2[] = {"-noloading", "gov.usgswim.sparrow.test._Test"};

		junit.swingui.TestRunner.main(args2);
	}
}
