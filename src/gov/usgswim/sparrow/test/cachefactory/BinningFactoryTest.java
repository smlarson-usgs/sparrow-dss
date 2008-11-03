package gov.usgswim.sparrow.test.cachefactory;

import junit.framework.TestCase;
import static gov.usgswim.sparrow.cachefactory.BinningFactory.round;

public class BinningFactoryTest extends TestCase {
	
	public void testRoundToZero() {
		assertEquals(0, round(.011, -.1, .44), .001);
	}
	
	public void testRoundToSingleDigit() {
		//rounding up
		assertEquals(9, round(8.77, 5.2, 10.1), .001);
		assertEquals(.9, round(.877, .52, 1.01), .001);
		assertEquals(.09, round(.0877, .052, 1.01), .0001);
		assertEquals(900, round(870.7, 520, 1010), .001);
		
		// rounding down
		assertEquals(9, round(9.47, 5.2, 10.1), .001);
		assertEquals(.9, round(.947, .52, 1.01), .001);
		assertEquals(.09, round(.0947, .052, 1.01), .0001);
		assertEquals(900, round(940.7, 520, 1010), .001);
	}
	
	public void testRoundTo1_5Digits() {
		// rounding up
		assertEquals(8.5, round(8.67, 8.2, 8.91), .001);
		assertEquals(.85, round(.867, .82, .891), .001);
		assertEquals(.085, round(.0867, .082, .0891), .0001);
		assertEquals(850, round(860.7, 820, 891), .001);
	}
	
	public void testRoundTo2Digits() {
		
	}

}
