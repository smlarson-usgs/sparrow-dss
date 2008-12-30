package gov.usgswim.sparrow.test.cachefactory;

import java.math.BigDecimal;

import junit.framework.TestCase;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.round;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.getEqualCountBins;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.digitAccuracyMultipliers;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.makeBigDecimal;

public class BinningFactoryTest extends TestCase {

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


	public static float[] sortedData = {-99.73f, -97.544f, -93.8991f, -86.773f, -85.1101f,
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

	public void testGetEqualCountBinsOfOne() {
		BigDecimal[] result = getEqualCountBins(sortedData, 1, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfTwo() {
		BigDecimal[] result = getEqualCountBins(sortedData, 2, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("0.01", result[1].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfThree() {
		BigDecimal[] result = getEqualCountBins(sortedData, 3, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-0.5", result[1].toString());
		assertEquals("1", result[2].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfFour() {
		BigDecimal[] result = getEqualCountBins(sortedData, 4, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-5", result[1].toString());
		assertEquals("0.01", result[2].toString());
		assertEquals("1.3", result[3].toString());
		assertEquals("6", result[lastIndex].toString());		
	}

	public void testGetEqualCountBinsOfFive() {
		BigDecimal[] result = getEqualCountBins(sortedData, 5, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-8.75", result[1].toString());
		assertEquals("0", result[2].toString());
		assertEquals("0.094", result[3].toString());
		assertEquals("1.8", result[4].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	public void testGetEqualCountBinsOfSix() {
		BigDecimal[] result = getEqualCountBins(sortedData, 6, Boolean.TRUE);

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

		BigDecimal[] result = getEqualCountBins(sortedData, 7, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-50", result[1].toString());
		assertEquals("-3", result[2].toString());
		assertEquals("0.0054", result[3].toString());
		assertEquals("0.08", result[4].toString());
		assertEquals("1.19", result[5].toString());
		assertEquals("1.89", result[6].toString());
		assertEquals("6", result[lastIndex].toString());

		// uncomment this to see the output
		//printBinningResult(result);

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


	@SuppressWarnings("unused")
	private void printBinningResult(BigDecimal[] result) {
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

}
