package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.sparrow.SparrowServiceTestBaseWithDBandCannedModel50;
import gov.usgswim.sparrow.SparrowTestBase;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ConflictingReachGroup;
import gov.usgswim.sparrow.domain.CriteriaType;

import org.junit.Test;

public class GetReachGroupsContainingReachLongRunTest extends SparrowServiceTestBaseWithDBandCannedModel50 {
	private static Long MODEL_ID = 50L;
	private static Long REACH_ID_IN_GROUP = 661780L;
	private static Long REACH_ID_OUTSIDE_GROUP = 10309L;
	
	@Test
	public void testReachInGroups() throws Exception {
		
		String adjustmentXml = SparrowTestBase.getXmlAsString(this.getClass(), null);
		AdjustmentGroups huc8AndIndGroups = buildAdjGroups(MODEL_ID, adjustmentXml);
		
		GetReachGroupsContainingReach action = new GetReachGroupsContainingReach(REACH_ID_IN_GROUP, huc8AndIndGroups);
		List<ConflictingReachGroup> results = action.run();
		
		boolean indvidualGroupFound = false;
		boolean hucGroupFound = false;
		boolean upstreamGroupFound = false;
		for(ConflictingReachGroup r : results) {
			if(r.getGroupName().equals("Individual")) indvidualGroupFound = true;
			if(r.getGroupName().equals("ECONFINA-STEINHATCHEE huc8 Group") && 
					r.getType().equals(CriteriaType.HUC8.toString())) hucGroupFound = true;
			if(r.getGroupName().equals("Upstream of rch 661780 Group") && 
					r.getType().equals(CriteriaType.REACH.toString())) upstreamGroupFound = true;
		}
		assertTrue("Reach found in individual group", indvidualGroupFound);
		assertTrue("Reach found in \"ECONFINA-STEINHATCHEE huc8 Group\" group", hucGroupFound);
		assertTrue("Reach found in \"Upstream of rch 661780 Group\" group", upstreamGroupFound);
		
		GetReachGroupsContainingReach action2 = new GetReachGroupsContainingReach(REACH_ID_OUTSIDE_GROUP, huc8AndIndGroups);
		List<ConflictingReachGroup> results2 = action2.run();
		
		boolean indvidualGroupFound2 = false;
		boolean hucGroupFound2 = false;
		boolean upstreamGroupFound2 = false;
		for(ConflictingReachGroup r : results2) {
			if(r.getGroupName().equals("individual")) indvidualGroupFound2 = true;
			if(r.getGroupName().equals("ECONFINA-STEINHATCHEE huc8 Group") && 
					r.getType().equals(CriteriaType.HUC8.toString())) hucGroupFound2 = true;
			if(r.getGroupName().equals("Upstream of rch 661780 Group") && 
					r.getType().equals(CriteriaType.REACH.toString())) upstreamGroupFound2 = true;
			if(r.getValue().equals("individual")) indvidualGroupFound2 = true;
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
