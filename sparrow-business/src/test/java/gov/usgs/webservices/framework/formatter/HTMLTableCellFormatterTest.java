/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.usgs.webservices.framework.formatter;

import gov.usgs.webservices.framework.formatter.IFormatter.OutputType;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
public class HTMLTableCellFormatterTest {

	public HTMLTableCellFormatterTest() {
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
	 * Test of format method, of class HTMLTableCellFormatter.
	 */
	@Test
	public void testFormat() {
		OutputType outputTypes[] = {OutputType.XHTML, OutputType.XHTML_TABLE, OutputType.XML};
		HTMLTableCellFormatter formatter;
		Map<String, String> inputToExpected = this.getInputToExpectedMap();
		for(OutputType outputType : outputTypes){
			 formatter = new HTMLTableCellFormatter(outputType);
			 for(String input : inputToExpected.keySet()){
				 String expectedValue = inputToExpected.get(input);
				 String actualValue = formatter.format(input);
				 assertEquals(expectedValue, actualValue);
			 }
		}
	}
	private Map<String, String> getInputToExpectedMap(){
		HashMap<String, String> inputToExpectedOutput = new HashMap<String, String>();
		//easy on the fingers, alias:
		Map<String, String> i2o = inputToExpectedOutput;
		//check #'s where 0 == integer component
		i2o.put("0.1", "0.1");
		i2o.put("0.12", "0.12");
		i2o.put("0.123", "0.123");
		//ensure no comma insertions in fraction portion of number
		i2o.put("0.1234", "0.1234");

		//check #'s where 0 < integer component
		i2o.put("1700", "1,700");
		i2o.put("1700.123", "1,700.123");
		i2o.put("1700.1234", "1,700.1234");
		i2o.put("1700.02345", "1,700.02345");
		i2o.put("170002345", "170,002,345");
		i2o.put("1100700345", "1,100,700,345");
		i2o.put("10000700345", "10,000,700,345");
		i2o.put("100000700345", "100,000,700,345");
		i2o.put("1000000700345", "1,000,000,700,345");
		//check non-#'s
		i2o.put("ALABAMA", "ALABAMA");
		i2o.put("asdf 123", "asdf 123");
		i2o.put("asdf123", "asdf123");
		i2o.put("123asdf", "123asdf");
		i2o.put("123 asdf", "123 asdf");

		Map<String, String> inputToExpectedWithFixedExpectedOutput = new HashMap<String, String>();

		for(String key : inputToExpectedOutput.keySet()){
			String value = inputToExpectedOutput.get(key);
			inputToExpectedWithFixedExpectedOutput.put(key,
				HTMLTableCellFormatter.PREFIX +
				value +
				HTMLTableCellFormatter.SUFFIX);
		}
		return inputToExpectedWithFixedExpectedOutput;
	}
}