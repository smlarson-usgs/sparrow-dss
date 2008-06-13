package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.test.TestHelper;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class AnalysisTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParse1() throws Exception {

		Analysis anal = buildTestInstance();

		assertEquals("contributors", anal.getLimitTo());
		assertEquals("HUC8", anal.getGroupBy());

	}
	
	public void testHashcode() throws Exception {

		Analysis analysis1 = buildTestInstance();
		Analysis analysis2 = buildTestInstance();
		TestHelper.testHashCode(analysis1, analysis2, analysis2.clone());

		// test IDs
		assertEquals(analysis1.hashCode(), analysis1.getId().intValue());
		assertEquals(analysis2.hashCode(), analysis2.getId().intValue());
	}
	
	
	@SuppressWarnings("static-access")
  public Analysis buildTestInstance() throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getTestRequest()));
		Analysis test = new Analysis();
		reader.next();
		test = test.parse(reader);
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(test.MAIN_ELEMENT_NAME, reader.getLocalName());
		
		return test;
	}
	
	public String getTestRequest() {
		String testRequest = "<analysis>"
			+ "	<select>"
			+ "		<data-series source=\"1\" per=\"area\">incremental</data-series>"
			+ "		<agg-function per=\"area\">avg</agg-function>"
			+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
			+ "		<nominal-comparison type=\"percent\"/>"
			+ "	</select>"
			+ "	<limit-to>contributors</limit-to>"
			+ "	<group-by>HUC8</group-by>"
			+ "</analysis>";
		return testRequest;
	}

}
