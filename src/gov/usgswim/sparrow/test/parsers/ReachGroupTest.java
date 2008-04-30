package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.ReachGroup;

import java.io.StringReader;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class ReachGroupTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParse1() throws XMLStreamException {
		String testRequest = "<reach-group enabled=\"true\" name=\"Northern Indiana Plants\"> <!--  ReachGroup Object -->"
		+ "	<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
		+ "	<notes>"
		+ "		I initially selected HUC 01746286 and 01746289,"
		+ "		but it looks like there are some others plants that need to be included."
		+ ""
		+ "		As a start, we are proposing a 10% reduction across the board,"
		+ "		but we will tailor this later based on plant type."
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
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup();
		reader.next();
		rg.parse(reader);

		assertTrue(rg.isEnabled());
		assertEquals("Northern Indiana Plants", rg.getName());
		assertTrue(rg.getDescription().contains("Clean' Project") );
		assertTrue(rg.getNotes().contains("based on plant type"));
	}
	
	public void testParse2() throws XMLStreamException {
		String testRequest = "<reach-group enabled=\"false\" name=\"Southern Indiana Fields\">"
		+ "	<desc>Fields in Southern Indiana</desc>"
		+ "	<notes>"
		+ "		The Farmer's Alminac says corn planting will be up 20% this year,"
		+ "		which will roughly result in a 5% increase in the aggrecultural source."
		+ "		This is an estimate so I'm leaving it out of the runs created	for the EPA."
		+ "	</notes>"
		+ "	<adjustment src=\"1\" coef=\"1.05\"/>"
		+ "	<logical-set>"
		+ "		<criteria attrib=\"reach\" relation=\"upstream\">8346289</criteria>"
		+ "	</logical-set>"
		+ "	<logical-set>"
		+ "		<criteria attrib=\"reach\" relation=\"upstream\">9374562</criteria>"
		+ "	</logical-set>"
		+ "</reach-group>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup();
		reader.next();
		rg.parse(reader);

		assertFalse(rg.isEnabled());
		assertEquals("Southern Indiana Fields", rg.getName());
		assertEquals("Fields in Southern Indiana", rg.getDescription());
		assertTrue(rg.getNotes().contains("for the EPA"));
	}
	
	public void testParse3() throws XMLStreamException {
		String testRequest = "<reach-group enabled=\"true\" name=\"Illinois\">"
		+ "	<desc>The entire state of Illinois</desc>"
		+ "	<notes>The Urban source for Illinois is predicted is to increase 20%.</notes>"
		+ "	<adjustment src=\"2\" coef=\"1.2\"/>"
		+ "	<logical-set>"
		+ "		<criteria attrib=\"state-code\">il</criteria>"
		+ "	</logical-set>"
		+ "</reach-group>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup();
		reader.next();
		rg.parse(reader);

		assertTrue(rg.isEnabled());
		assertEquals("Illinois", rg.getName());
		assertEquals("The entire state of Illinois", rg.getDescription());
		assertTrue(rg.getNotes().contains("to increase 20%."));
	}
	
	public void testParse4() throws XMLStreamException {
		String testRequest = "<reach-group enabled=\"true\" name=\"Illinois\">"
			+ "	<desc>Wisconsin River Plants</desc>"
			+ "	<notes>"
			+ "		We know of 3 plants on the Wisconsin River which have announced improved"
			+ "		BPM implementations."
			+ "	</notes>"
			+ "	<adjustment src=\"2\" coef=\".75\"/>"
			+ "	<reach id=\"483947453\">"
			+ "		<adjustment src=\"2\" coef=\".9\"/>"
			+ "	</reach>"
			+ "	<reach id=\"947839474\">"
			+ "		<adjustment src=\"2\" abs=\"91344\"/>"
			+ "	</reach>"
			+ "</reach-group>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup();
		reader.next();
		rg.parse(reader);

		assertTrue(rg.isEnabled());
		assertEquals("Illinois", rg.getName());
		assertEquals("Wisconsin River Plants", rg.getDescription());
		assertTrue(rg.getNotes().contains("River which have"));
	}
	

}
