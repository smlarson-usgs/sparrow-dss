package gov.usgswim.sparrow.service;

import static gov.usgswim.sparrow.service.ServiceTestConstants.WEB_XML_LOCATION;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;


public class SavedSessionServiceTest {

	private static final String SESSION_SERVICE_URL = "http://localhost:8088/sp_session";
	static ServletRunner servletRunner;
	static ServletUnitClient client;

	@BeforeClass
	public static void setupClass() throws IOException, SAXException {
		servletRunner = new ServletRunner(new File(WEB_XML_LOCATION));
		client = servletRunner.newClient();
	}

	@Test
	public void testNoModelSubmitted() throws IOException, SAXException {
        WebResponse response = client.getResponse( SESSION_SERVICE_URL );
        assertTrue("response should contain 'invalid'", response.getText().contains("invalid"));
	}

	@Test
	public void testRetrieveAllSessions() throws IOException, SAXException {
        WebResponse response = client.getResponse( SESSION_SERVICE_URL + "?model=-1");
        assertTrue("Response should contain a <sessions> element", response.getText().contains("<sessions>"));
	}

	@Test
	public void testRetrieveDesignatedSession() throws IOException, SAXException {
        WebResponse response = client.getResponse( SESSION_SERVICE_URL + "?model=-1&session=mySession" );
        assertTrue("Response should contain a JSON object (but is currently hello world)", response.getText().contains("hello world"));
	}
}
