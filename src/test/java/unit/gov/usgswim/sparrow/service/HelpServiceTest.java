package gov.usgswim.sparrow.service;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import gov.usgswim.sparrow.SparrowServiceUnitTest;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebResponse;

public class HelpServiceTest extends SparrowServiceUnitTest{

	private static final String HELP_SERVICE_URL = "http://localhost:8088/sp_help";

	@Test
	public void testLookupWithFieldIDAndModelID() throws IOException, SAXException {
        String queryString = "/lookup?model=" + SERVICE_TEST_MODEL + "&item=Model_name";

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);
        String responseBody = response.getText();
        assertFalse(responseBody.contains("error"));
        assertTrue(response.getText().contains("Test Model"));

	}

	@Test
	public void testgetSimpleKeys() throws IOException, SAXException {
        String queryString = "/getSimpleKeys?model=" + SERVICE_TEST_MODEL;

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);

        String responseBody = response.getText();
        assertFalse(responseBody.contains("error"));
        assertTrue(responseBody.contains("<keys>"));
        assertTrue(responseBody.contains("key>Authors.1</key"));

	}

	@Test
	public void testgetListKeys() throws IOException, SAXException {
        String queryString = "/getListKeys?model=" + SERVICE_TEST_MODEL;

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);

        String responseBody = response.getText();
        assertFalse(responseBody.contains("error"));
        assertTrue(responseBody.contains("<keys>"));
        assertTrue(responseBody.contains(">Sources<"));

	}

	@Test
	public void testgetList() throws IOException, SAXException {
        String queryString = "/getList?model=" + SERVICE_TEST_MODEL + "&listKey=Sources";

        WebResponse response = client.getResponse( HELP_SERVICE_URL + queryString);

        String responseBody = response.getText();
        //System.out.println(responseBody);
        assertFalse(responseBody.contains("error"));
        assertTrue(responseBody.contains("<list>"));
        assertTrue(responseBody.contains("<Sources>"));

	}
}
