package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.*;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Tests everything at the ReachGroup level and below
 * @author eeverman
 *
 */
public class DefaultGroupTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParse1() throws Exception {
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
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		DefaultGroup rg = new DefaultGroup();
		
		reader.next();
		rg.parse(reader);

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
		DefaultGroup rg = new DefaultGroup();
		
		reader.next();
		
		try {
	    rg.parse(reader);
	    fail("This should have caused an error due to the presence of the reach.");
    } catch (XMLParseValidationException e) {
	    //expected error
    }
	}
	

	

}
