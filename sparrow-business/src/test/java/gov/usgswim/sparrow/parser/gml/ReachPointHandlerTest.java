package gov.usgswim.sparrow.parser.gml;

import gov.usgswim.sparrow.parser.gml.ReachPointParser.ReachPointInfo;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

public class ReachPointHandlerTest {
	/**
	 * Test a known XML document to make sure the parser/handler work as expected
	 */
	@Test
	public void reachPointParsing() {
		double longitude = -87.99842834472656;
		double latitude = 30.226821899414062;
		
		String xmlTestFile = "_data.xml";
		String testResourcesPath = "src/test/resources";
		String basePath = ReachPointHandlerTest.class.getName().replace('.', '/');
		basePath = basePath + xmlTestFile;
		
		String fullPath = "";
		try {
			fullPath = new File(".").getCanonicalPath();
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		
		fullPath = fullPath + "/" + testResourcesPath + "/" + basePath;
		
		ReachPointParser rpParser = null;
		try {
			rpParser = new ReachPointParser(latitude, longitude);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}
		
		/**
		 * We have a hard coded XML file at the above path and we know we should
		 * get a Reach ID and a distance of:
		 * 		ReachID:  81202
		 * 		Distance: 0.14578610858288282
		 */		
		try {
			ReachPointInfo results = rpParser.parseReachPointSource(fullPath);
			Assert.assertEquals(results.getReachIdentifier(), 81202);
			Assert.assertEquals(results.getDistance(), 0.14578610858288282, 0);
		} catch (Exception e) {
			Assert.fail(e.getMessage());
		}		
	}
}
