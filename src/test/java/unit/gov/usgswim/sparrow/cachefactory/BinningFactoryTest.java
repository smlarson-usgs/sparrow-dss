package gov.usgswim.sparrow.cachefactory;

import java.math.BigDecimal;

import org.apache.commons.lang.ArrayUtils;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.round;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.getEqualCountBins;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.digitAccuracyMultipliers;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.makeBigDecimal;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.getUniqueValueCount;

public class BinningFactoryTest extends TestCase {

	public final static boolean ENABLE_ALL_LOG_OUTPUT = false;
	public final static boolean ENABLE_FAILURE_LOG_OUTPUT = true;
	
	public void testRoundToZero() {
		assertEquals("0", round(.011, -.1, .44).toString());
	}

	public void testRoundToSingleDigit() {
		//rounding up

		BigDecimal bd = round(8.77, 5.2, 10.1);
		bd = bd.setScale(0);
		assertEquals("10", bd.toString());
		assertEquals("1", round(.877, .52, 1.01).toString());
		assertEquals("0.1", round(.0877, .052, 1.01).toString());
		assertEquals("1000", round(870.7, 520, 1010).toString());

		// rounding down
		assertEquals("10", round(9.47, 5.2, 10.1).toString());
		assertEquals("1", round(.947, .52, 1.01).toString());
		assertEquals("0.1", round(.0947, .052, 1.01).toString());
		assertEquals("1000", round(940.7, 520, 1010).toString());
	}

	public void testRoundTo1_5Digits() {
		// rounding up
		assertEquals("8.5", round(8.67, 8.2, 8.91).toString());
		assertEquals("0.85", round(.867, .82, .891).toString());
		assertEquals("0.085", round(.0867, .082, .0891).toString());
		assertEquals("850", round(860.7, 820, 891).toString());
	}

	public void testRoundTo2Digits() {
		//rounding up
		assertEquals("8.8", round(8.77, 8.52, 8.91).toString());
		assertEquals("0.88", round(.877, .852, .891).toString());
		assertEquals("0.088", round(.0877, .0852, .0891).toString());
		assertEquals("0.000088", round(.0000877, .0000852, .0000891).toString());
		assertEquals("880", round(877.77, 852, 891).toString());

		// rounding down
		assertEquals("9.4", round(9.44, 9.25, 9.49).toString());
		assertEquals("0.94", round(.944, .925, .949).toString());
		assertEquals("0.094", round(.0944, .0925, .0949).toString());
		assertEquals("0.000094", round(.0000944, .0000925, .0000949).toString());
		assertEquals("940", round(940.7, 925, 949).toString());
	}

	public void testTooNarrowRoundingRange() {
		// String.startsWith() used since representation of value is not truncated,
		// (i.e., 8.770009000001 is returned as 8.770009000001[2342393758713480])
		assertTrue(round(8.770009000001, 8.770005000001, 8.770091).toString().startsWith("8.770009000001"));
	}


	public static Float[] sortedData100Normal = {-99.73f, -97.544f, -93.8991f, -86.773f, -85.1101f,
		-84.897f, -83.511f, -83.509f, -83.507f, -83.505f,
		-83.404f, -83.403f, -78.011f, -72.555f, -71.77f,
		-22.378f, -18.22f, -15.099f, -14.932f, -8.753f,
		-8.7519f, -8.7498f, -7.412f, -6.071f, -5.873f,
		-4.332f, -3.256f, -3.225f, -3.0012f, -2.983f,
		-1.455f, -.8896f, -.7337f, -.6332f, -.2219f,
		-.2189f, -.20166f, -.20133f, -.200988f, -.20977f,
		.004334f, .005112f, .005339f, .005455f, .0056711f,
		.0093443f, .0094775f, .0094993f, .009554f, .0096798f,
		.01003f, .01025f, .010378f, .010408f, .01153f,
		.02334f, .06778f, .07112f, .08951f, .09234f,
		.09443f, .09493f, .09512f, .09593f, .09607f,
		.09889f, .09912f, 1.003f, 1.074f, 1.082f,
		1.134f, 1.188f, 1.191f, 1.234f, 1.265f,
		1.277f, 1.341f, 1.456f, 1.567f, 1.612f,
		1.789f, 1.834f, 1.8402f, 1.862f, 1.871f,
		1.888f, 1.891f, 1.899f, 1.902f, 1.905f,
		1.908f, 1.911f, 1.912f, 1.913f, 1.914f,
		1.915f, 1.977f, 2.111f, 3.445f, 5.121f
	}; // 100 elements
	
