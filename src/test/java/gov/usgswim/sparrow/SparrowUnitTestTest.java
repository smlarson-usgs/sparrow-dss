package gov.usgswim.sparrow;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.action.CalcPrediction;
import gov.usgswim.sparrow.datatable.DataTableCompare;
import gov.usgswim.sparrow.datatable.PredictResult;
import gov.usgswim.sparrow.datatable.PredictResultImm;

import java.util.ArrayList;

import org.junit.Test;


/**
 * This test just tests the self consistency of the predict results loaded
 * from the model 50 predict.txt file.
 * @author eeverman
 *
 */
public class SparrowUnitTestTest extends SparrowUnitTest {

	@Test
	public void testLoadPredictResultsFromFile() throws Exception {
		PredictData filePredictData = SparrowUnitTest.getTestModelPredictData();
		PredictResult filePredictResult = SparrowUnitTest.getTestModelPredictResult();
		
		
		CalcPrediction action = new CalcPrediction(filePredictData);
		
		PredictResult calcResult = action.run();
		
		double delta = .0000000001d;
		for (int row = 0; row < calcResult.getRowCount(); row++) {
		
			double incTotal = calcResult.getIncremental(row);
			double inc1 = calcResult.getIncrementalForSrc(row, 1L);
			double inc2 = calcResult.getIncrementalForSrc(row, 2L);
			double inc3 = calcResult.getIncrementalForSrc(row, 3L);
			double inc4 = calcResult.getIncrementalForSrc(row, 4L);
			double inc5 = calcResult.getIncrementalForSrc(row, 5L);
			
			assertEquals(incTotal, inc1 + inc2 + inc3 + inc4 + inc5, delta);
			
			double totTotal = calcResult.getTotal(row);
			double tot1 = calcResult.getTotalForSrc(row, 1L);
			double tot2 = calcResult.getTotalForSrc(row, 2L);
			double tot3 = calcResult.getTotalForSrc(row, 3L);
			double tot4 = calcResult.getTotalForSrc(row, 4L);
			double tot5 = calcResult.getTotalForSrc(row, 5L);
			
			assertEquals(totTotal, tot1 + tot2 + tot3 + tot4 + tot5, delta);
		}
		
		assertTrue( compareTables(filePredictResult, calcResult, true, .000001d) );
		
		
	}
	
	@Test
	public void testIsEquals() {
		double allow = .1d;
		
		//Strings
		assertTrue(SparrowUnitTest.isEqual(null, null, allow));
		assertTrue(SparrowUnitTest.isEqual("hi", "hi", allow));
		assertFalse(SparrowUnitTest.isEqual("hi", "bye", allow));
		assertFalse(SparrowUnitTest.isEqual("hi", null, allow));
		assertFalse(SparrowUnitTest.isEqual(null, "hi", allow));
		
		//Doubles
		assertTrue(SparrowUnitTest.isEqual(new Double(5), new Double(5), allow));
		assertTrue(SparrowUnitTest.isEqual(new Double(5), new Double(5.49d), allow));
		assertFalse(SparrowUnitTest.isEqual(new Double(5), new Double(5.5d), allow));
		assertTrue(SparrowUnitTest.isEqual(new Double(-5), new Double(-5.49d), allow));
		assertFalse(SparrowUnitTest.isEqual(new Double(-5), new Double(-5.5d), allow));
		assertTrue(SparrowUnitTest.isEqual(new Double(0), new Double(0), allow));
		
		//Odd value Doubles
		assertTrue(SparrowUnitTest.isEqual(Double.NaN, Double.NaN, allow));
		assertFalse(SparrowUnitTest.isEqual(Double.NaN, new Double(5), allow));
		assertFalse(SparrowUnitTest.isEqual(new Double(5), Double.NaN, allow));
		assertFalse(SparrowUnitTest.isEqual(null, Double.NaN, allow));
		assertFalse(SparrowUnitTest.isEqual(Double.NaN, null, allow));
		assertTrue(SparrowUnitTest.isEqual(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, allow));
		assertTrue(SparrowUnitTest.isEqual(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, allow));
		assertFalse(SparrowUnitTest.isEqual(Double.POSITIVE_INFINITY, new Double(5), allow));
		assertFalse(SparrowUnitTest.isEqual(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, allow));
		assertFalse(SparrowUnitTest.isEqual(Double.POSITIVE_INFINITY, null, allow));
	}
}
