package gov.usgs.webservices.framework.utils;

import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.junit.Test;


public class SmartXMLPropertiesTest {
	static String TEST_XML = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
	+ "<root>"
	+ "	<name>Winnie the Poo</name>"
	+ "	<friends>"
	+ "		<bestFriend id=\"Tigger\">Tigger the tiger</bestFriend>"
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

	public static final String SAMPLE_XML = "<inventory>"
	+ "    <book year=\"2000\">"
	+ "        <title>Snow Crash</title>"
	+ "        <author>Neal Stephenson</author>"
	+ "        <publisher>Spectra</publisher>"
	+ "        <isbn>0553380958</isbn>"
	+ "        <price>14.95</price>"
	+ "    </book>"
	+ "    <book year=\"2005\">"
	+ "        <title>Burning Tower</title>"
	+ "        <author>Larry Niven</author>"
	+ "        <author>Jerry Pournelle</author>"
	+ "        <publisher>Pocket</publisher>"
	+ "        <isbn>0743416910</isbn>"
	+ "        <price>5.99</price>"
	+ "    </book>"
	+ "    <book year=\"1995\">"
	+ "        <title>Zodiac</title>"
	+ "        <author>Neal Stephenson</author>"
	+ "        <publisher>Spectra</publisher>"
	+ "        <isbn>0553573862</isbn>"
	+ "        <price>7.50</price>"
	+ "    </book>"
	+ "    <!-- more books... -->"
	+ "</inventory>";

	@Test
	public void testSimpleChild() throws XMLStreamException, XMLParseValidationException {
		SmartXMLProperties props = new SmartXMLProperties();
		props.parse(TEST_XML);

		System.out.println("== PROPS: ==");
		for (Entry<String, String> entry: props.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		System.out.println();
		System.out.println("== PROPS.get(): ==");
		for (String key: props.keySet()) {
			System.out.println(key + "=" + props.get(key));
		}

		System.out.println();
		System.out.println("== PROPSgetAsFullXMLNode(): ==");
		for (String key: props.keySet()) {
			System.out.println(key + "=" + props.getAsFullXMLNode(key));
		}
	}

//	@Test
//	public void testXPath() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		factory.setNamespaceAware(true); // never forget this!
//		DocumentBuilder builder = factory.newDocumentBuilder();
//		Document doc = builder.parse(new StringBufferInputStream(SAMPLE_XML));
//
//		XPathFactory xpFactory = XPathFactory.newInstance();
//		XPath xpath = xpFactory.newXPath();
//		XPathExpression expr = xpath.compile("//book[author='Neal Stephenson']/title/text()");
//
//		Object result = expr.evaluate(doc, XPathConstants.NODESET);
//
//	    NodeList nodes = (NodeList) result;
//	    for (int i = 0; i < nodes.getLength(); i++) {
//	        System.out.println(nodes.item(i).getNodeValue());
//	    }
//
//	}
//
//	@Test
//	public void testXPath2() throws ParserConfigurationException, SAXException, IOException, XPathExpressionException {
//		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
//		factory.setNamespaceAware(true); // never forget this!
//		DocumentBuilder builder = factory.newDocumentBuilder();
//		Document doc = builder.parse(new StringBufferInputStream(TEST_XML));
//
//		XPathFactory xpFactory = XPathFactory.newInstance();
//		XPath xpath = xpFactory.newXPath();
//		XPathExpression expr = xpath.compile("/*/name/text()");
//
//		Object result = expr.evaluate(doc, XPathConstants.NODESET);
//
//	    NodeList nodes = (NodeList) result;
//	    for (int i = 0; i < nodes.getLength(); i++) {
//	        System.out.println(nodes.item(i).toString());
//	    }
//
//	}
}
