package gov.usgswim.sparrow.util;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Properties;

import gov.usgswim.sparrow.service.HTTPServiceTestHelper;

import org.junit.Test;


public class ResourceLoaderUtilsTest {

	@Test
	public void testLoad() {
		String resourceFilePath = SparrowResourceUtils.getModelResourceFilePath(HTTPServiceTestHelper.TEST_MODEL, "models.xml");

	}

//	@Test
//	public void testPropertiesAsXML() throws IOException {
//		String propFilePath = SparrowResourceUtils.getModelResourceFilePath(HTTPServiceTestHelper.TEST_MODEL, "sessions.properties");
//		Properties props = ResourceLoaderUtils.loadResourceAsProperties(propFilePath);
//		PrintStream out = System.out;
//		props.storeToXML(out, "my comments");
//		out.flush();
//	}
}
