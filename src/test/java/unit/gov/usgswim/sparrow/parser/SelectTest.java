package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.parser.ComparisonType;
import gov.usgswim.sparrow.parser.DataSeriesType;
import gov.usgswim.sparrow.parser.Select;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class SelectTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public void testParse1() throws XMLStreamException, XMLParseValidationException {
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

		assertEquals(new Integer(1), select.getSource());
		assertEquals("area", select.getDataSeriesPer());
		assertEquals(DataSeriesType.incremental, select.getDataSeries());
		assertEquals("area", select.getAggFunctionPer());
		assertEquals("avg", select.getAggFunction());
		assertEquals("HUC6", select.getPartition());
		assertEquals("rank-desc", select.getAnalyticFunction());
		assertEquals(ComparisonType.percent, select.getNominalComparison());

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(Select.MAIN_ELEMENT_NAME, reader.getLocalName());
	}
	
	public void testParse2() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<select>"
			+ "		<data-series source=\"1\" per=\"area\">incremental</data-series>"
			+ "		<agg-function per=\"area\">avg</agg-function>"
			+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
			+ "		<nominal-comparison type=\"absolute\"/>"
			+ "	</select>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Select select = new Select();
		reader.next();
		select.parse(reader);


		assertEquals(ComparisonType.absolute, select.getNominalComparison());

	}
	
	public void testParse3() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<select>"
			+ "		<data-series source=\"1\" per=\"area\">incremental</data-series>"
			+ "		<agg-function per=\"area\">avg</agg-function>"
			+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
			+ "		<nominal-comparison type=\"none\"/>"
			+ "	</select>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Select select = new Select();
		reader.next();
		select.parse(reader);

		assertEquals(ComparisonType.none, select.getNominalComparison());

	}
	
	public void testParse4() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<select>"
			+ "		<data-series source=\"1\" per=\"area\">incremental</data-series>"
			+ "		<agg-function per=\"area\">avg</agg-function>"
			+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
			+ "	</select>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Select select = new Select();
		reader.next();
		select.parse(reader);

		assertEquals(ComparisonType.none, select.getNominalComparison());

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(Select.MAIN_ELEMENT_NAME, reader.getLocalName());
	}

	public void testMissingDataSeriesSourceParse() throws XMLStreamException, XMLParseValidationException {
		{
			String testRequest = "<select>"
				+ "		<data-series per=\"area\">incremental</data-series>"
				+ "		<agg-function per=\"area\">avg</agg-function>"
				+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
				+ "		<nominal-comparison type=\"percent\"/>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			try {
				select.parse(reader);
			} catch (Exception e) {
				fail("No exception should be thrown for missing data-series@source when data-series value is not \"source_value\"");
			}
		}

		{
			String testRequest = "<select>"
				+ "		<data-series per=\"area\">source_value</data-series>"
				+ "		<agg-function per=\"area\">avg</agg-function>"
				+ "		<analytic-function partition=\"HUC6\">rank-desc</analytic-function>"
				+ "		<nominal-comparison type=\"percent\"/>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			try {
				select.parse(reader);
				fail("A parsing error should have been thrown with no data-series@source when data-series value is \"source_value\"");
			} catch (Exception e) {
				// expected exception thrown
			}
		}

	}

}
