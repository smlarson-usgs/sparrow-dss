package gov.usgswim.sparrow.service.idbypoint;

import static org.junit.Assert.*;

import org.junit.Test;

public class TruncateDecimalTest {

	@Test public void testApply() {
		// truncate to two places after decimal
		TruncateDecimal rule = new TruncateDecimal(2);

		String result = rule.apply("1");
		assertEquals("no effect expected", "1", result);

		result = rule.apply("1.2");
		assertEquals("no effect expected", "1.2", result);

		result = rule.apply("1.23");
		assertEquals("no effect expected", "1.23", result);

		result = rule.apply("1.234");
		assertEquals("should round to two digits right of decimal", "1.23", result);

		result = rule.apply("1.2345");
		assertEquals("should round to two digits right of decimal", "1.23", result);

	}

}
