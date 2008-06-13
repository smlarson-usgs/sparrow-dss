package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.DefaultGroupParser;
import gov.usgswim.sparrow.parser.ReachGroup;
import gov.usgswim.sparrow.parser.XMLParseValidationException;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Tests everything at the ReachGroup level and below
 * @author eeverman
 *
 */
public class DefaultGroupTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	// ============
	// TEST METHODS
	// ============
	public void testStateHash() throws Exception {

		ReachGroup rg1 = buildDefaultGroup();
		ReachGroup rg2 = buildDefaultGroup();

		assertEquals("equals for distinct copies", rg1.getStateHash(), rg2.getStateHash());
		assertEquals("equals for clones", rg1.getStateHash(), rg2.clone().getStateHash());
	}

	
	public void testHashcode() throws Exception {

		ReachGroup rg1 = buildDefaultGroup();
		ReachGroup rg2 = buildDefaultGroup();
		
		assertNotSame("ReachGroup.hashCode() has not been defined, so using default from Object", rg1.hashCode(), rg2.hashCode());
		
		
		// Should uncomment this if hashCode() overridden
		// TestHelper.testHashCode(rg1, rg2, rg1.clone());

	}
	
	public void testParse1() throws Exception {

		ReachGroup rg = buildDefaultGroup();

		assertEquals("default-group", rg.getParseTarget());
		assertTrue(rg.isEnabled());
		assertTrue(rg.getDescription().contains("Clean' Project") );
		assertTrue(rg.getNotes().contains("based on plant type"));
	}

	public void testParse2() throws Exception {
		String testRequest = "<default-group enabled=\"true\"> <!--  DefaultGroup Object -->"
			+ "	<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
			+ "	<notes>"
			+ "		I initially selected HUC 01746286 and 01746289,"
			+ "		but it looks like there are some others plants that need to be included."
			+ ""
			+ "		As a start, we are proposing a 10% reduction across the board,"
			+ "		but we will tailor this later based on plant type."
			+ "	</notes>"
			+ "	<adjustment src=\"5\" coef=\".9\"/>	<!--  Existing Adjustment Object -->"
			+ "	<adjustment src=\"4\" coef=\".75\"/>"
			+ "	<reach id=\"947839474\">"
			+ "		<adjustment src=\"2\" abs=\"91344\"/>"
			+ "		<adjustment src=\"4\" coef=\".7\"/>"
			+ "	</reach>"
			+ "</default-group>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		DefaultGroupParser rg = new DefaultGroupParser(1);

		reader.next();

		try {
			rg.parse(reader);
			fail("This should have caused an error due to the presence of the reach.");
		} catch (XMLParseValidationException e) {
			//expected error
		}
		assertTrue("successful parse = pass", true);
	}
	
	public void testParseHandling() throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getTestRequest()));
		ReachGroup rg = new DefaultGroupParser(1);
		reader.next();
		rg = rg.parse(reader);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(DefaultGroupParser.MAIN_ELEMENT_NAME, reader.getLocalName());
	}

	// ==============
	// HELPER METHODS
	// ==============
	public ReachGroup buildDefaultGroup() throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getTestRequest()));
		ReachGroup rg = new DefaultGroupParser(1);
		reader.next();
		return rg.parse(reader);
	}

	public String getTestRequest() {
		String testRequest = "<default-group enabled=\"true\"> <!--  DefaultGroup Object -->"
			+ "	<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
			+ "	<notes>"
			+ "		I initially selected HUC 01746286 and 01746289,"
			+ "		but it looks like there are some others plants that need to be included."
			+ ""
			+ "		As a start, we are proposing a 10% reduction across the board,"
			+ "		but we will tailor this later based on plant type."
			+ "	</notes>"
			+ "	<adjustment src=\"5\" coef=\".9\"/>	<!--  Existing Adjustment Object -->"
			+ "	<adjustment src=\"4\" coef=\".75\"/>"
			+ "</default-group>";
		return testRequest;
	}


}
