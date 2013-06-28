package gov.usgs.webservices.framework.utils;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.junit.BeforeClass;
import org.junit.Test;


public class SmartXMLPropertiesTest {

	static SmartXMLProperties props = new SmartXMLProperties();
	// Complete list of simple property keys for the test document
	static final String[] simplePropKeys = {"name",
		"friends.Tigger",
		"friends.Piglet",
		"friends.ChristopherRobin",
		"friends.Eeyore",
		"Creator",
		"eats.honey"};

	// Complete list of simple property values for the test document
	static final String[] simplePropValues = {"Winnie the Poo",
		"Tigger the tiger",
		"Piglet the pig",
		"Christopher Robin<basedOn>Christopher Robin Milne, son of A. A. Milne</basedOn>",
		"Eeyore the Donkey",
		"<name>A. A. Milne</name><lifetime><born>18 January 1882</born><died>31 January 1956</died></lifetime>",
		"Pooh's favorite"
	};

	// Complete list of node values for the test document
	static final String[] nodeValues= {"<name>Winnie the Poo</name>",
		"<bestFriend id=\"Tigger\">Tigger the tiger</bestFriend>",
		"<friend id=\"Piglet\">Piglet the pig</friend>",
		"<friend id=\"ChristopherRobin\">Christopher Robin<basedOn>Christopher Robin Milne, son of A. A. Milne</basedOn></friend>",
		"<friend id=\"Eeyore\">Eeyore the Donkey</friend>",
		"<Creator><name>A. A. Milne</name><lifetime><born>18 January 1882</born><died>31 January 1956</died></lifetime></Creator>",
		"<food id=\"honey\">Pooh's favorite</food>"
	};

	// Complete list of list keys for the document
	static final String [] listKeySet = {"friends", "eats"};


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

//	public static final String SAMPLE_XML = "<inventory>"
//	+ "    <book year=\"2000\">"
//	+ "        <title>Snow Crash</title>"
//	+ "        <author>Neal Stephenson</author>"
//	+ "        <publisher>Spectra</publisher>"
//	+ "        <isbn>0553380958</isbn>"
//	+ "        <price>14.95</price>"
//	+ "    </book>"
//	+ "    <book year=\"2005\">"
//	+ "        <title>Burning Tower</title>"
//	+ "        <author>Larry Niven</author>"
//	+ "        <author>Jerry Pournelle</author>"
//	+ "        <publisher>Pocket</publisher>"
//	+ "        <isbn>0743416910</isbn>"
//	+ "        <price>5.99</price>"
//	+ "    </book>"
//	+ "    <book year=\"1995\">"
//	+ "        <title>Zodiac</title>"
//	+ "        <author>Neal Stephenson</author>"
//	+ "        <publisher>Spectra</publisher>"
//	+ "        <isbn>0553573862</isbn>"
//	+ "        <price>7.50</price>"
//	+ "    </book>"
//	+ "    <!-- more books... -->"
//	+ "</inventory>";

	@BeforeClass
	public static void setup() throws XMLStreamException, XMLParseValidationException {
		props = new SmartXMLProperties();
		props.parse(TEST_XML);
		assertEquals("Checking setup: number of values should = number of keys", simplePropValues.length, simplePropKeys.length);
		assertEquals("Checking setup: number of nodes should = number of values", nodeValues.length, simplePropKeys.length);

	}
	@Test
	public void testKeySet() {
		Set<String> keys = props.keySet();
		assertEquals("Should be 7 keys total", simplePropKeys.length, keys.size());
		assertTrue("Should contain specified property keys", props.keySet().containsAll(Arrays.asList(simplePropKeys)));
		assertArrayEquals("Keys should be in this order", keys.toArray(new String[0]), simplePropKeys);
	}

	@Test
	public void testKeyValues() {
		Set<Entry<String, String>> entries = props.entrySet();
		Iterator<Entry<String, String>> iter = entries.iterator();
		for (int i=0; i<simplePropValues.length; i++) {
			Entry<String, String> entry = iter.next();
			assertEquals(simplePropValues[i], entry.getValue());
		}
	}

	@Test
	public void testGetReturnsSameAsEntrySet() {
		Set<Entry<String, String>> entries = props.entrySet();
		Iterator<Entry<String, String>> iter = entries.iterator();
		for (int i=0; i<simplePropValues.length; i++) {
			Entry<String, String> entry = iter.next();
			assertEquals(entry.getValue(), props.get(entry.getKey()));
		}
	}

	@Test
	public void testGetFullXMLNodeOnSimpleProperties() {
		Set<Entry<String, String>> entries = props.entrySet();
		Iterator<Entry<String, String>> iter = entries.iterator();
		for (int i=0; i<nodeValues.length; i++) {
			Entry<String, String> entry = iter.next();
			assertEquals(nodeValues[i], props.getAsFullXMLNode(entry.getKey()));
		}
	}

	@Test
	public void testListKeySet() {
		Set<String> listKeys = props.listKeySet();
		String[] retrievedKeys = listKeys.toArray(new String[0]);
		assertArrayEquals(listKeySet, retrievedKeys);
	}

	@Test
	public void testGetListAsMap() {
		for (String key: props.keySet()) {
			if (SmartXMLProperties.isCompoundKey(key)) {
				String listKey = SmartXMLProperties.parseListKey(key);
				String itemKey = SmartXMLProperties.parseItemKey(key);

				Map<String, String> list = props.getListAsMap(listKey);
				assertEquals("should match for " + key, props.getAsFullXMLNode(key), list.get(itemKey));
			}
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
