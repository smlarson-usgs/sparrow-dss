package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.sparrow.SparrowServiceTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.LogicalSet;
import org.junit.Test;

public class GetReachGroupsOverlappingLogicalSetLongRunTest extends SparrowServiceTestBaseWithDBandCannedModel50 {
	private static Long MODEL_ID = 50L;
//	private static Long REACH_ID_IN_GROUP = 661780L;
//	private static Long REACH_ID_OUTSIDE_GROUP = 10309L;
//	private static Long REACH_DOWNSTREAM = 7631l;
	private static String ADJUSTMENTS_XML = 
		"<adjustmentGroups conflicts=\"accumulate\">"+
		"<reachGroup enabled=\"true\" name=\"ECONFINA-STEINHATCHEE huc8 Group\"><desc></desc><notes></notes><logicalSet><criteria name=\"ECONFINA-STEINHATCHEE\" attrib=\"huc8\" relation=\"in\">03110102</criteria></logicalSet></reachGroup>"+
		"<reachGroup enabled=\"true\" name=\"OGEECHEE-SAVANNAH huc4 Group\"><desc></desc><notes></notes><logicalSet><criteria name=\"OGEECHEE-SAVANNAH\" attrib=\"huc4\" relation=\"in\">0306</criteria></logicalSet></reachGroup>"+
		"<reachGroup enabled=\"true\" name=\"Upstream of rch 661780 Group\"><desc></desc><notes></notes><logicalSet><criteria name=\"upstream of 661780\" attrib=\"reach\" relation=\"upstream\">661780</criteria></logicalSet></reachGroup>"+
		"<individualGroup enabled=\"true\"><reach id=\"661780\" name=\"FENHOLLOWAY R\"><adjustment src=\"1\" abs=\"1.00\"></adjustment></reach></individualGroup>"+
		"</adjustmentGroups>";
	
	private static String NEW_OVERLAPPING_HUC4_FRAGMENT = "<logicalSet>"
        + "<criteria attrib=\"huc4\">0311</criteria>"
        + "</logicalSet>";
	
	private static String NEW_OVERLAPPING_UPSTREAM_FRAGMENT = "<logicalSet>"+
	"<criteria name=\"upstream of 7631\" attrib=\"reach\" relation=\"upstream\">7631</criteria></logicalSet>";
	
	private static String NEW_NONOVERLAPPING_HUC4_FRAGMENT = "<logicalSet>"
        + "<criteria attrib=\"huc4\">0314</criteria>"
        + "</logicalSet>";

	@Test
	public void testGetGroups() throws Exception {
		AdjustmentGroups adjGroups1 = buildAdjGroups(MODEL_ID);

		//Test adding a huc set that overlaps 3/4 sets
		GetReachGroupsOverlappingLogicalSet action = new GetReachGroupsOverlappingLogicalSet(buildNewLogicalSet(MODEL_ID, NEW_OVERLAPPING_HUC4_FRAGMENT), adjGroups1);
		List<Criteria> results = action.run();
		boolean ecofinaHucFound = false;
		boolean ogeecheeHucFound = false;
		boolean individualGroupFound = false;
		boolean streamGroupFound = false;
		for(Criteria r : results) {
			if(r.getValue().equals("ECONFINA-STEINHATCHEE huc8 Group")) ecofinaHucFound = true;
			if(r.getValue().equals("OGEECHEE-SAVANNAH huc4 Group")) ogeecheeHucFound = true;
			if(r.getValue().equals("Upstream of rch 661780 Group")) streamGroupFound = true;
			if(r.getValue().equals("individual")) individualGroupFound = true;
		}
		assertEquals("3/4 overlap", results.size(), 3);
		assertTrue("HUCs overlap", ecofinaHucFound);
		assertTrue("HUCs overlap with individual reach", individualGroupFound);
		assertFalse("HUCs should not overlap", ogeecheeHucFound);
		assertTrue("HUCs overlap with upstream group", streamGroupFound);
		
		//test adding a huc set which should not overlap with anything
		GetReachGroupsOverlappingLogicalSet action2 = new GetReachGroupsOverlappingLogicalSet(buildNewLogicalSet(MODEL_ID, NEW_NONOVERLAPPING_HUC4_FRAGMENT), adjGroups1);
		List<Criteria> results2 = action2.run();
		assertEquals("No overlap", results2.size(), 0);
		
		//Test adding upstream group that should overlap
		//Test adding a huc set that overlaps 3/4 sets
		GetReachGroupsOverlappingLogicalSet action3 = new GetReachGroupsOverlappingLogicalSet(buildNewLogicalSet(MODEL_ID, NEW_OVERLAPPING_UPSTREAM_FRAGMENT), adjGroups1);
		List<Criteria> results3 = action3.run();
		boolean ecofinaHucFound3 = false;
		boolean ogeecheeHucFound3 = false;
		boolean individualGroupFound3 = false;
		boolean streamGroupFound3 = false;
		for(Criteria r : results3) {
			System.out.println(r.getValue());
			if(r.getValue().equals("ECONFINA-STEINHATCHEE huc8 Group")) ecofinaHucFound3 = true;
			if(r.getValue().equals("OGEECHEE-SAVANNAH huc4 Group")) ogeecheeHucFound3 = true;
			if(r.getValue().equals("Upstream of rch 661780 Group")) streamGroupFound3 = true;
			if(r.getValue().equals("individual")) individualGroupFound3 = true;
		}
		assertEquals("3/4 overlap", results3.size(), 3);
		assertTrue("HUCs overlap", ecofinaHucFound3);
		assertTrue("HUCs overlap with individual reach", individualGroupFound3);
		assertFalse("HUCs should not overlap", ogeecheeHucFound3);
		assertTrue("HUCs overlap with upstream group", streamGroupFound3);
	}
	
	public AdjustmentGroups buildAdjGroups(long modelId) throws Exception {
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(ADJUSTMENTS_XML));
		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
		reader.next();
		adjGroups = adjGroups.parse(reader);

		return adjGroups;
	}
	
	public LogicalSet buildNewLogicalSet(long modelId, String xml) throws Exception {
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
		LogicalSet ls = new LogicalSet(modelId);
		reader.next();
		ls = ls.parse(reader);

		return ls;
	}
}
