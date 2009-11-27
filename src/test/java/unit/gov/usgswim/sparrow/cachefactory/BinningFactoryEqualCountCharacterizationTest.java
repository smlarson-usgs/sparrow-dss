package gov.usgswim.sparrow.cachefactory;

import static org.junit.Assert.assertEquals;

import java.math.BigDecimal;

import org.junit.Test;


public class BinningFactoryEqualCountCharacterizationTest {
	public static Double[] sortedData100Normal = {-99.73d, -97.544d, -93.8991d, -86.773d, -85.1101d,
		-84.897d, -83.511d, -83.509d, -83.507d, -83.505d,
		-83.404d, -83.403d, -78.011d, -72.555d, -71.77d,
		-22.378d, -18.22d, -15.099d, -14.932d, -8.753d,
		-8.7519d, -8.7498d, -7.412d, -6.071d, -5.873d,
		-4.332d, -3.256d, -3.225d, -3.0012d, -2.983d,
		-1.455d, -.8896d, -.7337d, -.6332d, -.2219d,
		-.2189d, -.20166d, -.20133d, -.200988d, -.20977d,
		.004334d, .005112d, .005339d, .005455d, .0056711d,
		.0093443d, .0094775d, .0094993d, .009554d, .0096798d,
		.01003d, .01025d, .010378d, .010408d, .01153d,
		.02334d, .06778d, .07112d, .08951d, .09234d,
		.09443d, .09493d, .09512d, .09593d, .09607d,
		.09889d, .09912d, 1.003d, 1.074d, 1.082d,
		1.134d, 1.188d, 1.191d, 1.234d, 1.265d,
		1.277d, 1.341d, 1.456d, 1.567d, 1.612d,
		1.789d, 1.834d, 1.8402d, 1.862d, 1.871d,
		1.888d, 1.891d, 1.899d, 1.902d, 1.905d,
		1.908d, 1.911d, 1.912d, 1.913d, 1.914d,
		1.915d, 1.977d, 2.111d, 3.445d, 5.121d
	}; // 100 elements
	

	@Test public void testGetEqualCountBinsOfOne() {
		BigDecimal[] result = BinningFactory.buildEqualCountBins(sortedData100Normal, 1, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	@Test public void testGetEqualCountBinsOfTwo() {
		BigDecimal[] result = BinningFactory.buildEqualCountBins(sortedData100Normal, 2, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("0.01", result[1].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	@Test public void testGetEqualCountBinsOfThree() {
		BigDecimal[] result = BinningFactory.buildEqualCountBins(sortedData100Normal, 3, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-0.5", result[1].toString());
		assertEquals("1", result[2].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	@Test public void testGetEqualCountBinsOfFour() {
		BigDecimal[] result = BinningFactory.buildEqualCountBins(sortedData100Normal, 4, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-5", result[1].toString());
		assertEquals("0.01", result[2].toString());
		assertEquals("1.3", result[3].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	@Test public void testGetEqualCountBinsOfFive() {
		BigDecimal[] result = BinningFactory.buildEqualCountBins(sortedData100Normal, 5, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-8.75", result[1].toString());
		assertEquals("0", result[2].toString());
		assertEquals("0.094", result[3].toString());
		assertEquals("1.8", result[4].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	@Test public void testGetEqualCountBinsOfSix() {
		BigDecimal[] result = BinningFactory.buildEqualCountBins(sortedData100Normal, 6, Boolean.TRUE);

		int lastIndex = result.length - 1;
		assertEquals("-100", result[0].toString());
		assertEquals("-17", result[1].toString());
		assertEquals("-0.5", result[2].toString());
		assertEquals("0.01", result[3].toString());
		assertEquals("1", result[4].toString());
		assertEquals("1.87", result[5].toString());
		assertEquals("6", result[lastIndex].toString());
	}

	@Test public void testGetEqualCountBinsOfSeven() {

		BigDecimal[] result = BinningFactory.buildEqualCountBins(sortedData100Normal, 7, Boolean.TRUE);

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
}