	public static Float[] sortedData2 = { 2f, 4f };
	public static Float[] sortedData2a = { 0f, 0f };
	public static Float[] sortedData2b = { 4f, 4f };
	public static Float[] sortedData2c = { 0f, 4f };
	
	public static Float[] sortedData2d = { -4f, -2f };
	public static Float[] sortedData2e = { -4f, -4f };
	public static Float[] sortedData2f = { -4f, 0f };
	
	public static Float[] sortedData2g = { -4f, 4f };
	
	//Two values w/ decimals
	public static Float[] sortedData2_ = { 2.1f, 4.3f };
	public static Float[] sortedData2a_ = { 0.1f, 0.1f };
	public static Float[] sortedData2b_ = { 4.1f, 4.1f };
	public static Float[] sortedData2c_ = { 0f, 4.1f };
	
	public static Float[] sortedData2d_ = { -4.1f, -2.1f };
	public static Float[] sortedData2e_ = { -4.1f, -4.1f };
	public static Float[] sortedData2f_ = { -4.1f, 0f };
	
	public static Float[] sortedData2g_ = { -4.1f, 4.1f };

	public void testGetEqualCountBinsOfOne() {
		BigDecimal[] result = getEqualCountBins(sortedData100Normal, 1, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfTwo() {
		BigDecimal[] result = getEqualCountBins(sortedData100Normal, 2, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("0.01", result[1].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfThree() {
		BigDecimal[] result = getEqualCountBins(sortedData100Normal, 3, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-0.5", result[1].toString());
		assertEquals("1", result[2].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfFour() {
		BigDecimal[] result = getEqualCountBins(sortedData100Normal, 4, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-5", result[1].toString());
		assertEquals("0.01", result[2].toString());
		assertEquals("1.3", result[3].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfFive() {
		BigDecimal[] result = getEqualCountBins(sortedData100Normal, 5, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-8.75", result[1].toString());
		assertEquals("0", result[2].toString());
		assertEquals("0.094", result[3].toString());
		assertEquals("1.8", result[4].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfSix() {
		BigDecimal[] result = getEqualCountBins(sortedData100Normal, 6, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-17", result[1].toString());
		assertEquals("-0.5", result[2].toString());
		assertEquals("0.01", result[3].toString());
		assertEquals("1", result[4].toString());
		assertEquals("1.87", result[5].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfSeven() {

		BigDecimal[] result = getEqualCountBins(sortedData100Normal, 7, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-50", result[1].toString());
		assertEquals("-3", result[2].toString());
		assertEquals("0.0054", result[3].toString());
		assertEquals("0.08", result[4].toString());
		assertEquals("1.19", result[5].toString());
		assertEquals("1.89", result[6].toString());
		assertEquals("6", result[lastIndex].toString());

	}
	
	
	////////////////////////////////////
	// Try small datasets
	////////////////////////////////////
	public void testEqualCount_SmallDataset_OneBin() {
		
		final int bins = 1;
		final boolean round = true;
		
		//Comments following the asserts indicate a preferred binning result,
		//though the tested values a acceptable.  Who's to say??
		doEqualCountAssert(sortedData2, bins, round, 0, 5);	//2 to 4
		doEqualCountAssert(sortedData2_, bins, round, 0, 5); //2 to 5
		doEqualCountAssert(sortedData2a, bins, round, 0, 1);
		doEqualCountAssert(sortedData2a_, bins, round, 0, .11);	//0 to .1
		doEqualCountAssert(sortedData2b, bins, round, 0, 4);
		doEqualCountAssert(sortedData2b_, bins, round, 0, 4.5);	//0 to 5
		doEqualCountAssert(sortedData2c, bins, round, 0, 5);	//0 to 4
		doEqualCountAssert(sortedData2c_, bins, round, 0, 5);
		doEqualCountAssert(sortedData2d, bins, round, -5, -0);
		doEqualCountAssert(sortedData2d_, bins, round, -5, -2);
		doEqualCountAssert(sortedData2e, bins, round, -4, 0);
		doEqualCountAssert(sortedData2e_, bins, round, -4.5, 0);
		doEqualCountAssert(sortedData2f, bins, round, -5, 0);	//-4 to 0
		doEqualCountAssert(sortedData2f_, bins, round, -5, 0);
		doEqualCountAssert(sortedData2g, bins, round, -10, 10);	//-4 to 4
		doEqualCountAssert(sortedData2g_, bins, round, -10, 10);	//-5 to 5
	}

	public void testEqualCount_SmallDataset_TwoBins() {
		final int bins = 2;
		final boolean round = true;
		
		//Comments following the asserts indicate a preferred binning result,
		//though the tested values a acceptable.  Who's to say??
		doEqualCountAssert(sortedData2, bins, round, 2, 3, 4);
		doEqualCountAssert(sortedData2_, bins, round, 2, 3.2, 4.4);
		doEqualCountAssert(sortedData2a, bins, round, 0, 1, 2);
		doEqualCountAssert(sortedData2a_, bins, round, 0, .055, .11);
		doEqualCountAssert(sortedData2b, bins, round, 0, 2, 4);
		doEqualCountAssert(sortedData2b_, bins, round, 0, 2.1, 4.2);
		doEqualCountAssert(sortedData2c, bins, round, 0, 2, 4);
		doEqualCountAssert(sortedData2c_, bins, round, 0, 2.1, 4.2);
		doEqualCountAssert(sortedData2d, bins, round, -4, -3, -2);
		
		//Ugly
		//Values like -4.0999999904 should be rounded to -4.1, but how???
		doEqualCountAssert(sortedData2d_, bins, round, -4.1, -3.1, 2.1);
		
		
		doEqualCountAssert(sortedData2e, bins, round, -4, -2, 0);
		doEqualCountAssert(sortedData2e_, bins, round, -4.1, -2, 0.1);
		doEqualCountAssert(sortedData2f, bins, round, -4, -2, 0);
		doEqualCountAssert(sortedData2f_, bins, round, -4.1, -2, .1);
		doEqualCountAssert(sortedData2g, bins, round, -4, 0, 4);
		doEqualCountAssert(sortedData2g_, bins, round, -4.5, 0, 4.5);
	}

	public void testGetEqualCountBinsOfThreeSmall() {
		BigDecimal[] result = getEqualCountBins(sortedData2, 3, Boolean.TRUE);

//		int lastIndex = result.length - 1;
//		assertEquals("2", result[0].toString());
//		assertEquals("-0.5", result[1].toString());
//		assertEquals("1", result[2].toString());
//		assertEquals("6", result[lastIndex].toString());
		printBinningResult("2 values --> 3 EQ Bins:", result);
	}

	public void testGetEqualCountBinsOfFourSmall() {
		BigDecimal[] result = getEqualCountBins(sortedData2, 4, Boolean.TRUE);

//		int lastIndex = result.length - 1;
//		assertEquals("-100", result[0].toString());
//		assertEquals("-5", result[1].toString());
//		assertEquals("0.01", result[2].toString());
//		assertEquals("1.3", result[3].toString());
//		assertEquals("6", result[lastIndex].toString());
		
		printBinningResult("2 values --> 4 EQ Bins:", result);
	}

	public void testGetEqualCountBinsOfFiveSmall() {
		BigDecimal[] result = getEqualCountBins(sortedData2, 5, Boolean.TRUE);

//		int lastIndex = result.length - 1;
//		assertEquals("-100", result[0].toString());
//		assertEquals("-8.75", result[1].toString());
//		assertEquals("0", result[2].toString());
//		assertEquals("0.094", result[3].toString());
//		assertEquals("1.8", result[4].toString());
//		assertEquals("6", result[lastIndex].toString());
		
		printBinningResult("2 values --> 5 EQ Bins:", result);
	}



	public void testMakeBigDecimalWith0Digit() {

		BigDecimal bd = makeBigDecimal(1, digitAccuracyMultipliers[0], 0);
		assertEquals("1 scale: 0", bd2String(bd));

		bd = makeBigDecimal(0, digitAccuracyMultipliers[0], 0);
		assertEquals("0 scale: 0", bd2String(bd));
		bd = makeBigDecimal(-1, digitAccuracyMultipliers[0], 0);
		assertEquals("-1 scale: 0", bd2String(bd));

		bd = makeBigDecimal(1, digitAccuracyMultipliers[0], 1);
		assertEquals("1E+1 scale: -1", bd2String(bd));
		bd = makeBigDecimal(0, digitAccuracyMultipliers[0], 1);
		assertEquals("0E+1 scale: -1", bd2String(bd));
		bd = makeBigDecimal(-1, digitAccuracyMultipliers[0], 1);
		assertEquals("-1E+1 scale: -1", bd2String(bd));

		bd = makeBigDecimal(1, digitAccuracyMultipliers[0], -1);
		assertEquals("0.1 scale: 1", bd2String(bd));
		bd = makeBigDecimal(0, digitAccuracyMultipliers[0], -1);
		assertEquals("0.0 scale: 1", bd2String(bd));
		bd = makeBigDecimal(-1, digitAccuracyMultipliers[0], -1);
		assertEquals("-0.1 scale: 1", bd2String(bd));
	}


	public void testMakeBigDecimalWithHalfDigit() {

		BigDecimal bd = makeBigDecimal(2, digitAccuracyMultipliers[1], 0);
		assertEquals("1 scale: 0", bd2String(bd));
		bd = makeBigDecimal(1, digitAccuracyMultipliers[1], 0);
		assertEquals("0.5 scale: 1", bd2String(bd));
		bd = makeBigDecimal(0, digitAccuracyMultipliers[1], 0);
		assertEquals("0 scale: 0", bd2String(bd));
		bd = makeBigDecimal(-1, digitAccuracyMultipliers[1], 0);
		assertEquals("-0.5 scale: 1", bd2String(bd));
		bd = makeBigDecimal(-2, digitAccuracyMultipliers[1], 0);
		assertEquals("-1 scale: 0", bd2String(bd));

		bd = makeBigDecimal(2, digitAccuracyMultipliers[1], 1);
		assertEquals("1E+1 scale: -1", bd2String(bd));
		bd = makeBigDecimal(1, digitAccuracyMultipliers[1], 1);
		assertEquals("5 scale: 0", bd2String(bd));
		bd = makeBigDecimal(0, digitAccuracyMultipliers[1], 1);
		assertEquals("0E+1 scale: -1", bd2String(bd));
		bd = makeBigDecimal(-1, digitAccuracyMultipliers[1], 1);
		assertEquals("-5 scale: 0", bd2String(bd));
		bd = makeBigDecimal(-2, digitAccuracyMultipliers[1], 1);
		assertEquals("-1E+1 scale: -1", bd2String(bd));

		bd = makeBigDecimal(2, digitAccuracyMultipliers[1], -1);
		assertEquals("0.1 scale: 1", bd2String(bd));
		bd = makeBigDecimal(1, digitAccuracyMultipliers[1], -1);
		assertEquals("0.05 scale: 2", bd2String(bd));
		bd = makeBigDecimal(0, digitAccuracyMultipliers[1], -1);
		assertEquals("0.0 scale: 1", bd2String(bd));
		bd = makeBigDecimal(-1, digitAccuracyMultipliers[1], -1);
		assertEquals("-0.05 scale: 2", bd2String(bd));
		bd = makeBigDecimal(-2, digitAccuracyMultipliers[1], -1);
		assertEquals("-0.1 scale: 1", bd2String(bd));
	}
	
	public void testUniqueValueCount() {
		Float[] data0 = new Float[] {}; 
		Float[] data1 = new Float[] { 1f }; 
		Float[] data2a = new Float[] { 0f, 0f };
		Float[] data2b = new Float[] { 0f, 1f };
		Float[] data2c = new Float[] { -4f, 4f };
		Float[] data4a = new Float[] { 0f, 0f, 1f, 1f }; 
		Float[] data4b = new Float[] { 0f, 1f, 2f, 3f };
		
		assertEquals(0, getUniqueValueCount(data0, 0));
		assertEquals(0, getUniqueValueCount(data0, 10));
		
		assertEquals(1, getUniqueValueCount(data1, 0));
		assertEquals(1, getUniqueValueCount(data1, 2));
		
		assertEquals(1, getUniqueValueCount(data2a, 0));
		assertEquals(1, getUniqueValueCount(data2a, 1));
		assertEquals(2, getUniqueValueCount(data2b, 0));
		assertEquals(1, getUniqueValueCount(data2b, 1));
		assertEquals(2, getUniqueValueCount(data2b, 2));
		assertEquals(1, getUniqueValueCount(data2c, 1));
		assertEquals(2, getUniqueValueCount(data2c, 2));
		assertEquals(2, getUniqueValueCount(data2c, 3));
		
		assertEquals(2, getUniqueValueCount(data4a, 0));
		assertEquals(1, getUniqueValueCount(data4a, 1));
		assertEquals(2, getUniqueValueCount(data4a, 2));
		assertEquals(2, getUniqueValueCount(data4a, 3));
		assertEquals(4, getUniqueValueCount(data4b, 0));
		assertEquals(1, getUniqueValueCount(data4b, 1));
		assertEquals(2, getUniqueValueCount(data4b, 2));
		assertEquals(3, getUniqueValueCount(data4b, 3));
		assertEquals(4, getUniqueValueCount(data4b, 4));
		assertEquals(4, getUniqueValueCount(data4b, 5));
	}


	@SuppressWarnings("unused")
	private void printBinningResult(String message, BigDecimal[] result) {
		System.out.println(message);
		for (int i=0; i<result.length; i++) {
			System.out.println(i + ": " + result[i]);
		}
	}

	@SuppressWarnings("unused")
	private void print(BigDecimal bd) {
		System.out.println(bd2String(bd));
	}

	private String bd2String(BigDecimal bd) {
		return bd + " scale: " + bd.scale();
	}
	
	private void doEqualCountAssert(Float[] sorted, int binCount,
			boolean round, double... expectedBins) {
		
		assertEquals(
				"TEST IS INCORRECTLY SETUP: " +
				"THE NUMBER OF BINS SHOULD MATCH (n + 1) THE EXPECTED BINS",
				binCount + 1, expectedBins.length);
		
		BigDecimal[] result = getEqualCountBins(sorted, binCount, round);
		
		String dataDesc = null;
		String binDesc = null;
		if (sorted.length < 10) {
			dataDesc = ArrayUtils.toString(sorted);
		} else {
			dataDesc =
				"{" + sorted[0] + " to " + sorted[sorted.length - 1] + "} " +
				"(" + sorted.length + " values)";
		}
		
		binDesc = "" + (expectedBins.length - 1) + " bins: " +
			ArrayUtils.toString(expectedBins);
		
		String desc = "Data " + dataDesc + " Eq. Cnt. binned to " + binDesc;
		
		doAssert(desc, result, expectedBins);
	}
	
	private void doAssert(String description, BigDecimal[] calculatedBins, double... expectedBins) {
		
		if (ENABLE_FAILURE_LOG_OUTPUT || ENABLE_ALL_LOG_OUTPUT) {
			
		}
		
		boolean testFailed = false;
		
		try {
			for (int i=0; i<calculatedBins.length; i++) {
				double calced = calculatedBins[i].doubleValue();
				double expect = expectedBins[i];
				double precision = expect / 10000d;
			
				try {
					assertEquals(expect, calced, precision);
				} catch (AssertionFailedError e) {
					testFailed = true;
					throw e;
				}
			}
		} finally {
			if (testFailed && (ENABLE_FAILURE_LOG_OUTPUT || ENABLE_ALL_LOG_OUTPUT)) {
				System.out.println("FAILED: " + description + ".  Calculated Bins:");
				for (int i=0; i<calculatedBins.length; i++) {
					System.out.println(i + ": " + calculatedBins[i]);
				}
			} else if (ENABLE_ALL_LOG_OUTPUT) {
				System.out.println(description + ".  Calculated Bins:");
				for (int i=0; i<calculatedBins.length; i++) {
					System.out.println(i + ": " + calculatedBins[i]);
				}
			}
		}
	}

}
