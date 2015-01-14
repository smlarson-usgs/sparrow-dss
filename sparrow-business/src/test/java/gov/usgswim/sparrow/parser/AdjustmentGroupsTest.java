package gov.usgswim.sparrow.parser;

import gov.usgswim.sparrow.test.SparrowTestBase;
import gov.usgswim.sparrow.domain.AdjustmentGroups;
import gov.usgswim.sparrow.domain.ReachGroup;

import java.io.StringReader;
import java.util.List;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamReader;

import org.junit.Test;
import static org.junit.Assert.*;

public class AdjustmentGroupsTest {

    public static final String VALID_REACH_GROUP_FRAG = ""
        + "<reachGroup enabled=\"true\" name=\"Northern Indiana Plants\">"
        + "  <desc>description</desc>"
        + "  <notes>notes</notes>"
        + "  <adjustment src=\"5\" coef=\"0.9\" />"
        + "  <reach id=\"12345\" />"
        + "  <logicalSet>"
        + "    <criteria attrib=\"huc2\">10</criteria>"
        + "  </logicalSet>"
        + "</reachGroup>";
	
    public static final String VALID_NO_ADJ_REACH_GROUP_FRAG = ""
        + "<reachGroup enabled=\"true\" name=\"Northern Indiana Plants\">"
        + "  <desc>description</desc>"
        + "  <notes>notes</notes>"
        + "  <reach id=\"12345\" />"
        + "  <logicalSet>"
        + "    <criteria attrib=\"huc2\">10</criteria>"
        + "  </logicalSet>"
        + "</reachGroup>";
	
    public static final String VALID_DISABLED_REACH_GROUP_FRAG = ""
        + "<reachGroup enabled=\"false\" name=\"Northern Indiana Plants\">"
        + "  <desc>description</desc>"
        + "  <notes>notes</notes>"
        + "  <adjustment src=\"5\" coef=\"0.9\" />"
        + "  <reach id=\"12345\" />"
        + "  <logicalSet>"
        + "    <criteria attrib=\"huc2\">10</criteria>"
        + "  </logicalSet>"
        + "</reachGroup>";
	
	
    public static final String VALID_DEFAULT_GROUP_FRAG = ""
        + "<default-group enabled=\"true\">"
        + "  <desc>description</desc>"
        + "  <notes>notes</notes>"
        + "  <adjustment src=\"5\" coef=\"0.9\" />"
        + "</default-group>";
	
    public static final String VALID_NO_ADJ_DEFAULT_GROUP_FRAG = ""
        + "<default-group enabled=\"true\">"
        + "  <desc>description</desc>"
        + "  <notes>notes</notes>"
        + "</default-group>";

	//Cannot disable the default group
//    public static final String VALID_DISABLED_DEFAULT_GROUP_FRAG = ""
//        + "<default-group enabled=\"false\">"
//        + "  <desc>description</desc>"
//        + "  <notes>notes</notes>"
//        + "  <adjustment src=\"5\" coef=\"0.9\" />"
//        + "</default-group>";

    public static final String VALID_INDIVIDUAL_GROUP_FRAG = ""
        + "<individualGroup enabled=\"true\">"
        + "  <reach id=\"12345\">"
        + "    <adjustment src=\"1\" abs=\"123\" />"
        + "  </reach>"
        + "</individualGroup>";
	
    public static final String VALID_NO_ADJ_INDIVIDUAL_GROUP_FRAG = ""
        + "<individualGroup enabled=\"true\">"
        + "</individualGroup>";
	
    public static final String VALID_DISABLED_INDIVIDUAL_GROUP_FRAG = ""
        + "<individualGroup enabled=\"false\">"
        + "  <reach id=\"12345\">"
        + "    <adjustment src=\"1\" abs=\"123\" />"
        + "  </reach>"
        + "</individualGroup>";
	
    /** Valid xml string represention of the adjustment groups. */
    public static final String VALID_FRAGMENT = ""
        + "<adjustmentGroups conflicts=\"supersede\">"
        + VALID_REACH_GROUP_FRAG
        + VALID_DEFAULT_GROUP_FRAG
        + VALID_INDIVIDUAL_GROUP_FRAG
        + "</adjustmentGroups>";
	
    /** Valid xml string represention of the adjustment groups. */
    public static final String VALID_NO_ADJ_FRAGMENT = ""
        + "<adjustmentGroups conflicts=\"supersede\">"
        + VALID_NO_ADJ_REACH_GROUP_FRAG
        + VALID_NO_ADJ_DEFAULT_GROUP_FRAG
        + VALID_NO_ADJ_INDIVIDUAL_GROUP_FRAG
        + "</adjustmentGroups>";
	
    /** Valid xml string represention of the adjustment groups. */
    public static final String VALID_DISABLED_FRAGMENT = ""
        + "<adjustmentGroups conflicts=\"supersede\">"
        + VALID_DISABLED_REACH_GROUP_FRAG
        + VALID_DISABLED_INDIVIDUAL_GROUP_FRAG
        + "</adjustmentGroups>";
	
    /** Valid xml string represention of the adjustment groups. */
    public static final String VALID_NULL_FRAGMENT = ""
        + "<adjustmentGroups conflicts=\"supersede\">"
        + "</adjustmentGroups>";

