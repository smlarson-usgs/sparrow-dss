package gov.usgswim.sparrow.test.parsers;

import static gov.usgswim.sparrow.test.TestHelper.getAttributeValue;
import static gov.usgswim.sparrow.test.TestHelper.pipeDispatch;
import static gov.usgswim.sparrow.test.TestHelper.readToString;
import gov.usgswim.sparrow.parser.ComparisonType;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;
import gov.usgswim.sparrow.test.TestHelper;
import gov.usgswim.sparrow.test.integration.IDServiceTest;
import gov.usgswim.sparrow.test.integration.LogicalAdjustmentTest;

import java.io.InputStream;
import java.io.StringReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;
public class PredictionContextTest extends TestCase {
	
	// ================
	// STATIC CONSTANTS
	// ================
	public static final String PRED_CONTEXT_1 = "/gov/usgswim/sparrow/test/sample/predict-context-1.xml";
	public static final String PRED_CONTEXT_2 = "/gov/usgswim/sparrow/test/sample/predict-context-2.xml";
	public static final String PRED_CONTEXT_3 = "/gov/usgswim/sparrow/test/sample/predict-context-3.xml";
	public static final String PRED_CONTEXT_4 = "/gov/usgswim/sparrow/test/sample/predict-context-4.xml";
	public static final String PRED_CONTEXT_5 = "/gov/usgswim/sparrow/test/sample/predict-context-5.xml";
	public static final String PRED_CONTEXT_BUG_1 = "/gov/usgswim/sparrow/test/sample/predict-context-bug_1.xml";
	
	public static final int PRED_CONTEXT_1_ID = 1411567658;
	public static final int PRED_CONTEXT_2_ID = 4609629;
	public static final int PRED_CONTEXT_3_ID = -1926160079;
	public static final int PRED_CONTEXT_4_ID = -1504305838;
	
	public static String contextIDRegex = "context-id=['\"]([-0-9]+)['\"]";
	public static Pattern patt = Pattern.compile(contextIDRegex);

	public static int extractContextIDFromPredictionContextResponse(String pcResponse) {
		Matcher m = patt.matcher(pcResponse);
		boolean isFound = m.find();
		if (isFound) {
			return Integer.valueOf(m.group(1));
		} else {
			System.err.println("Unable to extract context-id from prediction context response for test " + LogicalAdjustmentTest.class.getSimpleName());
			return 0;
		}
		
	}
	
