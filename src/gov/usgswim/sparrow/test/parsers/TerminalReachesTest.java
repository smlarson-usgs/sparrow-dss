package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.Analysis;
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
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParseMainUseCase() throws Exception {

		TerminalReaches termReaches = buildTestInstance(1L);
		
		List<Integer> reachIDs = termReaches.getReachIDs();
		assertEquals(3, reachIDs.size());
		assertEquals(Integer.valueOf(2345642), reachIDs.get(0));
		assertEquals(Integer.valueOf(3425688), reachIDs.get(1));
		assertEquals(Integer.valueOf(5235424), reachIDs.get(2));

	}
	
	public void testAcceptsLogicalSet() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<terminal-reaches>"
		+ "	<logical-set/>"
		+ "</terminal-reaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		TerminalReaches termReaches = new TerminalReaches(1L);
		reader.next();
		termReaches.parse(reader);
		// passes if no errors thrown
	}
	
	public void testDoesNotAcceptsBadTag() throws XMLStreamException, XMLParseValidationException {
		String testRequest = "<terminal-reaches>"
		+ "	<bad-tag/>"
		+ "</terminal-reaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		TerminalReaches termReaches = new TerminalReaches(1L);
		reader.next();
		try {
			termReaches.parse(reader);
			fail("exception should be thrown before this point");
		} catch (RuntimeException e) {
			assertTrue(e.getMessage().contains("unrecognized"));
		}
	}
	
	public void testHashcode() throws Exception {

		TerminalReaches test1 = buildTestInstance(1L);
		TerminalReaches test2 = buildTestInstance(1L);
		
		assertEquals(test1.hashCode(), test2.hashCode());
		assertEquals(test1.getId(), test2.getId());
		assertEquals(test1.getId().intValue(), test2.hashCode());
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
		String testRequest = "<terminal-reaches>"
			+ "	<reach>2345642</reach>"
			+ "	<reach>3425688</reach>"
			+ "	<reach>5235424</reach>"
			+ "</terminal-reaches>";
		return testRequest;
	}
}
