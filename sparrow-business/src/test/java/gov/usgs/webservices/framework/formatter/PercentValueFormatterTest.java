/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.webservices.framework.formatter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
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
public class PercentValueFormatterTest {

	public PercentValueFormatterTest() {
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
	 * Test of format method, of class HTMLRelativePercentValueFormatter.
	 */
	@Test
	public void testFormat() {

		String[][]  pairs = new String[][]{
		//      {input , expected output}
			{"100", "100"},
			{"99.99999", "99.99999"},
			{"50", "50"},
			{"50.02", "50.02"},
			{"1", "1"},
			{"0.99999", PercentValueFormatter.lessThanOne},
			{"0.0001", PercentValueFormatter.lessThanOne},
			{"0.00001", PercentValueFormatter.lessThanOne},
			{"0", PercentValueFormatter.lessThanOne},
			{"-1", PercentValueFormatter.lessThanOne},
			{"-1.09", PercentValueFormatter.lessThanOne},
			{"-10.9", PercentValueFormatter.lessThanOne},
			{"garbage", PercentValueFormatter.lessThanOne}
		};
		Map<String, String> inputToExpectedOutputMap = new HashMap<String, String>();


		for(String[] pair : pairs){
			inputToExpectedOutputMap.put(pair[0], pair[1]);
		}

		PercentValueFormatter formatter = new PercentValueFormatter();
		for(String input : inputToExpectedOutputMap.keySet()){
			assertEquals(inputToExpectedOutputMap.get(input), formatter.format(input));
		}

	}
}