package gov.usgswim.sparrow;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.action.CalcPrediction;
import gov.usgswim.sparrow.datatable.PredictResult;

import org.junit.Test;


/**
 * This test just tests the self consistency of the predict results loaded
 * from the model 50 predict.txt file.
 * @author eeverman
 *
 */
public class SparrowTestBaseTest extends SparrowTestBase {

	@Test
	public void testLoadPredictResultsFromFile() throws Exception {
		PredictData filePredictData = SparrowTestBase.getTestModelPredictData();
		PredictResult filePredictResult = SparrowTestBase.getTestModelPredictResult();
		
		
		CalcPrediction action = new CalcPrediction(filePredictData);
		
		PredictResult calcResult = action.run();
		
		int equalDecayNoDecayCount = 0;
		
		double delta = .00000001d;
		for (int row = 0; row < calcResult.getRowCount(); row++) {
		
			double incTotal = calcResult.getIncremental(row);
			double inc1 = calcResult.getIncrementalForSrc(row, 1L);
			double inc2 = calcResult.getIncrementalForSrc(row, 2L);
			double inc3 = calcResult.getIncrementalForSrc(row, 3L);
			double inc4 = calcResult.getIncrementalForSrc(row, 4L);
			double inc5 = calcResult.getIncrementalForSrc(row, 5L);
			
			//Double check that the column mapping are correct
			assertEquals(calcResult.getIncrementalColForSrc(1L), calcResult.getFirstIncrementalColForSrc());
			assertEquals(incTotal, calcResult.getDouble(row, calcResult.getIncrementalCol()), delta);
			assertEquals(inc1, calcResult.getDouble(row, calcResult.getIncrementalColForSrc(1L)), delta);
			assertEquals(inc5, calcResult.getDouble(row, calcResult.getIncrementalColForSrc(5L)), delta);
			
			assertEquals(incTotal, inc1 + inc2 + inc3 + inc4 + inc5, delta);
			
			double decIncTotal = calcResult.getDecayedIncremental(row);
			double decInc1 = calcResult.getDecayedIncrementalForSrc(row, 1L);
			double decInc2 = calcResult.getDecayedIncrementalForSrc(row, 2L);
			double decInc3 = calcResult.getDecayedIncrementalForSrc(row, 3L);
			double decInc4 = calcResult.getDecayedIncrementalForSrc(row, 4L);
			double decInc5 = calcResult.getDecayedIncrementalForSrc(row, 5L);
			
			//Double check that the column mapping are correct
			assertEquals(calcResult.getDecayedIncrementalColForSrc(1L), calcResult.getFirstDecayedIncrementalColForSrc());
			assertEquals(decIncTotal, calcResult.getDouble(row, calcResult.getDecayedIncrementalCol()), delta);
			assertEquals(decInc1, calcResult.getDouble(row, calcResult.getDecayedIncrementalColForSrc(1L)), delta);
			assertEquals(decInc5, calcResult.getDouble(row, calcResult.getDecayedIncrementalColForSrc(5L)), delta);
			
			assertEquals(decIncTotal, decInc1 + decInc2 + decInc3 + decInc4 + decInc5, delta);
			
			assertEquals(
					decIncTotal,
					incTotal * filePredictData.getDelivery().getDouble(row, PredictData.INSTREAM_DECAY_COL),
					delta);
			
			
			
			double totTotal = calcResult.getTotal(row);
			double tot1 = calcResult.getTotalForSrc(row, 1L);
			double tot2 = calcResult.getTotalForSrc(row, 2L);
			double tot3 = calcResult.getTotalForSrc(row, 3L);
			double tot4 = calcResult.getTotalForSrc(row, 4L);
			double tot5 = calcResult.getTotalForSrc(row, 5L);
			
			//Double check that the column mapping are correct
			assertEquals(calcResult.getTotalColForSrc(1L), calcResult.getFirstTotalColForSrc());
			assertEquals(totTotal, calcResult.getDouble(row, calcResult.getTotalCol()), delta);
			assertEquals(tot1, calcResult.getDouble(row, calcResult.getTotalColForSrc(1L)), delta);
			assertEquals(tot5, calcResult.getDouble(row, calcResult.getTotalColForSrc(5L)), delta);
			
			assertEquals(totTotal, tot1 + tot2 + tot3 + tot4 + tot5, delta);
		}
		
		assertTrue( compareTables(filePredictResult, calcResult, true, .000001d) );
		
		
	}
	
	@Test
	public void testIsEquals() {
		double allow = .1d;
		
		//Strings
		assertTrue(SparrowTestBase.isEqual(null, null, allow));
		assertTrue(SparrowTestBase.isEqual("hi", "hi", allow));
		assertFalse(SparrowTestBase.isEqual("hi", "bye", allow));
		assertFalse(SparrowTestBase.isEqual("hi", null, allow));
		assertFalse(SparrowTestBase.isEqual(null, "hi", allow));
		
		//Doubles
		assertTrue(SparrowTestBase.isEqual(new Double(5), new Double(5), allow));
		assertTrue(SparrowTestBase.isEqual(new Double(5), new Double(5.49d), allow));
		assertFalse(SparrowTestBase.isEqual(new Double(5), new Double(5.5d), allow));
		assertTrue(SparrowTestBase.isEqual(new Double(-5), new Double(-5.49d), allow));
		assertFalse(SparrowTestBase.isEqual(new Double(-5), new Double(-5.5d), allow));
		assertTrue(SparrowTestBase.isEqual(new Double(0), new Double(0), allow));
		
		//Odd value Doubles
		assertTrue(SparrowTestBase.isEqual(Double.NaN, Double.NaN, allow));
		assertFalse(SparrowTestBase.isEqual(Double.NaN, new Double(5), allow));
		assertFalse(SparrowTestBase.isEqual(new Double(5), Double.NaN, allow));
		assertFalse(SparrowTestBase.isEqual(null, Double.NaN, allow));
		assertFalse(SparrowTestBase.isEqual(Double.NaN, null, allow));
		assertTrue(SparrowTestBase.isEqual(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, allow));
		assertTrue(SparrowTestBase.isEqual(Double.NEGATIVE_INFINITY, Double.NEGATIVE_INFINITY, allow));
		assertFalse(SparrowTestBase.isEqual(Double.POSITIVE_INFINITY, new Double(5), allow));
		assertFalse(SparrowTestBase.isEqual(Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, allow));
		assertFalse(SparrowTestBase.isEqual(Double.POSITIVE_INFINITY, null, allow));
	}
}
