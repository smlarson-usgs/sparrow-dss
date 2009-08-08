package gov.usgswim.sparrow.service.findReachService;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;

import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;


public class FindReachServiceTest {
	String sampleResponse = "<sparrow-reach-response xmlns=\"http://www.usgs.gov/sparrow/id-response-schema/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" model-id=\"22\">"
		+ "	<status>OK</status>"
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



	String sampleRequest = "<sparrow-reach-request"
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
		+ "		<mime-type>XML</mime-type>"
		+ "	</response-format>"
		+ "</sparrow-reach-request>";

	public static final File PROD_WEB_XML = new File("public_html/WEB-INF/web.xml");



	@Test
	public void testService() throws IOException, SAXException {
		ServletRunner sr = new ServletRunner(PROD_WEB_XML);
		ServletUnitClient client = sr.newClient();

		WebRequest request = new PostMethodWebRequest("http://localhost/sp_findReach");
		request.setParameter("xmlreq", sampleRequest);

		client.sendRequest(request);

		WebResponse response = client.sendRequest(request);
		assertTrue(response != null);

		assertTrue(response.getText() != null);
		System.out.println(response.getText());
		System.out.println(stripWhiteSpace(response.getText()));
		System.out.println(sampleResponse);
		System.out.println(stripWhiteSpace(sampleResponse));
		assertEquals(stripWhiteSpace(sampleResponse), stripWhiteSpace(response.getText()));

		assertEquals("text/xml", response.getContentType());
	}


	public static String stripWhiteSpace(String value) {
		return value.replaceAll("\\s", "");
	}
}
