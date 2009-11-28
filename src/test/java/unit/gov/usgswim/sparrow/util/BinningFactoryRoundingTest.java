package gov.usgswim.sparrow.util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.util.BigDecimalUtils;

import java.math.BigDecimal;

import org.junit.Test;

public class BinningFactoryRoundingTest{
	@Test public void testRoundToZero() {
		assertEquals("0", BigDecimalUtils.round(.011, -.1, .44).toString());
	}

	@Test public void testRoundToSingleDigit() {
		//rounding up

		BigDecimal bd = BigDecimalUtils.round(8.77, 5.2, 10.1);
		bd = bd.setScale(0);
		assertEquals("10", bd.toString());
		assertEquals("1", BigDecimalUtils.round(.877, .52, 1.01).toString());
		assertEquals("0.1", BigDecimalUtils.round(.0877, .052, 1.01).toString());
		assertEquals("1000", BigDecimalUtils.round(870.7, 520, 1010).toString());

		// rounding down
		assertEquals("10", BigDecimalUtils.round(9.47, 5.2, 10.1).toString());
		assertEquals("1", BigDecimalUtils.round(.947, .52, 1.01).toString());
		assertEquals("0.1", BigDecimalUtils.round(.0947, .052, 1.01).toString());
		assertEquals("1000", BigDecimalUtils.round(940.7, 520, 1010).toString());
	}

	@Test public void testRoundTo1_5Digits() {
		// rounding up
		assertEquals("8.5", BigDecimalUtils.round(8.67, 8.2, 8.91).toString());
		assertEquals("0.85", BigDecimalUtils.round(.867, .82, .891).toString());
		assertEquals("0.085", BigDecimalUtils.round(.0867, .082, .0891).toString());
		assertEquals("850", BigDecimalUtils.round(860.7, 820, 891).toString());
	}

	@Test public void testRoundTo2Digits() {
		//rounding up
		assertEquals("8.8", BigDecimalUtils.round(8.77, 8.52, 8.91).toString());
		assertEquals("0.88", BigDecimalUtils.round(.877, .852, .891).toString());
		assertEquals("0.088", BigDecimalUtils.round(.0877, .0852, .0891).toString());
		assertEquals("0.000088", BigDecimalUtils.round(.0000877, .0000852, .0000891).toString());
		assertEquals("880", BigDecimalUtils.round(877.77, 852, 891).toString());

		// rounding down
		assertEquals("9.4", BigDecimalUtils.round(9.44, 9.25, 9.49).toString());
		assertEquals("0.94", BigDecimalUtils.round(.944, .925, .949).toString());
		assertEquals("0.094", BigDecimalUtils.round(.0944, .0925, .0949).toString());
		assertEquals("0.000094", BigDecimalUtils.round(.0000944, .0000925, .0000949).toString());
		assertEquals("940", BigDecimalUtils.round(940.7, 925, 949).toString());
	}

	@Test public void testTooNarrowRoundingRange() {
		// String.startsWith() used since representation of value is not truncated,
		// (i.e., 8.770009000001 is returned as 8.770009000001[2342393758713480])
		assertTrue(BigDecimalUtils.round(8.770009000001, 8.770005000001, 8.770091).toString().startsWith("8.770009000001"));
	}
}
