package gov.usgswim.sparrow;

import gov.usgswim.sparrow.test.SparrowTestBase;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.action.CalcPrediction;
import gov.usgswim.sparrow.action.CalcPrediction;
import gov.usgswim.sparrow.action.LoadModelPredictData;
import gov.usgswim.sparrow.action.LoadModelPredictData;
import gov.usgswim.sparrow.datatable.PredictResult;

import org.junit.Test;

/**
 * Tests the SparrowUnitTestBaseClass base class by validating the data loaded from
 * text files against the data loaded from the db.
 * @author eeverman
 *
 */
public class SparrowTestBaseLongRunTest extends SparrowTestBaseWithDB {
	@Test
	public void testLoadPredictResultsFromFile() throws Exception {
		PredictData filePredictData = SparrowTestBase.getTestModelPredictData();
		PredictResult filePredictResult = SparrowTestBase.getTestModelPredictResult();
		CalcPrediction fileCalc = new CalcPrediction(filePredictData);
		PredictResult fileBasedPredictResult = fileCalc.run();
		
		
		LoadModelPredictData loader = new LoadModelPredictData(TEST_MODEL_ID);
		PredictData dbPredictData = loader.run();
		CalcPrediction dbCalc = new CalcPrediction(dbPredictData);
		PredictResult dbPredictResult = dbCalc.run();
		
		assertTrue( compareTables(dbPredictResult, fileBasedPredictResult, true, .000001d) );
		assertTrue( compareTables(dbPredictResult, filePredictResult, true, .000001d) );
		
	}
}
