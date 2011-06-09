package gov.usgswim.sparrow.action;

import static org.junit.Assert.*;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;

import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.Criteria;
import gov.usgswim.sparrow.domain.LogicalSet;
import gov.usgswim.sparrow.parser.AdjustmentGroupsTest;

import org.junit.Ignore;
import org.junit.Test;

@Ignore
public class GetReachGroupsOverlappingLogicalSetTest {
	
//	private static String NEW_OVERLAPPING_HUC4_FRAGMENT = "<logicalSet>"
//        + "<criteria attrib=\"huc4\">1011</criteria>"
//        + "</logicalSet>";
//	
//	private static String NEW_NONOVERLAPPING_UPSTREAM_FRAGMENT = "<logicalSet>"
//        + "<criteria attrib=\"reach\" relation=\"upstream\">999</criteria>"
//        + "</logicalSet>";
//	
//	private static String NEW_NONOVERLAPPING_HUC4_FRAGMENT = "<logicalSet>"
//        + "<criteria attrib=\"huc2\">11</criteria>"
//        + "</logicalSet>";
//
//	@Test
//	public void testGetGroups() throws Exception {
//		AdjustmentGroups adjGroups1 = buildAdjGroups(1L);
//		LogicalSet ls = buildNewLogicalSet(1l, NEW_OVERLAPPING_HUC4_FRAGMENT);
//		LogicalSet ls2 = buildNewLogicalSet(1l, NEW_NONOVERLAPPING_HUC4_FRAGMENT);
//		LogicalSet ls3 = buildNewLogicalSet(1l, NEW_NONOVERLAPPING_UPSTREAM_FRAGMENT);
//		
//		GetReachGroupsOverlappingLogicalSet action = new GetReachGroupsOverlappingLogicalSet(ls, adjGroups1);
//		List<Criteria> results = action.run();
//		
//		boolean northernIndianFound = false;
//		for(Criteria r : results) {
//			if(r.getValue().equals("Northern Indiana Plants")) northernIndianFound = true;
//		}
//		assertTrue("HUCs overlap", northernIndianFound);
//		
//		GetReachGroupsOverlappingLogicalSet action2 = new GetReachGroupsOverlappingLogicalSet(ls2, adjGroups1);
//		List<Criteria> results2 = action2.run();
//		
//		boolean northernIndianFound2 = false;
//		for(Criteria r : results2) {
//			if(r.getValue().equals("Northern Indiana Plants")) northernIndianFound2 = true;
//		}
//		assertFalse("HUCs DON'T overlap", northernIndianFound2);
//		
//		GetReachGroupsOverlappingLogicalSet action3 = new GetReachGroupsOverlappingLogicalSet(ls3, adjGroups1);
//		List<Criteria> results3 = action3.run();
//		
//		boolean northernIndianFound3 = false;
//		for(Criteria r : results3) {
//			if(r.getValue().equals("Northern Indiana Plants")) northernIndianFound3 = true;
//		}
//		assertFalse("HUCs and streams don't overlap", northernIndianFound3);
//	}
//	
//	public AdjustmentGroups buildAdjGroups(long modelId) throws Exception {
//		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(AdjustmentGroupsTest.VALID_FRAGMENT));
//		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
//		reader.next();
//		adjGroups = adjGroups.parse(reader);
//
//		return adjGroups;
//	}
//	
//	public LogicalSet buildNewLogicalSet(long modelId, String xml) throws Exception {
//		XMLStreamReader reader = XMLInputFactory.newInstance().createXMLStreamReader(new StringReader(xml));
//		LogicalSet ls = new LogicalSet(modelId);
//		reader.next();
//		ls = ls.parse(reader);
//
//		return ls;
//	}
}
