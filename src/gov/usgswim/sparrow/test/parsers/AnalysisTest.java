package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.PredictionContext;
import gov.usgswim.sparrow.parser.ReachGroup;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class AnalysisTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParse1() throws XMLStreamException {
		String testRequest = "<analysis>"
		+ "	<select>"
		+ "		<data-series source=\"1\" per=\"area\">incremental</data-series>"
		+ "		<agg-function per=\"area\">avg</agg-function>"
		+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
		+ "		<nominal-comparison type=\"percent | absolute\"/>"
		+ "	</select>"
		+ "	<limit-to>contributors</limit-to>"
		+ "	<group-by>HUC8</group-by>"
		+ "</analysis>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Analysis anal = new Analysis();
		reader.next();
		anal.parse(reader);

		assertEquals("contributors", anal.getLimitTo());
		assertEquals("HUC8", anal.getGroupBy());
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(Analysis.MAIN_ELEMENT_NAME, reader.getLocalName());
	}

}
