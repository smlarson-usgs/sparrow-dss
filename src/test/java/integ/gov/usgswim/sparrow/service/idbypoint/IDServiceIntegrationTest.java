package gov.usgswim.sparrow.service.idbypoint;

import static gov.usgswim.sparrow.TestHelper.getAttributeValue;
import static gov.usgswim.sparrow.TestHelper.getElementValue;
import static gov.usgswim.sparrow.TestHelper.pipeDispatch;
import static gov.usgswim.sparrow.TestHelper.readToString;
import static gov.usgswim.sparrow.TestHelper.setElementValue;
import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.TestHelper;
import gov.usgswim.sparrow.action.Action;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.PredictionContextTest;
import gov.usgswim.sparrow.service.SharedApplication;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.util.ParserHelper;

import java.io.InputStream;

import javax.xml.stream.XMLStreamReader;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.custommonkey.xmlunit.XMLAssert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class IDServiceIntegrationTest extends SparrowDBTest {

	@BeforeClass
	public static void localInit() {
		log.setLevel(Level.DEBUG	);
		Logger.getLogger(IDByPointService.class).setLevel(Level.TRACE);
	}
	
	// ============
	// TEST METHODS
	// ============

	//TODO:  The spatial indexes on widev need to be fixed, at which point
	//This test will (should) work.  Modify to make this test look like the others.
	@Test
	public void testModelByPoint() throws Exception {

		String response = runRequest(getXmlAsString(this.getClass(), "req1"));
		//log.debug("Req 1 response: " + response);
		
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
	}
	

	
	@Test
	public void testModelByReachId() throws Exception {
		String expectedResponse = getXmlAsString(this.getClass(), "resp2");
		String actualResponse = runRequest(getXmlAsString(this.getClass(), "req2"));

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

		//log.debug(expectedResponse);
		//log.debug(actualResponse);
		XMLAssert.assertXMLEqual(expectedResponse, actualResponse);
		
		//Rerun - should be instant b/c we don't ask for attributes.
		long start = System.currentTimeMillis();
		actualResponse = runRequest(contextBasedIDReq);
		long end = System.currentTimeMillis();
		XMLAssert.assertXMLEqual(expectedResponse, actualResponse);
		assertTrue(end - start < 500L);
	}
	
	protected String runRequest(String request) throws Exception {
		IDByPointPipeline pipe = new IDByPointPipeline();
		IDByPointRequest req = pipe.parse(request);
		
		String response = pipeDispatch(req, new IDByPointPipeline());

		return response;
	}
	
}

