package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.action.LoadModelPredictData;

import java.io.IOException;

import junit.framework.TestCase;


public class LoadModelPredictDataOfflineTest extends TestCase {
	public LoadModelPredictDataOfflineTest(String sTestName) {
		super(sTestName);
	}

	public void testGetSelectReachCoefQueryWithParams() throws IOException {
		String query = LoadModelPredictData.getTextWithParamSubstitution("SelectReachCoef", LoadModelPredictData.class,
				new Object[] {"ModelId", 999, "Iteration", 888, "SourceId", 777});

		String expected =
		"SELECT coef.VALUE AS Value, rch.Identifier as Identifier " +
		"FROM SOURCE_REACH_COEF coef INNER JOIN MODEL_REACH rch " +
		"ON coef.MODEL_REACH_ID = rch.MODEL_REACH_ID " +
		"WHERE rch.SPARROW_MODEL_ID = 999 AND coef.Iteration = 888 " +
		"AND coef.SOURCE_ID = 777 ORDER BY rch.HYDSEQ, rch.IDENTIFIER";

		assertEquals("Has the query changed?", expected, query);
	}
	
	public void testPrintSample() {
		
	}

}
