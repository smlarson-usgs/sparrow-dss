package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.*;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

/**
 * Tests everything at the ReachGroup level and below
 * @author eeverman
 *
 */
public class ReachGroupTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	public void testParse1() throws Exception {
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
		assertEquals(2, lSets.size());
		assertTrue(lSets.get(0).getCriteria().keySet().contains("huc8"));
		assertEquals("01746286", lSets.get(0).getCriteria().get("huc8"));
		assertEquals("01746289", lSets.get(1).getCriteria().get("huc8"));

	}

	public void testParse2() throws Exception {
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
		ReachGroup rg = new ReachGroup(1);
		reader.next();
		rg.parse(reader);

		assertFalse(rg.isEnabled());
		assertEquals("Southern Indiana Fields", rg.getName());
		assertEquals("Fields in Southern Indiana", rg.getDescription());
		assertTrue(rg.getNotes().contains("for the EPA"));
	}

	public void testParse3() throws Exception {
		String testRequest = "<reach-group enabled=\"true\" name=\"Illinois\">"
			+ "	<desc>The entire state of Illinois</desc>"
			+ "	<notes>The Urban source for Illinois is predicted is to increase 20%.</notes>"
			+ "	<adjustment src=\"2\" coef=\"1.2\"/>"
			+ "	<logical-set>"
			+ "		<criteria attrib=\"state-code\">il</criteria>"
			+ "	</logical-set>"
			+ "</reach-group>";
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
			+ "		<adjustment src=\"4\" coef=\".7\"/>"
			+ "	</reach>"
			+ "</reach-group>";
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
		Reach reach0 = rg.getExplicitReaches().get(0);
		Reach reach1 = rg.getExplicitReaches().get(1);
		assertEquals(new Long(483947453), reach0.getId());
		assertEquals(new Long(947839474), reach1.getId());

		//Test the adjustments applied to these specific reaches - Reach 0:
		assertEquals(new Integer(2), reach0.getAdjustments().get(0).getSource());
		assertEquals(new Double(.9), reach0.getAdjustments().get(0).getCoefficient());
		assertNull(reach0.getAdjustments().get(0).getAbsolute());

		//Test the adjustments applied to these specific reaches - Reach 1 (two adj's):
		assertEquals(new Integer(2), reach1.getAdjustments().get(0).getSource());
		assertNull(reach1.getAdjustments().get(0).getCoefficient());
		assertEquals(new Double(91344), reach1.getAdjustments().get(0).getAbsolute());
		assertEquals(new Integer(4), reach1.getAdjustments().get(1).getSource());
		assertEquals(new Double(.7), reach1.getAdjustments().get(1).getCoefficient());
		assertNull(reach1.getAdjustments().get(1).getAbsolute());
	}

	public void testParse5() throws Exception {
		String testRequest = "<reach-group enabled=\"true\" name=\"Illinois\">"
			+ "	<desc>Wisconsin River Plants</desc>"
			+ "	<notes>"
			+ "		We know of 3 plants on the Wisconsin River which have announced improved"
			+ "		BPM implementations."
			+ "	</notes>"
			+ "	<adjustment src=\"2\" coef=\".75\"/>"
			+ "	<reach id=\"947839474\">"
			+ "		<adjustment src=\"2\" abs=\"91344\" coef=\".7\"/>"
			+ "		<adjustment src=\"4\" coef=\".7\"/>"
			+ "	</reach>"
			+ "</reach-group>";
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		ReachGroup rg = new ReachGroup(1);
		reader.next();


		try {
			rg.parse(reader);
			fail("This test should have thrown an error b/c it spec's abs and coef values in an Adjustment");
		} catch (XMLParseValidationException e) {
			//Expected exception
		}

	}


}
