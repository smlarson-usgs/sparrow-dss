package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.parser.AdjustmentGroupsTest;
import org.junit.Test;

public class GetReachGroupsContainingReachTest {
	
	private static Long REACH_ID_IN_GROUP = 12345L;
	private static Long REACH_ID_OUTSIDE_GROUP = 54321L;

	@Test
	public void testGetGroups() throws Exception {
		AdjustmentGroups adjGroups1 = buildAdjGroups(1L);
		
		GetReachGroupsContainingReach action = new GetReachGroupsContainingReach(REACH_ID_IN_GROUP, adjGroups1);
		List<Criteria> results = action.run();
		
		boolean indvidualGroupFound = false;
		boolean hucGroupFound = false;
		for(Criteria r : results) {
			if(r.getValue().equals("individual")) indvidualGroupFound = true;
			if(r.getValue().equals("Northern Indiana Plants")) hucGroupFound = true;
		}
		assertTrue("Reach found in individual group", indvidualGroupFound);
		assertTrue("Reach found in \"Northern Indiana Plants\" group", hucGroupFound);
		
		GetReachGroupsContainingReach action2 = new GetReachGroupsContainingReach(REACH_ID_OUTSIDE_GROUP, adjGroups1);
		List<Criteria> results2 = action2.run();
		
		boolean indvidualGroupFound2 = false;
		boolean hucGroupFound2 = false;
		for(Criteria r : results2) {
			if(r.getValue().equals("individual")) indvidualGroupFound2 = true;
			if(r.getValue().equals("Northern Indiana Plants")) hucGroupFound2 = true;
		}
		assertFalse("Reach should NOT be in individual group", indvidualGroupFound2);
		assertFalse("Reach should NOT be in \"Northern Indiana Plants\" group", hucGroupFound2);
	}
	
	public AdjustmentGroups buildAdjGroups(long modelId) throws Exception {
		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(AdjustmentGroupsTest.VALID_FRAGMENT));
		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
		reader.next();
		adjGroups = adjGroups.parse(reader);

		return adjGroups;
	}
}