    /** Used to create XMLStreamReaders from XML strings. */
	protected XMLInputFactory inFact = XMLInputFactory.newInstance();

	// ============
	// TEST METHODS
	// ============
	@Test
	public void validateNormalAdjustmentGroupsWithEnabledAdjustments() throws Exception {

		AdjustmentGroups adjGroups = buildAdjGroups(1L, VALID_FRAGMENT);

		List<ReachGroup> reachGroups = adjGroups.getReachGroups();
		assertEquals("supersede", adjGroups.getConflicts());
		assertEquals(new Long(1L), adjGroups.getModelID());
		assertEquals(1, reachGroups.size());
		assertNotNull(adjGroups.getDefaultGroup());
		assertNotNull(adjGroups.getIndividualGroup());
		assertTrue(adjGroups.hasEnabledAdjustmentsAppliedToReaches());
		assertFalse(adjGroups.isLikelyReusable());
	}
	
	@Test
	public void validateAdjustmentGroupsWithNoAdjustments() throws Exception {

		AdjustmentGroups adjGroups = buildAdjGroups(1L, VALID_NO_ADJ_FRAGMENT);

		List<ReachGroup> reachGroups = adjGroups.getReachGroups();
		assertEquals("supersede", adjGroups.getConflicts());
		assertEquals(new Long(1L), adjGroups.getModelID());
		assertEquals(1, reachGroups.size());
		assertNotNull(adjGroups.getDefaultGroup());
		assertNotNull(adjGroups.getIndividualGroup());
		assertFalse(adjGroups.hasEnabledAdjustmentsAppliedToReaches());
		assertTrue(adjGroups.isLikelyReusable());
	}
	
	@Test
	public void validateAdjustmentGroupsWithDisabledFlagsSet() throws Exception {

		AdjustmentGroups adjGroups = buildAdjGroups(1L, VALID_DISABLED_FRAGMENT);

		List<ReachGroup> reachGroups = adjGroups.getReachGroups();
		assertEquals("supersede", adjGroups.getConflicts());
		assertEquals(new Long(1L), adjGroups.getModelID());
		assertEquals(1, reachGroups.size());
		assertNull(adjGroups.getDefaultGroup());
		assertNotNull(adjGroups.getIndividualGroup());
		assertFalse(adjGroups.hasEnabledAdjustmentsAppliedToReaches());
		assertTrue(adjGroups.isLikelyReusable());
	}
	
	@Test
	public void validateAdjustmentGroupsWithNullAdjustments() throws Exception {

		AdjustmentGroups adjGroups = buildAdjGroups(1L, VALID_NULL_FRAGMENT);

		List<ReachGroup> reachGroups = adjGroups.getReachGroups();
		assertEquals("supersede", adjGroups.getConflicts());
		assertEquals(new Long(1L), adjGroups.getModelID());
		assertEquals(0, reachGroups.size());
		assertNull(adjGroups.getDefaultGroup());
		assertNull(adjGroups.getIndividualGroup());
		assertFalse(adjGroups.hasEnabledAdjustmentsAppliedToReaches());
		assertTrue(adjGroups.isLikelyReusable());
	}

	@Test
	public void ensureThatClonedAdjGroupsHaveTheSameHashcode() throws Exception {
		AdjustmentGroups adjGroups1 = buildAdjGroups(1L, VALID_FRAGMENT);
		AdjustmentGroups adjGroups2 = buildAdjGroups(1L, VALID_FRAGMENT);
		SparrowTestBase.testHashCode(adjGroups1, adjGroups2, adjGroups1.clone());

		// test IDs
		assertEquals(adjGroups1.hashCode(), adjGroups1.getId().intValue());
		assertEquals(adjGroups2.hashCode(), adjGroups2.getId().intValue());
	}
	
	@Test
	public void ensureThatAdjustmentGroupsWithNullDisabledOrNoAjustmentsResultInTheSameHashCode() throws Exception {
		AdjustmentGroups disabledGrp = buildAdjGroups(1L, VALID_DISABLED_FRAGMENT);
		AdjustmentGroups noAdjGrp = buildAdjGroups(1L, VALID_NO_ADJ_FRAGMENT);
		AdjustmentGroups nullAdjGrp = buildAdjGroups(1L, VALID_NULL_FRAGMENT);
		
		SparrowTestBase.testHashCode(disabledGrp, noAdjGrp, nullAdjGrp, disabledGrp.clone(), noAdjGrp.clone(), disabledGrp.clone());

	}

	public AdjustmentGroups buildAdjGroups(long modelId, String xmlFragment) throws Exception {
		XMLStreamReader reader = inFact.createXMLStreamReader(new StringReader(xmlFragment));
		AdjustmentGroups adjGroups = new AdjustmentGroups(modelId);
		reader.next();
		adjGroups = adjGroups.parse(reader);

		// should have stopped at the end tag
		assertTrue(reader.getEventType() == XMLStreamConstants.END_ELEMENT);
		assertEquals(AdjustmentGroups.MAIN_ELEMENT_NAME, reader.getLocalName());

		return adjGroups;
	}
}
