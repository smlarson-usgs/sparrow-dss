package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.TestHelper;
import gov.usgswim.sparrow.parser.TerminalReaches;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class TerminalReachesTest extends TestCase {

    /** Valid xml string represention of the terminal reaches. */
    public static final String VALID_FRAGMENT = ""
        + "<terminalReaches>"
        + "  <reach id='2345642'></reach>"
        + "  <reach id='3425688'></reach>"
        + "  <reach id='5235424'></reach>"
        + "  <logicalSet/>"
        + "</terminalReaches>"
        ;

    /** Used to create XMLStreamReaders from XML strings. */
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public void testParseMainUseCase() throws Exception {

		TerminalReaches termReaches = buildTestInstance(1L);

		List<Long> reachIDs = termReaches.getReachIDs();
		assertEquals(3, reachIDs.size());
		assertEquals(Long.valueOf(2345642), reachIDs.get(0));
		assertEquals(Long.valueOf(3425688), reachIDs.get(1));
		assertEquals(Long.valueOf(5235424), reachIDs.get(2));

	}

	public void testAcceptsLogicalSet() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<terminalReaches>"
		+ "	<logicalSet/>"
		+ "</terminalReaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		TerminalReaches termReaches = new TerminalReaches(1L);
		reader.next();
		termReaches.parse(reader);
		// passes if no errors thrown
	}

	public void testDoesNotAcceptsBadTag() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<terminalReaches>"
		+ "	<bad-tag/>"
		+ "</terminalReaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		TerminalReaches termReaches = new TerminalReaches(1L);
		reader.next();
		try {
			termReaches.parse(reader);
			fail("exception should be thrown before this point");
		} catch (XMLParseValidationException e) {
			assertTrue(e.getMessage().contains("unrecognized"));
		}
	}

	public void testHashcode() throws Exception {

		TerminalReaches term1 = buildTestInstance(1L);
		TerminalReaches term2 = buildTestInstance(1L);

		TestHelper.testHashCode(term1, term2);

		// test IDs
		assertEquals(term1.hashCode(), term1.getId().intValue());
		assertEquals(term2.hashCode(), term2.getId().intValue());

	}


	@SuppressWarnings("static-access")
  public TerminalReaches buildTestInstance(Long modelID) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getTestRequest()));
		TerminalReaches test = new TerminalReaches(modelID);
		reader.next();
		test = test.parse(reader);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(test.MAIN_ELEMENT_NAME, reader.getLocalName());

		return test;
	}

	public String getTestRequest() {
		String testRequest = "<terminalReaches>"
			+ "	<reach id='2345642'></reach>"
			+ "	<reach id='3425688'></reach>"
			+ "	<reach id='5235424'></reach>"
			+ "</terminalReaches>";
		return testRequest;
	}
}
