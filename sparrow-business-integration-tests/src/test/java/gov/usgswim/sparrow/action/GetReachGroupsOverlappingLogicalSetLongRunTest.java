package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.sparrow.SparrowServiceTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ConflictingReachGroup;
import gov.usgswim.sparrow.domain.LogicalSet;
import org.junit.Test;

public class GetReachGroupsOverlappingLogicalSetLongRunTest extends SparrowServiceTestBaseWithDBandCannedModel50 {
	private static Long MODEL_ID = 50L;
	
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
		
		String adjustmentXml = SparrowTestBase.getXmlAsString(this.getClass(), null);
		AdjustmentGroups existingAdjustmentGroups = buildAdjGroups(MODEL_ID, adjustmentXml);
		LogicalSet newLogicalSetToCheck = buildNewLogicalSet(MODEL_ID, NEW_OVERLAPPING_HUC4_FRAGMENT);

		//Test adding a huc set that overlaps 3/4 sets
		GetReachGroupsOverlappingLogicalSet action = new GetReachGroupsOverlappingLogicalSet(newLogicalSetToCheck, existingAdjustmentGroups);
		List<ConflictingReachGroup> results = action.run();
		boolean ecofinaHucFound = false;
		boolean ogeecheeHucFound = false;
		boolean individualGroupFound = false;
		boolean streamGroupFound = false;
		for(ConflictingReachGroup r : results) {
			if(r.getGroupName().equals("ECONFINA-STEINHATCHEE huc8 Group")) ecofinaHucFound = true;
			if(r.getGroupName().equals("OGEECHEE-SAVANNAH huc4 Group")) ogeecheeHucFound = true;
			if(r.getGroupName().equals("Upstream of rch 661780 Group")) streamGroupFound = true;
			if(r.getGroupName().equals("Individual")) individualGroupFound = true;
		}
		assertEquals("3/4 overlap", results.size(), 3);
		assertTrue("HUCs overlap", ecofinaHucFound);
		assertTrue("HUCs overlap with individual reach", individualGroupFound);
		assertFalse("HUCs should not overlap", ogeecheeHucFound);
		assertTrue("HUCs overlap with upstream group", streamGroupFound);
		
		//test adding a huc set which should not overlap with anything
		GetReachGroupsOverlappingLogicalSet action2 = new GetReachGroupsOverlappingLogicalSet(buildNewLogicalSet(MODEL_ID, NEW_NONOVERLAPPING_HUC4_FRAGMENT), existingAdjustmentGroups);
		List<ConflictingReachGroup> results2 = action2.run();
		assertEquals("No overlap", results2.size(), 0);
		
		//Test adding upstream group that should overlap
		//Test adding a huc set that overlaps 3/4 sets
		GetReachGroupsOverlappingLogicalSet action3 = new GetReachGroupsOverlappingLogicalSet(buildNewLogicalSet(MODEL_ID, NEW_OVERLAPPING_UPSTREAM_FRAGMENT), existingAdjustmentGroups);
		List<ConflictingReachGroup> results3 = action3.run();
		boolean ecofinaHucFound3 = false;
		boolean ogeecheeHucFound3 = false;
		boolean individualGroupFound3 = false;
		boolean streamGroupFound3 = false;
		for(ConflictingReachGroup r : results3) {
			if(r.getGroupName().equals("ECONFINA-STEINHATCHEE huc8 Group")) ecofinaHucFound3 = true;
			if(r.getGroupName().equals("OGEECHEE-SAVANNAH huc4 Group")) ogeecheeHucFound3 = true;
			if(r.getGroupName().equals("Upstream of rch 661780 Group")) streamGroupFound3 = true;
			if(r.getGroupName().equals("Individual")) individualGroupFound3 = true;
		}
		assertEquals("3/4 overlap", results3.size(), 3);
		assertTrue("HUCs overlap", ecofinaHucFound3);
		assertTrue("HUCs overlap with individual reach", individualGroupFound3);
		assertFalse("HUCs should not overlap", ogeecheeHucFound3);
		assertTrue("HUCs overlap with upstream group", streamGroupFound3);
	}
	
	public AdjustmentGroups buildAdjGroups(long modelId, String xml) throws Exception {
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
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
