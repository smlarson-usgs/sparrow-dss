package gov.usgswim.sparrow.parser;

import static org.junit.Assert.*;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.domain.TerminalReaches;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;

import junit.framework.TestCase;

public class TerminalReachesTest {

	/** Valid xml string represention of the terminal reaches. */
	public static final String VALID_FRAGMENT = "" + "<terminalReaches>"
			+ "  <reach id='2345642'></reach>"
			+ "  <reach id='3425688'></reach>"
			+ "  <reach id='5235424'></reach>" + "  <logicalSet/>"
			+ "</terminalReaches>";

	/** Used to create XMLStreamReaders from XML strings. */
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	@Test
	public void parseMainUseCase() throws Exception {

		TerminalReaches termReaches = buildTestInstance(1L);

		List<Long> reachIDs = termReaches.getReachIDs();
		assertEquals(3, reachIDs.size());
		assertEquals(Long.valueOf(2345642), reachIDs.get(0));
		assertEquals(Long.valueOf(3425688), reachIDs.get(1));
		assertEquals(Long.valueOf(5235424), reachIDs.get(2));

	}

	@Test
	public void acceptsLogicalSet() throws XMLStreamException,
			XMLParseValidationException {
		String testRequest = "<terminalReaches>" + "	<logicalSet/>"
				+ "</terminalReaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(
				testRequest));
		TerminalReaches termReaches = new TerminalReaches(1L);
		reader.next();
		termReaches.parse(reader);
		// passes if no errors thrown
	}

	@Test
	public void doesNotAcceptsBadTag() throws XMLStreamException,
			XMLParseValidationException {
		String testRequest = "<terminalReaches>" + "	<bad-tag/>"
				+ "</terminalReaches>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(
				testRequest));
		TerminalReaches termReaches = new TerminalReaches(1L);
		reader.next();
		try {
			termReaches.parse(reader);
			fail("exception should be thrown before this point");
		} catch (XMLParseValidationException e) {
			assertTrue(e.getMessage().contains("unrecognized"));
		}
	}

	@Test
	public void verifyHashcode() throws Exception {

		TerminalReaches term1 = buildTestInstance(1L);
		TerminalReaches term2 = buildTestInstance(1L);

		SparrowTestBase.testHashCode(term1, term2);

		// test IDs
		assertEquals(term1.hashCode(), term1.getId().intValue());
		assertEquals(term2.hashCode(), term2.getId().intValue());

	}

	@Test
	public void verifyContainsMethod() throws Exception {

		ArrayList<Long> reachIds = new ArrayList<Long>();
		reachIds.add(1L);
		reachIds.add(2L);
		TerminalReaches term1 = new TerminalReaches(1L, reachIds);

		assertTrue(term1.contains(1L));
		assertTrue(term1.contains(2L));

		assertFalse(term1.contains(3L));
		assertFalse(term1.contains(0L));
		assertFalse(term1.contains(-1L));
	}

	@Test
	public void passedInReachIdsShouldBeDetached() throws Exception {

		ArrayList<Long> reachIds = new ArrayList<Long>();
		reachIds.add(1L);
		reachIds.add(2L);
		TerminalReaches term1 = new TerminalReaches(1L, reachIds);

		assertTrue(term1.contains(1L));
		assertTrue(term1.contains(2L));
		assertEquals(2, term1.size());

		// Remove reach 2 from list - should still be in Terms
		reachIds.remove(1);

		assertTrue(term1.contains(1L));
		assertTrue(term1.contains(2L));

		// Remove reach 1 from list - should still be in Terms
		reachIds.remove(0);
		assertEquals(0, reachIds.size());

		assertTrue(term1.contains(1L));
		assertTrue(term1.contains(2L));
		assertEquals(2, term1.size());
	}

	@Test
	public void getReachIDsShouldBeDetached() throws Exception {

		ArrayList<Long> reachIds = new ArrayList<Long>();
		reachIds.add(1L);
		reachIds.add(2L);
		TerminalReaches term1 = new TerminalReaches(1L, reachIds);

		List<Long> copiedIds = term1.getReachIDs();
		copiedIds.remove(1);
		copiedIds.remove(0);
		assertEquals(0, copiedIds.size()); // copied list is empty

		// Term Reaches still contains both reaches
		assertTrue(term1.contains(1L));
		assertTrue(term1.contains(2L));
		assertEquals(2, term1.size());
	}

	@Test
	public void asSetShouldBeDetached() throws Exception {

		ArrayList<Long> reachIds = new ArrayList<Long>();
		reachIds.add(1L);
		reachIds.add(2L);
		TerminalReaches term1 = new TerminalReaches(1L, reachIds);

		Set<Long> copiedIds = term1.asSet();
		copiedIds.remove(1L);
		copiedIds.remove(2L);
		assertEquals(0, copiedIds.size()); // copied list is empty

		// Term Reaches still contains both reaches
		assertTrue(term1.contains(1L));
		assertTrue(term1.contains(2L));
		assertEquals(2, term1.size());
	}

	@SuppressWarnings("static-access")
	public TerminalReaches buildTestInstance(Long modelID) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(
				getTestRequest()));
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
				+ "	<reach id='5235424'></reach>" + "</terminalReaches>";
		return testRequest;
	}
}
