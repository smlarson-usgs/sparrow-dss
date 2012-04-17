package gov.usgswim.sparrow.domain;

import gov.usgswim.sparrow.parser.XMLParseValidationException;

import static org.junit.Assert.*;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

public class NominalComparisonTest {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	@Test
	public void parseNominalComparisonPercent() throws XMLStreamException, XMLParseValidationException {

		XMLStreamReader reader = buildReader("<nominalComparison type=\"percent\"/>");
		NominalComparison comp = new NominalComparison();
		
		comp.parse(reader);

		assertTrue(comp.isValid());
		assertEquals(ComparisonType.percent, comp.comparisonType);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(NominalComparison.MAIN_ELEMENT_NAME, reader.getLocalName());
	}
	
	@Test
	public void parseNominalComparisonAbsolute() throws XMLStreamException, XMLParseValidationException {

		XMLStreamReader reader = buildReader("<nominalComparison type=\"absolute\"/>");
		NominalComparison comp = new NominalComparison();
		
		comp.parse(reader);

		assertTrue(comp.isValid());
		assertEquals(ComparisonType.absolute, comp.comparisonType);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(NominalComparison.MAIN_ELEMENT_NAME, reader.getLocalName());
	}
	
	@Test
	public void parseNominalComparisonPercentAltForm() throws XMLStreamException, XMLParseValidationException {

		XMLStreamReader reader = buildReader("<nominalComparison type=\"percent\"></nominalComparison>");
		NominalComparison comp = new NominalComparison();
		
		comp.parse(reader);

		assertTrue(comp.isValid());
		assertEquals(ComparisonType.percent, comp.comparisonType);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(NominalComparison.MAIN_ELEMENT_NAME, reader.getLocalName());
	}
	
	
	
	protected XMLStreamReader buildReader(String source) throws XMLStreamException {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(source));
		reader.next();
		return reader;
	}
}
