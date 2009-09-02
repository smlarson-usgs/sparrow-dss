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
	public void testLookupWithFieldIDAndModelID() throws IOException, SAXException {
        String queryString = "/lookup?model=" + TEST_MODEL;

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);
        System.out.println(response.getText());
        // TODO make this a real test
	}

	@Test
	public void testgetSimpleKeys() throws IOException, SAXException {
        String queryString = "/getSimpleKeys?model=" + TEST_MODEL;

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);
        System.out.println(response.getText());
        // TODO make this a real test

	}

	@Test
	public void testgetListKeys() throws IOException, SAXException {
        String queryString = "/getListKeys?model=" + TEST_MODEL;

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);
        System.out.println(response.getText());
        // TODO make this a real test

	}

	@Test
	public void testgetList() throws IOException, SAXException {
        String queryString = "/getList?model=" + TEST_MODEL;

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);
        System.out.println(response.getText());
        // TODO make this a real test

	}
}
