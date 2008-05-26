package gov.usgswim.sparrow.test.parsers;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class AllParseTests extends TestCase {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Parser Tests");
		suite.addTestSuite(AdjustmentGroupsTest.class);
		suite.addTestSuite(AnalysisTest.class);
		suite.addTestSuite(DefaultGroupTest.class);
		suite.addTestSuite(ParserHelperTest.class);
		suite.addTestSuite(PredictionContextTest.class);
		suite.addTestSuite(ReachGroupTest.class);
		suite.addTestSuite(ResponseFormatTest.class);
		suite.addTestSuite(SelectTest.class);
		suite.addTestSuite(TerminalReachesTest.class);
		return suite;
	}
}
