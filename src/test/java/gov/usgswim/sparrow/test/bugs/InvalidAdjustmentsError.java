package gov.usgswim.sparrow.test.bugs;

import static org.custommonkey.xmlunit.XMLAssert.assertXpathEvaluatesTo;
import gov.usgswim.sparrow.SparrowServiceTestWithCannedModel50;

import org.apache.log4j.Level;
import org.junit.Test;

import com.meterware.httpunit.PostMethodWebRequest;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;

public class InvalidAdjustmentsError extends SparrowServiceTestWithCannedModel50 {
	private static final String CONTEXT_SERVICE_URL = "http://localhost:8088/sp_predictcontext";
	private static final String ID_SERVICE_URL = "http://localhost:8088/sp_idpoint";
	
	@Test
	public void testModelByPoint() throws Exception {
		log.setLevel(Level.DEBUG);
		
		String requestText = getXmlAsString(this.getClass(), "context");
		//String expectedResponse = getXmlAsString(this.getClass(), "resp1");
		WebRequest contextRequest = new PostMethodWebRequest(CONTEXT_SERVICE_URL);
		contextRequest.setParameter("xmlreq", requestText);
		WebResponse contextResponse = client.sendRequest(contextRequest);
		String contextTextResponse = contextResponse.getText();
		
		System.out.println(contextTextResponse);
		Integer contextId = getContextIdFromContext(contextTextResponse);
		
		
		String idReq = getAnyResourceWithSubstitutions("id.xml", this.getClass(), "context_id", contextId);
		System.out.println(idReq);
		WebRequest idRequest = new PostMethodWebRequest(ID_SERVICE_URL);
		idRequest.setParameter("xmlreq", idReq);
		WebResponse idResponse = client.sendRequest(idRequest);
		String idResponseText = idResponse.getText();
		
		System.out.println(idResponseText);
		
		
		assertXpathEvaluatesTo("OK", "//*[local-name()='status']", contextTextResponse);

	}
	

}
