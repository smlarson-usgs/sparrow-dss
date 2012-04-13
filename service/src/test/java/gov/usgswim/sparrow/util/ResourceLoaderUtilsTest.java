
package gov.usgswim.sparrow.util;

import gov.usgs.webservices.framework.utils.ResourceLoaderUtils;

import java.util.Properties;

import org.junit.Test;
import static org.junit.Assert.*;


public class ResourceLoaderUtilsTest {
	public static final String NONEXISTENT_FILE = "IDONOTEXIST.xml";


	@Test
	public void testLoadResourceAsPropertiesOfNonExistentModel() {
		Properties props = ResourceLoaderUtils.loadResourceAsProperties(NONEXISTENT_FILE);
		assertNotNull(props);
		assertTrue("loadResourceAsProperties() should return empty properties if file resource does not exist", props.isEmpty());
	}

}