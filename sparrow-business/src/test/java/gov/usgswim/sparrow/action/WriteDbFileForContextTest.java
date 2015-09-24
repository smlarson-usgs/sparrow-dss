package gov.usgswim.sparrow.action;

import gov.usgswim.sparrow.service.SharedApplication;
import java.io.File;
import java.io.IOException;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author eeverman
 */
public class WriteDbFileForContextTest {
	
	@Before
	public void before() {
		SharedApplication.getInstance().getConfiguration().remove("data-export-directory");
	}
	
	@Test
	public void testGetDefaultDataDirectoryWithoutSysprops() throws IOException {
			System.out.println("testGetDefaultDataDirectoryWithoutSysprops");
			WriteDbfFileForContext obj = new WriteDbfFileForContext();
			File result = obj.getDataDirectory();
			String  assertion = System.getProperty("user.home") 
												 + File.separatorChar 
												 + "sparrow"
												 + File.separatorChar
												 + "data";
			assertNotNull(result);
			assertEquals(result.getCanonicalPath(), assertion);
	}
         
	@Test
	 public void testGetDefaultDataDirectoryWithSysprops() throws IOException {
			 System.out.println("testGetDefaultDataDirectoryWithSysprops");
			 String TEST_PATH = "/i/am/a/test/property/file/path";
			 SharedApplication.getInstance().getConfiguration().setProperty("data-export-directory", TEST_PATH);
			 WriteDbfFileForContext obj = new WriteDbfFileForContext();
			 File result = obj.getDataDirectory();
			 String  assertion = TEST_PATH;
			 assertNotNull(result);
			 assertEquals(result.getCanonicalPath(), assertion);
	 }

}
