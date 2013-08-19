package gov.usgswim.sparrow.service.idbypoint;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import gov.usgswim.sparrow.SparrowServiceTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.util.ParserHelper;

import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Level;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.Test;

public class IDServiceIntegrationLongRunTest extends SparrowServiceTestBaseWithDBandCannedModel50 {
	
	@Override
	protected void doOneTimeCustomSetup() throws Exception {
		super.doOneTimeCustomSetup();
		setLogLevel(Level.DEBUG);
	}
	
	// ============
	// TEST METHODS
	// ============
	@Test
	public void testModelByPoint() throws Exception {
		String response = runRequest(getXmlAsString(this.getClass(), "req1"));
		//log.debug("Req 1 response: " + response);
		
		int reachID = Integer.parseInt( getXPathValue("//*[local-name()='id']", response) );
		String reachName = getXPathValue("//*[local-name()='name']", response);

		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
	}
	

	
	@Test
	public void testModelByReachId() throws Exception {
		String expectedResponse = getXmlAsString(this.getClass(), "resp2");
		String actualResponse = runRequest(getXmlAsString(this.getClass(), "req2"));
		//log.debug("Req 2 response: " + actualResponse);
		XMLAssert.assertXMLEqual(expectedResponse, actualResponse);
		
		//Rerun - should be instant b/c we don't ask for attributes.
		long start = System.currentTimeMillis();
		actualResponse = runRequest(getXmlAsString(this.getClass(), "req2"));
		long end = System.currentTimeMillis();
		
		XMLAssert.assertXMLEqual(expectedResponse, actualResponse);
		assertTrue(end - start < 500L);
	}
	
	@Test
	public void testModelByTwoReachIds() throws Exception {
		String expectedResponse = getXmlAsString(this.getClass(), "resp3");
		String actualResponse = runRequest(getXmlAsString(this.getClass(), "req3"));
		//log.debug("Req 3 response: " + actualResponse);
		
		XMLAssert.assertXMLEqual(expectedResponse, actualResponse);
		
		//Rerun - should be instant b/c we don't ask for attributes.
		long start = System.currentTimeMillis();
		actualResponse = runRequest(getXmlAsString(this.getClass(), "req3"));
		long end = System.currentTimeMillis();
		XMLAssert.assertXMLEqual(expectedResponse, actualResponse);
		assertTrue(end - start < 500L);
	}
	
	@Test
	public void testContextByTwoReachIds() throws Exception {

		XMLStreamReader contextReader = getSharedXMLAsReader("predict-context-1.xml");
		ParserHelper.parseToStartTag(contextReader, PredictionContext.MAIN_ELEMENT_NAME);
		PredictionContext pc = PredictionContext.parseStream(contextReader);
		SharedApplication.getInstance().putPredictionContext(pc);
		String id = pc.getId().toString();	//ID of our context
		
		String contextBasedIDReq = 
			getAnyResourceWithSubstitutions("req4.xml", this.getClass(), "context-id", id);
		
		String expectedResponse = getXmlAsString(this.getClass(), "resp4");
		String actualResponse = runRequest(contextBasedIDReq);
		//log.debug("Req 4 response: " + actualResponse);

		assertTrue(similarXMLIgnoreContextId(expectedResponse, actualResponse));
		
		//Rerun - should be instant b/c we don't ask for attributes.
		long start = System.currentTimeMillis();
		actualResponse = runRequest(contextBasedIDReq);
		long end = System.currentTimeMillis();
		assertTrue(similarXMLIgnoreContextId(expectedResponse, actualResponse));
		assertTrue(end - start < 500L);
	}
	
	protected String runRequest(String request) throws Exception {
		IDByPointPipeline pipe = new IDByPointPipeline();
		IDByPointRequest req = pipe.parse(request);
		
		String response = pipeDispatch(req, new IDByPointPipeline());
		
		return response;
	}
	
}

