package gov.usgswim.sparrow.util;

import gov.usgs.webservices.framework.utils.SmartXMLProperties;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;


public class SmartXMLPropertiesTest {
	static String TEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	+ "<root>"
	+ "	<name>Winnie the Poo</name>"
	+ "	<friends>"
	+ "		<friend id=\"Tigger\">Tigger the tiger</friend>"
	+ "		<friend id=\"Piglet\">Piglet the pig</friend>"
	+ "		<friend id=\"ChristopherRobin\">Christopher Robin<basedOn>Christopher Robin Milne, son of A. A. Milne</basedOn>"
	+ "		</friend>"
	+ "		<friend id=\"Eeyore\">Eeyore the Donkey</friend>"
	+ "	</friends>"
	+ "	<Creator>"
	+ "		<name>A. A. Milne</name>"
	+ "		<lifetime>"
	+ "			<born>18 January 1882</born>"
	+ "			<died>31 January 1956</died>"
	+ "		</lifetime>"
	+ "	</Creator>"
	+ "	<eats>"
	+ "		<food id=\"honey\">Pooh's favorite</food>"
	+ "	</eats>"
	+ "</root>";

	@Test
	public void testSimpleChild() throws XMLStreamException, XMLParseValidationException {
		SmartXMLProperties props = new SmartXMLProperties();
		props.parse(TEST_XML);


	}
}
