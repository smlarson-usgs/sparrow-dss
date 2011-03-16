package gov.usgswim.sparrow.service.predictservice;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowServiceTestBaseClass;

import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import org.apache.log4j.Level;

public class PredictContextServiceLongRunTest extends SparrowServiceTestBaseClass {

	private static final String SERVICE_URL = "http://localhost:8088/sp_predictcontext";

	@Test
	public void incrementalDeliveredYieldContext() throws Exception {
		log.setLevel(Level.DEBUG);
		String requestText = getXmlAsString(this.getClass(), "req1");
		String expectedResponse = getXmlAsString(this.getClass(), "resp1");
		WebRequest request = new PostMethodWebRequest(SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
//		System.out.println(actualResponse);
//		System.out.println(expectedResponse);
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualResponse);
		assertTrue(similarXMLIgnoreContextId(expectedResponse, actualResponse));
	}
	
	@Test
	public void incrementalDeliveredYieldWithBadReachID() throws Exception {
		String requestText = getXmlAsString(this.getClass(), "req2");
		//String expectedResponse = getXmlAsString(this.getClass(), "resp1_2");
		WebRequest request = new PostMethodWebRequest(SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
//		System.out.println(actualResponse);
		
		assertXpathEvaluatesTo("ERROR", "//*[local-name()='status']", actualResponse);
	}
	

	
}
