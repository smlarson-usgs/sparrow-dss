package gov.usgswim.sparrow.test.basic;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllBasicTests extends TestCase {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Basic Tests -- fix these first if broken");
		
		suite.addTestSuite(TabDelimFileUtil_Test.class);
		suite.addTestSuite(ReadStreamAsIntegersTest.class);
		suite.addTestSuite(ReadStreamAsDoubleTest.class);
		suite.addTestSuite(DataTableBuilderTest.class);
		
		return suite;
	}
}
