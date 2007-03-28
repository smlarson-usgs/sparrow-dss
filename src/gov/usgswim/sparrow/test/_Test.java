package gov.usgswim.sparrow.test;

import gov.usgswim.sparrow.Data2DView_Test;
import gov.usgswim.sparrow.PredictSimple_Test;
import gov.usgswim.sparrow.util.*;

import junit.framework.Test;
import junit.framework.TestSuite;

public class _Test {
	public static Test suite() {
		TestSuite suite;
		suite = new TestSuite("Prediction Tests");
		
		

		suite.addTestSuite(PredictSimple_Test.class);
	  suite.addTestSuite(TabDelimFileUtil_Test.class);
	  suite.addTestSuite(Data2DView_Test.class);
		suite.addTestSuite(JDBCUtil_Test.class);
	  
		return suite;
	}

	public static void main(String args[]) {
		String args2[] = {"-noloading", "gov.usgswim.sparrow.test._Test"};

		junit.swingui.TestRunner.main(args2);
	}
}
