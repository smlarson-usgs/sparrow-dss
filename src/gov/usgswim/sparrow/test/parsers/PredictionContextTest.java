package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.XMLParseValidationException;
import gov.usgswim.sparrow.service.predictcontext.PredictContextPipeline;
import gov.usgswim.sparrow.service.predictcontext.PredictContextRequest;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class PredictionContextTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public void testParseMainUseCase() throws Exception {
		PredictionContext pCon = buildContext();

		assertEquals(Long.valueOf(22), pCon.getModelID());
		assertEquals("HUC8", pCon.getAnalysis().getGroupBy());
		assertEquals(3, pCon.getTerminalReaches().getReachIDs().size());
	}
	
	public void testHashcode() throws Exception {

		PredictionContext pCon1 = buildContext();
		PredictionContext pCon2 = buildContext();
		PredictionContext pCon3 = pCon1.clone();
		
		assertEquals(pCon1.hashCode(), pCon2.hashCode());
		assertEquals(pCon1.hashCode(), pCon3.hashCode());
		
		
		///////
		pCon1 = getTestRequest2().getPredictionContext();
		pCon2 = getTestRequest2().getPredictionContext();
		pCon3 = pCon1.clone();
		
		assertEquals(pCon1.hashCode(), pCon2.hashCode());
		assertEquals(pCon1.hashCode(), pCon3.hashCode());
		
	}
	
	public void testOptionalAdjustmentGroup() throws XMLStreamException, XMLParseValidationException {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = getPredictionContextHeader()
			//+ getAdjustmentGroups()
			+ getAnalysis()
			+ getTerminalReaches()
			+ getAreaOfInterest()
			+ "</prediction-context>";

		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getFullTestRequest()));
		PredictionContext pCon = new PredictionContext();
		reader.next();
		pCon.parse(reader);
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testOptionalTerminalReaches() throws XMLStreamException, XMLParseValidationException {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = getPredictionContextHeader()
			+ getAdjustmentGroups()
			+ getAnalysis()
			//+ getTerminalReaches()
			+ getAreaOfInterest()
			+ "</prediction-context>";

		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getFullTestRequest()));
		PredictionContext pCon = new PredictionContext();
		reader.next();
		pCon.parse(reader);
		// successful parse is passing test
		assertTrue(true);
	}
	
	public void testOptionalAreaOfInterset() throws XMLStreamException, XMLParseValidationException {

		// No error should be thrown on optional adjustmentGroup
		String testRequest = getPredictionContextHeader()
			+ getAdjustmentGroups()
			+ getAnalysis()
			+ getTerminalReaches()
			//+ getAreaOfInterest()
			+ "</prediction-context>";

		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getFullTestRequest()));
		PredictionContext pCon = new PredictionContext();
		reader.next();
		pCon.parse(reader);
		// successful parse is passing test
		assertTrue(true);
	}
	
	// ==============
	// HELPER METHODS
	// ==============
	
	public PredictionContext buildContext() throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getFullTestRequest()));
		PredictionContext pCon = new PredictionContext();
		reader.next();
		pCon.parse(reader);
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(PredictionContext.MAIN_ELEMENT_NAME, reader.getLocalName());
		
		return pCon;
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
		+ "			<nominal-comparison type=\"percent | absolute\"/>"
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
		+ "				<adjustment src=\"2\" coef=\".9\"/>"
		+ "			</reach>"
		+ "			<reach id=\"947839474\">"
		+ "				<adjustment src=\"2\" abs=\"91344\"/>"
		+ "			</reach>"
		+ "		</reach-group>"
		+ "	</adjustment-groups>";
	}

	public PredictContextRequest getTestRequest2() throws Exception {
		InputStream is = getClass().getResourceAsStream("/gov/usgswim/sparrow/test/sample/predict-context-1.xml");
		String xml = readToString(is);
		
		PredictContextPipeline pipe = new PredictContextPipeline();
		return pipe.parse(xml);
	}
	
	public String readToString(InputStream is) {
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);

		StringBuffer sb = new StringBuffer();
		try {
			String line = null;
			while ((line = br.readLine()) != null) {
				sb.append(line + "\n");
			}
		} catch (Exception ex) {
			ex.getMessage();
		} finally {
			try {
				is.close();
			} catch (Exception ex) {
			}
		}
		return sb.toString();
	}
}
