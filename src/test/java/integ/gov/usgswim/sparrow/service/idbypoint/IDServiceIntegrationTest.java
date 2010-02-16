package gov.usgswim.sparrow.service.idbypoint;

import static gov.usgswim.sparrow.test.TestHelper.getAttributeValue;
import static gov.usgswim.sparrow.test.TestHelper.getElementValue;
import static gov.usgswim.sparrow.test.TestHelper.pipeDispatch;
import static gov.usgswim.sparrow.test.TestHelper.readToString;
import static gov.usgswim.sparrow.test.TestHelper.setElementValue;
import static org.junit.Assert.assertEquals;
import gov.usgswim.sparrow.SparrowDBTest;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.PredictionContextTest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.TestHelper;

import java.io.InputStream;

import org.apache.log4j.Level;
import org.junit.Test;

public class IDServiceIntegrationTest extends SparrowDBTest {

	private static String req1 = "<sparrow-id-request xmlns=\"http://www.usgs.gov/sparrow/id-point-request/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><model-id>50</model-id><point lat=\"33.743\" long=\"-88.563\" /><content><adjustments /><attributes /></content><response-format><mime-type>xml</mime-type></response-format></sparrow-id-request>";
	private static String req2 = "<sparrow-id-request xmlns=\"http://www.usgs.gov/sparrow/id-point-request/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><model-id>50</model-id><reach id=\"9190\"/><content><adjustments /><attributes /></content><response-format><mime-type>xml</mime-type></response-format></sparrow-id-request>";
	private static String req3 = "<sparrow-id-request xmlns=\"http://www.usgs.gov/sparrow/id-point-request/v0_2\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><model-id>50</model-id><reach id=\"9190\"/><reach id=\"6887\"/><content><adjustments /><attributes /></content><response-format><mime-type>xml</mime-type></response-format></sparrow-id-request>";

	
	
	
	
	
	private static final String EXPECTED_REACH_NAME_BY_POINT = "WESTERN RUN";

	private static final int EXPECTED_REACH_ID_BY_POINT = 3541;

	private static final String EXPECTED_REACH_NAME_BY_CONTEXT = "POTOMAC R";

	private static final int EXPECTED_REACH_ID_BY_CONTEXT = 4428;
	//3541
	

	
	// ============
	// TEST METHODS
	// ============

	@Test
	public void testModelByPoint() throws Exception {

		//IDByPointRequest req = buildIDByPointRequest1();
		

		
		String response = runRequest(req1);
		log.debug("Req 1 response: " + response);
		
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
		
		//Rerun - should be instant
		long start = System.currentTimeMillis();
		response = runRequest(req1);
		long end = System.currentTimeMillis();
		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
		//assertTrue(end - start < 10L);
	}
	

	
	@Test
	public void testModelByReachId() throws Exception {
		log.setLevel(Level.DEBUG);
		
		String response = runRequest(req2);
		log.debug("Req 2 response: " + response);
		
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
		
		//Rerun - should be instant, or not, if not cached.
		long start = System.currentTimeMillis();
		response = runRequest(req2);
		long end = System.currentTimeMillis();
		long totalTime = end - start;
		log.debug("Total time was: " + totalTime);
		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
		//assertTrue(end - start < 10L);
	}
	
	@Test
	public void testModelByTwoReachIds() throws Exception {
		log.setLevel(Level.DEBUG);
		
		String response = runRequest(req3);
		log.debug("Req 3 response: " + response);
		
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
		
		//Rerun - should be instant, or not, if not cached.
		long start = System.currentTimeMillis();
		response = runRequest(req2);
		long end = System.currentTimeMillis();
		long totalTime = end - start;
		log.debug("Total time was: " + totalTime);
		assertEquals(9190, reachID);
		assertEquals("HARRY KETTLE CR", reachName);
		//assertTrue(end - start < 10L);
	}
	
