package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.sparrow.SparrowServiceTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.CriteriaRelationType;
import gov.usgswim.sparrow.domain.CriteriaType;

import org.junit.Test;

public class GetReachGroupsContainingReachLongRunTest extends SparrowServiceTestBaseWithDBandCannedModel50 {
	private static Long MODEL_ID = 50L;
	private static Long REACH_ID_IN_GROUP = 661780L;
	private static Long REACH_ID_OUTSIDE_GROUP = 10309L;
	private static String ADJUSTMENTS_XML = 
		"<adjustmentGroups conflicts=\"accumulate\">"+
		"<reachGroup enabled=\"true\" name=\"ECONFINA-STEINHATCHEE huc8 Group\"><desc></desc><notes></notes><logicalSet><criteria name=\"ECONFINA-STEINHATCHEE\" attrib=\"huc8\" relation=\"in\">03110102</criteria></logicalSet></reachGroup>"+
		"<reachGroup enabled=\"true\" name=\"Upstream of rch 661780 Group\"><desc></desc><notes></notes><logicalSet><criteria name=\"upstream of 661780\" attrib=\"reach\" relation=\"upstream\">661780</criteria></logicalSet></reachGroup>"+
		"<individualGroup enabled=\"true\"><reach id=\"661780\" name=\"FENHOLLOWAY R\"><adjustment src=\"1\" abs=\"1.00\"></adjustment></reach></individualGroup>"+
		"</adjustmentGroups>";
	
	@Test
	public void testReachInGroups() throws Exception {
		AdjustmentGroups huc8AndIndGroups = buildAdjGroups(MODEL_ID, ADJUSTMENTS_XML);
		
		GetReachGroupsContainingReach action = new GetReachGroupsContainingReach(REACH_ID_IN_GROUP, huc8AndIndGroups);
		List<Criteria> results = action.run();
		
		boolean indvidualGroupFound = false;
		boolean hucGroupFound = false;
		boolean upstreamGroupFound = false;
		for(Criteria r : results) {
			if(r.getValue().equals("individual")) indvidualGroupFound = true;
			if(r.getValue().equals("ECONFINA-STEINHATCHEE huc8 Group") && 
					r.getCriteriaType() == CriteriaType.HUC8 &&
					r.getRelation() == CriteriaRelationType.IN) hucGroupFound = true;
			if(r.getValue().equals("Upstream of rch 661780 Group") && 
					r.getCriteriaType() == CriteriaType.REACH &&
					r.getRelation() == CriteriaRelationType.UPSTREAM) upstreamGroupFound = true;
		}
		assertTrue("Reach found in individual group", indvidualGroupFound);
		assertTrue("Reach found in \"ECONFINA-STEINHATCHEE huc8 Group\" group", hucGroupFound);
		assertTrue("Reach found in \"Upstream of rch 661780 Group\" group", upstreamGroupFound);
		
		GetReachGroupsContainingReach action2 = new GetReachGroupsContainingReach(REACH_ID_OUTSIDE_GROUP, huc8AndIndGroups);
		List<Criteria> results2 = action2.run();
		
		boolean indvidualGroupFound2 = false;
		boolean hucGroupFound2 = false;
		boolean upstreamGroupFound2 = false;
		for(Criteria r : results2) {
			if(r.getValue().equals("individual")) indvidualGroupFound2 = true;
			if(r.getValue().equals("ECONFINA-STEINHATCHEE huc8 Group") && 
					r.getCriteriaType() == CriteriaType.HUC8 &&
					r.getRelation() == CriteriaRelationType.IN) hucGroupFound2 = true;
			if(r.getValue().equals("Upstream of rch 661780 Group") && 
					r.getCriteriaType() == CriteriaType.REACH &&
					r.getRelation() == CriteriaRelationType.UPSTREAM) upstreamGroupFound2 = true;
		}
		assertFalse("Reach should NOT be in individual group", indvidualGroupFound2);
		assertFalse("Reach should NOT be in \"ECONFINA-STEINHATCHEE huc8 Group\" group", hucGroupFound2);
		assertFalse("Reach should NOT be in \"Upstream of rch 661780 Group\" group", upstreamGroupFound2);
	}
	
	public AdjustmentGroups buildAdjGroups(long modelId, String xml) throws Exception {
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
		reader.next();
		adjGroups = adjGroups.parse(reader);

		return adjGroups;
	}
}
