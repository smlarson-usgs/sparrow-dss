package gov.usgswim.sparrow.test.service.predict;

import static org.junit.Assert.fail;
import gov.usgswim.sparrow.service.SparrowServiceTest;

import java.io.IOException;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.xml.sax.SAXException;

import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.WebRequest;

public class IndividualReachPredictServiceTest extends SparrowServiceTest{

	private static final String INDIV_PREDICT_SERVICE_URL = "http://localhost:8088/sp_indivReachPredict";

	@BeforeClass
	public static void setupClass() throws IOException, SAXException {
		setupHTTPUnitTest();
	}

	@AfterClass
	public static void teardownClass() {
		teardownHTTPUnitTest();
	}

	@Test
	public void testDoGetHttpServletRequestHttpServletResponse() {
		fail("Not yet implemented");
	}

	@Test
	public void testDoPostHttpServletRequestHttpServletResponse() {
		WebRequest request = new GetMethodWebRequest(INDIV_PREDICT_SERVICE_URL);

		// sampleRequest2 contains reaches 36580, 36598, 50557
//		request.setParameter("context-id", sampleRequest2);
//		request.setParameter("model", sampleRequest2);
//		request.setParameter("reachID", sampleRequest2);
//		client.sendRequest(request);
//		WebResponse response = client.sendRequest(request);
//		assertTrue(response != null);
//
//		assertTrue(response.getText() != null);
//
//		// response contains reaches 36580, 36598, 50557 in unspecified order
//		String strippedResponse = stripWhiteSpace(response.getText());
//		assertTrue("should contain reach 36580", strippedResponse.contains(stripWhiteSpace(sampleResponse2_36580)));
//		assertTrue("should contain reach 36598", strippedResponse.contains(stripWhiteSpace(sampleResponse2_36598)));
//		assertTrue("should contain reach 50557", strippedResponse.contains(stripWhiteSpace(sampleResponse2_50557)));

//		assertEquals("text/xml", response.getContentType());
	}

}
