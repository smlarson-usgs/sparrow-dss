package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.Analysis;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.Select;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class SelectTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParse1() throws XMLStreamException {
		String testRequest = "<select>"
		+ "		<data-series source=\"1\" per=\"area\">incremental</data-series>"
		+ "		<agg-function per=\"area\">avg</agg-function>"
		+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
		+ "		<nominal-comparison type=\"percent\"/>"
		+ "	</select>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Select select = new Select();
		reader.next();
		select.parse(reader);

		assertEquals("1", select.getSource());
		assertEquals("area", select.getDataSeriesPer());
		assertEquals(DataSeriesType.incremental, select.getDataSeries());
		assertEquals("area", select.getAggFunctionPer());
		assertEquals("avg", select.getAggFunction());
		assertEquals("HUC6", select.getPartition());
		assertEquals("rank-desc", select.getAnalyticFunction());
		assertEquals("percent", select.getType());
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(Select.MAIN_ELEMENT_NAME, reader.getLocalName());
	}

}
