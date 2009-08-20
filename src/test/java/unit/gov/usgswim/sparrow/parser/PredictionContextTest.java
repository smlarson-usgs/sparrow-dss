package gov.usgswim.sparrow.parser;

import static gov.usgswim.sparrow.test.TestHelper.getAttributeValue;
import static gov.usgswim.sparrow.test.TestHelper.pipeDispatch;
import static gov.usgswim.sparrow.test.TestHelper.readToString;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.TestHelper;

import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Unit tests for the {@code PredictionContext} class.  The unit tests in this
 * class test parsing functionality that is specific to the
 * {@code PredictionContext} class and, hence, only need to ensure that a few
 * basic items are handled properly.  The majority of the parsing functionality
 * is handled by child-level classes who receive XML fragments from
 * {@code PredictionContext} by way of delegation.  It is in those classes'
 * tests that the more complicated testing occurs (e.g. adjustment calculation).
 */
public class PredictionContextTest extends TestCase {
    
    /** Valid xml string represention of the prediction context. */
    public static final String VALID_FRAGMENT = ""
        + "<prediction-context "
        + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        + "  model-id=\"22\">"
        + AdjustmentGroupsTest.VALID_FRAGMENT
        + AnalysisTest.VALID_FRAGMENT
        + TerminalReachesTest.VALID_FRAGMENT
        + AreaOfInterestTest.VALID_FRAGMENT
        + "</prediction-context>"
        ;
    
    /** Used to create XMLStreamReaders from XML strings. */
    protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	// ================
	// STATIC CONSTANTS
	// ================
	public static final String PRED_CONTEXT_1 = "/gov/usgswim/sparrow/test/sample/predict-context-1.xml";
	public static final String PRED_CONTEXT_2 = "/gov/usgswim/sparrow/test/sample/predict-context-2.xml";
	public static final String PRED_CONTEXT_3 = "/gov/usgswim/sparrow/test/sample/predict-context-3.xml";
	public static final String PRED_CONTEXT_4 = "/gov/usgswim/sparrow/test/sample/predict-context-4.xml";
	public static final String PRED_CONTEXT_5 = "/gov/usgswim/sparrow/test/sample/predict-context-5.xml";
	public static final String PRED_CONTEXT_BUG_1 = "/gov/usgswim/sparrow/test/sample/predict-context-bug_1.xml";
	
	public static final int PRED_CONTEXT_1_ID = 1143562390;
	public static final int PRED_CONTEXT_2_ID = -792246649;
	public static final int PRED_CONTEXT_3_ID = 923106886;
	public static final int PRED_CONTEXT_4_ID = -398183923;
	
	public static String contextIDRegex = "context-id=['\"]([-0-9]+)['\"]";
	public static Pattern patt = Pattern.compile(contextIDRegex);

	public static int extractContextIDFromPredictionContextResponse(String pcResponse) {
		Matcher m = patt.matcher(pcResponse);
		boolean isFound = m.find();
		if (isFound) {
			return Integer.valueOf(m.group(1));
		}
		System.err.println("Unable to extract context-id from prediction context response for test " + PredictionContextTest.class.getSimpleName());
		return 0;
		
	}
	
	// ============
	// TEST METHODS
	// ============
	public void testParseMainUseCase() throws Exception {
		PredictionContext pCon = buildContext(VALID_FRAGMENT);

		assertEquals(Long.valueOf(22), pCon.getModelID());
	}
	
	public void testPredictContext1() throws Exception {
		PredictContextRequest contextReq = buildPredictContext1();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		assertEquals("PredictionContext parsing has likely changed.", PRED_CONTEXT_1_ID, Integer.parseInt(getAttributeValue(response, "context-id")));
	}
	
	public void testPredictContext2() throws Exception {
		PredictContextRequest contextReq = buildPredictContext2();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		assertEquals("PredictionContext parsing has likely changed.", PRED_CONTEXT_2_ID, Integer.parseInt(getAttributeValue(response, "context-id")));
	}

	public void testPredictContext3() throws Exception {
		PredictContextRequest contextReq = buildPredictContext3();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		assertEquals("PredictionContext parsing has likely changed.", PRED_CONTEXT_3_ID, Integer.parseInt(getAttributeValue(response, "context-id")));
	}
	
