package gov.usgswim.sparrow.service;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebResponse;


public class SavedSessionServiceTest extends SparrowServiceTest{

	private static final String SESSION_SERVICE_URL = "http://localhost:8088/sp_session";

	@Test
	public void testNoModelSubmitted() throws IOException, SAXException {
        WebResponse response = client.getResponse( SESSION_SERVICE_URL );
        assertTrue("Response should contain 'invalid'", response.getText().contains("invalid"));

	}

	@Test
	public void testRetrieveAllSessions() throws IOException, SAXException {
        WebResponse response = client.getResponse( SESSION_SERVICE_URL + "?model=" + TEST_MODEL);
        assertTrue("Response should contain a <sessions> element", response.getText().contains("<sessions>"));
	}

	@Test
	public void testRetrieveDesignatedSession() throws IOException, SAXException {
        WebResponse response = client.getResponse( SESSION_SERVICE_URL + "?model=" + TEST_MODEL + "&session=mySession" );
        assertTrue("Response should contain a JSON object (but is currently hello world)", response.getText().contains("hello world"));
	}
}
