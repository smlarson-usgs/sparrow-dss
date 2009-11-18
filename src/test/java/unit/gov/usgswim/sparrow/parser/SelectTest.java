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
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static junit.framework.Assert.assertNull;

public class SelectTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public void testParse1() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<select>"
			+ "		<dataSeries source=\"1\" per=\"area\">incremental</dataSeries>"
			+ "		<analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
			+ "	</select>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Select select = new Select();
		reader.next();
		select.parse(reader);

		assertEquals(new Integer(1), select.getSource());
		assertEquals("area", select.getDataSeriesPer());
		assertEquals(DataSeriesType.incremental, select.getDataSeries());
		assertEquals("HUC6", select.getPartition());
		assertEquals("rank-desc", select.getAnalyticFunction());

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(Select.MAIN_ELEMENT_NAME, reader.getLocalName());
	}

	public void testParse2() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<select>"
			+ "		<dataSeries>incremental</dataSeries>"
			+ "		<analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
			+ "	</select>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Select select = new Select();
		reader.next();
		select.parse(reader);


		assertNull(select.getSource());
		assertNull(select.getDataSeriesPer());

	}

	public void testParse4() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<select>"
			+ "		<dataSeries source=\"1\" per=\"area\">incremental</dataSeries>"
			+ "		<analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
			+ "	</select>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		Select select = new Select();
		reader.next();
		select.parse(reader);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(Select.MAIN_ELEMENT_NAME, reader.getLocalName());
	}

	public void testMissingDataSeriesSourceParse() throws XMLStreamException, XMLParseValidationException {
		{
			String testRequest = "<select>"
				+ "		<dataSeries per=\"area\">incremental</dataSeries>"
				+ "		<analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			try {
				select.parse(reader);
			} catch (Exception e) {
				fail("No exception should be thrown for missing dataSeries@source when dataSeries value is not \"source_value\"");
			}
		}

		{
			String testRequest = "<select>"
				+ "		<dataSeries per=\"area\">source_value</dataSeries>"
				+ "		<analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			try {
				select.parse(reader);
				fail("A parsing error should have been thrown with no dataSeries@source when dataSeries value is \"source_value\"");
			} catch (Exception e) {
				// expected exception thrown
			}
		}

	}


	public void testErrorEstimateSeries() throws XMLStreamException, XMLParseValidationException {
		{
			String testRequest = "<select>"
				+ "		<dataSeries>total_std_error_estimate</dataSeries>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			select.parse(reader);
			
			assertEquals(DataSeriesType.total_std_error_estimate, select.getDataSeries());
			assertNull(select.getSource());
			assertTrue(select.isValid());
		}
		
		{
			String testRequest = "<select>"
				+ "		<dataSeries source=\"1\">total_std_error_estimate</dataSeries>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			select.parse(reader);
			
			assertEquals(DataSeriesType.total_std_error_estimate, select.getDataSeries());
			assertEquals(Integer.valueOf(1), select.getSource());
			assertTrue(select.isValid());
		}
		
		{
			String testRequest = "<select>"
				+ "		<dataSeries source=\"1\">total_std_error_estimate</dataSeries>"
				+ "		<analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			
			
			try {
				select.parse(reader);
				fail("This was supposed to throw a parse exception - analytics not allowed.");
			} catch (XMLParseValidationException e) {
				//expected
			}
			
			assertEquals(DataSeriesType.total_std_error_estimate, select.getDataSeries());
			assertFalse(select.isValid());
		}

		{
			String testRequest = "<select>"
				+ "		<dataSeries>incremental_std_error_estimate</dataSeries>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			select.parse(reader);
			
			assertEquals(DataSeriesType.incremental_std_error_estimate, select.getDataSeries());
			assertTrue(select.isValid());
		}
		
		{
			String testRequest = "<select>"
				+ "		<dataSeries source=\"1\">incremental_std_error_estimate</dataSeries>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			select.parse(reader);
			
			assertEquals(DataSeriesType.incremental_std_error_estimate, select.getDataSeries());
			assertEquals(Integer.valueOf(1), select.getSource());
			assertTrue(select.isValid());
		}
		
		{
			String testRequest = "<select>"
				+ "		<dataSeries source=\"1\">incremental_std_error_estimate</dataSeries>"
				+ "		<analyticFunction partition=\"HUC6\">rank-desc</analyticFunction>"
				+ "	</select>";
			XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
			Select select = new Select();
			reader.next();
			
			try {
				select.parse(reader);
				fail("This was supposed to throw a parse exception.");
			} catch (XMLParseValidationException e) {
				//expected
			}
			
			assertEquals(DataSeriesType.incremental_std_error_estimate, select.getDataSeries());
			assertFalse(select.isValid());
		}

	}
}
