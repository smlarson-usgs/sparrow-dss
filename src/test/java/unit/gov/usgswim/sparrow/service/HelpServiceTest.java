package gov.usgswim.sparrow.service;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebResponse;

public class HelpServiceTest extends HTTPServiceTestHelper{

	private static final String HELP_SERVICE_URL = "http://localhost:8088/sp_help";


	@BeforeClass
	public static void setupClass() throws IOException, SAXException {
		setupHTTPUnitTest();
	}

	@AfterClass
	public static void teardownClass() {
		teardownHTTPUnitTest();
	}

	@Test
	public void testDocServiceWithFieldIDAndModelID() throws IOException, SAXException {
        String queryString = "?model=" + TEST_MODEL;

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);
        System.out.println(response.getText());
        // TODO make this a real test

	}
}
