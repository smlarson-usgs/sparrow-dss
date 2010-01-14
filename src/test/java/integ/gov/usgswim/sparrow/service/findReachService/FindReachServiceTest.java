package gov.usgswim.sparrow.service.findReachService;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.service.HTTPServiceTestHelper;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class FindReachServiceTest extends HTTPServiceTestHelper{

	private static final String FINDREACH_SERVICE_URL = "http://localhost:8088/sp_findReach";

	@BeforeClass
	public static void setupClass() throws IOException, SAXException {
		setupHTTPUnitTest();
	}

	@AfterClass
	public static void teardownClass() {
		teardownHTTPUnitTest();
	}

	String sampleRequest1 = "<sparrow-reach-request"
		+ "  xmlns=\"http://www.usgs.gov/sparrow/id-point-request/v0_2\""
		+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
		+ "	<model-id>22</model-id>"
		+ "	<match-query>"
		+ "		<reach-name>WESTERN RUN</reach-name>"
		+ "		<meanQHi>123400</meanQHi>"
		+ "		<meanQLo>1</meanQLo>"
		+ "		<catch-area-hi>2345</catch-area-hi>"
		+ "		<catch-area-lo>1</catch-area-lo>"
		+ "	</match-query>"
		+ "	<content>"
		+ "		<adjustments/>"
		+ "	</content>"
		+ "	<response-format>"
		+ "		<mime-operation>XML</mime-operation>"
		+ "	</response-format>"
		+ "</sparrow-reach-request>";

	String sampleResponse1 = "<sparrow-reach-response xmlns=\"http://www.usgs.gov/sparrow/id-response-schema/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" model-id=\"22\">"
		+ "	<status>OK</status>"
		+ " <message></message"
		+ "	<reach>"
		+ "		<id>3541</id>"
		+ "		<name>WESTERN RUN</name>"
		+ "		<meanq>90.6125</meanq>"
		+ "		<catch-area>234</catch-area>"
		+ "		<hucs>"
		+ "			<huc8 id=\"02060003\" name=\"\"/>"
		+ "			<huc6 id=\"020600\" name=\"\"/>"
		+ "			<huc4 id=\"0206\" name=\"\"/>"
		+ "			<huc2 id=\"02\" name=\"\"/>"
		+ "		</hucs>"
		+ "	</reach>"
		+ "</sparrow-reach-response>";

	String sampleRequest2 = "<sparrow-reach-request"
		+ "  xmlns=\"http://www.usgs.gov/sparrow/id-point-request/v0_2\""
		+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
		+ "	<model-id>22</model-id>"
		+ "	<match-query>"
		+ "		<reach-ids>36580 36598 50557</reach-ids>"
		+ "	</match-query>"
		+ "	<content>"
		+ "		<adjustments/>"
		+ "	</content>"
		+ "	<response-format>"
		+ "		<mime-operation>XML</mime-operation>"
		+ "	</response-format>"
		+ "</sparrow-reach-request>";

	String sampleResponse2_36580 = "<reach>"
		+ "		<id>36580</id>"
		+ "		<name>WOLF CR</name>"
		+ "		<meanq>35.3972</meanq>"
		+ "		<catch-area>3</catch-area>"
		+ "		<hucs>"
		+ "			<huc8 id=\"11100203\" name=\"\"/>"
		+ "			<huc6 id=\"111002\" name=\"\"/>"
		+ "			<huc4 id=\"1110\" name=\"\"/>"
		+ "			<huc2 id=\"11\" name=\"\"/>"
		+ "		</hucs>"
		+ "	</reach>";
	String sampleResponse2_50557 = "<reach>"
		+ "		<id>50557</id>"
		+ "		<name>WOLF CR</name>"
		+ "		<meanq>15.692</meanq>"
		+ "		<catch-area>1</catch-area>"
		+ "		<hucs>"
		+ "			<huc8 id=\"17050116\" name=\"\"/>"
		+ "			<huc6 id=\"170501\" name=\"\"/>"
		+ "			<huc4 id=\"1705\" name=\"\"/>"
		+ "			<huc2 id=\"17\" name=\"\"/>"
		+ "		</hucs>"
		+ "	</reach>";
	String sampleResponse2_36598 = "<reach>"
		+ "		<id>36598</id>"
		+ "		<name>WOLF CR</name>"
		+ "		<meanq>56.3822</meanq>"
		+ "		<catch-area>0</catch-area>"
		+ "		<hucs>"
		+ "			<huc8 id=\"11100203\" name=\"\"/>"
		+ "			<huc6 id=\"111002\" name=\"\"/>"
		+ "			<huc4 id=\"1110\" name=\"\"/>"
		+ "			<huc2 id=\"11\" name=\"\"/>"
		+ "		</hucs>"
		+ "	</reach>";

	@Test
	public void testService() throws IOException, SAXException {
		WebRequest request = new PostMethodWebRequest(FINDREACH_SERVICE_URL);
		request.setParameter("xmlreq", sampleRequest1);
		client.sendRequest(request);
		WebResponse response = client.sendRequest(request);
		assertTrue(response != null);

		assertTrue(response.getText() != null);

		assertEquals(stripWhiteSpace(sampleResponse1), stripWhiteSpace(response.getText()));

		assertEquals("text/xml", response.getContentType());
	}

	@Test
	public void testServiceByMultipleIDSearch() throws IOException, SAXException {
		WebRequest request = new PostMethodWebRequest(FINDREACH_SERVICE_URL);

		// sampleRequest2 contains reaches 36580, 36598, 50557
		request.setParameter("xmlreq", sampleRequest2);
		client.sendRequest(request);
		WebResponse response = client.sendRequest(request);
		assertTrue(response != null);

		assertTrue(response.getText() != null);

		// response contains reaches 36580, 36598, 50557 in unspecified order
		String strippedResponse = stripWhiteSpace(response.getText());
		assertTrue("should contain reach 36580", strippedResponse.contains(stripWhiteSpace(sampleResponse2_36580)));
		assertTrue("should contain reach 36598", strippedResponse.contains(stripWhiteSpace(sampleResponse2_36598)));
		assertTrue("should contain reach 50557", strippedResponse.contains(stripWhiteSpace(sampleResponse2_50557)));

		assertEquals("text/xml", response.getContentType());
	}


	public static String stripWhiteSpace(String value) {
		return value.replaceAll("\\s", "");
	}
}
