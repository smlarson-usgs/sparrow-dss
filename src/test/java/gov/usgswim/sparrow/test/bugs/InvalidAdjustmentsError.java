package gov.usgswim.sparrow.test.bugs;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import gov.usgswim.sparrow.SparrowServiceTestWithCannedModel50;

import org.apache.log4j.Level;
import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class InvalidAdjustmentsError extends SparrowServiceTestWithCannedModel50 {
	private static final String SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	
	@Test
	public void testModelByPoint() throws Exception {
		log.setLevel(Level.DEBUG);
		
		String requestText = getXmlAsString(this.getClass(), null);
		//String expectedResponse = getXmlAsString(this.getClass(), "resp1");
		WebRequest request = new PostMethodWebRequest(SERVICE_URL);
		request.setParameter("xmlreq", requestText);
		WebResponse response = client.sendRequest(request);
		String actualResponse = response.getText();
		System.out.println(actualResponse);
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", actualResponse);

	}
	

}
