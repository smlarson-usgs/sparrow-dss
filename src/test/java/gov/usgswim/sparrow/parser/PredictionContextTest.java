package gov.usgswim.sparrow.parser;

import static gov.usgswim.sparrow.SparrowUnitTestBaseClass.readToString;
import gov.usgswim.sparrow.SparrowUnitTestBaseClass;
import gov.usgswim.sparrow.domain.DataSeriesType;
import gov.usgswim.sparrow.domain.PredictionContext;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

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

    /** Valid xml string representation of the prediction context. */
    public static final String VALID_FRAGMENT = ""
        + "<PredictionContext "
        + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
        + "  model-id=\"22\">"
        + AdjustmentGroupsTest.VALID_FRAGMENT
        + AnalysisTest.VALID_ADV_FRAGMENT_1
        + TerminalReachesTest.VALID_FRAGMENT
        + AreaOfInterestTest.VALID_FRAGMENT
        + "</PredictionContext>"
        ;
    
    public static final String VALID_FRAGMENT_BUG_1 = ""
    + "<PredictionContext "
    + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
    + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
    + "  model-id=\"22\">"
    + AdjustmentGroupsTest.VALID_FRAGMENT
    + AnalysisTest.VALID_ADV_FRAGMENT_BUG_1
    + TerminalReachesTest.VALID_FRAGMENT
    + AreaOfInterestTest.VALID_FRAGMENT
    + "</PredictionContext>"
    ;

    /** Used to create XMLStreamReaders from XML strings. */
    protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public static final Long TEST_MODEL_ID = 22L;

	// ================
	// STATIC CONSTANTS
	// ================
	public static final String PRED_CONTEXT_1 = "/gov/usgswim/sparrow/test/sample/predict-context-1.xml";
	public static final String PRED_CONTEXT_2 = "/gov/usgswim/sparrow/test/sample/predict-context-2.xml";
	public static final String PRED_CONTEXT_3 = "/gov/usgswim/sparrow/test/sample/predict-context-3.xml";
	public static final String PRED_CONTEXT_4 = "/gov/usgswim/sparrow/test/sample/predict-context-4.xml";
	public static final String PRED_CONTEXT_5 = "/gov/usgswim/sparrow/test/sample/predict-context-5.xml";
	public static final String PRED_CONTEXT_BUG_1 = "/gov/usgswim/sparrow/test/sample/predict-context-bug_1.xml";

	public static final int PRED_CONTEXT_1_ID = -585422559;
	public static final int PRED_CONTEXT_2_ID = -959878888;
	public static final int PRED_CONTEXT_3_ID = -763073805;
	public static final int PRED_CONTEXT_4_ID = -1906830649;


	
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

	/*
	 * These tests are now all broken and not really unit tests as written
	 * b/c the register a prediction context, which now requires the associated
	 * result to be calculated, which requires the prediction data.
	 *   //TODO:  Fix these tests to run w/ canned prediction data
	public void testPredictContext1() throws Exception {
		PredictContextRequest contextReq = buildPredictContext1();	//Build a context from a canned file

		assertTrue(contextReq.getPredictionContext().isValid());
		String response = pipeDispatch(contextReq, new PredictContextPipeline());
		assertEquals("PredictionContext parsing has likely changed.", PRED_CONTEXT_1_ID, Integer.parseInt(getAttributeValue(response, "context-id")));
	}

	public void testPredictContext2() throws Exception {
		PredictContextRequest contextReq = buildPredictContext2();	//Build a context from a canned file

		assertTrue(contextReq.getPredictionContext().isValid());
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
	*/
	
	public void testBug1() throws Exception {
		PredictionContext context = buildContext(VALID_FRAGMENT_BUG_1);
		
		assertEquals(DataSeriesType.incremental_std_error_estimate, 
				context.getAnalysis().getDataSeries());
		assertTrue(context.isValid());
		
	}

	public void testHashcode() throws Exception {
		{
			PredictionContext predCtxt1 = buildContext(VALID_FRAGMENT);
			PredictionContext prdCtxt2 = buildContext(VALID_FRAGMENT);
			PredictionContext predCtxt3 = predCtxt1.clone();

			SparrowUnitTestBaseClass.testHashCode(predCtxt1, prdCtxt2, predCtxt1.clone());

			// test IDs
			assertEquals(predCtxt1.hashCode(), predCtxt1.getId().intValue());
			assertEquals(prdCtxt2.hashCode(), prdCtxt2.getId().intValue());
			assertEquals(predCtxt3.hashCode(), predCtxt3.getId().intValue());
		}

		{
			// test PredictionContext-1
			PredictionContext predCtxt1 = buildPredictContext1().getPredictionContext();
			PredictionContext prdCtxt2 = buildPredictContext1().getPredictionContext();
			PredictionContext predCtxt3 = predCtxt1.clone();


			SparrowUnitTestBaseClass.testHashCode(predCtxt1, prdCtxt2, predCtxt1.clone());

			// test IDs
			assertEquals(predCtxt1.hashCode(), predCtxt1.getId().intValue());
			assertEquals(prdCtxt2.hashCode(), prdCtxt2.getId().intValue());
			assertEquals(predCtxt3.hashCode(), predCtxt3.getId().intValue());
		}

	}

	public void testOptionalAdjustmentGroup() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = ""
	        + "<PredictionContext "
	        + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
	        + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
	        + "  model-id=\"22\">"
			//+ getAdjustmentGroups()
			+ AnalysisTest.VALID_ADV_FRAGMENT_1
			+ TerminalReachesTest.VALID_FRAGMENT
			+ AreaOfInterestTest.VALID_FRAGMENT
			+ "</PredictionContext>";

		buildContext(testRequest);
		// successful parse is passing test
		assertTrue(true);
	}

	public void testOptionalTerminalReaches() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = ""
            + "<PredictionContext "
            + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "  model-id=\"22\">"
			+ AdjustmentGroupsTest.VALID_FRAGMENT
			+ AnalysisTest.VALID_ADV_FRAGMENT_1
			//+ TerminalReachesTest.VALID_FRAGMENT
			+ AreaOfInterestTest.VALID_FRAGMENT
			+ "</PredictionContext>";

		buildContext(testRequest);
		// successful parse is passing test
		assertTrue(true);
	}

	public void testOptionalAreaOfInterset() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = ""
            + "<PredictionContext "
            + "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\""
            + "  xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
            + "  model-id=\"22\">"
			+ AdjustmentGroupsTest.VALID_FRAGMENT
			+ AnalysisTest.VALID_ADV_FRAGMENT_1
			+ TerminalReachesTest.VALID_FRAGMENT
			//+ AreaOfInterestTest.VALID_FRAGMENT
			+ "</PredictionContext>";

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
		String xml = SparrowUnitTestBaseClass.readToString(is);

		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	public static PredictContextRequest buildPredictContext3() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PRED_CONTEXT_3);
		String xml = SparrowUnitTestBaseClass.readToString(is);

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
		String xml = SparrowUnitTestBaseClass.readToString(is);

		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}

	public static PredictContextRequest buildPredictContext5() throws Exception {
		InputStream is = PredictionContextTest.class.getResourceAsStream(PRED_CONTEXT_5);
		String xml = SparrowUnitTestBaseClass.readToString(is);

		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
}