	public void testPredictContext4() throws Exception {
		PredictContextRequest contextReq = buildPredictContext4();	//Build a context from a canned file
		
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		assertEquals("PredictionContext parsing has likely changed.", PRED_CONTEXT_4_ID, Integer.parseInt(getAttributeValue(response, "context-id")));
	}
	
	public void testHashcode() throws Exception {
		{
			PredictionContext predCtxt1 = buildContext(VALID_FRAGMENT);
			PredictionContext prdCtxt2 = buildContext(VALID_FRAGMENT);
			PredictionContext predCtxt3 = predCtxt1.clone();

			TestHelper.testHashCode(predCtxt1, prdCtxt2, predCtxt1.clone());

			// test IDs
			assertEquals(predCtxt1.hashCode(), predCtxt1.getId().intValue());
			assertEquals(prdCtxt2.hashCode(), prdCtxt2.getId().intValue());
			assertEquals(predCtxt3.hashCode(), predCtxt3.getId().intValue());
		}

		{
			// test prediction-context-1
			PredictionContext predCtxt1 = buildPredictContext1().getPredictionContext();
			PredictionContext prdCtxt2 = buildPredictContext1().getPredictionContext();
			PredictionContext predCtxt3 = predCtxt1.clone();


			TestHelper.testHashCode(predCtxt1, prdCtxt2, predCtxt1.clone());

			// test IDs
			assertEquals(predCtxt1.hashCode(), predCtxt1.getId().intValue());
			assertEquals(prdCtxt2.hashCode(), prdCtxt2.getId().intValue());
			assertEquals(predCtxt3.hashCode(), predCtxt3.getId().intValue());
		}

	}
	
	public void testOptionalAdjustmentGroup() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = ""
	        + "<prediction-context "
	        + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
	        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
	        + "  model-id=\"22\">"
			//+ getAdjustmentGroups()
			+ AnalysisTest.VALID_FRAGMENT
			+ TerminalReachesTest.VALID_FRAGMENT
			+ AreaOfInterestTest.VALID_FRAGMENT
			+ "</prediction-context>";

		buildContext(testRequest);
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testOptionalTerminalReaches() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = ""
            + "<prediction-context "
            + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "  model-id=\"22\">"
			+ AdjustmentGroupsTest.VALID_FRAGMENT
			+ AnalysisTest.VALID_FRAGMENT
			//+ TerminalReachesTest.VALID_FRAGMENT
			+ AreaOfInterestTest.VALID_FRAGMENT
			+ "</prediction-context>";

		buildContext(testRequest);
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testOptionalAreaOfInterset() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = ""
            + "<prediction-context "
            + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "  model-id=\"22\">"
			+ AdjustmentGroupsTest.VALID_FRAGMENT
			+ AnalysisTest.VALID_FRAGMENT
			+ TerminalReachesTest.VALID_FRAGMENT
			//+ AreaOfInterestTest.VALID_FRAGMENT
			+ "</prediction-context>";

		buildContext(testRequest);
		
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testParserHandling() throws XMLStreamException, XMLParseValidationException {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(VALID_FRAGMENT));
		PredictionContext pCon = new PredictionContext();
		reader.next();
		pCon.parse(reader);
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(PredictionContext.MAIN_ELEMENT_NAME, reader.getLocalName());
	}
	
	// ==============
	// HELPER METHODS
	// ==============
	
	public PredictionContext buildContext(String pcRequestXML) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(pcRequestXML));
		PredictionContext pCon = new PredictionContext();
		reader.next();
		return pCon.parse(reader);
	}
	
	public static PredictContextRequest buildPredictContext4() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PRED_CONTEXT_4);
		String xml = TestHelper.readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	public static PredictContextRequest buildPredictContext3() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PRED_CONTEXT_3);
		String xml = TestHelper.readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	public static PredictContextRequest buildPredictContext2() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PredictionContextTest.PRED_CONTEXT_2);
		String xml = readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	public static PredictContextRequest buildPredictContext1() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PRED_CONTEXT_1);
		String xml = TestHelper.readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	
	public static PredictContextRequest buildPredictContext5() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PRED_CONTEXT_5);
		String xml = TestHelper.readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
}
