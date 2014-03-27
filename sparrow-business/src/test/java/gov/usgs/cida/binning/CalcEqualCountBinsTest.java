package gov.usgs.cida.binning;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import gov.usgs.cida.binning.domain.Bin;
import gov.usgs.cida.binning.domain.BinSet;
import gov.usgs.cida.binning.domain.BinType;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.action.DeliveryReach;
import gov.usgswim.sparrow.domain.*;
import gov.usgswim.sparrow.request.BinningRequest;
import gov.usgs.cida.sparrow.service.util.ServiceResponseOperation;
import gov.usgs.cida.sparrow.service.util.ServiceResponseStatus;
import gov.usgs.cida.sparrow.service.util.ServiceResponseWrapper;
import gov.usgswim.sparrow.service.ServletResponseParser;
import gov.usgswim.sparrow.service.SharedApplication;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Level;
import org.junit.Test;

public class CalcEqualCountBinsTest extends CalcEqualRangeBinsTest {

	@Override
	public void doOneTimeCustomSetup() throws Exception {
		//Uncomment to debug
		//setLogLevel(Level.DEBUG);
	}
	
	@Test
	public void testSmoothEven100Values() throws Exception {
		double[] values = buildValues(0d, 100d, 100, 0, true, 245);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	
	@Test
	public void testDot1aLumpyUneven100Values() throws Exception {
		double[] values = buildValues(0d, 100d, 100, .1, true, 1245);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	
	@Test
	public void testDot1bLumpyUneven100Values() throws Exception {
		double[] values = buildValues(0d, 100d, 100, .1, true, 91245);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	
	@Test
	public void testDot1LumpyUneven1000ValuesA() throws Exception {
		double[] values = buildValues(0d, 100d, 1000, .1, true, 8942784);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(10);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	
	@Test
	public void testDot1LumpyUneven1000ValuesB() throws Exception {
		double[] values = buildValues(0d, 100d, 1000, .1, true, 8942784);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(3);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	
	@Test
	public void testDot1LumpyUneven1000ValuesC() throws Exception {
		//setLogLevel(Level.ERROR);
		
		double[] values = buildValues(-100d, 100d, 150, .1, true, 898372029723L);
		Arrays.sort(values);
		
		System.out.println("[ Max iterations allowed / Actual iterations ][ restartMultiplier  ] score");
		double outerBestScore = Double.MAX_VALUE;
		
		for (int maxIterationCount = 100; maxIterationCount < 201; maxIterationCount+= 100) {
			
			double innerBestScore = Double.MAX_VALUE;
			
			for (int restartMultiplier = 7; restartMultiplier < 8; restartMultiplier+= 2) {
				CalcEqualCountBins action = new CalcEqualCountBins();
				action.setBinCount(5);
				action.setSortedAndFilteredValues(values);
				action.setRestartMultiplier(restartMultiplier);
				action.setMaxAllowedIterations(maxIterationCount);
				action.setUseEqualCountStartPosts(false);
				BinSet result = action.run();
				
				if (action.getBestScore() < innerBestScore) innerBestScore = action.getBestScore();
				System.out.println("[no eq post starts][ " + maxIterationCount + " / " + action.getTotalIterations() + " ][ " + restartMultiplier + " ] score: " + action.getBestScore());
			
				action = new CalcEqualCountBins();
				action.setBinCount(5);
				action.setSortedAndFilteredValues(values);
				action.setRestartMultiplier(restartMultiplier);
				action.setMaxAllowedIterations(maxIterationCount);
				action.setUseEqualCountStartPosts(true);
				result = action.run();
				
				if (action.getBestScore() < innerBestScore) innerBestScore = action.getBestScore();
				System.out.println("[EQ POST STARTS][ " + maxIterationCount + " / " + action.getTotalIterations() + " ][ " + restartMultiplier + " ] score: " + action.getBestScore());
			
			
			}
			
//			for (int restartMultiplier = 5; restartMultiplier < 10; restartMultiplier+= 2) {
//				CalcEqualCountBins action = new CalcEqualCountBins();
//				action.setBinCount(5);
//				action.setSortedAndFilteredValues(values);
//				action.setRestartMultiplier(restartMultiplier);
//				action.setMaxAllowedIterations(maxIterationCount);
//				BinSet result = action.run();
//				
//				if (action.getBestScore() < innerBestScore) innerBestScore = action.getBestScore();
//				System.out.println("[no eq post starts][ " + maxIterationCount + " / " + action.getTotalIterations() + " ][ " + restartMultiplier + " ] score: " + action.getBestScore());
//			
//				action = new CalcEqualCountBins();
//				action.setBinCount(5);
//				action.setSortedAndFilteredValues(values);
//				action.setRestartMultiplier(restartMultiplier);
//				action.setMaxAllowedIterations(maxIterationCount);
//				result = action.run();
//				
//				if (action.getBestScore() < innerBestScore) innerBestScore = action.getBestScore();
//				System.out.println("[eq post starts][ " + maxIterationCount + " / " + action.getTotalIterations() + " ][ " + restartMultiplier + " ] score: " + action.getBestScore());
//			
//			
//			}
			System.out.println("^^ Best Score: " + innerBestScore);
			
			if (innerBestScore < outerBestScore) outerBestScore = innerBestScore;
		}
		
		System.out.println("^^ Overall Best Score: " + outerBestScore);

		
	
	}
	
	@Test
	public void testReallyLumpyUneven150ValuesA() throws Exception {
		double[] values = buildLumpyValues(0d, 100d, 150, .7, true, 8942784);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	
	@Test
	public void testReallyLumpyUneven150ValuesB() throws Exception {
		double[] values = buildLumpyValues(0d, 100d, 150, .7, true, 2784124);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	
	@Test
	public void testReallyLumpyUneven150ValuesC() throws Exception {
		double[] values = buildLumpyValues(0d, 100d, 150, .7, true, 4124);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		debug(result.getActualPostValues());
	}
	

	@Test
	public void testwithDetectionLimit() throws Exception {
		double[] values = buildLumpyValues(0d, .10d, 150, .1, true, 4124);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		action.setDetectionLimit(new BigDecimal(".05"));
		
		BinSet result = action.run();
		
		debug(result.getActualPostValues());
		
		//Bottom Bin (the non-detect bin)
		assertTrue(result.getBins()[0].isNonDetect());
		assertTrue(result.getBins()[0].getBottom().isUnbounded());
		assertTrue(result.getBins()[0].getBottom().getFunctional().compareTo(new BigDecimal("-0.01")) == 0);
		assertTrue(result.getBins()[0].getTop().getFunctional().compareTo(new BigDecimal("0.05")) == 0);
		assertEquals("<", result.getBins()[0].getBottom().getFormatted());
		assertEquals("0.05", result.getBins()[0].getTop().getFormatted());
		
		//Bin 1
		assertFalse(result.getBins()[1].isNonDetect());
		assertFalse(result.getBins()[1].getBottom().isUnbounded());
		assertTrue(result.getBins()[1].getBottom().getFunctional().compareTo(new BigDecimal("0.05")) == 0);
		assertTrue(result.getBins()[1].getTop().getFunctional().compareTo(new BigDecimal("0.06")) == 0);
		assertEquals("0.05", result.getBins()[1].getBottom().getFormatted());
		assertEquals("0.06", result.getBins()[1].getTop().getFormatted());
		
		//Bin 2
		assertFalse(result.getBins()[2].isNonDetect());
		assertFalse(result.getBins()[2].getBottom().isUnbounded());
		assertTrue(result.getBins()[2].getBottom().getFunctional().compareTo(new BigDecimal("0.06")) == 0);
		assertTrue(result.getBins()[2].getTop().getFunctional().compareTo(new BigDecimal("0.07")) == 0);
		assertEquals("0.06", result.getBins()[2].getBottom().getFormatted());
		assertEquals("0.07", result.getBins()[2].getTop().getFormatted());
		
		//Bin 3
		assertFalse(result.getBins()[3].isNonDetect());
		assertFalse(result.getBins()[3].getBottom().isUnbounded());
		assertTrue(result.getBins()[3].getBottom().getFunctional().compareTo(new BigDecimal("0.07")) == 0);
		assertTrue(result.getBins()[3].getTop().getFunctional().compareTo(new BigDecimal("0.08")) == 0);
		assertEquals("0.07", result.getBins()[3].getBottom().getFormatted());
		assertEquals("0.08", result.getBins()[3].getTop().getFormatted());
		
		//Bin 4
		assertFalse(result.getBins()[4].isNonDetect());
		assertFalse(result.getBins()[4].getBottom().isUnbounded());
		assertTrue(result.getBins()[4].getBottom().getFunctional().compareTo(new BigDecimal("0.08")) == 0);
		assertTrue(result.getBins()[4].getTop().getFunctional().compareTo(new BigDecimal("0.09")) == 0);
		assertEquals("0.08", result.getBins()[4].getBottom().getFormatted());
		assertEquals("0.09", result.getBins()[4].getTop().getFormatted());
		
		//Bin 5
		assertFalse(result.getBins()[5].isNonDetect());
		assertFalse(result.getBins()[5].getBottom().isUnbounded());
		assertTrue(result.getBins()[5].getBottom().getFunctional().compareTo(new BigDecimal("0.09")) == 0);
		assertTrue(result.getBins()[5].getTop().getFunctional().compareTo(new BigDecimal("0.10")) == 0);
		assertEquals("0.09", result.getBins()[5].getBottom().getFormatted());
		assertEquals("0.10", result.getBins()[5].getTop().getFormatted());
	}
	

	/**
	 * Large values lead to large large values to round to, which can cause
	 * the detection limit to be rounded to zero.  Here we just check that this
	 * doesn't happen.
	 * @throws Exception
	 */
	@Test
	public void testVeryLargeValuesWithDetectionLimit() throws Exception {
		double[] values = buildLumpyValues(-1000000000000d, 1000000000000d, 150, .1, true, 4124);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		action.setDetectionLimit(new BigDecimal(".05"));
		
		BinSet result = action.run();
		
		debug(result.getActualPostValues());
		
		//Bottom Bin (the non-detect bin)
		assertTrue(result.getBins()[0].isNonDetect());
		assertTrue(result.getBins()[0].getBottom().isUnbounded());
		//assertTrue(result.getBins()[0].getBottom().getFunctional().compareTo(new BigDecimal("-??")) == 0);
		assertTrue(result.getBins()[0].getTop().getFunctional().compareTo(new BigDecimal("0.05")) == 0);
		assertEquals("<", result.getBins()[0].getBottom().getFormatted());
		assertEquals("0.05", result.getBins()[0].getTop().getFormatted());
		
		//Bin 1
		assertFalse(result.getBins()[1].isNonDetect());
		assertFalse(result.getBins()[1].getBottom().isUnbounded());
		assertTrue(result.getBins()[1].getBottom().getFunctional().compareTo(new BigDecimal("0.05")) == 0);
		//assertTrue(result.getBins()[1].getTop().getFunctional().compareTo(new BigDecimal("some large value")) == 0);
		assertEquals("0.05", result.getBins()[1].getBottom().getFormatted());
		//assertEquals("some huge number", result.getBins()[1].getTop().getFormatted());
		
		//Don't care about the rest of the bins for this test

	}


	
	@Test
	public void testAllValuesBelowDetectionLimit() throws Exception {
		double[] values = buildLumpyValues(0d, .03d, 150, .7, true, 4124);
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		action.setDetectionLimit(new BigDecimal(".05"));
		
		BinSet result = action.run();
		
		debug(result.getActualPostValues());
		
		//Bottom Bin (the non-detect bin)
		assertTrue(result.getBins()[0].isNonDetect());
		assertTrue(result.getBins()[0].getBottom().isUnbounded());
		assertTrue(result.getBins()[0].getBottom().getFunctional().compareTo(new BigDecimal("-0.01")) == 0);
		assertTrue(result.getBins()[0].getTop().getFunctional().compareTo(new BigDecimal("0.05")) == 0);
		assertEquals("<", result.getBins()[0].getBottom().getFormatted());
		assertEquals("0.05", result.getBins()[0].getTop().getFormatted());
		
		//Bin 1
		assertFalse(result.getBins()[1].isNonDetect());
		assertFalse(result.getBins()[1].getBottom().isUnbounded());
		assertTrue(result.getBins()[1].getBottom().getFunctional().compareTo(new BigDecimal("0.05")) == 0);
		assertTrue(result.getBins()[1].getTop().getFunctional().compareTo(new BigDecimal("0.06")) == 0);
		assertEquals("0.05", result.getBins()[1].getBottom().getFormatted());
		assertEquals("0.06", result.getBins()[1].getTop().getFormatted());
		
		//Bin 2
		assertFalse(result.getBins()[2].isNonDetect());
		assertFalse(result.getBins()[2].getBottom().isUnbounded());
		assertTrue(result.getBins()[2].getBottom().getFunctional().compareTo(new BigDecimal("0.06")) == 0);
		assertTrue(result.getBins()[2].getTop().getFunctional().compareTo(new BigDecimal("0.07")) == 0);
		assertEquals("0.06", result.getBins()[2].getBottom().getFormatted());
		assertEquals("0.07", result.getBins()[2].getTop().getFormatted());
		
		//Bin 3
		assertFalse(result.getBins()[3].isNonDetect());
		assertFalse(result.getBins()[3].getBottom().isUnbounded());
		assertTrue(result.getBins()[3].getBottom().getFunctional().compareTo(new BigDecimal("0.07")) == 0);
		assertTrue(result.getBins()[3].getTop().getFunctional().compareTo(new BigDecimal("0.08")) == 0);
		assertEquals("0.07", result.getBins()[3].getBottom().getFormatted());
		assertEquals("0.08", result.getBins()[3].getTop().getFormatted());
		
		//Bin 4
		assertFalse(result.getBins()[4].isNonDetect());
		assertFalse(result.getBins()[4].getBottom().isUnbounded());
		assertTrue(result.getBins()[4].getBottom().getFunctional().compareTo(new BigDecimal("0.08")) == 0);
		assertTrue(result.getBins()[4].getTop().getFunctional().compareTo(new BigDecimal("0.09")) == 0);
		assertEquals("0.08", result.getBins()[4].getBottom().getFormatted());
		assertEquals("0.09", result.getBins()[4].getTop().getFormatted());
		
		//Bin 5
		assertFalse(result.getBins()[5].isNonDetect());
		assertFalse(result.getBins()[5].getBottom().isUnbounded());
		assertTrue(result.getBins()[5].getBottom().getFunctional().compareTo(new BigDecimal("0.09")) == 0);
		assertTrue(result.getBins()[5].getTop().getFunctional().compareTo(new BigDecimal("0.10")) == 0);
		assertEquals("0.09", result.getBins()[5].getBottom().getFormatted());
		assertEquals("0.10", result.getBins()[5].getTop().getFormatted());
	}
	
	/**
	 * This test is to address an issue with values that cover a wide range
	 * and have the values clustered at the bottom.  The result a very large
	 * CUV, resulting in the minimum possible bin containing all the values.
	 * 
	 * In these cases we need to check the quality of the EQ bins and allow the
	 * CUV to be smaller (if allowed by the max decimal places).
	 * 
	 * As part of this test, we are defining a standard of 10% variation b/t the bins.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWideRangeOfValuesWhereMostValuesAreClusteredAtBottom() throws Exception {
		//Build values w/ a wide range, but almost all vals in bottom bin
		double[] values = buildValues(0d, 100d, 150, 0, true, 4124);
		values[149] = 100000d;
		
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		
		BinSet result = action.run();
		
		debug(result.getActualPostValues());
		log.debug("Variance: " + result.getBinCountMaxVariancePercentage());
		log.debug("CUV: " + result.getCharacteristicUnitValue());
		
		assertTrue(result.getBinCountMaxVariancePercentage() < 18);
		assertTrue(result.getCharacteristicUnitValue().compareTo(new BigDecimal("1")) == 0);
	}
	
	/**
	 * This test is to address an issue with values that cover a wide range
	 * and have the values clustered at the bottom.  The result a very large
	 * CUV, resulting in the minimum possible bin containing all the values.
	 * 
	 * In these cases we need to check the quality of the EQ bins and allow the
	 * CUV to be smaller (if allowed by the max decimal places).
	 * 
	 * As part of this test, we are defining a standard of 10% variation b/t the bins.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testWideRangeOfValuesWhereMostValuesAreClusteredAtBottomWithMaxDecimalPlaces() throws Exception {
		//Build values w/ a wide range, but almost all vals in bottom bin
		double[] values = buildValues(0d, 100d, 150, 0, true, 4124);
		values[149] = 100000d;
		
		CalcEqualCountBins action = new CalcEqualCountBins();
		action.setBinCount(5);
		action.setUnsortedValues(values);
		action.setMaxDecimalPlaces(-2);
		
		BinSet result = action.run();
		
		debug(result.getActualPostValues());
		log.debug("Variance: " + result.getBinCountMaxVariancePercentage());
		log.debug("CUV: " + result.getCharacteristicUnitValue());
		
		assertTrue(result.getBinCountMaxVariancePercentage() == 100);	//Can't get to a good value here
		assertTrue(result.getCharacteristicUnitValue().compareTo(new BigDecimal("100")) == 0); //restriction from max dec places
	}
	
	@Test
	public void testPositiveBinsWithInfiniteTop() throws Exception {

		BinningRequest req = new BinningRequest(99999, 2, BinType.EQUAL_COUNT,
				DataSeriesType.total, ComparisonType.none,
				SparrowModel.TN_CONSTITUENT_NAME, null, null);
		
		CalcEqualCountBins action = new CalcEqualCountBins();
		
		
		action.setDataColumn(zeroTo50in50ValuesWithInfiniteTop);
		action.setBinCount(req.getBinCount());
		
		
		BinSet bs = action.run();
		
		assertEquals("0.0", bs.getBins()[0].getBottom().getFormatted());
		assertEquals("25.0", bs.getBins()[0].getTop().getFormatted());
		assertEquals("25.0", bs.getBins()[1].getBottom().getFormatted());
		assertEquals("50.0", bs.getBins()[1].getTop().getFormatted());
		
		assertEquals("-0.1", bs.getBins()[0].getBottom().getFormattedFunctional());
		assertEquals("25.0", bs.getBins()[0].getTop().getFormattedFunctional());
		assertEquals("25.0", bs.getBins()[1].getBottom().getFormattedFunctional());
		assertEquals("50.0", bs.getBins()[1].getTop().getFormattedFunctional());
		
		
		for (int i = 0; i < bs.getBins().length; i++) {
			Bin b = bs.getBins()[i];
			log.debug(b.getBottom().getFormatted() + " - " + b.getTop().getFormatted());
		}
		
		ServiceResponseWrapper wrap = new ServiceResponseWrapper(bs, 99999L, ServiceResponseStatus.OK,
				ServiceResponseOperation.CALCULATE);
		
		String xml = SharedApplication.getInstance().getXmlXStream().toXML(wrap);
		
		log.debug(xml);
		
	}
	
	@Test
	public void testInclusionFilterForDeliverySeries() throws Exception {
		
		//Create a delivery fraction map that indicates that all rows except
		//row 9 (which contains the value 10) are included.
		HashMap<Integer, DeliveryReach> map = new HashMap<Integer, DeliveryReach>();
		for (int i=0; i<10; i++) {
			map.put(i, new DeliveryReach(i, .5d, i));
		}
		ReachRowValueMap dfm = ReachRowValueMapImm.buildFromReachValues(map);
		
		CalcEqualCountBins action = new CalcEqualCountBins();

		
		action.setDataColumn(zeroTo10In10Values);
		action.setInclusionMap(dfm);
		action.setBinCount(5);
		
		
		BinSet bs = action.run();
		
		for (int i = 0; i < bs.getBins().length; i++) {
			Bin b = bs.getBins()[i];
			log.debug(b.getBottom().getFormatted() + " - " + b.getTop().getFormatted());
		}
		
		assertTrue(bs.getActualPostValues()[0].compareTo(BigDecimal.ZERO) == 0);
		assertTrue(bs.getActualPostValues()[5].compareTo(new BigDecimal("5")) == 0);

		
	}

	//
	//Util method tests
	//
//	@Test
//	public void testGetBinStats() {
//		CalcEqualCountBins action = new CalcEqualCountBins();
//		
//		double[] values = new double[] {0, 2, 4, 6, 8, 10};
//		BigDecimal[] posts = new BigDecimal[3];
//		posts[0] = BigDecimal.ZERO;
//		posts[1] = new BigDecimal(3);
//		posts[2] = new BigDecimal(10);
//		
//		int[][] stats = action.getBinStats(values, posts);
//		int[] binCounts = stats[0];
//		int[] binSplits = stats[1];
//		
//		//Counts
//		assertEquals(2, binCounts[0]);
//		assertEquals(4, binCounts[1]);
//		assertEquals(2, binCounts.length);
//		
//		//splits
//		assertEquals(2, binSplits[0]);
//		assertEquals(6, binSplits[1]);
//		assertEquals(2, binSplits.length);
//	}
//	
//	@Test
//	public void testGetBinsOrderedByCounts() {
//		CalcEqualCountBins action = new CalcEqualCountBins();
//		
//		int[] binCounts = new int[] {20, 12, 25, 35, 7};
//		//Rearranged by size:		  7, 12, 20, 25, 35
//		//Index of the rearranged:	  4,  1,  0,  2,  3
//		int[] binIndexes = action.getBinsOrderedByCounts(binCounts);
//		
//		assertEquals(4, binIndexes[0]);
//		assertEquals(1, binIndexes[1]);
//		assertEquals(0, binIndexes[2]);
//		assertEquals(2, binIndexes[3]);
//		assertEquals(3, binIndexes[4]);
//	}
//	
//	//Tests from the old EQ count implementation.  May be worth porting to the new
//    @Test public void testBuildEqualCountBinsSmallSet_HighestValueRoundDownPrevention() {
//
//        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1};
//        int binCount = 3;
//        BigDecimal[] result = OldCalcBinning.buildEqualCountBins(sortedData, binCount, true);
////        for (BigDecimal bin: result) {
////            System.out.println(bin);
////        }
//        BigDecimal largestBin = result[result.length-1];
//        assertTrue(sortedData[sortedData.length - 1] <= largestBin.doubleValue());
//    }
//    
//    @Test public void testBuildEqualCountBinsSmallSet_BinRounding() {
//
//        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1};
//        int binCount = 3;
//        BigDecimal[] result = OldCalcBinning.buildEqualCountBins(sortedData, binCount, true);
//        for (BigDecimal bin: result) {
//            String stringResult = bin.toPlainString();
//            assertTrue(stringResult.length() <=6);
//        }
//    }
//
//    @Test public void testBuildEqualCountBins_HighestValueRoundDownPrevention() {
//
//        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1, 6000.1, 6000.1, 6000.1};
//        int binCount = 5;
//        BigDecimal[] result = OldCalcBinning.buildEqualCountBins(sortedData, binCount, true);
//        BigDecimal largestBin = result[result.length-1];
//        assertTrue(sortedData[sortedData.length - 1] <= largestBin.doubleValue());
//        assertEquals("6000.1", largestBin.toPlainString());
//    }
//    
//    @Test public void testBuildEqualCountBins_BinRounding() {
//
//        Double[] sortedData = {0.1, 0.11, 0.13, 0.15, 0.151, 0.1522, 0.15223, 0.1523, 0.154, 0.1542, 0.16, 6000.1, 6000.1, 6000.1, 6000.1};
//        int binCount = 5;
//        BigDecimal[] result = OldCalcBinning.buildEqualCountBins(sortedData, binCount, true);
//        for (BigDecimal bin: result) {
//            String stringResult = bin.toPlainString();
//            assertTrue(stringResult.length() <=7);
//        }
//    }

	
	/**
	 * 
	 * @param bottom
	 * @param top
	 * @param valueCount
	 * @param likelyhoodOfDuplicateBlocks 0 to 1 where 1 would generate all duplicate values.
	 * @param forceNonUniformDistribution
	 * @param rndSeed
	 * @return
	 */
	private double[] buildValues(double bottom, double top, int valueCount,
			double likelyhoodOfDuplicateBlocks, 
			boolean forceNonUniformDistribution, long rndSeed) {
		
		double[] values = new double[valueCount];
		Random rnd = new Random(rndSeed);
		double range = top - bottom;
		int currentDupCountDown = 0;	//if greater than 0, we are duplicating
		double newVal = 0;	//not yet assigned
		
		for (int i = 0; i < valueCount; i++) {
			
			if (currentDupCountDown > 0) {
				values[i] = newVal;
				currentDupCountDown--;
			} else {
				double rndNumber = rnd.nextDouble();
				
				if (rndNumber < likelyhoodOfDuplicateBlocks) {
					double repeatFactor1 = rnd.nextDouble();
					double repeatFactor2 = rnd.nextDouble();
					double repeatFactor3 = rnd.nextDouble();
					double repeatFactor = repeatFactor1 * repeatFactor2 * repeatFactor3;
					currentDupCountDown = (int)(repeatFactor * (double)valueCount);
					//currentDupCountDown = (int)(Math.pow(repeatFactor, 5) * (double)valueCount);
					rndNumber = rnd.nextDouble();	//new number b/c the previous is biased.
				}
				if (forceNonUniformDistribution) rndNumber = rndNumber * rndNumber;
				
				newVal = (range * rndNumber) + bottom;
				values[i] = newVal;
			}
		}
		
		return values;
		
	}
	
	/**
	 * 
	 * @param bottom
	 * @param top
	 * @param valueCount
	 * @param repeatFraction For a value, the number of repeats can be expected be (repeatFraction * valueCount)
	 * @param useIntegers
	 * @param rndSeed
	 * @return
	 */
	private double[] buildLumpyValues(double bottom, double top, int valueCount,
			double repeatFraction, 
			boolean useIntegers, long rndSeed) {
		
		double[] values = new double[valueCount];
		Random rnd = new Random(rndSeed);
		double range = top - bottom;
		int currentDupCountDown = 0;	//if greater than 0, we are duplicating
		double newVal = 0;	//not yet assigned
		
		for (int i = 0; i < valueCount; i++) {
			
			if (currentDupCountDown > 0) {
				values[i] = newVal;
				currentDupCountDown--;
			} else {
				double rndNumber = rnd.nextDouble();
				
				double repeatFactor = rnd.nextDouble() * repeatFraction;
				currentDupCountDown = (int)(repeatFactor * (double)valueCount);
				//currentDupCountDown = (int)(Math.pow(repeatFactor, 5) * (double)valueCount);

				newVal = (range * rndNumber) + bottom;
				if (useIntegers) {
					newVal = (int)newVal;
				}
				values[i] = newVal;
			}
		}
		
		return values;
		
	}
	
	void debug(BigDecimal[] bins) {
		if (log.isDebugEnabled()) {
			for (int i=0; i< bins.length; i++) {
				log.debug("Bin " + i + ": " + bins[i].toPlainString());
			}
			
		}
	}

}