	public void testModelByID() throws Exception {

		IDByPointRequest req = buildIDByPointRequest2();
		String response = pipeDispatch(req, new IDByPointPipeline());
		
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");
		
		assertEquals(EXPECTED_REACH_ID_BY_CONTEXT, reachID);
		assertEquals(EXPECTED_REACH_NAME_BY_CONTEXT, reachName);
		
	}
	
	

	
	public void testContextByID() throws Exception {

		PredictContextRequest contextReq = PredictionContextTest.buildPredictContext2();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		String contextID = getAttributeValue(response, "context-id");
		
		IDByPointRequest req = buildIDByPointRequest4(Integer.parseInt(contextID));
		
		response = pipeDispatch(req, new IDByPointPipeline());
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(EXPECTED_REACH_ID_BY_CONTEXT, reachID);
		assertEquals(EXPECTED_REACH_NAME_BY_CONTEXT, reachName);
		
	}
	
	public void testContextByPoint() throws Exception {

		PredictContextRequest contextReq = PredictionContextTest.buildPredictContext2();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		String contextID = getAttributeValue(response, "context-id");

		IDByPointRequest req = buildIDByPointRequest3(Integer.parseInt(contextID));
		response = pipeDispatch(req, new IDByPointPipeline());
		
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(EXPECTED_REACH_ID_BY_POINT, reachID);
		assertEquals(EXPECTED_REACH_NAME_BY_POINT, reachName);
		
	}
	
	/**
	 * Trying to reproduce a bug.
	 * @throws Exception
	 */
	public void testContextByPointBug1() throws Exception {

		PredictContextRequest contextReq = buildPredictContextBug1();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		String contextID = TestHelper.getAttributeValue(response, "context-id");

		
		IDByPointRequest req = buildIDByPointRequestBug1(Integer.parseInt(contextID));
		response = pipeDispatch(req, new IDByPointPipeline());
		
		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(3535, reachID);
		assertEquals("GUNPOWDER FALLS", reachName);
		
	}
	
	
	public void testHashCode() throws Exception {
		PredictionContext context1 = PredictionContextTest.buildPredictContext1().getPredictionContext();
		PredictionContext context2 = PredictionContextTest.buildPredictContext1().getPredictionContext();
		TestHelper.testHashCode(context1, context2, context1.clone());

		// test IDs
		assertEquals(context1.hashCode(), context1.getId().intValue());
		assertEquals(context2.hashCode(), context2.getId().intValue());
	}
	
	protected String runRequest(String request) throws Exception {
		IDByPointPipeline pipe = new IDByPointPipeline();
		IDByPointRequest req = pipe.parse(request);
		
		String response = pipeDispatch(req, new IDByPointPipeline());

		return response;
	}
	
	//  TODO add testing for parsing REST requests

	// =====================================
	// STATIC HELPER REQUEST LOADING METHODS
	// =====================================
	public static IDByPointRequest buildIDByPointRequest(String fileName) throws Exception {
		InputStream is = IDServiceIntegrationTest.class.getResourceAsStream(fileName);
		String xml = readToString(is);
		
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
	public static IDByPointRequest buildIDByPointRequest(String fileName, int contextID) throws Exception {
		InputStream is = IDServiceIntegrationTest.class.getResourceAsStream(fileName);
		String xml = readToString(is);
		xml = setElementValue(xml, "context-id", Integer.toString(contextID));
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
	public static IDByPointRequest buildIDByPointRequest1() throws Exception {
		return buildIDByPointRequest("/gov/usgswim/sparrow/test/sample/id_request_1.xml");
	}
	
	public static IDByPointRequest buildIDByPointRequest2() throws Exception {
		return buildIDByPointRequest("/gov/usgswim/sparrow/test/sample/id_request_2.xml");
	}
	
	public static IDByPointRequest buildIDByPointRequest3(int contextID) throws Exception {
		return buildIDByPointRequest("/gov/usgswim/sparrow/test/sample/id_request_3.xml", contextID);
	}
	
	public static IDByPointRequest buildIDByPointRequest4(int contextID) throws Exception {
		return buildIDByPointRequest("/gov/usgswim/sparrow/test/sample/id_request_4.xml", contextID);
	}
	
	public static IDByPointRequest buildIDByPointRequestBug1(int contextID) throws Exception {
		return buildIDByPointRequest("/gov/usgswim/sparrow/test/sample/id_request_bug_1.xml", contextID);
	}
	
	public static PredictContextRequest buildPredictContextBug1() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PredictionContextTest.PRED_CONTEXT_BUG_1);
		String xml = readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	

}
