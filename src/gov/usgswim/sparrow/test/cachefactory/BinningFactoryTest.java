package gov.usgswim.sparrow.test.cachefactory;

import junit.framework.TestCase;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.round;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.getEqualCountBins;
public class BinningFactoryTest extends TestCase {

	public void testRoundToZero() {
		assertEquals(0, round(.011, -.1, .44), .001);
	}

	public void testRoundToSingleDigit() {
		//rounding up
		assertEquals(10, round(8.77, 5.2, 10.1), .001);
		assertEquals(1.0, round(.877, .52, 1.01), .001);
		assertEquals(.1, round(.0877, .052, 1.01), .0001);
		assertEquals(1000, round(870.7, 520, 1010), .001);

		// rounding down
		assertEquals(10, round(9.47, 5.2, 10.1), .001);
		assertEquals(1.0, round(.947, .52, 1.01), .001);
		assertEquals(.10, round(.0947, .052, 1.01), .0001);
		assertEquals(1000, round(940.7, 520, 1010), .001);
	}

	public void testRoundTo1_5Digits() {
		// rounding up
		assertEquals(8.5, round(8.67, 8.2, 8.91), .001);
		assertEquals(.85, round(.867, .82, .891), .001);
		assertEquals(.085, round(.0867, .082, .0891), .0001);
		assertEquals(850, round(860.7, 820, 891), .001);
	}

	public void testRoundTo2Digits() {
		//rounding up
		assertEquals(8.8, round(8.77, 8.52, 8.91), .0001);
		assertEquals(.88, round(.877, .852, .891), .0001);
		assertEquals(.088, round(.0877, .0852, .0891), .00001);
		assertEquals(.000088, round(.0000877, .0000852, .0000891), .00000001);
		assertEquals(880, round(877.77, 852, 891), .0001);

		// rounding down
		assertEquals(9.4, round(9.44, 9.25, 9.49), .0001);
		assertEquals(.94, round(.944, .925, .949), .0001);
		assertEquals(.094, round(.0944, .0925, .0949), .00001);
		assertEquals(.000094, round(.0000944, .0000925, .0000949), .00000001);
		assertEquals(940, round(940.7, 925, 949), .0001);
	}

	public void testTooNarrowRoundingRange() {
		assertEquals(8.77, round(8.77, 8.770052, 8.770091), .0001);
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
		double[] result = getEqualCountBins(sortedData, 1, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals(-100d, result[0], .00001);
		assertEquals(6d, result[lastIndex], .00001);
	}
	
	public void testGetEqualCountBinsOfTwo() {
		double[] result = getEqualCountBins(sortedData, 2, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals(-100d, result[0], .00001);
		assertEquals(.01d, result[1], .00001);
		assertEquals(6d, result[lastIndex], .00001);
	}
	
	public void testGetEqualCountBinsOfThree() {
		double[] result = getEqualCountBins(sortedData, 3, Boolean.TRUE);
		
		int lastIndex = result.length - 1;
		assertEquals(-100d, result[0], .00001);
		assertEquals(-.5d, result[1], .00001);
		assertEquals(1.0d, result[2], .00001);
		assertEquals(6d, result[lastIndex], .00001);
	}

	public void testGetEqualCountBinsOfFour() {
		double[] result = getEqualCountBins(sortedData, 4, Boolean.TRUE);
		
		int lastIndex = result.length - 1;
		assertEquals(-100d, result[0], .00001);
		assertEquals(-5d, result[1], .00001);
		assertEquals(.01, result[2], .00001);
		assertEquals(1.3, result[3], .00001);
		assertEquals(6d, result[lastIndex], .00001);		
	}
	
	public void testGetEqualCountBinsOfFive() {
		double[] result = getEqualCountBins(sortedData, 5, Boolean.TRUE);
		
		int lastIndex = result.length - 1;
		assertEquals(-100d, result[0], .00001);
		assertEquals(-8.75d, result[1], .00001);
		assertEquals(0, result[2], .00001);
		assertEquals(.094, result[3], .00001);
		assertEquals(1.8, result[4], .00001);
		assertEquals(6d, result[lastIndex], .00001);
	}
	
	public void testGetEqualCountBinsOfSix() {
		double[] result = getEqualCountBins(sortedData, 6, Boolean.TRUE);
		
		int lastIndex = result.length - 1;
		assertEquals(-100d, result[0], .00001);
		assertEquals(-17d, result[1], .00001);
		assertEquals(-.5, result[2], .00001);
		assertEquals(.01, result[3], .00001);
		assertEquals(1.0, result[4], .00001);
		assertEquals(1.87, result[5], .00001);
		assertEquals(6d, result[lastIndex], .00001);
	}
	
	public void testGetEqualCountBinsOfSeven() {

		double[] result = getEqualCountBins(sortedData, 7, Boolean.TRUE);
		
		int lastIndex = result.length - 1;
		assertEquals(-100d, result[0], .00001);
		assertEquals(-50d, result[1], .00001);
		assertEquals(-3, result[2], .00001);
		assertEquals(.0054, result[3], .00001);
		assertEquals(.08, result[4], .00001);
		assertEquals(1.19, result[5], .00001);
		assertEquals(1.89, result[6], .00001);
		assertEquals(6d, result[lastIndex], .00001);
		
		for (int i=0; i<result.length; i++) {
			System.out.println(i + ": " + result[i]);
		}
		
	}
}
