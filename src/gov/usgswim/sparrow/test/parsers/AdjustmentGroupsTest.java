package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.ReachGroup;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class AdjustmentGroupsTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	public void testParseMainUseCase() throws XMLStreamException {
		String testRequest = "<adjustment-groups conflicts=\"supersede\">	<!--  AdjustmentGroup Object?  Cached. -->"
		+ "	<reach-group enabled=\"true\" name=\"Northern Indiana Plants\"> <!--  ReachGroup Object -->"
		+ "		<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
		+ "		<notes>"
		+ "			I initially selected HUC 01746286 and 01746289,"
		+ "			but it looks like there are some others plants that need to be included."
		+ ""
		+ "			As a start, we are proposing a 10% reduction across the board,"
		+ "			but we will tailor this later based on plant type."
		+ "		</notes>"
		+ "		<!-- Multiple treatments are possible -->"
		+ "		<adjustment src=\"5\" coef=\".9\"/>	<!--  Existing Adjustment Object -->"
		+ "		<adjustment src=\"4\" coef=\".75\"/>"
		+ "		<logical-set>	<!--  LogicalSet Object?  (Hold Off) Used as cache key for a reach collection. -->"
		+ "			<criteria attrib=\"huc8\">01746286</criteria>"
		+ "		</logical-set>"
		+ "		<logical-set>"
		+ "			<criteria attrib=\"huc8\">01746289</criteria>"
		+ "		</logical-set>"
		+ "	</reach-group>"
		+ "	<reach-group enabled=\"false\" name=\"Southern Indiana Fields\">"
		+ "		<desc>Fields in Southern Indiana</desc>"
		+ "		<notes>"
		+ "			The Farmer's Alminac says corn planting will be up 20% this year,"
		+ "			which will roughly result in a 5% increase in the aggrecultural source."
		+ "			This is an estimate so I'm leaving it out of the runs created	for the EPA."
		+ "		</notes>"
		+ "		<adjustment src=\"1\" coef=\"1.05\"/>"
		+ "		<logical-set>"
		+ "			<criteria attrib=\"reach\" relation=\"upstream\">8346289</criteria>"
		+ "		</logical-set>"
		+ "		<logical-set>"
		+ "			<criteria attrib=\"reach\" relation=\"upstream\">9374562</criteria>"
		+ "		</logical-set>"
		+ "	</reach-group>"
		+ "	<reach-group enabled=\"true\" name=\"Illinois\">"
		+ "		<desc>The entire state of Illinois</desc>"
		+ "		<notes>The Urban source for Illinois is predicted is to increase 20%.</notes>"
		+ "		<adjustment src=\"2\" coef=\"1.2\"/>"
		+ "		<logical-set>"
		+ "			<criteria attrib=\"state-code\">il</criteria>"
		+ "		</logical-set>"
		+ "	</reach-group>"
		+ "	<reach-group enabled=\"true\" name=\"Illinois\">"
		+ "		<desc>Wisconsin River Plants</desc>"
		+ "		<notes>"
		+ "			We know of 3 plants on the Wisconsin River which have announced improved"
		+ "			BPM implementations."
		+ "		</notes>"
		+ "		<adjustment src=\"2\" coef=\".75\"/>"
		+ "		<reach id=\"483947453\">"
		+ "			<adjustment src=\"2\" coef=\".9\"/>"
		+ "		</reach>"
		+ "		<reach id=\"947839474\">"
		+ "			<adjustment src=\"2\" abs=\"91344\"/>"
		+ "		</reach>"
		+ "	</reach-group>"
		+ "	<!-- Do we still allow gross adjustments?  -->"
		+ "	<!-- <gross-src src=\"4\" coef=\"2\"/> -->"
		+ "</adjustment-groups>";
		
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(testRequest));
		AdjustmentGroups adjGroups = new AdjustmentGroups();
		reader.next();
		adjGroups.parse(reader);
		List<ReachGroup> reachGroups = adjGroups.getReachGroups();
		assertEquals("supersede", adjGroups.getConflicts());
		assertNull(adjGroups.getId());
		assertEquals(4, reachGroups.size());
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(AdjustmentGroups.MAIN_ELEMENT_NAME, reader.getLocalName());
	}
}
