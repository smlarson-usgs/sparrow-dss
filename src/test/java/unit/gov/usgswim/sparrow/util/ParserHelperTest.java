package gov.usgswim.sparrow.util;

import gov.usgswim.sparrow.util.ParserHelper;

import java.io.StringReader;

import javax.xml.XMLConstants;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class ParserHelperTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public final String TEST_REQUEST = "<reach-group enabled=\"true\" name=\"Northern Indiana Plants\"> <!--  ReachGroup Object -->"
		+ "	<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
		+ "	<notes>"
		+ "	</notes>"
		+ "	<!-- Multiple treatments are possible -->"
		+ "	<adjustment src=\"5\" coef=\".9\"/>	<!--  Existing Adjustment Object -->"
		+ "	<adjustment src=\"4\" coef=\".75\"/>"
		+ "	<logical-set>	<!--  LogicalSet Object?  (Hold Off) Used as cache key for a reach collection. -->"
		+ "		<criteria attrib=\"huc8\">01746286</criteria>"
		+ "	</logical-set>"
		+ "	<logical-set>"
		+ "		<criteria attrib=\"huc8\">01746289</criteria>"
		+ "	</logical-set>"
		+ "</reach-group>";

	public void testParseToDesignatedStartTag() throws XMLStreamException {
		XMLStreamReader in = inFact.createXMLStreamReader(new StringReader(TEST_REQUEST));

		ParserHelper.parseToStartTag(in, "notes");
		assertEquals(XMLStreamConstants.START_ELEMENT, in.getEventType());
		assertEquals("notes", in.getLocalName());

		ParserHelper.parseToStartTag(in, "adjustment");
		assertEquals(XMLStreamConstants.START_ELEMENT, in.getEventType());
		assertEquals("adjustment", in.getLocalName());
		assertEquals("5", in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "src"));
		assertEquals(".9", in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "coef"));

		ParserHelper.parseToStartTag(in, "criteria");
		assertEquals(XMLStreamConstants.START_ELEMENT, in.getEventType());
		assertEquals("criteria", in.getLocalName());
		assertEquals("huc8", in.getAttributeValue(XMLConstants.DEFAULT_NS_PREFIX, "attrib"));
	}

	public void testParseToStartTag() throws XMLStreamException {
		XMLStreamReader in = inFact.createXMLStreamReader(new StringReader(TEST_REQUEST));

		ParserHelper.parseToStartTag(in);
		assertEquals(XMLStreamConstants.START_ELEMENT, in.getEventType());
		assertEquals("Should be on first element of stream", "reach-group", in.getLocalName());

		ParserHelper.parseToStartTag(in);
		assertEquals(XMLStreamConstants.START_ELEMENT, in.getEventType());
		assertEquals("Another call should not advanced the stream since we're already on a start element", "reach-group", in.getLocalName());

		in.next();
		ParserHelper.parseToStartTag(in);
		assertEquals(XMLStreamConstants.START_ELEMENT, in.getEventType());
		assertEquals("desc", in.getLocalName());


	}
}