	protected static XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	// ============
	// TEST METHODS
	// ============
	public void testParseMainUseCase() throws Exception {
		PredictionContext pCon = buildContext(getFullTestRequest());

		assertEquals(Long.valueOf(22), pCon.getModelID());
		assertEquals("HUC8", pCon.getAnalysis().getGroupBy());
		assertEquals(3, pCon.getTerminalReaches().getReachIDs().size());
		assertEquals(ComparisonType.absolute, pCon.getAnalysis().getSelect().getNominalComparison());
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
			PredictionContext predCtxt1 = buildContext(getFullTestRequest());
			PredictionContext prdCtxt2 = buildContext(getFullTestRequest());
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
		String testRequest = getPredictionContextHeader()
			//+ getAdjustmentGroups()
			+ getAnalysis()
			+ getTerminalReaches()
			+ getAreaOfInterest()
			+ "</prediction-context>";

		buildContext(testRequest);
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testOptionalTerminalReaches() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = getPredictionContextHeader()
			+ getAdjustmentGroups()
			+ getAnalysis()
			//+ getTerminalReaches()
			+ getAreaOfInterest()
			+ "</prediction-context>";

		buildContext(testRequest);
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testOptionalAreaOfInterset() throws Exception {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = getPredictionContextHeader()
			+ getAdjustmentGroups()
			+ getAnalysis()
			+ getTerminalReaches()
			//+ getAreaOfInterest()
			+ "</prediction-context>";

		buildContext(testRequest);
		
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testParserHandling() throws XMLStreamException, XMLParseValidationException {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getFullTestRequest()));
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
	
	public static PredictionContext buildContext(String pcRequestXML) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(pcRequestXML));
		PredictionContext pCon = new PredictionContext();
		reader.next();
		return pCon.parse(reader);
	}
	
	public String getFullTestRequest() {
		String testRequest = getPredictionContextHeader()
			+ getAdjustmentGroups()
			+ getAnalysis()
			+ getTerminalReaches()
			+ getAreaOfInterest()
			+ "</prediction-context>";
		return testRequest;
	}
	
	private String getAnalysis() {
		return "	<analysis>"
		+ "		<select>"
		+ "			<data-series source=\"1\" per=\"area\">incremental</data-series>"
		+ "			<agg-function per=\"area\">avg</agg-function>"
		+ "			<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
		+ "			<nominal-comparison type=\"absolute\"/>"
		+ "		</select>"
		+ "		<limit-to>contributors | terminals | area-of-interest</limit-to>"
		+ "		<group-by>HUC8</group-by>"
		+ "	</analysis>";
	}

	private String getPredictionContextHeader() {
		return "<prediction-context "
		+ "  xmlns=\"http://www.usgs.gov/sparrow/prediction-schema/v0_2\" "
		+ "	xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
		+ "	model-id=\"22\">";
	}

	private String getAreaOfInterest() {
		return "	<area-of-interest>"
		+ "		<logical-set/>	"
		+ "	</area-of-interest>";
		
	}

	private String getTerminalReaches() {
		return "	<terminal-reaches>"
		+ "		<reach>2345642</reach>"
		+ "		<reach>3425688</reach>"
		+ "		<reach>5235424</reach>"
		+ "		<logical-set/>"
		+ "	</terminal-reaches>";
	}

	private String getAdjustmentGroups() {
		return "	<adjustment-groups conflicts=\"accumulate\">"
		+ "		<reach-group enabled=\"true\" name=\"Northern Indiana Plants\">"
		+ "			<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
		+ "			<notes>"
		+ "				I initially selected HUC 01746286 and 01746289,"
		+ "				but it looks like there are some others plants that need to be included."
		+ "				As a start, we are proposing a 10% reduction across the board,"
		+ "				but we will tailor this later based on plant type."
		+ "			</notes>"
		+ "			<adjustment src=\"5\" coef=\".9\"/>"
		+ "			<adjustment src=\"4\" coef=\".75\"/>"
		+ "			<logical-set>"
		+ "				<criteria attrib=\"huc8\">01746286</criteria>"
		+ "			</logical-set>"
		+ "			<logical-set>"
		+ "				<criteria attrib=\"huc8\">01746289</criteria>"
		+ "			</logical-set>"
		+ "		</reach-group>"
		+ "		<reach-group enabled=\"false\" name=\"Southern Indiana Fields\">"
		+ "			<desc>Fields in Southern Indiana</desc>"
		+ "			<notes>"
		+ "				The Farmer's Alminac says corn planting will be up 20% this year,"
		+ "				which will roughly result in a 5% increase in the aggrecultural source."
		+ "				This is an estimate so I'm leaving it out of the runs created	for the EPA."
		+ "			</notes>"
		+ "			<adjustment src=\"1\" coef=\"1.05\"/>"
		+ "			<logical-set>"
		+ "				<criteria attrib=\"reach\" relation=\"upstream\">8346289</criteria>"
		+ "			</logical-set>"
		+ "			<logical-set>"
		+ "				<criteria attrib=\"reach\" relation=\"upstream\">9374562</criteria>"
		+ "			</logical-set>"
		+ "		</reach-group>"
		+ "		<reach-group enabled=\"true\" name=\"Illinois\">"
		+ "			<desc>The entire state of Illinois</desc>"
		+ "			<notes>The Urban source for Illinois is predicted is to increase 20%.</notes>"
		+ "			<adjustment src=\"2\" coef=\"1.2\"/>"
		+ "			<logical-set>"
		+ "				<criteria attrib=\"state-code\">il</criteria>"
		+ "			</logical-set>"
		+ "		</reach-group>"
		+ "		<reach-group enabled=\"true\" name=\"Illinois\">"
		+ "			<desc>Wisconsin River Plants</desc>"
		+ "			<notes>"
		+ "				We know of 3 plants on the Wisconsin River which have announced improved"
		+ "				BPM implementations."
		+ "			</notes>"
		+ "			<adjustment src=\"2\" coef=\".75\"/>"
		+ "			<reach id=\"483947453\">"
		+ "				<adjustment src=\"2\" abs=\".9\"/>"
		+ "			</reach>"
		+ "			<reach id=\"947839474\">"
		+ "				<adjustment src=\"2\" abs=\"91344\"/>"
		+ "			</reach>"
		+ "		</reach-group>"
		+ "	</adjustment-groups>";
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
		InputStream is = IDServiceTest.class.getResourceAsStream(PredictionContextTest.PRED_CONTEXT_2);
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
