package gov.usgswim.sparrow.parser.gml;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class ReachOverlayIdentifierHandlerTest {
	/**
	 * Test a known GML document to make sure the parser/handler work as expected
	 */
	@Test
	public void reachOverlayParsing() {
		String xmlTestFile = "_data.xml";
		String testResourcesPath = "src/test/resources";
		String basePath = ReachOverlayIdentifierHandlerTest.class.getName().replace('.', '/');
		basePath = basePath + xmlTestFile;
		
		String fullPath = "";
		try {
			fullPath = new File(".").getCanonicalPath();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		
		fullPath = fullPath + "/" + testResourcesPath + "/" + basePath;
		
		ReachOverlayIdentifierParser roiParser = null;
		try {
			roiParser = new ReachOverlayIdentifierParser();
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
		/**
		 * We have a hard coded GML file at the above path and we know we should
		 * get a list of IDs that look like:
		 * 	7666, 7349, 7351, 7347, 7323, 664590
		 */
		List<String> expected = Arrays.asList("7666","7349","7351","7347","7323","664590");
		
		try {
			List<String> results = roiParser.parseReachOverlayIdentifierSource(fullPath);
			Assert.assertTrue(expected.equals(results));
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}		
	}
}
