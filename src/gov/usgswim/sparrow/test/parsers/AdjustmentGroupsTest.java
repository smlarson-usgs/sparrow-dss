package gov.usgswim.sparrow.test.parsers;

import gov.usgswim.sparrow.parser.AdjustmentGroups;
import gov.usgswim.sparrow.parser.ReachGroup;
import gov.usgswim.sparrow.test.TestHelper;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import junit.framework.TestCase;

public class AdjustmentGroupsTest extends TestCase {
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();
	
	// ============
	// TEST METHODS
	// ============
	public void testParseMainUseCase() throws Exception {

		AdjustmentGroups adjGroups = buildAdjGroups(1L);
		
		List<ReachGroup> reachGroups = adjGroups.getReachGroups();
		assertEquals("supersede", adjGroups.getConflicts());
		assertEquals(new Long(1L), adjGroups.getModelID());
		assertEquals(4, reachGroups.size());
		
		assertEquals(2, adjGroups.getDefaultGroup().getAdjustments().size());
		assertEquals(true, adjGroups.getDefaultGroup().isEnabled());
	}
	
	
	public void testHashcode() throws Exception {	
		AdjustmentGroups adjGroups1 = buildAdjGroups(1L);
		AdjustmentGroups adjGroups2 = buildAdjGroups(1L);
		TestHelper.testHashCode(adjGroups1, adjGroups2, adjGroups1.clone());
	
		// test IDs
		assertEquals(adjGroups1.hashCode(), adjGroups1.getId().intValue());
		assertEquals(adjGroups2.hashCode(), adjGroups2.getId().intValue());
	}
	
	public AdjustmentGroups buildAdjGroups(long modelId) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(getTestRequest()));
		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
		reader.next();
		adjGroups = adjGroups.parse(reader);
		
		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(AdjustmentGroups.MAIN_ELEMENT_NAME, reader.getLocalName());
		
		return adjGroups;
	}
	
	public String getTestRequest() {
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
			+ "			<adjustment src=\"2\" abs=\".9\"/>"
			+ "		</reach>"
			+ "		<reach id=\"947839474\">"
			+ "			<adjustment src=\"2\" abs=\"91344\"/>"
			+ "		</reach>"
			+ "	</reach-group>"
			+ "<default-group enabled=\"true\"> <!--  DefaultGroup Object -->"
			+ "	<desc>Plants in Northern Indiana that are part of the 'Keep Gary Clean' Project</desc>"
			+ "	<notes>"
			+ "		I initially selected HUC 01746286 and 01746289,"
			+ "		but it looks like there are some others plants that need to be included."
			+ ""
			+ "		As a start, we are proposing a 10% reduction across the board,"
			+ "		but we will tailor this later based on plant type."
			+ "	</notes>"
			+ "	<adjustment src=\"5\" coef=\".9\"/>"
			+ "	<adjustment src=\"4\" coef=\".75\"/>"
			+ "</default-group>"
			+ "</adjustment-groups>";
		return testRequest;
	}
}
