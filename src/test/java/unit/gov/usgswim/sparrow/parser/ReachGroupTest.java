package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.parser.Adjustment;
import gov.usgswim.sparrow.parser.LogicalSet;
import gov.usgswim.sparrow.parser.ReachElement;
import gov.usgswim.sparrow.parser.ReachGroup;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Tests everything at the ReachGroup level and below
 * @author eeverman
 */
public class ReachGroupTest extends TestCase {

    /** Valid xml string represention of a reach group. */
    public static final String VALID_FRAGMENT = ""
        + "<reachGroup enabled=\"true\" name=\"Northern Indiana Plants\">"
        + "  <desc>description</desc>"
        + "  <notes>notes</notes>"
        + "  <adjustment src=\"5\" coef=\"0.9\" />"
        + "  <reach id=\"12345\" />"
        + "  <logicalSet>"
        + "    <criteria attrib=\"huc2\">10</criteria>"
        + "  </logicalSet>"
        + "</reachGroup>";
        ;

    /** Used to create XMLStreamReaders from XML strings. */
    protected XMLInputFactory inFact = XMLInputFactory.newInstance();

    public static String testRequest1 = "<reachGroup enabled=\"true\" name=\"Northern Indiana Plants\"> <!--  ReachGroup Object -->"
		+ "	<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
		+ "	<notes>"
		+ "		I initially selected HUC 10040202"
		+ "		but it looks like there are some others plants that need to be included."
		+ ""
		+ "		As a start, we are proposing a 10% reduction across the board,"
		+ "		but we will tailor this later based on plant type."
		+ "	</notes>"
		+ "	<!-- Multiple treatments are possible -->"
		+ "	<adjustment src=\"5\" coef=\".9\"/>	<!--  Existing Adjustment Object -->"
		+ "	<adjustment src=\"4\" coef=\".75\"/>"
		+ "	<logicalSet>	<!--  LogicalSet Object?  (Hold Off) Used as cache key for a reach collection. -->"
		+ "		<criteria attrib=\"huc8\">10040202</criteria>"
		+ "	</logicalSet>"
		+ "	<logicalSet>"
		+ "		<criteria attrib=\"huc6\">101701</criteria>"
		+ "	</logicalSet>"
		+ "	<logicalSet>"
		+ "		<criteria attrib=\"huc4\">1705</criteria>"
		+ "	</logicalSet>"
		+ "	<logicalSet>"
		+ "		<criteria attrib=\"huc2\">10</criteria>"
		+ "	</logicalSet>"
		+ "</reachGroup>";

	public void testParse1() throws Exception {

		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest1));
		ReachGroup rg = new ReachGroup(1);
		reader.next();
		rg.parse(reader);

		assertEquals(1L, rg.getModelID());
		assertTrue(rg.isEnabled());
		assertEquals("Northern Indiana Plants", rg.getName());
		assertTrue(rg.getDescription().contains("Clean' Project") );
		assertTrue(rg.getNotes().contains("based on plant type"));

		// test adjustments
		List<Adjustment> adj = rg.getAdjustments();
		assertEquals(2, adj.size());
		Adjustment adj0 = adj.get(0);
		assertEquals(Integer.valueOf(5), adj0.getSource());
		assertEquals(.9, adj0.getCoefficient(), .00001);
		assertEquals(.75, adj.get(1).getCoefficient(), .00001);

		// test logical sets
		List<LogicalSet> lSets = rg.getLogicalSets();
		assertEquals(4, lSets.size());
		assertTrue(lSets.get(0).getCriteria().keySet().contains("huc8"));
		assertEquals("10040202", lSets.get(0).getCriteria().get("huc8"));
		assertEquals("101701", lSets.get(1).getCriteria().get("huc6"));

	}

	public void testParse2() throws Exception {
		String testRequest = "<reachGroup enabled=\"false\" name=\"Southern Indiana Fields\">"
			+ "	<desc>Fields in Southern Indiana</desc>"
			+ "	<notes>"
			+ "		The Farmer's Alminac says corn planting will be up 20% this year,"
			+ "		which will roughly result in a 5% increase in the aggrecultural source."
			+ "		This is an estimate so I'm leaving it out of the runs created	for the EPA."
			+ "	</notes>"
			+ "	<adjustment src=\"1\" coef=\"1.05\"/>"
			+ "	<logicalSet>"
			+ "		<criteria attrib=\"reach\" relation=\"upstream\">8346289</criteria>"
			+ "	</logicalSet>"
			+ "	<logicalSet>"
			+ "		<criteria attrib=\"reach\" relation=\"upstream\">9374562</criteria>"
			+ "	</logicalSet>"
			+ "</reachGroup>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup(1);
		reader.next();
		rg.parse(reader);

		assertFalse(rg.isEnabled());
		assertEquals("Southern Indiana Fields", rg.getName());
		assertEquals("Fields in Southern Indiana", rg.getDescription());
		assertTrue(rg.getNotes().contains("for the EPA"));
	}

	public void testParse3() throws Exception {
		String testRequest = "<reachGroup enabled=\"true\" name=\"Illinois\">"
			+ "	<desc>The entire state of Illinois</desc>"
			+ "	<notes>The Urban source for Illinois is predicted is to increase 20%.</notes>"
			+ "	<adjustment src=\"2\" coef=\"1.2\"/>"
			+ "	<logicalSet>"
			+ "		<criteria attrib=\"state-code\">il</criteria>"
			+ "	</logicalSet>"
			+ "</reachGroup>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup(1);
		reader.next();
		rg.parse(reader);

		assertTrue(rg.isEnabled());
		assertEquals("Illinois", rg.getName());
		assertEquals("The entire state of Illinois", rg.getDescription());
		assertTrue(rg.getNotes().contains("to increase 20%."));
	}

	public void testParse4() throws Exception {
		String testRequest = "<reachGroup enabled=\"true\" name=\"Illinois\">"
			+ "	<desc>Wisconsin River Plants</desc>"
			+ "	<notes>"
			+ "		We know of 3 plants on the Wisconsin River which have announced improved"
			+ "		BPM implementations."
			+ "	</notes>"
			+ "	<adjustment src=\"2\" coef=\".75\"/>"
			+ "	<reach id=\"483947453\" />"
			+ "	<reach id=\"947839474\" />"
			+ "</reachGroup>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup(1);
		reader.next();
		rg.parse(reader);

		assertTrue(rg.isEnabled());
		assertEquals("Illinois", rg.getName());
		assertEquals("Wisconsin River Plants", rg.getDescription());
		assertTrue(rg.getNotes().contains("River which have"));

		assertEquals(2, rg.getExplicitReaches().size());

		//Test the individual reaches
		ReachElement reach0 = rg.getExplicitReaches().get(0);
		ReachElement reach1 = rg.getExplicitReaches().get(1);
		assertEquals(new Long(483947453), reach0.getId());
		assertEquals(new Long(947839474), reach1.getId());
	}
}
