package gov.usgs.cida.config;

import static gov.usgs.cida.config.DynamicReadOnlyProperties.SUBST_REGEXP;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gov.usgs.cida.config.DynamicReadOnlyProperties.NullKeyHandlingOption;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

import net.sf.ehcache.CacheManager;

import org.junit.Test;


public class DynamicReadOnlyPropertiesTest {


	private static final String TEST_STRING = "My name is ${username} and I cost ${price} -- less than $10{}  false ${";

	@Test
	public void testRegularExpressionPattern() {
		Matcher m = SUBST_REGEXP.matcher(TEST_STRING);

		assertTrue(m.find());
		assertEquals(11, m.start());
		assertEquals("First match should be the username key","username", m.group(1));

		assertTrue(m.find());
		assertEquals(34, m.start());
		assertEquals("Second match should be the price key","price", m.group(1));

		assertFalse("Remaining braces and $ are false matches and should not be found", m.find());

	}

	@Test
	public void testRegularExpressionPattern_SubstitutionLogic() {
		String value = TEST_STRING;
		Matcher m = SUBST_REGEXP.matcher(value);

		assertTrue(m.find());
		assertEquals(11, m.start());
		assertEquals("First match should be the username key","username", m.group(1));

		// Substituting is more painful than it really ought to be
		int startPos = m.start();
		int endPos = m.end();
		value =  value.substring(0, startPos) + "I-Lin" + value.substring(endPos);

		assertEquals("My name is I-Lin and I cost ${price} -- less than $10{}  false ${", value);
	}

	@Test
	public void testRegularExpressionPattern_SubstitutionLogicEdgeCases() {
		String value = "${beginingMatch} and an ${endMatch}";
		Matcher m = SUBST_REGEXP.matcher(value);

		assertTrue(m.find());
		assertEquals(0, m.start());
		assertEquals("First match should be the beginingMatch key","beginingMatch", m.group(1));

		// Substituting is more painful than it really ought to be
		int startPos = m.start();
		int endPos = m.end();
		value =  value.substring(0, startPos) + "I-Lin" + value.substring(endPos);

		assertEquals("I-Lin and an ${endMatch}", value);

		m = SUBST_REGEXP.matcher(value);
		assertTrue(m.find());
		startPos = m.start();
		endPos = m.end();
		value =  value.substring(0, startPos) + "I-Lin" + value.substring(endPos);
		assertEquals("I-Lin and an I-Lin", value);
	}

	@Test
	public void testGetEqualsGetProperty() {
		Map<String, String> map = getTestMap();
		DynamicReadOnlyProperties props = new DynamicReadOnlyProperties(map);
		
		// Check equality for keys in the map
		for (String key: map.keySet()) {
			assertEquals(props.get(key), props.getProperty(key));
		}
		
		// Check equality for keys not in the map
		String nonExistentKey = "HowdyDoody";
		assertEquals(props.get(nonExistentKey), props.getProperty(nonExistentKey));
	}
	
	@Test
	public void testExpand() {
		Map<String, String> map = new HashMap<String, String>();
		map.put("username", "George Bush");
		map.put("password", "failure");
		map.put("url", "miserable failure");
		map.put("message.login.all", "LOGIN ERROR ${message.login.error.badusername}; ${message.login.error.badconnection}");
		map.put("message.login.error.badusername", "bad username ${username}");
		map.put("message.login.error.badconnection", "bad connection url ${url}");

		map = DynamicReadOnlyProperties.expand(map);

		assertEquals(6, map.size());
		assertEquals("George Bush", map.get("username"));
		assertEquals("failure", map.get("password"));
		assertEquals("miserable failure", map.get("url"));
		assertEquals("bad username George Bush", map.get("message.login.error.badusername"));
		assertEquals("bad connection url miserable failure", map.get("message.login.error.badconnection"));
		assertEquals("LOGIN ERROR bad username George Bush; bad connection url miserable failure",
				map.get("message.login.all"));
	}

	@Test
	public void testExpand_WithDeeperNesting() {
		Map<String, String> map = new HashMap<String, String>();
		{ //initialize the map
			map.put("result", "${z1} ${w9}");
			map.put("z1", "${y1} ${y2}");
			map.put("y2", "${x3} ${x4}");
			map.put("y1", "${x1} ${x2}");
			map.put("x4", "${w7} ${w8}");
			map.put("x3", "${w5} ${w6}");
			map.put("x2", "${w3} ${w4}");
			map.put("x1", "${w1} ${w2}");
			map.put("w1", "George");
			map.put("w2", "Bush");
			map.put("w3", "is");
			map.put("w4", "a");
			map.put("w5", "miserable");
			map.put("w6", "failure");
			map.put("w7", "as");
			map.put("w8", "a");
			map.put("w9", "president");
		}

		map = DynamicReadOnlyProperties.expand(map);

		assertEquals("George Bush is a miserable failure as a president",
				map.get("result"));
	}

	@Test 
	public void testExpand_WithNonExistentKey_AndDefaultNullHandling() {
		Map<String, String> map = getTestMap();

		map = DynamicReadOnlyProperties.expand(map);

		assertEquals("The nonexistent key for 'ocean' should not be touched",
				"My bonnie lies over the ${ocean}",	map.get("result"));
	}
	
	@Test
	public void testExpandWithInputString_UseDefault_DO_NOTHING_NullHandling() throws IOException {
		Map<String, String> map = getTestMap();
		
		DynamicReadOnlyProperties dynProps = new DynamicReadOnlyProperties(map);
		InputStream in = new ByteArrayInputStream("My ${bonnie} lies over the ${ocean}".getBytes());
		InputStream out = dynProps.expand(in);
		StringBuilder result = DynamicReadOnlyProperties.readStream2StringBuilder(out);
		
		assertEquals("My bonnie lies over the ${ocean}", result.toString());
	}
	
	
	@SuppressWarnings("unchecked")
	@Test 
	public void testAsMap() {
		Map<String, String> map = new HashMap<String, String>();
		{ //initialize the map
			map.put("George", "is my Bush");
		}

		DynamicReadOnlyProperties prop = new DynamicReadOnlyProperties(map);
		map = prop.asMap();
		//		for (String key: map.keySet()) {
		//			System.out.println(key + ": " + map.get(key));
		//		}

		assertEquals("George is in the map/properties", "is my Bush", map.get("George"));
	}
	
	// [IK] uncomment this later when we have mavenized and can easily mock a JNDI context
//	@Test
//	public void testJNDILookup() {
//		DynamicReadOnlyProperties prop = new DynamicReadOnlyProperties();
//		prop.addJNDIContexts();
//		
//		System.out.println(prop.get("java:myJNDIProp"));
//		
//	}
	// TODO need tests for JNDI capabilities, but wait until mavenized to use
	// Spring's mock, otherwise, dependencies too hard to manage.

	// ===================
	// HELPER TEST METHODS
	// ===================
	public Map<String, String> getTestMap() {
		Map<String, String> map = new HashMap<String, String>();
		{ //initialize the map
			map.put("result", "My ${bonnie} lies over the ${ocean}");
			map.put("bonnie", "bonnie");
		}
		return map;
	}


}
