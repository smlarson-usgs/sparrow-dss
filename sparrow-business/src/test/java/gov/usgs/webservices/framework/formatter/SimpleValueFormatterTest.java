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
public class SimpleValueFormatterTest {

	public SimpleValueFormatterTest() {
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
	public void testFormat() {
		try{
			SimpleValueFormatter instance = new SimpleValueFormatter(IFormatter.OutputType.ZIP);
			instance.format("dummy");
		}
		catch(NotImplementedException e){
			assertTrue(true);
			return;
		}
		fail("The formatter should throw an exception if it attempts to format an usupported OutputType");
	}
}