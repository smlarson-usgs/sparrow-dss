package gov.usgswim.sparrow.test.integration;

import static gov.usgswim.sparrow.test.TestHelper.getAttributeValue;
import static gov.usgswim.sparrow.test.TestHelper.getElementValue;
import static gov.usgswim.sparrow.test.TestHelper.pipeDispatch;
import static gov.usgswim.sparrow.test.TestHelper.readToString;
import static gov.usgswim.sparrow.test.TestHelper.setElementValue;
import gov.usgswim.sparrow.LifecycleListener;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.service.idbypoint.IDByPointPipeline;
import gov.usgswim.sparrow.service.idbypoint.IDByPointRequest;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.TestHelper;
import gov.usgswim.sparrow.test.parsers.PredictionContextTest;

import java.io.InputStream;

import junit.framework.TestCase;

public class IDServiceTest extends TestCase {

	private static final String EXPECTED_REACH_NAME_BY_POINT = "WESTERN RUN";

	private static final int EXPECTED_REACH_ID_BY_POINT = 3541;

	private static final String EXPECTED_REACH_NAME_BY_CONTEXT = "POTOMAC R";

	private static final int EXPECTED_REACH_ID_BY_CONTEXT = 4428;
	//3541
	
	LifecycleListener lifecycle = new LifecycleListener();
	
	protected void setUp() throws Exception {
		super.setUp();
		lifecycle.contextInitialized(null, true);
	}

	protected void tearDown() throws Exception {
		super.tearDown();
		lifecycle.contextDestroyed(null, true);
	}
	
	// ============
	// TEST METHODS
	// ============

	public void testModelByPoint() throws Exception {

		IDByPointRequest req = buildIDByPointRequest1();
		String response = pipeDispatch(req, new IDByPointPipeline());

		int reachID = Integer.parseInt( getElementValue(response, "id") );
		String reachName = getElementValue(response, "name");

		assertEquals(EXPECTED_REACH_ID_BY_POINT, reachID);
		assertEquals(EXPECTED_REACH_NAME_BY_POINT, reachName);
		
	}
	
	public void testModelByID() throws Exception {

		IDByPointRequest req = buildIDByPointRequest2();
		String response = pipeDispatch(req, new IDByPointPipeline());
		
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

		assertEquals(3425, reachID);
		assertEquals("Needs to be fixed. currently returns null", reachName);
		
	}
	
	
	public void testHashCode() throws Exception {
		PredictionContext context1 = PredictionContextTest.buildPredictContext1().getPredictionContext();
		PredictionContext context2 = PredictionContextTest.buildPredictContext1().getPredictionContext();
		TestHelper.testHashCode(context1, context2, context1.clone());

		// test IDs
		assertEquals(context1.hashCode(), context1.getId().intValue());
		assertEquals(context2.hashCode(), context2.getId().intValue());
	}
	
	//  TODO add testing for parsing REST requests

	// =====================================
	// STATIC HELPER REQUEST LOADING METHODS
	// =====================================
	public static IDByPointRequest buildIDByPointRequest(String fileName) throws Exception {
		InputStream is = IDServiceTest.class.getResourceAsStream(fileName);
		String xml = readToString(is);
		
		IDByPointPipeline pipe = new IDByPointPipeline();
		return pipe.parse(xml);
	}
	
	public static IDByPointRequest buildIDByPointRequest(String fileName, int contextID) throws Exception {
		InputStream is = IDServiceTest.class.getResourceAsStream(fileName);
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

