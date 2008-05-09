package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.TerminalReaches;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class TerminalReachesTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParseMainUseCase() throws XMLStreamException {
		String testRequest = "<terminal-reaches>"
		+ "	<reach>2345642</reach>"
		+ "	<reach>3425688</reach>"
		+ "	<reach>5235424</reach>"
		+ "</terminal-reaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		TerminalReaches termReaches = new TerminalReaches();
		reader.next();
		termReaches.parse(reader);
		List<Integer> reachIDs = termReaches.getReachIDs();
		assertEquals(3, reachIDs.size());
		assertEquals(Integer.valueOf(2345642), reachIDs.get(0));
		assertEquals(Integer.valueOf(3425688), reachIDs.get(1));
		assertEquals(Integer.valueOf(5235424), reachIDs.get(2));
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(TerminalReaches.MAIN_ELEMENT_NAME, reader.getLocalName());
	}
	
	public void testAcceptsLogicalSet() throws XMLStreamException {
		String testRequest = "<terminal-reaches>"
		+ "	<logical-set/>"
		+ "</terminal-reaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		TerminalReaches termReaches = new TerminalReaches();
		reader.next();
		termReaches.parse(reader);
		// passes if no errors thrown
	}
	
	public void testDoesNotAcceptsBadTag() throws XMLStreamException {
		String testRequest = "<terminal-reaches>"
		+ "	<bad-tag/>"
		+ "</terminal-reaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		TerminalReaches termReaches = new TerminalReaches();
		reader.next();
		try {
			termReaches.parse(reader);
			fail("exception should be thrown before this point");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("unrecognized"));
		}
		

	}
}
