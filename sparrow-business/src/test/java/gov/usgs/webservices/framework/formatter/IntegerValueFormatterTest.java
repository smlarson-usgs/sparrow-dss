/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.webservices.framework.formatter;

import org.apache.commons.lang.NotImplementedException;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author cschroed
 */
public class IntegerValueFormatterTest {

	public IntegerValueFormatterTest() {
	}

	@BeforeClass
	public static void setUpClass() {
	}

	@AfterClass
	public static void tearDownClass() {
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
	}
	
	@Test
	public void testCsvBigNumberDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.CSV);
		String formatted = instance.format("123456.7890123");
		assertEquals("\"123,457\"", formatted);
	}
	
	@Test
	public void testTabBigNumberDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.TAB);
		String formatted = instance.format("123456.7890123");
		assertEquals("123,457", formatted); 
	}
	
	@Test
	public void testCsvSmallNumberDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.CSV);
		String formatted = instance.format("123.456789");
		assertEquals("Shouldn't need the wrapper quotes if there is no comma", "123", formatted);
	}
	
	@Test
	public void testTabRoundingUpDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.TAB);
		String formatted = instance.format("123456.5555");
		assertEquals("123,457", formatted); 
	}
	
	@Test
	public void testTabRoundingDownDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.TAB);
		String formatted = instance.format("123456.000004555");
		assertEquals("123,456", formatted); 
	}
	
	@Test
	public void testTabNegativeNumberDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.TAB);
		String formatted = instance.format("-123456.7890123");
		assertEquals("-123,457", formatted); 
	}
	
	@Test
	public void testCsvUnreadableNumberDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.CSV);
		String formatted = instance.format("Not a number,eh");
		assertEquals("\"Not a number,eh\"", formatted); 
	}
	
	@Test
	public void testTABUnreadableNumberDefault() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.TAB);
		String formatted = instance.format("Not a number,eh");
		assertEquals("Not a number,eh", formatted); 
	}
	
	@Test
	public void testNullValue() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.TAB);
		String formatted = instance.format(null);
		assertTrue("".equals(formatted));
	}
	
	//
	//
	@Test
	public void testTabBigNumberWoGrouping() {
		IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.TAB, null);
		String formatted = instance.format("123456.7890123");
		assertEquals("123457", formatted);
	}

	/**
	 * Test of format method, of class SimpleValueFormatter.
	 * This test ensures that the formatter throws exceptions when a user
	 * specifies an OutputType that is in the OutputType enum, but that the
	 * formatter does not implement.
	 *
	 * The current value used to throw the error is ZIP, but if
	 * SimpleValueFormatter changes to support this OutputType, it should
	 * be changed to a different OutputType that SimpleValueFormatter does
	 * not implement.
	 */
	@Test
	public void testUnsupportedOutputType(){
		try{
			IntegerValueFormatter instance = new IntegerValueFormatter(IFormatter.OutputType.ZIP);
			instance.format("dummy");
		} catch(NotImplementedException e){
			assertTrue(true);
			return;
		}
		fail("The formatter should throw an exception if it attempts to format an usupported OutputType");
	}
}